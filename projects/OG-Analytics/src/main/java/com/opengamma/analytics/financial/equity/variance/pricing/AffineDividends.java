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
  /** The times */
  private final double[] _tau;
  /** The cash dividends */
  private final double[] _alpha;
  /** The proportional dividends */
  private final double[] _beta;
  /** The number of times */
  private final int _n;

  /**
   * @return An object representing no dividends
   */
  public static AffineDividends noDividends() {
    final double[] z = new double[0];
    return new AffineDividends(z, z, z);
  }

  /**
   * @param tau The dividend payment times, not null. The values must be greater than zero and strictly increasing.
   * @param alpha The cash dividends, not null. Must be the same length as the times with all values positive.
   * @param beta The proportional dividends, not null. Must be the same length as the times with all values between 0 (inclusive) and 1 (exclusive).
   */
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
      ArgumentChecker.isTrue(beta[0] >= 0.0 && beta[0] < 1.0, "Proportional dividend must be between 0.0 (inclusive) and 1.0 (exclusive). Value is {}", beta[0]);
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
    final double[] tau = Arrays.copyOf(_tau, _n);
    return tau;
  }

  /**
   * Gets the cash dividends.
   * @return the alpha
   */
  public double[] getAlpha() {
    final double[] alpha = Arrays.copyOf(_alpha, _n);
    return alpha;
  }

  /**
   * Gets the proportional dividends.
   * @return the beta
   */
  public double[] getBeta() {
    final double[] beta = Arrays.copyOf(_beta, _n);
    return beta;
  }

  /**
   * Gets the dividend times
   * @param index the index of the dividend
   * @return the tau
   */
  public double getTau(final int index) {
    return _tau[index];
  }

  /**
   * Gets the cash dividends.
   * @param index the index of the dividend
   * @return the alpha
   */
  public double getAlpha(final int index) {
    return _alpha[index];
  }

  /**
   * Gets the proportional dividends.
   * @param index the index of the dividend
   * @return the beta
   */
  public double getBeta(final int index) {
    return _beta[index];
  }

  /**
   * Gets the number of dividends.
   * @return the number of dividends
   */
  public int getNumberOfDividends() {
    return _n;
  }

  /**
   * Change one of the dividend times
   * @param value The new value of the dividend time, tau
   * @param index The index of the new dividend time
   * @return A new AffineDividends with the changed tau
   */
  public AffineDividends withTau(final double value, final int index) {
    ArgumentChecker.isTrue(value >= 0.0, "negative tau");
    ArgumentChecker.isTrue(index >= 0 && index < _n, "index out of range");
    final double[] tau = Arrays.copyOf(_tau, _n);
    tau[index] = value;
    return new AffineDividends(tau, _alpha, _beta);
  }

  /**
   * Change one of the alpha values
   * @param value The new value of alpha
   * @param index The index of the new alpha
   * @return A new AffineDividends with the changed alpha
   */
  public AffineDividends withAlpha(final double value, final int index) {
    ArgumentChecker.isTrue(value >= 0.0, "negative alpha");
    ArgumentChecker.isTrue(index >= 0 && index < _n, "index out of range");
    final double[] alpha = Arrays.copyOf(_alpha, _n);
    alpha[index] = value;
    return new AffineDividends(_tau, alpha, _beta);
  }

  /**
   * Change one of the beta values
   * @param value The new value of beta
   * @param index The index of the new beta
   * @return A new AffineDividends with the changed beta
   */
  public AffineDividends withBeta(final double value, final int index) {
    ArgumentChecker.isTrue(value >= 0.0, "negative beta");
    ArgumentChecker.isTrue(index >= 0 && index < _n, "index out of range");
    final double[] beta = Arrays.copyOf(_beta, _n);
    beta[index] = value;
    return new AffineDividends(_tau, _alpha, beta);
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
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof AffineDividends)) {
      return false;
    }
    final AffineDividends other = (AffineDividends) obj;
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
