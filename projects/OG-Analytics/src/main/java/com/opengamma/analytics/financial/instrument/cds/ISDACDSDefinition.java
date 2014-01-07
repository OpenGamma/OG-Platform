/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.cds;

import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjuster;

import com.opengamma.analytics.financial.credit.cds.ISDACDSDerivative;
import com.opengamma.analytics.financial.instrument.Convention;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.daycount.AccruedInterestCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.util.ArgumentChecker;

/**
 * ISDA definition for CDS securities
 *
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 *
 * @see ISDACDSDerivative
 * @see InstrumentDefinition
 */
public class ISDACDSDefinition implements InstrumentDefinition<ISDACDSDerivative> {
  /** The day count used by ISDA */
  private static final DayCount ACT_365F = DayCounts.ACT_365;

  /** The start date of the CDS */
  private final ZonedDateTime _startDate;
  /** The maturity of the CDS */
  private final ZonedDateTime _maturity;

  /** The premium */
  private final ISDACDSPremiumDefinition _premium;

  /** The notional */
  private final double _notional;
  /** The spread */
  private final double _spread;
  /** The recovery rate */
  private final double _recoveryRate;

  /** Should accrued interest be paid in the event of a default */
  private final boolean _accrualOnDefault;
  /** Is the protection payment made on default or at maturity */
  private final boolean _payOnDefault;
  /** Is the start date protected */
  private final boolean _protectStart;

  /** The coupon frequency */
  private final Frequency _couponFrequency;
  /** The convention */
  private final Convention _convention;
  /** The stub type */
  private final StubType _stubType;

  /**
   * Create an (immutable) CDS definition
   * @param startDate Protection start date of the CDS contract (may be in the past, not null)
   * @param maturity Maturity date of the CDS contract (not null)
   * @param premium Definition for the premium payments (not null)
   * @param notional Notional of the CDS contract
   * @param spread Spread (a.k.a. coupon rate) of the CDS contract
   * @param recoveryRate Recovery rate against the underlying
   * @param accrualOnDefault Whether, in the event of default, accrued interest must be paid for the current period up to the default date
   * @param payOnDefault Whether protection payment is due on default (true) or at maturity (false)
   * @param protectStart Whether the start date is protected (i.e. one extra day of protection)
   * @param couponFrequency The premium coupon frequency
   * @param convention Convention data
   * @param stubType The premium stub type
   */
  public ISDACDSDefinition(final ZonedDateTime startDate, final ZonedDateTime maturity, final ISDACDSPremiumDefinition premium,
      final double notional, final double spread, final double recoveryRate,
      final boolean accrualOnDefault, final boolean payOnDefault, final boolean protectStart,
      final Frequency couponFrequency, final Convention convention, final StubType stubType) {

    ArgumentChecker.notNull(startDate, "start date");
    ArgumentChecker.notNull(maturity, "maturity");
    ArgumentChecker.notNull(premium, "premium");
    ArgumentChecker.notNull(convention, "convention");

    _startDate = startDate;
    _maturity = maturity;
    _premium = premium;
    _notional = notional;
    _spread = spread;
    _recoveryRate = recoveryRate;
    _accrualOnDefault = accrualOnDefault;
    _payOnDefault = payOnDefault;
    _protectStart = protectStart;
    _couponFrequency = couponFrequency;
    _convention = convention;
    _stubType = stubType;
  }

  /**
   * Create a {@link ISDACDSDerivative} object for pricing relative to the given pricing date
   *
   * @param pricingDate Pricing point for offsetting t values
   * @param yieldCurveNames Curve names: 0 = discount, 1 = credit spread (optional)
   * @return CDS derivative object ready for pricing
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public ISDACDSDerivative toDerivative(final ZonedDateTime pricingDate, final String... yieldCurveNames) {

    final ZonedDateTime stepinDate = pricingDate.isAfter(_startDate) ? pricingDate.plusDays(1) : _startDate;
    final ZonedDateTime settlementDate = findSettlementDate(pricingDate, _convention);

    return toDerivative(pricingDate, stepinDate, settlementDate, yieldCurveNames);
  }

  /**
   * @param pricingDate The pricing date
   * @param stepinDate The step-in date
   * @param settlementDate The settlement date
   * @param yieldCurveNames The yield curve names, not null
   * @return The derivative form of a CDS
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  public ISDACDSDerivative toDerivative(final ZonedDateTime pricingDate, final ZonedDateTime stepinDate, final ZonedDateTime settlementDate, final String... yieldCurveNames) {

    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.isTrue(yieldCurveNames.length >= 1, "At least one curve required (discount, credit spread is optional)");

    final String discountCurveName = yieldCurveNames[0];
    final String spreadCurveName =  yieldCurveNames.length > 1 ? yieldCurveNames[1] : null;

    return new ISDACDSDerivative(
        discountCurveName, spreadCurveName,
        _premium.toDerivative(pricingDate, discountCurveName),
        getTimeBetween(pricingDate, _startDate),
        getTimeBetween(pricingDate, _maturity),
        getTimeBetween(pricingDate, stepinDate),
        getTimeBetween(pricingDate, settlementDate),
        _notional, _spread, _recoveryRate, accruedInterest(stepinDate),
        _accrualOnDefault, _payOnDefault, _protectStart,
        _couponFrequency, _convention, _stubType);
  }

  /**
   * Create a {@link ISDACDSDerivative} object for pricing relative to the given pricing date
   *
   * @param pricingDate Pricing point for offsetting t values
   * @return CDS derivative object ready for pricing
   */
  @Override
  public ISDACDSDerivative toDerivative(final ZonedDateTime pricingDate) {

    final ZonedDateTime stepinDate = pricingDate.isAfter(_startDate) ? pricingDate.plusDays(1) : _startDate;
    final ZonedDateTime settlementDate = findSettlementDate(pricingDate, _convention);

    return toDerivative(pricingDate, stepinDate, settlementDate);
  }

  /**
   * @param pricingDate The pricing date
   * @param stepinDate The step-in date
   * @param settlementDate The settlement date
   * @return The derivative form of a CDS
   */
  public ISDACDSDerivative toDerivative(final ZonedDateTime pricingDate, final ZonedDateTime stepinDate, final ZonedDateTime settlementDate) {

    return new ISDACDSDerivative(
        _premium.toDerivative(pricingDate),
        getTimeBetween(pricingDate, _startDate),
        getTimeBetween(pricingDate, _maturity),
        getTimeBetween(pricingDate, stepinDate),
        getTimeBetween(pricingDate, settlementDate),
        _notional, _spread, _recoveryRate, accruedInterest(stepinDate),
        _accrualOnDefault, _payOnDefault, _protectStart,
        _couponFrequency, _convention, _stubType);
  }

  private ZonedDateTime findSettlementDate(final ZonedDateTime startDate, final Convention convention) {

    final TemporalAdjuster adjuster = convention.getBusinessDayConvention().getTemporalAdjuster(convention.getWorkingDayCalendar());

    ZonedDateTime result = startDate;

    for (int i = 0, n = convention.getSettlementDays(); i < n; ++i) {
      result = result.plusDays(1).with(adjuster);
    }

    return result;
  }

  private static double getTimeBetween(final ZonedDateTime date1, final ZonedDateTime date2) {

    final ZonedDateTime rebasedDate2 = date2.withZoneSameInstant(date1.getZone());

    return rebasedDate2.isBefore(date1)
        ? -ACT_365F.getDayCountFraction(rebasedDate2, date1)
            :  ACT_365F.getDayCountFraction(date1, rebasedDate2);
  }

  /**
   * @param stepinDate The step-in date
   * @return The accrued interest at this date
   */
  public double accruedInterest(final ZonedDateTime stepinDate) {

    final int nCoupons = _premium.getNumberOfPayments();
    int couponIndex = 0;
    boolean found = false;

    for (int i = nCoupons - 1; i >= 0; --i) {
      if (!_premium.getNthPayment(i).getAccrualStartDate().isAfter(stepinDate)) {
        couponIndex = i;
        found = true;
        break;
      }
    }

    if (!found) {
      return 0.0;
    }

    final CouponFixedDefinition currentPeriod = _premium.getNthPayment(couponIndex);
    final ZonedDateTime previousAccrualDate = currentPeriod.getAccrualStartDate();
    final ZonedDateTime nextAccrualDate = currentPeriod.getAccrualEndDate();

    return currentPeriod.getNotional() * AccruedInterestCalculator.getAccruedInterest(
        _convention.getDayCount(), couponIndex, nCoupons, previousAccrualDate, stepinDate, nextAccrualDate,
        currentPeriod.getRate(), Math.round(1.0 / currentPeriod.getPaymentYearFraction()), /* isEOM */ false);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCDSDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCDSDefinition(this);
  }

  /**
   * Gets the start date.
   * @return The start date
   */
  public ZonedDateTime getStartDate() {
    return _startDate;
  }

  /**
   * Gets the maturity date.
   * @return The maturity date
   */
  public ZonedDateTime getMaturity() {
    return _maturity;
  }

  /**
   * Gets the premium.
   * @return The premium
   */
  public ISDACDSPremiumDefinition getPremium() {
    return _premium;
  }

  /**
   * Gets the notional.
   * @return The notional
   */
  public double getNotional() {
    return _notional;
  }

  /**
   * Gets the spread.
   * @return The spread
   */
  public double getSpread() {
    return _spread;
  }

  /**
   * Gets the recovery rate.
   * @return The recovery rate
   */
  public double getRecoveryRate() {
    return _recoveryRate;
  }

  /**
   * Is accrued interest paid on default.
   * @return true if the accrued interest should be paid on default
   */
  public boolean isAccrualOnDefault() {
    return _accrualOnDefault;
  }

  /**
   * Is the payment made on the default or at maturity.
   * @return true if the payment is made on the default date
   */
  public boolean isPayOnDefault() {
    return _payOnDefault;
  }

  /**
   * Is the start date protected.
   * @return true if the start date is protected.
   */
  public boolean isProtectStart() {
    return _protectStart;
  }

  /**
   * Gets the coupon frequency.
   * @return The coupon frequency
   */
  public Frequency getCouponFrequency() {
    return _couponFrequency;
  }

  /**
   * Gets the convention
   * @return The convention
   */
  public Convention getConvention() {
    return _convention;
  }

  /**
   * Gets the stub type
   * @return The stub type
   */
  public StubType getStubType() {
    return _stubType;
  }

}
