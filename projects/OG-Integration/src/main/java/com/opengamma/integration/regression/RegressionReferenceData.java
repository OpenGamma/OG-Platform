package com.opengamma.integration.regression;

import java.util.HashMap;
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

import com.google.common.collect.ImmutableMap;
import com.opengamma.bbg.referencedata.ReferenceData;

/**
 * Holds reference data used by regression framework.
 */
@BeanDefinition
public final class RegressionReferenceData implements ImmutableBean {

  @PropertyDefinition(validate = "notNull")
  private final ImmutableMap<String, ReferenceData> _referenceData;
  
  /**
   * Creates a regression reference data object
   * @param referenceData the reference data
   * @return a new instance
   */
  public static RegressionReferenceData create(ImmutableMap<String, ReferenceData> referenceData) {
    return new RegressionReferenceData(referenceData);
  }
  
  
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code RegressionReferenceData}.
   * @return the meta-bean, not null
   */
  public static RegressionReferenceData.Meta meta() {
    return RegressionReferenceData.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(RegressionReferenceData.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static RegressionReferenceData.Builder builder() {
    return new RegressionReferenceData.Builder();
  }

  private RegressionReferenceData(
      Map<String, ReferenceData> referenceData) {
    JodaBeanUtils.notNull(referenceData, "referenceData");
    this._referenceData = ImmutableMap.copyOf(referenceData);
  }

  @Override
  public RegressionReferenceData.Meta metaBean() {
    return RegressionReferenceData.Meta.INSTANCE;
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
   * Gets the referenceData.
   * @return the value of the property, not null
   */
  public ImmutableMap<String, ReferenceData> getReferenceData() {
    return _referenceData;
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
      RegressionReferenceData other = (RegressionReferenceData) obj;
      return JodaBeanUtils.equal(getReferenceData(), other.getReferenceData());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getReferenceData());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("RegressionReferenceData{");
    buf.append("referenceData").append('=').append(JodaBeanUtils.toString(getReferenceData()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code RegressionReferenceData}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code referenceData} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<ImmutableMap<String, ReferenceData>> _referenceData = DirectMetaProperty.ofImmutable(
        this, "referenceData", RegressionReferenceData.class, (Class) ImmutableMap.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "referenceData");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1600456085:  // referenceData
          return _referenceData;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public RegressionReferenceData.Builder builder() {
      return new RegressionReferenceData.Builder();
    }

    @Override
    public Class<? extends RegressionReferenceData> beanType() {
      return RegressionReferenceData.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code referenceData} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ImmutableMap<String, ReferenceData>> referenceData() {
      return _referenceData;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1600456085:  // referenceData
          return ((RegressionReferenceData) bean).getReferenceData();
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
   * The bean-builder for {@code RegressionReferenceData}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<RegressionReferenceData> {

    private Map<String, ReferenceData> _referenceData = new HashMap<String, ReferenceData>();

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(RegressionReferenceData beanToCopy) {
      this._referenceData = new HashMap<String, ReferenceData>(beanToCopy.getReferenceData());
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1600456085:  // referenceData
          return _referenceData;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 1600456085:  // referenceData
          this._referenceData = (Map<String, ReferenceData>) newValue;
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
    public RegressionReferenceData build() {
      return new RegressionReferenceData(
          _referenceData);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code referenceData} property in the builder.
     * @param referenceData  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder referenceData(Map<String, ReferenceData> referenceData) {
      JodaBeanUtils.notNull(referenceData, "referenceData");
      this._referenceData = referenceData;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(64);
      buf.append("RegressionReferenceData.Builder{");
      buf.append("referenceData").append('=').append(JodaBeanUtils.toString(_referenceData));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
