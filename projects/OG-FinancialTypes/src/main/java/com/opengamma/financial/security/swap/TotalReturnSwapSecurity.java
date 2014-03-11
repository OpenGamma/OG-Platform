/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.instrument.annuity.DateRelativeTo;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.financial.convention.rolldate.RollDateAdjuster;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLeg;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * Base security class for total return swaps.
 */
@BeanDefinition
public abstract class TotalReturnSwapSecurity extends FinancialSecurity {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * The funding leg.
   */
  @PropertyDefinition(validate = "notNull")
  private FloatingInterestRateSwapLeg _fundingLeg;

  /**
   * External ids of the asset being swapped.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalIdBundle _assetId;

  /**
   * The effective date.
   */
  @PropertyDefinition(validate = "notNull")
  private LocalDate _effectiveDate;

  /**
   * The (unadjusted) maturity date.
   */
  @PropertyDefinition(validate = "notNull")
  private LocalDate _maturityDate;

  /**
   * The number of payment settlement days.
   */
  @PropertyDefinition
  private int _paymentSettlementDays;

  /**
   * The calendars used to adjust the return payment dates.
   */
  @PropertyDefinition(validate = "notNull")
  private final Set<ExternalId> _paymentDateCalendar = new HashSet<>();

  /**
   * The business day convention used for return payment dates.
   */
  @PropertyDefinition(validate = "notNull")
  private BusinessDayConvention _paymentBusinessDayConvention;

  /**
   * Flag that describes whether the reset date is relative to the start or end of the accrual periods.
   */
  @PropertyDefinition(validate = "notNull")
  private final DateRelativeTo _resetDateRelativeTo = DateRelativeTo.START;

  /**
   * The frequency of the return payment dates.
   */
  @PropertyDefinition(validate = "notNull")
  private Frequency _paymentFrequency;

  /**
   * The roll convention of the return payment dates.
   */
  @PropertyDefinition(validate = "notNull")
  private RollConvention _rollConvention;

  /**
   * The number for which custom payment dates are provided.
   */
  @PropertyDefinition
  private int[] _dates;

  /**
   * The custom payment dates.
   */
  @PropertyDefinition
  private LocalDate[] _paymentDates;

  /**
   * Sets only the security type.
   * @param securityType The security type string.
   */
  protected TotalReturnSwapSecurity(final String securityType) {
    super(securityType);
  }

  /**
   * @param securityType The security type string, not null
   * @param fundingLeg The funding leg, not null
   * @param assetId The asset external id bundle, not null
   * @param effectiveDate The effective date, not null
   * @param maturityDate The maturity date, not null
   * @param paymentSettlementDays The number of days to settle for the payments
   * @param paymentBusinessDayConvention The business day convention for the payments, not null
   * @param paymentFrequency The payment frequency, not null
   * @param rollConvention The payment roll convention, not null
   */
  public TotalReturnSwapSecurity(final String securityType, final FloatingInterestRateSwapLeg fundingLeg, final ExternalIdBundle assetId,
      final LocalDate effectiveDate, final LocalDate maturityDate, final int paymentSettlementDays,
      final BusinessDayConvention paymentBusinessDayConvention, final Frequency paymentFrequency, final RollConvention rollConvention) {
    super(securityType);
    setFundingLeg(fundingLeg);
    setAssetId(assetId);
    setEffectiveDate(effectiveDate);
    setMaturityDate(maturityDate);
    setPaymentSettlementDays(paymentSettlementDays);
    setPaymentBusinessDayConvention(paymentBusinessDayConvention);
    setPaymentFrequency(paymentFrequency);
    setRollConvention(rollConvention);
  }

  /**
   * Gets the <i>n</i>th payment date by returning a custom date if supplied for <i>n</i> or by
   * using the payment convention information in this security.
   * @param n The number of the payment date
   * @param startDate The start date, not null
   * @param calendar The calendar, not null
   * @return The <i>n</i>th payment date
   */
  public LocalDate getPaymentDate(final int n, final LocalDate startDate, final Calendar calendar) {
    JodaBeanUtils.notNull(startDate, "startDate");
    JodaBeanUtils.notNull(calendar, "calendar");
    if (getPaymentDates() != null && getPaymentDates().length != 0) {
      final int index = Arrays.binarySearch(_dates, n);
      if (index >= 0) {
        return _paymentDates[index];
      }
    }
    // override not provided - fall back to convention
    final int monthsToAdvance = (int) PeriodFrequency.convertToPeriodFrequency(getPaymentFrequency()).getPeriod().toTotalMonths() * n;
    final RollDateAdjuster adjuster = getRollConvention().getRollDateAdjuster(monthsToAdvance);
    final BusinessDayConvention convention = getPaymentBusinessDayConvention();
    final int settlementDays = getPaymentSettlementDays();
    return convention.adjustDate(calendar, startDate.plusMonths(adjuster.getMonthsToAdjust()).minusDays(settlementDays).with(adjuster));
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code TotalReturnSwapSecurity}.
   * @return the meta-bean, not null
   */
  public static TotalReturnSwapSecurity.Meta meta() {
    return TotalReturnSwapSecurity.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(TotalReturnSwapSecurity.Meta.INSTANCE);
  }

  @Override
  public TotalReturnSwapSecurity.Meta metaBean() {
    return TotalReturnSwapSecurity.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the funding leg.
   * @return the value of the property, not null
   */
  public FloatingInterestRateSwapLeg getFundingLeg() {
    return _fundingLeg;
  }

  /**
   * Sets the funding leg.
   * @param fundingLeg  the new value of the property, not null
   */
  public void setFundingLeg(FloatingInterestRateSwapLeg fundingLeg) {
    JodaBeanUtils.notNull(fundingLeg, "fundingLeg");
    this._fundingLeg = fundingLeg;
  }

  /**
   * Gets the the {@code fundingLeg} property.
   * @return the property, not null
   */
  public final Property<FloatingInterestRateSwapLeg> fundingLeg() {
    return metaBean().fundingLeg().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets external ids of the asset being swapped.
   * @return the value of the property, not null
   */
  public ExternalIdBundle getAssetId() {
    return _assetId;
  }

  /**
   * Sets external ids of the asset being swapped.
   * @param assetId  the new value of the property, not null
   */
  public void setAssetId(ExternalIdBundle assetId) {
    JodaBeanUtils.notNull(assetId, "assetId");
    this._assetId = assetId;
  }

  /**
   * Gets the the {@code assetId} property.
   * @return the property, not null
   */
  public final Property<ExternalIdBundle> assetId() {
    return metaBean().assetId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the effective date.
   * @return the value of the property, not null
   */
  public LocalDate getEffectiveDate() {
    return _effectiveDate;
  }

  /**
   * Sets the effective date.
   * @param effectiveDate  the new value of the property, not null
   */
  public void setEffectiveDate(LocalDate effectiveDate) {
    JodaBeanUtils.notNull(effectiveDate, "effectiveDate");
    this._effectiveDate = effectiveDate;
  }

  /**
   * Gets the the {@code effectiveDate} property.
   * @return the property, not null
   */
  public final Property<LocalDate> effectiveDate() {
    return metaBean().effectiveDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the (unadjusted) maturity date.
   * @return the value of the property, not null
   */
  public LocalDate getMaturityDate() {
    return _maturityDate;
  }

  /**
   * Sets the (unadjusted) maturity date.
   * @param maturityDate  the new value of the property, not null
   */
  public void setMaturityDate(LocalDate maturityDate) {
    JodaBeanUtils.notNull(maturityDate, "maturityDate");
    this._maturityDate = maturityDate;
  }

  /**
   * Gets the the {@code maturityDate} property.
   * @return the property, not null
   */
  public final Property<LocalDate> maturityDate() {
    return metaBean().maturityDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the number of payment settlement days.
   * @return the value of the property
   */
  public int getPaymentSettlementDays() {
    return _paymentSettlementDays;
  }

  /**
   * Sets the number of payment settlement days.
   * @param paymentSettlementDays  the new value of the property
   */
  public void setPaymentSettlementDays(int paymentSettlementDays) {
    this._paymentSettlementDays = paymentSettlementDays;
  }

  /**
   * Gets the the {@code paymentSettlementDays} property.
   * @return the property, not null
   */
  public final Property<Integer> paymentSettlementDays() {
    return metaBean().paymentSettlementDays().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the calendars used to adjust the return payment dates.
   * @return the value of the property, not null
   */
  public Set<ExternalId> getPaymentDateCalendar() {
    return _paymentDateCalendar;
  }

  /**
   * Sets the calendars used to adjust the return payment dates.
   * @param paymentDateCalendar  the new value of the property, not null
   */
  public void setPaymentDateCalendar(Set<ExternalId> paymentDateCalendar) {
    JodaBeanUtils.notNull(paymentDateCalendar, "paymentDateCalendar");
    this._paymentDateCalendar.clear();
    this._paymentDateCalendar.addAll(paymentDateCalendar);
  }

  /**
   * Gets the the {@code paymentDateCalendar} property.
   * @return the property, not null
   */
  public final Property<Set<ExternalId>> paymentDateCalendar() {
    return metaBean().paymentDateCalendar().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the business day convention used for return payment dates.
   * @return the value of the property, not null
   */
  public BusinessDayConvention getPaymentBusinessDayConvention() {
    return _paymentBusinessDayConvention;
  }

  /**
   * Sets the business day convention used for return payment dates.
   * @param paymentBusinessDayConvention  the new value of the property, not null
   */
  public void setPaymentBusinessDayConvention(BusinessDayConvention paymentBusinessDayConvention) {
    JodaBeanUtils.notNull(paymentBusinessDayConvention, "paymentBusinessDayConvention");
    this._paymentBusinessDayConvention = paymentBusinessDayConvention;
  }

  /**
   * Gets the the {@code paymentBusinessDayConvention} property.
   * @return the property, not null
   */
  public final Property<BusinessDayConvention> paymentBusinessDayConvention() {
    return metaBean().paymentBusinessDayConvention().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets flag that describes whether the reset date is relative to the start or end of the accrual periods.
   * @return the value of the property, not null
   */
  public DateRelativeTo getResetDateRelativeTo() {
    return _resetDateRelativeTo;
  }

  /**
   * Gets the the {@code resetDateRelativeTo} property.
   * @return the property, not null
   */
  public final Property<DateRelativeTo> resetDateRelativeTo() {
    return metaBean().resetDateRelativeTo().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the frequency of the return payment dates.
   * @return the value of the property, not null
   */
  public Frequency getPaymentFrequency() {
    return _paymentFrequency;
  }

  /**
   * Sets the frequency of the return payment dates.
   * @param paymentFrequency  the new value of the property, not null
   */
  public void setPaymentFrequency(Frequency paymentFrequency) {
    JodaBeanUtils.notNull(paymentFrequency, "paymentFrequency");
    this._paymentFrequency = paymentFrequency;
  }

  /**
   * Gets the the {@code paymentFrequency} property.
   * @return the property, not null
   */
  public final Property<Frequency> paymentFrequency() {
    return metaBean().paymentFrequency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the roll convention of the return payment dates.
   * @return the value of the property, not null
   */
  public RollConvention getRollConvention() {
    return _rollConvention;
  }

  /**
   * Sets the roll convention of the return payment dates.
   * @param rollConvention  the new value of the property, not null
   */
  public void setRollConvention(RollConvention rollConvention) {
    JodaBeanUtils.notNull(rollConvention, "rollConvention");
    this._rollConvention = rollConvention;
  }

  /**
   * Gets the the {@code rollConvention} property.
   * @return the property, not null
   */
  public final Property<RollConvention> rollConvention() {
    return metaBean().rollConvention().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the number for which custom payment dates are provided.
   * @return the value of the property
   */
  public int[] getDates() {
    return _dates;
  }

  /**
   * Sets the number for which custom payment dates are provided.
   * @param dates  the new value of the property
   */
  public void setDates(int[] dates) {
    this._dates = dates;
  }

  /**
   * Gets the the {@code dates} property.
   * @return the property, not null
   */
  public final Property<int[]> dates() {
    return metaBean().dates().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the custom payment dates.
   * @return the value of the property
   */
  public LocalDate[] getPaymentDates() {
    return _paymentDates;
  }

  /**
   * Sets the custom payment dates.
   * @param paymentDates  the new value of the property
   */
  public void setPaymentDates(LocalDate[] paymentDates) {
    this._paymentDates = paymentDates;
  }

  /**
   * Gets the the {@code paymentDates} property.
   * @return the property, not null
   */
  public final Property<LocalDate[]> paymentDates() {
    return metaBean().paymentDates().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      TotalReturnSwapSecurity other = (TotalReturnSwapSecurity) obj;
      return JodaBeanUtils.equal(getFundingLeg(), other.getFundingLeg()) &&
          JodaBeanUtils.equal(getAssetId(), other.getAssetId()) &&
          JodaBeanUtils.equal(getEffectiveDate(), other.getEffectiveDate()) &&
          JodaBeanUtils.equal(getMaturityDate(), other.getMaturityDate()) &&
          (getPaymentSettlementDays() == other.getPaymentSettlementDays()) &&
          JodaBeanUtils.equal(getPaymentDateCalendar(), other.getPaymentDateCalendar()) &&
          JodaBeanUtils.equal(getPaymentBusinessDayConvention(), other.getPaymentBusinessDayConvention()) &&
          JodaBeanUtils.equal(getResetDateRelativeTo(), other.getResetDateRelativeTo()) &&
          JodaBeanUtils.equal(getPaymentFrequency(), other.getPaymentFrequency()) &&
          JodaBeanUtils.equal(getRollConvention(), other.getRollConvention()) &&
          JodaBeanUtils.equal(getDates(), other.getDates()) &&
          JodaBeanUtils.equal(getPaymentDates(), other.getPaymentDates()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getFundingLeg());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAssetId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getEffectiveDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMaturityDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPaymentSettlementDays());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPaymentDateCalendar());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPaymentBusinessDayConvention());
    hash += hash * 31 + JodaBeanUtils.hashCode(getResetDateRelativeTo());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPaymentFrequency());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRollConvention());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDates());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPaymentDates());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(416);
    buf.append("TotalReturnSwapSecurity{");
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
    buf.append("fundingLeg").append('=').append(JodaBeanUtils.toString(getFundingLeg())).append(',').append(' ');
    buf.append("assetId").append('=').append(JodaBeanUtils.toString(getAssetId())).append(',').append(' ');
    buf.append("effectiveDate").append('=').append(JodaBeanUtils.toString(getEffectiveDate())).append(',').append(' ');
    buf.append("maturityDate").append('=').append(JodaBeanUtils.toString(getMaturityDate())).append(',').append(' ');
    buf.append("paymentSettlementDays").append('=').append(JodaBeanUtils.toString(getPaymentSettlementDays())).append(',').append(' ');
    buf.append("paymentDateCalendar").append('=').append(JodaBeanUtils.toString(getPaymentDateCalendar())).append(',').append(' ');
    buf.append("paymentBusinessDayConvention").append('=').append(JodaBeanUtils.toString(getPaymentBusinessDayConvention())).append(',').append(' ');
    buf.append("resetDateRelativeTo").append('=').append(JodaBeanUtils.toString(getResetDateRelativeTo())).append(',').append(' ');
    buf.append("paymentFrequency").append('=').append(JodaBeanUtils.toString(getPaymentFrequency())).append(',').append(' ');
    buf.append("rollConvention").append('=').append(JodaBeanUtils.toString(getRollConvention())).append(',').append(' ');
    buf.append("dates").append('=').append(JodaBeanUtils.toString(getDates())).append(',').append(' ');
    buf.append("paymentDates").append('=').append(JodaBeanUtils.toString(getPaymentDates())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code TotalReturnSwapSecurity}.
   */
  public static class Meta extends FinancialSecurity.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code fundingLeg} property.
     */
    private final MetaProperty<FloatingInterestRateSwapLeg> _fundingLeg = DirectMetaProperty.ofReadWrite(
        this, "fundingLeg", TotalReturnSwapSecurity.class, FloatingInterestRateSwapLeg.class);
    /**
     * The meta-property for the {@code assetId} property.
     */
    private final MetaProperty<ExternalIdBundle> _assetId = DirectMetaProperty.ofReadWrite(
        this, "assetId", TotalReturnSwapSecurity.class, ExternalIdBundle.class);
    /**
     * The meta-property for the {@code effectiveDate} property.
     */
    private final MetaProperty<LocalDate> _effectiveDate = DirectMetaProperty.ofReadWrite(
        this, "effectiveDate", TotalReturnSwapSecurity.class, LocalDate.class);
    /**
     * The meta-property for the {@code maturityDate} property.
     */
    private final MetaProperty<LocalDate> _maturityDate = DirectMetaProperty.ofReadWrite(
        this, "maturityDate", TotalReturnSwapSecurity.class, LocalDate.class);
    /**
     * The meta-property for the {@code paymentSettlementDays} property.
     */
    private final MetaProperty<Integer> _paymentSettlementDays = DirectMetaProperty.ofReadWrite(
        this, "paymentSettlementDays", TotalReturnSwapSecurity.class, Integer.TYPE);
    /**
     * The meta-property for the {@code paymentDateCalendar} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<ExternalId>> _paymentDateCalendar = DirectMetaProperty.ofReadWrite(
        this, "paymentDateCalendar", TotalReturnSwapSecurity.class, (Class) Set.class);
    /**
     * The meta-property for the {@code paymentBusinessDayConvention} property.
     */
    private final MetaProperty<BusinessDayConvention> _paymentBusinessDayConvention = DirectMetaProperty.ofReadWrite(
        this, "paymentBusinessDayConvention", TotalReturnSwapSecurity.class, BusinessDayConvention.class);
    /**
     * The meta-property for the {@code resetDateRelativeTo} property.
     */
    private final MetaProperty<DateRelativeTo> _resetDateRelativeTo = DirectMetaProperty.ofReadOnly(
        this, "resetDateRelativeTo", TotalReturnSwapSecurity.class, DateRelativeTo.class);
    /**
     * The meta-property for the {@code paymentFrequency} property.
     */
    private final MetaProperty<Frequency> _paymentFrequency = DirectMetaProperty.ofReadWrite(
        this, "paymentFrequency", TotalReturnSwapSecurity.class, Frequency.class);
    /**
     * The meta-property for the {@code rollConvention} property.
     */
    private final MetaProperty<RollConvention> _rollConvention = DirectMetaProperty.ofReadWrite(
        this, "rollConvention", TotalReturnSwapSecurity.class, RollConvention.class);
    /**
     * The meta-property for the {@code dates} property.
     */
    private final MetaProperty<int[]> _dates = DirectMetaProperty.ofReadWrite(
        this, "dates", TotalReturnSwapSecurity.class, int[].class);
    /**
     * The meta-property for the {@code paymentDates} property.
     */
    private final MetaProperty<LocalDate[]> _paymentDates = DirectMetaProperty.ofReadWrite(
        this, "paymentDates", TotalReturnSwapSecurity.class, LocalDate[].class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "fundingLeg",
        "assetId",
        "effectiveDate",
        "maturityDate",
        "paymentSettlementDays",
        "paymentDateCalendar",
        "paymentBusinessDayConvention",
        "resetDateRelativeTo",
        "paymentFrequency",
        "rollConvention",
        "dates",
        "paymentDates");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 514140625:  // fundingLeg
          return _fundingLeg;
        case -704776149:  // assetId
          return _assetId;
        case -930389515:  // effectiveDate
          return _effectiveDate;
        case -414641441:  // maturityDate
          return _maturityDate;
        case 1487154374:  // paymentSettlementDays
          return _paymentSettlementDays;
        case -724865486:  // paymentDateCalendar
          return _paymentDateCalendar;
        case -1357599257:  // paymentBusinessDayConvention
          return _paymentBusinessDayConvention;
        case 397410276:  // resetDateRelativeTo
          return _resetDateRelativeTo;
        case 863656438:  // paymentFrequency
          return _paymentFrequency;
        case -10223666:  // rollConvention
          return _rollConvention;
        case 95356549:  // dates
          return _dates;
        case -522438625:  // paymentDates
          return _paymentDates;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends TotalReturnSwapSecurity> builder() {
      throw new UnsupportedOperationException("TotalReturnSwapSecurity is an abstract class");
    }

    @Override
    public Class<? extends TotalReturnSwapSecurity> beanType() {
      return TotalReturnSwapSecurity.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code fundingLeg} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<FloatingInterestRateSwapLeg> fundingLeg() {
      return _fundingLeg;
    }

    /**
     * The meta-property for the {@code assetId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalIdBundle> assetId() {
      return _assetId;
    }

    /**
     * The meta-property for the {@code effectiveDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> effectiveDate() {
      return _effectiveDate;
    }

    /**
     * The meta-property for the {@code maturityDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> maturityDate() {
      return _maturityDate;
    }

    /**
     * The meta-property for the {@code paymentSettlementDays} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> paymentSettlementDays() {
      return _paymentSettlementDays;
    }

    /**
     * The meta-property for the {@code paymentDateCalendar} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<ExternalId>> paymentDateCalendar() {
      return _paymentDateCalendar;
    }

    /**
     * The meta-property for the {@code paymentBusinessDayConvention} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BusinessDayConvention> paymentBusinessDayConvention() {
      return _paymentBusinessDayConvention;
    }

    /**
     * The meta-property for the {@code resetDateRelativeTo} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DateRelativeTo> resetDateRelativeTo() {
      return _resetDateRelativeTo;
    }

    /**
     * The meta-property for the {@code paymentFrequency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Frequency> paymentFrequency() {
      return _paymentFrequency;
    }

    /**
     * The meta-property for the {@code rollConvention} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<RollConvention> rollConvention() {
      return _rollConvention;
    }

    /**
     * The meta-property for the {@code dates} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<int[]> dates() {
      return _dates;
    }

    /**
     * The meta-property for the {@code paymentDates} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate[]> paymentDates() {
      return _paymentDates;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 514140625:  // fundingLeg
          return ((TotalReturnSwapSecurity) bean).getFundingLeg();
        case -704776149:  // assetId
          return ((TotalReturnSwapSecurity) bean).getAssetId();
        case -930389515:  // effectiveDate
          return ((TotalReturnSwapSecurity) bean).getEffectiveDate();
        case -414641441:  // maturityDate
          return ((TotalReturnSwapSecurity) bean).getMaturityDate();
        case 1487154374:  // paymentSettlementDays
          return ((TotalReturnSwapSecurity) bean).getPaymentSettlementDays();
        case -724865486:  // paymentDateCalendar
          return ((TotalReturnSwapSecurity) bean).getPaymentDateCalendar();
        case -1357599257:  // paymentBusinessDayConvention
          return ((TotalReturnSwapSecurity) bean).getPaymentBusinessDayConvention();
        case 397410276:  // resetDateRelativeTo
          return ((TotalReturnSwapSecurity) bean).getResetDateRelativeTo();
        case 863656438:  // paymentFrequency
          return ((TotalReturnSwapSecurity) bean).getPaymentFrequency();
        case -10223666:  // rollConvention
          return ((TotalReturnSwapSecurity) bean).getRollConvention();
        case 95356549:  // dates
          return ((TotalReturnSwapSecurity) bean).getDates();
        case -522438625:  // paymentDates
          return ((TotalReturnSwapSecurity) bean).getPaymentDates();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 514140625:  // fundingLeg
          ((TotalReturnSwapSecurity) bean).setFundingLeg((FloatingInterestRateSwapLeg) newValue);
          return;
        case -704776149:  // assetId
          ((TotalReturnSwapSecurity) bean).setAssetId((ExternalIdBundle) newValue);
          return;
        case -930389515:  // effectiveDate
          ((TotalReturnSwapSecurity) bean).setEffectiveDate((LocalDate) newValue);
          return;
        case -414641441:  // maturityDate
          ((TotalReturnSwapSecurity) bean).setMaturityDate((LocalDate) newValue);
          return;
        case 1487154374:  // paymentSettlementDays
          ((TotalReturnSwapSecurity) bean).setPaymentSettlementDays((Integer) newValue);
          return;
        case -724865486:  // paymentDateCalendar
          ((TotalReturnSwapSecurity) bean).setPaymentDateCalendar((Set<ExternalId>) newValue);
          return;
        case -1357599257:  // paymentBusinessDayConvention
          ((TotalReturnSwapSecurity) bean).setPaymentBusinessDayConvention((BusinessDayConvention) newValue);
          return;
        case 397410276:  // resetDateRelativeTo
          if (quiet) {
            return;
          }
          throw new UnsupportedOperationException("Property cannot be written: resetDateRelativeTo");
        case 863656438:  // paymentFrequency
          ((TotalReturnSwapSecurity) bean).setPaymentFrequency((Frequency) newValue);
          return;
        case -10223666:  // rollConvention
          ((TotalReturnSwapSecurity) bean).setRollConvention((RollConvention) newValue);
          return;
        case 95356549:  // dates
          ((TotalReturnSwapSecurity) bean).setDates((int[]) newValue);
          return;
        case -522438625:  // paymentDates
          ((TotalReturnSwapSecurity) bean).setPaymentDates((LocalDate[]) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((TotalReturnSwapSecurity) bean)._fundingLeg, "fundingLeg");
      JodaBeanUtils.notNull(((TotalReturnSwapSecurity) bean)._assetId, "assetId");
      JodaBeanUtils.notNull(((TotalReturnSwapSecurity) bean)._effectiveDate, "effectiveDate");
      JodaBeanUtils.notNull(((TotalReturnSwapSecurity) bean)._maturityDate, "maturityDate");
      JodaBeanUtils.notNull(((TotalReturnSwapSecurity) bean)._paymentDateCalendar, "paymentDateCalendar");
      JodaBeanUtils.notNull(((TotalReturnSwapSecurity) bean)._paymentBusinessDayConvention, "paymentBusinessDayConvention");
      JodaBeanUtils.notNull(((TotalReturnSwapSecurity) bean)._resetDateRelativeTo, "resetDateRelativeTo");
      JodaBeanUtils.notNull(((TotalReturnSwapSecurity) bean)._paymentFrequency, "paymentFrequency");
      JodaBeanUtils.notNull(((TotalReturnSwapSecurity) bean)._rollConvention, "rollConvention");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
