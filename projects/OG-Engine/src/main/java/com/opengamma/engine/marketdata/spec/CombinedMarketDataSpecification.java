/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.spec;

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

/**
 * 
 */
@BeanDefinition
public final class CombinedMarketDataSpecification implements ImmutableBean, MarketDataSpecification {
  
  private static final long serialVersionUID = 1L;

  @PropertyDefinition(validate = "notNull")
  private final MarketDataSpecification _preferredSpecification;
  
  @PropertyDefinition(validate = "notNull")
  private final MarketDataSpecification _fallbackSpecification;
  
  public static CombinedMarketDataSpecification of(MarketDataSpecification preferredSpecification, MarketDataSpecification fallbackSpecification) {
    return new CombinedMarketDataSpecification(preferredSpecification, fallbackSpecification);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CombinedMarketDataSpecification}.
   * @return the meta-bean, not null
   */
  public static CombinedMarketDataSpecification.Meta meta() {
    return CombinedMarketDataSpecification.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(CombinedMarketDataSpecification.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static CombinedMarketDataSpecification.Builder builder() {
    return new CombinedMarketDataSpecification.Builder();
  }

  private CombinedMarketDataSpecification(
      MarketDataSpecification preferredSpecification,
      MarketDataSpecification fallbackSpecification) {
    JodaBeanUtils.notNull(preferredSpecification, "preferredSpecification");
    JodaBeanUtils.notNull(fallbackSpecification, "fallbackSpecification");
    this._preferredSpecification = preferredSpecification;
    this._fallbackSpecification = fallbackSpecification;
  }

  @Override
  public CombinedMarketDataSpecification.Meta metaBean() {
    return CombinedMarketDataSpecification.Meta.INSTANCE;
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
   * Gets the preferredSpecification.
   * @return the value of the property, not null
   */
  public MarketDataSpecification getPreferredSpecification() {
    return _preferredSpecification;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the fallbackSpecification.
   * @return the value of the property, not null
   */
  public MarketDataSpecification getFallbackSpecification() {
    return _fallbackSpecification;
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
      CombinedMarketDataSpecification other = (CombinedMarketDataSpecification) obj;
      return JodaBeanUtils.equal(getPreferredSpecification(), other.getPreferredSpecification()) &&
          JodaBeanUtils.equal(getFallbackSpecification(), other.getFallbackSpecification());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getPreferredSpecification());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFallbackSpecification());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("CombinedMarketDataSpecification{");
    buf.append("preferredSpecification").append('=').append(getPreferredSpecification()).append(',').append(' ');
    buf.append("fallbackSpecification").append('=').append(JodaBeanUtils.toString(getFallbackSpecification()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CombinedMarketDataSpecification}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code preferredSpecification} property.
     */
    private final MetaProperty<MarketDataSpecification> _preferredSpecification = DirectMetaProperty.ofImmutable(
        this, "preferredSpecification", CombinedMarketDataSpecification.class, MarketDataSpecification.class);
    /**
     * The meta-property for the {@code fallbackSpecification} property.
     */
    private final MetaProperty<MarketDataSpecification> _fallbackSpecification = DirectMetaProperty.ofImmutable(
        this, "fallbackSpecification", CombinedMarketDataSpecification.class, MarketDataSpecification.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "preferredSpecification",
        "fallbackSpecification");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -482312702:  // preferredSpecification
          return _preferredSpecification;
        case 2031323457:  // fallbackSpecification
          return _fallbackSpecification;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public CombinedMarketDataSpecification.Builder builder() {
      return new CombinedMarketDataSpecification.Builder();
    }

    @Override
    public Class<? extends CombinedMarketDataSpecification> beanType() {
      return CombinedMarketDataSpecification.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code preferredSpecification} property.
     * @return the meta-property, not null
     */
    public MetaProperty<MarketDataSpecification> preferredSpecification() {
      return _preferredSpecification;
    }

    /**
     * The meta-property for the {@code fallbackSpecification} property.
     * @return the meta-property, not null
     */
    public MetaProperty<MarketDataSpecification> fallbackSpecification() {
      return _fallbackSpecification;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -482312702:  // preferredSpecification
          return ((CombinedMarketDataSpecification) bean).getPreferredSpecification();
        case 2031323457:  // fallbackSpecification
          return ((CombinedMarketDataSpecification) bean).getFallbackSpecification();
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
   * The bean-builder for {@code CombinedMarketDataSpecification}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<CombinedMarketDataSpecification> {

    private MarketDataSpecification _preferredSpecification;
    private MarketDataSpecification _fallbackSpecification;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(CombinedMarketDataSpecification beanToCopy) {
      this._preferredSpecification = beanToCopy.getPreferredSpecification();
      this._fallbackSpecification = beanToCopy.getFallbackSpecification();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -482312702:  // preferredSpecification
          return _preferredSpecification;
        case 2031323457:  // fallbackSpecification
          return _fallbackSpecification;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -482312702:  // preferredSpecification
          this._preferredSpecification = (MarketDataSpecification) newValue;
          break;
        case 2031323457:  // fallbackSpecification
          this._fallbackSpecification = (MarketDataSpecification) newValue;
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
    public CombinedMarketDataSpecification build() {
      return new CombinedMarketDataSpecification(
          _preferredSpecification,
          _fallbackSpecification);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code preferredSpecification} property in the builder.
     * @param preferredSpecification  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder preferredSpecification(MarketDataSpecification preferredSpecification) {
      JodaBeanUtils.notNull(preferredSpecification, "preferredSpecification");
      this._preferredSpecification = preferredSpecification;
      return this;
    }

    /**
     * Sets the {@code fallbackSpecification} property in the builder.
     * @param fallbackSpecification  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder fallbackSpecification(MarketDataSpecification fallbackSpecification) {
      JodaBeanUtils.notNull(fallbackSpecification, "fallbackSpecification");
      this._fallbackSpecification = fallbackSpecification;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("CombinedMarketDataSpecification.Builder{");
      buf.append("preferredSpecification").append('=').append(JodaBeanUtils.toString(_preferredSpecification)).append(',').append(' ');
      buf.append("fallbackSpecification").append('=').append(JodaBeanUtils.toString(_fallbackSpecification));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
