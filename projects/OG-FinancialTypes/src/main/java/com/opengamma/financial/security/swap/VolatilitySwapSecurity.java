/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * An abstract base class for volatility swap securities.
 */
@BeanDefinition
public abstract class VolatilitySwapSecurity extends FinancialSecurity {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * The currency of the notional.
   */
  @PropertyDefinition(validate = "notNull")
  private Currency _currency;

  /**
   * The notional.
   */
  @PropertyDefinition
  private double _notional;

  /**
   * The volatility swap type.
   */
  @PropertyDefinition(validate = "notNull")
  private VolatilitySwapType _volatilitySwapType;

  /**
   * The strike.
   */
  @PropertyDefinition
  private double _strike;

  /**
   * The settlement date.
   */
  @PropertyDefinition(validate = "notNull")
  private ZonedDateTime _settlementDate;

  /**
   * The maturity date.
   */
  @PropertyDefinition(validate = "notNull")
  private ZonedDateTime _maturityDate;

  /**
   * The annualization factor.
   */
  @PropertyDefinition
  private double _annualizationFactor;

  /**
   * The first observation date.
   */
  @PropertyDefinition(validate = "notNull")
  private ZonedDateTime _firstObservationDate;

  /**
   * The last observation date.
   */
  @PropertyDefinition(validate = "notNull")
  private ZonedDateTime _lastObservationDate;

  /**
   * The observation frequency.
   */
  @PropertyDefinition(validate = "notNull")
  private Frequency _observationFrequency;

  /**
   * The id of the underlying time series.
   */
  @PropertyDefinition
  private ExternalId _underlyingId;

  /**
   * For the builder.
   * @param securityType The security type string, not null or empty
   */
  protected VolatilitySwapSecurity(final String securityType) {
    super(securityType);
  }

  /**
   * @param securityType The security type string, not null or empty
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
   */
  public VolatilitySwapSecurity(final String securityType, final Currency currency, final double notional, final VolatilitySwapType volatilitySwapType,
      final double strike, final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double annualizationFactor,
      final ZonedDateTime firstObservationDate, final ZonedDateTime lastObservationDate, final Frequency observationFrequency) {
    super(securityType);
    setCurrency(currency);
    setNotional(notional);
    setVolatilitySwapType(volatilitySwapType);
    setStrike(strike);
    setSettlementDate(settlementDate);
    setMaturityDate(maturityDate);
    setAnnualizationFactor(annualizationFactor);
    setFirstObservationDate(firstObservationDate);
    setLastObservationDate(lastObservationDate);
    setObservationFrequency(observationFrequency);
    setUnderlyingId(null);
  }

  /**
   * @param securityType The security type string, not null or empty
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
   * @param underlyingId The id of the underlying, not null
   */
  public VolatilitySwapSecurity(final String securityType, final Currency currency, final double notional, final VolatilitySwapType volatilitySwapType,
      final double strike, final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final double annualizationFactor,
      final ZonedDateTime firstObservationDate, final ZonedDateTime lastObservationDate, final Frequency observationFrequency,
      final ExternalId underlyingId) {
    super(securityType);
    setCurrency(currency);
    setNotional(notional);
    setVolatilitySwapType(volatilitySwapType);
    setStrike(strike);
    setSettlementDate(settlementDate);
    setMaturityDate(maturityDate);
    setAnnualizationFactor(annualizationFactor);
    setFirstObservationDate(firstObservationDate);
    setLastObservationDate(lastObservationDate);
    setObservationFrequency(observationFrequency);
    setUnderlyingId(underlyingId);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code VolatilitySwapSecurity}.
   * @return the meta-bean, not null
   */
  public static VolatilitySwapSecurity.Meta meta() {
    return VolatilitySwapSecurity.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(VolatilitySwapSecurity.Meta.INSTANCE);
  }

  @Override
  public VolatilitySwapSecurity.Meta metaBean() {
    return VolatilitySwapSecurity.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the currency of the notional.
   * @return the value of the property, not null
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Sets the currency of the notional.
   * @param currency  the new value of the property, not null
   */
  public void setCurrency(Currency currency) {
    JodaBeanUtils.notNull(currency, "currency");
    this._currency = currency;
  }

  /**
   * Gets the the {@code currency} property.
   * @return the property, not null
   */
  public final Property<Currency> currency() {
    return metaBean().currency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the notional.
   * @return the value of the property
   */
  public double getNotional() {
    return _notional;
  }

  /**
   * Sets the notional.
   * @param notional  the new value of the property
   */
  public void setNotional(double notional) {
    this._notional = notional;
  }

  /**
   * Gets the the {@code notional} property.
   * @return the property, not null
   */
  public final Property<Double> notional() {
    return metaBean().notional().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the volatility swap type.
   * @return the value of the property, not null
   */
  public VolatilitySwapType getVolatilitySwapType() {
    return _volatilitySwapType;
  }

  /**
   * Sets the volatility swap type.
   * @param volatilitySwapType  the new value of the property, not null
   */
  public void setVolatilitySwapType(VolatilitySwapType volatilitySwapType) {
    JodaBeanUtils.notNull(volatilitySwapType, "volatilitySwapType");
    this._volatilitySwapType = volatilitySwapType;
  }

  /**
   * Gets the the {@code volatilitySwapType} property.
   * @return the property, not null
   */
  public final Property<VolatilitySwapType> volatilitySwapType() {
    return metaBean().volatilitySwapType().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the strike.
   * @return the value of the property
   */
  public double getStrike() {
    return _strike;
  }

  /**
   * Sets the strike.
   * @param strike  the new value of the property
   */
  public void setStrike(double strike) {
    this._strike = strike;
  }

  /**
   * Gets the the {@code strike} property.
   * @return the property, not null
   */
  public final Property<Double> strike() {
    return metaBean().strike().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the settlement date.
   * @return the value of the property, not null
   */
  public ZonedDateTime getSettlementDate() {
    return _settlementDate;
  }

  /**
   * Sets the settlement date.
   * @param settlementDate  the new value of the property, not null
   */
  public void setSettlementDate(ZonedDateTime settlementDate) {
    JodaBeanUtils.notNull(settlementDate, "settlementDate");
    this._settlementDate = settlementDate;
  }

  /**
   * Gets the the {@code settlementDate} property.
   * @return the property, not null
   */
  public final Property<ZonedDateTime> settlementDate() {
    return metaBean().settlementDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the maturity date.
   * @return the value of the property, not null
   */
  public ZonedDateTime getMaturityDate() {
    return _maturityDate;
  }

  /**
   * Sets the maturity date.
   * @param maturityDate  the new value of the property, not null
   */
  public void setMaturityDate(ZonedDateTime maturityDate) {
    JodaBeanUtils.notNull(maturityDate, "maturityDate");
    this._maturityDate = maturityDate;
  }

  /**
   * Gets the the {@code maturityDate} property.
   * @return the property, not null
   */
  public final Property<ZonedDateTime> maturityDate() {
    return metaBean().maturityDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the annualization factor.
   * @return the value of the property
   */
  public double getAnnualizationFactor() {
    return _annualizationFactor;
  }

  /**
   * Sets the annualization factor.
   * @param annualizationFactor  the new value of the property
   */
  public void setAnnualizationFactor(double annualizationFactor) {
    this._annualizationFactor = annualizationFactor;
  }

  /**
   * Gets the the {@code annualizationFactor} property.
   * @return the property, not null
   */
  public final Property<Double> annualizationFactor() {
    return metaBean().annualizationFactor().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the first observation date.
   * @return the value of the property, not null
   */
  public ZonedDateTime getFirstObservationDate() {
    return _firstObservationDate;
  }

  /**
   * Sets the first observation date.
   * @param firstObservationDate  the new value of the property, not null
   */
  public void setFirstObservationDate(ZonedDateTime firstObservationDate) {
    JodaBeanUtils.notNull(firstObservationDate, "firstObservationDate");
    this._firstObservationDate = firstObservationDate;
  }

  /**
   * Gets the the {@code firstObservationDate} property.
   * @return the property, not null
   */
  public final Property<ZonedDateTime> firstObservationDate() {
    return metaBean().firstObservationDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the last observation date.
   * @return the value of the property, not null
   */
  public ZonedDateTime getLastObservationDate() {
    return _lastObservationDate;
  }

  /**
   * Sets the last observation date.
   * @param lastObservationDate  the new value of the property, not null
   */
  public void setLastObservationDate(ZonedDateTime lastObservationDate) {
    JodaBeanUtils.notNull(lastObservationDate, "lastObservationDate");
    this._lastObservationDate = lastObservationDate;
  }

  /**
   * Gets the the {@code lastObservationDate} property.
   * @return the property, not null
   */
  public final Property<ZonedDateTime> lastObservationDate() {
    return metaBean().lastObservationDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the observation frequency.
   * @return the value of the property, not null
   */
  public Frequency getObservationFrequency() {
    return _observationFrequency;
  }

  /**
   * Sets the observation frequency.
   * @param observationFrequency  the new value of the property, not null
   */
  public void setObservationFrequency(Frequency observationFrequency) {
    JodaBeanUtils.notNull(observationFrequency, "observationFrequency");
    this._observationFrequency = observationFrequency;
  }

  /**
   * Gets the the {@code observationFrequency} property.
   * @return the property, not null
   */
  public final Property<Frequency> observationFrequency() {
    return metaBean().observationFrequency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the id of the underlying time series.
   * @return the value of the property
   */
  public ExternalId getUnderlyingId() {
    return _underlyingId;
  }

  /**
   * Sets the id of the underlying time series.
   * @param underlyingId  the new value of the property
   */
  public void setUnderlyingId(ExternalId underlyingId) {
    this._underlyingId = underlyingId;
  }

  /**
   * Gets the the {@code underlyingId} property.
   * @return the property, not null
   */
  public final Property<ExternalId> underlyingId() {
    return metaBean().underlyingId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      VolatilitySwapSecurity other = (VolatilitySwapSecurity) obj;
      return JodaBeanUtils.equal(getCurrency(), other.getCurrency()) &&
          JodaBeanUtils.equal(getNotional(), other.getNotional()) &&
          JodaBeanUtils.equal(getVolatilitySwapType(), other.getVolatilitySwapType()) &&
          JodaBeanUtils.equal(getStrike(), other.getStrike()) &&
          JodaBeanUtils.equal(getSettlementDate(), other.getSettlementDate()) &&
          JodaBeanUtils.equal(getMaturityDate(), other.getMaturityDate()) &&
          JodaBeanUtils.equal(getAnnualizationFactor(), other.getAnnualizationFactor()) &&
          JodaBeanUtils.equal(getFirstObservationDate(), other.getFirstObservationDate()) &&
          JodaBeanUtils.equal(getLastObservationDate(), other.getLastObservationDate()) &&
          JodaBeanUtils.equal(getObservationFrequency(), other.getObservationFrequency()) &&
          JodaBeanUtils.equal(getUnderlyingId(), other.getUnderlyingId()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getCurrency());
    hash = hash * 31 + JodaBeanUtils.hashCode(getNotional());
    hash = hash * 31 + JodaBeanUtils.hashCode(getVolatilitySwapType());
    hash = hash * 31 + JodaBeanUtils.hashCode(getStrike());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSettlementDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getMaturityDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getAnnualizationFactor());
    hash = hash * 31 + JodaBeanUtils.hashCode(getFirstObservationDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getLastObservationDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getObservationFrequency());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUnderlyingId());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(384);
    buf.append("VolatilitySwapSecurity{");
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
    buf.append("currency").append('=').append(JodaBeanUtils.toString(getCurrency())).append(',').append(' ');
    buf.append("notional").append('=').append(JodaBeanUtils.toString(getNotional())).append(',').append(' ');
    buf.append("volatilitySwapType").append('=').append(JodaBeanUtils.toString(getVolatilitySwapType())).append(',').append(' ');
    buf.append("strike").append('=').append(JodaBeanUtils.toString(getStrike())).append(',').append(' ');
    buf.append("settlementDate").append('=').append(JodaBeanUtils.toString(getSettlementDate())).append(',').append(' ');
    buf.append("maturityDate").append('=').append(JodaBeanUtils.toString(getMaturityDate())).append(',').append(' ');
    buf.append("annualizationFactor").append('=').append(JodaBeanUtils.toString(getAnnualizationFactor())).append(',').append(' ');
    buf.append("firstObservationDate").append('=').append(JodaBeanUtils.toString(getFirstObservationDate())).append(',').append(' ');
    buf.append("lastObservationDate").append('=').append(JodaBeanUtils.toString(getLastObservationDate())).append(',').append(' ');
    buf.append("observationFrequency").append('=').append(JodaBeanUtils.toString(getObservationFrequency())).append(',').append(' ');
    buf.append("underlyingId").append('=').append(JodaBeanUtils.toString(getUnderlyingId())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code VolatilitySwapSecurity}.
   */
  public static class Meta extends FinancialSecurity.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code currency} property.
     */
    private final MetaProperty<Currency> _currency = DirectMetaProperty.ofReadWrite(
        this, "currency", VolatilitySwapSecurity.class, Currency.class);
    /**
     * The meta-property for the {@code notional} property.
     */
    private final MetaProperty<Double> _notional = DirectMetaProperty.ofReadWrite(
        this, "notional", VolatilitySwapSecurity.class, Double.TYPE);
    /**
     * The meta-property for the {@code volatilitySwapType} property.
     */
    private final MetaProperty<VolatilitySwapType> _volatilitySwapType = DirectMetaProperty.ofReadWrite(
        this, "volatilitySwapType", VolatilitySwapSecurity.class, VolatilitySwapType.class);
    /**
     * The meta-property for the {@code strike} property.
     */
    private final MetaProperty<Double> _strike = DirectMetaProperty.ofReadWrite(
        this, "strike", VolatilitySwapSecurity.class, Double.TYPE);
    /**
     * The meta-property for the {@code settlementDate} property.
     */
    private final MetaProperty<ZonedDateTime> _settlementDate = DirectMetaProperty.ofReadWrite(
        this, "settlementDate", VolatilitySwapSecurity.class, ZonedDateTime.class);
    /**
     * The meta-property for the {@code maturityDate} property.
     */
    private final MetaProperty<ZonedDateTime> _maturityDate = DirectMetaProperty.ofReadWrite(
        this, "maturityDate", VolatilitySwapSecurity.class, ZonedDateTime.class);
    /**
     * The meta-property for the {@code annualizationFactor} property.
     */
    private final MetaProperty<Double> _annualizationFactor = DirectMetaProperty.ofReadWrite(
        this, "annualizationFactor", VolatilitySwapSecurity.class, Double.TYPE);
    /**
     * The meta-property for the {@code firstObservationDate} property.
     */
    private final MetaProperty<ZonedDateTime> _firstObservationDate = DirectMetaProperty.ofReadWrite(
        this, "firstObservationDate", VolatilitySwapSecurity.class, ZonedDateTime.class);
    /**
     * The meta-property for the {@code lastObservationDate} property.
     */
    private final MetaProperty<ZonedDateTime> _lastObservationDate = DirectMetaProperty.ofReadWrite(
        this, "lastObservationDate", VolatilitySwapSecurity.class, ZonedDateTime.class);
    /**
     * The meta-property for the {@code observationFrequency} property.
     */
    private final MetaProperty<Frequency> _observationFrequency = DirectMetaProperty.ofReadWrite(
        this, "observationFrequency", VolatilitySwapSecurity.class, Frequency.class);
    /**
     * The meta-property for the {@code underlyingId} property.
     */
    private final MetaProperty<ExternalId> _underlyingId = DirectMetaProperty.ofReadWrite(
        this, "underlyingId", VolatilitySwapSecurity.class, ExternalId.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "currency",
        "notional",
        "volatilitySwapType",
        "strike",
        "settlementDate",
        "maturityDate",
        "annualizationFactor",
        "firstObservationDate",
        "lastObservationDate",
        "observationFrequency",
        "underlyingId");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 575402001:  // currency
          return _currency;
        case 1585636160:  // notional
          return _notional;
        case -32659790:  // volatilitySwapType
          return _volatilitySwapType;
        case -891985998:  // strike
          return _strike;
        case -295948169:  // settlementDate
          return _settlementDate;
        case -414641441:  // maturityDate
          return _maturityDate;
        case 663363412:  // annualizationFactor
          return _annualizationFactor;
        case -1644595926:  // firstObservationDate
          return _firstObservationDate;
        case -1362285436:  // lastObservationDate
          return _lastObservationDate;
        case -213041520:  // observationFrequency
          return _observationFrequency;
        case -771625640:  // underlyingId
          return _underlyingId;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends VolatilitySwapSecurity> builder() {
      throw new UnsupportedOperationException("VolatilitySwapSecurity is an abstract class");
    }

    @Override
    public Class<? extends VolatilitySwapSecurity> beanType() {
      return VolatilitySwapSecurity.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code currency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Currency> currency() {
      return _currency;
    }

    /**
     * The meta-property for the {@code notional} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> notional() {
      return _notional;
    }

    /**
     * The meta-property for the {@code volatilitySwapType} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<VolatilitySwapType> volatilitySwapType() {
      return _volatilitySwapType;
    }

    /**
     * The meta-property for the {@code strike} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> strike() {
      return _strike;
    }

    /**
     * The meta-property for the {@code settlementDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZonedDateTime> settlementDate() {
      return _settlementDate;
    }

    /**
     * The meta-property for the {@code maturityDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZonedDateTime> maturityDate() {
      return _maturityDate;
    }

    /**
     * The meta-property for the {@code annualizationFactor} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> annualizationFactor() {
      return _annualizationFactor;
    }

    /**
     * The meta-property for the {@code firstObservationDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZonedDateTime> firstObservationDate() {
      return _firstObservationDate;
    }

    /**
     * The meta-property for the {@code lastObservationDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZonedDateTime> lastObservationDate() {
      return _lastObservationDate;
    }

    /**
     * The meta-property for the {@code observationFrequency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Frequency> observationFrequency() {
      return _observationFrequency;
    }

    /**
     * The meta-property for the {@code underlyingId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> underlyingId() {
      return _underlyingId;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 575402001:  // currency
          return ((VolatilitySwapSecurity) bean).getCurrency();
        case 1585636160:  // notional
          return ((VolatilitySwapSecurity) bean).getNotional();
        case -32659790:  // volatilitySwapType
          return ((VolatilitySwapSecurity) bean).getVolatilitySwapType();
        case -891985998:  // strike
          return ((VolatilitySwapSecurity) bean).getStrike();
        case -295948169:  // settlementDate
          return ((VolatilitySwapSecurity) bean).getSettlementDate();
        case -414641441:  // maturityDate
          return ((VolatilitySwapSecurity) bean).getMaturityDate();
        case 663363412:  // annualizationFactor
          return ((VolatilitySwapSecurity) bean).getAnnualizationFactor();
        case -1644595926:  // firstObservationDate
          return ((VolatilitySwapSecurity) bean).getFirstObservationDate();
        case -1362285436:  // lastObservationDate
          return ((VolatilitySwapSecurity) bean).getLastObservationDate();
        case -213041520:  // observationFrequency
          return ((VolatilitySwapSecurity) bean).getObservationFrequency();
        case -771625640:  // underlyingId
          return ((VolatilitySwapSecurity) bean).getUnderlyingId();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 575402001:  // currency
          ((VolatilitySwapSecurity) bean).setCurrency((Currency) newValue);
          return;
        case 1585636160:  // notional
          ((VolatilitySwapSecurity) bean).setNotional((Double) newValue);
          return;
        case -32659790:  // volatilitySwapType
          ((VolatilitySwapSecurity) bean).setVolatilitySwapType((VolatilitySwapType) newValue);
          return;
        case -891985998:  // strike
          ((VolatilitySwapSecurity) bean).setStrike((Double) newValue);
          return;
        case -295948169:  // settlementDate
          ((VolatilitySwapSecurity) bean).setSettlementDate((ZonedDateTime) newValue);
          return;
        case -414641441:  // maturityDate
          ((VolatilitySwapSecurity) bean).setMaturityDate((ZonedDateTime) newValue);
          return;
        case 663363412:  // annualizationFactor
          ((VolatilitySwapSecurity) bean).setAnnualizationFactor((Double) newValue);
          return;
        case -1644595926:  // firstObservationDate
          ((VolatilitySwapSecurity) bean).setFirstObservationDate((ZonedDateTime) newValue);
          return;
        case -1362285436:  // lastObservationDate
          ((VolatilitySwapSecurity) bean).setLastObservationDate((ZonedDateTime) newValue);
          return;
        case -213041520:  // observationFrequency
          ((VolatilitySwapSecurity) bean).setObservationFrequency((Frequency) newValue);
          return;
        case -771625640:  // underlyingId
          ((VolatilitySwapSecurity) bean).setUnderlyingId((ExternalId) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((VolatilitySwapSecurity) bean)._currency, "currency");
      JodaBeanUtils.notNull(((VolatilitySwapSecurity) bean)._volatilitySwapType, "volatilitySwapType");
      JodaBeanUtils.notNull(((VolatilitySwapSecurity) bean)._settlementDate, "settlementDate");
      JodaBeanUtils.notNull(((VolatilitySwapSecurity) bean)._maturityDate, "maturityDate");
      JodaBeanUtils.notNull(((VolatilitySwapSecurity) bean)._firstObservationDate, "firstObservationDate");
      JodaBeanUtils.notNull(((VolatilitySwapSecurity) bean)._lastObservationDate, "lastObservationDate");
      JodaBeanUtils.notNull(((VolatilitySwapSecurity) bean)._observationFrequency, "observationFrequency");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
