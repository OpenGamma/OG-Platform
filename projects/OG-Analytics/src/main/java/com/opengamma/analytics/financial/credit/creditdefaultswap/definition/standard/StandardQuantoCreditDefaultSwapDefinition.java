/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.StandardCDSCoupon;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.obligormodel.definition.Obligor;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Definition of a Standard quanto CDS i.e. with the features of CDS contracts post the Big Bang in 2009 - WIP
 */
public class StandardQuantoCreditDefaultSwapDefinition extends CreditDefaultSwapDefinition {

  //----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Need to add the test file for this object
  // TODO : Check hashCode (and need to fix this) and equals methods
  // TODO : Need to add the member variables specific to this contract

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Member variables specific to the standard Quanto CDS contract

  // The quoted market observed spread (differs from the contract standard spread)
  private final double _quotedSpread;

  // The standard coupon to apply to the premium leg (chosen from an enumerated list e.g. 100 or 500bps)
  private final StandardCDSCoupon _premiumLegCoupon;

  // The upfront amount to exchange at contract inception (can be positive or negative)
  private final double _upfrontAmount;

  // The number of business days after trade date for the exchange of the upfront amount
  private final ZonedDateTime _cashSettlementDate;

  // Flag to determine if we business day adjust the cash settlement date (not a feature of standard CDS, but included to give user flexibility)
  private final boolean _adjustCashSettlementDate;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Ctor for the Standard Quanto CDS contract

  public StandardQuantoCreditDefaultSwapDefinition(
      final BuySellProtection buySellProtection,
      final Obligor protectionBuyer,
      final Obligor protectionSeller,
      final Obligor referenceEntity,
      final Currency currency,
      final DebtSeniority debtSeniority,
      final RestructuringClause restructuringClause,
      final Calendar calendar,
      final ZonedDateTime startDate,
      final ZonedDateTime effectiveDate,
      final ZonedDateTime maturityDate,
      final StubType stubType,
      final PeriodFrequency couponFrequency,
      final DayCount daycountFractionConvention,
      final BusinessDayConvention businessdayAdjustmentConvention,
      final boolean immAdjustMaturityDate,
      final boolean adjustEffectiveDate,
      final boolean adjustMaturityDate,
      final double notional,
      final double recoveryRate,
      final boolean includeAccruedPremium,
      final boolean protectionStart,
      final double quotedSpread,
      final StandardCDSCoupon premiumLegCoupon,
      final double upfrontAmount,
      final ZonedDateTime cashSettlementDate,
      final boolean adjustCashSettlementDate) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

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

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check the validity of the input quoted spread
    ArgumentChecker.notNegative(quotedSpread, "Quoted spread");

    // Check that the (standard) premium leg coupon is not null
    ArgumentChecker.notNull(premiumLegCoupon, "Standard coupon");

    // Check the validity of the input cash settlement date
    ArgumentChecker.notNull(cashSettlementDate, "Cash settlement date");
    ArgumentChecker.isTrue(!startDate.isAfter(cashSettlementDate), "Start date {} must be on or before cash settlement date {}", startDate, cashSettlementDate);
    ArgumentChecker.isTrue(!effectiveDate.isAfter(cashSettlementDate), "Effective date {} must be on or before cash settlement date {}", effectiveDate, cashSettlementDate);
    ArgumentChecker.isTrue(!cashSettlementDate.isAfter(maturityDate), "Cash settlement date {} must be on or before maturity date {}", cashSettlementDate, maturityDate);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Assign the member variables for the features specific to a standard Quanto CDS

    _quotedSpread = quotedSpread;
    _premiumLegCoupon = premiumLegCoupon;
    _upfrontAmount = upfrontAmount;

    _cashSettlementDate = cashSettlementDate;
    _adjustCashSettlementDate = adjustCashSettlementDate;

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public double getQuotedSpread() {
    return _quotedSpread;
  }

  public StandardCDSCoupon getPremiumLegCoupon() {
    return _premiumLegCoupon;
  }

  public double getUpfrontAmount() {
    return _upfrontAmount;
  }

  public ZonedDateTime getCashSettlementDate() {
    return _cashSettlementDate;
  }

  public boolean getAdjustCashSettlementDate() {
    return _adjustCashSettlementDate;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Builder method to allow the maturity of a standard Quanto CDS object to be modified (used during calibration of the survival curve)

  public StandardQuantoCreditDefaultSwapDefinition withMaturity(final ZonedDateTime maturityDate) {

    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.isTrue(!getEffectiveDate().isAfter(maturityDate), "Effective date {} must be on or before maturity date {} (calibration error)", getEffectiveDate(), maturityDate);

    final StandardQuantoCreditDefaultSwapDefinition modifiedCDS = new StandardQuantoCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(),
        getProtectionSeller(), getReferenceEntity(), getCurrency(), getDebtSeniority(), getRestructuringClause(), getCalendar(), getStartDate(), getEffectiveDate(),
        maturityDate, getStubType(), getCouponFrequency(), getDayCountFractionConvention(), getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(),
        getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(), getRecoveryRate(), getIncludeAccruedPremium(), getProtectionStart(),
        _quotedSpread, _premiumLegCoupon, _upfrontAmount, _cashSettlementDate, _adjustCashSettlementDate);

    return modifiedCDS;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Builder method to allow the premium leg coupon of a standard Quanto CDS object to be modified (used during calibration of the survival curve)

  public StandardQuantoCreditDefaultSwapDefinition withSpread(final double quotedSpread) {

    final StandardQuantoCreditDefaultSwapDefinition modifiedCDS = new StandardQuantoCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(),
        getReferenceEntity(), getCurrency(), getDebtSeniority(), getRestructuringClause(), getCalendar(), getStartDate(), getEffectiveDate(), getMaturityDate(), getStubType(), getCouponFrequency(),
        getDayCountFractionConvention(), getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(),
        getRecoveryRate(), getIncludeAccruedPremium(), getProtectionStart(), quotedSpread, _premiumLegCoupon, _upfrontAmount, _cashSettlementDate, _adjustCashSettlementDate);

    return modifiedCDS;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Builder method to allow the recovery rate of a standard Quanto CDS object to be modified (used during calibration of the survival curve)

  public StandardQuantoCreditDefaultSwapDefinition withRecoveryRate(final double recoveryRate) {

    final StandardQuantoCreditDefaultSwapDefinition modifiedCDS = new StandardQuantoCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(),
        getReferenceEntity(), getCurrency(), getDebtSeniority(), getRestructuringClause(), getCalendar(), getStartDate(), getEffectiveDate(), getMaturityDate(), getStubType(), getCouponFrequency(),
        getDayCountFractionConvention(), getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(),
        recoveryRate, getIncludeAccruedPremium(), getProtectionStart(), _quotedSpread, _premiumLegCoupon, _upfrontAmount, _cashSettlementDate, _adjustCashSettlementDate);

    return modifiedCDS;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    //result = prime * result + _cashSettlementDate;
    result = prime * result + _premiumLegCoupon.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_quotedSpread);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_upfrontAmount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Override
  public boolean equals(final Object obj) {

    if (this == obj) {
      return true;
    }

    if (!super.equals(obj)) {
      return false;
    }

    if (!(obj instanceof StandardCreditDefaultSwapDefinition)) {
      return false;
    }

    final StandardQuantoCreditDefaultSwapDefinition other = (StandardQuantoCreditDefaultSwapDefinition) obj;

    if (_cashSettlementDate != other._cashSettlementDate) {
      return false;
    }

    if (_premiumLegCoupon != other._premiumLegCoupon) {
      return false;
    }

    if (Double.compare(_quotedSpread, other._quotedSpread) != 0) {
      return false;
    }

    if (Double.compare(_upfrontAmount, other._upfrontAmount) != 0) {
      return false;
    }

    return true;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
