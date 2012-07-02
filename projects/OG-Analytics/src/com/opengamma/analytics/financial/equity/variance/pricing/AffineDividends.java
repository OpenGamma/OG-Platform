/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance.pricing;

import java.util.Arrays;

import com.opengamma.util.ArgumentChecker;

/**
 * Dividend payment (per share) at time $\tau_i$ of the form $\alpha_i + \beta_iS_{\tau_{i^-}}$  where $S_{\tau_{i^-}}$ is the stock price immediately before the 
 * dividend payment. 
 */
public class AffineDividends {

  private final double[] _tau;
  private final double[] _alpha;
  private final double[] _beta;
  private final int _n;

  public AffineDividends(final double[] tau, final double[] alpha, final double[] beta) {
    ArgumentChecker.notNull(tau, "null tau");
    ArgumentChecker.notNull(alpha, "null alpha");
    ArgumentChecker.notNull(beta, "null beta");
    _n = tau.length;
    ArgumentChecker.isTrue(_n == alpha.length, "alpha wrong length");
    ArgumentChecker.isTrue(_n == beta.length, "beta wrong length");

    if (_n > 0) {
      ArgumentChecker.isTrue(tau[0] >= 0.0, "first dividend at negative time. Please remove from list");
      ArgumentChecker.isTrue(alpha[0] >= 0.0, "first cash dividend is negative.");
      ArgumentChecker.isTrue(beta[0] >= 0.0 && beta[0] < 1.0, "Proportional dividend must be between 0.0 (inclusive) and 1.0 (exclusive). Value is ", beta[0]);
      for (int i = 1; i < _n; i++) {
        ArgumentChecker.isTrue(tau[i] > tau[i - 1], "Dividends not increasing. {}th dividend is {}, and {}th is {}", i, tau[i], i - 1, tau[i - 1]);
        ArgumentChecker.isTrue(alpha[i] >= 0.0, "Cash dividend is negative. alpha[{}] = {}", i, alpha[i]);
        ArgumentChecker.isTrue(beta[i] >= 0.0 && beta[i] < 1.0, "Proportional dividend must be between 0.0 (inclusive) and 1.0 (exclusive). beta[{}] = {}", i, beta[i]);
      }
    }
    _tau = tau;
    _alpha = alpha;
    _beta = beta;
  }

  /**
   * Gets the dividend times 
   * @return the tau
   */
  public double[] getTau() {
    return _tau;
  }

  /**
   * Gets the cash dividends .
   * @return the alpha
   */
  public double[] getAlpha() {
    return _alpha;
  }

  /**
   * Gets the proportional dividends.
   * @return the beta
   */
  public double[] getBeta() {
    return _beta;
  }

  /**
   * Gets the dividend times 
   * @param index the index of the dividend
   * @return the tau
   */
  public double getTau(int index) {
    return _tau[index];
  }

  /**
   * Gets the cash dividends.
   * @param index the index of the dividend
   * @return the alpha
   */
  public double getAlpha(int index) {
    return _alpha[index];
  }

  /**
   * Gets the proportional dividends.
   * @param index the index of the dividend
   * @return the beta
   */
  public double getBeta(int index) {
    return _beta[index];
  }

  /**
   * Gets the number of dividends.
   * @return the number of dividends
   */
  public int getNumberOfDividends() {
    return _n;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_alpha);
    result = prime * result + Arrays.hashCode(_beta);
    result = prime * result + Arrays.hashCode(_tau);
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
    AffineDividends other = (AffineDividends) obj;
    if (!Arrays.equals(_alpha, other._alpha)) {
      return false;
    }
    if (!Arrays.equals(_beta, other._beta)) {
      return false;
    }
    if (!Arrays.equals(_tau, other._tau)) {
      return false;
    }
    return true;
  }

}
