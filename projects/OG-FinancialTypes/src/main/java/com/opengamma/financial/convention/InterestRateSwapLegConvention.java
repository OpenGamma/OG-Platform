/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

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

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod;
import com.opengamma.analytics.financial.instrument.annuity.DateRelativeTo;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract class describing conventions for an interest swap leg.
 */
@BeanDefinition
public abstract class InterestRateSwapLegConvention extends FinancialConvention {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The payment calendar.
   */
  @PropertyDefinition
  private final Set<ExternalId> _paymentCalendars = Sets.newHashSet();
  /**
   * The calculation calendar.
   */
  @PropertyDefinition
  private final Set<ExternalId> _calculationCalendars = Sets.newHashSet();
  /**
   * The maturity calendar.
   */
  @PropertyDefinition
  private Set<ExternalId> _maturityCalendars = Sets.newHashSet();
  /**
   * The payment business day calendar.
   */
  @PropertyDefinition(validate = "notNull")
  private BusinessDayConvention _paymentDayConvention;
  /**
   * The calculation business day calendar.
   */
  @PropertyDefinition(validate = "notNull")
  private BusinessDayConvention _calculationBusinessDayConvention;
  /**
   * The maturity business day calendar.
   */
  @PropertyDefinition(validate = "notNull")
  private BusinessDayConvention _maturityBusinessDayConvention;
  /**
   * The day count.
   */
  @PropertyDefinition(validate = "notNull")
  private DayCount _dayCountConvention;
  /**
   * The coupon payment frequency.
   */
  @PropertyDefinition(validate = "notNull")
  private Frequency _paymentFrequency;
  /**
   * The calculation frequency.
   */
  @PropertyDefinition(validate = "notNull")
  private Frequency _calculationFrequency;
  /**
   * The payment is relative to the beginning or end of the period.
   */
  @PropertyDefinition(validate = "notNull")
  private DateRelativeTo _paymentRelativeTo = DateRelativeTo.START;
  /**
   * Should the accrual be adjusted.
   */
  @PropertyDefinition
  private boolean _adjustedAccrual;
  /**
   * The number of settlement days.
   */
  @PropertyDefinition
  private int _settlementDays;
  /**
   * The roll convention (e.g. EOM)
   */
  @PropertyDefinition(validate = "notNull")
  private RollConvention _rollConvention = RollConvention.NONE;
  /**
   * The compounding.
   */
  @PropertyDefinition(validate = "notNull")
  private CompoundingMethod _compoundingMethod = CompoundingMethod.NONE;

  /**
   * Creates an instance.
   */
  protected InterestRateSwapLegConvention() {
    super();
  }

  /**
   * Creates an instance.
   * <p>
   * This instance will be incomplete with fields that are null that should not be.
   * 
   * @param name  the convention name, not null
   * @param externalIdBundle  the external identifiers for this convention, not null
   */
  public InterestRateSwapLegConvention(final String name, final ExternalIdBundle externalIdBundle) {
    super(name, externalIdBundle);
  }

  /**
   * Creates an instance.
   * 
   * @param name  the convention name, not null
   * @param externalIdBundle  the external identifiers for this convention, not null
   * @param paymentCalendars  the payment calendars, not null
   * @param calculationCalendars  the calculation calendars, not null
   * @param maturityCalendars  the maturity calendars, not null
   * @param paymentDayConvention  the payment day convention, not null
   * @param calculationBusinessDayConvention  the calculation day convention, not null
   * @param maturityBusinessDayConvention  the maturity day convention, not null
   * @param dayCountConvention  the day count frequency, not null
   * @param paymentFrequency  the payment frequency, not null
   * @param calculationFrequency  the calculation frequency, not null
   * @param paymentRelativeTo  the payment is relative to the beginning or end of the period, not null
   * @param adjustedAccrual  whether the accrual should be adjusted
   * @param settlementDays  the number of settlement days
   * @param rollConvention  the roll convention, not null
   * @param compoundingMethod  the compounding, not null
   */
  public InterestRateSwapLegConvention(final String name, final ExternalIdBundle externalIdBundle,
      Set<ExternalId> paymentCalendars,
      Set<ExternalId> calculationCalendars,
      Set<ExternalId> maturityCalendars,
      BusinessDayConvention paymentDayConvention,
      BusinessDayConvention calculationBusinessDayConvention,
      BusinessDayConvention maturityBusinessDayConvention,
      DayCount dayCountConvention,
      Frequency paymentFrequency,
      Frequency calculationFrequency,
      DateRelativeTo paymentRelativeTo,
      boolean adjustedAccrual,
      int settlementDays,
      RollConvention rollConvention,
      CompoundingMethod compoundingMethod) {
    super(name, externalIdBundle);
    setPaymentCalendars(paymentCalendars);
    setCalculationCalendars(calculationCalendars);
    setMaturityCalendars(maturityCalendars);
    setPaymentDayConvention(paymentDayConvention);
    setCalculationBusinessDayConvention(calculationBusinessDayConvention);
    setMaturityBusinessDayConvention(maturityBusinessDayConvention);
    setDayCountConvention(dayCountConvention);
    setPaymentFrequency(paymentFrequency);
    setCalculationFrequency(calculationFrequency);
    setPaymentRelativeTo(paymentRelativeTo);
    setAdjustedAccrual(adjustedAccrual);
    setSettlementDays(settlementDays);
    setRollConvention(rollConvention);
    setCompoundingMethod(compoundingMethod);
  }

  //-------------------------------------------------------------------------
  /**
   * Validates the data.
   */
  protected void validate() {
    ArgumentChecker.notNull(getDayCountConvention(), "daycount");
    ArgumentChecker.notNull(getPaymentDayConvention(), "payment daycount");
    ArgumentChecker.notNull(getPaymentFrequency(), "payment frequency");
    ArgumentChecker.notNull(getCalculationFrequency(), "calculation frequency");
    ArgumentChecker.notNull(getMaturityBusinessDayConvention(), "maturity business day convention");
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code InterestRateSwapLegConvention}.
   * @return the meta-bean, not null
   */
  public static InterestRateSwapLegConvention.Meta meta() {
    return InterestRateSwapLegConvention.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(InterestRateSwapLegConvention.Meta.INSTANCE);
  }

  @Override
  public InterestRateSwapLegConvention.Meta metaBean() {
    return InterestRateSwapLegConvention.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the payment calendar.
   * @return the value of the property, not null
   */
  public Set<ExternalId> getPaymentCalendars() {
    return _paymentCalendars;
  }

  /**
   * Sets the payment calendar.
   * @param paymentCalendars  the new value of the property, not null
   */
  public void setPaymentCalendars(Set<ExternalId> paymentCalendars) {
    JodaBeanUtils.notNull(paymentCalendars, "paymentCalendars");
    this._paymentCalendars.clear();
    this._paymentCalendars.addAll(paymentCalendars);
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
   * Gets the calculation calendar.
   * @return the value of the property, not null
   */
  public Set<ExternalId> getCalculationCalendars() {
    return _calculationCalendars;
  }

  /**
   * Sets the calculation calendar.
   * @param calculationCalendars  the new value of the property, not null
   */
  public void setCalculationCalendars(Set<ExternalId> calculationCalendars) {
    JodaBeanUtils.notNull(calculationCalendars, "calculationCalendars");
    this._calculationCalendars.clear();
    this._calculationCalendars.addAll(calculationCalendars);
  }

  /**
   * Gets the the {@code calculationCalendars} property.
   * @return the property, not null
   */
  public final Property<Set<ExternalId>> calculationCalendars() {
    return metaBean().calculationCalendars().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the maturity calendar.
   * @return the value of the property
   */
  public Set<ExternalId> getMaturityCalendars() {
    return _maturityCalendars;
  }

  /**
   * Sets the maturity calendar.
   * @param maturityCalendars  the new value of the property
   */
  public void setMaturityCalendars(Set<ExternalId> maturityCalendars) {
    this._maturityCalendars = maturityCalendars;
  }

  /**
   * Gets the the {@code maturityCalendars} property.
   * @return the property, not null
   */
  public final Property<Set<ExternalId>> maturityCalendars() {
    return metaBean().maturityCalendars().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the payment business day calendar.
   * @return the value of the property, not null
   */
  public BusinessDayConvention getPaymentDayConvention() {
    return _paymentDayConvention;
  }

  /**
   * Sets the payment business day calendar.
   * @param paymentDayConvention  the new value of the property, not null
   */
  public void setPaymentDayConvention(BusinessDayConvention paymentDayConvention) {
    JodaBeanUtils.notNull(paymentDayConvention, "paymentDayConvention");
    this._paymentDayConvention = paymentDayConvention;
  }

  /**
   * Gets the the {@code paymentDayConvention} property.
   * @return the property, not null
   */
  public final Property<BusinessDayConvention> paymentDayConvention() {
    return metaBean().paymentDayConvention().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the calculation business day calendar.
   * @return the value of the property, not null
   */
  public BusinessDayConvention getCalculationBusinessDayConvention() {
    return _calculationBusinessDayConvention;
  }

  /**
   * Sets the calculation business day calendar.
   * @param calculationBusinessDayConvention  the new value of the property, not null
   */
  public void setCalculationBusinessDayConvention(BusinessDayConvention calculationBusinessDayConvention) {
    JodaBeanUtils.notNull(calculationBusinessDayConvention, "calculationBusinessDayConvention");
    this._calculationBusinessDayConvention = calculationBusinessDayConvention;
  }

  /**
   * Gets the the {@code calculationBusinessDayConvention} property.
   * @return the property, not null
   */
  public final Property<BusinessDayConvention> calculationBusinessDayConvention() {
    return metaBean().calculationBusinessDayConvention().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the maturity business day calendar.
   * @return the value of the property, not null
   */
  public BusinessDayConvention getMaturityBusinessDayConvention() {
    return _maturityBusinessDayConvention;
  }

  /**
   * Sets the maturity business day calendar.
   * @param maturityBusinessDayConvention  the new value of the property, not null
   */
  public void setMaturityBusinessDayConvention(BusinessDayConvention maturityBusinessDayConvention) {
    JodaBeanUtils.notNull(maturityBusinessDayConvention, "maturityBusinessDayConvention");
    this._maturityBusinessDayConvention = maturityBusinessDayConvention;
  }

  /**
   * Gets the the {@code maturityBusinessDayConvention} property.
   * @return the property, not null
   */
  public final Property<BusinessDayConvention> maturityBusinessDayConvention() {
    return metaBean().maturityBusinessDayConvention().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the day count.
   * @return the value of the property, not null
   */
  public DayCount getDayCountConvention() {
    return _dayCountConvention;
  }

  /**
   * Sets the day count.
   * @param dayCountConvention  the new value of the property, not null
   */
  public void setDayCountConvention(DayCount dayCountConvention) {
    JodaBeanUtils.notNull(dayCountConvention, "dayCountConvention");
    this._dayCountConvention = dayCountConvention;
  }

  /**
   * Gets the the {@code dayCountConvention} property.
   * @return the property, not null
   */
  public final Property<DayCount> dayCountConvention() {
    return metaBean().dayCountConvention().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the coupon payment frequency.
   * @return the value of the property, not null
   */
  public Frequency getPaymentFrequency() {
    return _paymentFrequency;
  }

  /**
   * Sets the coupon payment frequency.
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
   * Gets the calculation frequency.
   * @return the value of the property, not null
   */
  public Frequency getCalculationFrequency() {
    return _calculationFrequency;
  }

  /**
   * Sets the calculation frequency.
   * @param calculationFrequency  the new value of the property, not null
   */
  public void setCalculationFrequency(Frequency calculationFrequency) {
    JodaBeanUtils.notNull(calculationFrequency, "calculationFrequency");
    this._calculationFrequency = calculationFrequency;
  }

  /**
   * Gets the the {@code calculationFrequency} property.
   * @return the property, not null
   */
  public final Property<Frequency> calculationFrequency() {
    return metaBean().calculationFrequency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the payment is relative to the beginning or end of the period.
   * @return the value of the property, not null
   */
  public DateRelativeTo getPaymentRelativeTo() {
    return _paymentRelativeTo;
  }

  /**
   * Sets the payment is relative to the beginning or end of the period.
   * @param paymentRelativeTo  the new value of the property, not null
   */
  public void setPaymentRelativeTo(DateRelativeTo paymentRelativeTo) {
    JodaBeanUtils.notNull(paymentRelativeTo, "paymentRelativeTo");
    this._paymentRelativeTo = paymentRelativeTo;
  }

  /**
   * Gets the the {@code paymentRelativeTo} property.
   * @return the property, not null
   */
  public final Property<DateRelativeTo> paymentRelativeTo() {
    return metaBean().paymentRelativeTo().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets should the accrual be adjusted.
   * @return the value of the property
   */
  public boolean isAdjustedAccrual() {
    return _adjustedAccrual;
  }

  /**
   * Sets should the accrual be adjusted.
   * @param adjustedAccrual  the new value of the property
   */
  public void setAdjustedAccrual(boolean adjustedAccrual) {
    this._adjustedAccrual = adjustedAccrual;
  }

  /**
   * Gets the the {@code adjustedAccrual} property.
   * @return the property, not null
   */
  public final Property<Boolean> adjustedAccrual() {
    return metaBean().adjustedAccrual().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the number of settlement days.
   * @return the value of the property
   */
  public int getSettlementDays() {
    return _settlementDays;
  }

  /**
   * Sets the number of settlement days.
   * @param settlementDays  the new value of the property
   */
  public void setSettlementDays(int settlementDays) {
    this._settlementDays = settlementDays;
  }

  /**
   * Gets the the {@code settlementDays} property.
   * @return the property, not null
   */
  public final Property<Integer> settlementDays() {
    return metaBean().settlementDays().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the roll convention (e.g. EOM)
   * @return the value of the property, not null
   */
  public RollConvention getRollConvention() {
    return _rollConvention;
  }

  /**
   * Sets the roll convention (e.g. EOM)
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
   * Gets the compounding.
   * @return the value of the property, not null
   */
  public CompoundingMethod getCompoundingMethod() {
    return _compoundingMethod;
  }

  /**
   * Sets the compounding.
   * @param compoundingMethod  the new value of the property, not null
   */
  public void setCompoundingMethod(CompoundingMethod compoundingMethod) {
    JodaBeanUtils.notNull(compoundingMethod, "compoundingMethod");
    this._compoundingMethod = compoundingMethod;
  }

  /**
   * Gets the the {@code compoundingMethod} property.
   * @return the property, not null
   */
  public final Property<CompoundingMethod> compoundingMethod() {
    return metaBean().compoundingMethod().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      InterestRateSwapLegConvention other = (InterestRateSwapLegConvention) obj;
      return JodaBeanUtils.equal(getPaymentCalendars(), other.getPaymentCalendars()) &&
          JodaBeanUtils.equal(getCalculationCalendars(), other.getCalculationCalendars()) &&
          JodaBeanUtils.equal(getMaturityCalendars(), other.getMaturityCalendars()) &&
          JodaBeanUtils.equal(getPaymentDayConvention(), other.getPaymentDayConvention()) &&
          JodaBeanUtils.equal(getCalculationBusinessDayConvention(), other.getCalculationBusinessDayConvention()) &&
          JodaBeanUtils.equal(getMaturityBusinessDayConvention(), other.getMaturityBusinessDayConvention()) &&
          JodaBeanUtils.equal(getDayCountConvention(), other.getDayCountConvention()) &&
          JodaBeanUtils.equal(getPaymentFrequency(), other.getPaymentFrequency()) &&
          JodaBeanUtils.equal(getCalculationFrequency(), other.getCalculationFrequency()) &&
          JodaBeanUtils.equal(getPaymentRelativeTo(), other.getPaymentRelativeTo()) &&
          (isAdjustedAccrual() == other.isAdjustedAccrual()) &&
          (getSettlementDays() == other.getSettlementDays()) &&
          JodaBeanUtils.equal(getRollConvention(), other.getRollConvention()) &&
          JodaBeanUtils.equal(getCompoundingMethod(), other.getCompoundingMethod()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getPaymentCalendars());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCalculationCalendars());
    hash = hash * 31 + JodaBeanUtils.hashCode(getMaturityCalendars());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPaymentDayConvention());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCalculationBusinessDayConvention());
    hash = hash * 31 + JodaBeanUtils.hashCode(getMaturityBusinessDayConvention());
    hash = hash * 31 + JodaBeanUtils.hashCode(getDayCountConvention());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPaymentFrequency());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCalculationFrequency());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPaymentRelativeTo());
    hash = hash * 31 + JodaBeanUtils.hashCode(isAdjustedAccrual());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSettlementDays());
    hash = hash * 31 + JodaBeanUtils.hashCode(getRollConvention());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCompoundingMethod());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(480);
    buf.append("InterestRateSwapLegConvention{");
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
    buf.append("paymentCalendars").append('=').append(JodaBeanUtils.toString(getPaymentCalendars())).append(',').append(' ');
    buf.append("calculationCalendars").append('=').append(JodaBeanUtils.toString(getCalculationCalendars())).append(',').append(' ');
    buf.append("maturityCalendars").append('=').append(JodaBeanUtils.toString(getMaturityCalendars())).append(',').append(' ');
    buf.append("paymentDayConvention").append('=').append(JodaBeanUtils.toString(getPaymentDayConvention())).append(',').append(' ');
    buf.append("calculationBusinessDayConvention").append('=').append(JodaBeanUtils.toString(getCalculationBusinessDayConvention())).append(',').append(' ');
    buf.append("maturityBusinessDayConvention").append('=').append(JodaBeanUtils.toString(getMaturityBusinessDayConvention())).append(',').append(' ');
    buf.append("dayCountConvention").append('=').append(JodaBeanUtils.toString(getDayCountConvention())).append(',').append(' ');
    buf.append("paymentFrequency").append('=').append(JodaBeanUtils.toString(getPaymentFrequency())).append(',').append(' ');
    buf.append("calculationFrequency").append('=').append(JodaBeanUtils.toString(getCalculationFrequency())).append(',').append(' ');
    buf.append("paymentRelativeTo").append('=').append(JodaBeanUtils.toString(getPaymentRelativeTo())).append(',').append(' ');
    buf.append("adjustedAccrual").append('=').append(JodaBeanUtils.toString(isAdjustedAccrual())).append(',').append(' ');
    buf.append("settlementDays").append('=').append(JodaBeanUtils.toString(getSettlementDays())).append(',').append(' ');
    buf.append("rollConvention").append('=').append(JodaBeanUtils.toString(getRollConvention())).append(',').append(' ');
    buf.append("compoundingMethod").append('=').append(JodaBeanUtils.toString(getCompoundingMethod())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code InterestRateSwapLegConvention}.
   */
  public static class Meta extends FinancialConvention.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code paymentCalendars} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<ExternalId>> _paymentCalendars = DirectMetaProperty.ofReadWrite(
        this, "paymentCalendars", InterestRateSwapLegConvention.class, (Class) Set.class);
    /**
     * The meta-property for the {@code calculationCalendars} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<ExternalId>> _calculationCalendars = DirectMetaProperty.ofReadWrite(
        this, "calculationCalendars", InterestRateSwapLegConvention.class, (Class) Set.class);
    /**
     * The meta-property for the {@code maturityCalendars} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<ExternalId>> _maturityCalendars = DirectMetaProperty.ofReadWrite(
        this, "maturityCalendars", InterestRateSwapLegConvention.class, (Class) Set.class);
    /**
     * The meta-property for the {@code paymentDayConvention} property.
     */
    private final MetaProperty<BusinessDayConvention> _paymentDayConvention = DirectMetaProperty.ofReadWrite(
        this, "paymentDayConvention", InterestRateSwapLegConvention.class, BusinessDayConvention.class);
    /**
     * The meta-property for the {@code calculationBusinessDayConvention} property.
     */
    private final MetaProperty<BusinessDayConvention> _calculationBusinessDayConvention = DirectMetaProperty.ofReadWrite(
        this, "calculationBusinessDayConvention", InterestRateSwapLegConvention.class, BusinessDayConvention.class);
    /**
     * The meta-property for the {@code maturityBusinessDayConvention} property.
     */
    private final MetaProperty<BusinessDayConvention> _maturityBusinessDayConvention = DirectMetaProperty.ofReadWrite(
        this, "maturityBusinessDayConvention", InterestRateSwapLegConvention.class, BusinessDayConvention.class);
    /**
     * The meta-property for the {@code dayCountConvention} property.
     */
    private final MetaProperty<DayCount> _dayCountConvention = DirectMetaProperty.ofReadWrite(
        this, "dayCountConvention", InterestRateSwapLegConvention.class, DayCount.class);
    /**
     * The meta-property for the {@code paymentFrequency} property.
     */
    private final MetaProperty<Frequency> _paymentFrequency = DirectMetaProperty.ofReadWrite(
        this, "paymentFrequency", InterestRateSwapLegConvention.class, Frequency.class);
    /**
     * The meta-property for the {@code calculationFrequency} property.
     */
    private final MetaProperty<Frequency> _calculationFrequency = DirectMetaProperty.ofReadWrite(
        this, "calculationFrequency", InterestRateSwapLegConvention.class, Frequency.class);
    /**
     * The meta-property for the {@code paymentRelativeTo} property.
     */
    private final MetaProperty<DateRelativeTo> _paymentRelativeTo = DirectMetaProperty.ofReadWrite(
        this, "paymentRelativeTo", InterestRateSwapLegConvention.class, DateRelativeTo.class);
    /**
     * The meta-property for the {@code adjustedAccrual} property.
     */
    private final MetaProperty<Boolean> _adjustedAccrual = DirectMetaProperty.ofReadWrite(
        this, "adjustedAccrual", InterestRateSwapLegConvention.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code settlementDays} property.
     */
    private final MetaProperty<Integer> _settlementDays = DirectMetaProperty.ofReadWrite(
        this, "settlementDays", InterestRateSwapLegConvention.class, Integer.TYPE);
    /**
     * The meta-property for the {@code rollConvention} property.
     */
    private final MetaProperty<RollConvention> _rollConvention = DirectMetaProperty.ofReadWrite(
        this, "rollConvention", InterestRateSwapLegConvention.class, RollConvention.class);
    /**
     * The meta-property for the {@code compoundingMethod} property.
     */
    private final MetaProperty<CompoundingMethod> _compoundingMethod = DirectMetaProperty.ofReadWrite(
        this, "compoundingMethod", InterestRateSwapLegConvention.class, CompoundingMethod.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "paymentCalendars",
        "calculationCalendars",
        "maturityCalendars",
        "paymentDayConvention",
        "calculationBusinessDayConvention",
        "maturityBusinessDayConvention",
        "dayCountConvention",
        "paymentFrequency",
        "calculationFrequency",
        "paymentRelativeTo",
        "adjustedAccrual",
        "settlementDays",
        "rollConvention",
        "compoundingMethod");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -299417201:  // paymentCalendars
          return _paymentCalendars;
        case 629948460:  // calculationCalendars
          return _calculationCalendars;
        case 1021419620:  // maturityCalendars
          return _maturityCalendars;
        case 244375495:  // paymentDayConvention
          return _paymentDayConvention;
        case 927443204:  // calculationBusinessDayConvention
          return _calculationBusinessDayConvention;
        case 1177974076:  // maturityBusinessDayConvention
          return _maturityBusinessDayConvention;
        case 589154980:  // dayCountConvention
          return _dayCountConvention;
        case 863656438:  // paymentFrequency
          return _paymentFrequency;
        case 1793022099:  // calculationFrequency
          return _calculationFrequency;
        case -1357627123:  // paymentRelativeTo
          return _paymentRelativeTo;
        case 1362995553:  // adjustedAccrual
          return _adjustedAccrual;
        case -295948000:  // settlementDays
          return _settlementDays;
        case -10223666:  // rollConvention
          return _rollConvention;
        case -1376171496:  // compoundingMethod
          return _compoundingMethod;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends InterestRateSwapLegConvention> builder() {
      throw new UnsupportedOperationException("InterestRateSwapLegConvention is an abstract class");
    }

    @Override
    public Class<? extends InterestRateSwapLegConvention> beanType() {
      return InterestRateSwapLegConvention.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code paymentCalendars} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<ExternalId>> paymentCalendars() {
      return _paymentCalendars;
    }

    /**
     * The meta-property for the {@code calculationCalendars} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<ExternalId>> calculationCalendars() {
      return _calculationCalendars;
    }

    /**
     * The meta-property for the {@code maturityCalendars} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<ExternalId>> maturityCalendars() {
      return _maturityCalendars;
    }

    /**
     * The meta-property for the {@code paymentDayConvention} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BusinessDayConvention> paymentDayConvention() {
      return _paymentDayConvention;
    }

    /**
     * The meta-property for the {@code calculationBusinessDayConvention} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BusinessDayConvention> calculationBusinessDayConvention() {
      return _calculationBusinessDayConvention;
    }

    /**
     * The meta-property for the {@code maturityBusinessDayConvention} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BusinessDayConvention> maturityBusinessDayConvention() {
      return _maturityBusinessDayConvention;
    }

    /**
     * The meta-property for the {@code dayCountConvention} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DayCount> dayCountConvention() {
      return _dayCountConvention;
    }

    /**
     * The meta-property for the {@code paymentFrequency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Frequency> paymentFrequency() {
      return _paymentFrequency;
    }

    /**
     * The meta-property for the {@code calculationFrequency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Frequency> calculationFrequency() {
      return _calculationFrequency;
    }

    /**
     * The meta-property for the {@code paymentRelativeTo} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DateRelativeTo> paymentRelativeTo() {
      return _paymentRelativeTo;
    }

    /**
     * The meta-property for the {@code adjustedAccrual} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> adjustedAccrual() {
      return _adjustedAccrual;
    }

    /**
     * The meta-property for the {@code settlementDays} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> settlementDays() {
      return _settlementDays;
    }

    /**
     * The meta-property for the {@code rollConvention} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<RollConvention> rollConvention() {
      return _rollConvention;
    }

    /**
     * The meta-property for the {@code compoundingMethod} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CompoundingMethod> compoundingMethod() {
      return _compoundingMethod;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -299417201:  // paymentCalendars
          return ((InterestRateSwapLegConvention) bean).getPaymentCalendars();
        case 629948460:  // calculationCalendars
          return ((InterestRateSwapLegConvention) bean).getCalculationCalendars();
        case 1021419620:  // maturityCalendars
          return ((InterestRateSwapLegConvention) bean).getMaturityCalendars();
        case 244375495:  // paymentDayConvention
          return ((InterestRateSwapLegConvention) bean).getPaymentDayConvention();
        case 927443204:  // calculationBusinessDayConvention
          return ((InterestRateSwapLegConvention) bean).getCalculationBusinessDayConvention();
        case 1177974076:  // maturityBusinessDayConvention
          return ((InterestRateSwapLegConvention) bean).getMaturityBusinessDayConvention();
        case 589154980:  // dayCountConvention
          return ((InterestRateSwapLegConvention) bean).getDayCountConvention();
        case 863656438:  // paymentFrequency
          return ((InterestRateSwapLegConvention) bean).getPaymentFrequency();
        case 1793022099:  // calculationFrequency
          return ((InterestRateSwapLegConvention) bean).getCalculationFrequency();
        case -1357627123:  // paymentRelativeTo
          return ((InterestRateSwapLegConvention) bean).getPaymentRelativeTo();
        case 1362995553:  // adjustedAccrual
          return ((InterestRateSwapLegConvention) bean).isAdjustedAccrual();
        case -295948000:  // settlementDays
          return ((InterestRateSwapLegConvention) bean).getSettlementDays();
        case -10223666:  // rollConvention
          return ((InterestRateSwapLegConvention) bean).getRollConvention();
        case -1376171496:  // compoundingMethod
          return ((InterestRateSwapLegConvention) bean).getCompoundingMethod();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -299417201:  // paymentCalendars
          ((InterestRateSwapLegConvention) bean).setPaymentCalendars((Set<ExternalId>) newValue);
          return;
        case 629948460:  // calculationCalendars
          ((InterestRateSwapLegConvention) bean).setCalculationCalendars((Set<ExternalId>) newValue);
          return;
        case 1021419620:  // maturityCalendars
          ((InterestRateSwapLegConvention) bean).setMaturityCalendars((Set<ExternalId>) newValue);
          return;
        case 244375495:  // paymentDayConvention
          ((InterestRateSwapLegConvention) bean).setPaymentDayConvention((BusinessDayConvention) newValue);
          return;
        case 927443204:  // calculationBusinessDayConvention
          ((InterestRateSwapLegConvention) bean).setCalculationBusinessDayConvention((BusinessDayConvention) newValue);
          return;
        case 1177974076:  // maturityBusinessDayConvention
          ((InterestRateSwapLegConvention) bean).setMaturityBusinessDayConvention((BusinessDayConvention) newValue);
          return;
        case 589154980:  // dayCountConvention
          ((InterestRateSwapLegConvention) bean).setDayCountConvention((DayCount) newValue);
          return;
        case 863656438:  // paymentFrequency
          ((InterestRateSwapLegConvention) bean).setPaymentFrequency((Frequency) newValue);
          return;
        case 1793022099:  // calculationFrequency
          ((InterestRateSwapLegConvention) bean).setCalculationFrequency((Frequency) newValue);
          return;
        case -1357627123:  // paymentRelativeTo
          ((InterestRateSwapLegConvention) bean).setPaymentRelativeTo((DateRelativeTo) newValue);
          return;
        case 1362995553:  // adjustedAccrual
          ((InterestRateSwapLegConvention) bean).setAdjustedAccrual((Boolean) newValue);
          return;
        case -295948000:  // settlementDays
          ((InterestRateSwapLegConvention) bean).setSettlementDays((Integer) newValue);
          return;
        case -10223666:  // rollConvention
          ((InterestRateSwapLegConvention) bean).setRollConvention((RollConvention) newValue);
          return;
        case -1376171496:  // compoundingMethod
          ((InterestRateSwapLegConvention) bean).setCompoundingMethod((CompoundingMethod) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((InterestRateSwapLegConvention) bean)._paymentCalendars, "paymentCalendars");
      JodaBeanUtils.notNull(((InterestRateSwapLegConvention) bean)._calculationCalendars, "calculationCalendars");
      JodaBeanUtils.notNull(((InterestRateSwapLegConvention) bean)._paymentDayConvention, "paymentDayConvention");
      JodaBeanUtils.notNull(((InterestRateSwapLegConvention) bean)._calculationBusinessDayConvention, "calculationBusinessDayConvention");
      JodaBeanUtils.notNull(((InterestRateSwapLegConvention) bean)._maturityBusinessDayConvention, "maturityBusinessDayConvention");
      JodaBeanUtils.notNull(((InterestRateSwapLegConvention) bean)._dayCountConvention, "dayCountConvention");
      JodaBeanUtils.notNull(((InterestRateSwapLegConvention) bean)._paymentFrequency, "paymentFrequency");
      JodaBeanUtils.notNull(((InterestRateSwapLegConvention) bean)._calculationFrequency, "calculationFrequency");
      JodaBeanUtils.notNull(((InterestRateSwapLegConvention) bean)._paymentRelativeTo, "paymentRelativeTo");
      JodaBeanUtils.notNull(((InterestRateSwapLegConvention) bean)._rollConvention, "rollConvention");
      JodaBeanUtils.notNull(((InterestRateSwapLegConvention) bean)._compoundingMethod, "compoundingMethod");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
