/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutableConstructor;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.ArgumentChecker;

/**
 * Joda bean containing the ValueRequirement and Column Set/Calc Config for a specific cell
 */
@BeanDefinition
public final class ValueRequirementTargetForCell implements ImmutableBean {

  /**
   * Name of the Column Set/Calc Config.
   */
  @PropertyDefinition
  private final String _columnSet;

  /**
   * ValueSpecification for cell.
   */
  @PropertyDefinition
  private final ValueRequirement _valueRequirement;

  /**
   * Constructor for the definition.
   *
   * @param columnSet the name of the Column Set/Calc Config
   * @param valueRequirement ValueRequirement for cell
   */
  @ImmutableConstructor
  public ValueRequirementTargetForCell(
      String columnSet,
      ValueRequirement valueRequirement) {

    ArgumentChecker.notNull(columnSet, "columnSet");
    ArgumentChecker.notNull(valueRequirement, "valuleSpecification");
    this._columnSet = columnSet;
    this._valueRequirement = valueRequirement;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ValueRequirementTargetForCell}.
   * @return the meta-bean, not null
   */
  public static ValueRequirementTargetForCell.Meta meta() {
    return ValueRequirementTargetForCell.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ValueRequirementTargetForCell.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static ValueRequirementTargetForCell.Builder builder() {
    return new ValueRequirementTargetForCell.Builder();
  }

  @Override
  public ValueRequirementTargetForCell.Meta metaBean() {
    return ValueRequirementTargetForCell.Meta.INSTANCE;
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
   * Gets name of the Column Set/Calc Config.
   * @return the value of the property
   */
  public String getColumnSet() {
    return _columnSet;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets valueSpecification for cell.
   * @return the value of the property
   */
  public ValueRequirement getValueRequirement() {
    return _valueRequirement;
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
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ValueRequirementTargetForCell other = (ValueRequirementTargetForCell) obj;
      return JodaBeanUtils.equal(getColumnSet(), other.getColumnSet()) &&
          JodaBeanUtils.equal(getValueRequirement(), other.getValueRequirement());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getColumnSet());
    hash += hash * 31 + JodaBeanUtils.hashCode(getValueRequirement());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("ValueRequirementTargetForCell{");
    buf.append("columnSet").append('=').append(getColumnSet()).append(',').append(' ');
    buf.append("valueRequirement").append('=').append(JodaBeanUtils.toString(getValueRequirement()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ValueRequirementTargetForCell}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code columnSet} property.
     */
    private final MetaProperty<String> _columnSet = DirectMetaProperty.ofImmutable(
        this, "columnSet", ValueRequirementTargetForCell.class, String.class);
    /**
     * The meta-property for the {@code valueRequirement} property.
     */
    private final MetaProperty<ValueRequirement> _valueRequirement = DirectMetaProperty.ofImmutable(
        this, "valueRequirement", ValueRequirementTargetForCell.class, ValueRequirement.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "columnSet",
        "valueRequirement");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -2146129620:  // columnSet
          return _columnSet;
        case -755281390:  // valueRequirement
          return _valueRequirement;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public ValueRequirementTargetForCell.Builder builder() {
      return new ValueRequirementTargetForCell.Builder();
    }

    @Override
    public Class<? extends ValueRequirementTargetForCell> beanType() {
      return ValueRequirementTargetForCell.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code columnSet} property.
     * @return the meta-property, not null
     */
    public MetaProperty<String> columnSet() {
      return _columnSet;
    }

    /**
     * The meta-property for the {@code valueRequirement} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ValueRequirement> valueRequirement() {
      return _valueRequirement;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -2146129620:  // columnSet
          return ((ValueRequirementTargetForCell) bean).getColumnSet();
        case -755281390:  // valueRequirement
          return ((ValueRequirementTargetForCell) bean).getValueRequirement();
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
   * The bean-builder for {@code ValueRequirementTargetForCell}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<ValueRequirementTargetForCell> {

    private String _columnSet;
    private ValueRequirement _valueRequirement;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(ValueRequirementTargetForCell beanToCopy) {
      this._columnSet = beanToCopy.getColumnSet();
      this._valueRequirement = beanToCopy.getValueRequirement();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -2146129620:  // columnSet
          return _columnSet;
        case -755281390:  // valueRequirement
          return _valueRequirement;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -2146129620:  // columnSet
          this._columnSet = (String) newValue;
          break;
        case -755281390:  // valueRequirement
          this._valueRequirement = (ValueRequirement) newValue;
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
    public ValueRequirementTargetForCell build() {
      return new ValueRequirementTargetForCell(
          _columnSet,
          _valueRequirement);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code columnSet} property in the builder.
     * @param columnSet  the new value
     * @return this, for chaining, not null
     */
    public Builder columnSet(String columnSet) {
      this._columnSet = columnSet;
      return this;
    }

    /**
     * Sets the {@code valueRequirement} property in the builder.
     * @param valueRequirement  the new value
     * @return this, for chaining, not null
     */
    public Builder valueRequirement(ValueRequirement valueRequirement) {
      this._valueRequirement = valueRequirement;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("ValueRequirementTargetForCell.Builder{");
      buf.append("columnSet").append('=').append(JodaBeanUtils.toString(_columnSet)).append(',').append(' ');
      buf.append("valueRequirement").append('=').append(JodaBeanUtils.toString(_valueRequirement));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
