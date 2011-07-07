/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.varswap.definition;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.equity.varswap.derivative.VarianceSwap;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.TimeCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

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
  private final int _nObsExpected; // TODO Case 2011-07-01 : Who will count nObsExpected? 
  private final double _annualizationFactor;
  private final Calendar _calendar; // TODO Case 2011-07-01 : What do I do with the calendar??

  /**
   * Constructor based upon Vega (Volatility) parameterisation - strike and notional.
   * For a constructor based on Variance, please use fromVarianceParams().
   * For clarity, we recommend using fromVegaParams() instead of this constructor directly.
   *   
   * @param obsStartDate Date of first observation. Negative if observations have begun.
   * @param obsEndDate Date of final observation. Negative if observations have finished.
   * @param settlementDate Date of cash settlement. If negative, the swap has expired.
   * @param obsFreq The frequency of observations, typically DAILY
   * @param nObsExpected Number of observations expected as of trade inception
   * @param currency Currency of cash settlement
   * @param calendar Specification of good business days (and holidays)
   * @param volStrike Fair value of Volatility, the square root of Variance, struck at trade date
   * @param volNotional Trade pays the difference between realized and strike variance multiplied by 0.5 * volNotional / volStrike
   * @param annualizationFactor Number of business days per year
   */
  public VarianceSwapDefinition(ZonedDateTime obsStartDate, ZonedDateTime obsEndDate, ZonedDateTime settlementDate, PeriodFrequency obsFreq, int nObsExpected, Currency currency,
      Calendar calendar, double annualizationFactor, double volStrike, double volNotional) {

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
    _nObsExpected = nObsExpected;
    _annualizationFactor = annualizationFactor;
    _currency = currency;
    _calendar = calendar;
    _volStrike = volStrike;
    _volNotional = volNotional;
    _varStrike = volStrike * volStrike;
    _varNotional = 0.5 * volNotional / volStrike;
  }

  public VarianceSwapDefinition fromVegaParams(ZonedDateTime obsStartDate, ZonedDateTime obsEndDate, ZonedDateTime settlementDate, PeriodFrequency obsFreq, int nObsExpected, Currency currency,
      Calendar calendar, double annualizationFactor, double volStrike, double volNotional) {
    return new VarianceSwapDefinition(obsStartDate, obsEndDate, settlementDate, obsFreq, nObsExpected, currency, calendar, annualizationFactor, volStrike, volNotional);
  }

  public VarianceSwapDefinition fromVarianceParams(ZonedDateTime obsStartDate, ZonedDateTime obsEndDate, ZonedDateTime settlementDate, PeriodFrequency obsFreq, int nObsExpected, Currency currency,
      Calendar calendar, double annualizationFactor, double varStrike, double varNotional) {

    double volStrike = Math.sqrt(varStrike);
    double volNotional = 2 * varNotional * volStrike;

    return fromVegaParams(obsStartDate, obsEndDate, settlementDate, obsFreq, nObsExpected, currency, calendar, annualizationFactor, volStrike, volNotional);

  }

  /**
   * The definition is responsible for constructing the derivative for pricing visitors.
   * In particular,  it resolves calendars. The VarianceSwap needs an array of observations, as well as its *expected* length. 
   * The actual number of observations may be less than that expected at trade inception because of a market disruption event.
   * ( For an example of a market disruption event, see http://cfe.cboe.com/Products/Spec_VT.aspx )
   * @param date The current date
   * @param underlyingTimeSeries Time series of underlying observations
   * @return VarianceSwap derivative as of date
   */
  public VarianceSwap toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> underlyingTimeSeries) {
    Validate.notNull(date, "date");
    double timeToObsStart = TimeCalculator.getTimeBetween(date, _obsEndDate);
    double timeToObsEnd = TimeCalculator.getTimeBetween(date, _obsStartDate);
    double timeToSettlement = TimeCalculator.getTimeBetween(date, _settlementDate);
    Validate.isTrue(timeToObsStart < 0.08, "VarianceSwap needs update to properly handle forward starting variance swaps."
        + "Contact quant@opengamma.com. In the meantime, book as difference of two spot starting VarianceSwaps.");
    // FIXME CASE 2011-07-04 !!! Calendar. I need to know, from the _calendar, what number of observations to expect, both from [t,T] and [t,0], the latter to compute the number of market disruption events
    // FIXME CASE 2011-07-01 !!! Calendar. Treatment of nObsExpected. What I need is the number expected as of trade inception between _obsStartDate and date

    Validate.notNull(underlyingTimeSeries, "VarianceSwapDefinition has begun observations. A TimeSeries of observations must be provided.");
    DoubleTimeSeries<ZonedDateTime> realizedTS = underlyingTimeSeries.subSeries(_obsStartDate, true, date, true);
    double[] observations = realizedTS.toFastIntDoubleTimeSeries().valuesArrayFast();
    double[] observationWeights = {}; // TODO Case 2011-06-29 Calendar functionality for non-trivial weighting of observations
    final int nObsDisrupted = 0;

    VarianceSwap newDeriv = new VarianceSwap(timeToObsStart, timeToObsEnd, timeToSettlement,
        _varNotional, _annualizationFactor, _currency, _varStrike, _nObsExpected, nObsDisrupted, observations, observationWeights);
    return newDeriv;
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
