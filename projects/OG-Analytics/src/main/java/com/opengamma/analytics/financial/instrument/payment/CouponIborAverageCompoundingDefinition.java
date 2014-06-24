/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import java.util.Arrays;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverageCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class CouponIborAverageCompoundingDefinition extends CouponDefinition implements InstrumentDefinitionWithData<Payment, DoubleTimeSeries<ZonedDateTime>> {

  private final IborIndex _index;
  private final double[][] _weights;
  private final ZonedDateTime[][] _fixingDates;
  private final ZonedDateTime[][] _fixingPeriodStartDates;
  private final ZonedDateTime[][] _fixingPeriodEndDates;

  private final double[][] _fixingPeriodAccrualFactors;
  private final double[] _paymentAccrualFactors;

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
   */
  public CouponIborAverageCompoundingDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final double[] paymentAccrualFactors, final IborIndex index, final ZonedDateTime[][] fixingDates, final double[][] weights,
      final Calendar iborCalendar) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional);

    final int nPeriods = fixingDates.length;
    final int nDates = fixingDates[0].length; //number of fixing dates per period
    ArgumentChecker.isTrue(nPeriods == weights.length, "weights length different from fixingDate length");
    ArgumentChecker.isTrue(nDates == weights[0].length, "weights length different from fixingDate length");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "index currency different from payment currency");

    _weights = new double[nPeriods][nDates];
    _fixingDates = new ZonedDateTime[nPeriods][nDates];
    _fixingPeriodStartDates = new ZonedDateTime[nPeriods][nDates];
    _fixingPeriodEndDates = new ZonedDateTime[nPeriods][nDates];
    _fixingPeriodAccrualFactors = new double[nPeriods][nDates];

    _index = index;
    _paymentAccrualFactors = Arrays.copyOf(paymentAccrualFactors, nPeriods);
    for (int i = 0; i < nPeriods; ++i) {
      System.arraycopy(weights[i], 0, _weights[i], 0, nDates);
      System.arraycopy(fixingDates[i], 0, _fixingDates[i], 0, nDates);
      for (int j = 0; j < nDates; ++j) {
        _fixingPeriodStartDates[i][j] = ScheduleCalculator.getAdjustedDate(fixingDates[i][j], index.getSpotLag(), iborCalendar);
        _fixingPeriodEndDates[i][j] = ScheduleCalculator.getAdjustedDate(_fixingPeriodStartDates[i][j], index.getTenor(), index.getBusinessDayConvention(), iborCalendar, index.isEndOfMonth());
        _fixingPeriodAccrualFactors[i][j] = index.getDayCount().getDayCountFraction(_fixingPeriodStartDates[i][j], _fixingPeriodEndDates[i][j], iborCalendar);
      }
    }
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
   */
  public CouponIborAverageCompoundingDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final double[] paymentAccrualFactors, final IborIndex index, final ZonedDateTime[][] fixingDates, final double[][] weights,
      final ZonedDateTime[][] fixingPeriodStartDates, final ZonedDateTime[][] fixingPeriodEndDates, final double[][] fixingPeriodAccrualFactors) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional);

    final int nPeriods = fixingDates.length;
    final int nDates = fixingDates[0].length; //number of fixing dates per period
    ArgumentChecker.isTrue(nPeriods == weights.length, "weights length different from fixingDate length");
    ArgumentChecker.isTrue(nDates == weights[0].length, "weights length different from fixingDate length");
    ArgumentChecker.isTrue(nPeriods == fixingPeriodStartDates.length, "fixingPeriodStartDates length different from fixingDate length");
    ArgumentChecker.isTrue(nDates == fixingPeriodStartDates[0].length, "fixingPeriodStartDates length different from fixingDate length");
    ArgumentChecker.isTrue(nPeriods == fixingPeriodEndDates.length, "fixingPeriodEndDates length different from fixingDate length");
    ArgumentChecker.isTrue(nDates == fixingPeriodEndDates[0].length, "fixingPeriodEndDates length different from fixingDate length");
    ArgumentChecker.isTrue(nPeriods == fixingPeriodAccrualFactors.length, "fixingPeriodAccrualFactors length different from fixingDate length");
    ArgumentChecker.isTrue(nDates == fixingPeriodAccrualFactors[0].length, "fixingPeriodAccrualFactors length different from fixingDate length");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "index currency different from payment currency");

    _weights = new double[nPeriods][nDates];
    _fixingDates = new ZonedDateTime[nPeriods][nDates];
    _fixingPeriodStartDates = new ZonedDateTime[nPeriods][nDates];
    _fixingPeriodEndDates = new ZonedDateTime[nPeriods][nDates];
    _fixingPeriodAccrualFactors = new double[nPeriods][nDates];

    _index = index;
    _paymentAccrualFactors = Arrays.copyOf(paymentAccrualFactors, nPeriods);
    for (int i = 0; i < nPeriods; ++i) {
      System.arraycopy(weights[i], 0, _weights[i], 0, nDates);
      System.arraycopy(fixingDates[i], 0, _fixingDates[i], 0, nDates);
      System.arraycopy(fixingPeriodStartDates[i], 0, _fixingPeriodStartDates[i], 0, nDates);
      System.arraycopy(fixingPeriodEndDates[i], 0, _fixingPeriodEndDates[i], 0, nDates);
      System.arraycopy(fixingPeriodAccrualFactors[i], 0, _fixingPeriodAccrualFactors[i], 0, nDates);
    }
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
   * @return The coupon
   */
  public static CouponIborAverageCompoundingDefinition from(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final double[] paymentAccrualFactors, final IborIndex index, final ZonedDateTime[][] fixingDates, final double[][] weights,
      final Calendar iborCalendar) {
    return new CouponIborAverageCompoundingDefinition(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional, paymentAccrualFactors, index, fixingDates, weights,
        iborCalendar);
  }

  /**
   * Construct coupon with full details
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
   * @return The coupon
   */
  public static CouponIborAverageCompoundingDefinition from(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final double[] paymentAccrualFactors, final IborIndex index, final ZonedDateTime[][] fixingDates, final double[][] weights,
      final ZonedDateTime[][] fixingPeriodStartDates, final ZonedDateTime[][] fixingPeriodEndDates, final double[][] fixingPeriodAccrualFactors) {
    return new CouponIborAverageCompoundingDefinition(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional, paymentAccrualFactors, index, fixingDates, weights,
        fixingPeriodStartDates, fixingPeriodEndDates, fixingPeriodAccrualFactors);
  }

  /**
   * Construct a new coupon with the same detail except notional
   * @param notional The notional
   * @return The coupon
   */
  public CouponIborAverageCompoundingDefinition withNotional(final double notional) {
    return new CouponIborAverageCompoundingDefinition(getCurrency(), getPaymentDate(), getAccrualStartDate(), getAccrualEndDate(), getPaymentYearFraction(), notional, getPaymentAccrualFactors(),
        getIndex(), getFixingDates(), getWeight(), getFixingPeriodStartDates(), getFixingPeriodEndDates(), getFixingPeriodAccrualFactor());
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborAverageCompoundingDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborAverageCompoundingDefinition(this);
  }

  /**
   * Gets the index.
   * @return the index
   */
  public IborIndex getIndex() {
    return _index;
  }

  /**
   * Gets the paymentAccrualFactors.
   * @return the paymentAccrualFactors
   */
  public double[] getPaymentAccrualFactors() {
    return _paymentAccrualFactors;
  }

  /**
   * Gets the weight.
   * @return the weight
   */
  public double[][] getWeight() {
    return _weights;
  }

  /**
   * Gets the fixingPeriodStartDates.
   * @return the fixingPeriodStartDates
   */
  public ZonedDateTime[][] getFixingPeriodStartDates() {
    return _fixingPeriodStartDates;
  }

  /**
   * Gets the fixingPeriodEndDates.
   * @return the fixingPeriodEndDates
   */
  public ZonedDateTime[][] getFixingPeriodEndDates() {
    return _fixingPeriodEndDates;
  }

  /**
   * Gets the fixingDates.
   * @return the fixingDates
   */
  public ZonedDateTime[][] getFixingDates() {
    return _fixingDates;
  }

  /**
   * Gets the fixingPeriodAccrualFactor.
   * @return the fixingPeriodAccrualFactor
   */
  public double[][] getFixingPeriodAccrualFactor() {
    return _fixingPeriodAccrualFactors;
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Override
  @Deprecated
  public CouponIborAverageCompounding toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    return toDerivative(date);
  }

  @Override
  public CouponIborAverageCompounding toDerivative(final ZonedDateTime date) {
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

    return new CouponIborAverageCompounding(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getPaymentAccrualFactors(), getIndex(), fixingTime, getWeight(),
        fixingPeriodStartTime, fixingPeriodEndTime, getFixingPeriodAccrualFactor(), 0.);
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Override
  @Deprecated
  public Coupon toDerivative(ZonedDateTime date, DoubleTimeSeries<ZonedDateTime> data, String... yieldCurveNames) {
    return toDerivative(date, data);
  }

  @Override
  public Coupon toDerivative(ZonedDateTime dateTime, DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries) {
    final LocalDate dateConversion = dateTime.toLocalDate();
    ArgumentChecker.notNull(indexFixingTimeSeries, "Index fixing time series");
    ArgumentChecker.isTrue(!dateConversion.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");

    final int nPeriods = getFixingDates().length;
    final int nDates = getFixingDates()[0].length; //number of fixing dates per period
    final LocalDate dayConversion = dateTime.toLocalDate();

    final double paymentTime = TimeCalculator.getTimeBetween(dateTime, getPaymentDate());

    if (dayConversion.isBefore(getFixingDates()[0][0].toLocalDate())) {
      return toDerivative(dateTime);
    }

    int position = 0;
    double amountAccrued = 1.;
    while (position < nPeriods && !(dayConversion.isBefore(getFixingDates()[position][nDates - 1].toLocalDate()))) {
      double tmp = 0.0;
      for (int i = 0; i < nDates; ++i) {
        final Double fixedRate = indexFixingTimeSeries.getValue(_fixingDates[position][i]);
        if (fixedRate == null) {
          throw new OpenGammaRuntimeException("Could not get fixing value for date " + getFixingDates()[position][i]);
        }
        tmp += getWeight()[position][i] * fixedRate;
      }
      amountAccrued *= (1.0 + tmp * getPaymentAccrualFactors()[position]);
      ++position;
    }

    if (position == nPeriods) {
      final double rate = (amountAccrued - 1.0) / getPaymentYearFraction();
      return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), rate, getAccrualStartDate(), getAccrualEndDate());
    }

    final int nPeriodsLeft = nPeriods - position;
    final double[][] fixingTime = new double[nPeriodsLeft][nDates];
    final double[][] fixingPeriodStartTime = new double[nPeriodsLeft][nDates];
    final double[][] fixingPeriodEndTime = new double[nPeriodsLeft][nDates];
    for (int i = 0; i < nPeriodsLeft; ++i) {
      for (int j = 0; j < nDates; ++j) {
        fixingTime[i][j] = TimeCalculator.getTimeBetween(dateTime, getFixingDates()[i][j]);
        fixingPeriodStartTime[i][j] = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodStartDates()[i][j]);
        fixingPeriodEndTime[i][j] = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodEndDates()[i][j]);
      }
    }

    final double[][] weightLeft = new double[nPeriodsLeft][nDates];
    final double[][] fixingPeriodAccrualFactorLeft = new double[nPeriodsLeft][nDates];

    for (int i = 0; i < nPeriodsLeft; ++i) {
      System.arraycopy(_weights[i + position], 0, weightLeft[i], 0, nPeriodsLeft);
      System.arraycopy(_fixingPeriodAccrualFactors[i + position], 0, fixingPeriodAccrualFactorLeft[i], 0, nPeriodsLeft);
    }

    return new CouponIborAverageCompounding(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getPaymentAccrualFactors(), getIndex(), fixingTime, weightLeft,
        fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodAccrualFactorLeft, amountAccrued);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.deepHashCode(_fixingDates);
    result = prime * result + Arrays.deepHashCode(_fixingPeriodAccrualFactors);
    result = prime * result + Arrays.deepHashCode(_fixingPeriodEndDates);
    result = prime * result + Arrays.deepHashCode(_fixingPeriodStartDates);
    result = prime * result + ((_index == null) ? 0 : _index.hashCode());
    result = prime * result + Arrays.hashCode(_paymentAccrualFactors);
    result = prime * result + Arrays.deepHashCode(_weights);
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
    if (!(obj instanceof CouponIborAverageCompoundingDefinition)) {
      return false;
    }
    CouponIborAverageCompoundingDefinition other = (CouponIborAverageCompoundingDefinition) obj;
    if (!Arrays.deepEquals(_fixingDates, other._fixingDates)) {
      return false;
    }
    if (!Arrays.deepEquals(_fixingPeriodAccrualFactors, other._fixingPeriodAccrualFactors)) {
      return false;
    }
    if (!Arrays.deepEquals(_fixingPeriodEndDates, other._fixingPeriodEndDates)) {
      return false;
    }
    if (!Arrays.deepEquals(_fixingPeriodStartDates, other._fixingPeriodStartDates)) {
      return false;
    }
    if (_index == null) {
      if (other._index != null) {
        return false;
      }
    } else if (!_index.equals(other._index)) {
      return false;
    }
    if (!Arrays.equals(_paymentAccrualFactors, other._paymentAccrualFactors)) {
      return false;
    }
    if (!Arrays.deepEquals(_weights, other._weights)) {
      return false;
    }
    return true;
  }

}
