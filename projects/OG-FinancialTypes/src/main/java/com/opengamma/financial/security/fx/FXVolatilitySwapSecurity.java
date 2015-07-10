/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.fx;

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
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.swap.VolatilitySwapSecurity;
import com.opengamma.financial.security.swap.VolatilitySwapType;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.SecurityDescription;
import com.opengamma.util.money.Currency;

/**
 * Class representing a FX volatility swap.
 */
@BeanDefinition
@SecurityDescription(type = FXVolatilitySwapSecurity.SECURITY_TYPE, description = "Fx volatility swap")
public class FXVolatilitySwapSecurity extends VolatilitySwapSecurity {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * The security type.
   */
  public static final String SECURITY_TYPE = "FX_VOLATILITY_SWAP";
  /**
   * The base currency.
   */
  @PropertyDefinition(validate = "notNull")
  private Currency _baseCurrency;

  /**
   * The counter currency.
   */
  @PropertyDefinition(validate = "notNull")
  private Currency _counterCurrency;

  /**
   * For the builder.
   */
  /* package */FXVolatilitySwapSecurity() {
    super(SECURITY_TYPE);
  }

  /**
   * @param currency The currency, not null
   * @param notional The notional
   * @param volatilitySwapType The volatility swap type, not null
   * @param strike The strike
   * @param settlementDate The settlement date, not null
   * @param maturityDate The maturity date, not null
   * @param annualizationFactor The annualization factor
   * @param firstObservationDate The first observation date, not null
   * @param lastObservationDate The last observation date, not null
   * @param observationFrequency The observation frequency, not null
   * @param baseCurrency The base currency, not null
   * @param counterCurrency The counter currency, not null
   */
  public FXVolatilitySwapSecurity(final Currency currency, final double notional, final VolatilitySwapType volatilitySwapType,
      final double strike, final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double annualizationFactor,
      final ZonedDateTime firstObservationDate, final ZonedDateTime lastObservationDate, final Frequency observationFrequency,
      final Currency baseCurrency, final Currency counterCurrency) {
    super(SECURITY_TYPE, counterCurrency, notional, volatilitySwapType, strike, settlementDate, maturityDate,
        annualizationFactor, firstObservationDate, lastObservationDate, observationFrequency);
    setBaseCurrency(baseCurrency);
    setCounterCurrency(counterCurrency);
  }

  /**
   * @param currency The currency, not null
   * @param notional The notional
   * @param volatilitySwapType The volatility swap type, not null
   * @param strike The strike
   * @param settlementDate The settlement date, not null
   * @param maturityDate The maturity date, not null
   * @param annualizationFactor The annualization factor
   * @param firstObservationDate The first observation date, not null
   * @param lastObservationDate The last observation date, not null
   * @param observationFrequency The observation frequency, not null
   * @param underlyingId The id of the underlying fixing series, not null
   * @param baseCurrency The base currency, not null
   * @param counterCurrency The counter currency, not null
   */
  public FXVolatilitySwapSecurity(final Currency currency, final double notional, final VolatilitySwapType volatilitySwapType,
      final double strike, final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double annualizationFactor,
      final ZonedDateTime firstObservationDate, final ZonedDateTime lastObservationDate, final Frequency observationFrequency,
      final ExternalId underlyingId, final Currency baseCurrency, final Currency counterCurrency) {
    super(SECURITY_TYPE, counterCurrency, notional, volatilitySwapType, strike, settlementDate, maturityDate,
        annualizationFactor, firstObservationDate, lastObservationDate, observationFrequency, underlyingId);
    setBaseCurrency(baseCurrency);
    setCounterCurrency(counterCurrency);
  }

  //-------------------------------------------------------------------------
  @Override
  public final <T> T accept(final FinancialSecurityVisitor<T> visitor) {
    return visitor.visitFXVolatilitySwapSecurity(this);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FXVolatilitySwapSecurity}.
   * @return the meta-bean, not null
   */
  public static FXVolatilitySwapSecurity.Meta meta() {
    return FXVolatilitySwapSecurity.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FXVolatilitySwapSecurity.Meta.INSTANCE);
  }

  @Override
  public FXVolatilitySwapSecurity.Meta metaBean() {
    return FXVolatilitySwapSecurity.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the base currency.
   * @return the value of the property, not null
   */
  public Currency getBaseCurrency() {
    return _baseCurrency;
  }

  /**
   * Sets the base currency.
   * @param baseCurrency  the new value of the property, not null
   */
  public void setBaseCurrency(Currency baseCurrency) {
    JodaBeanUtils.notNull(baseCurrency, "baseCurrency");
    this._baseCurrency = baseCurrency;
  }

  /**
   * Gets the the {@code baseCurrency} property.
   * @return the property, not null
   */
  public final Property<Currency> baseCurrency() {
    return metaBean().baseCurrency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the counter currency.
   * @return the value of the property, not null
   */
  public Currency getCounterCurrency() {
    return _counterCurrency;
  }

  /**
   * Sets the counter currency.
   * @param counterCurrency  the new value of the property, not null
   */
  public void setCounterCurrency(Currency counterCurrency) {
    JodaBeanUtils.notNull(counterCurrency, "counterCurrency");
    this._counterCurrency = counterCurrency;
  }

  /**
   * Gets the the {@code counterCurrency} property.
   * @return the property, not null
   */
  public final Property<Currency> counterCurrency() {
    return metaBean().counterCurrency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public FXVolatilitySwapSecurity clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      FXVolatilitySwapSecurity other = (FXVolatilitySwapSecurity) obj;
      return JodaBeanUtils.equal(getBaseCurrency(), other.getBaseCurrency()) &&
          JodaBeanUtils.equal(getCounterCurrency(), other.getCounterCurrency()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getBaseCurrency());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCounterCurrency());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("FXVolatilitySwapSecurity{");
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
    buf.append("baseCurrency").append('=').append(JodaBeanUtils.toString(getBaseCurrency())).append(',').append(' ');
    buf.append("counterCurrency").append('=').append(JodaBeanUtils.toString(getCounterCurrency())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FXVolatilitySwapSecurity}.
   */
  public static class Meta extends VolatilitySwapSecurity.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code baseCurrency} property.
     */
    private final MetaProperty<Currency> _baseCurrency = DirectMetaProperty.ofReadWrite(
        this, "baseCurrency", FXVolatilitySwapSecurity.class, Currency.class);
    /**
     * The meta-property for the {@code counterCurrency} property.
     */
    private final MetaProperty<Currency> _counterCurrency = DirectMetaProperty.ofReadWrite(
        this, "counterCurrency", FXVolatilitySwapSecurity.class, Currency.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "baseCurrency",
        "counterCurrency");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1093862910:  // baseCurrency
          return _baseCurrency;
        case 74837549:  // counterCurrency
          return _counterCurrency;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends FXVolatilitySwapSecurity> builder() {
      return new DirectBeanBuilder<FXVolatilitySwapSecurity>(new FXVolatilitySwapSecurity());
    }

    @Override
    public Class<? extends FXVolatilitySwapSecurity> beanType() {
      return FXVolatilitySwapSecurity.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code baseCurrency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Currency> baseCurrency() {
      return _baseCurrency;
    }

    /**
     * The meta-property for the {@code counterCurrency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Currency> counterCurrency() {
      return _counterCurrency;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1093862910:  // baseCurrency
          return ((FXVolatilitySwapSecurity) bean).getBaseCurrency();
        case 74837549:  // counterCurrency
          return ((FXVolatilitySwapSecurity) bean).getCounterCurrency();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1093862910:  // baseCurrency
          ((FXVolatilitySwapSecurity) bean).setBaseCurrency((Currency) newValue);
          return;
        case 74837549:  // counterCurrency
          ((FXVolatilitySwapSecurity) bean).setCounterCurrency((Currency) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((FXVolatilitySwapSecurity) bean)._baseCurrency, "baseCurrency");
      JodaBeanUtils.notNull(((FXVolatilitySwapSecurity) bean)._counterCurrency, "counterCurrency");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
