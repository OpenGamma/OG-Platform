/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.function;

import java.util.Arrays;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.util.ArgumentChecker;

/**
 * Derive volatility smile of a mixed log-normal model with mixed normal variable Z = X-Y under the assumption that X, Y are mixed bivariate normal variables. 
 * Here all of the parameters of X,Y and their correlations are input parameters.
 */
public class MixedBivariateLogNormalModelVolatility {

  private MixedLogNormalModelData _data;

  private double[] _weights;
  private double[] _sigmasX;
  private double[] _sigmasY;
  private double[] _relativePartialForwardsX;
  private double[] _relativePartialForwardsY;
  private double[] _rhos;

  private double _driftCorrection;

  private int _nNormals;

  /**
   * Set up mixed log-normal models with mixed normal variables X, Y and another mixed log-normal model with Z = X-Y
   * @param weights The weights  <b>These weights must sum to 1</b> 
   * @param sigmasX The standard deviation of the individual normal distributions in X 
   * @param sigmasY The standard deviation of the individual normal distributions in Y 
   * @param rhos The correlation between distributions of X and Y
   */
  public MixedBivariateLogNormalModelVolatility(final double[] weights, final double[] sigmasX, final double[] sigmasY, final double[] rhos) {
    ArgumentChecker.isTrue(sigmasX.length == sigmasY.length, "sigmasX must be the same length as sigmasY");
    ArgumentChecker.isTrue(sigmasX.length == rhos.length, "sigmasX must be the same length as rhos");

    _nNormals = sigmasX.length;
    _relativePartialForwardsX = new double[_nNormals];
    _relativePartialForwardsY = new double[_nNormals];
    Arrays.fill(_relativePartialForwardsX, 1.);
    Arrays.fill(_relativePartialForwardsY, 1.);

    _weights = new double[_nNormals];
    _sigmasX = new double[_nNormals];
    _sigmasY = new double[_nNormals];
    _rhos = new double[_nNormals];

    for (int i = 0; i < _nNormals; ++i) {
      _weights[i] = weights[i];
      _sigmasX[i] = sigmasX[i];
      _sigmasY[i] = sigmasY[i];
      _rhos[i] = rhos[i];
    }

    double[] sigmas = getVolatilityZ(_sigmasX, _sigmasY, _rhos);
    double[] relativePartialForwards = getrelativePartialForwardZ(_relativePartialForwardsX, _relativePartialForwardsY, _sigmasX, _sigmasY, _rhos);
    _driftCorrection = getDriftCorrectionZ(_weights, _relativePartialForwardsX, _relativePartialForwardsY, _sigmasX, _sigmasY, _rhos);

    for (int i = 0; i < _nNormals; ++i) {
      relativePartialForwards[i] *= _driftCorrection;
    }

    //Avoid negative sigmas
    for (int i = 0; i < _nNormals; ++i) {
      if (sigmas[i] <= 0.) {
        sigmas[i] = 1e-5;
      }
    }

    int j = 0;
    double tmpSigmas = 0;
    double tmpWeights = 0;
    double tmpRelativePartialForwards = 0;
    for (int i = 0; i < _nNormals; ++i) {
      j = i;
      for (int k = i; k < _nNormals; ++k) {
        if (sigmas[j] > sigmas[k]) {
          j = k;
        }
      }
      tmpSigmas = sigmas[i];
      sigmas[i] = sigmas[j];
      sigmas[j] = tmpSigmas;
      tmpWeights = _weights[i];
      _weights[i] = _weights[j];
      _weights[j] = tmpWeights;
      tmpRelativePartialForwards = relativePartialForwards[i];
      relativePartialForwards[i] = relativePartialForwards[j];
      relativePartialForwards[j] = tmpRelativePartialForwards;

      tmpSigmas = _sigmasX[i];
      _sigmasX[i] = _sigmasX[j];
      _sigmasX[j] = tmpSigmas;
      tmpRelativePartialForwards = _relativePartialForwardsX[i];
      _relativePartialForwardsX[i] = _relativePartialForwardsX[j];
      _relativePartialForwardsX[j] = tmpRelativePartialForwards;

      tmpSigmas = _sigmasY[i];
      _sigmasY[i] = _sigmasY[j];
      _sigmasY[j] = tmpSigmas;
      tmpRelativePartialForwards = _relativePartialForwardsY[i];
      _relativePartialForwardsY[i] = _relativePartialForwardsY[j];
      _relativePartialForwardsY[j] = tmpRelativePartialForwards;
    }

    _data = new MixedLogNormalModelData(_weights, sigmas, relativePartialForwards);
  }

  /**
   * Set up mixed log-normal models with mixed bivariate normal variables X, Y and another mixed log-normal model with Z = X-Y
   * @param weights The weights  <b>These weights must sum to 1</b> 
   * @param sigmasX The standard deviation of the normal distributions in X 
   * @param sigmasY The standard deviation of the normal distributions in Y 
   * @param relativePartialForwardsX The expectation of each distribution in X is rpf_i*forward
   * @param relativePartialForwardsY The expectation of each distribution in Y is rpf_i*forward
   * (rpf_i is the ith relativePartialForwards)
   * <b>Must have sum w_i*rpf_i = 1.0</b>
   * @param rhos The correlation between the distributions
   */
  public MixedBivariateLogNormalModelVolatility(final double[] weights, final double[] sigmasX, final double[] sigmasY, final double[] relativePartialForwardsX,
      final double[] relativePartialForwardsY,
      final double[] rhos) {
    ArgumentChecker.isTrue(sigmasX.length == sigmasY.length, "sigmasX must be the same length as sigmasY");
    ArgumentChecker.isTrue(sigmasX.length == rhos.length, "sigmasX must be the same length as rhos");
    ArgumentChecker.isTrue(relativePartialForwardsX.length == relativePartialForwardsY.length, "relativePartialForwardsX must be the same length as relativePartialForwardsY");
    ArgumentChecker.isTrue(relativePartialForwardsX.length == rhos.length, "relativePartialForwardsX must be the same length as rhos");

    _nNormals = sigmasX.length;

    _weights = new double[_nNormals];
    _sigmasX = new double[_nNormals];
    _sigmasY = new double[_nNormals];
    _relativePartialForwardsX = new double[_nNormals];
    _relativePartialForwardsY = new double[_nNormals];
    _rhos = new double[_nNormals];

    for (int i = 0; i < _nNormals; ++i) {
      _weights[i] = weights[i];
      _sigmasX[i] = sigmasX[i];
      _sigmasY[i] = sigmasY[i];
      _relativePartialForwardsX[i] = relativePartialForwardsX[i];
      _relativePartialForwardsY[i] = relativePartialForwardsY[i];
      _rhos[i] = rhos[i];
    }

    _driftCorrection = getDriftCorrectionZ(_weights, _relativePartialForwardsX, _relativePartialForwardsY, _sigmasX, _sigmasY, _rhos);

    double[] sigmas = getVolatilityZ(_sigmasX, _sigmasY, _rhos);
    double[] relativePartialForwards = getrelativePartialForwardZ(_relativePartialForwardsX, _relativePartialForwardsY, _sigmasX, _sigmasY, _rhos);

    for (int i = 0; i < _nNormals; ++i) {
      relativePartialForwards[i] *= _driftCorrection;
    }

    // sigmas should be descending order.
    // Labels for input variables of X,Y are also changed. These will be useful for rho-fitting of Z (see MixedLogNormal2DCorrelationFinder). 
    int j = 0;
    double tmpSigmas = 0;
    double tmpWeights = 0;
    double tmpRelativePartialForwards = 0;
    for (int i = 0; i < _nNormals; ++i) {
      j = i;
      for (int k = i; k < _nNormals; ++k) {
        if (sigmas[j] > sigmas[k]) {
          j = k;
        }
      }

      tmpSigmas = sigmas[i];
      sigmas[i] = sigmas[j];
      sigmas[j] = tmpSigmas;
      tmpWeights = _weights[i];
      _weights[i] = _weights[j];
      _weights[j] = tmpWeights;
      tmpRelativePartialForwards = relativePartialForwards[i];
      relativePartialForwards[i] = relativePartialForwards[j];
      relativePartialForwards[j] = tmpRelativePartialForwards;

      tmpSigmas = _sigmasX[i];
      _sigmasX[i] = _sigmasX[j];
      _sigmasX[j] = tmpSigmas;
      tmpRelativePartialForwards = _relativePartialForwardsX[i];
      _relativePartialForwardsX[i] = _relativePartialForwardsX[j];
      _relativePartialForwardsX[j] = tmpRelativePartialForwards;

      tmpSigmas = _sigmasY[i];
      _sigmasY[i] = _sigmasY[j];
      _sigmasY[j] = tmpSigmas;
      tmpRelativePartialForwards = _relativePartialForwardsY[i];
      _relativePartialForwardsY[i] = _relativePartialForwardsY[j];
      _relativePartialForwardsY[j] = tmpRelativePartialForwards;
    }

    _data = new MixedLogNormalModelData(_weights, sigmas, relativePartialForwards);
  }

  /**
   * Sigmas calculator for the normal distributions in Z
   */
  private double[] getVolatilityZ(final double[] sigX, final double[] sigY, final double[] rh)
  {
    final int nNormals = sigX.length;
    final double[] res = new double[nNormals];

    for (int i = 0; i < nNormals; i++) {
      res[i] = Math.sqrt(sigX[i] * sigX[i] + sigY[i] * sigY[i] - 2. * rh[i] * sigX[i] * sigY[i]);
    }

    return res;
  }

  /**
   * Calculate "relative partial forward" of the normal distributions in Z 
   */
  private double[] getrelativePartialForwardZ(final double[] rpfX, final double[] rpfY, final double[] sigX, final double[] sigY, final double[] rh)
  {
    final int nNormals = rpfY.length;
    final double[] res = new double[nNormals];
    for (int i = 0; i < nNormals; i++) {
      res[i] = Math.exp(Math.log(rpfX[i]) - Math.log(rpfY[i]) + sigY[i] * sigY[i] - rh[i] * sigX[i] * sigY[i]);
    }

    return res;
  }

  /**
   * Fix the extra degree of freedom in Z such that sum w_i*rpf_i = 1.0 is satisfied
   * (rpf_i for Z is then relativePartialForwardsZ[i] * driftCorrectionZ)
   */
  private double getDriftCorrectionZ(final double[] wght, final double[] rpfX, final double[] rpfY, final double[] sigX, final double[] sigY,
      final double[] rhos) {

    final int nNormals = wght.length;
    double tmp = 0.;
    for (int i = 0; i < nNormals; i++) {
      tmp += wght[i] *
          Math.exp(Math.log(rpfX[i]) - Math.log(rpfY[i]) + sigY[i] * sigY[i] - rhos[i] * sigX[i] * sigY[i]);
    }

    return 1. / tmp;
  }

  /**
   * @return exp(-(driftCorrection)) where rpf_i -> rpf_i * exp(-(driftCorrection)) for all i
   */
  public double getInvExpDriftCorrection() {
    return _driftCorrection;
  }

  public double[] getOrderedWeights() {
    return _data.getWeights();
  }

  /**
   * @return input sigmas for Z computed from sigmasX, sigmasY and rhos by getVolatilityZ(double[], double[], double[])
   */
  public double[] getSigmasZ() {
    return _data.getVolatilities();
  }

  public double[] getRelativeForwardsZ() {
    return _data.getRelativeForwards();
  }

  /**
   * @param option TimeToExpiry Strike and OptionType are contained
   * @param forward 
   * @return implied volatility
   */
  public double getImpliedVolatilityZ(final EuropeanVanillaOption option, final double forward) {
    final MixedLogNormalVolatilityFunction volfunc = MixedLogNormalVolatilityFunction.getInstance();
    return volfunc.getVolatility(option, forward, _data);
  }

  public double getPriceZ(final EuropeanVanillaOption option, final double forward) {
    final MixedLogNormalVolatilityFunction volfunc = MixedLogNormalVolatilityFunction.getInstance();
    return volfunc.getPrice(option, forward, _data);
  }

  public double[] getOrderedSigmasX() {
    return _sigmasX;
  }

  public double[] getOrderedSigmasY() {
    return _sigmasY;
  }

  public double[] getOrderedRelativePartialForwardsX() {
    return _relativePartialForwardsX;
  }

  public double[] getOrderedRelativePartialForwardsY() {
    return _relativePartialForwardsY;
  }

}
