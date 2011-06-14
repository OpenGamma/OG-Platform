/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.varswap.derivative;

import com.opengamma.financial.equity.EquityDerivative;
import com.opengamma.financial.equity.EquityDerivativeVisitor;
import com.opengamma.util.money.Currency;

/**
 *  TODO Case 2011-06-07 - Add Javadoc description
 */
public class VarianceSwap implements EquityDerivative {
  private final double _timeToObsStart;
  private final double _timeToObsEnd;
  private final double _timeToSettlement;

  private final int _nObsExpected;
  private final int _nObsRemaining; // nExpectedToHaveOccurred = _nObsExpected - _nObsRemaining
  private final int _nObsActObs; // number of observations that have actually been made

  private final Currency _currency;

  private final double _varStrike; // volStrike^2 
  private final double _varNotional; // := 0.5 * _volNotional / _volStrike

  /**
   * @param timeToObsStart Time of first observation. Negative if observations have begun.
   * @param timeToObsEnd Time of final observation. Negative if observations have finished.
   * @param timeToSettlement Time of cash settlement. If negative, the swap has expired.
   * @param nObsExpected Number of observations expected as of trade inception
   * @param nObsRemaining Number of expected observations remaining  
   * @param nObsActObs Actual number of observations made thus far
   * @param currency Currency of cash settlement
   * @param varStrike Fair value of Variance struck at trade date
   * @param varNotional Trade pays the difference between realized and strike variance multiplied by this
   */
  public VarianceSwap(double timeToObsStart, double timeToObsEnd, double timeToSettlement,
      int nObsExpected, int nObsRemaining, int nObsActObs,
      Currency currency, double varStrike, double varNotional) {

    _timeToObsStart = timeToObsStart;
    _timeToObsEnd = timeToObsEnd;
    _timeToSettlement = timeToSettlement;
    _nObsExpected = nObsExpected;
    _nObsRemaining = nObsRemaining;
    _nObsActObs = nObsActObs;
    _currency = currency;
    _varStrike = varStrike;
    _varNotional = varNotional;
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
   * Gets the nObsRemaining.
   * @return the nObsRemaining
   */
  public int getObsRemaining() {
    return _nObsRemaining;
  }

  /**
   * Gets the nObsActObs.
   * @return the nObsActObs
   */
  public int getObsActObs() {
    return _nObsActObs;
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_currency == null) ? 0 : _currency.hashCode());
    result = prime * result + _nObsActObs;
    result = prime * result + _nObsExpected;
    result = prime * result + _nObsRemaining;
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
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    VarianceSwap other = (VarianceSwap) obj;
    if (_currency == null) {
      if (other._currency != null) {
        return false;
      }
    } else if (!_currency.equals(other._currency)) {
      return false;
    }
    if (_nObsActObs != other._nObsActObs) {
      return false;
    }
    if (_nObsExpected != other._nObsExpected) {
      return false;
    }
    if (_nObsRemaining != other._nObsRemaining) {
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
