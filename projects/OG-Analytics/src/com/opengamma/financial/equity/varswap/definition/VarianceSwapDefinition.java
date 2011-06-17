/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.varswap.definition;

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
  private final double _volNotional; // _varNotional := 0.5 * _volNotional / _volStrike. Provides a rough estimate of the payoff if vol realizes 1 point above strike

  private final double _varStrike; // Computed internally
  private final double _varNotional; // Computed internally

  private final ZonedDateTime _obsStartDate;
  private final ZonedDateTime _obsEndDate;
  private final ZonedDateTime _settlementDate;
  private final PeriodFrequency _obsFreq;
  private final int _nObsExpected;

  // TODO Case 2011.06.07 -   private final HolidaySource _holidaySource; ?!?

  /**
   * Constructor based upon a Volatility or Vega parameterization - strike and notional.
   *
   * TODO Case 2011.06.07 - Shall we add another for a Variance parameterization? To be clear, constructors called 'fromVegaParams' and 'fromVarianceParams'
   *   
   * @param obsStartDate Date of first observation. Negative if observations have begun.
   * @param obsEndDate Date of final observation. Negative if observations have finished.
   * @param settlementDate Date of cash settlement. If negative, the swap has expired.
   * @param obsFreq The frequency of observations, typically DAILY
   * @param nObsExpected Number of observations expected as of trade inception
   * @param currency Currency of cash settlement
   * @param volStrike Fair value of Volatility, the square root of Variance, struck at trade date
   * @param volNotional Trade pays the difference between realized and strike variance multiplied by 0.5 * volNotional / volStrike
   */
  public VarianceSwapDefinition(ZonedDateTime obsStartDate, ZonedDateTime obsEndDate, ZonedDateTime settlementDate, PeriodFrequency obsFreq, int nObsExpected, Currency currency, double volStrike,
      double volNotional) {
    _obsStartDate = obsStartDate;
    _obsEndDate = obsEndDate;
    _settlementDate = settlementDate;
    _obsFreq = obsFreq;
    _nObsExpected = nObsExpected;
    _currency = currency;
    _volStrike = volStrike;
    _volNotional = volNotional;

    _varStrike = volStrike * volStrike; // TODO Case 2011.06.07 -Might be simpler to simply have a getter that computes this 
    _varNotional = 0.5 * volNotional / volStrike; // Same
  }

  public VarianceSwap toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> obs) {
    Validate.notNull(date, "date");
    double timeToObsStart = TimeCalculator.getTimeBetween(date, _obsEndDate);
    double timeToObsEnd = TimeCalculator.getTimeBetween(date, _obsStartDate);
    double timeToSettlement = TimeCalculator.getTimeBetween(date, _settlementDate);

    // FIXME Case 2011.06.07 - This assumes that the trade is forward starting
    if (timeToObsStart > 0) {
      // obs.subSeries(...).valuesArray() will give the realized values. Validate to ensure not more frequent than daily

      Validate.notNull(obs, "VarianceSwapDefinition has begun observations. A TimeSeries of observations must be provided.");
    }
    Validate.isTrue(timeToObsStart > 0, "VarianceSwapDefinition has begun observations. Only forward start is handled. Contact Quant");
    int nObsRemaining = _nObsExpected;
    int nObsActObs = 0;

    VarianceSwap newDeriv = new VarianceSwap(timeToObsStart, timeToObsEnd, timeToSettlement,
        _nObsExpected, nObsRemaining, nObsActObs, _currency, _varStrike, _varNotional);
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

  /**
   * Gets the obsFreq.
   * @return the obsFreq
   */
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

  /**
   * Gets the currency.
   * @return the currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Gets the volStrike.
   * @return the volStrike
   */
  public double getVolStrike() {
    return _volStrike;
  }

  /**
   * Gets the volNotional.
   * @return the volNotional
   */
  public double getVolNotional() {
    return _volNotional;
  }

  /**
   * Gets the varStrike.
   * @return the varStrike
   */
  public double getVarStrike() {
    return _varStrike;
  }

  /**
   * Gets the varNotional.
   * @return the varNotional
   */
  public double getVarNotional() {
    return _varNotional;
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
