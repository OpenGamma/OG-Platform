/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.function;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.google.common.reflect.TypeToken;
import com.opengamma.util.ArgumentChecker;

/**
 * Type information for an array that contains the array class and the class of its elements.
 */
@BeanDefinition
public class ArrayType extends ParameterType implements ImmutableBean {

  /** The array type. */
  @PropertyDefinition(validate = "notNull")
  private final Class<?> _type;

  /** The type of the array's elements. */
  @PropertyDefinition(validate = "notNull")
  private final Class<?> _elementType;

  /* package */ ArrayType(Type type) {
    _type = TypeToken.of(ArgumentChecker.notNull(type, "type")).getRawType();
    _elementType = ParameterUtils.getElementType(type);
  }

  @Override
  public String getName() {
    return _elementType.getSimpleName() + "[]";
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ArrayType}.
   * @return the meta-bean, not null
   */
  public static ArrayType.Meta meta() {
    return ArrayType.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ArrayType.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static ArrayType.Builder builder() {
    return new ArrayType.Builder();
  }

  /**
   * Restricted constructor.
   * @param builder  the builder to copy from, not null
   */
  protected ArrayType(ArrayType.Builder builder) {
    JodaBeanUtils.notNull(builder._type, "type");
    JodaBeanUtils.notNull(builder._elementType, "elementType");
    this._type = builder._type;
    this._elementType = builder._elementType;
  }

  @Override
  public ArrayType.Meta metaBean() {
    return ArrayType.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the type.
   * @return the value of the property, not null
   */
  public Class<?> getType() {
    return _type;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the elementType.
   * @return the value of the property, not null
   */
  public Class<?> getElementType() {
    return _elementType;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public ArrayType clone() {
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ArrayType other = (ArrayType) obj;
      return JodaBeanUtils.equal(getType(), other.getType()) &&
          JodaBeanUtils.equal(getElementType(), other.getElementType());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getType());
    hash += hash * 31 + JodaBeanUtils.hashCode(getElementType());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("ArrayType{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("type").append('=').append(JodaBeanUtils.toString(getType())).append(',').append(' ');
    buf.append("elementType").append('=').append(JodaBeanUtils.toString(getElementType())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ArrayType}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code type} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Class<?>> _type = DirectMetaProperty.ofImmutable(
        this, "type", ArrayType.class, (Class) Class.class);
    /**
     * The meta-property for the {@code elementType} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Class<?>> _elementType = DirectMetaProperty.ofImmutable(
        this, "elementType", ArrayType.class, (Class) Class.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "type",
        "elementType");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3575610:  // type
          return _type;
        case 673924182:  // elementType
          return _elementType;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public ArrayType.Builder builder() {
      return new ArrayType.Builder();
    }

    @Override
    public Class<? extends ArrayType> beanType() {
      return ArrayType.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code type} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Class<?>> type() {
      return _type;
    }

    /**
     * The meta-property for the {@code elementType} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Class<?>> elementType() {
      return _elementType;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 3575610:  // type
          return ((ArrayType) bean).getType();
        case 673924182:  // elementType
          return ((ArrayType) bean).getElementType();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code ArrayType}.
   */
  public static class Builder extends DirectFieldsBeanBuilder<ArrayType> {

    private Class<?> _type;
    private Class<?> _elementType;

    /**
     * Restricted constructor.
     */
    protected Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    protected Builder(ArrayType beanToCopy) {
      this._type = beanToCopy.getType();
      this._elementType = beanToCopy.getElementType();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3575610:  // type
          return _type;
        case 673924182:  // elementType
          return _elementType;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 3575610:  // type
          this._type = (Class<?>) newValue;
          break;
        case 673924182:  // elementType
          this._elementType = (Class<?>) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public ArrayType build() {
      return new ArrayType(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code type} property in the builder.
     * @param type  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder type(Class<?> type) {
      JodaBeanUtils.notNull(type, "type");
      this._type = type;
      return this;
    }

    /**
     * Sets the {@code elementType} property in the builder.
     * @param elementType  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder elementType(Class<?> elementType) {
      JodaBeanUtils.notNull(elementType, "elementType");
      this._elementType = elementType;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("ArrayType.Builder{");
      int len = buf.length();
      toString(buf);
      if (buf.length() > len) {
        buf.setLength(buf.length() - 2);
      }
      buf.append('}');
      return buf.toString();
    }

    protected void toString(StringBuilder buf) {
      buf.append("type").append('=').append(JodaBeanUtils.toString(_type)).append(',').append(' ');
      buf.append("elementType").append('=').append(JodaBeanUtils.toString(_elementType)).append(',').append(' ');
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
