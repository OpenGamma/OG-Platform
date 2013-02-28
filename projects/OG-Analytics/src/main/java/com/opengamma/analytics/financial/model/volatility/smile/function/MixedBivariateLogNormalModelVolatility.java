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
 * Data container for mixed bivariate log-normal model, where data for X,Y are reordered such that sigmasZ are appropriately ordered
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

  private final double _driftCorrection;

  /**
   * Set up mixed log-normal models with mixed normal variables X, Y and another mixed log-normal model with Z = X-Y
   * @param weights The weights  <b>These weights must sum to 1</b> 
   * @param sigmasX The standard deviation of the individual normal distributions in X 
   * @param sigmasY The standard deviation of the individual normal distributions in Y 
   * @param rhos The correlation between distributions of X and Y
   */
  public MixedBivariateLogNormalModelVolatility(final double[] weights, final double[] sigmasX, final double[] sigmasY, final double[] rhos) {
    ArgumentChecker.notNull(weights, "weights is Null");
    ArgumentChecker.notNull(sigmasX, "sigmasX is Null");
    ArgumentChecker.notNull(sigmasY, "sigmasY is Null");
    ArgumentChecker.notNull(rhos, "rhos is Null");

    ArgumentChecker.isTrue(sigmasX.length == sigmasY.length, "sigmasX must be the same length as sigmasY");
    ArgumentChecker.isTrue(sigmasX.length == rhos.length, "sigmasX must be the same length as rhos");
    ArgumentChecker.isTrue(sigmasX.length == weights.length, "sigmasX must be the same length as weights");

    final int nNormals = sigmasX.length;

    for (int i = 0; i < nNormals; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(weights[i]), "weights containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(weights[i]), "weights containing Infinity");
      ArgumentChecker.isFalse(Double.isNaN(sigmasX[i]), "sigmasX containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(sigmasX[i]), "sigmasX containing Infinity");
      ArgumentChecker.isFalse(Double.isNaN(sigmasY[i]), "sigmasY containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(sigmasY[i]), "sigmasY containing Infinity");
      ArgumentChecker.isFalse(Double.isNaN(rhos[i]), "rhos containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(rhos[i]), "rhos containing Infinity");
    }

    _relativePartialForwardsX = new double[nNormals];
    _relativePartialForwardsY = new double[nNormals];
    Arrays.fill(_relativePartialForwardsX, 1.);
    Arrays.fill(_relativePartialForwardsY, 1.);

    _weights = new double[nNormals];
    _sigmasX = new double[nNormals];
    _sigmasY = new double[nNormals];
    _rhos = new double[nNormals];

    for (int i = 0; i < nNormals; ++i) {
      _weights[i] = weights[i];
      _sigmasX[i] = sigmasX[i];
      _sigmasY[i] = sigmasY[i];
      _rhos[i] = rhos[i];
    }

    double[] sigmas = getVolatilityZ(_sigmasX, _sigmasY, _rhos);
    double[] relativePartialForwards = getrelativePartialForwardZ(_relativePartialForwardsX, _relativePartialForwardsY, _sigmasX, _sigmasY, _rhos);
    _driftCorrection = getDriftCorrectionZ(_weights, _relativePartialForwardsX, _relativePartialForwardsY, _sigmasX, _sigmasY, _rhos);

    for (int i = 0; i < nNormals; ++i) {
      relativePartialForwards[i] *= _driftCorrection;
    }

    int j = 0;
    double tmpSigmas = 0;
    double tmpWeights = 0;
    double tmpRelativePartialForwards = 0;
    for (int i = 0; i < nNormals; ++i) {
      j = i;
      for (int k = i; k < nNormals; ++k) {
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

    ArgumentChecker.notNull(weights, "weights is Null");
    ArgumentChecker.notNull(sigmasX, "sigmasX is Null");
    ArgumentChecker.notNull(sigmasY, "sigmasY is Null");
    ArgumentChecker.notNull(relativePartialForwardsX, "relativePartialForwardsX is Null");
    ArgumentChecker.notNull(relativePartialForwardsY, "relativePartialForwardsY is Null");
    ArgumentChecker.notNull(rhos, "rhos is Null");

    ArgumentChecker.isTrue(sigmasX.length == sigmasY.length, "sigmasX must be the same length as sigmasY");
    ArgumentChecker.isTrue(sigmasX.length == rhos.length, "sigmasX must be the same length as rhos");
    ArgumentChecker.isTrue(sigmasX.length == weights.length, "sigmasX must be the same length as weights");
    ArgumentChecker.isTrue(sigmasX.length == relativePartialForwardsX.length, "sigmasX must be the same length as relativePartialForwardsX");
    ArgumentChecker.isTrue(sigmasX.length == relativePartialForwardsY.length, "sigmasX must be the same length as relativePartialForwardsY");

    final int nNormals = sigmasX.length;

    for (int i = 0; i < nNormals; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(weights[i]), "weights containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(weights[i]), "weights containing Infinity");
      ArgumentChecker.isFalse(Double.isNaN(sigmasX[i]), "sigmasX containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(sigmasX[i]), "sigmasX containing Infinity");
      ArgumentChecker.isFalse(Double.isNaN(sigmasY[i]), "sigmasY containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(sigmasY[i]), "sigmasY containing Infinity");
      ArgumentChecker.isFalse(Double.isNaN(relativePartialForwardsX[i]), "relativePartialForwardsX containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(relativePartialForwardsX[i]), "relativePartialForwardsX containing Infinity");
      ArgumentChecker.isFalse(Double.isNaN(relativePartialForwardsY[i]), "relativePartialForwardsY containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(relativePartialForwardsY[i]), "relativePartialForwardsY containing Infinity");
      ArgumentChecker.isFalse(Double.isNaN(rhos[i]), "rhos containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(rhos[i]), "rhos containing Infinity");
    }

    _weights = new double[nNormals];
    _sigmasX = new double[nNormals];
    _sigmasY = new double[nNormals];
    _relativePartialForwardsX = new double[nNormals];
    _relativePartialForwardsY = new double[nNormals];
    _rhos = new double[nNormals];

    for (int i = 0; i < nNormals; ++i) {
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

    for (int i = 0; i < nNormals; ++i) {
      relativePartialForwards[i] *= _driftCorrection;
    }

    // sigmas should be descending order.
    // Labels for input variables of X,Y are also changed. These will be useful for rho-fitting of Z (see MixedLogNormal2DCorrelationFinder). 
    int j = 0;
    double tmpSigmas = 0;
    double tmpWeights = 0;
    double tmpRelativePartialForwards = 0;
    for (int i = 0; i < nNormals; ++i) {
      j = i;
      for (int k = i; k < nNormals; ++k) {
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
   * Access drift correction
   * @return _driftCorrection
   */
  public double getInvExpDriftCorrection() {
    return _driftCorrection;
  }

  /**
   * Access weights ordered in terms of sigmasZ
   * @return weights
   */
  public double[] getOrderedWeights() {
    return _data.getWeights();
  }

  /**
   * @return input sigmas for Z computed from sigmasX, sigmasY and rhos by getVolatilityZ(double[], double[], double[])
   */
  public double[] getSigmasZ() {
    return _data.getVolatilities();
  }

  /**
   * Access relativeForwardsZ
   * @return relativeForwardsZ
   */
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

  /**
   * Call price for Z, used for checking call-put parity
   * @param option 
   * @param forward 
   * @return call price  
   */
  public double getPriceZ(final EuropeanVanillaOption option, final double forward) {
    final MixedLogNormalVolatilityFunction volfunc = MixedLogNormalVolatilityFunction.getInstance();
    return volfunc.getPrice(option, forward, _data);
  }

  /**
   * Access sigmasX reordered in terms of sigmasZ
   * @return sigmasX
   */
  public double[] getOrderedSigmasX() {
    return _sigmasX;
  }

  /**
   * Access sigmasY reordered in terms of sigmasZ
   * @return sigmasY
   */
  public double[] getOrderedSigmasY() {
    return _sigmasY;
  }

  /**
   * Access relativePartialForwardsX reordered in terms of sigmasZ
   * @return relativePartialForwardsX
   */
  public double[] getOrderedRelativePartialForwardsX() {
    return _relativePartialForwardsX;
  }

  /**
   * Access relativePartialForwardsY reordered in terms of sigmasZ
   * @return relativePartialForwardsY
   */
  public double[] getOrderedRelativePartialForwardsY() {
    return _relativePartialForwardsY;
  }

}
