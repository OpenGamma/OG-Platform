/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.function;

import java.util.Arrays;

import com.opengamma.analytics.math.minimization.SumToOne;
import com.opengamma.util.ArgumentChecker;

/**
 * If a PDF is constructed as the weighted sum of log-normal distributions, then a European option price is give by the weighted sum of Black prices (with different volatilities and
 * (potentially) different forwards). Sufficiently many log-normal distributions can reproduce any PDE and therefore any arbitrage free smile.
 */
public class MultiHorizonMixedLogNormalModelData {

  private static final double TOL = 1e-9;
  private final SumToOne _sto;

  private final int _nNorms;
  private final int _nParams;
  private final double[] _sigmas;
  private final double[] _w;
  private final double[] _mus;
  private final boolean _shiftedMeans;

  //for a mixture of n log-normals, the parameters are ordered as: sigma_0, deltaSigma_1....deltaSigma_{n-1}, theta_1...theta_{n-1}, phi_1...phi_{n-1}
  //where sigma_0 is the lowest volatility state, and the volatility of state i, sigma_i = sigma_{i-1} + deltaSigma_i, so the volatility states are strictly increasing
  //(with  deltaSigma_i > 0). The angles theta encode the weights (via the SumToOne class) and the angles phi encode the partial forwards (if they are used). Therefore, there
  //are 3n-2 free parameters (or 2n-1 in the case that the partial forwards are all fixed to one)
  private final double[] _parameters;

  /**
   * Set up a mixed log-normal model with the means of the distributions all the same value
   * @param parameters The 2n-1 parameters (where n is the number of normals) in order as: sigma_0, deltaSigma_1....deltaSigma_{n-1}, theta_1...theta_{n-1} where sigma_0
   *  is the lowest volatility state, and the volatility of state i, sigma_i = sigma_{i-1} + deltaSigma_i, so the volatility states are strictly increasing (with  deltaSigma_i > 0).
   * The angles theta encode the weights 
   *  (via the SumToOne class). 
   */
  public MultiHorizonMixedLogNormalModelData(final double[] parameters) {
    this(parameters, true);
  }

  /**
   * Set up a mixed log-normal model with option to have distributions with different means 
   * @param parameters The 2n-1 or 3n-2 parameters (where n is the number of normals) depending on whether useShiftedMeans is false or true. The parameters in order as:
   * sigma_0, deltaSigma_1....deltaSigma_{n-1}, theta_1...theta_{n-1}, phi_1...phi_{n-1}
   * where sigma_0 is the lowest volatility state, and the volatility of state i, sigma_i = sigma_{i-1} + deltaSigma_i, so the volatility states are strictly increasing
   * (with deltaSigma_i > 0). The angles theta encode the weights (via the SumToOne class) and the angles phi encode the partial forwards (if they are used).
   * @param useShiftedMeans If true the distributions can have different means (and 3n-2 parameters must be supplied), otherwise they are all the same (and 2n-1 parameters must be supplied)
   */
  public MultiHorizonMixedLogNormalModelData(final double[] parameters, final boolean useShiftedMeans) {
    ArgumentChecker.notNull(parameters, "parameters");
    _nParams = parameters.length;
    _shiftedMeans = useShiftedMeans;
    int n;
    if (useShiftedMeans) {
      ArgumentChecker.isTrue(_nParams % 3 == 2, "Wrong length of parameters - length {}, but must be 3n-2, where n is an integer", _nParams);
      n = (_nParams + 1) / 3;
    } else {
      ArgumentChecker.isTrue(_nParams % 2 == 1, "Wrong length of parameters - length {}, but must be 2n-1, where n is an integer", _nParams);
      n = (_nParams + 1) / 2;
    }
    _nNorms = n;

    //check parameters
    for (int i = 0; i < n; i++) {
      ArgumentChecker.isTrue(parameters[i] >= 0.0, "parameters {} have value {}, must be >= 0", i, parameters[i]);
    }

    _sto = new SumToOne(n);
    _parameters = parameters;
    _sigmas = new double[n];
    _sigmas[0] = _parameters[0];
    for (int i = 1; i < n; i++) {
      _sigmas[i] = _sigmas[i - 1] + _parameters[i];
    }
    double[] temp = Arrays.copyOfRange(_parameters, n, 2 * n - 1);
    _w = _sto.transform(temp);
    if (useShiftedMeans) {
      _mus = Arrays.copyOfRange(_parameters, 2 * n - 1, 3 * n - 1);
    } else {
      _mus = new double[n];
      Arrays.fill(_mus, 0.0);
    }
  }

  /**
   * Set up a mixed log-normal model with the means of the distributions all the same value
  * @param weights The weights of (i.e. probability of being in) each state <b>These weights must sum to 1</b> 
   * @param sigmas The volatility of the geometric Brownian motion in each state 
   */
  public MultiHorizonMixedLogNormalModelData(final double[] weights, final double[] sigmas) {
    ArgumentChecker.notNull(sigmas, "null sigmas");
    ArgumentChecker.notNull(weights, "null weights");
    _shiftedMeans = false;
    final int n = sigmas.length;
    _nNorms = n;
    ArgumentChecker.isTrue(n == weights.length, "Weights not the same length as sigmas");
    ArgumentChecker.isTrue(n > 0, "no weights");
    double sum = 0.0;
    for (int i = 0; i < n; i++) {
      ArgumentChecker.isTrue(sigmas[i] > 0.0, "zero or negative sigma");
      ArgumentChecker.isTrue(weights[i] >= 0.0, "negative weight");
      sum += weights[i];
    }
    ArgumentChecker.isTrue(Math.abs(sum - 1.0) < TOL, "Weights do not sum to 1.0");
    _nParams = 2 * n - 1;
    _sigmas = sigmas;
    _w = weights;
    _mus = new double[n];
    Arrays.fill(_mus, 0.0);

    _sto = new SumToOne(n);
    _parameters = new double[_nParams];
    _parameters[0] = sigmas[0];
    for (int i = 1; i < n; i++) {
      final double temp = sigmas[i] - sigmas[i - 1];
      ArgumentChecker.isTrue(temp >= 0, "sigmas must be increasing"); //TODO drop this and parallel sort into increasing order
      _parameters[i] = temp;
    }
    final double[] theta = _sto.inverseTransform(weights);
    System.arraycopy(theta, 0, _parameters, n, n - 1);
  }

  /**
   * Set up a mixed log-normal model with the means of the distributions can take different values 
   * @param weights The weights of (i.e. probability of being in) each state <b>These weights must sum to 1</b> 
   * @param sigmas The volatility of the geometric Brownian motion in each state 
   * @param mus The drift in each state 
   * <b>Must have sum w_i*rpf_i = 1.0</b>
   */
  public MultiHorizonMixedLogNormalModelData(final double[] weights, final double[] sigmas, final double[] mus) {
    _shiftedMeans = true;
    ArgumentChecker.notNull(sigmas, "null sigmas");
    ArgumentChecker.notNull(weights, "null weights");
    final int n = sigmas.length;
    _nNorms = n;
    ArgumentChecker.isTrue(n == weights.length, "Weights not the same length as sigmas");
    ArgumentChecker.isTrue(n == mus.length, "Partial forwards not the same length as sigmas");
    ArgumentChecker.isTrue(n > 0, "no weights");
    double sum = 0.0;
    for (int i = 0; i < n; i++) {
      ArgumentChecker.isTrue(sigmas[i] > 0.0, "zero or negative sigma");
      ArgumentChecker.isTrue(weights[i] >= 0.0, "negative weight");
      sum += weights[i];
    }
    ArgumentChecker.isTrue(Math.abs(sum - 1.0) < TOL, "Weights do not sum to 1.0");
    _sigmas = sigmas;
    _w = weights;
    _mus = mus;
    _nParams = 3 * n - 1;

    _sto = new SumToOne(n);
    _parameters = new double[_nParams];
    _parameters[0] = sigmas[0];
    for (int i = 1; i < n; i++) {
      final double temp = sigmas[i] - sigmas[i - 1];
      ArgumentChecker.isTrue(temp >= 0, "sigmas must be increasing"); //TODO drop this and parallel sort into increasing order
      _parameters[i] = temp;
    }
    final double[] theta = _sto.inverseTransform(weights);
    System.arraycopy(theta, 0, _parameters, n, n - 1);

    System.arraycopy(mus, 0, _parameters, 2 * n - 1, n);
  }

  public double[] getWeights() {
    return _w;
  }

  public double[] getVolatilities() {
    return _sigmas;
  }

  public double[] getMus() {
    return _mus;
  }

  /**
   * The matrix of partial derivatives of weights with respect to the angles theta
   * @return the n by n-1 Jacobian, where n is the number of normals
   */
  public double[][] getWeightsJacobian() {
    final double[] temp = Arrays.copyOfRange(_parameters, _nNorms, 2 * _nNorms - 1);
    return _sto.jacobian(temp);
  }

  public int getNumberOfParameters() {
    return _nParams;
  }

  public double getParameter(final int index) {
    final double temp = _parameters[index];
    if (temp >= 0 && temp <= Math.PI / 2) {
      return temp;
    }
    return toZeroToPiByTwo(temp);
  }

  private double toZeroToPiByTwo(final double theta) {
    double x = theta;
    if (x < 0) {
      x = -x;
    }
    if (x > Math.PI / 2) {
      final int p = (int) (x / Math.PI);
      x -= p * Math.PI;
      if (x > Math.PI / 2) {
        x = -x + Math.PI;
      }
    }
    return x;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_mus);
    result = prime * result + (_shiftedMeans ? 1231 : 1237);
    result = prime * result + Arrays.hashCode(_sigmas);
    result = prime * result + Arrays.hashCode(_w);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MultiHorizonMixedLogNormalModelData other = (MultiHorizonMixedLogNormalModelData) obj;
    if (_shiftedMeans && !Arrays.equals(_mus, other._mus)) {
      return false;
    }
    if (_shiftedMeans != other._shiftedMeans) {
      return false;
    }
    if (!Arrays.equals(_sigmas, other._sigmas)) {
      return false;
    }
    if (!Arrays.equals(_w, other._w)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "MixedLogNormalModelData [_sigmas=" + Arrays.toString(_sigmas) + ", _w=" + Arrays.toString(_w) + ", _mus=" + Arrays.toString(_mus) + "]";
  }

}
