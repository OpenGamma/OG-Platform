/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.analytics.financial.instrument.payment;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedCompounding;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a fixed compounded coupon. The fixed rate is compounded over several sub-periods.
 * The amount paid is equal to
 * $$
 * \begin{equation*}
 * \left(\prod_{i=1}^n (1+\delta_i r) \right)-1
 * \end{equation*}
 * $$
 * where the $\delta_i$ are the accrual factors of the sub periods and the $r$ the fixed rate for the same periods.
 */
public final class CouponFixedCompoundingDefinition extends CouponDefinition {

  /**
   * The fixed rate.
   * All the coupon sub-periods use the same fixed rate.
   */
  private final double _rate;
  /**
   * The start dates of the accrual sub-periods.
   */
  private final ZonedDateTime[] _accrualStartDates;
  /**
   * The end dates of the accrual sub-periods.
   */
  private final ZonedDateTime[] _accrualEndDates;
  /**
   * The accrual factors (or year fraction) associated to the sub-periods.
   */
  private final double[] _paymentAccrualFactors;

  /**
   * Constructor.
   * @param currency The coupon currency.
   * @param paymentDate The coupon payment date.
   * @param accrualStartDate The start date of the accrual period.
   * @param accrualEndDate The end date of the accrual period.
   * @param paymentAccrualFactor The accrual factor of the accrual period.
   * @param notional The coupon notional.
   * @param rate Fixed rate.
   * @param accrualStartDates The start dates of the accrual sub-periods.
   * @param accrualEndDates The end dates of the accrual sub-periods.
   * @param paymentAccrualFactors The accrual factors (or year fraction) associated to the sub-periods.
   */
  private CouponFixedCompoundingDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final double rate, final ZonedDateTime[] accrualStartDates, final ZonedDateTime[] accrualEndDates,
      final double[] paymentAccrualFactors) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional);
    ArgumentChecker.isTrue(accrualStartDates.length == accrualEndDates.length, "Accrual start and end dates should have same length");
    _rate = rate;
    _accrualStartDates = accrualStartDates;
    _accrualEndDates = accrualEndDates;
    _paymentAccrualFactors = paymentAccrualFactors;
  }

  /**
   * Builds a fixed compounded coupon from all the details.
   * @param currency The coupon currency.
   * @param paymentDate The coupon payment date.
   * @param accrualStartDate The start date of the accrual period.
   * @param accrualEndDate The end date of the accrual period.
   * @param paymentAccrualFactor The accrual factor of the accrual period.
   * @param notional The coupon notional.
   * @param rate Fixed rate.
   * @param accrualStartDates The start dates of the accrual sub-periods.
   * @param accrualEndDates The end dates of the accrual sub-periods.
   * @param paymentAccrualFactors The accrual factors (or year fraction) associated to the sub-periods.
   * @return The compounded coupon.
   */
  public static CouponFixedCompoundingDefinition from(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final double rate, final ZonedDateTime[] accrualStartDates, final ZonedDateTime[] accrualEndDates,
      final double[] paymentAccrualFactors) {
    return new CouponFixedCompoundingDefinition(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional, rate, accrualStartDates, accrualEndDates,
        paymentAccrualFactors);
  }

  /**
   * Builds an fixed compounded coupon from the accrual and payment details.
   * @param currency The coupon currency.
   * @param paymentDate The coupon payment date.
   * @param notional The coupon notional.
   * @param rate The fixed rate.
   * @param accrualStartDates The start dates of the accrual sub-periods.
   * @param accrualEndDates The end dates of the accrual sub-periods.
   * @param paymentAccrualFactors The accrual factors (or year fraction) associated to the sub-periods.
   * @return The compounded coupon.
   */
  public static CouponFixedCompoundingDefinition from(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime[] accrualStartDates,
      final ZonedDateTime[] accrualEndDates, final double[] paymentAccrualFactors, final double notional, final double rate) {
    final int nbSubPeriod = accrualEndDates.length;
    final ZonedDateTime accrualStartDate = accrualStartDates[0];
    final ZonedDateTime accrualEndDate = accrualEndDates[nbSubPeriod - 1];
    double paymentAccrualFactor = 0.0;
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {
      paymentAccrualFactor += paymentAccrualFactors[loopsub];
    }
    return new CouponFixedCompoundingDefinition(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional, rate, accrualStartDates, accrualEndDates,
        paymentAccrualFactors);
  }

  /**
   * Builds a fixed compounded coupon from a total period.
   * If required the stub of the sub-periods will be short and last. The payment date is the start accrual date plus the tenor in the index conventions.
   * @param currency The currency.
   * @param accrualStartDate The first accrual date.
   * @param accrualEndDate The last accrual date.
   * @param notional The coupon notional.
   * @param tenorPeriod Perriod between two consecutive accrual dates(usually 1 year).
   * @param rate The fixed rate.
   * @return The compounded coupon.
   */
  public static CouponFixedCompoundingDefinition from(final Currency currency, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double notional,
      final Period tenorPeriod, final double rate) {
    final ZonedDateTime[] accrualEndDates = ScheduleCalculator.getUnadjustedDateSchedule(accrualStartDate, accrualEndDate, tenorPeriod, true, false);
    final int nbSubPeriod = accrualEndDates.length;
    final ZonedDateTime[] accrualStartDates = new ZonedDateTime[nbSubPeriod];
    accrualStartDates[0] = accrualStartDate;
    System.arraycopy(accrualEndDates, 0, accrualStartDates, 1, nbSubPeriod - 1);
    final double[] paymentAccrualFactors = new double[nbSubPeriod];
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {
      paymentAccrualFactors[loopsub] = TimeCalculator.getTimeBetween(accrualStartDates[loopsub], accrualEndDates[loopsub]);
    }
    return from(currency, accrualEndDates[nbSubPeriod - 1], accrualStartDates, accrualEndDates, paymentAccrualFactors, notional, rate);
  }

  /**
   * Builds a fixed compounded coupon from a tenor in years. This constructor is needed for inflation ZC swap.
   * If required the stub of the sub-periods will be short and last. The payment date is the start accrual date plus the tenor in the index conventions.
   * @param currency The currency.
   * @param accrualStartDate The first accrual date.
   * @param paymentDate The payment date.
   * @param notional The coupon notional.
   * @param tenor The total coupon tenor.
   * @param rate The fixed rate.
   * @return The compounded coupon.
   */
  public static CouponFixedCompoundingDefinition from(final Currency currency, final ZonedDateTime accrualStartDate, final ZonedDateTime paymentDate, final double notional,
      final int tenor, final double rate) {
    final ZonedDateTime[] accrualEndDates = ScheduleCalculator.getUnadjustedDateSchedule(accrualStartDate, accrualStartDate.plus(Period.ofYears(tenor)), Period.ofYears(1), true, false);

    final int nbSubPeriod = accrualEndDates.length;
    final ZonedDateTime[] accrualStartDates = new ZonedDateTime[nbSubPeriod];
    accrualStartDates[0] = accrualStartDate;
    System.arraycopy(accrualEndDates, 0, accrualStartDates, 1, nbSubPeriod - 1);
    final double[] paymentAccrualFactors = new double[nbSubPeriod];
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {
      paymentAccrualFactors[loopsub] = 1.0;
    }
    return from(currency, paymentDate, accrualStartDates, accrualEndDates, paymentAccrualFactors, notional, rate);
  }

  public double getRate() {
    return _rate;
  }

  public ZonedDateTime[] getAccrualStartDates() {
    return _accrualStartDates;
  }

  public ZonedDateTime[] getAccrualEndDates() {
    return _accrualEndDates;
  }

  public double[] getPaymentAccrualFactors() {
    return _paymentAccrualFactors;
  }

  @Override
  public String toString() {
    return "CouponFixedCompoundingDefinition [_rate=" + _rate + "]";
  }

  @Override
  public CouponFixedCompounding toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    final LocalDate dayConversion = date.toLocalDate();
    ArgumentChecker.isTrue(!dayConversion.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    return new CouponFixedCompounding(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(),
        getPaymentAccrualFactors(), getRate());
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponFixedCompoundingDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponFixedCompoundingDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_rate);
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
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CouponFixedCompoundingDefinition other = (CouponFixedCompoundingDefinition) obj;
    if (Double.doubleToLongBits(_rate) != Double.doubleToLongBits(other._rate)) {
      return false;
    }
    return true;
  }

}
