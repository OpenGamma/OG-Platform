/*
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.credit;

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

import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.core.legalentity.SeniorityLevel;
import com.opengamma.core.position.Trade;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.SecurityDescription;

/**
 *  A legacy (i.e pre big bang) credit default swap.
 */
@BeanDefinition
@SecurityDescription(type = LegacyCDSSecurity.SECURITY_TYPE, description = "legacy cds")
public class LegacyCDSSecurity extends FinancialSecurity {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * The security type
   */
  public static final String SECURITY_TYPE = "LEGACY_CDS";

  /**
   * The trade date, aka T.
   */
  @PropertyDefinition(validate = "notNull")
  private LocalDate _tradeDate;

  /**
   * The start date
   */
  @PropertyDefinition(validate = "notNull")
  private LocalDate _startDate;

  /**
   * The maturity date.
   */
  @PropertyDefinition(validate = "notNull")
  private LocalDate _maturityDate;

  /**
   * The reference entity.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalId _referenceEntity;

  /**
   * Is protection being bought?
   */
  @PropertyDefinition(validate = "notNull")
  private boolean _buyProtection;

  /**
   * The notional.
   */
  @PropertyDefinition(validate = "notNull")
  private InterestRateNotional _notional;

  /**
   * The debt seniority.
   */
  @PropertyDefinition(validate = "notNull")
  private SeniorityLevel _seniority;

  /**
   * The premium leg coupon (fractional i.e. 100 bps = 0.01)
   */
  @PropertyDefinition(validate = "notNull")
  private double _coupon;

  /**
   * The coupon frequency
   */
  @PropertyDefinition(validate = "notNull")
  private Frequency _couponFrequency;

  /**
   * The Daycount
   */
  @PropertyDefinition(validate = "notNull")
  private DayCount _dayCount;

  /**
   * The business day convention
   */
  @PropertyDefinition(validate = "notNull")
  private BusinessDayConvention _businessDayConvention;

  /**
   * The holiday calendars
   */
  @PropertyDefinition(validate = "notNull")
  private Set<ExternalId> _calendars;

  /**
   * The restructuring clause.
   */
  @PropertyDefinition(validate = "notNull")
  private RestructuringClause _restructuringClause;

  /**
   * The amount of upfront exchanged (on settlementDate) - can be positive or negative.
   */
  @PropertyDefinition(validate = "notNull")
  private InterestRateNotional _upfrontPayment;

  /**
   * The fee settlement date
   */
  @PropertyDefinition(validate = "notNull")
  private LocalDate _feeSettlementDate;

  /**
   * accrued on default
   */
  @PropertyDefinition(validate = "notNull")
  private boolean _accruedOnDefault;

  /**
   * Recovery rate for a fixed recovery cds. Optional.
   */
  @PropertyDefinition
  private Double _fixedRecovery;

  LegacyCDSSecurity() { // for fudge
    super(SECURITY_TYPE);
  }

  /**
   * @param ids the trade identifier, not null
   * @param tradeDate the trade date, not null
   * @param maturityDate the maturity date, not null
   * @param referenceEntity the reference entity, not null
   * @param notional the notional, not null
   * @param isBuy is protection being bought, not null
   * @param coupon the coupon, in basis points, not null
   * @param seniority the seniority, not null
   * @param couponFrequency the coupon frequency, not null
   * @param daycount the daycount, not null
   * @param businessDayConvention the business day convention, not null
   * @param calendars the holiday calendars, not null
   * @param restructuring the restructuring clause, not null
   * @param upfrontPayment the upfront payment, not null
   * @param feeSettlementDate the settlement date, not null
   * @param accruedOnDefault accrual on default flag, not null
   *
   * @deprecated use constructor that uses start date.
   */
  @Deprecated
  public LegacyCDSSecurity(final ExternalIdBundle ids,
                           final LocalDate tradeDate,
                           final LocalDate maturityDate,
                           final ExternalId referenceEntity,
                           final InterestRateNotional notional,
                           final boolean isBuy,
                           final double coupon,
                           final SeniorityLevel seniority,
                           final Frequency couponFrequency,
                           final DayCount daycount,
                           final BusinessDayConvention businessDayConvention,
                           final Set<ExternalId> calendars,
                           final RestructuringClause restructuring,
                           final InterestRateNotional upfrontPayment,
                           final LocalDate feeSettlementDate,
                           final boolean accruedOnDefault) {
    super(SECURITY_TYPE);
    throw new UnsupportedOperationException("Unsupported LegacyCDSSecurity constructor.");
  }

  /**
   * @param ids the trade identifier, not null
   * @param name descriptive name for the security, not null
   * @param tradeDate the trade date, not null
   * @param maturityDate the maturity date, not null
   * @param referenceEntity the reference entity, not null
   * @param notional the notional, not null
   * @param isBuy is protection being bought, not null
   * @param coupon the coupon, in basis points, not null
   * @param seniority the seniority, not null
   * @param couponFrequency the coupon frequency, not null
   * @param daycount the daycount, not null
   * @param businessDayConvention the business day convention, not null
   * @param calendars the holiday calendars, not null
   * @param restructuring the restructuring clause, not null
   * @param upfrontPayment the upfront payment, not null
   * @param feeSettlementDate the settlement date, not null
   * @param accruedOnDefault accrual on default flag, not null
   *
   * @deprecated use constructor that uses start date.
   */
  @Deprecated
  public LegacyCDSSecurity(final ExternalIdBundle ids,
                           final String name,
                           final LocalDate tradeDate,
                           final LocalDate maturityDate,
                           final ExternalId referenceEntity,
                           final InterestRateNotional notional,
                           final boolean isBuy,
                           final double coupon,
                           final SeniorityLevel seniority,
                           final Frequency couponFrequency,
                           final DayCount daycount,
                           final BusinessDayConvention businessDayConvention,
                           final Set<ExternalId> calendars,
                           final RestructuringClause restructuring,
                           final InterestRateNotional upfrontPayment,
                           final LocalDate feeSettlementDate,
                           final boolean accruedOnDefault) {
    super(SECURITY_TYPE);
    throw new UnsupportedOperationException("Unsupported LegacyCDSSecurity constructor.");
  }

  /**
   * @param ids the trade identifier, not null
   * @param tradeDate the trade date, not null
   * @param startDate the start date, not null
   * @param maturityDate the maturity date, not null
   * @param referenceEntity the reference entity, not null
   * @param notional the notional, not null
   * @param isBuy is protection being bought, not null
   * @param coupon the coupon, in basis points, not null
   * @param seniority the seniority, not null
   * @param couponFrequency the coupon frequency, not null
   * @param daycount the daycount, not null
   * @param businessDayConvention the business day convention, not null
   * @param calendars the holiday calendars, not null
   * @param restructuring the restructuring clause, not null
   * @param upfrontPayment the upfront payment, not null
   * @param feeSettlementDate the settlement date, not null
   * @param accruedOnDefault accrual on default flag, not null
   */
  public LegacyCDSSecurity(final ExternalIdBundle ids, 
                           final LocalDate tradeDate,
                           final LocalDate startDate,
                           final LocalDate maturityDate, 
                           final ExternalId referenceEntity,
                           final InterestRateNotional notional, 
                           final boolean isBuy, 
                           final double coupon, 
                           final SeniorityLevel seniority,
                           final Frequency couponFrequency, 
                           final DayCount daycount, 
                           final BusinessDayConvention businessDayConvention,
                           final Set<ExternalId> calendars, 
                           final RestructuringClause restructuring, 
                           final InterestRateNotional upfrontPayment,
                           final LocalDate feeSettlementDate, 
                           final boolean accruedOnDefault) {
    super(SECURITY_TYPE);
    setExternalIdBundle(ids);
    setTradeDate(tradeDate);
    setStartDate(startDate);
    setMaturityDate(maturityDate);
    setReferenceEntity(referenceEntity);
    setNotional(notional);
    setBuyProtection(isBuy);
    setCoupon(coupon);
    setSeniority(seniority);
    setCouponFrequency(couponFrequency);
    setDayCount(daycount);
    setBusinessDayConvention(businessDayConvention);
    setCalendars(calendars);
    setRestructuringClause(restructuring);
    setUpfrontPayment(upfrontPayment);
    setFeeSettlementDate(feeSettlementDate);
    setAccruedOnDefault(accruedOnDefault);
  }

  /**
   * @param ids the trade identifier, not null
   * @param name descriptive name for the security, not null
   * @param tradeDate the trade date, not null
   * @param startDate the start date, not null
   * @param maturityDate the maturity date, not null
   * @param referenceEntity the reference entity, not null
   * @param notional the notional, not null
   * @param isBuy is protection being bought, not null
   * @param coupon the coupon, in basis points, not null
   * @param seniority the seniority, not null
   * @param couponFrequency the coupon frequency, not null
   * @param daycount the daycount, not null
   * @param businessDayConvention the business day convention, not null
   * @param calendars the holiday calendars, not null
   * @param restructuring the restructuring clause, not null
   * @param upfrontPayment the upfront payment, not null
   * @param feeSettlementDate the settlement date, not null
   * @param accruedOnDefault accrual on default flag, not null
   */
  public LegacyCDSSecurity(final ExternalIdBundle ids, 
                           final String name, 
                           final LocalDate tradeDate,
                           final LocalDate startDate,
                           final LocalDate maturityDate, 
                           final ExternalId referenceEntity,
                           final InterestRateNotional notional, 
                           final boolean isBuy, 
                           final double coupon, 
                           final SeniorityLevel seniority,
                           final Frequency couponFrequency, 
                           final DayCount daycount, 
                           final BusinessDayConvention businessDayConvention,
                           final Set<ExternalId> calendars, 
                           final RestructuringClause restructuring, 
                           final InterestRateNotional upfrontPayment,
                           final LocalDate feeSettlementDate, 
                           final boolean accruedOnDefault) {
    super(SECURITY_TYPE);
    setName(name);
    setExternalIdBundle(ids);
    setTradeDate(tradeDate);
    setStartDate(startDate);
    setMaturityDate(maturityDate);
    setReferenceEntity(referenceEntity);
    setNotional(notional);
    setBuyProtection(isBuy);
    setCoupon(coupon);
    setSeniority(seniority);
    setCouponFrequency(couponFrequency);
    setDayCount(daycount);
    setBusinessDayConvention(businessDayConvention);
    setCalendars(calendars);
    setRestructuringClause(restructuring);
    setUpfrontPayment(upfrontPayment);
    setFeeSettlementDate(feeSettlementDate);
    setAccruedOnDefault(accruedOnDefault);
  }

  @Override
  public final <T> T accept(final FinancialSecurityVisitor<T> visitor) {
    return visitor.visitLegacyCDSSecurity(this);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code LegacyCDSSecurity}.
   * @return the meta-bean, not null
   */
  public static LegacyCDSSecurity.Meta meta() {
    return LegacyCDSSecurity.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(LegacyCDSSecurity.Meta.INSTANCE);
  }

  @Override
  public LegacyCDSSecurity.Meta metaBean() {
    return LegacyCDSSecurity.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the trade date, aka T.
   * @return the value of the property, not null
   */
  public LocalDate getTradeDate() {
    return _tradeDate;
  }

  /**
   * Sets the trade date, aka T.
   * @param tradeDate  the new value of the property, not null
   */
  public void setTradeDate(LocalDate tradeDate) {
    JodaBeanUtils.notNull(tradeDate, "tradeDate");
    this._tradeDate = tradeDate;
  }

  /**
   * Gets the the {@code tradeDate} property.
   * @return the property, not null
   */
  public final Property<LocalDate> tradeDate() {
    return metaBean().tradeDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the start date
   * @return the value of the property, not null
   */
  public LocalDate getStartDate() {
    return _startDate;
  }

  /**
   * Sets the start date
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
   * Gets the maturity date.
   * @return the value of the property, not null
   */
  public LocalDate getMaturityDate() {
    return _maturityDate;
  }

  /**
   * Sets the maturity date.
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
   * Gets the reference entity.
   * @return the value of the property, not null
   */
  public ExternalId getReferenceEntity() {
    return _referenceEntity;
  }

  /**
   * Sets the reference entity.
   * @param referenceEntity  the new value of the property, not null
   */
  public void setReferenceEntity(ExternalId referenceEntity) {
    JodaBeanUtils.notNull(referenceEntity, "referenceEntity");
    this._referenceEntity = referenceEntity;
  }

  /**
   * Gets the the {@code referenceEntity} property.
   * @return the property, not null
   */
  public final Property<ExternalId> referenceEntity() {
    return metaBean().referenceEntity().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets is protection being bought?
   * @return the value of the property, not null
   */
  public boolean isBuyProtection() {
    return _buyProtection;
  }

  /**
   * Sets is protection being bought?
   * @param buyProtection  the new value of the property, not null
   */
  public void setBuyProtection(boolean buyProtection) {
    JodaBeanUtils.notNull(buyProtection, "buyProtection");
    this._buyProtection = buyProtection;
  }

  /**
   * Gets the the {@code buyProtection} property.
   * @return the property, not null
   */
  public final Property<Boolean> buyProtection() {
    return metaBean().buyProtection().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the notional.
   * @return the value of the property, not null
   */
  public InterestRateNotional getNotional() {
    return _notional;
  }

  /**
   * Sets the notional.
   * @param notional  the new value of the property, not null
   */
  public void setNotional(InterestRateNotional notional) {
    JodaBeanUtils.notNull(notional, "notional");
    this._notional = notional;
  }

  /**
   * Gets the the {@code notional} property.
   * @return the property, not null
   */
  public final Property<InterestRateNotional> notional() {
    return metaBean().notional().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the debt seniority.
   * @return the value of the property, not null
   */
  public SeniorityLevel getSeniority() {
    return _seniority;
  }

  /**
   * Sets the debt seniority.
   * @param seniority  the new value of the property, not null
   */
  public void setSeniority(SeniorityLevel seniority) {
    JodaBeanUtils.notNull(seniority, "seniority");
    this._seniority = seniority;
  }

  /**
   * Gets the the {@code seniority} property.
   * @return the property, not null
   */
  public final Property<SeniorityLevel> seniority() {
    return metaBean().seniority().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the premium leg coupon (fractional i.e. 100 bps = 0.01)
   * @return the value of the property, not null
   */
  public double getCoupon() {
    return _coupon;
  }

  /**
   * Sets the premium leg coupon (fractional i.e. 100 bps = 0.01)
   * @param coupon  the new value of the property, not null
   */
  public void setCoupon(double coupon) {
    JodaBeanUtils.notNull(coupon, "coupon");
    this._coupon = coupon;
  }

  /**
   * Gets the the {@code coupon} property.
   * @return the property, not null
   */
  public final Property<Double> coupon() {
    return metaBean().coupon().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the coupon frequency
   * @return the value of the property, not null
   */
  public Frequency getCouponFrequency() {
    return _couponFrequency;
  }

  /**
   * Sets the coupon frequency
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
  /**
   * Gets the Daycount
   * @return the value of the property, not null
   */
  public DayCount getDayCount() {
    return _dayCount;
  }

  /**
   * Sets the Daycount
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
   * Gets the business day convention
   * @return the value of the property, not null
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }

  /**
   * Sets the business day convention
   * @param businessDayConvention  the new value of the property, not null
   */
  public void setBusinessDayConvention(BusinessDayConvention businessDayConvention) {
    JodaBeanUtils.notNull(businessDayConvention, "businessDayConvention");
    this._businessDayConvention = businessDayConvention;
  }

  /**
   * Gets the the {@code businessDayConvention} property.
   * @return the property, not null
   */
  public final Property<BusinessDayConvention> businessDayConvention() {
    return metaBean().businessDayConvention().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the holiday calendars
   * @return the value of the property, not null
   */
  public Set<ExternalId> getCalendars() {
    return _calendars;
  }

  /**
   * Sets the holiday calendars
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
   * Gets the restructuring clause.
   * @return the value of the property, not null
   */
  public RestructuringClause getRestructuringClause() {
    return _restructuringClause;
  }

  /**
   * Sets the restructuring clause.
   * @param restructuringClause  the new value of the property, not null
   */
  public void setRestructuringClause(RestructuringClause restructuringClause) {
    JodaBeanUtils.notNull(restructuringClause, "restructuringClause");
    this._restructuringClause = restructuringClause;
  }

  /**
   * Gets the the {@code restructuringClause} property.
   * @return the property, not null
   */
  public final Property<RestructuringClause> restructuringClause() {
    return metaBean().restructuringClause().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the amount of upfront exchanged (on settlementDate) - can be positive or negative.
   * @return the value of the property, not null
   */
  public InterestRateNotional getUpfrontPayment() {
    return _upfrontPayment;
  }

  /**
   * Sets the amount of upfront exchanged (on settlementDate) - can be positive or negative.
   * @param upfrontPayment  the new value of the property, not null
   */
  public void setUpfrontPayment(InterestRateNotional upfrontPayment) {
    JodaBeanUtils.notNull(upfrontPayment, "upfrontPayment");
    this._upfrontPayment = upfrontPayment;
  }

  /**
   * Gets the the {@code upfrontPayment} property.
   * @return the property, not null
   */
  public final Property<InterestRateNotional> upfrontPayment() {
    return metaBean().upfrontPayment().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the fee settlement date
   * @return the value of the property, not null
   */
  public LocalDate getFeeSettlementDate() {
    return _feeSettlementDate;
  }

  /**
   * Sets the fee settlement date
   * @param feeSettlementDate  the new value of the property, not null
   */
  public void setFeeSettlementDate(LocalDate feeSettlementDate) {
    JodaBeanUtils.notNull(feeSettlementDate, "feeSettlementDate");
    this._feeSettlementDate = feeSettlementDate;
  }

  /**
   * Gets the the {@code feeSettlementDate} property.
   * @return the property, not null
   */
  public final Property<LocalDate> feeSettlementDate() {
    return metaBean().feeSettlementDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets accrued on default
   * @return the value of the property, not null
   */
  public boolean isAccruedOnDefault() {
    return _accruedOnDefault;
  }

  /**
   * Sets accrued on default
   * @param accruedOnDefault  the new value of the property, not null
   */
  public void setAccruedOnDefault(boolean accruedOnDefault) {
    JodaBeanUtils.notNull(accruedOnDefault, "accruedOnDefault");
    this._accruedOnDefault = accruedOnDefault;
  }

  /**
   * Gets the the {@code accruedOnDefault} property.
   * @return the property, not null
   */
  public final Property<Boolean> accruedOnDefault() {
    return metaBean().accruedOnDefault().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets recovery rate for a fixed recovery cds. Optional.
   * @return the value of the property
   */
  public Double getFixedRecovery() {
    return _fixedRecovery;
  }

  /**
   * Sets recovery rate for a fixed recovery cds. Optional.
   * @param fixedRecovery  the new value of the property
   */
  public void setFixedRecovery(Double fixedRecovery) {
    this._fixedRecovery = fixedRecovery;
  }

  /**
   * Gets the the {@code fixedRecovery} property.
   * @return the property, not null
   */
  public final Property<Double> fixedRecovery() {
    return metaBean().fixedRecovery().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public LegacyCDSSecurity clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      LegacyCDSSecurity other = (LegacyCDSSecurity) obj;
      return JodaBeanUtils.equal(getTradeDate(), other.getTradeDate()) &&
          JodaBeanUtils.equal(getStartDate(), other.getStartDate()) &&
          JodaBeanUtils.equal(getMaturityDate(), other.getMaturityDate()) &&
          JodaBeanUtils.equal(getReferenceEntity(), other.getReferenceEntity()) &&
          (isBuyProtection() == other.isBuyProtection()) &&
          JodaBeanUtils.equal(getNotional(), other.getNotional()) &&
          JodaBeanUtils.equal(getSeniority(), other.getSeniority()) &&
          JodaBeanUtils.equal(getCoupon(), other.getCoupon()) &&
          JodaBeanUtils.equal(getCouponFrequency(), other.getCouponFrequency()) &&
          JodaBeanUtils.equal(getDayCount(), other.getDayCount()) &&
          JodaBeanUtils.equal(getBusinessDayConvention(), other.getBusinessDayConvention()) &&
          JodaBeanUtils.equal(getCalendars(), other.getCalendars()) &&
          JodaBeanUtils.equal(getRestructuringClause(), other.getRestructuringClause()) &&
          JodaBeanUtils.equal(getUpfrontPayment(), other.getUpfrontPayment()) &&
          JodaBeanUtils.equal(getFeeSettlementDate(), other.getFeeSettlementDate()) &&
          (isAccruedOnDefault() == other.isAccruedOnDefault()) &&
          JodaBeanUtils.equal(getFixedRecovery(), other.getFixedRecovery()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getTradeDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getStartDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMaturityDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getReferenceEntity());
    hash += hash * 31 + JodaBeanUtils.hashCode(isBuyProtection());
    hash += hash * 31 + JodaBeanUtils.hashCode(getNotional());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSeniority());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCoupon());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCouponFrequency());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDayCount());
    hash += hash * 31 + JodaBeanUtils.hashCode(getBusinessDayConvention());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCalendars());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRestructuringClause());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUpfrontPayment());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFeeSettlementDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(isAccruedOnDefault());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFixedRecovery());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(576);
    buf.append("LegacyCDSSecurity{");
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
    buf.append("tradeDate").append('=').append(JodaBeanUtils.toString(getTradeDate())).append(',').append(' ');
    buf.append("startDate").append('=').append(JodaBeanUtils.toString(getStartDate())).append(',').append(' ');
    buf.append("maturityDate").append('=').append(JodaBeanUtils.toString(getMaturityDate())).append(',').append(' ');
    buf.append("referenceEntity").append('=').append(JodaBeanUtils.toString(getReferenceEntity())).append(',').append(' ');
    buf.append("buyProtection").append('=').append(JodaBeanUtils.toString(isBuyProtection())).append(',').append(' ');
    buf.append("notional").append('=').append(JodaBeanUtils.toString(getNotional())).append(',').append(' ');
    buf.append("seniority").append('=').append(JodaBeanUtils.toString(getSeniority())).append(',').append(' ');
    buf.append("coupon").append('=').append(JodaBeanUtils.toString(getCoupon())).append(',').append(' ');
    buf.append("couponFrequency").append('=').append(JodaBeanUtils.toString(getCouponFrequency())).append(',').append(' ');
    buf.append("dayCount").append('=').append(JodaBeanUtils.toString(getDayCount())).append(',').append(' ');
    buf.append("businessDayConvention").append('=').append(JodaBeanUtils.toString(getBusinessDayConvention())).append(',').append(' ');
    buf.append("calendars").append('=').append(JodaBeanUtils.toString(getCalendars())).append(',').append(' ');
    buf.append("restructuringClause").append('=').append(JodaBeanUtils.toString(getRestructuringClause())).append(',').append(' ');
    buf.append("upfrontPayment").append('=').append(JodaBeanUtils.toString(getUpfrontPayment())).append(',').append(' ');
    buf.append("feeSettlementDate").append('=').append(JodaBeanUtils.toString(getFeeSettlementDate())).append(',').append(' ');
    buf.append("accruedOnDefault").append('=').append(JodaBeanUtils.toString(isAccruedOnDefault())).append(',').append(' ');
    buf.append("fixedRecovery").append('=').append(JodaBeanUtils.toString(getFixedRecovery())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code LegacyCDSSecurity}.
   */
  public static class Meta extends FinancialSecurity.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code tradeDate} property.
     */
    private final MetaProperty<LocalDate> _tradeDate = DirectMetaProperty.ofReadWrite(
        this, "tradeDate", LegacyCDSSecurity.class, LocalDate.class);
    /**
     * The meta-property for the {@code startDate} property.
     */
    private final MetaProperty<LocalDate> _startDate = DirectMetaProperty.ofReadWrite(
        this, "startDate", LegacyCDSSecurity.class, LocalDate.class);
    /**
     * The meta-property for the {@code maturityDate} property.
     */
    private final MetaProperty<LocalDate> _maturityDate = DirectMetaProperty.ofReadWrite(
        this, "maturityDate", LegacyCDSSecurity.class, LocalDate.class);
    /**
     * The meta-property for the {@code referenceEntity} property.
     */
    private final MetaProperty<ExternalId> _referenceEntity = DirectMetaProperty.ofReadWrite(
        this, "referenceEntity", LegacyCDSSecurity.class, ExternalId.class);
    /**
     * The meta-property for the {@code buyProtection} property.
     */
    private final MetaProperty<Boolean> _buyProtection = DirectMetaProperty.ofReadWrite(
        this, "buyProtection", LegacyCDSSecurity.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code notional} property.
     */
    private final MetaProperty<InterestRateNotional> _notional = DirectMetaProperty.ofReadWrite(
        this, "notional", LegacyCDSSecurity.class, InterestRateNotional.class);
    /**
     * The meta-property for the {@code seniority} property.
     */
    private final MetaProperty<SeniorityLevel> _seniority = DirectMetaProperty.ofReadWrite(
        this, "seniority", LegacyCDSSecurity.class, SeniorityLevel.class);
    /**
     * The meta-property for the {@code coupon} property.
     */
    private final MetaProperty<Double> _coupon = DirectMetaProperty.ofReadWrite(
        this, "coupon", LegacyCDSSecurity.class, Double.TYPE);
    /**
     * The meta-property for the {@code couponFrequency} property.
     */
    private final MetaProperty<Frequency> _couponFrequency = DirectMetaProperty.ofReadWrite(
        this, "couponFrequency", LegacyCDSSecurity.class, Frequency.class);
    /**
     * The meta-property for the {@code dayCount} property.
     */
    private final MetaProperty<DayCount> _dayCount = DirectMetaProperty.ofReadWrite(
        this, "dayCount", LegacyCDSSecurity.class, DayCount.class);
    /**
     * The meta-property for the {@code businessDayConvention} property.
     */
    private final MetaProperty<BusinessDayConvention> _businessDayConvention = DirectMetaProperty.ofReadWrite(
        this, "businessDayConvention", LegacyCDSSecurity.class, BusinessDayConvention.class);
    /**
     * The meta-property for the {@code calendars} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<ExternalId>> _calendars = DirectMetaProperty.ofReadWrite(
        this, "calendars", LegacyCDSSecurity.class, (Class) Set.class);
    /**
     * The meta-property for the {@code restructuringClause} property.
     */
    private final MetaProperty<RestructuringClause> _restructuringClause = DirectMetaProperty.ofReadWrite(
        this, "restructuringClause", LegacyCDSSecurity.class, RestructuringClause.class);
    /**
     * The meta-property for the {@code upfrontPayment} property.
     */
    private final MetaProperty<InterestRateNotional> _upfrontPayment = DirectMetaProperty.ofReadWrite(
        this, "upfrontPayment", LegacyCDSSecurity.class, InterestRateNotional.class);
    /**
     * The meta-property for the {@code feeSettlementDate} property.
     */
    private final MetaProperty<LocalDate> _feeSettlementDate = DirectMetaProperty.ofReadWrite(
        this, "feeSettlementDate", LegacyCDSSecurity.class, LocalDate.class);
    /**
     * The meta-property for the {@code accruedOnDefault} property.
     */
    private final MetaProperty<Boolean> _accruedOnDefault = DirectMetaProperty.ofReadWrite(
        this, "accruedOnDefault", LegacyCDSSecurity.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code fixedRecovery} property.
     */
    private final MetaProperty<Double> _fixedRecovery = DirectMetaProperty.ofReadWrite(
        this, "fixedRecovery", LegacyCDSSecurity.class, Double.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "tradeDate",
        "startDate",
        "maturityDate",
        "referenceEntity",
        "buyProtection",
        "notional",
        "seniority",
        "coupon",
        "couponFrequency",
        "dayCount",
        "businessDayConvention",
        "calendars",
        "restructuringClause",
        "upfrontPayment",
        "feeSettlementDate",
        "accruedOnDefault",
        "fixedRecovery");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 752419634:  // tradeDate
          return _tradeDate;
        case -2129778896:  // startDate
          return _startDate;
        case -414641441:  // maturityDate
          return _maturityDate;
        case 480652046:  // referenceEntity
          return _referenceEntity;
        case 1154909695:  // buyProtection
          return _buyProtection;
        case 1585636160:  // notional
          return _notional;
        case 184581246:  // seniority
          return _seniority;
        case -1354573786:  // coupon
          return _coupon;
        case 144480214:  // couponFrequency
          return _couponFrequency;
        case 1905311443:  // dayCount
          return _dayCount;
        case -1002835891:  // businessDayConvention
          return _businessDayConvention;
        case -1233097483:  // calendars
          return _calendars;
        case -1774904020:  // restructuringClause
          return _restructuringClause;
        case -638821960:  // upfrontPayment
          return _upfrontPayment;
        case 1215227293:  // feeSettlementDate
          return _feeSettlementDate;
        case -1719383937:  // accruedOnDefault
          return _accruedOnDefault;
        case 456333097:  // fixedRecovery
          return _fixedRecovery;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends LegacyCDSSecurity> builder() {
      return new DirectBeanBuilder<LegacyCDSSecurity>(new LegacyCDSSecurity());
    }

    @Override
    public Class<? extends LegacyCDSSecurity> beanType() {
      return LegacyCDSSecurity.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code tradeDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> tradeDate() {
      return _tradeDate;
    }

    /**
     * The meta-property for the {@code startDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> startDate() {
      return _startDate;
    }

    /**
     * The meta-property for the {@code maturityDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> maturityDate() {
      return _maturityDate;
    }

    /**
     * The meta-property for the {@code referenceEntity} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> referenceEntity() {
      return _referenceEntity;
    }

    /**
     * The meta-property for the {@code buyProtection} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> buyProtection() {
      return _buyProtection;
    }

    /**
     * The meta-property for the {@code notional} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<InterestRateNotional> notional() {
      return _notional;
    }

    /**
     * The meta-property for the {@code seniority} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<SeniorityLevel> seniority() {
      return _seniority;
    }

    /**
     * The meta-property for the {@code coupon} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> coupon() {
      return _coupon;
    }

    /**
     * The meta-property for the {@code couponFrequency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Frequency> couponFrequency() {
      return _couponFrequency;
    }

    /**
     * The meta-property for the {@code dayCount} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DayCount> dayCount() {
      return _dayCount;
    }

    /**
     * The meta-property for the {@code businessDayConvention} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BusinessDayConvention> businessDayConvention() {
      return _businessDayConvention;
    }

    /**
     * The meta-property for the {@code calendars} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<ExternalId>> calendars() {
      return _calendars;
    }

    /**
     * The meta-property for the {@code restructuringClause} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<RestructuringClause> restructuringClause() {
      return _restructuringClause;
    }

    /**
     * The meta-property for the {@code upfrontPayment} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<InterestRateNotional> upfrontPayment() {
      return _upfrontPayment;
    }

    /**
     * The meta-property for the {@code feeSettlementDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> feeSettlementDate() {
      return _feeSettlementDate;
    }

    /**
     * The meta-property for the {@code accruedOnDefault} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> accruedOnDefault() {
      return _accruedOnDefault;
    }

    /**
     * The meta-property for the {@code fixedRecovery} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> fixedRecovery() {
      return _fixedRecovery;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 752419634:  // tradeDate
          return ((LegacyCDSSecurity) bean).getTradeDate();
        case -2129778896:  // startDate
          return ((LegacyCDSSecurity) bean).getStartDate();
        case -414641441:  // maturityDate
          return ((LegacyCDSSecurity) bean).getMaturityDate();
        case 480652046:  // referenceEntity
          return ((LegacyCDSSecurity) bean).getReferenceEntity();
        case 1154909695:  // buyProtection
          return ((LegacyCDSSecurity) bean).isBuyProtection();
        case 1585636160:  // notional
          return ((LegacyCDSSecurity) bean).getNotional();
        case 184581246:  // seniority
          return ((LegacyCDSSecurity) bean).getSeniority();
        case -1354573786:  // coupon
          return ((LegacyCDSSecurity) bean).getCoupon();
        case 144480214:  // couponFrequency
          return ((LegacyCDSSecurity) bean).getCouponFrequency();
        case 1905311443:  // dayCount
          return ((LegacyCDSSecurity) bean).getDayCount();
        case -1002835891:  // businessDayConvention
          return ((LegacyCDSSecurity) bean).getBusinessDayConvention();
        case -1233097483:  // calendars
          return ((LegacyCDSSecurity) bean).getCalendars();
        case -1774904020:  // restructuringClause
          return ((LegacyCDSSecurity) bean).getRestructuringClause();
        case -638821960:  // upfrontPayment
          return ((LegacyCDSSecurity) bean).getUpfrontPayment();
        case 1215227293:  // feeSettlementDate
          return ((LegacyCDSSecurity) bean).getFeeSettlementDate();
        case -1719383937:  // accruedOnDefault
          return ((LegacyCDSSecurity) bean).isAccruedOnDefault();
        case 456333097:  // fixedRecovery
          return ((LegacyCDSSecurity) bean).getFixedRecovery();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 752419634:  // tradeDate
          ((LegacyCDSSecurity) bean).setTradeDate((LocalDate) newValue);
          return;
        case -2129778896:  // startDate
          ((LegacyCDSSecurity) bean).setStartDate((LocalDate) newValue);
          return;
        case -414641441:  // maturityDate
          ((LegacyCDSSecurity) bean).setMaturityDate((LocalDate) newValue);
          return;
        case 480652046:  // referenceEntity
          ((LegacyCDSSecurity) bean).setReferenceEntity((ExternalId) newValue);
          return;
        case 1154909695:  // buyProtection
          ((LegacyCDSSecurity) bean).setBuyProtection((Boolean) newValue);
          return;
        case 1585636160:  // notional
          ((LegacyCDSSecurity) bean).setNotional((InterestRateNotional) newValue);
          return;
        case 184581246:  // seniority
          ((LegacyCDSSecurity) bean).setSeniority((SeniorityLevel) newValue);
          return;
        case -1354573786:  // coupon
          ((LegacyCDSSecurity) bean).setCoupon((Double) newValue);
          return;
        case 144480214:  // couponFrequency
          ((LegacyCDSSecurity) bean).setCouponFrequency((Frequency) newValue);
          return;
        case 1905311443:  // dayCount
          ((LegacyCDSSecurity) bean).setDayCount((DayCount) newValue);
          return;
        case -1002835891:  // businessDayConvention
          ((LegacyCDSSecurity) bean).setBusinessDayConvention((BusinessDayConvention) newValue);
          return;
        case -1233097483:  // calendars
          ((LegacyCDSSecurity) bean).setCalendars((Set<ExternalId>) newValue);
          return;
        case -1774904020:  // restructuringClause
          ((LegacyCDSSecurity) bean).setRestructuringClause((RestructuringClause) newValue);
          return;
        case -638821960:  // upfrontPayment
          ((LegacyCDSSecurity) bean).setUpfrontPayment((InterestRateNotional) newValue);
          return;
        case 1215227293:  // feeSettlementDate
          ((LegacyCDSSecurity) bean).setFeeSettlementDate((LocalDate) newValue);
          return;
        case -1719383937:  // accruedOnDefault
          ((LegacyCDSSecurity) bean).setAccruedOnDefault((Boolean) newValue);
          return;
        case 456333097:  // fixedRecovery
          ((LegacyCDSSecurity) bean).setFixedRecovery((Double) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((LegacyCDSSecurity) bean)._tradeDate, "tradeDate");
      JodaBeanUtils.notNull(((LegacyCDSSecurity) bean)._startDate, "startDate");
      JodaBeanUtils.notNull(((LegacyCDSSecurity) bean)._maturityDate, "maturityDate");
      JodaBeanUtils.notNull(((LegacyCDSSecurity) bean)._referenceEntity, "referenceEntity");
      JodaBeanUtils.notNull(((LegacyCDSSecurity) bean)._buyProtection, "buyProtection");
      JodaBeanUtils.notNull(((LegacyCDSSecurity) bean)._notional, "notional");
      JodaBeanUtils.notNull(((LegacyCDSSecurity) bean)._seniority, "seniority");
      JodaBeanUtils.notNull(((LegacyCDSSecurity) bean)._coupon, "coupon");
      JodaBeanUtils.notNull(((LegacyCDSSecurity) bean)._couponFrequency, "couponFrequency");
      JodaBeanUtils.notNull(((LegacyCDSSecurity) bean)._dayCount, "dayCount");
      JodaBeanUtils.notNull(((LegacyCDSSecurity) bean)._businessDayConvention, "businessDayConvention");
      JodaBeanUtils.notNull(((LegacyCDSSecurity) bean)._calendars, "calendars");
      JodaBeanUtils.notNull(((LegacyCDSSecurity) bean)._restructuringClause, "restructuringClause");
      JodaBeanUtils.notNull(((LegacyCDSSecurity) bean)._upfrontPayment, "upfrontPayment");
      JodaBeanUtils.notNull(((LegacyCDSSecurity) bean)._feeSettlementDate, "feeSettlementDate");
      JodaBeanUtils.notNull(((LegacyCDSSecurity) bean)._accruedOnDefault, "accruedOnDefault");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
