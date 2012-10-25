/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.StandardCDSCoupon;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.obligormodel.definition.Obligor;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Definition of a Standard CDS i.e. with the features of CDS contracts post the Big Bang in 2009
 */
public class StandardCreditDefaultSwapDefinition extends CreditDefaultSwapDefinition {

  // -----------------------------------------------------------------------------------------------

  // TODO : Replace the int _cashSettlementDate with a ZoneddateTime

  // -----------------------------------------------------------------------------------------------

  // Member variables specific to the standard CDS contract 

  // The quoted market observed spread (differs from the contract standard spread) 
  private final double _quotedSpread;

  // The standard coupon to apply to the premium leg (e.g. 100 or 500bps)
  private final StandardCDSCoupon _premiumLegCoupon;

  // The upfront amount to exchange at contract inception
  private final double _upfrontAmount;

  // The number of business days after trade date for the exchange of upfront
  private final int _cashSettlementDate;

  // -----------------------------------------------------------------------------------------------

  // Ctor for the Standard CDS contract

  public StandardCreditDefaultSwapDefinition(
      BuySellProtection buySellProtection,
      Obligor protectionBuyer,
      Obligor protectionSeller,
      Obligor referenceEntity,
      Currency currency,
      DebtSeniority debtSeniority,
      RestructuringClause restructuringClause,
      Calendar calendar,
      ZonedDateTime startDate,
      ZonedDateTime effectiveDate,
      ZonedDateTime maturityDate,
      ZonedDateTime valuationDate,
      StubType stubType,
      PeriodFrequency couponFrequency,
      DayCount daycountFractionConvention,
      BusinessDayConvention businessdayAdjustmentConvention,
      boolean immAdjustMaturityDate,
      boolean adjustEffectiveDate,
      boolean adjustMaturityDate,
      double notional,
      double recoveryRate,
      boolean includeAccruedPremium,
      boolean protectionStart,
      double quotedSpread,
      StandardCDSCoupon premiumLegCoupon,
      double upfrontAmount,
      int cashSettlementDate) {

    // -----------------------------------------------------------------------------------------------

    // Call the ctor for the superclass (corresponding to the CDS characteristics common to all types of CDS)

    super(buySellProtection,
        protectionBuyer,
        protectionSeller,
        referenceEntity,
        currency,
        debtSeniority,
        restructuringClause,
        calendar,
        startDate,
        effectiveDate,
        maturityDate,
        valuationDate,
        stubType,
        couponFrequency,
        daycountFractionConvention,
        businessdayAdjustmentConvention,
        immAdjustMaturityDate,
        adjustEffectiveDate,
        adjustMaturityDate,
        notional,
        recoveryRate,
        includeAccruedPremium,
        protectionStart);

    // -----------------------------------------------------------------------------------------------

    // Check the validity of the input quoted spread
    ArgumentChecker.notNegative(quotedSpread, "Quoted spread");

    // Check that the (standard) premium leg coupon is not null
    ArgumentChecker.notNull(premiumLegCoupon, "Standard coupon");

    // Check that the cash settlement date is not in the past
    ArgumentChecker.notNegative(cashSettlementDate, "Cash settlement date");

    // -----------------------------------------------------------------------------------------------

    // Assign the member variables for the features specific to a standard CDS

    _quotedSpread = quotedSpread;

    _premiumLegCoupon = premiumLegCoupon;

    _upfrontAmount = upfrontAmount;

    _cashSettlementDate = cashSettlementDate;

    // -----------------------------------------------------------------------------------------------
  }

  // -----------------------------------------------------------------------------------------------

  public double getQuotedSpread() {
    return _quotedSpread;
  }

  public StandardCDSCoupon getPremiumLegCoupon() {
    return _premiumLegCoupon;
  }

  public double getUpfrontAmount() {
    return _upfrontAmount;
  }

  public int getCashSettlementDate() {
    return _cashSettlementDate;
  }

  // -----------------------------------------------------------------------------------------------

  //Builder method to allow the maturity of a Legacy CDS object to be modified (used during calibration of the survival curve)

  public StandardCreditDefaultSwapDefinition withMaturity(ZonedDateTime maturityDate) {

    StandardCreditDefaultSwapDefinition modifiedCDS = new StandardCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(), getReferenceEntity(), getCurrency(),
        getDebtSeniority(), getRestructuringClause(), getCalendar(), getStartDate(), getEffectiveDate(), maturityDate, getValuationDate(), getStubType(), getCouponFrequency(),
        getDayCountFractionConvention(), getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(),
        getRecoveryRate(), getIncludeAccruedPremium(), getProtectionStart(), _quotedSpread, _premiumLegCoupon, _upfrontAmount, _cashSettlementDate);

    return modifiedCDS;
  }

  // -----------------------------------------------------------------------------------------------

  // Builder method to allow the premium leg coupon of a Legacy CDS object to be modified (used during calibration of the survival curve)

  public StandardCreditDefaultSwapDefinition withSpread(double quotedSpread) {

    StandardCreditDefaultSwapDefinition modifiedCDS = new StandardCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(), getReferenceEntity(), getCurrency(),
        getDebtSeniority(), getRestructuringClause(), getCalendar(), getStartDate(), getEffectiveDate(), getMaturityDate(), getValuationDate(), getStubType(), getCouponFrequency(),
        getDayCountFractionConvention(), getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(),
        getRecoveryRate(), getIncludeAccruedPremium(), getProtectionStart(), quotedSpread, _premiumLegCoupon, _upfrontAmount, _cashSettlementDate);

    return modifiedCDS;
  }

  // -----------------------------------------------------------------------------------------------

  // Builder method to allow the recovery rate of a Legacy CDS object to be modified (used during calibration of the survival curve)

  public StandardCreditDefaultSwapDefinition withRecoveryRate(double recoveryRate) {

    StandardCreditDefaultSwapDefinition modifiedCDS = new StandardCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(), getReferenceEntity(), getCurrency(),
        getDebtSeniority(), getRestructuringClause(), getCalendar(), getStartDate(), getEffectiveDate(), getMaturityDate(), getValuationDate(), getStubType(), getCouponFrequency(),
        getDayCountFractionConvention(), getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(),
        recoveryRate, getIncludeAccruedPremium(), getProtectionStart(), _quotedSpread, _premiumLegCoupon, _upfrontAmount, _cashSettlementDate);

    return modifiedCDS;
  }

  // -----------------------------------------------------------------------------------------------

  // Builder method to allow the valuationDate of a Legacy CDS object to be modified (used during testing and in simulation models)

  public StandardCreditDefaultSwapDefinition withValuationDate(ZonedDateTime valuationDate) {

    StandardCreditDefaultSwapDefinition modifiedCDS = new StandardCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(), getReferenceEntity(), getCurrency(),
        getDebtSeniority(), getRestructuringClause(), getCalendar(), getStartDate(), getEffectiveDate(), getMaturityDate(), valuationDate, getStubType(), getCouponFrequency(),
        getDayCountFractionConvention(), getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(),
        getRecoveryRate(), getIncludeAccruedPremium(), getProtectionStart(), _quotedSpread, _premiumLegCoupon, _upfrontAmount, _cashSettlementDate);

    return modifiedCDS;
  }

  // -----------------------------------------------------------------------------------------------
}
