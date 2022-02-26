package org.inferred.freebuilder.processor;

import static org.inferred.freebuilder.processor.model.ModelUtils.findAnnotationMirror;
import static org.inferred.freebuilder.processor.model.ModelUtils.findProperty;

import com.google.common.annotations.GwtCompatible;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import org.inferred.freebuilder.processor.Datatype.Visibility;
import org.inferred.freebuilder.processor.property.Property;
import org.inferred.freebuilder.processor.property.PropertyCodeGenerator;
import org.inferred.freebuilder.processor.source.Excerpt;
import org.inferred.freebuilder.processor.source.Excerpts;
import org.inferred.freebuilder.processor.source.QualifiedName;
import org.inferred.freebuilder.processor.source.SourceBuilder;
import org.inferred.freebuilder.processor.source.ValueType;
import org.inferred.freebuilder.processor.source.Variable;

class GwtSupport {

  private static final QualifiedName CUSTOM_FIELD_SERIALIZER =
      QualifiedName.of("com.google.gwt.user.client.rpc", "CustomFieldSerializer");
  private static final QualifiedName SERIALIZATION_EXCEPTION =
      QualifiedName.of("com.google.gwt.user.client.rpc", "SerializationException");
  private static final QualifiedName SERIALIZATION_STREAM_READER =
      QualifiedName.of("com.google.gwt.user.client.rpc", "SerializationStreamReader");
  private static final QualifiedName SERIALIZATION_STREAM_WRITER =
      QualifiedName.of("com.google.gwt.user.client.rpc", "SerializationStreamWriter");

  public static Datatype.Builder gwtMetadata(
      TypeElement type,
      Datatype datatype,
      Map<Property, PropertyCodeGenerator> generatorsByProperty) {
    Datatype.Builder extraMetadata = new Datatype.Builder();
    Optional<AnnotationMirror> annotation = findAnnotationMirror(type, GwtCompatible.class);
    if (annotation.isPresent()) {
      extraMetadata.addGeneratedBuilderAnnotations(Excerpts.add("@%s%n", GwtCompatible.class));
      Optional<AnnotationValue> serializable = findProperty(annotation.get(), "serializable");
      if (serializable.isPresent() && serializable.get().getValue().equals(Boolean.TRUE)) {
        // Due to a bug in GWT's handling of nested types, we have to declare Value as package
        // scoped so Value_CustomFieldSerializer can access it.
        extraMetadata.setValueTypeVisibility(Visibility.PACKAGE);
        extraMetadata.addValueTypeAnnotations(
            Excerpts.add("@%s(serializable = true)%n", GwtCompatible.class));
        extraMetadata.addNestedClasses(new CustomValueSerializer(datatype, generatorsByProperty));
        extraMetadata.addNestedClasses(new GwtWhitelist(datatype, generatorsByProperty.keySet()));
      }
    }
    return extraMetadata;
  }

  private static final class CustomValueSerializer extends ValueType implements Excerpt {

    private final Datatype datatype;
    private final Map<Property, PropertyCodeGenerator> generatorsByProperty;

    private CustomValueSerializer(
        Datatype datatype, Map<Property, PropertyCodeGenerator> generatorsByProperty) {
      this.datatype = datatype;
      this.generatorsByProperty = generatorsByProperty;
    }

    @Override
    public void addTo(SourceBuilder code) {
      code.addLine("").addLine("@%s", GwtCompatible.class);
      if (datatype.getType().isParameterized()) {
        code.addLine("@%s(\"unchecked\")", SuppressWarnings.class);
      }
      code.addLine("public static class Value_CustomFieldSerializer")
          .addLine("    extends %s<%s> {", CUSTOM_FIELD_SERIALIZER, datatype.getValueType())
          .addLine("")
          .addLine("  @%s", Override.class)
          .addLine(
              "  public void deserializeInstance(%s reader, %s instance) { }",
              SERIALIZATION_STREAM_READER, datatype.getValueType())
          .addLine("")
          .addLine("  @%s", Override.class)
          .addLine("  public boolean hasCustomInstantiateInstance() {")
          .addLine("    return true;")
          .addLine("  }");
      addInstantiateInstance(code);
      addSerializeInstance(code);
      code.addLine("")
          .addLine(
              "  private static final Value_CustomFieldSerializer INSTANCE ="
                  + " new Value_CustomFieldSerializer();")
          .addLine("")
          .addLine(
              "  public static void deserialize(%s reader, %s instance) {",
              SERIALIZATION_STREAM_READER, datatype.getValueType())
          .addLine("    INSTANCE.deserializeInstance(reader, instance);")
          .addLine("  }")
          .addLine("")
          .addLine(
              "  public static %s instantiate(%s reader)",
              datatype.getValueType(), SERIALIZATION_STREAM_READER)
          .addLine("      throws %s {", SERIALIZATION_EXCEPTION)
          .addLine("    return INSTANCE.instantiateInstance(reader);")
          .addLine("  }")
          .addLine("")
          .addLine(
              "  public static void serialize(%s writer, %s instance)",
              SERIALIZATION_STREAM_WRITER, datatype.getValueType())
          .addLine("      throws %s {", SERIALIZATION_EXCEPTION)
          .addLine("    INSTANCE.serializeInstance(writer, instance);")
          .addLine("  }")
          .addLine("}");
    }

    private void addInstantiateInstance(SourceBuilder code) {
      Variable builder = new Variable("builder");
      code.addLine("")
          .addLine("  @%s", Override.class)
          .addLine(
              "  public %s instantiateInstance(%s reader) throws %s {",
              datatype.getValueType(), SERIALIZATION_STREAM_READER, SERIALIZATION_EXCEPTION)
          .addLine("    %1$s %2$s = new %1$s();", datatype.getBuilder(), builder);
      for (Property property : generatorsByProperty.keySet()) {
        Variable temporary = new Variable(property.getName());
        if (property.getType().getKind().isPrimitive()) {
          code.addLine(
              "    %s %s = reader.read%s();",
              property.getType(), temporary, withInitialCapital(property.getType()));
          generatorsByProperty.get(property).addSetFromResult(code, builder, temporary);
        } else if (String.class.getName().equals(property.getType().toString())) {
          code.addLine("    %s %s = reader.readString();", property.getType(), temporary);
          generatorsByProperty.get(property).addSetFromResult(code, builder, temporary);
        } else {
          code.addLine("    try {");
          if (!property.isFullyCheckedCast()) {
            code.addLine("      @SuppressWarnings(\"unchecked\")");
          }
          code.addLine(
              "      %1$s %2$s = (%1$s) reader.readObject();", property.getType(), temporary);
          generatorsByProperty.get(property).addSetFromResult(code, builder, temporary);
          code.addLine("    } catch (%s e) {", ClassCastException.class)
              .addLine("      throw new %s(", SERIALIZATION_EXCEPTION)
              .addLine("          \"Wrong type for property '%s'\", e);", property.getName())
              .addLine("    }");
        }
      }
      code.addLine(
              "    return (%s) %s.%s();",
              datatype.getValueType(), builder, datatype.getBuildMethod().name())
          .addLine("  }");
    }

    private void addSerializeInstance(SourceBuilder code) {
      code.addLine("")
          .addLine("  @%s", Override.class)
          .addLine(
              "  public void serializeInstance(%s writer, %s instance)",
              SERIALIZATION_STREAM_WRITER, datatype.getValueType())
          .addLine("      throws %s {", SERIALIZATION_EXCEPTION);
      for (Property property : generatorsByProperty.keySet()) {
        if (property.getType().getKind().isPrimitive()) {
          code.add("    writer.write%s(", withInitialCapital(property.getType()));
        } else if (String.class.getName().equals(property.getType().toString())) {
          code.add("    writer.writeString(");
        } else {
          code.add("    writer.writeObject(");
        }
        generatorsByProperty
            .get(property)
            .addReadValueFragment(code, property.getField().on("instance"));
        code.add(");\n");
      }
      code.addLine("  }");
    }

    @Override
    protected void addFields(FieldReceiver fields) {
      fields.add("datatype", datatype);
      fields.add("generatorsByProperty", generatorsByProperty);
    }
  }

  private static final class GwtWhitelist extends ValueType implements Excerpt {

    private final Datatype datatype;
    private final Collection<Property> properties;

    private GwtWhitelist(Datatype datatype, Collection<Property> properties) {
      this.datatype = datatype;
      this.properties = properties;
    }

    @Override
    public void addTo(SourceBuilder code) {
      code.addLine("")
          .addLine("/** This class exists solely to ensure GWT whitelists all required types. */")
          .addLine("@%s(serializable = true)", GwtCompatible.class)
          .addLine(
              "static final class GwtWhitelist%s %s %s {",
              datatype.getType().declarationParameters(),
              datatype.isInterfaceType() ? "implements " : "extends ",
              datatype.getType())
          .addLine("");
      for (Property property : properties) {
        code.addLine("  %s %s;", property.getType(), property.getField());
      }
      code.addLine("")
          .addLine("  private GwtWhitelist() {")
          .addLine("    throw new %s();", UnsupportedOperationException.class)
          .addLine("   }");
      for (Property property : properties) {
        code.addLine("")
            .addLine("  @%s", Override.class)
            .addLine("  public %s %s() {", property.getType(), property.getGetterName())
            .addLine("    throw new %s();", UnsupportedOperationException.class)
            .addLine("  }");
      }
      code.addLine("}");
    }

    @Override
    protected void addFields(FieldReceiver fields) {
      fields.add("datatype", datatype);
      fields.add("generatorsByProperty", properties);
    }
  }

  private static String withInitialCapital(Object obj) {
    String s = obj.toString();
    return s.substring(0, 1).toUpperCase() + s.substring(1);
  }
}
