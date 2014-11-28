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
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONSpread;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a overnight-indexed floating coupon with a spread. The description is simplified by not creating the full set of fixing dates.
 * Only the start and the end of the fixing period are described. The description is enough to construct curves from OIS and to price forward OIS but not
 * to describe and price OIS coupons for which some fixing already took place.
 */
public class CouponONSpreadSimplifiedDefinition extends CouponDefinition {

  /**
   * The overnight index on which the coupon fixes. The index currency should be the same as the coupon currency.
   */
  private final IndexON _index;
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
   * @param paymentAccrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param index The OIS-like index on which the coupon fixes.
   * @param fixingPeriodStartDate The start date of the fixing period.
   * @param fixingPeriodEndDate The end date of the fixing period.
   * @param fixingPeriodAccrualFactor The accrual factor (or year fraction) associated to the fixing period in the Index day count convention.
   * @param spread The spread.
   */
  public CouponONSpreadSimplifiedDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate,
      final ZonedDateTime accrualEndDate, final double paymentAccrualFactor, final double notional, final IndexON index, final ZonedDateTime fixingPeriodStartDate,
      final ZonedDateTime fixingPeriodEndDate, final double fixingPeriodAccrualFactor, final double spread) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional);
    ArgumentChecker.notNull(index, "Coupon OIS Simplified: index");
    ArgumentChecker.notNull(fixingPeriodStartDate, "Coupon OIS Simplified: fixingPeriodStartDate");
    ArgumentChecker.notNull(fixingPeriodEndDate, "Coupon OIS Simplified: fixingPeriodEndDate");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "Currency and index not compatible");
    _index = index;
    _fixingPeriodStartDate = fixingPeriodStartDate;
    _fixingPeriodEndDate = fixingPeriodEndDate;
    _fixingPeriodAccrualFactor = fixingPeriodAccrualFactor;
    _spread = spread;
    _spreadAmount = spread * paymentAccrualFactor * notional;
  }

  /**
   * Builder from financial details. The accrual and fixing dates (start and end) are the same. The day count for the payment is the same as the one for the index.
   * @param index The OIS index.
   * @param settlementDate The coupon settlement date.
   * @param tenor The coupon tenor.
   * @param notional The notional.
   * @param spread The spread.
   * @param settlementDays The number of days between last fixing and the payment (also called spot lag).
   * @param businessDayConvention The business day convention to compute the end date of the coupon.
   * @param isEOM The end-of-month convention to compute the end date of the coupon.
   * @param calendar The holiday calendar for the overnight index.
   * @return The OIS coupon.
   */
  public static CouponONSpreadSimplifiedDefinition from(final IndexON index, final ZonedDateTime settlementDate, final Period tenor, final double notional, final double spread,
      final int settlementDays, final BusinessDayConvention businessDayConvention, final boolean isEOM, final Calendar calendar) {
    final ZonedDateTime endFixingPeriodDate = ScheduleCalculator.getAdjustedDate(settlementDate, tenor, businessDayConvention, calendar, isEOM);
    return CouponONSpreadSimplifiedDefinition.from(index, settlementDate, endFixingPeriodDate, notional, spread, settlementDays, calendar);
  }

  /**
   * Builder from the financial details.  The accrual and fixing dates (start and end) are the same. The day count for the payment is the same as the one for the index.
   * The payment date is computed from the endFixingPeriodDate by moving backward by one day (overnight), then forward by the index publication lag and finally by the settlementDays days.
   * @param index The OIS index.
   * @param settlementDate The coupon settlement date.
   * @param endFixingPeriodDate The end date of the fixing period (also used for the end accrual date).
   * @param notional The notional.
   * @param spread The spread.
   * @param settlementDays The number of days between last fixing date and the payment date (also called payment lag).
   * @param calendar The holiday calendar for the overnight index.
   * @return The OIS coupon.
   */
  public static CouponONSpreadSimplifiedDefinition from(final IndexON index, final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate,
      final double notional, final double spread, final int settlementDays, final Calendar calendar) {
    ZonedDateTime lastFixingDate = ScheduleCalculator.getAdjustedDate(endFixingPeriodDate, -1, calendar); // Overnight
    lastFixingDate = ScheduleCalculator.getAdjustedDate(lastFixingDate, index.getPublicationLag(), calendar); // Lag
    final ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(lastFixingDate, settlementDays, calendar);
    final double payementAccrualFactor = index.getDayCount().getDayCountFraction(settlementDate, endFixingPeriodDate, calendar);
    return new CouponONSpreadSimplifiedDefinition(index.getCurrency(), paymentDate, settlementDate, endFixingPeriodDate, payementAccrualFactor, notional, index,
        settlementDate, endFixingPeriodDate, payementAccrualFactor, spread);
  }

  /**
   * Gets the OIS index of the instrument.
   * @return The index.
   */
  public IndexON getIndex() {
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
   * Gets the spread.
   * @return The spread.
   */
  public double getSpread() {
    return _spread;
  }

  /**
   * Gets the spread amount.
   * @return The amount.
   */
  public double getSpreadAmount() {
    return _spreadAmount;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_fixingPeriodAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_fixingPeriodEndDate == null) ? 0 : _fixingPeriodEndDate.hashCode());
    result = prime * result + ((_fixingPeriodStartDate == null) ? 0 : _fixingPeriodStartDate.hashCode());
    result = prime * result + ((_index == null) ? 0 : _index.hashCode());
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
    final CouponONSpreadSimplifiedDefinition other = (CouponONSpreadSimplifiedDefinition) obj;
    if (Double.doubleToLongBits(_fixingPeriodAccrualFactor) != Double.doubleToLongBits(other._fixingPeriodAccrualFactor)) {
      return false;
    }
    if (!ObjectUtils.equals(_fixingPeriodEndDate, other._fixingPeriodEndDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_fixingPeriodStartDate, other._fixingPeriodStartDate)) {
      return false;
    }
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

  @Override
  public CouponONSpread toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(_fixingPeriodStartDate) || date.toLocalDate().equals(_fixingPeriodStartDate.toLocalDate()),
        "Simplified Coupon OIS only valid for dates where the fixing has not taken place yet.");
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(date, _fixingPeriodStartDate);
    final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(date, _fixingPeriodEndDate);
    final CouponONSpread cpn = new CouponONSpread(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), _index, fixingPeriodStartTime,
        fixingPeriodEndTime, _fixingPeriodAccrualFactor, getNotional(), _spreadAmount);
    return cpn;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponONSpreadSimplifiedDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponONSpreadSimplifiedDefinition(this);
  }

}
