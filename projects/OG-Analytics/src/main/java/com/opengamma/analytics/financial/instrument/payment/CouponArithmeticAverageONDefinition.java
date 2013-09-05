/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponArithmeticAverageON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a Fed Fund swap-like floating coupon (arithmetic average on overnight rates).
 */
public final class CouponArithmeticAverageONDefinition extends CouponDefinition implements InstrumentDefinitionWithData<Payment, DoubleTimeSeries<ZonedDateTime>> {

  /**
   * The overnight index on which the coupon fixes. The index currency should be the same as the coupon currency.
   */
  private final IndexON _index;
  /**
   * The dates of the fixing periods. The length is one greater than the number of periods, as it includes accrual start and end.
   */
  private final ZonedDateTime[] _fixingPeriodDate;
  /**
   * The accrual factors (or year fractions) associated to the fixing periods in the Index day count convention.
   */
  private final double[] _fixingPeriodAccrualFactor;

  // TODO: Implement the rate cut-off mechanism (the two last periods use the same fixing)

  /**
   * Constructor from all the coupon details.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param paymentAccrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param index The OIS-like index on which the coupon fixes.
   * @param fixingPeriodStartDate The start date of the fixing period.
   * @param fixingPeriodEndDate The end date of the fixing period.
   * @param calendar The holiday calendar for the overnight leg.
   */
  private CouponArithmeticAverageONDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final IndexON index, final ZonedDateTime fixingPeriodStartDate, final ZonedDateTime fixingPeriodEndDate,
      final Calendar calendar) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional);
    ArgumentChecker.notNull(index, "CouponArithmeticAverageONDefinition: index");
    ArgumentChecker.notNull(fixingPeriodStartDate, "CouponArithmeticAverageONDefinition: fixingPeriodStartDate");
    ArgumentChecker.notNull(fixingPeriodEndDate, "CouponArithmeticAverageONDefinition: fixingPeriodEndDate");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "Coupon and index currencies are not compatible. Expected to be the same");
    _index = index;
    final List<ZonedDateTime> fixingDateList = new ArrayList<>();
    final List<Double> fixingAccrualFactorList = new ArrayList<>();
    ZonedDateTime currentDate = fixingPeriodStartDate;
    fixingDateList.add(currentDate);
    ZonedDateTime nextDate;
    while (currentDate.isBefore(fixingPeriodEndDate)) {
      nextDate = ScheduleCalculator.getAdjustedDate(currentDate, 1, calendar);
      fixingDateList.add(nextDate);
      fixingAccrualFactorList.add(index.getDayCount().getDayCountFraction(currentDate, nextDate, calendar));
      currentDate = nextDate;
    }
    _fixingPeriodDate = fixingDateList.toArray(new ZonedDateTime[fixingDateList.size()]);
    _fixingPeriodAccrualFactor = ArrayUtils.toPrimitive(fixingAccrualFactorList.toArray(new Double[fixingAccrualFactorList.size()]));
  }

  /**
   * Builder from financial details. The accrual and fixing start and end dates are the same. The day count for the payment is the same as the one for the index.
   * The payment date is adjusted by the publication lag and the settlement days.
   * @param index The OIS index.
   * @param fixingPeriodStartDate The coupon settlement date and start of the fixing period.
   * @param tenor The coupon tenor.
   * @param notional The notional.
   * @param paymentLag The number of days between last fixing and the payment (also called payment delay).
   * @param businessDayConvention The business day convention to compute the end date of the coupon.
   * @param isEOM The end-of-month convention to compute the end date of the coupon.
   * @param calendar The holiday calendar for the overnight index.
   * @return The OIS coupon.
   */
  public static CouponArithmeticAverageONDefinition from(final IndexON index, final ZonedDateTime fixingPeriodStartDate, final Period tenor, final double notional, final int paymentLag,
      final BusinessDayConvention businessDayConvention, final boolean isEOM, final Calendar calendar) {
    ArgumentChecker.notNull(index, "Index");
    final ZonedDateTime fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(fixingPeriodStartDate, tenor, businessDayConvention, calendar, isEOM);
    return from(index, fixingPeriodStartDate, fixingPeriodEndDate, notional, paymentLag, calendar);
  }

  /**
   * Builder from financial details. The accrual and fixing start and end dates are the same. The day count for the payment is the same as the one of the index.
   * The payment date is adjusted by the publication lag and the settlement days.
   * @param index The OIS index.
   * @param fixingPeriodStartDate The coupon settlement date and start of the fixing period.
   * @param fixingPeriodEndDate The last date of the fixing period. Interest accrues up to this date. If publicationLag==0, 1 day following publication. If lag==1, the publication date.
   * @param notional The notional.
   * @param paymentLag The number of days between last fixing and the payment (also called payment delay).
   * @param calendar The holiday calendar for the overnight index.
   * @return The OIS coupon.
   */
  public static CouponArithmeticAverageONDefinition from(final IndexON index, final ZonedDateTime fixingPeriodStartDate, final ZonedDateTime fixingPeriodEndDate, final double notional,
      final int paymentLag, final Calendar calendar) {
    ArgumentChecker.notNull(fixingPeriodEndDate, "Fixing Period End Date");
    final ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(fixingPeriodEndDate, -1 + index.getPublicationLag() + paymentLag, calendar);
    final double paymentAccrualFactor = index.getDayCount().getDayCountFraction(fixingPeriodStartDate, fixingPeriodEndDate, calendar);
    return new CouponArithmeticAverageONDefinition(index.getCurrency(), paymentDate, fixingPeriodStartDate, fixingPeriodEndDate, paymentAccrualFactor,
        notional, index, fixingPeriodStartDate, fixingPeriodEndDate, calendar);
  }

  /**
   * Gets the OIS index of the instrument.
   * @return The index.
   */
  public IndexON getIndex() {
    return _index;
  }

  /**
   * Gets the dates of the fixing periods (start and end). There is one date more than period.
   * @return The dates of the fixing periods.
   */
  public ZonedDateTime[] getFixingPeriodDate() {
    return _fixingPeriodDate;
  }

  /**
   * Gets the accrual factors (or year fractions) associated to the fixing periods in the Index day count convention.
   * @return The accrual factors.
   */
  public double[] getFixingPeriodAccrualFactor() {
    return _fixingPeriodAccrualFactor;
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public CouponArithmeticAverageON toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    ArgumentChecker.isTrue(!_fixingPeriodDate[0].plusDays(_index.getPublicationLag()).isBefore(date), "First fixing publication strictly before reference date");
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final double[] fixingPeriodTimes = TimeCalculator.getTimeBetween(date, _fixingPeriodDate);
    return CouponArithmeticAverageON.from(paymentTime, getPaymentYearFraction(), getNotional(), _index, fixingPeriodTimes, _fixingPeriodAccrualFactor, 0);
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public Coupon toDerivative(final ZonedDateTime valZdt, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries, final String... yieldCurveNames) {
    return null; // TODO
  }

  @Override
  public CouponArithmeticAverageON toDerivative(final ZonedDateTime date) {
    ArgumentChecker.isTrue(!_fixingPeriodDate[0].plusDays(_index.getPublicationLag()).isBefore(date), "First fixing publication strictly before reference date");
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final double[] fixingPeriodTimes = TimeCalculator.getTimeBetween(date, _fixingPeriodDate);
    return CouponArithmeticAverageON.from(paymentTime, getPaymentYearFraction(), getNotional(), _index, fixingPeriodTimes, _fixingPeriodAccrualFactor, 0);
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime valZdt, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries) {
    return null; // TODO
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponArithmeticAverageONDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponArithmeticAverageONDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_fixingPeriodAccrualFactor);
    result = prime * result + Arrays.hashCode(_fixingPeriodDate);
    result = prime * result + _index.hashCode();
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
    final CouponArithmeticAverageONDefinition other = (CouponArithmeticAverageONDefinition) obj;
    if (!Arrays.equals(_fixingPeriodAccrualFactor, other._fixingPeriodAccrualFactor)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodDate, other._fixingPeriodDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    return true;
  }

}
