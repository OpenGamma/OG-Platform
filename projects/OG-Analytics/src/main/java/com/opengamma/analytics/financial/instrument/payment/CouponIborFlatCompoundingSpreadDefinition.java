/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborFlatCompoundingSpread;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CouponIborFlatCompoundingSpreadDefinition extends CouponIborAverageCompoundingDefinition {

  private final double _spread;

  /**
   * Constructor without start dates and end dates of fixing period
   * @param currency The coupon currency
   * @param paymentDate The coupon payment date.
   * @param accrualStartDate The start date of the accrual period
   * @param accrualEndDate The end date of the accrual period
   * @param paymentAccrualFactor The accrual factor of the total accrual period
   * @param notional The coupon notional
   * @param paymentAccrualFactors The accrual factors associated to the sub-periods
   * @param index The coupon Ibor index
   * @param fixingDates The coupon fixing dates
   * @param weights The weights for the index
   * @param iborCalendar The holiday calendar for the index
   * @param spread The spread
   */
  public CouponIborFlatCompoundingSpreadDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final double[] paymentAccrualFactors, final IborIndex index, final ZonedDateTime[][] fixingDates, final double[][] weights,
      final Calendar iborCalendar, final double spread) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional, paymentAccrualFactors, index, fixingDates, weights, iborCalendar);
    _spread = spread;
  }

  /**
   * Constructor with full details
   * @param currency The coupon currency
   * @param paymentDate The coupon payment date.
   * @param accrualStartDate The start date of the accrual period
   * @param accrualEndDate The end date of the accrual period
   * @param paymentAccrualFactor The accrual factor of the total accrual period
   * @param notional The coupon notional
   * @param paymentAccrualFactors The accrual factors associated to the sub-periods
   * @param index The coupon Ibor index
   * @param fixingDates The coupon fixing dates
   * @param weights The weights for the index
   * @param fixingPeriodStartDates The start date of the fixing periods
   * @param fixingPeriodEndDates The end date of the fixing periods
   * @param fixingPeriodAccrualFactors The accrual factors of fixing periods
   * @param spread The spread
   */
  public CouponIborFlatCompoundingSpreadDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final double[] paymentAccrualFactors, final IborIndex index, final ZonedDateTime[][] fixingDates, final double[][] weights,
      final ZonedDateTime[][] fixingPeriodStartDates, final ZonedDateTime[][] fixingPeriodEndDates, final double[][] fixingPeriodAccrualFactors, final double spread) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional, paymentAccrualFactors, index, fixingDates, weights, fixingPeriodStartDates, fixingPeriodEndDates,
        fixingPeriodAccrualFactors);
    _spread = spread;
  }

  /**
   * Construct a coupon without start dates and end dates of fixing period
   * @param currency The coupon currency
   * @param paymentDate The coupon payment date.
   * @param accrualStartDate The start date of the accrual period
   * @param accrualEndDate The end date of the accrual period
   * @param paymentAccrualFactor The accrual factor of the total accrual period
   * @param notional The coupon notional
   * @param paymentAccrualFactors The accrual factors associated to the sub-periods
   * @param index The coupon Ibor index
   * @param fixingDates The coupon fixing dates
   * @param weights The weights for the index
   * @param iborCalendar The holiday calendar for the index
   * @param spread The spread
   * @return The coupon
   */
  public static CouponIborFlatCompoundingSpreadDefinition from(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final double[] paymentAccrualFactors, final IborIndex index, final ZonedDateTime[][] fixingDates, final double[][] weights,
      final Calendar iborCalendar, final double spread) {
    return new CouponIborFlatCompoundingSpreadDefinition(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional, paymentAccrualFactors, index, fixingDates, weights,
        iborCalendar, spread);
  }

  /**
   * Construct a coupon with full details
   * @param currency The coupon currency
   * @param paymentDate The coupon payment date.
   * @param accrualStartDate The start date of the accrual period
   * @param accrualEndDate The end date of the accrual period
   * @param paymentAccrualFactor The accrual factor of the total accrual period
   * @param notional The coupon notional
   * @param paymentAccrualFactors The accrual factors associated to the sub-periods
   * @param index The coupon Ibor index
   * @param fixingDates The coupon fixing dates
   * @param weights The weights for the index
   * @param fixingPeriodStartDates The start date of the fixing periods
   * @param fixingPeriodEndDates The end date of the fixing periods
   * @param fixingPeriodAccrualFactors The accrual factors of fixing periods
   * @param spread The spread
   * @return The coupon
   */
  public static CouponIborFlatCompoundingSpreadDefinition from(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final double[] paymentAccrualFactors, final IborIndex index, final ZonedDateTime[][] fixingDates, final double[][] weights,
      final ZonedDateTime[][] fixingPeriodStartDates, final ZonedDateTime[][] fixingPeriodEndDates, final double[][] fixingPeriodAccrualFactors, final double spread) {
    return new CouponIborFlatCompoundingSpreadDefinition(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional, paymentAccrualFactors, index, fixingDates, weights,
        fixingPeriodStartDates, fixingPeriodEndDates, fixingPeriodAccrualFactors, spread);
  }

  @Override
  public CouponIborFlatCompoundingSpreadDefinition withNotional(final double notional) {
    return new CouponIborFlatCompoundingSpreadDefinition(getCurrency(), getPaymentDate(), getAccrualStartDate(), getAccrualEndDate(), getPaymentYearFraction(), notional, getPaymentAccrualFactors(),
        getIndex(), getFixingDates(), getWeight(), getFixingPeriodStartDates(), getFixingPeriodEndDates(), getFixingPeriodAccrualFactor(), getSpread());
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborFlatCompoundingSpreadDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborFlatCompoundingSpreadDefinition(this);
  }

  /**
   * Gets the spread.
   * @return the spread
   */
  public double getSpread() {
    return _spread;
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Override
  @Deprecated
  public Coupon toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    return toDerivative(date);
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");

    final int nPeriods = getFixingDates().length;
    final int nDates = getFixingDates()[0].length; //number of fixing dates per period
    final LocalDate dayConversion = date.toLocalDate();

    ArgumentChecker.isTrue(!dayConversion.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");
    for (int i = 0; i < nPeriods; ++i) {
      for (int j = 0; j < nDates; ++j) {
        ArgumentChecker.isTrue(!dayConversion.isAfter(getFixingDates()[i][j].toLocalDate()), "Do not have any fixing data but are asking for a derivative at " + date
            + " which is after fixing date " + getFixingDates()[i][j]);
      }
    }
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());

    final double[][] fixingTime = new double[nPeriods][nDates];
    final double[][] fixingPeriodStartTime = new double[nPeriods][nDates];
    final double[][] fixingPeriodEndTime = new double[nPeriods][nDates];

    for (int i = 0; i < nPeriods; ++i) {
      for (int j = 0; j < nDates; ++j) {
        fixingTime[i][j] = TimeCalculator.getTimeBetween(date, getFixingDates()[i][j]);
        fixingPeriodStartTime[i][j] = TimeCalculator.getTimeBetween(date, getFixingPeriodStartDates()[i][j]);
        fixingPeriodEndTime[i][j] = TimeCalculator.getTimeBetween(date, getFixingPeriodEndDates()[i][j]);
      }
    }

    return new CouponIborFlatCompoundingSpread(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getPaymentAccrualFactors(), getIndex(), fixingTime, getWeight(),
        fixingPeriodStartTime, fixingPeriodEndTime, getFixingPeriodAccrualFactor(), getSpread());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_spread);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    if (!(obj instanceof CouponIborFlatCompoundingSpreadDefinition)) {
      return false;
    }
    CouponIborFlatCompoundingSpreadDefinition other = (CouponIborFlatCompoundingSpreadDefinition) obj;
    if (Double.doubleToLongBits(_spread) != Double.doubleToLongBits(other._spread)) {
      return false;
    }
    return true;
  }

}
