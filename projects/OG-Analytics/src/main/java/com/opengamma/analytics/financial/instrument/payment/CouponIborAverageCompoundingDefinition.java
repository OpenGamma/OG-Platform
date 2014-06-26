/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import java.util.Arrays;

import org.apache.commons.lang.NotImplementedException;
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
 * Class describing an average coupon by weighted mean of index values with difference fixing dates. 
 * The weighted averages over several sub-periods are compounded over the total period.
 */
public class CouponIborAverageCompoundingDefinition extends CouponDefinition implements InstrumentDefinitionWithData<Payment, DoubleTimeSeries<ZonedDateTime>> {

  /** The index on which the fixing is done. The same index is used for all the fixings. */
  private final IborIndex _index;
  /** The fixing dates of the index. The dates are in increasing order. */
  private final ZonedDateTime[][] _fixingDates;
  /** The weights or quantity used for each fixing. The total weight is not necessarily 1. Same size as _fixingDate. */
  private final double[][] _weights;
  /** The start dates of the underlying index period. Same size as _fixingDate. */
  private final ZonedDateTime[][] _fixingPeriodStartDates;
  /** The end dates of the underlying index period. Same size as _fixingDate. */
  private final ZonedDateTime[][] _fixingPeriodEndDates;
  /** The index periods accrual factors. Same size as _fixingDate. */
  private final double[][] _fixingPeriodAccrualFactors;
  /** The payment accrual factors for the different sub-periods on which the compounding is computed. */
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
    final int[] nDates = new int[nPeriods]; // Number of fixing dates per sub-period, can be different for each sub-period.
    ArgumentChecker.isTrue(nPeriods == weights.length, "weights length different from fixingDate length");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "index currency different from payment currency");
    _weights = new double[nPeriods][];
    _fixingDates = new ZonedDateTime[nPeriods][];
    _fixingPeriodStartDates = new ZonedDateTime[nPeriods][];
    _fixingPeriodEndDates = new ZonedDateTime[nPeriods][];
    _fixingPeriodAccrualFactors = new double[nPeriods][];
    _index = index;
    _paymentAccrualFactors = paymentAccrualFactors.clone();
    for (int i = 0; i < nPeriods; ++i) {
      nDates[i] = fixingDates[i].length;
      _weights[i] = weights[i].clone();
      _fixingDates[i] = fixingDates[i].clone();
      _fixingPeriodStartDates[i] = new ZonedDateTime[nDates[i]];
      _fixingPeriodEndDates[i] = new ZonedDateTime[nDates[i]];
      _fixingPeriodAccrualFactors[i] = new double[nDates[i]];
      for (int j = 0; j < nDates[i]; ++j) {
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
    ArgumentChecker.isTrue(nPeriods == weights.length, "weights length different from fixingDate length");
    ArgumentChecker.isTrue(nPeriods == fixingPeriodStartDates.length, "fixingPeriodStartDates length different from fixingDate length");
    ArgumentChecker.isTrue(nPeriods == fixingPeriodEndDates.length, "fixingPeriodEndDates length different from fixingDate length");
    ArgumentChecker.isTrue(nPeriods == fixingPeriodAccrualFactors.length, "fixingPeriodAccrualFactors length different from fixingDate length");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "index currency different from payment currency");
    _weights = new double[nPeriods][];
    _fixingDates = new ZonedDateTime[nPeriods][];
    _fixingPeriodStartDates = new ZonedDateTime[nPeriods][];
    _fixingPeriodEndDates = new ZonedDateTime[nPeriods][];
    _fixingPeriodAccrualFactors = new double[nPeriods][];
    _index = index;
    _paymentAccrualFactors = paymentAccrualFactors.clone();
    for (int i = 0; i < nPeriods; ++i) {
      _weights[i] = weights[i].clone();
      _fixingDates[i] = fixingDates[i].clone();
      _fixingPeriodStartDates[i] = fixingPeriodStartDates[i].clone();
      _fixingPeriodEndDates[i] = fixingPeriodEndDates[i].clone();
      _fixingPeriodAccrualFactors[i] = fixingPeriodAccrualFactors[i].clone();
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

  @Override
  public CouponIborAverageCompounding toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    final int nPeriods = getFixingDates().length;
    final int[] nDates = new int[nPeriods]; // Number of fixing dates per sub-period, can be different for each sub-period.
    final LocalDate dayConversion = date.toLocalDate();
    ArgumentChecker.isTrue(!dayConversion.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");
    ArgumentChecker.isTrue(!dayConversion.isAfter(getFixingDates()[0][0].toLocalDate()), "Do not have any fixing data but are asking for a derivative at " + date
        + " which is after fixing date " + getFixingDates()[0][0]);
    // Fixing dates are in increasing order; only the first one need to be checked.
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final double[][] fixingTime = new double[nPeriods][];
    final double[][] fixingPeriodStartTime = new double[nPeriods][];
    final double[][] fixingPeriodEndTime = new double[nPeriods][];
    for (int i = 0; i < nPeriods; ++i) {
      nDates[i] = getFixingDates()[i].length;
      fixingTime[i] = new double[nDates[i]];
      fixingPeriodStartTime[i] = new double[nDates[i]];
      fixingPeriodEndTime[i] = new double[nDates[i]];
      for (int j = 0; j < nDates[i]; ++j) {
        fixingTime[i][j] = TimeCalculator.getTimeBetween(date, getFixingDates()[i][j]);
        fixingPeriodStartTime[i][j] = TimeCalculator.getTimeBetween(date, getFixingPeriodStartDates()[i][j]);
        fixingPeriodEndTime[i][j] = TimeCalculator.getTimeBetween(date, getFixingPeriodEndDates()[i][j]);
      }
    }
    return new CouponIborAverageCompounding(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getPaymentAccrualFactors(), getIndex(), fixingTime, getWeight(),
        fixingPeriodStartTime, fixingPeriodEndTime, getFixingPeriodAccrualFactor(), 0., 0.);
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

    int posPeriod = 0;
    int posDate = 0;
    double amountAccrued = 1.;
    double sumRateFixed = 0.0;
    while (posPeriod < nPeriods && !(dayConversion.isBefore(getFixingDates()[posPeriod][0].toLocalDate()))) {
      sumRateFixed = 0.0;
      posDate = 0;
      while (posDate < nDates && dayConversion.isAfter(getFixingDates()[posPeriod][posDate].toLocalDate())) {
        final Double fixedRate = indexFixingTimeSeries.getValue(_fixingDates[posPeriod][posDate]);
        if (fixedRate == null) {
          throw new OpenGammaRuntimeException("Could not get fixing value for date " + getFixingDates()[posPeriod][posDate]);
        }
        sumRateFixed += getWeight()[posPeriod][posDate] * fixedRate;
        ++posDate;
      }
      if (posDate < nDates && dayConversion.equals(getFixingDates()[posPeriod][posDate].toLocalDate())) {
        final Double fixedRate = indexFixingTimeSeries.getValue(_fixingDates[posPeriod][posDate]);
        if (fixedRate != null) {
          sumRateFixed += getWeight()[posPeriod][posDate] * fixedRate;
          ++posDate;
        }
      }
      if (posDate == nDates) {
        amountAccrued *= (1.0 + sumRateFixed * getPaymentAccrualFactors()[posPeriod]);
        sumRateFixed = 0.0;
      }
      ++posPeriod;
    }

    if (posPeriod == nPeriods && posDate == nDates) {
      final double rate = (amountAccrued - 1.0) / getPaymentYearFraction();
      return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), rate, getAccrualStartDate(), getAccrualEndDate());
    }

    final int start = posDate != nDates ? 1 : 0;
    final int nPeriodsLeft = nPeriods - posPeriod + start; // include partially fixed period
    final int nDatesLeft = nDates - posDate; //can be 0
    final double[][] fixingTimeLeft = new double[nPeriodsLeft][nDates];
    final double[][] fixingPeriodStartTimeLeft = new double[nPeriodsLeft][nDates];
    final double[][] fixingPeriodEndTimeLeft = new double[nPeriodsLeft][nDates];
    final double[] paymentAccrualFactorsLeft = new double[nPeriodsLeft];
    System.arraycopy(_paymentAccrualFactors, posPeriod - start, paymentAccrualFactorsLeft, 0, nPeriodsLeft);
    for (int i = start; i < nPeriodsLeft; ++i) {
      for (int j = 0; j < nDates; ++j) {
        fixingTimeLeft[i][j] = TimeCalculator.getTimeBetween(dateTime, getFixingDates()[i + posPeriod - start][j]);
        fixingPeriodStartTimeLeft[i][j] = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodStartDates()[i + posPeriod - start][j]);
        fixingPeriodEndTimeLeft[i][j] = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodEndDates()[i + posPeriod - start][j]);
      }
    }

    final double[][] weightLeft = new double[nPeriodsLeft][nDates];
    final double[][] fixingPeriodAccrualFactorLeft = new double[nPeriodsLeft][nDates];
    for (int i = start; i < nPeriodsLeft; ++i) {
      System.arraycopy(_weights[i + posPeriod - start], 0, weightLeft[i], 0, nDates);
      System.arraycopy(_fixingPeriodAccrualFactors[i + posPeriod - start], 0, fixingPeriodAccrualFactorLeft[i], 0, nDates);
    }

    if (posDate != nDates) {
      final double[] fixingTimeLeftIni = new double[nDatesLeft];
      final double[] fixingPeriodStartTimeLeftIni = new double[nDatesLeft];
      final double[] fixingPeriodEndTimeLeftIni = new double[nDatesLeft];
      final double[] weightLeftIni = new double[nDatesLeft];
      final double[] fixingPeriodAccrualFactorLeftIni = new double[nDatesLeft];
      for (int j = 0; j < nDatesLeft; ++j) {
        fixingTimeLeftIni[j] = TimeCalculator.getTimeBetween(dateTime, getFixingDates()[posPeriod - start][j + posDate]);
        fixingPeriodStartTimeLeftIni[j] = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodStartDates()[posPeriod - start][j + posDate]);
        fixingPeriodEndTimeLeftIni[j] = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodEndDates()[posPeriod - start][j + posDate]);
      }
      fixingTimeLeft[0] = fixingTimeLeftIni;
      fixingPeriodStartTimeLeft[0] = fixingPeriodStartTimeLeftIni;
      fixingPeriodEndTimeLeft[0] = fixingPeriodEndTimeLeftIni;
      System.arraycopy(_weights[posPeriod - start], posDate, weightLeftIni, 0, nDatesLeft);
      System.arraycopy(_fixingPeriodAccrualFactors[posPeriod - start], posDate, fixingPeriodAccrualFactorLeftIni, 0, nDatesLeft);
      weightLeft[0] = weightLeftIni;
      fixingPeriodAccrualFactorLeft[0] = fixingPeriodAccrualFactorLeftIni;
    }

    return new CouponIborAverageCompounding(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), paymentAccrualFactorsLeft, getIndex(), fixingTimeLeft, weightLeft,
        fixingPeriodStartTimeLeft, fixingPeriodEndTimeLeft, fixingPeriodAccrualFactorLeft, amountAccrued, sumRateFixed);
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Override
  @Deprecated
  public Coupon toDerivative(ZonedDateTime date, DoubleTimeSeries<ZonedDateTime> data, String... yieldCurveNames) {
    throw new NotImplementedException("toDerivative not implemented with yield curve names.");
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Override
  @Deprecated
  public CouponIborAverageCompounding toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    throw new NotImplementedException("toDerivative not implemented with yield curve names.");
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
