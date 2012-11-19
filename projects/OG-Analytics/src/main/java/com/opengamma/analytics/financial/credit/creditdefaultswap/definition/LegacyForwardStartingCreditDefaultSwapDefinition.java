/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.definition;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.DebtSeniority;
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
      final ZonedDateTime forwardStartDate,
      final double parSpread) {

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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _forwardStartDate.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_parSpread);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof LegacyForwardStartingCreditDefaultSwapDefinition)) {
      return false;
    }
    final LegacyForwardStartingCreditDefaultSwapDefinition other = (LegacyForwardStartingCreditDefaultSwapDefinition) obj;
    if (Double.compare(_parSpread, other._parSpread) != 0) {
      return false;
    }
    if (!ObjectUtils.equals(_forwardStartDate, other._forwardStartDate)) {
      return false;
    }
    return true;
  }

  //-----------------------------------------------------------------------------------------------


}
