/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.obligormodel.definition.Obligor;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Definition of a Legacy forward starting CDS i.e. with the features of CDS contracts prior to the Big Bang in 2009 - WIP 
 */
public class LegacyForwardStartingCreditDefaultSwapDefinition extends CreditDefaultSwapDefinition {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Check hashCode and equals methods
  // TODO : Need to add the test file for this object

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Member variables specific to the legacy forward starting CDS contract

  // The date in the future at which the CDS is to start
  private final ZonedDateTime _forwardStartDate;

  // The par spread is the coupon rate to apply to the premium leg to give a PV of zero
  private final double _parSpread;

  // ----------------------------------------------------------------------------------------------------------------------------------------

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

    // Check the validity of the input forward start date
    ArgumentChecker.notNull(forwardStartDate, "Forward start date");
    ArgumentChecker.isTrue(!startDate.isAfter(forwardStartDate), "Start date {} must be on or before forward start date {}", startDate, forwardStartDate);
    ArgumentChecker.isTrue(!effectiveDate.isAfter(forwardStartDate), "Effective date {} must be on or before forward start date {}", effectiveDate, forwardStartDate);
    ArgumentChecker.isTrue(!forwardStartDate.isAfter(maturityDate), "Forward start date {} must be on or before maturity date {}", forwardStartDate, maturityDate);

    // Check the validity of the input par spread
    ArgumentChecker.notNegative(parSpread, "Par spread");

    // Assign the member variables for the features specific to a legacy CDS
    _forwardStartDate = forwardStartDate;
    _parSpread = parSpread;

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public ZonedDateTime getForwardStartDate() {
    return _forwardStartDate;
  }

  public double getParSpread() {
    return _parSpread;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Builder method to allow the maturity of a Legacy CDS object to be modified (used during calibration of the hazard rate curve)

  public LegacyForwardStartingCreditDefaultSwapDefinition withMaturityDate(final ZonedDateTime maturityDate) {

    ArgumentChecker.notNull(maturityDate, "maturity date");
    ArgumentChecker.isTrue(!getEffectiveDate().isAfter(maturityDate), "Effective date {} must be on or before maturity date {} (calibration error)", getEffectiveDate(), maturityDate);

    final LegacyForwardStartingCreditDefaultSwapDefinition modifiedCDS = new LegacyForwardStartingCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(),
        getReferenceEntity(), getCurrency(), getDebtSeniority(), getRestructuringClause(), getCalendar(), getStartDate(), getEffectiveDate(), maturityDate, getStubType(), getCouponFrequency(),
        getDayCountFractionConvention(), getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(),
        getRecoveryRate(), getIncludeAccruedPremium(), getProtectionStart(), _forwardStartDate, _parSpread);

    return modifiedCDS;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Builder method to allow the premium leg coupon of a Legacy CDS object to be modified (used during calibration of the hazard rate curve)

  public LegacyForwardStartingCreditDefaultSwapDefinition withSpread(final double parSpread) {

    final LegacyForwardStartingCreditDefaultSwapDefinition modifiedCDS = new LegacyForwardStartingCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(),
        getReferenceEntity(), getCurrency(), getDebtSeniority(), getRestructuringClause(), getCalendar(), getStartDate(), getEffectiveDate(), getMaturityDate(), getStubType(), getCouponFrequency(),
        getDayCountFractionConvention(), getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(),
        getRecoveryRate(), getIncludeAccruedPremium(), getProtectionStart(), _forwardStartDate, parSpread);

    return modifiedCDS;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Builder method to allow the recovery rate of a Legacy CDS object to be modified (used during calibration of the hazard rate curve)

  public LegacyForwardStartingCreditDefaultSwapDefinition withRecoveryRate(final double recoveryRate) {

    final LegacyForwardStartingCreditDefaultSwapDefinition modifiedCDS = new LegacyForwardStartingCreditDefaultSwapDefinition(getBuySellProtection(), getProtectionBuyer(), getProtectionSeller(),
        getReferenceEntity(), getCurrency(), getDebtSeniority(), getRestructuringClause(), getCalendar(), getStartDate(), getEffectiveDate(), getMaturityDate(), getStubType(), getCouponFrequency(),
        getDayCountFractionConvention(), getBusinessDayAdjustmentConvention(), getIMMAdjustMaturityDate(), getAdjustEffectiveDate(), getAdjustMaturityDate(), getNotional(),
        recoveryRate, getIncludeAccruedPremium(), getProtectionStart(), _forwardStartDate, _parSpread);

    return modifiedCDS;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

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

  // ----------------------------------------------------------------------------------------------------------------------------------------

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

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
