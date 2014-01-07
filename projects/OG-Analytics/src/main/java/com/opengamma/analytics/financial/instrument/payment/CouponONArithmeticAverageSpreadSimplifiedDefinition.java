/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpreadSimplified;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a Fed Fund swap-like floating coupon (arithmetic average on overnight rates) with a spread.
 * Simplified definition which contains only the starting date and end date (not all intermediary dates). The fixing and accrual dates are equal.
 * Can not be used for "aged" coupon but only for forward coupons when all the details of intermediary fixing are not required.
 * In particular can be used for
 */
public class CouponONArithmeticAverageSpreadSimplifiedDefinition extends CouponDefinition {

  /**
   * The overnight index on which the coupon fixes. The index currency should be the same as the coupon currency. Not null.
   */
  private final IndexON _index;
  /**
   * The spread rate paid above the arithmetic average.
   */
  private final double _spread;
  /**
   * The fixed amount related to the spread.
   */
  private final double _spreadAmount;

  /**
   * Constructor from all the coupon details.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param paymentYearFraction Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param index The OIS-like index on which the coupon fixes.
   * @param spread The spread rate paid above the arithmetic average.
   */
  public CouponONArithmeticAverageSpreadSimplifiedDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentYearFraction, final double notional, final IndexON index, final double spread) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentYearFraction, notional);
    ArgumentChecker.notNull(index, "CouponOISDefinition: index");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "Coupon and index currencies are not compatible. Expected to be the same");
    _index = index;
    _spread = spread;
    _spreadAmount = spread * paymentYearFraction * notional;
  }

  /**
   * Builder from financial details. The accrual and fixing start and end dates are the same. The day count for the payment is the same as the one for the index.
   * The payment date is adjusted by the publication lag and the settlement days.
   * @param index The OIS index.
   * @param fixingPeriodStartDate The coupon settlement date and start of the fixing period.
   * @param tenor The coupon tenor.
   * @param notional The notional.
   * @param paymentLag The number of days between last fixing date and the payment date (also called payment delay).
   * @param businessDayConvention The business day convention to compute the end date of the coupon.
   * @param isEOM The end-of-month convention to compute the end date of the coupon.
   * @param spread The spread rate paid above the arithmetic average.
   * @param calendar The holiday calendar for the overnight index.
   * @return The OIS coupon.
   */
  public static CouponONArithmeticAverageSpreadSimplifiedDefinition from(final IndexON index, final ZonedDateTime fixingPeriodStartDate, final Period tenor, final double notional,
      final int paymentLag, final BusinessDayConvention businessDayConvention, final boolean isEOM, final double spread, final Calendar calendar) {
    final ZonedDateTime fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(fixingPeriodStartDate, tenor, businessDayConvention, calendar, isEOM);
    return from(index, fixingPeriodStartDate, fixingPeriodEndDate, notional, paymentLag, spread, calendar);
  }

  /**
   * Builder from financial details. The accrual and fixing start and end dates are the same. The day count for the payment is the same as the one for the index.
   * The payment date is adjusted by the publication lag and the settlement days.
   * @param index The OIS index.
   * @param fixingPeriodStartDate The coupon settlement date and start of the fixing period.
   * @param fixingPeriodEndDate The last date of the fixing period. Interest accrues up to this date. If publicationLag==0, 1 day following publication. If lag==1, the publication date.
   * @param notional The notional.
   * @param paymentLag The number of days between last fixing date and the payment date (also called payment delay).
   * @param spread The spread rate paid above the arithmetic average.
   * @param calendar The holiday calendar for the overnight index.
   * @return The OIS coupon.
   */
  public static CouponONArithmeticAverageSpreadSimplifiedDefinition from(final IndexON index, final ZonedDateTime fixingPeriodStartDate, final ZonedDateTime fixingPeriodEndDate,
      final double notional, final int paymentLag, final double spread, final Calendar calendar) {
    final ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(fixingPeriodEndDate, -1 + index.getPublicationLag() + paymentLag, calendar);
    final double paymentYearFraction = index.getDayCount().getDayCountFraction(fixingPeriodStartDate, fixingPeriodEndDate, calendar);
    return new CouponONArithmeticAverageSpreadSimplifiedDefinition(index.getCurrency(), paymentDate, fixingPeriodStartDate, fixingPeriodEndDate, paymentYearFraction, notional, index, spread);
  }

  /**
   * Gets the OIS index of the instrument.
   * @return The index.
   */
  public IndexON getIndex() {
    return _index;
  }

  /**
   * Returns the spread rate paid above the arithmetic average.
   * @return The spread.
   */
  public double getSpread() {
    return _spread;
  }

  /**
   * Returns the fixed amount related to the spread.
   * @return The amount.
   */
  public double getSpreadAmount() {
    return _spreadAmount;
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public CouponONArithmeticAverageSpreadSimplified toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    ArgumentChecker.isTrue(!getAccrualStartDate().plusDays(_index.getPublicationLag()).isBefore(date), "First fixing publication strictly before reference date");
    return toDerivative(date);
  }

  @Override
  public CouponONArithmeticAverageSpreadSimplified toDerivative(final ZonedDateTime date) {
    ArgumentChecker.isTrue(!getAccrualStartDate().plusDays(_index.getPublicationLag()).isBefore(date), "First fixing publication strictly before reference date");
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final double fixingPeriodStartTimes = TimeCalculator.getTimeBetween(date, getAccrualStartDate());
    final double fixingPeriodEndTimes = TimeCalculator.getTimeBetween(date, getAccrualEndDate());
    return CouponONArithmeticAverageSpreadSimplified.from(paymentTime, getPaymentYearFraction(), getNotional(), _index, fixingPeriodStartTimes, fixingPeriodEndTimes,
        getPaymentYearFraction(), _spread);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponArithmeticAverageONSpreadSimplifiedDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponArithmeticAverageONSpreadSimplifiedDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _index.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_spread);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_spreadAmount);
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
    final CouponONArithmeticAverageSpreadSimplifiedDefinition other = (CouponONArithmeticAverageSpreadSimplifiedDefinition) obj;
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (Double.doubleToLongBits(_spread) != Double.doubleToLongBits(other._spread)) {
      return false;
    }
    if (Double.doubleToLongBits(_spreadAmount) != Double.doubleToLongBits(other._spreadAmount)) {
      return false;
    }
    return true;
  }

}
