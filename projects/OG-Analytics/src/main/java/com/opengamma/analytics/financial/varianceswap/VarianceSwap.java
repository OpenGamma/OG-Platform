/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.varianceswap;

import java.util.Arrays;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A variance swap is a forward contract on the realised variance of a generic underlying. This could be a single equity price, the value of an equity index,
 * an FX rate or <b>any</b> other financial metric on which a variance swap contract is based.<p>
 * The floating leg of a variance swap is the realized variance and is calculated using the second moment of log returns of the underlying asset.
 * <p>
 * Because variance is additive in time, the value of a variance swap can be decomposed at any point in time between realized and implied variance as
 * _varNotional * Z(t,T) * [ t/T * RealizedVol(0,t)^2 + (T-t)/T * ImpliedVol(t,T)^2 - volStrike^2 ]
 */
public class VarianceSwap implements InstrumentDerivative {
  /** The time in years to the start of variance observations */
  private final double _timeToObsStart;
  /** The time in years to the end of variance observations */
  private final double _timeToObsEnd;
  /** The time year years to settlement */
  private final double _timeToSettlement;
  /** The variance strike. volStrike ^ 2 */
  private final double _varStrike;
  /** The variance notional. 0.5 * _volNotional / _volStrike */
  private final double _varNotional;
  /** The currency */
  private final Currency _currency;
  /** The annualization factor */
  private final double _annualizationFactor; // typically 252 with daily observations
  /** The number of expected observations */
  private final int _nObsExpected;
  /** The number of missing observations */
  private final int _nObsDisrupted;
  /** The observed variances */
  private final double[] _observations;
  /** The observation weights */
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
  public VarianceSwap(final double timeToObsStart, final double timeToObsEnd, final double timeToSettlement,
      final double varStrike, final double varNotional, final Currency currency, final double annualizationFactor,
      final int nObsExpected, final int nObsDisrupted, final double[] observations, final double[] observationWeights) {

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
      final int nWeights = _observationWeights.length;
      final int nObs = _observations.length;
      ArgumentChecker.isTrue(nWeights + 1 == nObs,
          "If provided, observationWeights must be of length one less than observations, as they weight returns log(obs[i]/obs[i-1])."
              + " Found {} weights and {} observations.", nWeights, nObs);
    }
    ArgumentChecker.isTrue(_nObsExpected > 0, "Encountered a VarianceSwap with 0 expected observations");
  }

  /**
   * Copy constructor
   * @param other VarianceSwap to copy from
   */
  public VarianceSwap(final VarianceSwap other) {
    ArgumentChecker.notNull(other, "variance swap to copy");
    _timeToObsStart = other._timeToObsStart;
    _timeToObsEnd = other._timeToObsEnd;
    _timeToSettlement = other._timeToSettlement;
    _varStrike = other._varStrike;
    _varNotional = other._varNotional;
    _currency = other._currency;
    _annualizationFactor = other._annualizationFactor;
    _nObsExpected = other._nObsExpected;
    _nObsDisrupted = other._nObsDisrupted;
    _observations = Arrays.copyOf(other._observations, other._observations.length);
    _observationWeights = Arrays.copyOf(other._observationWeights, other._observationWeights.length);
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

  /**
   * Gets the volStrike.
   * @return the volStrike
   */
  public double getVolStrike() {
    return Math.sqrt(_varStrike);
  }

  /**
   * Gets the volNotional.
   * @return the volNotional
   */
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
    long temp;
    temp = Double.doubleToLongBits(_annualizationFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_currency == null) ? 0 : _currency.hashCode());
    result = prime * result + _nObsDisrupted;
    result = prime * result + _nObsExpected;
    result = prime * result + ((_observations == null) ? 0 : Arrays.hashCode(_observations));
    result = prime * result + ((_observationWeights == null) ? 0 : Arrays.hashCode(_observations));
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
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof VarianceSwap)) {
      return false;
    }
    final VarianceSwap other = (VarianceSwap) obj;
    if (Double.doubleToLongBits(_annualizationFactor) != Double.doubleToLongBits(other._annualizationFactor)) {
      return false;
    }
    if (_currency == null) {
      if (other._currency != null) {
        return false;
      }
    } else if (!_currency.equals(other._currency)) {
      return false;
    }
    if (_nObsDisrupted != other._nObsDisrupted) {
      return false;
    }
    if (_nObsExpected != other._nObsExpected) {
      return false;
    }
    if (!Arrays.equals(_observationWeights, other._observationWeights)) {
      return false;
    }
    if (!Arrays.equals(_observations, other._observations)) {
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

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitVarianceSwap(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitVarianceSwap(this);
  }

}
