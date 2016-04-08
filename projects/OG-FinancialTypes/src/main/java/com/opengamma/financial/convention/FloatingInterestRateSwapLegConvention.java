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
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod;
import com.opengamma.analytics.financial.instrument.annuity.DateRelativeTo;
import com.opengamma.analytics.financial.instrument.annuity.OffsetType;
import com.opengamma.core.convention.ConventionType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapNotional;
import com.opengamma.financial.security.irs.PayReceiveType;
import com.opengamma.financial.security.irs.Rate;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * The conventions for a floating interest rate leg.
 */
@BeanDefinition
public class FloatingInterestRateSwapLegConvention extends InterestRateSwapLegConvention {

  /**
   * Type of the convention.
   */
  public static final ConventionType TYPE = ConventionType.of("FloatingInterestRateSwapLeg");

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * The rate type.
   */
  @PropertyDefinition(validate = "notNull")
  private FloatingRateType _rateType;
  /**
   * The fixing calendar.
   */
  @PropertyDefinition
  private final Set<ExternalId> _fixingCalendars = Sets.newHashSet();
  /**
   * The business day convention used for fixing.
   */
  @PropertyDefinition(validate = "notNull")
  private BusinessDayConvention _fixingBusinessDayConvention;
  /**
   * The day type (calendar or business) for the fixing lag.
   */
  @PropertyDefinition(validate = "notNull")
  private OffsetType _settlementDayType = OffsetType.BUSINESS;
  /**
   * The reset frequency.
   */
  @PropertyDefinition(validate = "notNull")
  private Frequency _resetFrequency;
  /**
   * The reset calendar.
   */
  @PropertyDefinition
  private final Set<ExternalId> _resetCalendars = Sets.newHashSet();
  /**
   * The reset business day convention
   */
  @PropertyDefinition(validate = "notNull")
  private BusinessDayConvention _resetBusinessDayConvention;
  /**
   * The reset relative to either the start or end of the period.
   */
  @PropertyDefinition(validate = "notNull")
  private DateRelativeTo _resetRelativeTo = DateRelativeTo.START;
  /**
   * The payment lag in days.
   */
  @PropertyDefinition
  private int _paymentLag;

  /**
   * Creates an instance.
   */
  protected FloatingInterestRateSwapLegConvention() {
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
  public FloatingInterestRateSwapLegConvention(final String name, final ExternalIdBundle externalIdBundle) {
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
   * @param rateType  the rate type, not null
   * @param fixingCalendars  the fixing calendars, not null
   * @param fixingBusinessDayConvention  the fixing day convention, not null
   * @param settlementDayType  the settlement date type, not null
   * @param resetFrequency  the reset frequency, not null
   * @param resetCalendars  the reset calendars, not null
   * @param resetBusinessDayConvention  the reset day convention, not null
   * @param resetRelativeTo  the reset relative to, not null
   * @param paymentLag the payment lag in days
   */
  public FloatingInterestRateSwapLegConvention(final String name, final ExternalIdBundle externalIdBundle,  // CSIGNORE
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
      CompoundingMethod compoundingMethod,
      FloatingRateType rateType,
      Set<ExternalId> fixingCalendars,
      BusinessDayConvention fixingBusinessDayConvention,
      OffsetType settlementDayType,
      Frequency resetFrequency,
      Set<ExternalId> resetCalendars,
      BusinessDayConvention resetBusinessDayConvention,
      DateRelativeTo resetRelativeTo,
      int paymentLag) {
    super(name, externalIdBundle, paymentCalendars, calculationCalendars, maturityCalendars,
        paymentDayConvention, calculationBusinessDayConvention, maturityBusinessDayConvention,
        dayCountConvention, paymentFrequency, calculationFrequency, paymentRelativeTo,
        adjustedAccrual, settlementDays, rollConvention, compoundingMethod);
    setRateType(rateType);
    setFixingCalendars(fixingCalendars);
    setFixingBusinessDayConvention(fixingBusinessDayConvention);
    setSettlementDayType(settlementDayType);
    setResetFrequency(resetFrequency);
    setResetCalendars(resetCalendars);
    setResetBusinessDayConvention(resetBusinessDayConvention);
    setResetRelativeTo(resetRelativeTo);
    setPaymentLag(paymentLag);
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
   * @param rateType  the rate type, not null
   * @param fixingCalendars  the fixing calendars, not null
   * @param fixingBusinessDayConvention  the fixing day convention, not null
   * @param settlementDayType  the settlement date type, not null
   * @param resetFrequency  the reset frequency, not null
   * @param resetCalendars  the reset calendars, not null
   * @param resetBusinessDayConvention  the reset day convention, not null
   * @param resetRelativeTo  the reset relative to, not null
   */
  public FloatingInterestRateSwapLegConvention(final String name, final ExternalIdBundle externalIdBundle,  // CSIGNORE
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
      CompoundingMethod compoundingMethod,
      FloatingRateType rateType,
      Set<ExternalId> fixingCalendars,
      BusinessDayConvention fixingBusinessDayConvention,
      OffsetType settlementDayType,
      Frequency resetFrequency,
      Set<ExternalId> resetCalendars,
      BusinessDayConvention resetBusinessDayConvention,
      DateRelativeTo resetRelativeTo) {
    super(name, externalIdBundle, paymentCalendars, calculationCalendars, maturityCalendars,
        paymentDayConvention, calculationBusinessDayConvention, maturityBusinessDayConvention,
        dayCountConvention, paymentFrequency, calculationFrequency, paymentRelativeTo,
        adjustedAccrual, settlementDays, rollConvention, compoundingMethod);
    setRateType(rateType);
    setFixingCalendars(fixingCalendars);
    setFixingBusinessDayConvention(fixingBusinessDayConvention);
    setSettlementDayType(settlementDayType);
    setResetFrequency(resetFrequency);
    setResetCalendars(resetCalendars);
    setResetBusinessDayConvention(resetBusinessDayConvention);
    setResetRelativeTo(resetRelativeTo);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the type identifying this convention.
   * 
   * @return the {@link #TYPE} constant, not null
   */
  @Override
  public ConventionType getConventionType() {
    return TYPE;
  }

  /**
   * Accepts a visitor to manage traversal of the hierarchy.
   *
   * @param <T>  the result type of the visitor
   * @param visitor  the visitor, not null
   * @return the result
   */
  @Override
  public <T> T accept(final FinancialConventionVisitor<T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitFloatingInterestRateSwapLegConvention(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Create a leg from a convention.
   *
   * @param notional  the notional (may be simple or complex)
   * @param payOrReceive  is this a pay or receive leg?
   * @return the leg, not null
   */
  public FloatingInterestRateSwapLeg toLeg(final InterestRateSwapNotional notional, final PayReceiveType payOrReceive, final LocalDate effectiveDate, final LocalDate terminationDate) {
    ArgumentChecker.notNull(getDayCountConvention(), "Daycount");
    FloatingInterestRateSwapLeg leg = new FloatingInterestRateSwapLeg();
    leg.setPayReceiveType(payOrReceive);
    leg.setNotional(notional);
    leg.setDayCountConvention(getDayCountConvention());
    leg.setRollConvention(getRollConvention());
    leg.setEffectiveDate(effectiveDate);
    leg.setUnadjustedMaturityDate(terminationDate);
    // float leg specific parameters
    leg.setFloatingReferenceRateId(Iterators.getOnlyElement(getExternalIdBundle().iterator()));
    leg.setFloatingRateType(_rateType);
    // maturity date parameters
    leg.setMaturityDateBusinessDayConvention(getMaturityBusinessDayConvention());
    leg.setMaturityDateCalendars(getMaturityCalendars());
    // payment date parameters
    leg.setPaymentDateBusinessDayConvention(getPaymentDayConvention());
    leg.setPaymentDateCalendars(getPaymentCalendars());
    leg.setPaymentDateFrequency(getPaymentFrequency());
    leg.setPaymentOffset(-_paymentLag);
    leg.setPaymentDateRelativeTo(getPaymentRelativeTo());
    // accrual period parameters
    leg.setAccrualPeriodBusinessDayConvention(getCalculationBusinessDayConvention());
    leg.setAccrualPeriodCalendars(getCalculationCalendars());
    leg.setAccrualPeriodFrequency(getCalculationFrequency());
    // reset period parameters
    leg.setResetPeriodBusinessDayConvention(_resetBusinessDayConvention);
    leg.setResetPeriodCalendars(_resetCalendars);
    leg.setResetDateRelativeTo(_resetRelativeTo);
    leg.setResetPeriodFrequency(getResetFrequency());
    leg.setCompoundingMethod(getCompoundingMethod());
    // fixing period parameters
    leg.setFixingDateBusinessDayConvention(_fixingBusinessDayConvention);
    leg.setFixingDateCalendars(_fixingCalendars);
    leg.setFixingDateOffset(getSettlementDays());
    leg.setFixingDateOffsetType(getSettlementDayType());
    return leg;
  }

  /**
   * Create a leg from a convention.
   *
   * @param notional  the notional (may be simple or complex)
   * @param payOrReceive  is this a pay or receive leg?
   * @return the leg, not null
   */
  public FloatingInterestRateSwapLeg toLeg(final InterestRateSwapNotional notional, final PayReceiveType payOrReceive) {
    return toLeg(notional, payOrReceive, null, null);
  }

  /**
   * Create a leg from a convention.
   *
   * @param notional  the notional (may be simple or complex)
   * @param payOrReceive  is this a pay or receive leg?
   * @param spread  the spread. may be null
   * @return the leg, not null
   */
  public FloatingInterestRateSwapLeg toLeg(final InterestRateSwapNotional notional, final PayReceiveType payOrReceive, Rate spread) {
    FloatingInterestRateSwapLeg leg = toLeg(notional, payOrReceive);
    leg.setSpreadSchedule(spread);
    return leg;
  }

  @Override
  protected void validate() {
    super.validate();
    ArgumentChecker.notNull(getExternalIdBundle(), "Index name");
    ArgumentChecker.notEmpty(getExternalIdBundle(), "Index name");
    ArgumentChecker.notNull(getRateType(), "rate type");
    ArgumentChecker.notNull(getResetFrequency(), "reset frequency");
    ArgumentChecker.notNull(getResetBusinessDayConvention(), "reset BDC");
    ArgumentChecker.notNull(getFixingBusinessDayConvention(), "fixing frequency");
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FloatingInterestRateSwapLegConvention}.
   * @return the meta-bean, not null
   */
  public static FloatingInterestRateSwapLegConvention.Meta meta() {
    return FloatingInterestRateSwapLegConvention.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FloatingInterestRateSwapLegConvention.Meta.INSTANCE);
  }

  @Override
  public FloatingInterestRateSwapLegConvention.Meta metaBean() {
    return FloatingInterestRateSwapLegConvention.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the rate type.
   * @return the value of the property, not null
   */
  public FloatingRateType getRateType() {
    return _rateType;
  }

  /**
   * Sets the rate type.
   * @param rateType  the new value of the property, not null
   */
  public void setRateType(FloatingRateType rateType) {
    JodaBeanUtils.notNull(rateType, "rateType");
    this._rateType = rateType;
  }

  /**
   * Gets the the {@code rateType} property.
   * @return the property, not null
   */
  public final Property<FloatingRateType> rateType() {
    return metaBean().rateType().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the fixing calendar.
   * @return the value of the property, not null
   */
  public Set<ExternalId> getFixingCalendars() {
    return _fixingCalendars;
  }

  /**
   * Sets the fixing calendar.
   * @param fixingCalendars  the new value of the property, not null
   */
  public void setFixingCalendars(Set<ExternalId> fixingCalendars) {
    JodaBeanUtils.notNull(fixingCalendars, "fixingCalendars");
    this._fixingCalendars.clear();
    this._fixingCalendars.addAll(fixingCalendars);
  }

  /**
   * Gets the the {@code fixingCalendars} property.
   * @return the property, not null
   */
  public final Property<Set<ExternalId>> fixingCalendars() {
    return metaBean().fixingCalendars().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the business day convention used for fixing.
   * @return the value of the property, not null
   */
  public BusinessDayConvention getFixingBusinessDayConvention() {
    return _fixingBusinessDayConvention;
  }

  /**
   * Sets the business day convention used for fixing.
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
   * Gets the day type (calendar or business) for the fixing lag.
   * @return the value of the property, not null
   */
  public OffsetType getSettlementDayType() {
    return _settlementDayType;
  }

  /**
   * Sets the day type (calendar or business) for the fixing lag.
   * @param settlementDayType  the new value of the property, not null
   */
  public void setSettlementDayType(OffsetType settlementDayType) {
    JodaBeanUtils.notNull(settlementDayType, "settlementDayType");
    this._settlementDayType = settlementDayType;
  }

  /**
   * Gets the the {@code settlementDayType} property.
   * @return the property, not null
   */
  public final Property<OffsetType> settlementDayType() {
    return metaBean().settlementDayType().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the reset frequency.
   * @return the value of the property, not null
   */
  public Frequency getResetFrequency() {
    return _resetFrequency;
  }

  /**
   * Sets the reset frequency.
   * @param resetFrequency  the new value of the property, not null
   */
  public void setResetFrequency(Frequency resetFrequency) {
    JodaBeanUtils.notNull(resetFrequency, "resetFrequency");
    this._resetFrequency = resetFrequency;
  }

  /**
   * Gets the the {@code resetFrequency} property.
   * @return the property, not null
   */
  public final Property<Frequency> resetFrequency() {
    return metaBean().resetFrequency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the reset calendar.
   * @return the value of the property, not null
   */
  public Set<ExternalId> getResetCalendars() {
    return _resetCalendars;
  }

  /**
   * Sets the reset calendar.
   * @param resetCalendars  the new value of the property, not null
   */
  public void setResetCalendars(Set<ExternalId> resetCalendars) {
    JodaBeanUtils.notNull(resetCalendars, "resetCalendars");
    this._resetCalendars.clear();
    this._resetCalendars.addAll(resetCalendars);
  }

  /**
   * Gets the the {@code resetCalendars} property.
   * @return the property, not null
   */
  public final Property<Set<ExternalId>> resetCalendars() {
    return metaBean().resetCalendars().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the reset business day convention
   * @return the value of the property, not null
   */
  public BusinessDayConvention getResetBusinessDayConvention() {
    return _resetBusinessDayConvention;
  }

  /**
   * Sets the reset business day convention
   * @param resetBusinessDayConvention  the new value of the property, not null
   */
  public void setResetBusinessDayConvention(BusinessDayConvention resetBusinessDayConvention) {
    JodaBeanUtils.notNull(resetBusinessDayConvention, "resetBusinessDayConvention");
    this._resetBusinessDayConvention = resetBusinessDayConvention;
  }

  /**
   * Gets the the {@code resetBusinessDayConvention} property.
   * @return the property, not null
   */
  public final Property<BusinessDayConvention> resetBusinessDayConvention() {
    return metaBean().resetBusinessDayConvention().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the reset relative to either the start or end of the period.
   * @return the value of the property, not null
   */
  public DateRelativeTo getResetRelativeTo() {
    return _resetRelativeTo;
  }

  /**
   * Sets the reset relative to either the start or end of the period.
   * @param resetRelativeTo  the new value of the property, not null
   */
  public void setResetRelativeTo(DateRelativeTo resetRelativeTo) {
    JodaBeanUtils.notNull(resetRelativeTo, "resetRelativeTo");
    this._resetRelativeTo = resetRelativeTo;
  }

  /**
   * Gets the the {@code resetRelativeTo} property.
   * @return the property, not null
   */
  public final Property<DateRelativeTo> resetRelativeTo() {
    return metaBean().resetRelativeTo().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the payment lag in days.
   * @return the value of the property
   */
  public int getPaymentLag() {
    return _paymentLag;
  }

  /**
   * Sets the payment lag in days.
   * @param paymentLag  the new value of the property
   */
  public void setPaymentLag(int paymentLag) {
    this._paymentLag = paymentLag;
  }

  /**
   * Gets the the {@code paymentLag} property.
   * @return the property, not null
   */
  public final Property<Integer> paymentLag() {
    return metaBean().paymentLag().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public FloatingInterestRateSwapLegConvention clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      FloatingInterestRateSwapLegConvention other = (FloatingInterestRateSwapLegConvention) obj;
      return JodaBeanUtils.equal(getRateType(), other.getRateType()) &&
          JodaBeanUtils.equal(getFixingCalendars(), other.getFixingCalendars()) &&
          JodaBeanUtils.equal(getFixingBusinessDayConvention(), other.getFixingBusinessDayConvention()) &&
          JodaBeanUtils.equal(getSettlementDayType(), other.getSettlementDayType()) &&
          JodaBeanUtils.equal(getResetFrequency(), other.getResetFrequency()) &&
          JodaBeanUtils.equal(getResetCalendars(), other.getResetCalendars()) &&
          JodaBeanUtils.equal(getResetBusinessDayConvention(), other.getResetBusinessDayConvention()) &&
          JodaBeanUtils.equal(getResetRelativeTo(), other.getResetRelativeTo()) &&
          (getPaymentLag() == other.getPaymentLag()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getRateType());
    hash = hash * 31 + JodaBeanUtils.hashCode(getFixingCalendars());
    hash = hash * 31 + JodaBeanUtils.hashCode(getFixingBusinessDayConvention());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSettlementDayType());
    hash = hash * 31 + JodaBeanUtils.hashCode(getResetFrequency());
    hash = hash * 31 + JodaBeanUtils.hashCode(getResetCalendars());
    hash = hash * 31 + JodaBeanUtils.hashCode(getResetBusinessDayConvention());
    hash = hash * 31 + JodaBeanUtils.hashCode(getResetRelativeTo());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPaymentLag());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(320);
    buf.append("FloatingInterestRateSwapLegConvention{");
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
    buf.append("rateType").append('=').append(JodaBeanUtils.toString(getRateType())).append(',').append(' ');
    buf.append("fixingCalendars").append('=').append(JodaBeanUtils.toString(getFixingCalendars())).append(',').append(' ');
    buf.append("fixingBusinessDayConvention").append('=').append(JodaBeanUtils.toString(getFixingBusinessDayConvention())).append(',').append(' ');
    buf.append("settlementDayType").append('=').append(JodaBeanUtils.toString(getSettlementDayType())).append(',').append(' ');
    buf.append("resetFrequency").append('=').append(JodaBeanUtils.toString(getResetFrequency())).append(',').append(' ');
    buf.append("resetCalendars").append('=').append(JodaBeanUtils.toString(getResetCalendars())).append(',').append(' ');
    buf.append("resetBusinessDayConvention").append('=').append(JodaBeanUtils.toString(getResetBusinessDayConvention())).append(',').append(' ');
    buf.append("resetRelativeTo").append('=').append(JodaBeanUtils.toString(getResetRelativeTo())).append(',').append(' ');
    buf.append("paymentLag").append('=').append(JodaBeanUtils.toString(getPaymentLag())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FloatingInterestRateSwapLegConvention}.
   */
  public static class Meta extends InterestRateSwapLegConvention.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code rateType} property.
     */
    private final MetaProperty<FloatingRateType> _rateType = DirectMetaProperty.ofReadWrite(
        this, "rateType", FloatingInterestRateSwapLegConvention.class, FloatingRateType.class);
    /**
     * The meta-property for the {@code fixingCalendars} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<ExternalId>> _fixingCalendars = DirectMetaProperty.ofReadWrite(
        this, "fixingCalendars", FloatingInterestRateSwapLegConvention.class, (Class) Set.class);
    /**
     * The meta-property for the {@code fixingBusinessDayConvention} property.
     */
    private final MetaProperty<BusinessDayConvention> _fixingBusinessDayConvention = DirectMetaProperty.ofReadWrite(
        this, "fixingBusinessDayConvention", FloatingInterestRateSwapLegConvention.class, BusinessDayConvention.class);
    /**
     * The meta-property for the {@code settlementDayType} property.
     */
    private final MetaProperty<OffsetType> _settlementDayType = DirectMetaProperty.ofReadWrite(
        this, "settlementDayType", FloatingInterestRateSwapLegConvention.class, OffsetType.class);
    /**
     * The meta-property for the {@code resetFrequency} property.
     */
    private final MetaProperty<Frequency> _resetFrequency = DirectMetaProperty.ofReadWrite(
        this, "resetFrequency", FloatingInterestRateSwapLegConvention.class, Frequency.class);
    /**
     * The meta-property for the {@code resetCalendars} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<ExternalId>> _resetCalendars = DirectMetaProperty.ofReadWrite(
        this, "resetCalendars", FloatingInterestRateSwapLegConvention.class, (Class) Set.class);
    /**
     * The meta-property for the {@code resetBusinessDayConvention} property.
     */
    private final MetaProperty<BusinessDayConvention> _resetBusinessDayConvention = DirectMetaProperty.ofReadWrite(
        this, "resetBusinessDayConvention", FloatingInterestRateSwapLegConvention.class, BusinessDayConvention.class);
    /**
     * The meta-property for the {@code resetRelativeTo} property.
     */
    private final MetaProperty<DateRelativeTo> _resetRelativeTo = DirectMetaProperty.ofReadWrite(
        this, "resetRelativeTo", FloatingInterestRateSwapLegConvention.class, DateRelativeTo.class);
    /**
     * The meta-property for the {@code paymentLag} property.
     */
    private final MetaProperty<Integer> _paymentLag = DirectMetaProperty.ofReadWrite(
        this, "paymentLag", FloatingInterestRateSwapLegConvention.class, Integer.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "rateType",
        "fixingCalendars",
        "fixingBusinessDayConvention",
        "settlementDayType",
        "resetFrequency",
        "resetCalendars",
        "resetBusinessDayConvention",
        "resetRelativeTo",
        "paymentLag");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 422305850:  // rateType
          return _rateType;
        case -663763000:  // fixingCalendars
          return _fixingCalendars;
        case 502310560:  // fixingBusinessDayConvention
          return _fixingBusinessDayConvention;
        case 980187021:  // settlementDayType
          return _settlementDayType;
        case 101322957:  // resetFrequency
          return _resetFrequency;
        case -1061750682:  // resetCalendars
          return _resetCalendars;
        case -1714562498:  // resetBusinessDayConvention
          return _resetBusinessDayConvention;
        case 779838742:  // resetRelativeTo
          return _resetRelativeTo;
        case 1612870060:  // paymentLag
          return _paymentLag;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends FloatingInterestRateSwapLegConvention> builder() {
      return new DirectBeanBuilder<FloatingInterestRateSwapLegConvention>(new FloatingInterestRateSwapLegConvention());
    }

    @Override
    public Class<? extends FloatingInterestRateSwapLegConvention> beanType() {
      return FloatingInterestRateSwapLegConvention.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code rateType} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<FloatingRateType> rateType() {
      return _rateType;
    }

    /**
     * The meta-property for the {@code fixingCalendars} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<ExternalId>> fixingCalendars() {
      return _fixingCalendars;
    }

    /**
     * The meta-property for the {@code fixingBusinessDayConvention} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BusinessDayConvention> fixingBusinessDayConvention() {
      return _fixingBusinessDayConvention;
    }

    /**
     * The meta-property for the {@code settlementDayType} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<OffsetType> settlementDayType() {
      return _settlementDayType;
    }

    /**
     * The meta-property for the {@code resetFrequency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Frequency> resetFrequency() {
      return _resetFrequency;
    }

    /**
     * The meta-property for the {@code resetCalendars} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<ExternalId>> resetCalendars() {
      return _resetCalendars;
    }

    /**
     * The meta-property for the {@code resetBusinessDayConvention} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BusinessDayConvention> resetBusinessDayConvention() {
      return _resetBusinessDayConvention;
    }

    /**
     * The meta-property for the {@code resetRelativeTo} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DateRelativeTo> resetRelativeTo() {
      return _resetRelativeTo;
    }

    /**
     * The meta-property for the {@code paymentLag} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> paymentLag() {
      return _paymentLag;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 422305850:  // rateType
          return ((FloatingInterestRateSwapLegConvention) bean).getRateType();
        case -663763000:  // fixingCalendars
          return ((FloatingInterestRateSwapLegConvention) bean).getFixingCalendars();
        case 502310560:  // fixingBusinessDayConvention
          return ((FloatingInterestRateSwapLegConvention) bean).getFixingBusinessDayConvention();
        case 980187021:  // settlementDayType
          return ((FloatingInterestRateSwapLegConvention) bean).getSettlementDayType();
        case 101322957:  // resetFrequency
          return ((FloatingInterestRateSwapLegConvention) bean).getResetFrequency();
        case -1061750682:  // resetCalendars
          return ((FloatingInterestRateSwapLegConvention) bean).getResetCalendars();
        case -1714562498:  // resetBusinessDayConvention
          return ((FloatingInterestRateSwapLegConvention) bean).getResetBusinessDayConvention();
        case 779838742:  // resetRelativeTo
          return ((FloatingInterestRateSwapLegConvention) bean).getResetRelativeTo();
        case 1612870060:  // paymentLag
          return ((FloatingInterestRateSwapLegConvention) bean).getPaymentLag();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 422305850:  // rateType
          ((FloatingInterestRateSwapLegConvention) bean).setRateType((FloatingRateType) newValue);
          return;
        case -663763000:  // fixingCalendars
          ((FloatingInterestRateSwapLegConvention) bean).setFixingCalendars((Set<ExternalId>) newValue);
          return;
        case 502310560:  // fixingBusinessDayConvention
          ((FloatingInterestRateSwapLegConvention) bean).setFixingBusinessDayConvention((BusinessDayConvention) newValue);
          return;
        case 980187021:  // settlementDayType
          ((FloatingInterestRateSwapLegConvention) bean).setSettlementDayType((OffsetType) newValue);
          return;
        case 101322957:  // resetFrequency
          ((FloatingInterestRateSwapLegConvention) bean).setResetFrequency((Frequency) newValue);
          return;
        case -1061750682:  // resetCalendars
          ((FloatingInterestRateSwapLegConvention) bean).setResetCalendars((Set<ExternalId>) newValue);
          return;
        case -1714562498:  // resetBusinessDayConvention
          ((FloatingInterestRateSwapLegConvention) bean).setResetBusinessDayConvention((BusinessDayConvention) newValue);
          return;
        case 779838742:  // resetRelativeTo
          ((FloatingInterestRateSwapLegConvention) bean).setResetRelativeTo((DateRelativeTo) newValue);
          return;
        case 1612870060:  // paymentLag
          ((FloatingInterestRateSwapLegConvention) bean).setPaymentLag((Integer) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((FloatingInterestRateSwapLegConvention) bean)._rateType, "rateType");
      JodaBeanUtils.notNull(((FloatingInterestRateSwapLegConvention) bean)._fixingCalendars, "fixingCalendars");
      JodaBeanUtils.notNull(((FloatingInterestRateSwapLegConvention) bean)._fixingBusinessDayConvention, "fixingBusinessDayConvention");
      JodaBeanUtils.notNull(((FloatingInterestRateSwapLegConvention) bean)._settlementDayType, "settlementDayType");
      JodaBeanUtils.notNull(((FloatingInterestRateSwapLegConvention) bean)._resetFrequency, "resetFrequency");
      JodaBeanUtils.notNull(((FloatingInterestRateSwapLegConvention) bean)._resetCalendars, "resetCalendars");
      JodaBeanUtils.notNull(((FloatingInterestRateSwapLegConvention) bean)._resetBusinessDayConvention, "resetBusinessDayConvention");
      JodaBeanUtils.notNull(((FloatingInterestRateSwapLegConvention) bean)._resetRelativeTo, "resetRelativeTo");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
