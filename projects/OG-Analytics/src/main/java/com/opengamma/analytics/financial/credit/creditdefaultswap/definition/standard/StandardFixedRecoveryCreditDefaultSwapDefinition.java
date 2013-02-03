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
 * Definition of a Standard fixed recovery CDS i.e. with the features of CDS contracts post the Big Bang in 2009 - WIP
 */
public class StandardFixedRecoveryCreditDefaultSwapDefinition extends StandardCreditDefaultSwapDefinition {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Need to add the test file for this object
  // TODO : Check hashCode (and need to fix this) and equals methods
  // TODO : Need to add the member variables specific to this contract

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Member variables specific to the standard fixed recovery CDS contract

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Ctor for the Standard fixed recovery CDS contract

  public StandardFixedRecoveryCreditDefaultSwapDefinition(
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
        protectionStart,
        quotedSpread,
        premiumLegCoupon,
        upfrontAmount,
        cashSettlementDate,
        adjustCashSettlementDate);

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  /*
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
  */

  // ----------------------------------------------------------------------------------------------------------------------------------------

  /*
  @Override
  public boolean equals(final Object obj) {

    if (this == obj) {
      return true;
    }

    if (!super.equals(obj)) {
      return false;
    }

    if (!(obj instanceof StandardVanillaCreditDefaultSwapDefinition)) {
      return false;
    }

    final StandardFixedRecoveryCreditDefaultSwapDefinition other = (StandardFixedRecoveryCreditDefaultSwapDefinition) obj;

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
  */

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
