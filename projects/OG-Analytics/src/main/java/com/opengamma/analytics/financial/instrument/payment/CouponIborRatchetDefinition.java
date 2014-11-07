/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import java.util.Arrays;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.DoubleTimeSeries;
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
 * subject to the floor :
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
public class CouponIborRatchetDefinition extends CouponFloatingDefinition {

  /**
   * Ibor-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
   */
  private final IborIndex _index;
  /**
   * The start date of the fixing period.
   */
  private final ZonedDateTime _fixingPeriodStartDate;
  /**
   * The end date of the fixing period.
   */
  private final ZonedDateTime _fixingPeriodEndDate;
  /**
   * The accrual factor (or year fraction) associated to the fixing period in the Index day count convention.
   */
  private final double _fixingPeriodAccrualFactor;
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
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param index The coupon Ibor index. Should of the same currency as the payment.
   * @param mainCoefficients The coefficients of the main payment (before floor and cap). Array of length 3.
   * @param floorCoefficients The coefficients of the floor. Array of length 3. The first coefficient is the previous coupon factor,
   * the second is the Ibor factor and the third is the additive term.
   * @param capCoefficients The coefficients of the cap. Array of length 3.
   * @param calendar The holiday calendar for the ibor index.
   */
  public CouponIborRatchetDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double accrualFactor,
      final double notional, final ZonedDateTime fixingDate, final IborIndex index, final double[] mainCoefficients, final double[] floorCoefficients, final double[] capCoefficients,
      final Calendar calendar) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate);
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "index currency different from payment currency");
    ArgumentChecker.notNull(mainCoefficients, "Main coefficients");
    ArgumentChecker.notNull(floorCoefficients, "Floor coefficients");
    ArgumentChecker.notNull(capCoefficients, "Cap coefficients");
    ArgumentChecker.isTrue(mainCoefficients.length == 3, "Requires 3 main coefficients");
    ArgumentChecker.isTrue(floorCoefficients.length == 3, "Requires 3 floor coefficients");
    ArgumentChecker.isTrue(capCoefficients.length == 3, "Requires 3 cap coefficients");
    _index = index;
    _fixingPeriodStartDate = ScheduleCalculator.getAdjustedDate(fixingDate, _index.getSpotLag(), calendar);
    _fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(_fixingPeriodStartDate, index.getTenor(), index.getBusinessDayConvention(), calendar, index.isEndOfMonth());
    _fixingPeriodAccrualFactor = index.getDayCount().getDayCountFraction(_fixingPeriodStartDate, _fixingPeriodEndDate, calendar);
    _mainCoefficients = mainCoefficients;
    _floorCoefficients = floorCoefficients;
    _capCoefficients = capCoefficients;
  }

  /**
   * Gets the Ibor index of the instrument.
   * @return The index.
   */
  public IborIndex getIndex() {
    return _index;
  }

  /**
   * Gets the start date of the fixing period.
   * @return The start date of the fixing period.
   */
  public ZonedDateTime getFixingPeriodStartDate() {
    return _fixingPeriodStartDate;
  }

  /**
   * Gets the end date of the fixing period.
   * @return The end date of the fixing period.
   */
  public ZonedDateTime getFixingPeriodEndDate() {
    return _fixingPeriodEndDate;
  }

  /**
   * Gets the accrual factor (or year fraction) associated to the fixing period in the Index day count convention.
   * @return The accrual factor.
   */
  public double getFixingPeriodAccrualFactor() {
    return _fixingPeriodAccrualFactor;
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

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborRatchetDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborRatchetDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_capCoefficients);
    result = prime * result + Arrays.hashCode(_floorCoefficients);
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
    final CouponIborRatchetDefinition other = (CouponIborRatchetDefinition) obj;
    if (!Arrays.equals(_capCoefficients, other._capCoefficients)) {
      return false;
    }
    if (!Arrays.equals(_floorCoefficients, other._floorCoefficients)) {
      return false;
    }
    if (!Arrays.equals(_mainCoefficients, other._mainCoefficients)) {
      return false;
    }
    return true;
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public Payment toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> data, final String... yieldCurveNames) {
    throw new UnsupportedOperationException();
  }

  @Override
  public CouponIborRatchet toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(getFixingDate()), "Do not have any fixing data but are asking for a derivative after the fixing date " + getFixingDate() + " " + date);
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final double fixingTime = TimeCalculator.getTimeBetween(date, getFixingDate());
    final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(date, getFixingPeriodStartDate());
    final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(date, getFixingPeriodEndDate());
    return new CouponIborRatchet(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixingTime, fixingPeriodStartTime, fixingPeriodEndTime,
        getFixingPeriodAccrualFactor(), getIndex(), _mainCoefficients, _floorCoefficients, _capCoefficients);
  }

  @Override
  public Payment toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> data) {
    return null;
    //TODO: coupon with fixing!
  }

}
