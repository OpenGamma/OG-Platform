/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance.derivative;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.equity.EquityDerivative;
import com.opengamma.analytics.financial.equity.EquityDerivativeVisitor;
import com.opengamma.util.money.Currency;

/**
 * A Variance Swap is a forward contract on the realized variance of an underlying security. 
 * The floating leg of a Variance Swap is the realized variance and is calculate using the second moment of log returns of the underlying asset
 * 
 * Because variance is additive in time, the value of a VarianceSwap can be decomposed at any point in time between realized and implied variance as
 * _varNotional * Z(t,T) * [ t/T * RealizedVol(0,t)^2 + (T-t)/T * ImpliedVol(t,T)^2 - volStrike^2 ] 
 */
public class VarianceSwap implements EquityDerivative {
  private final double _timeToObsStart;
  private final double _timeToObsEnd;
  private final double _timeToSettlement;

  private final double _varStrike; // volStrike^2 
  private final double _varNotional; // := 0.5 * _volNotional / _volStrike
  private final Currency _currency;
  private final double _annualizationFactor; // typically 252 with daily observations

  private final int _nObsExpected;
  private final int _nObsDisrupted;
  private final double[] _observations;
  private final double[] _observationWeights;

  /**
   * @param timeToObsStart Time of first observation. Negative if observations have begun.
   * @param timeToObsEnd Time of final observation. Negative if observations have finished.
   * @param timeToSettlement Time of cash settlement. If negative, the swap has expired.
   * @param varStrike Fair value of Variance struck at trade date
   * @param varNotional Trade pays the difference between realized and strike variance multiplied by this
   * @param currency Currency of cash settlement
   * @param annualizationFactor Number of business days per year
   * @param nObsExpected Number of observations expected as of trade inception
   * @param nObsDisrupted Number of expected observations that did not occur because of a market disruption
   * @param observations Array of observations of the underlying spot
   * @param observationWeights Array of weights to give observation returns. If null, all weights are 1. Else, length must be: observations.length-1
   */
  public VarianceSwap(double timeToObsStart, double timeToObsEnd, double timeToSettlement,
                      double varStrike, double varNotional, Currency currency, double annualizationFactor,
                      int nObsExpected, int nObsDisrupted, double[] observations, double[] observationWeights) {

    _timeToObsStart = timeToObsStart;
    _timeToObsEnd = timeToObsEnd;
    _timeToSettlement = timeToSettlement;
    _varStrike = varStrike;
    _varNotional = varNotional;
    _currency = currency;
    _annualizationFactor = annualizationFactor;
    _nObsExpected = nObsExpected;
    _nObsDisrupted = nObsDisrupted;
    _observations = observations;
    _observationWeights = observationWeights;
    if (_observationWeights.length > 1) {
      int nWeights = _observationWeights.length;
      int nObs = _observations.length;
      Validate.isTrue(nWeights + 1 == nObs,
            "If provided, observationWeights must be of length one less than observations, as they weight returns log(obs[i]/obs[i-1])."
                + " Found " + nWeights + " weights and " + nObs + " observations.");
    }

    Validate.isTrue(_nObsExpected > 0, "Encountered a VarianceSwap with 0 nObsExpected! "
        + "If it is impractical to count, contact Quant to default this value in VarianceSwap constructor.");
  }

  @Override
  public <S, T> T accept(EquityDerivativeVisitor<S, T> visitor, S data) {
    return null;
  }

  @Override
  public <T> T accept(EquityDerivativeVisitor<?, T> visitor) {
    return null;
  }

  /**
   * Gets the timeToObsStart.
   * @return the timeToObsStart
   */
  public double getTimeToObsStart() {
    return _timeToObsStart;
  }

  /**
   * Gets the timeToObsEnd.
   * @return the timeToObsEnd
   */
  public double getTimeToObsEnd() {
    return _timeToObsEnd;
  }

  /**
   * Gets the timeToSettlement.
   * @return the timeToSettlement
   */
  public double getTimeToSettlement() {
    return _timeToSettlement;
  }

  /**
   * Gets the nObsExpected.
   * @return the nObsExpected
   */
  public int getObsExpected() {
    return _nObsExpected;
  }

  /**
   * Gets the nObsDisrupted.
   * @return the nObsDisrupted
   */
  public int getObsDisrupted() {
    return _nObsDisrupted;
  }

  /**
   * Gets the currency.
   * @return the currency
   */
  public Currency getCurrency() {
    return _currency;
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

  public double getVolStrike() {
    return Math.sqrt(_varStrike);
  }

  public double getVolNotional() {
    return _varNotional * 2 * Math.sqrt(_varStrike);
  }

  /**
   * Gets the annualizationFactor.
   * @return the annualizationFactor
   */
  public double getAnnualizationFactor() {
    return _annualizationFactor;
  }

  /**
   * Gets the observations.
   * @return the observations
   */
  public double[] getObservations() {
    return _observations;
  }

  /**
   * Gets the observationWeights.
   * @return the observationWeights
   */
  public final double[] getObservationWeights() {
    return _observationWeights;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_currency == null) ? 0 : _currency.hashCode());
    result = prime * result + _nObsExpected;
    result = prime * result + ((_observations == null) ? 0 : _observations.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_timeToObsEnd);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_timeToObsStart);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_timeToSettlement);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_varNotional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_varStrike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof VarianceSwap)) {
      return false;
    }
    VarianceSwap other = (VarianceSwap) obj;
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (!ObjectUtils.equals(_observations, other._observations)) {
      return false;
    }
    if (!ObjectUtils.equals(_observationWeights, other._observationWeights)) {
      return false;
    }
    if (_nObsExpected != other._nObsExpected) {
      return false;
    }
    if (Double.doubleToLongBits(_timeToObsEnd) != Double.doubleToLongBits(other._timeToObsEnd)) {
      return false;
    }
    if (Double.doubleToLongBits(_timeToObsStart) != Double.doubleToLongBits(other._timeToObsStart)) {
      return false;
    }
    if (Double.doubleToLongBits(_timeToSettlement) != Double.doubleToLongBits(other._timeToSettlement)) {
      return false;
    }
    if (Double.doubleToLongBits(_varNotional) != Double.doubleToLongBits(other._varNotional)) {
      return false;
    }
    if (Double.doubleToLongBits(_varStrike) != Double.doubleToLongBits(other._varStrike)) {
      return false;
    }
    return true;
  }

}
