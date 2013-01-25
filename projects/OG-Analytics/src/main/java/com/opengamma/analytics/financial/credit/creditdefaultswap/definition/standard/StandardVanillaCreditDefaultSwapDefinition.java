/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.StandardCDSCoupon;
import com.opengamma.analytics.financial.credit.obligor.definition.Obligor;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.money.Currency;

/**
 * Definition of a Standard CDS i.e. with the features of CDS contracts post the Big Bang in 2009
 */
public class StandardVanillaCreditDefaultSwapDefinition extends StandardCreditDefaultSwapDefinition {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Need to add the test file for this object
  // TODO : Need to add the hashCode and equals methods

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Member variables specific to the standard CDS contract

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Ctor for the Standard CDS contract

  public StandardVanillaCreditDefaultSwapDefinition(
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

    // Call the ctor for the StandardCreditDefaultSwapDefinition superclass (corresponding to the CDS characteristics common to all types of CDS)

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
        protectionStart,
        quotedSpread,
        premiumLegCoupon,
        upfrontAmount,
        cashSettlementDate,
        adjustCashSettlementDate);

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
