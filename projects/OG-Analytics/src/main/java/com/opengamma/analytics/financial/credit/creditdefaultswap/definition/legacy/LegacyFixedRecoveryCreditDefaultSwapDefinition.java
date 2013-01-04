/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.obligor.definition.Obligor;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.money.Currency;

/**
 * Definition of a Legacy fixed recovery CDS i.e. with the features of CDS contracts prior to the Big Bang in 2009 - WIP
 */
public class LegacyFixedRecoveryCreditDefaultSwapDefinition extends LegacyCreditDefaultSwapDefinition {

  //----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Check hashCode and equals methods (fix these)
  // TODO : Remove the builder method code

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Member variables specific to the legacy fixed recovery CDS contract

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Ctor for the Legacy fixed recovery CDS

  public LegacyFixedRecoveryCreditDefaultSwapDefinition(
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
      final double parSpread) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Call the ctor for the LegacyCreditDefaultSwapDefinition superclass (corresponding to the CDS characteristics common to all types of CDS)

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
        parSpread);

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  /*
  // Builder method to allow the maturity of a Legacy CDS object to be modified (used during calibration of the hazard rate curve)

  public LegacyVanillaCreditDefaultSwapDefinition withMaturityDate(final ZonedDateTime maturityDate) {

    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.isTrue(!getEffectiveDate().isAfter(maturityDate), "Effective date {} must be on or before maturity date {} (calibration error)", getEffectiveDate(), maturityDate);

    final LegacyVanillaCreditDefaultSwapDefinition modifiedCDS = new LegacyVanillaCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(),
        getReferenceEntity(), getCurrency(), getDebtSeniority(), getRestructuringClause(), getCalendar(), getStartDate(), getEffectiveDate(), maturityDate, getStubType(), getCouponFrequency(),
        getDayCountFractionConvention(), getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(),
        getRecoveryRate(), getIncludeAccruedPremium(), getProtectionStart(), _parSpread);

    return modifiedCDS;
  }
  */

  // ----------------------------------------------------------------------------------------------------------------------------------------

  /*
  // Builder method to allow the premium leg coupon of a Legacy CDS object to be modified (used during calibration of the hazard rate curve)

  public LegacyVanillaCreditDefaultSwapDefinition withSpread(final double parSpread) {

    final LegacyVanillaCreditDefaultSwapDefinition modifiedCDS = new LegacyVanillaCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(),
        getReferenceEntity(), getCurrency(), getDebtSeniority(), getRestructuringClause(), getCalendar(), getStartDate(), getEffectiveDate(), getMaturityDate(),
        getStubType(), getCouponFrequency(), getDayCountFractionConvention(), getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(),
        getAdjustMaturityDate(), getNotional(), getRecoveryRate(), getIncludeAccruedPremium(), getProtectionStart(), parSpread);

    return modifiedCDS;
  }
  */

  // ----------------------------------------------------------------------------------------------------------------------------------------

  /*
  // Builder method to allow the recovery rate of a Legacy CDS object to be modified (used during calibration of the hazard rate curve)

  public LegacyVanillaCreditDefaultSwapDefinition withRecoveryRate(final double recoveryRate) {

    final LegacyVanillaCreditDefaultSwapDefinition modifiedCDS = new LegacyVanillaCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(),
        getReferenceEntity(), getCurrency(), getDebtSeniority(), getRestructuringClause(), getCalendar(), getStartDate(), getEffectiveDate(), getMaturityDate(), getStubType(), getCouponFrequency(),
        getDayCountFractionConvention(), getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(),
        recoveryRate, getIncludeAccruedPremium(), getProtectionStart(), _parSpread);

    return modifiedCDS;
  }
  */

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = 0; //Double.doubleToLongBits(_parSpread);
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
    if (!(obj instanceof LegacyFixedRecoveryCreditDefaultSwapDefinition)) {
      return false;
    }

    return true;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
