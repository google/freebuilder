// Autogenerated code. Do not modify.
package org.inferred.freebuilder.processor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import org.inferred.freebuilder.processor.util.Excerpt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Generated;
import javax.annotation.Nullable;
import javax.lang.model.type.TypeMirror;

/**
 * Auto-generated superclass of {@link Metadata.Property.Builder},
 * derived from the API of {@link Metadata.Property}.
 */
@Generated("org.inferred.freebuilder.processor.CodeGenerator")
abstract class Metadata_Property_Builder {

  private static final Joiner COMMA_JOINER = Joiner.on(", ").skipNulls();

  private enum Property {
    TYPE("type"),
    NAME("name"),
    CAPITALIZED_NAME("capitalizedName"),
    ALL_CAPS_NAME("allCapsName"),
    GETTER_NAME("getterName"),
    FULLY_CHECKED_CAST("fullyCheckedCast"),
    ;

    private final String name;

    private Property(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  private TypeMirror type;
  @Nullable private TypeMirror boxedType = null;
  private String name;
  private String capitalizedName;
  private String allCapsName;
  private String getterName;
  @Nullable private PropertyCodeGenerator codeGenerator = null;
  private boolean fullyCheckedCast;
  private final ArrayList<Excerpt> accessorAnnotations = new ArrayList<Excerpt>();
  private final EnumSet<Metadata_Property_Builder.Property> _unsetProperties =
      EnumSet.allOf(Metadata_Property_Builder.Property.class);

  /**
   * Sets the value to be returned by {@link Metadata.Property#getType()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code type} is null
   */
  public Metadata.Property.Builder setType(TypeMirror type) {
    this.type = Preconditions.checkNotNull(type);
    _unsetProperties.remove(Metadata_Property_Builder.Property.TYPE);
    return (Metadata.Property.Builder) this;
  }

  /**
   * Returns the value that will be returned by {@link Metadata.Property#getType()}.
   *
   * @throws IllegalStateException if the field has not been set
   */
  public TypeMirror getType() {
    Preconditions.checkState(
        !_unsetProperties.contains(Metadata_Property_Builder.Property.TYPE), "type not set");
    return type;
  }

  /**
   * Sets the value to be returned by {@link Metadata.Property#getBoxedType()}.
   *
   * @return this {@code Builder} object
   */
  public Metadata.Property.Builder setBoxedType(@Nullable TypeMirror boxedType) {
    this.boxedType = boxedType;
    return (Metadata.Property.Builder) this;
  }

  /**
   * Returns the value that will be returned by {@link Metadata.Property#getBoxedType()}.
   */
  @Nullable
  public TypeMirror getBoxedType() {
    return boxedType;
  }

  /**
   * Sets the value to be returned by {@link Metadata.Property#getName()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code name} is null
   */
  public Metadata.Property.Builder setName(String name) {
    this.name = Preconditions.checkNotNull(name);
    _unsetProperties.remove(Metadata_Property_Builder.Property.NAME);
    return (Metadata.Property.Builder) this;
  }

  /**
   * Returns the value that will be returned by {@link Metadata.Property#getName()}.
   *
   * @throws IllegalStateException if the field has not been set
   */
  public String getName() {
    Preconditions.checkState(
        !_unsetProperties.contains(Metadata_Property_Builder.Property.NAME), "name not set");
    return name;
  }

  /**
   * Sets the value to be returned by {@link Metadata.Property#getCapitalizedName()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code capitalizedName} is null
   */
  public Metadata.Property.Builder setCapitalizedName(String capitalizedName) {
    this.capitalizedName = Preconditions.checkNotNull(capitalizedName);
    _unsetProperties.remove(Metadata_Property_Builder.Property.CAPITALIZED_NAME);
    return (Metadata.Property.Builder) this;
  }

  /**
   * Returns the value that will be returned by {@link Metadata.Property#getCapitalizedName()}.
   *
   * @throws IllegalStateException if the field has not been set
   */
  public String getCapitalizedName() {
    Preconditions.checkState(
        !_unsetProperties.contains(Metadata_Property_Builder.Property.CAPITALIZED_NAME),
        "capitalizedName not set");
    return capitalizedName;
  }

  /**
   * Sets the value to be returned by {@link Metadata.Property#getAllCapsName()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code allCapsName} is null
   */
  public Metadata.Property.Builder setAllCapsName(String allCapsName) {
    this.allCapsName = Preconditions.checkNotNull(allCapsName);
    _unsetProperties.remove(Metadata_Property_Builder.Property.ALL_CAPS_NAME);
    return (Metadata.Property.Builder) this;
  }

  /**
   * Returns the value that will be returned by {@link Metadata.Property#getAllCapsName()}.
   *
   * @throws IllegalStateException if the field has not been set
   */
  public String getAllCapsName() {
    Preconditions.checkState(
        !_unsetProperties.contains(Metadata_Property_Builder.Property.ALL_CAPS_NAME),
        "allCapsName not set");
    return allCapsName;
  }

  /**
   * Sets the value to be returned by {@link Metadata.Property#getGetterName()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code getterName} is null
   */
  public Metadata.Property.Builder setGetterName(String getterName) {
    this.getterName = Preconditions.checkNotNull(getterName);
    _unsetProperties.remove(Metadata_Property_Builder.Property.GETTER_NAME);
    return (Metadata.Property.Builder) this;
  }

  /**
   * Returns the value that will be returned by {@link Metadata.Property#getGetterName()}.
   *
   * @throws IllegalStateException if the field has not been set
   */
  public String getGetterName() {
    Preconditions.checkState(
        !_unsetProperties.contains(Metadata_Property_Builder.Property.GETTER_NAME),
        "getterName not set");
    return getterName;
  }

  /**
   * Sets the value to be returned by {@link Metadata.Property#getCodeGenerator()}.
   *
   * @return this {@code Builder} object
   */
  public Metadata.Property.Builder setCodeGenerator(@Nullable PropertyCodeGenerator codeGenerator) {
    this.codeGenerator = codeGenerator;
    return (Metadata.Property.Builder) this;
  }

  /**
   * Returns the value that will be returned by {@link Metadata.Property#getCodeGenerator()}.
   */
  @Nullable
  public PropertyCodeGenerator getCodeGenerator() {
    return codeGenerator;
  }

  /**
   * Sets the value to be returned by {@link Metadata.Property#isFullyCheckedCast()}.
   *
   * @return this {@code Builder} object
   */
  public Metadata.Property.Builder setFullyCheckedCast(boolean fullyCheckedCast) {
    this.fullyCheckedCast = fullyCheckedCast;
    _unsetProperties.remove(Metadata_Property_Builder.Property.FULLY_CHECKED_CAST);
    return (Metadata.Property.Builder) this;
  }

  /**
   * Returns the value that will be returned by {@link Metadata.Property#isFullyCheckedCast()}.
   *
   * @throws IllegalStateException if the field has not been set
   */
  public boolean isFullyCheckedCast() {
    Preconditions.checkState(
        !_unsetProperties.contains(Metadata_Property_Builder.Property.FULLY_CHECKED_CAST),
        "fullyCheckedCast not set");
    return fullyCheckedCast;
  }

  /**
   * Adds {@code element} to the list to be returned from {@link Metadata.Property#getAccessorAnnotations()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code element} is null
   */
  public Metadata.Property.Builder addAccessorAnnotations(Excerpt element) {
    this.accessorAnnotations.add(Preconditions.checkNotNull(element));
    return (Metadata.Property.Builder) this;
  }

  /**
   * Adds each element of {@code elements} to the list to be returned from
   * {@link Metadata.Property#getAccessorAnnotations()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code elements} is null or contains a
   *     null element
   */
  public Metadata.Property.Builder addAccessorAnnotations(Excerpt... elements) {
    accessorAnnotations.ensureCapacity(accessorAnnotations.size() + elements.length);
    for (Excerpt element : elements) {
      addAccessorAnnotations(element);
    }
    return (Metadata.Property.Builder) this;
  }

  /**
   * Adds each element of {@code elements} to the list to be returned from
   * {@link Metadata.Property#getAccessorAnnotations()}.
   *
   * @return this {@code Builder} object
   * @throws NullPointerException if {@code elements} is null or contains a
   *     null element
   */
  public Metadata.Property.Builder addAllAccessorAnnotations(Iterable<? extends Excerpt> elements) {
    if (elements instanceof Collection) {
      accessorAnnotations.ensureCapacity(
          accessorAnnotations.size() + ((Collection<?>) elements).size());
    }
    for (Excerpt element : elements) {
      addAccessorAnnotations(element);
    }
    return (Metadata.Property.Builder) this;
  }

  /**
   * Clears the list to be returned from {@link Metadata.Property#getAccessorAnnotations()}.
   *
   * @return this {@code Builder} object
   */
  public Metadata.Property.Builder clearAccessorAnnotations() {
    this.accessorAnnotations.clear();
    return (Metadata.Property.Builder) this;
  }

  /**
   * Returns an unmodifiable view of the list that will be returned by
   * {@link Metadata.Property#getAccessorAnnotations()}.
   * Changes to this builder will be reflected in the view.
   */
  public List<Excerpt> getAccessorAnnotations() {
    return Collections.unmodifiableList(accessorAnnotations);
  }

  /**
   * Sets all property values using the given {@code Metadata.Property} as a template.
   */
  public Metadata.Property.Builder mergeFrom(Metadata.Property value) {
    setType(value.getType());
    setBoxedType(value.getBoxedType());
    setName(value.getName());
    setCapitalizedName(value.getCapitalizedName());
    setAllCapsName(value.getAllCapsName());
    setGetterName(value.getGetterName());
    setCodeGenerator(value.getCodeGenerator());
    setFullyCheckedCast(value.isFullyCheckedCast());
    addAllAccessorAnnotations(value.getAccessorAnnotations());
    return (Metadata.Property.Builder) this;
  }

  /**
   * Copies values from the given {@code Builder}.
   * Does not affect any properties not set on the input.
   */
  public Metadata.Property.Builder mergeFrom(Metadata.Property.Builder template) {
    // Upcast to access the private _unsetProperties field.
    // Otherwise, oddly, we get an access violation.
    EnumSet<Metadata_Property_Builder.Property> _templateUnset =
        ((Metadata_Property_Builder) template)._unsetProperties;
    if (!_templateUnset.contains(Metadata_Property_Builder.Property.TYPE)) {
      setType(template.getType());
    }
    setBoxedType(template.getBoxedType());
    if (!_templateUnset.contains(Metadata_Property_Builder.Property.NAME)) {
      setName(template.getName());
    }
    if (!_templateUnset.contains(Metadata_Property_Builder.Property.CAPITALIZED_NAME)) {
      setCapitalizedName(template.getCapitalizedName());
    }
    if (!_templateUnset.contains(Metadata_Property_Builder.Property.ALL_CAPS_NAME)) {
      setAllCapsName(template.getAllCapsName());
    }
    if (!_templateUnset.contains(Metadata_Property_Builder.Property.GETTER_NAME)) {
      setGetterName(template.getGetterName());
    }
    setCodeGenerator(template.getCodeGenerator());
    if (!_templateUnset.contains(Metadata_Property_Builder.Property.FULLY_CHECKED_CAST)) {
      setFullyCheckedCast(template.isFullyCheckedCast());
    }
    addAllAccessorAnnotations(((Metadata_Property_Builder) template).accessorAnnotations);
    return (Metadata.Property.Builder) this;
  }

  /**
   * Resets the state of this builder.
   */
  public Metadata.Property.Builder clear() {
    Metadata_Property_Builder _template = new Metadata.Property.Builder();
    type = _template.type;
    boxedType = _template.boxedType;
    name = _template.name;
    capitalizedName = _template.capitalizedName;
    allCapsName = _template.allCapsName;
    getterName = _template.getterName;
    codeGenerator = _template.codeGenerator;
    fullyCheckedCast = _template.fullyCheckedCast;
    accessorAnnotations.clear();
    _unsetProperties.clear();
    _unsetProperties.addAll(_template._unsetProperties);
    return (Metadata.Property.Builder) this;
  }

  /**
   * Returns a newly-created {@link Metadata.Property} based on the contents of the {@code Builder}.
   *
   * @throws IllegalStateException if any field has not been set
   */
  public Metadata.Property build() {
    Preconditions.checkState(_unsetProperties.isEmpty(), "Not set: %s", _unsetProperties);
    return new Metadata_Property_Builder.Value(this);
  }

  /**
   * Returns a newly-created partial {@link Metadata.Property}
   * based on the contents of the {@code Builder}.
   * State checking will not be performed.
   * Unset properties will throw an {@link UnsupportedOperationException}
   * when accessed via the partial object.
   *
   * <p>Partials should only ever be used in tests.
   */
  @VisibleForTesting()
  public Metadata.Property buildPartial() {
    return new Metadata_Property_Builder.Partial(this);
  }

  private static final class Value extends Metadata.Property {
    private final TypeMirror type;
    @Nullable private final TypeMirror boxedType;
    private final String name;
    private final String capitalizedName;
    private final String allCapsName;
    private final String getterName;
    @Nullable private final PropertyCodeGenerator codeGenerator;
    private final boolean fullyCheckedCast;
    private final ImmutableList<Excerpt> accessorAnnotations;

    private Value(Metadata_Property_Builder builder) {
      this.type = builder.type;
      this.boxedType = builder.boxedType;
      this.name = builder.name;
      this.capitalizedName = builder.capitalizedName;
      this.allCapsName = builder.allCapsName;
      this.getterName = builder.getterName;
      this.codeGenerator = builder.codeGenerator;
      this.fullyCheckedCast = builder.fullyCheckedCast;
      this.accessorAnnotations = ImmutableList.copyOf(builder.accessorAnnotations);
    }

    @Override
    public TypeMirror getType() {
      return type;
    }

    @Override
    @Nullable
    public TypeMirror getBoxedType() {
      return boxedType;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public String getCapitalizedName() {
      return capitalizedName;
    }

    @Override
    public String getAllCapsName() {
      return allCapsName;
    }

    @Override
    public String getGetterName() {
      return getterName;
    }

    @Override
    @Nullable
    public PropertyCodeGenerator getCodeGenerator() {
      return codeGenerator;
    }

    @Override
    public boolean isFullyCheckedCast() {
      return fullyCheckedCast;
    }

    @Override
    public ImmutableList<Excerpt> getAccessorAnnotations() {
      return accessorAnnotations;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof Metadata_Property_Builder.Value)) {
        return false;
      }
      Metadata_Property_Builder.Value other = (Metadata_Property_Builder.Value) obj;
      if (!type.equals(other.type)) {
        return false;
      }
      if (boxedType != other.boxedType
          && (boxedType == null || !boxedType.equals(other.boxedType))) {
        return false;
      }
      if (!name.equals(other.name)) {
        return false;
      }
      if (!capitalizedName.equals(other.capitalizedName)) {
        return false;
      }
      if (!allCapsName.equals(other.allCapsName)) {
        return false;
      }
      if (!getterName.equals(other.getterName)) {
        return false;
      }
      if (codeGenerator != other.codeGenerator
          && (codeGenerator == null || !codeGenerator.equals(other.codeGenerator))) {
        return false;
      }
      if (fullyCheckedCast != other.fullyCheckedCast) {
        return false;
      }
      if (!accessorAnnotations.equals(other.accessorAnnotations)) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(
          new Object[] {
            type,
            boxedType,
            name,
            capitalizedName,
            allCapsName,
            getterName,
            codeGenerator,
            fullyCheckedCast,
            accessorAnnotations
          });
    }

    @Override
    public String toString() {
      return "Property{"
          + COMMA_JOINER.join(
              "type=" + type,
              (boxedType != null ? "boxedType=" + boxedType : null),
              "name=" + name,
              "capitalizedName=" + capitalizedName,
              "allCapsName=" + allCapsName,
              "getterName=" + getterName,
              (codeGenerator != null ? "codeGenerator=" + codeGenerator : null),
              "fullyCheckedCast=" + fullyCheckedCast,
              "accessorAnnotations=" + accessorAnnotations)
          + "}";
    }
  }

  private static final class Partial extends Metadata.Property {
    private final TypeMirror type;
    @Nullable private final TypeMirror boxedType;
    private final String name;
    private final String capitalizedName;
    private final String allCapsName;
    private final String getterName;
    @Nullable private final PropertyCodeGenerator codeGenerator;
    private final boolean fullyCheckedCast;
    private final ImmutableList<Excerpt> accessorAnnotations;
    private final EnumSet<Metadata_Property_Builder.Property> _unsetProperties;

    Partial(Metadata_Property_Builder builder) {
      this.type = builder.type;
      this.boxedType = builder.boxedType;
      this.name = builder.name;
      this.capitalizedName = builder.capitalizedName;
      this.allCapsName = builder.allCapsName;
      this.getterName = builder.getterName;
      this.codeGenerator = builder.codeGenerator;
      this.fullyCheckedCast = builder.fullyCheckedCast;
      this.accessorAnnotations = ImmutableList.copyOf(builder.accessorAnnotations);
      this._unsetProperties = builder._unsetProperties.clone();
    }

    @Override
    public TypeMirror getType() {
      if (_unsetProperties.contains(Metadata_Property_Builder.Property.TYPE)) {
        throw new UnsupportedOperationException("type not set");
      }
      return type;
    }

    @Override
    @Nullable
    public TypeMirror getBoxedType() {
      return boxedType;
    }

    @Override
    public String getName() {
      if (_unsetProperties.contains(Metadata_Property_Builder.Property.NAME)) {
        throw new UnsupportedOperationException("name not set");
      }
      return name;
    }

    @Override
    public String getCapitalizedName() {
      if (_unsetProperties.contains(Metadata_Property_Builder.Property.CAPITALIZED_NAME)) {
        throw new UnsupportedOperationException("capitalizedName not set");
      }
      return capitalizedName;
    }

    @Override
    public String getAllCapsName() {
      if (_unsetProperties.contains(Metadata_Property_Builder.Property.ALL_CAPS_NAME)) {
        throw new UnsupportedOperationException("allCapsName not set");
      }
      return allCapsName;
    }

    @Override
    public String getGetterName() {
      if (_unsetProperties.contains(Metadata_Property_Builder.Property.GETTER_NAME)) {
        throw new UnsupportedOperationException("getterName not set");
      }
      return getterName;
    }

    @Override
    @Nullable
    public PropertyCodeGenerator getCodeGenerator() {
      return codeGenerator;
    }

    @Override
    public boolean isFullyCheckedCast() {
      if (_unsetProperties.contains(Metadata_Property_Builder.Property.FULLY_CHECKED_CAST)) {
        throw new UnsupportedOperationException("fullyCheckedCast not set");
      }
      return fullyCheckedCast;
    }

    @Override
    public ImmutableList<Excerpt> getAccessorAnnotations() {
      return accessorAnnotations;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof Metadata_Property_Builder.Partial)) {
        return false;
      }
      Metadata_Property_Builder.Partial other = (Metadata_Property_Builder.Partial) obj;
      if (type != other.type && (type == null || !type.equals(other.type))) {
        return false;
      }
      if (boxedType != other.boxedType
          && (boxedType == null || !boxedType.equals(other.boxedType))) {
        return false;
      }
      if (name != other.name && (name == null || !name.equals(other.name))) {
        return false;
      }
      if (capitalizedName != other.capitalizedName
          && (capitalizedName == null || !capitalizedName.equals(other.capitalizedName))) {
        return false;
      }
      if (allCapsName != other.allCapsName
          && (allCapsName == null || !allCapsName.equals(other.allCapsName))) {
        return false;
      }
      if (getterName != other.getterName
          && (getterName == null || !getterName.equals(other.getterName))) {
        return false;
      }
      if (codeGenerator != other.codeGenerator
          && (codeGenerator == null || !codeGenerator.equals(other.codeGenerator))) {
        return false;
      }
      if (fullyCheckedCast != other.fullyCheckedCast) {
        return false;
      }
      if (!accessorAnnotations.equals(other.accessorAnnotations)) {
        return false;
      }
      return _unsetProperties.equals(other._unsetProperties);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(
          new Object[] {
            type,
            boxedType,
            name,
            capitalizedName,
            allCapsName,
            getterName,
            codeGenerator,
            fullyCheckedCast,
            accessorAnnotations,
            _unsetProperties
          });
    }

    @Override
    public String toString() {
      return "partial Property{"
          + COMMA_JOINER.join(
              (!_unsetProperties.contains(Metadata_Property_Builder.Property.TYPE)
                  ? "type=" + type
                  : null),
              (boxedType != null ? "boxedType=" + boxedType : null),
              (!_unsetProperties.contains(Metadata_Property_Builder.Property.NAME)
                  ? "name=" + name
                  : null),
              (!_unsetProperties.contains(Metadata_Property_Builder.Property.CAPITALIZED_NAME)
                  ? "capitalizedName=" + capitalizedName
                  : null),
              (!_unsetProperties.contains(Metadata_Property_Builder.Property.ALL_CAPS_NAME)
                  ? "allCapsName=" + allCapsName
                  : null),
              (!_unsetProperties.contains(Metadata_Property_Builder.Property.GETTER_NAME)
                  ? "getterName=" + getterName
                  : null),
              (codeGenerator != null ? "codeGenerator=" + codeGenerator : null),
              (!_unsetProperties.contains(Metadata_Property_Builder.Property.FULLY_CHECKED_CAST)
                  ? "fullyCheckedCast=" + fullyCheckedCast
                  : null),
              "accessorAnnotations=" + accessorAnnotations)
          + "}";
    }
  }
}
