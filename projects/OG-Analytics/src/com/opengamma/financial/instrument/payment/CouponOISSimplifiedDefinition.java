/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.payment;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.index.IndexON;
import com.opengamma.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.TimeCalculator;

/**
 * Class describing a OIS-like floating coupon. The description is simplified by not creating the full set of fixing dates. 
 * Only the start and the end of the fixing period are described. The description is enough to construct curves from OIS and to price forward OIS but not 
 * to describe and price OIS coupons for which some fixing already took place.
 */
public class CouponOISSimplifiedDefinition extends CouponDefinition {

  /**
   * The OIS-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
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
   * Constructor from all the coupon details.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param paymentYearFraction Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param index The OIS-like index on which the coupon fixes.
   * @param fixingPeriodStartDate The start date of the fixing period.
   * @param fixingPeriodEndDate The end date of the fixing period.
   * @param fixingPeriodAccrualFactor The accrual factor (or year fraction) associated to the fixing period in the Index day count convention.
   */
  public CouponOISSimplifiedDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, double paymentYearFraction,
      double notional, final IndexON index, final ZonedDateTime fixingPeriodStartDate, final ZonedDateTime fixingPeriodEndDate, double fixingPeriodAccrualFactor) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentYearFraction, notional);
    Validate.notNull(index, "Coupon OIS Simplified: index");
    Validate.notNull(fixingPeriodStartDate, "Coupon OIS Simplified: fixingPeriodStartDate");
    Validate.notNull(fixingPeriodEndDate, "Coupon OIS Simplified: fixingPeriodEndDate");
    Validate.isTrue(currency.equals(index.getCurrency()), "Currency and index not compatible");
    _index = index;
    _fixingPeriodStartDate = fixingPeriodStartDate;
    _fixingPeriodEndDate = fixingPeriodEndDate;
    _fixingPeriodAccrualFactor = fixingPeriodAccrualFactor;
  }

  /**
   * Builder from financial details. The accrual and fixing dates (start and end) are the same. The day count for the payment is the same as the one for the index.
   * @param index The OIS index.
   * @param settlementDate The coupon settlement date.
   * @param tenor The coupon tenor.
   * @param notional The notional.
   * @param settlementDays The number of days between last fixing and the payment (also called spot lag). 
   * @param businessDayConvention The business day convention to compute the end date of the coupon.
   * @param isEOM The end-of-month convention to compute the end date of the coupon.
   * @return The OIS coupon.
   */
  public static CouponOISSimplifiedDefinition from(final IndexON index, final ZonedDateTime settlementDate, final Period tenor, final double notional, final int settlementDays,
      final BusinessDayConvention businessDayConvention, final boolean isEOM) {
    ZonedDateTime endFixingPeriodDate = ScheduleCalculator.getAdjustedDate(settlementDate, businessDayConvention, index.getCalendar(), isEOM, tenor);
    return CouponOISSimplifiedDefinition.from(index, settlementDate, endFixingPeriodDate, notional, settlementDays);
  }

  /**
   * Builder from the financial details.  The accrual and fixing dates (start and end) are the same. The day count for the payment is the same as the one for the index. 
   * The payment date is computed from the endFixingPeriodDate by moving backward by one day (overnight), then forward by the index publication lag and finally by the settlementDays days.
   * @param index The OIS index.
   * @param settlementDate The coupon settlement date.
   * @param endFixingPeriodDate The end date of the fixing period (also used for the end accrual date).
   * @param notional The notional.
   * @param settlementDays The number of days between last fixing and the payment (also called spot lag). 
   * @return The OIS coupon.
   */
  public static CouponOISSimplifiedDefinition from(final IndexON index, final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final double notional, final int settlementDays) {
    ZonedDateTime lastFixingDate = ScheduleCalculator.getAdjustedDate(endFixingPeriodDate, index.getCalendar(), -1); // Overnight
    lastFixingDate = ScheduleCalculator.getAdjustedDate(lastFixingDate, index.getCalendar(), index.getPublicationLag()); // Lag
    ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(lastFixingDate, index.getCalendar(), settlementDays);
    double payementAccrualFactor = index.getDayCount().getDayCountFraction(settlementDate, endFixingPeriodDate);
    return new CouponOISSimplifiedDefinition(index.getCurrency(), paymentDate, settlementDate, endFixingPeriodDate, payementAccrualFactor, notional, index, settlementDate, endFixingPeriodDate,
        payementAccrualFactor);
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_fixingPeriodAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _fixingPeriodEndDate.hashCode();
    result = prime * result + _fixingPeriodStartDate.hashCode();
    result = prime * result + _index.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CouponOISSimplifiedDefinition other = (CouponOISSimplifiedDefinition) obj;
    if (!ObjectUtils.equals(_fixingPeriodEndDate, other._fixingPeriodEndDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_fixingPeriodStartDate, other._fixingPeriodStartDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingPeriodAccrualFactor) != Double.doubleToLongBits(other._fixingPeriodAccrualFactor)) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    return true;
  }

  @Override
  public CouponOIS toDerivative(ZonedDateTime date, String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.isTrue(!date.isAfter(_fixingPeriodStartDate), "Simplified Coupon OIS only valid at dates where the fixing has not taken place yet.");
    Validate.isTrue(yieldCurveNames.length > 1, "at least two curves required");
    double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    double fixingPeriodStartTime = TimeCalculator.getTimeBetween(date, _fixingPeriodStartDate);
    double fixingPeriodEndTime = TimeCalculator.getTimeBetween(date, _fixingPeriodEndDate);
    CouponOIS cpn = new CouponOIS(getCurrency(), paymentTime, yieldCurveNames[0], getPaymentYearFraction(), getNotional(), _index, fixingPeriodStartTime, fixingPeriodEndTime,
        _fixingPeriodAccrualFactor, getNotional(), yieldCurveNames[1]);
    return cpn;
  }

  @Override
  public <U, V> V accept(InstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitCouponOISSimplified(this, data);
  }

  @Override
  public <V> V accept(InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitCouponOISSimplified(this);
  }

}
