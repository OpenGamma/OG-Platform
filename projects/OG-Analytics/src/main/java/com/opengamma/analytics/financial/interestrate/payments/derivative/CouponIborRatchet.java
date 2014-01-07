/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a Ratchet on Ibor coupon. The coupon payment depends on the
 * previous coupon ($C_{i-1}$), the current Ibor fixing ($L_i$). The pay-off is:
 * $$
 * \begin{equation*}
 * \alpha^M_i C_{i-1} + \beta^M_i L_i + \gamma^M_i
 * \end{equation*}
 * $$
 * subject to the floor:
 * $$
 * \begin{equation*}
 * \alpha^F_i C_{i-1} + \beta^F_i L_i + \gamma^F_i
 * \end{equation*}
 * $$
 * and the cap:
 * $$
 * \begin{equation*}
 * \alpha^C_i C_{i-1} + \beta^C_i L_i + \gamma^C_i
 * \end{equation*}
 * $$
 */
public class CouponIborRatchet extends CouponIborSpread {

  /**
   * Ibor-like index on which the coupon fixes. The index currency should be the same as the index currency.
   */
  private final IborIndex _index;
  // TODO: move the index to CouponIbor
  /**
   * The coefficients of the main payment (before floor and cap). Array of length 3. The first coefficient is the previous coupon factor,
   * the second is the Ibor factor and the third is the additive term.
   */
  private final double[] _mainCoefficients;
  /**
   * The coefficients of the floor. Array of length 3. The first coefficient is the previous coupon factor,
   * the second is the Ibor factor and the third is the additive term.
   */
  private final double[] _floorCoefficients;
  /**
   * The coefficients of the cap. Array of length 3. The first coefficient is the previous coupon factor,
   * the second is the Ibor factor and the third is the additive term.
   */
  private final double[] _capCoefficients;

  /**
   * Constructor from all the details.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param discountingCurveName The name of the discounting curve.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param fixingTime Time (in years) up to fixing.
   * @param fixingPeriodStartTime Time (in years) up to the start of the fixing period.
   * @param fixingPeriodEndTime Time (in years) up to the end of the fixing period.
   * @param fixingYearFraction The year fraction (or accrual factor) for the fixing period.
   * @param forwardCurveName The name of the forward curve.
   * @param index The coupon Ibor index. Should of the same currency as the payment.
   * @param mainCoefficients The coefficients of the main payment (before floor and cap). Array of length 3.
   * @param floorCoefficients The coefficients of the floor. Array of length 3.
   * @param capCoefficients The coefficients of the cap. Array of length 3.
   * @deprecated Use the constructor that does not take yield curve names.
   */
  @Deprecated
  public CouponIborRatchet(final Currency currency, final double paymentTime, final String discountingCurveName, final double paymentYearFraction,
      final double notional, final double fixingTime, final double fixingPeriodStartTime,
      final double fixingPeriodEndTime, final double fixingYearFraction, final String forwardCurveName, final IborIndex index, final double[] mainCoefficients,
      final double[] floorCoefficients, final double[] capCoefficients) {
    super(currency, paymentTime, discountingCurveName, paymentYearFraction, notional, fixingTime, index, fixingPeriodStartTime, fixingPeriodEndTime, fixingYearFraction, 0.0, forwardCurveName);
    ArgumentChecker.notNull(index, "Index");
    ArgumentChecker.notNull(mainCoefficients, "Main coefficients");
    ArgumentChecker.notNull(floorCoefficients, "Floor coefficients");
    ArgumentChecker.notNull(capCoefficients, "Cap coefficients");
    ArgumentChecker.isTrue(mainCoefficients.length == 3, "Requires 3 main coefficients");
    ArgumentChecker.isTrue(floorCoefficients.length == 3, "Requires 3 floor coefficients");
    ArgumentChecker.isTrue(capCoefficients.length == 3, "Requires 3 cap coefficients");
    _index = index;
    _mainCoefficients = mainCoefficients;
    _floorCoefficients = floorCoefficients;
    _capCoefficients = capCoefficients;
  }

  /**
   * Constructor from all the details.
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param fixingTime Time (in years) up to fixing.
   * @param fixingPeriodStartTime Time (in years) up to the start of the fixing period.
   * @param fixingPeriodEndTime Time (in years) up to the end of the fixing period.
   * @param fixingYearFraction The year fraction (or accrual factor) for the fixing period.
   * @param index The coupon Ibor index. Should of the same currency as the payment.
   * @param mainCoefficients The coefficients of the main payment (before floor and cap). Array of length 3.
   * @param floorCoefficients The coefficients of the floor. Array of length 3.
   * @param capCoefficients The coefficients of the cap. Array of length 3.
   */
  public CouponIborRatchet(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final double fixingTime, final double fixingPeriodStartTime,
      final double fixingPeriodEndTime, final double fixingYearFraction, final IborIndex index, final double[] mainCoefficients, final double[] floorCoefficients, final double[] capCoefficients) {
    super(currency, paymentTime, paymentYearFraction, notional, fixingTime, index, fixingPeriodStartTime, fixingPeriodEndTime, fixingYearFraction, 0.0);
    ArgumentChecker.notNull(index, "Index");
    ArgumentChecker.notNull(mainCoefficients, "Main coefficients");
    ArgumentChecker.notNull(floorCoefficients, "Floor coefficients");
    ArgumentChecker.notNull(capCoefficients, "Cap coefficients");
    ArgumentChecker.isTrue(mainCoefficients.length == 3, "Requires 3 main coefficients");
    ArgumentChecker.isTrue(floorCoefficients.length == 3, "Requires 3 floor coefficients");
    ArgumentChecker.isTrue(capCoefficients.length == 3, "Requires 3 cap coefficients");
    _index = index;
    _mainCoefficients = mainCoefficients;
    _floorCoefficients = floorCoefficients;
    _capCoefficients = capCoefficients;
  }

  /**
   * Gets the coefficients of the main payment (before floor and cap).
   * @return The coefficients of the main payment (before floor and cap).
   */
  public double[] getMainCoefficients() {
    return _mainCoefficients;
  }

  /**
   * Gets the coefficients of the floor.
   * @return The coefficients of the floor.
   */
  public double[] getFloorCoefficients() {
    return _floorCoefficients;
  }

  /**
   * Gets the coefficients of the cap.
   * @return The coefficients of the cap.
   */
  public double[] getCapCoefficients() {
    return _capCoefficients;
  }

  @SuppressWarnings("deprecation")
  @Override
  public CouponIborRatchet withNotional(final double notional) {
    try {
      return new CouponIborRatchet(getCurrency(), getPaymentTime(), getFundingCurveName(), getPaymentYearFraction(), notional, getFixingTime(), getFixingPeriodStartTime(),
          getFixingPeriodEndTime(), getFixingAccrualFactor(), getForwardCurveName(), _index, _mainCoefficients, _floorCoefficients, _capCoefficients);
    } catch (final IllegalStateException e) {
      return new CouponIborRatchet(getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, getFixingTime(), getFixingPeriodStartTime(),
          getFixingPeriodEndTime(), getFixingAccrualFactor(), _index, _mainCoefficients, _floorCoefficients, _capCoefficients);
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_capCoefficients);
    result = prime * result + Arrays.hashCode(_floorCoefficients);
    result = prime * result + ((_index == null) ? 0 : _index.hashCode());
    result = prime * result + Arrays.hashCode(_mainCoefficients);
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
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CouponIborRatchet other = (CouponIborRatchet) obj;
    if (!Arrays.equals(_capCoefficients, other._capCoefficients)) {
      return false;
    }
    if (!Arrays.equals(_floorCoefficients, other._floorCoefficients)) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (!Arrays.equals(_mainCoefficients, other._mainCoefficients)) {
      return false;
    }
    return true;
  }

}
