/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.isdastandardmodel.StubType;
import com.opengamma.analytics.financial.credit.obligor.definition.Obligor;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Definition of a Standard CDS i.e. with the features of CDS contracts post the Big Bang in 2009
 *@deprecated this will be deleted 
 */
@Deprecated
public abstract class StandardCreditDefaultSwapDefinition extends CreditDefaultSwapDefinition {

  //----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Need to add the test file for this object
  // TODO : Check hashCode (and need to fix this) and equals methods

  // TODO : Add a flag to determine if we want to business day adjust the cash settlement date or not

  // TODO : Maybe replace the _cashSettlementDate with an integer to make this more like the MarkIt calculator

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Member variables specific to the standard CDS contract

  // The quoted market observed spread (differs from the contract standard spread)
  private final double _quotedSpread;

  // The standard coupon to apply to the premium leg (chosen from an enumerated list e.g. 100 or 500bps)
  private final double _premiumLegCoupon;

  // The upfront amount to exchange at contract inception (can be positive or negative)
  private final double _upfrontAmount;

  // The number of business days after trade date for the exchange of the upfront amount
  private final ZonedDateTime _cashSettlementDate;

  // Flag to determine if we business day adjust the cash settlement date (not a feature of standard CDS, but included to give user flexibility)
  private final boolean _adjustCashSettlementDate;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  //Ctor for the Standard CDS contract

  public StandardCreditDefaultSwapDefinition(final BuySellProtection buySellProtection, final LegalEntity protectionBuyer, final LegalEntity protectionSeller, final LegalEntity referenceEntity,
      final Currency currency, final DebtSeniority debtSeniority, final RestructuringClause restructuringClause, final Calendar calendar, final ZonedDateTime startDate,
      final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final StubType stubType, final PeriodFrequency couponFrequency, final DayCount daycountFractionConvention,
      final BusinessDayConvention businessdayAdjustmentConvention, final boolean immAdjustMaturityDate, final boolean adjustEffectiveDate, final boolean adjustMaturityDate, final double notional,
      final double recoveryRate, final boolean includeAccruedPremium, final boolean protectionStart, final double quotedSpread, final double premiumLegCoupon, final double upfrontAmount,
      final ZonedDateTime cashSettlementDate, final boolean adjustCashSettlementDate) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Call the ctor for the superclass (corresponding to the CDS characteristics common to all types of CDS)

    super(buySellProtection, protectionBuyer, protectionSeller, referenceEntity, currency, debtSeniority, restructuringClause, calendar, startDate, effectiveDate, maturityDate, stubType,
        couponFrequency, daycountFractionConvention, businessdayAdjustmentConvention, immAdjustMaturityDate, adjustEffectiveDate, adjustMaturityDate, notional, recoveryRate, includeAccruedPremium,
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

    // Assign the member variables for the features specific to a standard CDS

    _quotedSpread = quotedSpread;
    _premiumLegCoupon = premiumLegCoupon;
    _upfrontAmount = upfrontAmount;

    _cashSettlementDate = cashSettlementDate;
    _adjustCashSettlementDate = adjustCashSettlementDate;

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  public double getQuotedSpread() {
    return _quotedSpread;
  }

  public double getPremiumLegCoupon() {
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

  public abstract StandardCreditDefaultSwapDefinition withSpread(double spread);

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
