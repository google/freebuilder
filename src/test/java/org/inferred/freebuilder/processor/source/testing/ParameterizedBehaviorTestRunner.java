package org.inferred.freebuilder.processor.source.testing;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.getOnlyElement;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.processing.Processor;
import javax.tools.JavaFileObject;
import org.inferred.freebuilder.processor.source.feature.FeatureSet;
import org.inferred.freebuilder.processor.source.testing.BehaviorTester.CompilationSubject;
import org.inferred.freebuilder.processor.source.testing.ParameterizedBehaviorTestFactory.Shared;
import org.inferred.freebuilder.processor.source.testing.TestBuilder.TestSource;
import org.junit.rules.ExpectedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.parameterized.BlockJUnit4ClassRunnerWithParameters;
import org.junit.runners.parameterized.TestWithParameters;

/** Test runner for {@link ParameterizedBehaviorTestRunner}. */
class ParameterizedBehaviorTestRunner extends BlockJUnit4ClassRunnerWithParameters {

  interface TestSupplier {
    Object get() throws Exception;
  }

  /** Error thrown if no tests can share a compiler. */
  private static final String MERGE_FAILURE_MESSAGE =
      "Failed to merge any test compilation steps; "
          + "have you correctly defined equals and hashCode on your processor?";
  /** Error thrown by {@link ExpectedException} if no {@link CompilationException} is thrown. */
  private static final String COMPILATION_FAILURE_EXPECTED =
      "Expected test to throw an instance of " + CompilationException.class.getName();

  /** The &#64;Shared BehaviorTester field on the test class. */
  private final Field testerField;
  /** The "Introspection" test, which fails if no tests can share a compiler. */
  private final Description introspection;

  private final FeatureSet features;

  /** Metadata about the child tests, built by {@link #getChildMetadata()}. */
  private List<Child> children;
  /** The BehaviorTester to inject into test fixtures created by {@link #createTest()}. */
  private BehaviorTester tester;

  ParameterizedBehaviorTestRunner(TestWithParameters test) throws InitializationError {
    super(test);

    features = FluentIterable.from(test.getParameters()).filter(FeatureSet.class).first().orNull();
    checkState(features != null, "No FeatureSet parameter found");

    List<FrameworkField> testerFields = getTestClass().getAnnotatedFields(Shared.class);
    if (testerFields.isEmpty()) {
      throw new InitializationError("No public @Shared field found");
    } else if (testerFields.size() > 1) {
      throw new InitializationError("Multiple public @Shared fields found");
    }
    FrameworkField frameworkField = getOnlyElement(testerFields);
    if (!frameworkField.isPublic()) {
      throw new InitializationError("@Shared field " + frameworkField + " must be public");
    }
    if (!frameworkField.getType().isAssignableFrom(BehaviorTester.class)) {
      throw new InitializationError(
          String.format(
              "@Shared field %s must be of type %s",
              frameworkField, BehaviorTester.class.getSimpleName()));
    }
    testerField = frameworkField.getField();
    introspection =
        Description.createTestDescription(
            getTestClass().getJavaClass(), "Introspect" + test.getParameters());
  }

  /**
   * Returns a {@link Description} showing the {@link #introspection} task, plus the children of
   * this runner.
   */
  @Override
  public Description getDescription() {
    Description originalDescription = super.getDescription();
    if (originalDescription.getChildren().size() > 1) {
      Description suiteDescription = originalDescription.childlessCopy();
      suiteDescription.addChild(introspection);
      originalDescription.getChildren().forEach(suiteDescription::addChild);
      return suiteDescription;
    } else {
      // Allow individual tests to be rerun without adding the "Introspect" task.
      return originalDescription;
    }
  }

  /** Returns a statement that runs all children with compilations coalesced for performance. */
  @Override
  public Statement childrenInvoker(RunNotifier notifier) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        runChildren(notifier);
      }
    };
  }

  /** Runs all children with compilations coalesced for performance. */
  private void runChildren(RunNotifier notifier) throws Throwable {
    if (super.getDescription().getChildren().size() > 1) {
      Queue<SharedCompiler> sharedCompilers = getSharedCompilers(notifier);
      while (!sharedCompilers.isEmpty()) {
        // Removing each shared compiler from the queue as we start it ensures it can be
        // garbage-collected as soon as we're finished with it.
        SharedCompiler sharedCompiler = sharedCompilers.remove();
        for (FrameworkMethod child : sharedCompiler.children) {
          runChild(notifier, sharedCompiler, child);
        }
      }
    } else {
      // Rerun individual tests with the default BehaviorTester.
      tester = BehaviorTester.create(features);
      try {
        super.childrenInvoker(notifier).evaluate();
      } finally {
        tester = null;
      }
    }
  }

  /** Determines how many compilers we need and which children to pass them to. */
  private Queue<SharedCompiler> getSharedCompilers(RunNotifier notifier) throws Throwable {
    notifier.fireTestStarted(introspection);
    try {
      List<Child> children = getChildMetadata();
      Queue<SharedCompiler> sharedCompilers = shareCompilers(children);
      verifyCompilerShared(notifier, children.size(), sharedCompilers.size());
      notifier.fireTestFinished(introspection);
      return sharedCompilers;
    } catch (Throwable t) {
      notifier.fireTestFailure(new Failure(introspection, t));
      throw t;
    }
  }

  /** Pre-runs children to find out what they want to compile. */
  private List<Child> getChildMetadata() throws Throwable {
    children = new ArrayList<>();
    try {
      Statement childrenInvoker = super.childrenInvoker(new RunNotifier() {});
      childrenInvoker.evaluate();
      return children;
    } finally {
      children = null;
    }
  }

  /**
   * Pre-runs child, injecting a {@link Child} behavior tester to determine what they pass to the
   * compiler.
   */
  @Override
  public void runChild(FrameworkMethod method, RunNotifier notifier) {
    if (tester != null) {
      // Only one child, no pre-run needed.
      super.runChild(method, notifier);
      return;
    }
    Child child = new Child(method);
    tester = child;
    try {
      super.runChild(
          method,
          new RunNotifier() {
            @Override
            public void fireTestFailure(Failure failure) {
              if (COMPILATION_FAILURE_EXPECTED.equals(failure.getMessage())) {
                // Test is expecting compilation to fail, so merging with other tests will only
                // propagate the failure. Mark the test as unmergeable.
                child.unmergeable = true;
              }
            }
          });
    } finally {
      tester = null;
      children.add(child);
    }
  }

  /** Runs {@code child} with {@code sharedCompiler}. */
  private void runChild(
      RunNotifier notifier, SharedCompiler sharedCompiler, FrameworkMethod child) {
    tester = sharedCompiler.behaviorTester();
    try {
      super.runChild(child, notifier);
    } finally {
      tester = null;
    }
  }

  /**
   * Returns a new fixture for running a test. Executes the test class's no-argument constructor,
   * then injects the correct BehaviorTester implementation.
   */
  @Override
  public Object createTest() throws Exception {
    checkState(tester != null);
    Object test = super.createTest();
    // Inject the correct BehaviorTester implementation.
    testerField.set(test, tester);
    return test;
  }

  /** Groups children that can share a compiler. */
  private Queue<SharedCompiler> shareCompilers(List<Child> children) {
    Queue<SharedCompiler> sharedCompilers = new ArrayDeque<>();
    for (Child child : children) {
      Optional<SharedCompiler> sharedCompiler =
          sharedCompilers.stream().filter(c -> c.canShareCompiler(child)).findAny();
      if (sharedCompiler.isPresent()) {
        sharedCompiler.get().addChild(child);
      } else {
        sharedCompilers.add(new SharedCompiler(child, features));
      }
    }
    return sharedCompilers;
  }

  /** Fails the {@link #introspection} test unless we managed to share a compiler. */
  private void verifyCompilerShared(RunNotifier notifier, int numChildren, int numCompilations) {
    int numFilteredChildren = super.getDescription().getChildren().size();
    if (numFilteredChildren == numChildren) {
      if (numChildren == numCompilations) {
        notifier.fireTestFailure(
            new Failure(introspection, new AssertionError(MERGE_FAILURE_MESSAGE)));
      }
    }
  }

  /**
   * A BehaviorTester that just remembers the processors, units and tests it was asked to compile.
   */
  private static class Child implements BehaviorTester {

    final FrameworkMethod method;
    boolean unmergeable = false;
    final Set<Processor> processors = new LinkedHashSet<>();
    final Set<Package> permittedPackages = new LinkedHashSet<>();
    final List<JavaFileObject> compilationUnits = new ArrayList<>();
    final List<TestSource> testSources = new ArrayList<>();

    Child(FrameworkMethod method) {
      this.method = method;
    }

    @Override
    public String toString() {
      return method.getName();
    }

    @Override
    public BehaviorTester with(Processor processor) {
      processors.add(processor);
      return this;
    }

    @Override
    public BehaviorTester with(JavaFileObject compilationUnit) {
      compilationUnits.add(compilationUnit);
      return this;
    }

    @Override
    public BehaviorTester with(TestSource testSource) {
      testSources.add(testSource);
      return this;
    }

    @Override
    public BehaviorTester withPermittedPackage(Package pkg) {
      permittedPackages.add(pkg);
      return this;
    }

    @Override
    public BehaviorTester withContextClassLoader() {
      return this;
    }

    @Override
    public CompilationFailureSubject failsToCompile() {
      unmergeable = true;
      return new CompilationFailureSubject() {
        @Override
        public CompilationFailureSubject withErrorThat(
            Consumer<DiagnosticSubject> diagnosticAssertions) {
          return this;
        }
      };
    }

    @Override
    public CompilationSubject compiles() {
      return new CompilationSubject() {
        @Override
        public CompilationSubject withNoWarnings() {
          return this;
        }

        @Override
        public CompilationSubject withWarningThat(
            Consumer<DiagnosticSubject> diagnosticAssertions) {
          unmergeable = true;
          return this;
        }

        @Override
        public CompilationSubject allTestsPass() {
          return this;
        }

        @Override
        public CompilationSubject testsPass(
            Iterable<? extends TestSource> testSources, boolean shouldSetContextClassLoader) {
          return this;
        }
      };
    }
  }

  /** A compiler that can be shared between a set of children. */
  private static class SharedCompiler {
    private final Set<FrameworkMethod> children = new LinkedHashSet<>();
    final boolean unmergeable;
    Set<Processor> processors;
    final Map<String, JavaFileObject> compilationUnits = new LinkedHashMap<>();
    final List<TestSource> testSources = new ArrayList<>();
    private CompilationSubject subject;
    private RuntimeException compilationException;
    private final FeatureSet features;
    private final Set<Package> permittedPackages = new LinkedHashSet<>();

    SharedCompiler(Child child, FeatureSet features) {
      children.add(child.method);
      unmergeable = child.unmergeable;
      processors = ImmutableSet.copyOf(child.processors);
      for (JavaFileObject compilationUnit : child.compilationUnits) {
        compilationUnits.put(compilationUnit.getName(), compilationUnit);
      }
      permittedPackages.addAll(child.permittedPackages);
      testSources.addAll(child.testSources);
      this.features = features;
    }

    BehaviorTester behaviorTester() {
      if (children.size() > 1) {
        return new DelegatingBehaviorTester(this, features);
      } else {
        return new SingleBehaviorTester(features);
      }
    }

    boolean canShareCompiler(Child child) {
      if (unmergeable || child.unmergeable) {
        return false;
      }
      if (!processors.equals(child.processors)) {
        return false;
      }
      for (JavaFileObject otherUnit : child.compilationUnits) {
        JavaFileObject unit = compilationUnits.get(otherUnit.getName());
        if (unit != null && !unit.equals(otherUnit)) {
          return false;
        }
      }
      return true;
    }

    SharedCompiler addChild(Child child) {
      children.add(child.method);
      for (JavaFileObject compilationUnit : child.compilationUnits) {
        compilationUnits.put(compilationUnit.getName(), compilationUnit);
      }
      for (Package pkg : child.permittedPackages) {
        permittedPackages.add(pkg);
      }
      testSources.addAll(child.testSources);
      return this;
    }

    public CompilationSubject compiles() {
      if (compilationException != null) {
        throw compilationException;
      }
      if (subject == null) {
        SingleBehaviorTester tester = new SingleBehaviorTester(features);
        for (Processor processor : processors) {
          tester.with(processor);
        }
        processors = null; // Allow compilers to be reclaimed by GC (processors retain a reference)
        for (JavaFileObject compilationUnit : compilationUnits.values()) {
          tester.with(compilationUnit);
        }
        for (TestSource testSource : testSources) {
          tester.with(testSource);
        }
        for (Package pkg : permittedPackages) {
          tester.withPermittedPackage(pkg);
        }
        try {
          subject = tester.compiles();
        } catch (RuntimeException e) {
          compilationException = e;
          throw e;
        }
      }
      return subject;
    }
  }

  /**
   * A BehaviorTester that uses a shared {@link SharedCompiler} to reduce overheads, but can fall
   * back to an unshared compiler if compilation fails, to preserve test isolation.
   */
  private static class DelegatingBehaviorTester implements BehaviorTester {

    private final SharedCompiler sharedCompiler;
    private final SingleBehaviorTester fallbackCompiler;
    private final List<TestSource> testSources = new ArrayList<>();
    private boolean shouldSetContextClassLoader = false;

    DelegatingBehaviorTester(SharedCompiler sharedCompiler, FeatureSet features) {
      this.sharedCompiler = sharedCompiler;
      this.fallbackCompiler = new SingleBehaviorTester(features);
    }

    @Override
    public BehaviorTester with(Processor processor) {
      fallbackCompiler.with(processor);
      return this;
    }

    @Override
    public BehaviorTester with(JavaFileObject compilationUnit) {
      fallbackCompiler.with(compilationUnit);
      return this;
    }

    @Override
    public BehaviorTester with(TestSource testSource) {
      testSources.add(testSource);
      fallbackCompiler.with(testSource);
      return this;
    }

    @Override
    public BehaviorTester withPermittedPackage(Package pkg) {
      fallbackCompiler.withPermittedPackage(pkg);
      return this;
    }

    @Override
    public BehaviorTester withContextClassLoader() {
      shouldSetContextClassLoader = true;
      fallbackCompiler.withContextClassLoader();
      return this;
    }

    @Override
    public CompilationFailureSubject failsToCompile() {
      throw new UnsupportedOperationException();
    }

    @Override
    public CompilationSubject compiles() {
      try {
        CompilationSubject assertCompiled = sharedCompiler.compiles();
        return new CompilationSubject() {
          @Override
          public CompilationSubject withNoWarnings() {
            assertCompiled.withNoWarnings();
            return this;
          }

          @Override
          public CompilationSubject withWarningThat(
              Consumer<DiagnosticSubject> diagnosticAssertions) {
            throw new UnsupportedOperationException();
          }

          @Override
          public CompilationSubject allTestsPass() {
            assertCompiled.testsPass(testSources, shouldSetContextClassLoader);
            return this;
          }

          @Override
          public CompilationSubject testsPass(
              Iterable<? extends TestSource> testSources, boolean shouldSetContextClassLoader) {
            assertCompiled.testsPass(testSources, shouldSetContextClassLoader);
            return this;
          }
        };
      } catch (RuntimeException e) {
        return fallbackCompiler.compiles();
      }
    }
  }
}
