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
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class LegacyForwardStartingCreditDefaultSwapDefinition extends CreditDefaultSwapDefinition {

  // -----------------------------------------------------------------------------------------------

  // TODO : Add hashCode and equals methods
  // TODO : Add builder methods

  // -----------------------------------------------------------------------------------------------

  // Member variables specific to the legacy forward starting CDS contract 

  // The date in the future at which the CDS is to start
  private final ZonedDateTime _forwardStartDate;

  // The par spread is the coupon rate to apply to the premium leg to give a PV of zero
  private final double _parSpread;

  // -----------------------------------------------------------------------------------------------

  // Ctor for the Legacy forward starting CDS

  public LegacyForwardStartingCreditDefaultSwapDefinition(
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
      ZonedDateTime forwardStartDate,
      double parSpread) {

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

    // Check the validity of the input par spread
    ArgumentChecker.notNegative(parSpread, "Par spread");

    // Assign the member variables for the features specific to a legacy CDS

    _forwardStartDate = forwardStartDate;

    _parSpread = parSpread;

    // -----------------------------------------------------------------------------------------------
  }

  // -----------------------------------------------------------------------------------------------

  public ZonedDateTime getForwardStartDate() {
    return _forwardStartDate;
  }

  public double getParSpread() {
    return _parSpread;
  }

  //-----------------------------------------------------------------------------------------------
}
