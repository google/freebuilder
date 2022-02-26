package org.inferred.freebuilder.processor.source.feature;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import org.inferred.freebuilder.processor.source.Excerpt;
import org.inferred.freebuilder.processor.source.QualifiedName;
import org.inferred.freebuilder.processor.source.SourceBuilder;

/** Types from JSR 305, if available. Not available by default in tests. */
public enum Jsr305 implements Feature<Jsr305> {
  AVAILABLE("JSR 305"),
  UNAVAILABLE("No JSR 305");

  /**
   * Constant to pass to {@link SourceBuilder#feature(FeatureType)} to get the current status of
   * {@link Jsr305}.
   */
  public static final FeatureType<Jsr305> JSR305 =
      new FeatureType<Jsr305>() {

        @Override
        protected Jsr305 testDefault(FeatureSet features) {
          return UNAVAILABLE;
        }

        @Override
        protected Jsr305 forEnvironment(ProcessingEnvironment env, FeatureSet features) {
          return hasType(env.getElementUtils(), NULLABLE) ? AVAILABLE : UNAVAILABLE;
        }
      };

  /** Excerpt that adds a JSR-303 Nullable annotation, if available. */
  public static Excerpt nullable() {
    return new NullableExcerpt();
  }

  private static class NullableExcerpt implements Excerpt {

    @Override
    public void addTo(SourceBuilder source) {
      switch (source.feature(JSR305)) {
        case AVAILABLE:
          source.add("@%s", NULLABLE);
          break;

        default:
          break;
      }
    }

    @Override
    public String toString() {
      return "@Nullable";
    }
  }

  private static final QualifiedName NULLABLE = QualifiedName.of("javax.annotation", "Nullable");

  private final String humanReadableFormat;

  Jsr305(String humanReadableFormat) {
    this.humanReadableFormat = humanReadableFormat;
  }

  @Override
  public String toString() {
    return humanReadableFormat;
  }

  private static boolean hasType(Elements elements, QualifiedName type) {
    try {
      return elements.getTypeElement(type.toString()) != null;
    } catch (RuntimeException e) {
      // Work around Eclipse bug
      return false;
    }
  }
}
