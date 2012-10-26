/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.obligormodel.definition.Obligor;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.money.Currency;

/**
 * Definition of a Standard fixed recovery CDS i.e. with the features of CDS contracts post the Big Bang in 2009 - WIP
 */
public class StandardFixedRecoveryCreditDefaultSwapDefinition extends CreditDefaultSwapDefinition {

  // -----------------------------------------------------------------------------------------------

  // TODO : Replace the int _cashSettlementDate with a ZoneddateTime
  // TODO : Add hashCode and equals methods

  // -----------------------------------------------------------------------------------------------

  // Member variables specific to the standard CDS contract 

  private final double _quotedSpread;

  private final double _upfrontAmount;

  // -----------------------------------------------------------------------------------------------

  // Ctor for the Standard CDS contract

  public StandardFixedRecoveryCreditDefaultSwapDefinition(
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
      PriceType priceType,
      boolean protectionStart,
      double quotedSpread,
      double upfrontAmount) {

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
        priceType,
        protectionStart);

    // -----------------------------------------------------------------------------------------------

    // Assign the member variables for the features specific to a standard CDS

    _quotedSpread = quotedSpread;

    _upfrontAmount = upfrontAmount;

    // -----------------------------------------------------------------------------------------------
  }

  // -----------------------------------------------------------------------------------------------

  public double getQuotedSpread() {
    return _quotedSpread;
  }

  public double getUpfrontAmount() {
    return _upfrontAmount;
  }

  // -----------------------------------------------------------------------------------------------
}
