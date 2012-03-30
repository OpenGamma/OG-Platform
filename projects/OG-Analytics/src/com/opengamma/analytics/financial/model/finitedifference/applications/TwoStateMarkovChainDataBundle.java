/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class TwoStateMarkovChainDataBundle {
  private final double _vol1;
  private final double _vol2;
  private final double _beta1;
  private final double _beta2;
  private final double _lambda12;
  private final double _lambda21;
  private final double _pi1;
  private final double _p0;

  /**
   * 
  * @param vol1 Volatility of state 1 
   * @param vol2  Volatility of state 1 
   * @param lambda12 Transition rate from state 1 to 2 
   * @param lambda21 Transition rate from state 2 to 1 
   * @param probS1 Probability of starting in state 1 
   */
  public TwoStateMarkovChainDataBundle(final double vol1, final double vol2,
      final double lambda12, final double lambda21, final double probS1) {
    this(vol1, vol2, lambda12, lambda21, probS1, 1.0, 1.0);
  }

  /**
   * 
   * @param vol1 Volatility of state 1 
   * @param vol2  Volatility of state 1 
   * @param lambda12 Transition rate from state 1 to 2 
   * @param lambda21 Transition rate from state 2 to 1 
   * @param probS1 Probability of starting in state 1 
   * @param beta1 CEV parameter in state 1
   * @param beta2 CEV parameter in state 2
   */
  public TwoStateMarkovChainDataBundle(final double vol1, final double vol2,
      final double lambda12, final double lambda21, final double probS1, final double beta1, final double beta2) {

    Validate.isTrue(vol1 >= 0.0, "vol1 < 0");
    Validate.isTrue(vol2 >= vol1, "vol2 < vol1");
    Validate.isTrue(lambda12 >= 0.0, "lambda12 < 0");
    Validate.isTrue(lambda21 >= 0.0, "lambda21 < 0");
    Validate.isTrue(probS1 >= 0.0 && probS1 <= 1.0, "Need 0 <= probS1 <= 1.0");
    Validate.isTrue(beta1 >= 0.0 && beta1 <= 2.0, "Need 0 <= beta1 <= 2.0");
    Validate.isTrue(beta2 >= 0.0 && beta2 <= 2.0, "Need 0 <= beta2 <= 2.0");

    _vol1 = vol1;
    _vol2 = vol2;
    _beta1 = beta1;
    _beta2 = beta2;
    _lambda12 = lambda12;
    _lambda21 = lambda21;
    _p0 = probS1;

    double sum = lambda12 + lambda21;
    if (sum == 0) {
      _pi1 = probS1;
    } else {
      _pi1 = lambda21 / sum;
    }
  }

  /**
   * Gets the vol1.
   * @return the vol1
   */
  public double getVol1() {
    return _vol1;
  }

  /**
   * Gets the vol2.
   * @return the vol2
   */
  public double getVol2() {
    return _vol2;
  }

  /**
   * Gets the beta1.
   * @return the beta1
   */
  public double getBeta1() {
    return _beta1;
  }

  /**
   * Gets the beta2.
   * @return the beta2
   */
  public double getBeta2() {
    return _beta2;
  }

  /**
   * Gets the lambda12.
   * @return the lambda12
   */
  public double getLambda12() {
    return _lambda12;
  }

  /**
   * Gets the lambda21.
   * @return the lambda21
   */
  public double getLambda21() {
    return _lambda21;
  }

  /**
   * Gets the p0.
   * @return the p0
   */
  public double getP0() {
    return _p0;
  }

  public double getSteadyStateProb() {
    return _pi1;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_beta1);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_beta2);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_lambda12);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_lambda21);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_p0);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_vol1);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_vol2);
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
    TwoStateMarkovChainDataBundle other = (TwoStateMarkovChainDataBundle) obj;
    if (Double.doubleToLongBits(_beta1) != Double.doubleToLongBits(other._beta1)) {
      return false;
    }
    if (Double.doubleToLongBits(_beta2) != Double.doubleToLongBits(other._beta2)) {
      return false;
    }
    if (Double.doubleToLongBits(_lambda12) != Double.doubleToLongBits(other._lambda12)) {
      return false;
    }
    if (Double.doubleToLongBits(_lambda21) != Double.doubleToLongBits(other._lambda21)) {
      return false;
    }
    if (Double.doubleToLongBits(_p0) != Double.doubleToLongBits(other._p0)) {
      return false;
    }
    if (Double.doubleToLongBits(_vol1) != Double.doubleToLongBits(other._vol1)) {
      return false;
    }
    if (Double.doubleToLongBits(_vol2) != Double.doubleToLongBits(other._vol2)) {
      return false;
    }
    return true;
  }

}
