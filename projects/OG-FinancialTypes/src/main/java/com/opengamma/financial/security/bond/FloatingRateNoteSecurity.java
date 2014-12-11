/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.bond;

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

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.SecurityDescription;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * A security for floating rate notes.
 */
@BeanDefinition
@SecurityDescription(type = FloatingRateNoteSecurity.SECURITY_TYPE, description = "Floating rate note")
public class FloatingRateNoteSecurity extends FinancialSecurity {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * The security type for floating rate notes.
   */
  public static final String SECURITY_TYPE = "FLOATING_RATE_NOTE";

  /**
   * The currency.
   */
  @PropertyDefinition(validate = "notNull")
  private Currency _currency;

  /**
   * The maturity date.
   */
  @PropertyDefinition(validate = "notNull")
  private Expiry _maturityDate;

  /**
   * The issue date.
   */
  @PropertyDefinition(validate = "notNull")
  private ZonedDateTime _issueDate;

  /**
   * The minimum increment.
   */
  @PropertyDefinition
  private double _minimumIncrement;

  /**
   * The number of settlement days.
   */
  @PropertyDefinition
  private int _daysToSettle;

  /**
   * The number of days prior to the coupon when the floating rate is reset.
   */
  @PropertyDefinition
  private int _resetDays;

  /**
   * The day count.
   */
  @PropertyDefinition(validate = "notNull")
  private DayCount _dayCount;

  /**
   * The region id.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalId _regionId;

  /**
   * The legal entity identifier.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalId _legalEntityId;

  /**
   * The benchmark rate identifier.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalId _benchmarkRateId;

  /**
   * The spread over the benchmark rate.
   */
  @PropertyDefinition
  private double _spread;

  /**
   * The leverage factor.
   */
  @PropertyDefinition
  private double _leverageFactor = 1;

  /**
   * The coupon frequency.
   */
  @PropertyDefinition(validate = "notNull")
  private Frequency _couponFrequency;

  /**
   * For the builder.
   */
  /* package */ FloatingRateNoteSecurity() {
    super(SECURITY_TYPE);
  }

  /**
   * Creates a floating rate note with a leverage factor of one.
   * @param currency The currency, not null
   * @param maturityDate The maturity date, not null
   * @param issueDate The issue date, not null
   * @param minimumIncrement The minimum increment
   * @param daysToSettle The number of days to settle
   * @param resetDays The number of days prior to the coupon payment when the
   * floating rate is reset
   * @param dayCount The day count, not null
   * @param regionId The region identifier, not null
   * @param legalEntityId The legal entity identifier, not null
   * @param benchmarkRateId The benchmark rate identifier, not null
   * @param spread The spread
   * @param couponFrequency The coupon frequency, not null
   */
  public FloatingRateNoteSecurity(final Currency currency, final Expiry maturityDate, final ZonedDateTime issueDate, final double minimumIncrement,
      final int daysToSettle, final int resetDays, final DayCount dayCount, final ExternalId regionId, final ExternalId legalEntityId,
      final ExternalId benchmarkRateId, final double spread, final Frequency couponFrequency) {
    super(SECURITY_TYPE);
    setCurrency(currency);
    setMaturityDate(maturityDate);
    setIssueDate(issueDate);
    setMinimumIncrement(minimumIncrement);
    setDaysToSettle(daysToSettle);
    setResetDays(resetDays);
    setDayCount(dayCount);
    setRegionId(regionId);
    setLegalEntityId(legalEntityId);
    setBenchmarkRateId(benchmarkRateId);
    setSpread(spread);
    setCouponFrequency(couponFrequency);
  }

  /**
   * Creates a floating rate note with a leverage factor of one.
   * @param currency The currency, not null
   * @param maturityDate The maturity date, not null
   * @param issueDate The issue date, not null
   * @param minimumIncrement The minimum increment
   * @param daysToSettle The number of days to settle
   * @param resetDays The number of days prior to the coupon payment when the
   * floating rate is reset
   * @param dayCount The day count, not null
   * @param regionId The region identifier, not null
   * @param legalEntityId The legal entity identifier, not null
   * @param benchmarkRateId The benchmark rate identifier, not null
   * @param spread The spread
   * @param leverageFactor The leverage factor
   * @param couponFrequency The coupon frequency, not null
   */
  public FloatingRateNoteSecurity(final Currency currency, final Expiry maturityDate, final ZonedDateTime issueDate, final double minimumIncrement,
      final int daysToSettle, final int resetDays, final DayCount dayCount, final ExternalId regionId, final ExternalId legalEntityId,
      final ExternalId benchmarkRateId, final double spread, final double leverageFactor, final Frequency couponFrequency) {
    super(SECURITY_TYPE);
    setCurrency(currency);
    setMaturityDate(maturityDate);
    setIssueDate(issueDate);
    setMinimumIncrement(minimumIncrement);
    setDaysToSettle(daysToSettle);
    setResetDays(resetDays);
    setDayCount(dayCount);
    setRegionId(regionId);
    setLegalEntityId(legalEntityId);
    setBenchmarkRateId(benchmarkRateId);
    setSpread(spread);
    setLeverageFactor(leverageFactor);
    setCouponFrequency(couponFrequency);
  }

  @Override
  public <T> T accept(final FinancialSecurityVisitor<T> visitor) {
    return visitor.visitFloatingRateNoteSecurity(this);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FloatingRateNoteSecurity}.
   * @return the meta-bean, not null
   */
  public static FloatingRateNoteSecurity.Meta meta() {
    return FloatingRateNoteSecurity.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FloatingRateNoteSecurity.Meta.INSTANCE);
  }

  @Override
  public FloatingRateNoteSecurity.Meta metaBean() {
    return FloatingRateNoteSecurity.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the currency.
   * @return the value of the property, not null
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Sets the currency.
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
   * Gets the maturity date.
   * @return the value of the property, not null
   */
  public Expiry getMaturityDate() {
    return _maturityDate;
  }

  /**
   * Sets the maturity date.
   * @param maturityDate  the new value of the property, not null
   */
  public void setMaturityDate(Expiry maturityDate) {
    JodaBeanUtils.notNull(maturityDate, "maturityDate");
    this._maturityDate = maturityDate;
  }

  /**
   * Gets the the {@code maturityDate} property.
   * @return the property, not null
   */
  public final Property<Expiry> maturityDate() {
    return metaBean().maturityDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the issue date.
   * @return the value of the property, not null
   */
  public ZonedDateTime getIssueDate() {
    return _issueDate;
  }

  /**
   * Sets the issue date.
   * @param issueDate  the new value of the property, not null
   */
  public void setIssueDate(ZonedDateTime issueDate) {
    JodaBeanUtils.notNull(issueDate, "issueDate");
    this._issueDate = issueDate;
  }

  /**
   * Gets the the {@code issueDate} property.
   * @return the property, not null
   */
  public final Property<ZonedDateTime> issueDate() {
    return metaBean().issueDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the minimum increment.
   * @return the value of the property
   */
  public double getMinimumIncrement() {
    return _minimumIncrement;
  }

  /**
   * Sets the minimum increment.
   * @param minimumIncrement  the new value of the property
   */
  public void setMinimumIncrement(double minimumIncrement) {
    this._minimumIncrement = minimumIncrement;
  }

  /**
   * Gets the the {@code minimumIncrement} property.
   * @return the property, not null
   */
  public final Property<Double> minimumIncrement() {
    return metaBean().minimumIncrement().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the number of settlement days.
   * @return the value of the property
   */
  public int getDaysToSettle() {
    return _daysToSettle;
  }

  /**
   * Sets the number of settlement days.
   * @param daysToSettle  the new value of the property
   */
  public void setDaysToSettle(int daysToSettle) {
    this._daysToSettle = daysToSettle;
  }

  /**
   * Gets the the {@code daysToSettle} property.
   * @return the property, not null
   */
  public final Property<Integer> daysToSettle() {
    return metaBean().daysToSettle().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the number of days prior to the coupon when the floating rate is reset.
   * @return the value of the property
   */
  public int getResetDays() {
    return _resetDays;
  }

  /**
   * Sets the number of days prior to the coupon when the floating rate is reset.
   * @param resetDays  the new value of the property
   */
  public void setResetDays(int resetDays) {
    this._resetDays = resetDays;
  }

  /**
   * Gets the the {@code resetDays} property.
   * @return the property, not null
   */
  public final Property<Integer> resetDays() {
    return metaBean().resetDays().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the day count.
   * @return the value of the property, not null
   */
  public DayCount getDayCount() {
    return _dayCount;
  }

  /**
   * Sets the day count.
   * @param dayCount  the new value of the property, not null
   */
  public void setDayCount(DayCount dayCount) {
    JodaBeanUtils.notNull(dayCount, "dayCount");
    this._dayCount = dayCount;
  }

  /**
   * Gets the the {@code dayCount} property.
   * @return the property, not null
   */
  public final Property<DayCount> dayCount() {
    return metaBean().dayCount().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the region id.
   * @return the value of the property, not null
   */
  public ExternalId getRegionId() {
    return _regionId;
  }

  /**
   * Sets the region id.
   * @param regionId  the new value of the property, not null
   */
  public void setRegionId(ExternalId regionId) {
    JodaBeanUtils.notNull(regionId, "regionId");
    this._regionId = regionId;
  }

  /**
   * Gets the the {@code regionId} property.
   * @return the property, not null
   */
  public final Property<ExternalId> regionId() {
    return metaBean().regionId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the legal entity identifier.
   * @return the value of the property, not null
   */
  public ExternalId getLegalEntityId() {
    return _legalEntityId;
  }

  /**
   * Sets the legal entity identifier.
   * @param legalEntityId  the new value of the property, not null
   */
  public void setLegalEntityId(ExternalId legalEntityId) {
    JodaBeanUtils.notNull(legalEntityId, "legalEntityId");
    this._legalEntityId = legalEntityId;
  }

  /**
   * Gets the the {@code legalEntityId} property.
   * @return the property, not null
   */
  public final Property<ExternalId> legalEntityId() {
    return metaBean().legalEntityId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the benchmark rate identifier.
   * @return the value of the property, not null
   */
  public ExternalId getBenchmarkRateId() {
    return _benchmarkRateId;
  }

  /**
   * Sets the benchmark rate identifier.
   * @param benchmarkRateId  the new value of the property, not null
   */
  public void setBenchmarkRateId(ExternalId benchmarkRateId) {
    JodaBeanUtils.notNull(benchmarkRateId, "benchmarkRateId");
    this._benchmarkRateId = benchmarkRateId;
  }

  /**
   * Gets the the {@code benchmarkRateId} property.
   * @return the property, not null
   */
  public final Property<ExternalId> benchmarkRateId() {
    return metaBean().benchmarkRateId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the spread over the benchmark rate.
   * @return the value of the property
   */
  public double getSpread() {
    return _spread;
  }

  /**
   * Sets the spread over the benchmark rate.
   * @param spread  the new value of the property
   */
  public void setSpread(double spread) {
    this._spread = spread;
  }

  /**
   * Gets the the {@code spread} property.
   * @return the property, not null
   */
  public final Property<Double> spread() {
    return metaBean().spread().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the leverage factor.
   * @return the value of the property
   */
  public double getLeverageFactor() {
    return _leverageFactor;
  }

  /**
   * Sets the leverage factor.
   * @param leverageFactor  the new value of the property
   */
  public void setLeverageFactor(double leverageFactor) {
    this._leverageFactor = leverageFactor;
  }

  /**
   * Gets the the {@code leverageFactor} property.
   * @return the property, not null
   */
  public final Property<Double> leverageFactor() {
    return metaBean().leverageFactor().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the coupon frequency.
   * @return the value of the property, not null
   */
  public Frequency getCouponFrequency() {
    return _couponFrequency;
  }

  /**
   * Sets the coupon frequency.
   * @param couponFrequency  the new value of the property, not null
   */
  public void setCouponFrequency(Frequency couponFrequency) {
    JodaBeanUtils.notNull(couponFrequency, "couponFrequency");
    this._couponFrequency = couponFrequency;
  }

  /**
   * Gets the the {@code couponFrequency} property.
   * @return the property, not null
   */
  public final Property<Frequency> couponFrequency() {
    return metaBean().couponFrequency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public FloatingRateNoteSecurity clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      FloatingRateNoteSecurity other = (FloatingRateNoteSecurity) obj;
      return JodaBeanUtils.equal(getCurrency(), other.getCurrency()) &&
          JodaBeanUtils.equal(getMaturityDate(), other.getMaturityDate()) &&
          JodaBeanUtils.equal(getIssueDate(), other.getIssueDate()) &&
          JodaBeanUtils.equal(getMinimumIncrement(), other.getMinimumIncrement()) &&
          (getDaysToSettle() == other.getDaysToSettle()) &&
          (getResetDays() == other.getResetDays()) &&
          JodaBeanUtils.equal(getDayCount(), other.getDayCount()) &&
          JodaBeanUtils.equal(getRegionId(), other.getRegionId()) &&
          JodaBeanUtils.equal(getLegalEntityId(), other.getLegalEntityId()) &&
          JodaBeanUtils.equal(getBenchmarkRateId(), other.getBenchmarkRateId()) &&
          JodaBeanUtils.equal(getSpread(), other.getSpread()) &&
          JodaBeanUtils.equal(getLeverageFactor(), other.getLeverageFactor()) &&
          JodaBeanUtils.equal(getCouponFrequency(), other.getCouponFrequency()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getCurrency());
    hash = hash * 31 + JodaBeanUtils.hashCode(getMaturityDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getIssueDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getMinimumIncrement());
    hash = hash * 31 + JodaBeanUtils.hashCode(getDaysToSettle());
    hash = hash * 31 + JodaBeanUtils.hashCode(getResetDays());
    hash = hash * 31 + JodaBeanUtils.hashCode(getDayCount());
    hash = hash * 31 + JodaBeanUtils.hashCode(getRegionId());
    hash = hash * 31 + JodaBeanUtils.hashCode(getLegalEntityId());
    hash = hash * 31 + JodaBeanUtils.hashCode(getBenchmarkRateId());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSpread());
    hash = hash * 31 + JodaBeanUtils.hashCode(getLeverageFactor());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCouponFrequency());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(448);
    buf.append("FloatingRateNoteSecurity{");
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
    buf.append("maturityDate").append('=').append(JodaBeanUtils.toString(getMaturityDate())).append(',').append(' ');
    buf.append("issueDate").append('=').append(JodaBeanUtils.toString(getIssueDate())).append(',').append(' ');
    buf.append("minimumIncrement").append('=').append(JodaBeanUtils.toString(getMinimumIncrement())).append(',').append(' ');
    buf.append("daysToSettle").append('=').append(JodaBeanUtils.toString(getDaysToSettle())).append(',').append(' ');
    buf.append("resetDays").append('=').append(JodaBeanUtils.toString(getResetDays())).append(',').append(' ');
    buf.append("dayCount").append('=').append(JodaBeanUtils.toString(getDayCount())).append(',').append(' ');
    buf.append("regionId").append('=').append(JodaBeanUtils.toString(getRegionId())).append(',').append(' ');
    buf.append("legalEntityId").append('=').append(JodaBeanUtils.toString(getLegalEntityId())).append(',').append(' ');
    buf.append("benchmarkRateId").append('=').append(JodaBeanUtils.toString(getBenchmarkRateId())).append(',').append(' ');
    buf.append("spread").append('=').append(JodaBeanUtils.toString(getSpread())).append(',').append(' ');
    buf.append("leverageFactor").append('=').append(JodaBeanUtils.toString(getLeverageFactor())).append(',').append(' ');
    buf.append("couponFrequency").append('=').append(JodaBeanUtils.toString(getCouponFrequency())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FloatingRateNoteSecurity}.
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
        this, "currency", FloatingRateNoteSecurity.class, Currency.class);
    /**
     * The meta-property for the {@code maturityDate} property.
     */
    private final MetaProperty<Expiry> _maturityDate = DirectMetaProperty.ofReadWrite(
        this, "maturityDate", FloatingRateNoteSecurity.class, Expiry.class);
    /**
     * The meta-property for the {@code issueDate} property.
     */
    private final MetaProperty<ZonedDateTime> _issueDate = DirectMetaProperty.ofReadWrite(
        this, "issueDate", FloatingRateNoteSecurity.class, ZonedDateTime.class);
    /**
     * The meta-property for the {@code minimumIncrement} property.
     */
    private final MetaProperty<Double> _minimumIncrement = DirectMetaProperty.ofReadWrite(
        this, "minimumIncrement", FloatingRateNoteSecurity.class, Double.TYPE);
    /**
     * The meta-property for the {@code daysToSettle} property.
     */
    private final MetaProperty<Integer> _daysToSettle = DirectMetaProperty.ofReadWrite(
        this, "daysToSettle", FloatingRateNoteSecurity.class, Integer.TYPE);
    /**
     * The meta-property for the {@code resetDays} property.
     */
    private final MetaProperty<Integer> _resetDays = DirectMetaProperty.ofReadWrite(
        this, "resetDays", FloatingRateNoteSecurity.class, Integer.TYPE);
    /**
     * The meta-property for the {@code dayCount} property.
     */
    private final MetaProperty<DayCount> _dayCount = DirectMetaProperty.ofReadWrite(
        this, "dayCount", FloatingRateNoteSecurity.class, DayCount.class);
    /**
     * The meta-property for the {@code regionId} property.
     */
    private final MetaProperty<ExternalId> _regionId = DirectMetaProperty.ofReadWrite(
        this, "regionId", FloatingRateNoteSecurity.class, ExternalId.class);
    /**
     * The meta-property for the {@code legalEntityId} property.
     */
    private final MetaProperty<ExternalId> _legalEntityId = DirectMetaProperty.ofReadWrite(
        this, "legalEntityId", FloatingRateNoteSecurity.class, ExternalId.class);
    /**
     * The meta-property for the {@code benchmarkRateId} property.
     */
    private final MetaProperty<ExternalId> _benchmarkRateId = DirectMetaProperty.ofReadWrite(
        this, "benchmarkRateId", FloatingRateNoteSecurity.class, ExternalId.class);
    /**
     * The meta-property for the {@code spread} property.
     */
    private final MetaProperty<Double> _spread = DirectMetaProperty.ofReadWrite(
        this, "spread", FloatingRateNoteSecurity.class, Double.TYPE);
    /**
     * The meta-property for the {@code leverageFactor} property.
     */
    private final MetaProperty<Double> _leverageFactor = DirectMetaProperty.ofReadWrite(
        this, "leverageFactor", FloatingRateNoteSecurity.class, Double.TYPE);
    /**
     * The meta-property for the {@code couponFrequency} property.
     */
    private final MetaProperty<Frequency> _couponFrequency = DirectMetaProperty.ofReadWrite(
        this, "couponFrequency", FloatingRateNoteSecurity.class, Frequency.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "currency",
        "maturityDate",
        "issueDate",
        "minimumIncrement",
        "daysToSettle",
        "resetDays",
        "dayCount",
        "regionId",
        "legalEntityId",
        "benchmarkRateId",
        "spread",
        "leverageFactor",
        "couponFrequency");

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
        case -414641441:  // maturityDate
          return _maturityDate;
        case 184285223:  // issueDate
          return _issueDate;
        case 1160465153:  // minimumIncrement
          return _minimumIncrement;
        case 379523357:  // daysToSettle
          return _daysToSettle;
        case 2023309894:  // resetDays
          return _resetDays;
        case 1905311443:  // dayCount
          return _dayCount;
        case -690339025:  // regionId
          return _regionId;
        case 866287159:  // legalEntityId
          return _legalEntityId;
        case -1768547976:  // benchmarkRateId
          return _benchmarkRateId;
        case -895684237:  // spread
          return _spread;
        case -882302236:  // leverageFactor
          return _leverageFactor;
        case 144480214:  // couponFrequency
          return _couponFrequency;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends FloatingRateNoteSecurity> builder() {
      return new DirectBeanBuilder<FloatingRateNoteSecurity>(new FloatingRateNoteSecurity());
    }

    @Override
    public Class<? extends FloatingRateNoteSecurity> beanType() {
      return FloatingRateNoteSecurity.class;
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
     * The meta-property for the {@code maturityDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Expiry> maturityDate() {
      return _maturityDate;
    }

    /**
     * The meta-property for the {@code issueDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZonedDateTime> issueDate() {
      return _issueDate;
    }

    /**
     * The meta-property for the {@code minimumIncrement} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> minimumIncrement() {
      return _minimumIncrement;
    }

    /**
     * The meta-property for the {@code daysToSettle} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> daysToSettle() {
      return _daysToSettle;
    }

    /**
     * The meta-property for the {@code resetDays} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> resetDays() {
      return _resetDays;
    }

    /**
     * The meta-property for the {@code dayCount} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DayCount> dayCount() {
      return _dayCount;
    }

    /**
     * The meta-property for the {@code regionId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> regionId() {
      return _regionId;
    }

    /**
     * The meta-property for the {@code legalEntityId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> legalEntityId() {
      return _legalEntityId;
    }

    /**
     * The meta-property for the {@code benchmarkRateId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> benchmarkRateId() {
      return _benchmarkRateId;
    }

    /**
     * The meta-property for the {@code spread} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> spread() {
      return _spread;
    }

    /**
     * The meta-property for the {@code leverageFactor} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> leverageFactor() {
      return _leverageFactor;
    }

    /**
     * The meta-property for the {@code couponFrequency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Frequency> couponFrequency() {
      return _couponFrequency;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 575402001:  // currency
          return ((FloatingRateNoteSecurity) bean).getCurrency();
        case -414641441:  // maturityDate
          return ((FloatingRateNoteSecurity) bean).getMaturityDate();
        case 184285223:  // issueDate
          return ((FloatingRateNoteSecurity) bean).getIssueDate();
        case 1160465153:  // minimumIncrement
          return ((FloatingRateNoteSecurity) bean).getMinimumIncrement();
        case 379523357:  // daysToSettle
          return ((FloatingRateNoteSecurity) bean).getDaysToSettle();
        case 2023309894:  // resetDays
          return ((FloatingRateNoteSecurity) bean).getResetDays();
        case 1905311443:  // dayCount
          return ((FloatingRateNoteSecurity) bean).getDayCount();
        case -690339025:  // regionId
          return ((FloatingRateNoteSecurity) bean).getRegionId();
        case 866287159:  // legalEntityId
          return ((FloatingRateNoteSecurity) bean).getLegalEntityId();
        case -1768547976:  // benchmarkRateId
          return ((FloatingRateNoteSecurity) bean).getBenchmarkRateId();
        case -895684237:  // spread
          return ((FloatingRateNoteSecurity) bean).getSpread();
        case -882302236:  // leverageFactor
          return ((FloatingRateNoteSecurity) bean).getLeverageFactor();
        case 144480214:  // couponFrequency
          return ((FloatingRateNoteSecurity) bean).getCouponFrequency();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 575402001:  // currency
          ((FloatingRateNoteSecurity) bean).setCurrency((Currency) newValue);
          return;
        case -414641441:  // maturityDate
          ((FloatingRateNoteSecurity) bean).setMaturityDate((Expiry) newValue);
          return;
        case 184285223:  // issueDate
          ((FloatingRateNoteSecurity) bean).setIssueDate((ZonedDateTime) newValue);
          return;
        case 1160465153:  // minimumIncrement
          ((FloatingRateNoteSecurity) bean).setMinimumIncrement((Double) newValue);
          return;
        case 379523357:  // daysToSettle
          ((FloatingRateNoteSecurity) bean).setDaysToSettle((Integer) newValue);
          return;
        case 2023309894:  // resetDays
          ((FloatingRateNoteSecurity) bean).setResetDays((Integer) newValue);
          return;
        case 1905311443:  // dayCount
          ((FloatingRateNoteSecurity) bean).setDayCount((DayCount) newValue);
          return;
        case -690339025:  // regionId
          ((FloatingRateNoteSecurity) bean).setRegionId((ExternalId) newValue);
          return;
        case 866287159:  // legalEntityId
          ((FloatingRateNoteSecurity) bean).setLegalEntityId((ExternalId) newValue);
          return;
        case -1768547976:  // benchmarkRateId
          ((FloatingRateNoteSecurity) bean).setBenchmarkRateId((ExternalId) newValue);
          return;
        case -895684237:  // spread
          ((FloatingRateNoteSecurity) bean).setSpread((Double) newValue);
          return;
        case -882302236:  // leverageFactor
          ((FloatingRateNoteSecurity) bean).setLeverageFactor((Double) newValue);
          return;
        case 144480214:  // couponFrequency
          ((FloatingRateNoteSecurity) bean).setCouponFrequency((Frequency) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((FloatingRateNoteSecurity) bean)._currency, "currency");
      JodaBeanUtils.notNull(((FloatingRateNoteSecurity) bean)._maturityDate, "maturityDate");
      JodaBeanUtils.notNull(((FloatingRateNoteSecurity) bean)._issueDate, "issueDate");
      JodaBeanUtils.notNull(((FloatingRateNoteSecurity) bean)._dayCount, "dayCount");
      JodaBeanUtils.notNull(((FloatingRateNoteSecurity) bean)._regionId, "regionId");
      JodaBeanUtils.notNull(((FloatingRateNoteSecurity) bean)._legalEntityId, "legalEntityId");
      JodaBeanUtils.notNull(((FloatingRateNoteSecurity) bean)._benchmarkRateId, "benchmarkRateId");
      JodaBeanUtils.notNull(((FloatingRateNoteSecurity) bean)._couponFrequency, "couponFrequency");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
