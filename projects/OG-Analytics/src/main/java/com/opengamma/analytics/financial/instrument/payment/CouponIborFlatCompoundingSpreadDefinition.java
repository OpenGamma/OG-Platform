/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborFlatCompoundingSpread;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.DoubleTimeSeries;
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
  public CouponIborFlatCompoundingSpread toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    return toDerivative(date);
  }

  @Override
  public CouponIborFlatCompoundingSpread toDerivative(final ZonedDateTime date) {
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
        fixingPeriodStartTime, fixingPeriodEndTime, getFixingPeriodAccrualFactor(), 0., 0., getSpread());
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

    int posPeriod = 0;
    int posDate = 0;
    double amountAccrued = 0.0;
    double sumRateFixed = 0.0;
    while (posPeriod < nPeriods && !(dayConversion.isBefore(getFixingDates()[posPeriod][0].toLocalDate()))) {
      sumRateFixed = 0.0;
      posDate = 0;
      while (posDate < nDates && dayConversion.isAfter(getFixingDates()[posPeriod][posDate].toLocalDate())) {
        final Double fixedRate = indexFixingTimeSeries.getValue(getFixingDates()[posPeriod][posDate]);
        if (fixedRate == null) {
          throw new OpenGammaRuntimeException("Could not get fixing value for date " + getFixingDates()[posPeriod][posDate]);
        }
        sumRateFixed += getWeight()[posPeriod][posDate] * fixedRate;
        ++posDate;
      }
      if (posDate < nDates && dayConversion.equals(getFixingDates()[posPeriod][posDate].toLocalDate())) {
        final Double fixedRate = indexFixingTimeSeries.getValue(getFixingDates()[posPeriod][posDate]);
        if (fixedRate != null) {
          sumRateFixed += getWeight()[posPeriod][posDate] * fixedRate;
          ++posDate;
        }
      }
      if (posDate == nDates) {
        final double unitCpa = (sumRateFixed + getSpread()) * getPaymentAccrualFactors()[posPeriod] + amountAccrued * getPaymentAccrualFactors()[posPeriod] * sumRateFixed;
        amountAccrued += unitCpa;
        sumRateFixed = 0.0;
      }
      ++posPeriod;
    }

    if (posPeriod == nPeriods && posDate == nDates) {
      final double rate = amountAccrued / getPaymentYearFraction();
      return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), rate, getAccrualStartDate(), getAccrualEndDate());
    }

    final int start = posDate != nDates ? 1 : 0;
    final int nPeriodsLeft = nPeriods - posPeriod + start; // include partially fixed period
    final int nDatesLeft = nDates - posDate; //can be 0
    final double[][] fixingTimeLeft = new double[nPeriodsLeft][nDates];
    final double[][] fixingPeriodStartTimeLeft = new double[nPeriodsLeft][nDates];
    final double[][] fixingPeriodEndTimeLeft = new double[nPeriodsLeft][nDates];
    final double[] paymentAccrualFactorsLeft = new double[nPeriodsLeft];
    System.arraycopy(getPaymentAccrualFactors(), posPeriod - start, paymentAccrualFactorsLeft, 0, nPeriodsLeft);
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
      System.arraycopy(getWeight()[i + posPeriod - start], 0, weightLeft[i], 0, nDates);
      System.arraycopy(getFixingPeriodAccrualFactor()[i + posPeriod - start], 0, fixingPeriodAccrualFactorLeft[i], 0, nDates);
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
      System.arraycopy(getWeight()[posPeriod - start], posDate, weightLeftIni, 0, nDatesLeft);
      System.arraycopy(getFixingPeriodAccrualFactor()[posPeriod - start], posDate, fixingPeriodAccrualFactorLeftIni, 0, nDatesLeft);
      weightLeft[0] = weightLeftIni;
      fixingPeriodAccrualFactorLeft[0] = fixingPeriodAccrualFactorLeftIni;
    }

    return new CouponIborFlatCompoundingSpread(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), paymentAccrualFactorsLeft, getIndex(), fixingTimeLeft, weightLeft,
        fixingPeriodStartTimeLeft, fixingPeriodEndTimeLeft, fixingPeriodAccrualFactorLeft, amountAccrued, sumRateFixed, getSpread());
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
