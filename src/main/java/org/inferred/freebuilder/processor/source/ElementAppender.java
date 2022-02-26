package org.inferred.freebuilder.processor.source;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.SimpleElementVisitor8;

class ElementAppender extends SimpleElementVisitor8<Void, QualifiedNameAppendable> {
  private static final ElementAppender INSTANCE = new ElementAppender();

  public static void appendShortened(Element arg, QualifiedNameAppendable source) {
    INSTANCE.visit(arg, source);
  }

  private ElementAppender() {}

  @Override
  public Void visitPackage(PackageElement pkg, QualifiedNameAppendable a) {
    a.append(pkg.getQualifiedName());
    return null;
  }

  @Override
  public Void visitType(TypeElement type, QualifiedNameAppendable a) {
    a.append(QualifiedName.of(type));
    return null;
  }

  @Override
  protected Void defaultAction(Element e, QualifiedNameAppendable a) {
    a.append(e.toString());
    return null;
  }
}
