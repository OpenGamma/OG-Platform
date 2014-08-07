/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.fra;

import java.util.Map;
import java.util.Set;

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
import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.SecurityDescription;
import com.opengamma.util.money.Currency;

/**
 * A security for FRAs.
 */
@BeanDefinition
@SecurityDescription(type = ForwardRateAgreementSecurity.SECURITY_TYPE, description = "Forward rate agreement")
public class ForwardRateAgreementSecurity extends FinancialSecurity {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The security type.
   */
  public static final String SECURITY_TYPE = "ForwardRateAgreement";

  /**
   * The currency.
   */
  @PropertyDefinition(validate = "notNull")
  private Currency _currency;
  /**
   * The fixing calendars.
   */
  @PropertyDefinition(validate = "notNull")
  private Set<ExternalId> _calendars;
  /**
   * The payment calendars.
   */
  @PropertyDefinition()
  private Set<ExternalId> _paymentCalendars;
  /**
   * The start date.
   */
  @PropertyDefinition(validate = "notNull")
  private LocalDate _startDate;
  /**
   * The end date.
   */
  @PropertyDefinition(validate = "notNull")
  private LocalDate _endDate;
  /**
   * The fixing date.
   * 
   * If fixing date is null, the ForwardRateAgreementDefinition will calculate the fixing date from the start date, the
   * index spot lag and the fixing calendar.
   */
  @PropertyDefinition
  private LocalDate _fixingDate;
  /**
   * The rate.
   */
  @PropertyDefinition
  private double _rate;
  /**
   * The amount.
   */
  @PropertyDefinition
  private double _amount;

  /**
   * The underlying identifier.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalId _underlyingId;

  /**
   * The underlying identifier.
   */
  @PropertyDefinition(validate = "notNull")
  private Frequency _indexFrequency;

  /**
   * The day count convention
   */
  @PropertyDefinition(validate = "notNull")
  private DayCount _dayCount;

  /**
   * The fixing business day convention
   */
  @PropertyDefinition(validate = "notNull")
  private BusinessDayConvention _fixingBusinessDayConvention;

  /**
   * The fixing lag (generally 0 or 2)
   */
  @PropertyDefinition(validate = "notNull")
  private Integer _fixingLag;

  ForwardRateAgreementSecurity() { //For builder
    super(SECURITY_TYPE);
  }

  /**
   * Creates an instance
   *
   * @param currency  the currency, not null.
   * @param underlyingId  the id of the underlying index (assumed Ibor), not null
   * @param indexFrequency  the index frequency, not null
   * @param startDate  the start date, not null
   * @param endDate  the end date, not null
   * @param rate  the rate
   * @param amount  the amount (-ve if payer)
   * @param fixingDate  the fixing date, not null
   * @param dayCount  the day count convention, not null
   * @param fixingBusinessDayConvention  the business dya convention, not null
   * @param calendars  the calendars to be used, not null
   * @param fixingLag  the fixing lag
   */
  public ForwardRateAgreementSecurity(Currency currency,
                                      ExternalId underlyingId,
                                      Frequency indexFrequency,
                                      LocalDate startDate,
                                      LocalDate endDate,
                                      double rate,
                                      double amount,
                                      LocalDate fixingDate,
                                      DayCount dayCount,
                                      BusinessDayConvention fixingBusinessDayConvention,
                                      Set<ExternalId> calendars,
                                      int fixingLag) {
    super(SECURITY_TYPE);
    setExternalIdBundle(ExternalIdBundle.EMPTY);
    setCurrency(currency);
    setStartDate(startDate);
    setEndDate(endDate);
    setRate(rate);
    setAmount(amount);
    setIndexFrequency(indexFrequency);
    setUnderlyingId(underlyingId);
    setFixingDate(fixingDate);
    setDayCount(dayCount);
    setFixingBusinessDayConvention(fixingBusinessDayConvention);
    setCalendars(calendars);
    setFixingLag(fixingLag);
  }

  /**
   * Creates an instance
   *
   * @param currency  the currency, not null.
   * @param underlyingId  the id of the underlying index (assumed Ibor), not null
   * @param indexFrequency  the index frequency, not null
   * @param startDate  the start date, not null
   * @param endDate  the end date, not null
   * @param rate  the rate
   * @param amount  the amount (-ve if payer)
   * @param fixingDate  the fixing date, not null
   * @param dayCount  the day count convention, not null
   * @param fixingBusinessDayConvention  the business dya convention, not null
   * @param fixingCalendars  the calendars to be used, not null
   * @param paymentCalendars the payment calendars, if null the fixing calendars will be used
   * @param fixingLag  the fixing lag
   */
  public ForwardRateAgreementSecurity(Currency currency,
                                      ExternalId underlyingId,
                                      Frequency indexFrequency,
                                      LocalDate startDate,
                                      LocalDate endDate,
                                      double rate,
                                      double amount,
                                      LocalDate fixingDate,
                                      DayCount dayCount,
                                      BusinessDayConvention fixingBusinessDayConvention,
                                      Set<ExternalId> fixingCalendars, Set<ExternalId> paymentCalendars,
                                      int fixingLag) {
    super(SECURITY_TYPE);
    setExternalIdBundle(ExternalIdBundle.EMPTY);
    setCurrency(currency);
    setStartDate(startDate);
    setEndDate(endDate);
    setRate(rate);
    setAmount(amount);
    setIndexFrequency(indexFrequency);
    setUnderlyingId(underlyingId);
    setFixingDate(fixingDate);
    setDayCount(dayCount);
    setFixingBusinessDayConvention(fixingBusinessDayConvention);
    setCalendars(fixingCalendars);
    setPaymentCalendars(paymentCalendars);
    setFixingLag(fixingLag);
  }

  /**
   * Creates an instance
   *
   * @param currency  the currency, not null.
   * @param underlyingId  the id of the underlying index (assumed Ibor), not null
   * @param indexFrequency  the index frequency, not null
   * @param startDate  the start date, not null
   * @param endDate  the end date, not null
   * @param rate  the rate
   * @param amount  the amount (-ve if payer)
   * @param dayCount  the day count convention, not null
   * @param fixingBusinessDayConvention  the business dya convention, not null
   * @param fixingCalendars  the calendars to be used, not null
   * @param paymentCalendars the payment calendars, if null the fixing calendars will be used
   * @param fixingLag  the fixing lag
   */
  public ForwardRateAgreementSecurity(Currency currency,
                                      ExternalId underlyingId,
                                      Frequency indexFrequency,
                                      LocalDate startDate,
                                      LocalDate endDate,
                                      double rate,
                                      double amount,
                                      DayCount dayCount,
                                      BusinessDayConvention fixingBusinessDayConvention,
                                      Set<ExternalId> fixingCalendars, Set<ExternalId> paymentCalendars,
                                      int fixingLag) {
    super(SECURITY_TYPE);
    setExternalIdBundle(ExternalIdBundle.EMPTY);
    setCurrency(currency);
    setStartDate(startDate);
    setEndDate(endDate);
    setRate(rate);
    setAmount(amount);
    setIndexFrequency(indexFrequency);
    setUnderlyingId(underlyingId);
    setDayCount(dayCount);
    setFixingBusinessDayConvention(fixingBusinessDayConvention);
    setCalendars(fixingCalendars);
    setPaymentCalendars(paymentCalendars);
    setFixingLag(fixingLag);
  }

  //-------------------------------------------------------------------------
  @Override
  public final <T> T accept(FinancialSecurityVisitor<T> visitor) {
    return visitor.visitForwardRateAgreementSecurity(this);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ForwardRateAgreementSecurity}.
   * @return the meta-bean, not null
   */
  public static ForwardRateAgreementSecurity.Meta meta() {
    return ForwardRateAgreementSecurity.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ForwardRateAgreementSecurity.Meta.INSTANCE);
  }

  @Override
  public ForwardRateAgreementSecurity.Meta metaBean() {
    return ForwardRateAgreementSecurity.Meta.INSTANCE;
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
   * Gets the fixing calendars.
   * @return the value of the property, not null
   */
  public Set<ExternalId> getCalendars() {
    return _calendars;
  }

  /**
   * Sets the fixing calendars.
   * @param calendars  the new value of the property, not null
   */
  public void setCalendars(Set<ExternalId> calendars) {
    JodaBeanUtils.notNull(calendars, "calendars");
    this._calendars = calendars;
  }

  /**
   * Gets the the {@code calendars} property.
   * @return the property, not null
   */
  public final Property<Set<ExternalId>> calendars() {
    return metaBean().calendars().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the payment calendars.
   * @return the value of the property
   */
  public Set<ExternalId> getPaymentCalendars() {
    return _paymentCalendars;
  }

  /**
   * Sets the payment calendars.
   * @param paymentCalendars  the new value of the property
   */
  public void setPaymentCalendars(Set<ExternalId> paymentCalendars) {
    this._paymentCalendars = paymentCalendars;
  }

  /**
   * Gets the the {@code paymentCalendars} property.
   * @return the property, not null
   */
  public final Property<Set<ExternalId>> paymentCalendars() {
    return metaBean().paymentCalendars().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the start date.
   * @return the value of the property, not null
   */
  public LocalDate getStartDate() {
    return _startDate;
  }

  /**
   * Sets the start date.
   * @param startDate  the new value of the property, not null
   */
  public void setStartDate(LocalDate startDate) {
    JodaBeanUtils.notNull(startDate, "startDate");
    this._startDate = startDate;
  }

  /**
   * Gets the the {@code startDate} property.
   * @return the property, not null
   */
  public final Property<LocalDate> startDate() {
    return metaBean().startDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the end date.
   * @return the value of the property, not null
   */
  public LocalDate getEndDate() {
    return _endDate;
  }

  /**
   * Sets the end date.
   * @param endDate  the new value of the property, not null
   */
  public void setEndDate(LocalDate endDate) {
    JodaBeanUtils.notNull(endDate, "endDate");
    this._endDate = endDate;
  }

  /**
   * Gets the the {@code endDate} property.
   * @return the property, not null
   */
  public final Property<LocalDate> endDate() {
    return metaBean().endDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the fixing date.
   * 
   * If fixing date is null, the ForwardRateAgreementDefinition will calculate the fixing date from the start date, the
   * index spot lag and the fixing calendar.
   * @return the value of the property
   */
  public LocalDate getFixingDate() {
    return _fixingDate;
  }

  /**
   * Sets the fixing date.
   * 
   * If fixing date is null, the ForwardRateAgreementDefinition will calculate the fixing date from the start date, the
   * index spot lag and the fixing calendar.
   * @param fixingDate  the new value of the property
   */
  public void setFixingDate(LocalDate fixingDate) {
    this._fixingDate = fixingDate;
  }

  /**
   * Gets the the {@code fixingDate} property.
   * 
   * If fixing date is null, the ForwardRateAgreementDefinition will calculate the fixing date from the start date, the
   * index spot lag and the fixing calendar.
   * @return the property, not null
   */
  public final Property<LocalDate> fixingDate() {
    return metaBean().fixingDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the rate.
   * @return the value of the property
   */
  public double getRate() {
    return _rate;
  }

  /**
   * Sets the rate.
   * @param rate  the new value of the property
   */
  public void setRate(double rate) {
    this._rate = rate;
  }

  /**
   * Gets the the {@code rate} property.
   * @return the property, not null
   */
  public final Property<Double> rate() {
    return metaBean().rate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the amount.
   * @return the value of the property
   */
  public double getAmount() {
    return _amount;
  }

  /**
   * Sets the amount.
   * @param amount  the new value of the property
   */
  public void setAmount(double amount) {
    this._amount = amount;
  }

  /**
   * Gets the the {@code amount} property.
   * @return the property, not null
   */
  public final Property<Double> amount() {
    return metaBean().amount().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the underlying identifier.
   * @return the value of the property, not null
   */
  public ExternalId getUnderlyingId() {
    return _underlyingId;
  }

  /**
   * Sets the underlying identifier.
   * @param underlyingId  the new value of the property, not null
   */
  public void setUnderlyingId(ExternalId underlyingId) {
    JodaBeanUtils.notNull(underlyingId, "underlyingId");
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
  /**
   * Gets the underlying identifier.
   * @return the value of the property, not null
   */
  public Frequency getIndexFrequency() {
    return _indexFrequency;
  }

  /**
   * Sets the underlying identifier.
   * @param indexFrequency  the new value of the property, not null
   */
  public void setIndexFrequency(Frequency indexFrequency) {
    JodaBeanUtils.notNull(indexFrequency, "indexFrequency");
    this._indexFrequency = indexFrequency;
  }

  /**
   * Gets the the {@code indexFrequency} property.
   * @return the property, not null
   */
  public final Property<Frequency> indexFrequency() {
    return metaBean().indexFrequency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the day count convention
   * @return the value of the property, not null
   */
  public DayCount getDayCount() {
    return _dayCount;
  }

  /**
   * Sets the day count convention
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
   * Gets the fixing business day convention
   * @return the value of the property, not null
   */
  public BusinessDayConvention getFixingBusinessDayConvention() {
    return _fixingBusinessDayConvention;
  }

  /**
   * Sets the fixing business day convention
   * @param fixingBusinessDayConvention  the new value of the property, not null
   */
  public void setFixingBusinessDayConvention(BusinessDayConvention fixingBusinessDayConvention) {
    JodaBeanUtils.notNull(fixingBusinessDayConvention, "fixingBusinessDayConvention");
    this._fixingBusinessDayConvention = fixingBusinessDayConvention;
  }

  /**
   * Gets the the {@code fixingBusinessDayConvention} property.
   * @return the property, not null
   */
  public final Property<BusinessDayConvention> fixingBusinessDayConvention() {
    return metaBean().fixingBusinessDayConvention().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the fixing lag (generally 0 or 2)
   * @return the value of the property, not null
   */
  public Integer getFixingLag() {
    return _fixingLag;
  }

  /**
   * Sets the fixing lag (generally 0 or 2)
   * @param fixingLag  the new value of the property, not null
   */
  public void setFixingLag(Integer fixingLag) {
    JodaBeanUtils.notNull(fixingLag, "fixingLag");
    this._fixingLag = fixingLag;
  }

  /**
   * Gets the the {@code fixingLag} property.
   * @return the property, not null
   */
  public final Property<Integer> fixingLag() {
    return metaBean().fixingLag().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ForwardRateAgreementSecurity clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ForwardRateAgreementSecurity other = (ForwardRateAgreementSecurity) obj;
      return JodaBeanUtils.equal(getCurrency(), other.getCurrency()) &&
          JodaBeanUtils.equal(getCalendars(), other.getCalendars()) &&
          JodaBeanUtils.equal(getPaymentCalendars(), other.getPaymentCalendars()) &&
          JodaBeanUtils.equal(getStartDate(), other.getStartDate()) &&
          JodaBeanUtils.equal(getEndDate(), other.getEndDate()) &&
          JodaBeanUtils.equal(getFixingDate(), other.getFixingDate()) &&
          JodaBeanUtils.equal(getRate(), other.getRate()) &&
          JodaBeanUtils.equal(getAmount(), other.getAmount()) &&
          JodaBeanUtils.equal(getUnderlyingId(), other.getUnderlyingId()) &&
          JodaBeanUtils.equal(getIndexFrequency(), other.getIndexFrequency()) &&
          JodaBeanUtils.equal(getDayCount(), other.getDayCount()) &&
          JodaBeanUtils.equal(getFixingBusinessDayConvention(), other.getFixingBusinessDayConvention()) &&
          JodaBeanUtils.equal(getFixingLag(), other.getFixingLag()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getCurrency());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCalendars());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPaymentCalendars());
    hash += hash * 31 + JodaBeanUtils.hashCode(getStartDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getEndDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFixingDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAmount());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUnderlyingId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getIndexFrequency());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDayCount());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFixingBusinessDayConvention());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFixingLag());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(448);
    buf.append("ForwardRateAgreementSecurity{");
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
    buf.append("calendars").append('=').append(JodaBeanUtils.toString(getCalendars())).append(',').append(' ');
    buf.append("paymentCalendars").append('=').append(JodaBeanUtils.toString(getPaymentCalendars())).append(',').append(' ');
    buf.append("startDate").append('=').append(JodaBeanUtils.toString(getStartDate())).append(',').append(' ');
    buf.append("endDate").append('=').append(JodaBeanUtils.toString(getEndDate())).append(',').append(' ');
    buf.append("fixingDate").append('=').append(JodaBeanUtils.toString(getFixingDate())).append(',').append(' ');
    buf.append("rate").append('=').append(JodaBeanUtils.toString(getRate())).append(',').append(' ');
    buf.append("amount").append('=').append(JodaBeanUtils.toString(getAmount())).append(',').append(' ');
    buf.append("underlyingId").append('=').append(JodaBeanUtils.toString(getUnderlyingId())).append(',').append(' ');
    buf.append("indexFrequency").append('=').append(JodaBeanUtils.toString(getIndexFrequency())).append(',').append(' ');
    buf.append("dayCount").append('=').append(JodaBeanUtils.toString(getDayCount())).append(',').append(' ');
    buf.append("fixingBusinessDayConvention").append('=').append(JodaBeanUtils.toString(getFixingBusinessDayConvention())).append(',').append(' ');
    buf.append("fixingLag").append('=').append(JodaBeanUtils.toString(getFixingLag())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ForwardRateAgreementSecurity}.
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
        this, "currency", ForwardRateAgreementSecurity.class, Currency.class);
    /**
     * The meta-property for the {@code calendars} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<ExternalId>> _calendars = DirectMetaProperty.ofReadWrite(
        this, "calendars", ForwardRateAgreementSecurity.class, (Class) Set.class);
    /**
     * The meta-property for the {@code paymentCalendars} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<ExternalId>> _paymentCalendars = DirectMetaProperty.ofReadWrite(
        this, "paymentCalendars", ForwardRateAgreementSecurity.class, (Class) Set.class);
    /**
     * The meta-property for the {@code startDate} property.
     */
    private final MetaProperty<LocalDate> _startDate = DirectMetaProperty.ofReadWrite(
        this, "startDate", ForwardRateAgreementSecurity.class, LocalDate.class);
    /**
     * The meta-property for the {@code endDate} property.
     */
    private final MetaProperty<LocalDate> _endDate = DirectMetaProperty.ofReadWrite(
        this, "endDate", ForwardRateAgreementSecurity.class, LocalDate.class);
    /**
     * The meta-property for the {@code fixingDate} property.
     */
    private final MetaProperty<LocalDate> _fixingDate = DirectMetaProperty.ofReadWrite(
        this, "fixingDate", ForwardRateAgreementSecurity.class, LocalDate.class);
    /**
     * The meta-property for the {@code rate} property.
     */
    private final MetaProperty<Double> _rate = DirectMetaProperty.ofReadWrite(
        this, "rate", ForwardRateAgreementSecurity.class, Double.TYPE);
    /**
     * The meta-property for the {@code amount} property.
     */
    private final MetaProperty<Double> _amount = DirectMetaProperty.ofReadWrite(
        this, "amount", ForwardRateAgreementSecurity.class, Double.TYPE);
    /**
     * The meta-property for the {@code underlyingId} property.
     */
    private final MetaProperty<ExternalId> _underlyingId = DirectMetaProperty.ofReadWrite(
        this, "underlyingId", ForwardRateAgreementSecurity.class, ExternalId.class);
    /**
     * The meta-property for the {@code indexFrequency} property.
     */
    private final MetaProperty<Frequency> _indexFrequency = DirectMetaProperty.ofReadWrite(
        this, "indexFrequency", ForwardRateAgreementSecurity.class, Frequency.class);
    /**
     * The meta-property for the {@code dayCount} property.
     */
    private final MetaProperty<DayCount> _dayCount = DirectMetaProperty.ofReadWrite(
        this, "dayCount", ForwardRateAgreementSecurity.class, DayCount.class);
    /**
     * The meta-property for the {@code fixingBusinessDayConvention} property.
     */
    private final MetaProperty<BusinessDayConvention> _fixingBusinessDayConvention = DirectMetaProperty.ofReadWrite(
        this, "fixingBusinessDayConvention", ForwardRateAgreementSecurity.class, BusinessDayConvention.class);
    /**
     * The meta-property for the {@code fixingLag} property.
     */
    private final MetaProperty<Integer> _fixingLag = DirectMetaProperty.ofReadWrite(
        this, "fixingLag", ForwardRateAgreementSecurity.class, Integer.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "currency",
        "calendars",
        "paymentCalendars",
        "startDate",
        "endDate",
        "fixingDate",
        "rate",
        "amount",
        "underlyingId",
        "indexFrequency",
        "dayCount",
        "fixingBusinessDayConvention",
        "fixingLag");

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
        case -1233097483:  // calendars
          return _calendars;
        case -299417201:  // paymentCalendars
          return _paymentCalendars;
        case -2129778896:  // startDate
          return _startDate;
        case -1607727319:  // endDate
          return _endDate;
        case 1255202043:  // fixingDate
          return _fixingDate;
        case 3493088:  // rate
          return _rate;
        case -1413853096:  // amount
          return _amount;
        case -771625640:  // underlyingId
          return _underlyingId;
        case -711571286:  // indexFrequency
          return _indexFrequency;
        case 1905311443:  // dayCount
          return _dayCount;
        case 502310560:  // fixingBusinessDayConvention
          return _fixingBusinessDayConvention;
        case 871782053:  // fixingLag
          return _fixingLag;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ForwardRateAgreementSecurity> builder() {
      return new DirectBeanBuilder<ForwardRateAgreementSecurity>(new ForwardRateAgreementSecurity());
    }

    @Override
    public Class<? extends ForwardRateAgreementSecurity> beanType() {
      return ForwardRateAgreementSecurity.class;
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
     * The meta-property for the {@code calendars} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<ExternalId>> calendars() {
      return _calendars;
    }

    /**
     * The meta-property for the {@code paymentCalendars} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<ExternalId>> paymentCalendars() {
      return _paymentCalendars;
    }

    /**
     * The meta-property for the {@code startDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> startDate() {
      return _startDate;
    }

    /**
     * The meta-property for the {@code endDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> endDate() {
      return _endDate;
    }

    /**
     * The meta-property for the {@code fixingDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> fixingDate() {
      return _fixingDate;
    }

    /**
     * The meta-property for the {@code rate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> rate() {
      return _rate;
    }

    /**
     * The meta-property for the {@code amount} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> amount() {
      return _amount;
    }

    /**
     * The meta-property for the {@code underlyingId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> underlyingId() {
      return _underlyingId;
    }

    /**
     * The meta-property for the {@code indexFrequency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Frequency> indexFrequency() {
      return _indexFrequency;
    }

    /**
     * The meta-property for the {@code dayCount} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DayCount> dayCount() {
      return _dayCount;
    }

    /**
     * The meta-property for the {@code fixingBusinessDayConvention} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BusinessDayConvention> fixingBusinessDayConvention() {
      return _fixingBusinessDayConvention;
    }

    /**
     * The meta-property for the {@code fixingLag} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> fixingLag() {
      return _fixingLag;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 575402001:  // currency
          return ((ForwardRateAgreementSecurity) bean).getCurrency();
        case -1233097483:  // calendars
          return ((ForwardRateAgreementSecurity) bean).getCalendars();
        case -299417201:  // paymentCalendars
          return ((ForwardRateAgreementSecurity) bean).getPaymentCalendars();
        case -2129778896:  // startDate
          return ((ForwardRateAgreementSecurity) bean).getStartDate();
        case -1607727319:  // endDate
          return ((ForwardRateAgreementSecurity) bean).getEndDate();
        case 1255202043:  // fixingDate
          return ((ForwardRateAgreementSecurity) bean).getFixingDate();
        case 3493088:  // rate
          return ((ForwardRateAgreementSecurity) bean).getRate();
        case -1413853096:  // amount
          return ((ForwardRateAgreementSecurity) bean).getAmount();
        case -771625640:  // underlyingId
          return ((ForwardRateAgreementSecurity) bean).getUnderlyingId();
        case -711571286:  // indexFrequency
          return ((ForwardRateAgreementSecurity) bean).getIndexFrequency();
        case 1905311443:  // dayCount
          return ((ForwardRateAgreementSecurity) bean).getDayCount();
        case 502310560:  // fixingBusinessDayConvention
          return ((ForwardRateAgreementSecurity) bean).getFixingBusinessDayConvention();
        case 871782053:  // fixingLag
          return ((ForwardRateAgreementSecurity) bean).getFixingLag();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 575402001:  // currency
          ((ForwardRateAgreementSecurity) bean).setCurrency((Currency) newValue);
          return;
        case -1233097483:  // calendars
          ((ForwardRateAgreementSecurity) bean).setCalendars((Set<ExternalId>) newValue);
          return;
        case -299417201:  // paymentCalendars
          ((ForwardRateAgreementSecurity) bean).setPaymentCalendars((Set<ExternalId>) newValue);
          return;
        case -2129778896:  // startDate
          ((ForwardRateAgreementSecurity) bean).setStartDate((LocalDate) newValue);
          return;
        case -1607727319:  // endDate
          ((ForwardRateAgreementSecurity) bean).setEndDate((LocalDate) newValue);
          return;
        case 1255202043:  // fixingDate
          ((ForwardRateAgreementSecurity) bean).setFixingDate((LocalDate) newValue);
          return;
        case 3493088:  // rate
          ((ForwardRateAgreementSecurity) bean).setRate((Double) newValue);
          return;
        case -1413853096:  // amount
          ((ForwardRateAgreementSecurity) bean).setAmount((Double) newValue);
          return;
        case -771625640:  // underlyingId
          ((ForwardRateAgreementSecurity) bean).setUnderlyingId((ExternalId) newValue);
          return;
        case -711571286:  // indexFrequency
          ((ForwardRateAgreementSecurity) bean).setIndexFrequency((Frequency) newValue);
          return;
        case 1905311443:  // dayCount
          ((ForwardRateAgreementSecurity) bean).setDayCount((DayCount) newValue);
          return;
        case 502310560:  // fixingBusinessDayConvention
          ((ForwardRateAgreementSecurity) bean).setFixingBusinessDayConvention((BusinessDayConvention) newValue);
          return;
        case 871782053:  // fixingLag
          ((ForwardRateAgreementSecurity) bean).setFixingLag((Integer) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((ForwardRateAgreementSecurity) bean)._currency, "currency");
      JodaBeanUtils.notNull(((ForwardRateAgreementSecurity) bean)._calendars, "calendars");
      JodaBeanUtils.notNull(((ForwardRateAgreementSecurity) bean)._startDate, "startDate");
      JodaBeanUtils.notNull(((ForwardRateAgreementSecurity) bean)._endDate, "endDate");
      JodaBeanUtils.notNull(((ForwardRateAgreementSecurity) bean)._underlyingId, "underlyingId");
      JodaBeanUtils.notNull(((ForwardRateAgreementSecurity) bean)._indexFrequency, "indexFrequency");
      JodaBeanUtils.notNull(((ForwardRateAgreementSecurity) bean)._dayCount, "dayCount");
      JodaBeanUtils.notNull(((ForwardRateAgreementSecurity) bean)._fixingBusinessDayConvention, "fixingBusinessDayConvention");
      JodaBeanUtils.notNull(((ForwardRateAgreementSecurity) bean)._fixingLag, "fixingLag");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
