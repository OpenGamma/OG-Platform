/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

/**
 * Configuration object for curves that are to be used as discounting curves.
 */
@BeanDefinition
public class DiscountingCurveTypeConfiguration extends CurveTypeConfiguration {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * The reference.
   */
  @PropertyDefinition(validate = "notNull")
  private String _reference;

  /**
   * For the builder.
   */
  /* package */ DiscountingCurveTypeConfiguration() {
    super();
  }

  /**
   * @param reference The code for this curve (e.g. a currency), not null
   */
  public DiscountingCurveTypeConfiguration(final String reference) {
    super();
    setReference(reference);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code DiscountingCurveTypeConfiguration}.
   * @return the meta-bean, not null
   */
  public static DiscountingCurveTypeConfiguration.Meta meta() {
    return DiscountingCurveTypeConfiguration.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(DiscountingCurveTypeConfiguration.Meta.INSTANCE);
  }

  @Override
  public DiscountingCurveTypeConfiguration.Meta metaBean() {
    return DiscountingCurveTypeConfiguration.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the reference.
   * @return the value of the property, not null
   */
  public String getReference() {
    return _reference;
  }

  /**
   * Sets the reference.
   * @param reference  the new value of the property, not null
   */
  public void setReference(String reference) {
    JodaBeanUtils.notNull(reference, "reference");
    this._reference = reference;
  }

  /**
   * Gets the the {@code reference} property.
   * @return the property, not null
   */
  public final Property<String> reference() {
    return metaBean().reference().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public DiscountingCurveTypeConfiguration clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      DiscountingCurveTypeConfiguration other = (DiscountingCurveTypeConfiguration) obj;
      return JodaBeanUtils.equal(getReference(), other.getReference()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getReference());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("DiscountingCurveTypeConfiguration{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  @Override
  protected void toString(StringBuilder buf) {
    super.toString(buf);
    buf.append("reference").append('=').append(JodaBeanUtils.toString(getReference())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code DiscountingCurveTypeConfiguration}.
   */
  public static class Meta extends CurveTypeConfiguration.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code reference} property.
     */
    private final MetaProperty<String> _reference = DirectMetaProperty.ofReadWrite(
        this, "reference", DiscountingCurveTypeConfiguration.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "reference");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -925155509:  // reference
          return _reference;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends DiscountingCurveTypeConfiguration> builder() {
      return new DirectBeanBuilder<DiscountingCurveTypeConfiguration>(new DiscountingCurveTypeConfiguration());
    }

    @Override
    public Class<? extends DiscountingCurveTypeConfiguration> beanType() {
      return DiscountingCurveTypeConfiguration.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code reference} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> reference() {
      return _reference;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -925155509:  // reference
          return ((DiscountingCurveTypeConfiguration) bean).getReference();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -925155509:  // reference
          ((DiscountingCurveTypeConfiguration) bean).setReference((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((DiscountingCurveTypeConfiguration) bean)._reference, "reference");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
