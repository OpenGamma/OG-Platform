/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.function;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.minimization.SumToOne;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class MixedLogNormalModelData implements SmileModelData {

  private static final double TOL = 1e-9;
  private final SumToOne _sto;

  private final int _nNorms;
  private final int _nParams;
  private final double[] _sigmas;
  private final double[] _w;
  private final double[] _f;
  private final boolean _shiftedMeans;

  //for a mixture of n log-normals, the parameters are ordered as: sigma_0, deltaSigma_1....deltaSigma_{n-1}, theta_1...theta_{n-1}, phi_1...phi_{n-1}
  //where sigma_0 is the lowest volatility state, and the volatility of state i, sigma_i = sigma_{i-1} + deltaSigma_i, so the volatility states are strictly increasing
  //(with  deltaSigma_i > 0). The angles theta encode the weights (via the SumToOne class) and the angles phi encode the partial forwards (if they are used). Therefore, there
  //are 3n-2 free parameters (or 2n-1 in the case that the partial forwards are all fixed to one)
  private double[] _parameters;

  public MixedLogNormalModelData(final double[] parameters) {
    this(parameters, true);
  }

  public MixedLogNormalModelData(final double[] parameters, final boolean useShiftedMeans) {
    _nParams = parameters.length;
    _shiftedMeans = useShiftedMeans;
    int n;
    if (useShiftedMeans) {
      ArgumentChecker.isTrue(_nParams % 3 == 1, "Wrong length of parameters - length {}, but must be 3n-2, where n is an integer", _nParams);
      n = (_nParams + 2) / 3;
    } else {
      ArgumentChecker.isTrue(_nParams % 2 == 1, "Wrong length of parameters - length {}, but must be 2n-1, where n is an integer", _nParams);
      n = (_nParams + 1) / 2;
    }
    _nNorms = n;

    //check parameters
    for (int i = 0; i < n; i++) {
      ArgumentChecker.isTrue(parameters[i] >= 0.0, "parameters {} have value {}, must be >= 0", i, parameters[i]);
    }
    //Review it is not clear whether we wish to restrict the range of angles
    //    for (int i = n; i < 2 * n - 1; i++) {
    //      ArgumentChecker.isTrue(parameters[i] >= 0.0, "parameters {} have value {}, must be >= 0", i, parameters[i]);
    //      ArgumentChecker.isTrue(parameters[i] <= 1.0, "parameters {} have value {}, must be <= 1.0", i, parameters[i]);
    //    }
    //    if (useShiftedMeans) {
    //      for (int i = 2 * n - 1; i < np; i++) {
    //        ArgumentChecker.isTrue(parameters[i] >= 0.0, "parameters {} have value {}, must be >= 0", i, parameters[i]);
    //        ArgumentChecker.isTrue(parameters[i] <= 1.0, "parameters {} have value {}, must be <= 1.0", i, parameters[i]);
    //      }
    //    }

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
      temp = Arrays.copyOfRange(_parameters, 2 * n - 1, 3 * n - 2);
      double[] a = _sto.transform(temp);
      _f = new double[n];
      for (int i = 0; i < n; i++) {
        if (_w[i] > 0) {
          _f[i] = a[i] / _w[i];
        } else {
          _f[i] = 1.0; //if the weight is zero, this will not count towards the price
        }
      }
    } else {
      _f = new double[n];
      Arrays.fill(_f, 1.0);
    }
  }

  public MixedLogNormalModelData(final double[] weights, final double[] sigmas) {
    _shiftedMeans = false;
    Validate.notNull(sigmas, "null sigmas");
    Validate.notNull(weights, "null weights");
    final int n = sigmas.length;
    _nNorms = n;
    Validate.isTrue(n == weights.length, "Weights not the same length as sigmas");
    Validate.isTrue(n > 0, "no weights");
    double sum = 0.0;
    for (int i = 0; i < n; i++) {
      Validate.isTrue(sigmas[i] > 0.0, "zero or negative sigma");
      Validate.isTrue(weights[i] >= 0.0, "negative weight");
      sum += weights[i];
    }
    Validate.isTrue(Math.abs(sum - 1.0) < TOL, "Weights do not sum to 1.0");
    _nParams = 2 * n - 1;
    _sigmas = sigmas;
    _w = weights;
    _f = new double[n];
    Arrays.fill(_f, 1.0);

    _sto = new SumToOne(n);
    _parameters = new double[_nParams];
    _parameters[0] = sigmas[0];
    for (int i = 1; i < n; i++) {
      double temp = sigmas[i] - sigmas[i - 1];
      Validate.isTrue(temp >= 0, "sigmas must be increasing"); //TODO drop this and parallel sort into increasing order
      _parameters[i] = temp;
    }
    double[] theta = _sto.inverseTransform(weights);
    System.arraycopy(theta, 0, _parameters, n, n - 1);
  }

  public MixedLogNormalModelData(final double[] weights, final double[] sigmas, final double[] relativePartialForwards) {
    _shiftedMeans = true;
    Validate.notNull(sigmas, "null sigmas");
    Validate.notNull(weights, "null weights");
    final int n = sigmas.length;
    _nNorms = n;
    Validate.isTrue(n == weights.length, "Weights not the same length as sigmas");
    Validate.isTrue(n == relativePartialForwards.length, "Partial forwards not the same length as sigmas");
    Validate.isTrue(n > 0, "no weights");
    double sum = 0.0;
    double sumF = 0.0;
    double[] a = new double[n];
    for (int i = 0; i < n; i++) {
      Validate.isTrue(sigmas[i] > 0.0, "zero or negative sigma");
      Validate.isTrue(weights[i] >= 0.0, "negative weight");
      Validate.isTrue(relativePartialForwards[i] > 0.0, "zero of negative partial forward");
      sum += weights[i];
      double temp = weights[i] * relativePartialForwards[i];
      sumF += temp;
      a[i] = temp;
    }
    Validate.isTrue(Math.abs(sum - 1.0) < TOL, "Weights do not sum to 1.0");
    Validate.isTrue(Math.abs(sumF - 1.0) < TOL, "Weighted partial forwards do not sum to forward");
    _sigmas = sigmas;
    _w = weights;
    _f = relativePartialForwards;
    _nParams = 3 * n - 2;

    _sto = new SumToOne(n);
    _parameters = new double[_nParams];
    _parameters[0] = sigmas[0];
    for (int i = 1; i < n; i++) {
      double temp = sigmas[i] - sigmas[i - 1];
      Validate.isTrue(temp >= 0, "sigmas must be increasing"); //TODO drop this and parallel sort into increasing order
      _parameters[i] = temp;
    }
    double[] theta = _sto.inverseTransform(weights);
    System.arraycopy(theta, 0, _parameters, n, n - 1);

    double[] phi = _sto.inverseTransform(a);
    System.arraycopy(phi, 0, _parameters, 2 * n - 1, n - 1);
  }

  @Override
  public boolean isAllowed(int index, double value) {
    if (index < _nNorms) {
      return value >= 0.0;
    }
    return true;
  }

  public double[] getWeights() {
    return _w;
  }

  public double[] getVolatilities() {
    return _sigmas;
  }

  public double[] getRelativeForwards() {
    return _f;
  }

  /**
   * The matrix of partial derivatives of weights with respect to the angles theta
   * @return the n by n-1 Jacobian, where n is the number of normals
   */
  public double[][] getWeightsJacobian() {
    double[] temp = Arrays.copyOfRange(_parameters, _nNorms, 2 * _nNorms - 1);
    return _sto.jacobian(temp);
  }

  /**
   * The matrix of partial derivatives of relative forwards  with respect to the angles phi
   * <b>Note</b> The returned matrix has each row multiplied by the weight
   * @return the n by n-1 Jacobian, where n is the number of normals
   */
  public double[][] getRelativeForwardsJacobian() {
    if (!_shiftedMeans) {
      throw new IllegalArgumentException("This model does not used shifted means, therefore no Jacobian exists");
    }
    double[] temp = Arrays.copyOfRange(_parameters, 2 * _nNorms - 1, 3 * _nNorms - 2);
    return _sto.jacobian(temp);
  }

  @Override
  public int getNumberOfparameters() {
    return _nParams;
  }

  @Override
  public double getParameter(int index) {
    final double temp = _parameters[index];
    if (temp >= 0 && temp <= Math.PI / 2) {
      return temp;
    }
    return toZeroToPiByTwo(temp);
  }

  @Override
  public SmileModelData with(int index, double value) {
    double[] temp = new double[_nParams];
    System.arraycopy(_parameters, 0, temp, 0, _nParams);
    temp[index] = value;
    return new MixedLogNormalModelData(temp, _shiftedMeans);
  }

  private double toZeroToPiByTwo(final double theta) {
    double x = theta;
    if (x < 0) {
      x = -x;
    }
    if (x > Math.PI / 2) {
      int p = (int) (x / Math.PI);
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
    result = prime * result + Arrays.hashCode(_f);
    result = prime * result + (_shiftedMeans ? 1231 : 1237);
    result = prime * result + Arrays.hashCode(_sigmas);
    result = prime * result + Arrays.hashCode(_w);
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
    MixedLogNormalModelData other = (MixedLogNormalModelData) obj;
    if (_shiftedMeans && !Arrays.equals(_f, other._f)) {
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
    return "MixedLogNormalModelData [_sigmas=" + Arrays.toString(_sigmas) + ", _w=" + Arrays.toString(_w) + ", _f=" + Arrays.toString(_f) + "]";
  }



}
