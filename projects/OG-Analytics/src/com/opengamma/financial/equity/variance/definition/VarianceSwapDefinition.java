/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.variance.definition;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.equity.variance.derivative.VarianceSwap;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.TimeCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * A Variance Swap is a forward contract on the realized variance of an underlying security. 
 * The floaing leg of a Variance Swap is the realized variance and is calculate using the second moment of log returns of the underlying asset
 */
public class VarianceSwapDefinition {

  private final Currency _currency;

  private final double _volStrike; // _varStrike := _volStrike^2 until we need something more elaborate 
  private final double _volNotional; // _varNotional := 0.5 * _volNotional / _volStrike. Provides a rough estimate of the payoff if volatility realizes 1 point above strike
  private final double _varStrike; // Computed internally
  private final double _varNotional; // Computed internally

  private final ZonedDateTime _obsStartDate;
  private final ZonedDateTime _obsEndDate;
  private final ZonedDateTime _settlementDate;
  private final PeriodFrequency _obsFreq;
  private final int _nObsExpected;
  private final double _annualizationFactor;
  private final Calendar _calendar;

  /**
   * Constructor based upon Vega (Volatility) parameterisation - strike and notional.
   * For a constructor based on Variance, please use fromVarianceParams().
   * For clarity, we recommend using fromVegaParams() instead of this constructor directly.
   *   
   * @param obsStartDate Date of first observation. Negative if observations have begun.
   * @param obsEndDate Date of final observation. Negative if observations have finished.
   * @param settlementDate Date of cash settlement. If negative, the swap has expired.
   * @param obsFreq The frequency of observations, typically DAILY
   * @param currency Currency of cash settlement
   * @param calendar Specification of good business days (and holidays)
   * @param annualizationFactor Number of business days per year
   * @param volStrike Fair value of Volatility, the square root of Variance, struck at trade date
   * @param volNotional Trade pays the difference between realized and strike variance multiplied by 0.5 * volNotional / volStrike
   */
  public VarianceSwapDefinition(ZonedDateTime obsStartDate, ZonedDateTime obsEndDate, ZonedDateTime settlementDate, PeriodFrequency obsFreq, Currency currency, Calendar calendar,
      double annualizationFactor, double volStrike, double volNotional) {

    Validate.notNull(obsStartDate, "obsStartDate");
    Validate.notNull(obsEndDate, "obsEndDate");
    Validate.notNull(settlementDate, "settlementDate");
    Validate.notNull(obsFreq, "obsFreq");
    Validate.notNull(currency, "currency");
    Validate.notNull(calendar, "calendar");

    _obsStartDate = obsStartDate;
    _obsEndDate = obsEndDate;
    _settlementDate = settlementDate;
    _obsFreq = obsFreq;
    Validate.isTrue(obsFreq == PeriodFrequency.DAILY, "Only DAILY observation frequencies are currently supported. obsFreq = " + obsFreq.toString()
        + ". Please contact quant to extend.");
    // TODO CASE Extend to periods longer than daily. Consider working this into ScheduleCalculator

    _currency = currency;
    _calendar = calendar;
    // Determine the number of observations expected as of trade inception.
    _nObsExpected = countGoodDays(_obsEndDate);

    _annualizationFactor = annualizationFactor;

    _volStrike = volStrike;
    _volNotional = volNotional;
    _varStrike = volStrike * volStrike;
    _varNotional = 0.5 * volNotional / volStrike;

  }

  /**
   * Given start date, frequency and calendar, count number of good business days up to and including upToThisDate.
   * This is only given a calendar, hence will not be aware if there was a market disruption, hence it provides an expected number
   * @param upToThiDate up to and including upToThisDate
   * @return number of business days between _obsStartDate and upToThisDate inclusive, spaced at _obsFreq 
   */
  private int countGoodDays(ZonedDateTime upToThiDate) {
    int nGood = 0;
    ZonedDateTime date = _obsStartDate;
    while (!date.isAfter(_obsEndDate)) {
      if (_calendar.isWorkingDay(date.toLocalDate())) {
        nGood++;
      }
      date = date.plus(_obsFreq.getPeriod());
    }
    return nGood;
  }

  public static VarianceSwapDefinition fromVegaParams(ZonedDateTime obsStartDate, ZonedDateTime obsEndDate, ZonedDateTime settlementDate, PeriodFrequency obsFreq, Currency currency,
      Calendar calendar, double annualizationFactor, double volStrike, double volNotional) {
    return new VarianceSwapDefinition(obsStartDate, obsEndDate, settlementDate, obsFreq, currency, calendar, annualizationFactor, volStrike, volNotional);
  }

  public static VarianceSwapDefinition fromVarianceParams(ZonedDateTime obsStartDate, ZonedDateTime obsEndDate, ZonedDateTime settlementDate, PeriodFrequency obsFreq,
      Currency currency, Calendar calendar, double annualizationFactor, double varStrike, double varNotional) {

    double volStrike = Math.sqrt(varStrike);
    double volNotional = 2 * varNotional * volStrike;

    return fromVegaParams(obsStartDate, obsEndDate, settlementDate, obsFreq, currency, calendar, annualizationFactor, volStrike, volNotional);

  }

  /**
   * The definition is responsible for constructing the derivative for pricing visitors.
   * In particular,  it resolves calendars. The VarianceSwap needs an array of observations, as well as its *expected* length. 
   * The actual number of observations may be less than that expected at trade inception because of a market disruption event.
   * ( For an example of a market disruption event, see http://cfe.cboe.com/Products/Spec_VT.aspx )
   * @param valueDate Date at which valuation will occur
   * @param underlyingTimeSeries Time series of underlying observations
   * @return VarianceSwap derivative as of date
   */
  public VarianceSwap toDerivative(final ZonedDateTime valueDate, final DoubleTimeSeries<LocalDate> underlyingTimeSeries) {
    Validate.notNull(valueDate, "date");
    double timeToObsStart = TimeCalculator.getTimeBetween(valueDate, _obsStartDate);
    double timeToObsEnd = TimeCalculator.getTimeBetween(valueDate, _obsEndDate);
    double timeToSettlement = TimeCalculator.getTimeBetween(valueDate, _settlementDate);
    
    Validate.notNull(underlyingTimeSeries, "A TimeSeries of observations must be provided. If observations have not begun, please pass an empty series.");
    DoubleTimeSeries<LocalDate> realizedTS = underlyingTimeSeries.subSeries(_obsStartDate.toLocalDate(), true, valueDate.toLocalDate(), false);
    double[] observations = realizedTS.toFastIntDoubleTimeSeries().valuesArrayFast();
    double[] observationWeights = {}; // TODO Case 2011-06-29 Calendar Add functionality for non-trivial weighting of observations
    final int nObsDisrupted = countGoodDays(valueDate) - observations.length;
    Validate.isTrue(nObsDisrupted >= 0, "Somehow we have more observations than we have good business days", nObsDisrupted);
    
    return new VarianceSwap(timeToObsStart, timeToObsEnd, timeToSettlement,
                                              _varStrike, _varNotional, _currency, _annualizationFactor,
                                              _nObsExpected, nObsDisrupted, observations, observationWeights);
  }

  /**
   * Gets the obsStartDate.
   * @return the obsStartDate
   */
  public ZonedDateTime getObsStartDate() {
    return _obsStartDate;
  }

  /**
   * Gets the obsEndDate.
   * @return the obsEndDate
   */
  public ZonedDateTime getObsEndDate() {
    return _obsEndDate;
  }

  /**
   * Gets the settlementDate.
   * @return the settlementDate
   */
  public ZonedDateTime getSettlementDate() {
    return _settlementDate;
  }

  public PeriodFrequency getObsFreq() {
    return _obsFreq;
  }

  /**
   * Gets the number of Observations Expected. This is the number of good business days as expected at trade inception.
   * The actual number of observations may be less if a market disruption event occurs. 
   * @return the nObsExpected
   */
  public int getObsExpected() {
    return _nObsExpected;
  }

  public Currency getCurrency() {
    return _currency;
  }

  public double getVolStrike() {
    return _volStrike;
  }

  public double getVolNotional() {
    return _volNotional;
  }

  public double getVarStrike() {
    return _varStrike;
  }

  public double getVarNotional() {
    return _varNotional;
  }

  public Calendar getCalendar() {
    return _calendar;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_currency == null) ? 0 : _currency.hashCode());
    result = prime * result + ((_obsEndDate == null) ? 0 : _obsEndDate.hashCode());
    result = prime * result + ((_obsStartDate == null) ? 0 : _obsStartDate.hashCode());
    result = prime * result + ((_settlementDate == null) ? 0 : _settlementDate.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_volNotional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_volStrike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    VarianceSwapDefinition other = (VarianceSwapDefinition) obj;
    if (_currency == null) {
      if (other._currency != null) {
        return false;
      }
    } else if (!_currency.equals(other._currency)) {
      return false;
    }
    if (_obsEndDate == null) {
      if (other._obsEndDate != null) {
        return false;
      }
    } else if (!_obsEndDate.equals(other._obsEndDate)) {
      return false;
    }
    if (_obsStartDate == null) {
      if (other._obsStartDate != null) {
        return false;
      }
    } else if (!_obsStartDate.equals(other._obsStartDate)) {
      return false;
    }
    if (_settlementDate == null) {
      if (other._settlementDate != null) {
        return false;
      }
    } else if (!_settlementDate.equals(other._settlementDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_volNotional) != Double.doubleToLongBits(other._volNotional)) {
      return false;
    }
    if (Double.doubleToLongBits(_volStrike) != Double.doubleToLongBits(other._volStrike)) {
      return false;
    }
    return true;
  }

}
