/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.config;

import java.io.Serializable;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.core.config.Config;
import com.opengamma.core.config.ConfigGroups;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;

/**
 * Config object that contains bloomberg field overrides.
 */
@BeanDefinition
@Config(description = "Bloomberg field override", group = ConfigGroups.MISC)
public class BloombergFieldOverride extends DirectBean implements Serializable, UniqueIdentifiable, MutableUniqueIdentifiable {
  
  @PropertyDefinition
  private UniqueId _uniqueId;
  
  @PropertyDefinition
  private String _bloombergId;
  
  @PropertyDefinition
  private String _fieldName;
  
  @PropertyDefinition
  private Object _overrideValue;
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code BloombergFieldOverride}.
   * @return the meta-bean, not null
   */
  public static BloombergFieldOverride.Meta meta() {
    return BloombergFieldOverride.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(BloombergFieldOverride.Meta.INSTANCE);
  }

  @Override
  public BloombergFieldOverride.Meta metaBean() {
    return BloombergFieldOverride.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the uniqueId.
   * @return the value of the property
   */
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the uniqueId.
   * @param uniqueId  the new value of the property
   */
  public void setUniqueId(UniqueId uniqueId) {
    this._uniqueId = uniqueId;
  }

  /**
   * Gets the the {@code uniqueId} property.
   * @return the property, not null
   */
  public final Property<UniqueId> uniqueId() {
    return metaBean().uniqueId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the bloombergId.
   * @return the value of the property
   */
  public String getBloombergId() {
    return _bloombergId;
  }

  /**
   * Sets the bloombergId.
   * @param bloombergId  the new value of the property
   */
  public void setBloombergId(String bloombergId) {
    this._bloombergId = bloombergId;
  }

  /**
   * Gets the the {@code bloombergId} property.
   * @return the property, not null
   */
  public final Property<String> bloombergId() {
    return metaBean().bloombergId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the fieldName.
   * @return the value of the property
   */
  public String getFieldName() {
    return _fieldName;
  }

  /**
   * Sets the fieldName.
   * @param fieldName  the new value of the property
   */
  public void setFieldName(String fieldName) {
    this._fieldName = fieldName;
  }

  /**
   * Gets the the {@code fieldName} property.
   * @return the property, not null
   */
  public final Property<String> fieldName() {
    return metaBean().fieldName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the overrideValue.
   * @return the value of the property
   */
  public Object getOverrideValue() {
    return _overrideValue;
  }

  /**
   * Sets the overrideValue.
   * @param overrideValue  the new value of the property
   */
  public void setOverrideValue(Object overrideValue) {
    this._overrideValue = overrideValue;
  }

  /**
   * Gets the the {@code overrideValue} property.
   * @return the property, not null
   */
  public final Property<Object> overrideValue() {
    return metaBean().overrideValue().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public BloombergFieldOverride clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      BloombergFieldOverride other = (BloombergFieldOverride) obj;
      return JodaBeanUtils.equal(getUniqueId(), other.getUniqueId()) &&
          JodaBeanUtils.equal(getBloombergId(), other.getBloombergId()) &&
          JodaBeanUtils.equal(getFieldName(), other.getFieldName()) &&
          JodaBeanUtils.equal(getOverrideValue(), other.getOverrideValue());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getUniqueId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getBloombergId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFieldName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getOverrideValue());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(160);
    buf.append("BloombergFieldOverride{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("uniqueId").append('=').append(JodaBeanUtils.toString(getUniqueId())).append(',').append(' ');
    buf.append("bloombergId").append('=').append(JodaBeanUtils.toString(getBloombergId())).append(',').append(' ');
    buf.append("fieldName").append('=').append(JodaBeanUtils.toString(getFieldName())).append(',').append(' ');
    buf.append("overrideValue").append('=').append(JodaBeanUtils.toString(getOverrideValue())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code BloombergFieldOverride}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code uniqueId} property.
     */
    private final MetaProperty<UniqueId> _uniqueId = DirectMetaProperty.ofReadWrite(
        this, "uniqueId", BloombergFieldOverride.class, UniqueId.class);
    /**
     * The meta-property for the {@code bloombergId} property.
     */
    private final MetaProperty<String> _bloombergId = DirectMetaProperty.ofReadWrite(
        this, "bloombergId", BloombergFieldOverride.class, String.class);
    /**
     * The meta-property for the {@code fieldName} property.
     */
    private final MetaProperty<String> _fieldName = DirectMetaProperty.ofReadWrite(
        this, "fieldName", BloombergFieldOverride.class, String.class);
    /**
     * The meta-property for the {@code overrideValue} property.
     */
    private final MetaProperty<Object> _overrideValue = DirectMetaProperty.ofReadWrite(
        this, "overrideValue", BloombergFieldOverride.class, Object.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "uniqueId",
        "bloombergId",
        "fieldName",
        "overrideValue");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return _uniqueId;
        case -1780643658:  // bloombergId
          return _bloombergId;
        case 1265009317:  // fieldName
          return _fieldName;
        case 2057831685:  // overrideValue
          return _overrideValue;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends BloombergFieldOverride> builder() {
      return new DirectBeanBuilder<BloombergFieldOverride>(new BloombergFieldOverride());
    }

    @Override
    public Class<? extends BloombergFieldOverride> beanType() {
      return BloombergFieldOverride.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code uniqueId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueId> uniqueId() {
      return _uniqueId;
    }

    /**
     * The meta-property for the {@code bloombergId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> bloombergId() {
      return _bloombergId;
    }

    /**
     * The meta-property for the {@code fieldName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> fieldName() {
      return _fieldName;
    }

    /**
     * The meta-property for the {@code overrideValue} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Object> overrideValue() {
      return _overrideValue;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return ((BloombergFieldOverride) bean).getUniqueId();
        case -1780643658:  // bloombergId
          return ((BloombergFieldOverride) bean).getBloombergId();
        case 1265009317:  // fieldName
          return ((BloombergFieldOverride) bean).getFieldName();
        case 2057831685:  // overrideValue
          return ((BloombergFieldOverride) bean).getOverrideValue();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          ((BloombergFieldOverride) bean).setUniqueId((UniqueId) newValue);
          return;
        case -1780643658:  // bloombergId
          ((BloombergFieldOverride) bean).setBloombergId((String) newValue);
          return;
        case 1265009317:  // fieldName
          ((BloombergFieldOverride) bean).setFieldName((String) newValue);
          return;
        case 2057831685:  // overrideValue
          ((BloombergFieldOverride) bean).setOverrideValue((Object) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
