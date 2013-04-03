/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import static com.opengamma.analytics.math.matrix.MatrixAlgebraFactory.OG_ALGEBRA;

import java.util.Arrays;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedBivariateLogNormalModelVolatility;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalModelData;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalVolatilityFunction;
import com.opengamma.analytics.math.linearalgebra.Decomposition;
import com.opengamma.analytics.math.linearalgebra.LUDecompositionCommons;
import com.opengamma.analytics.math.linearalgebra.LUDecompositionResult;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 * Once given a set of parameters of two log-normal models with normal variables X,Y,
 * find the best-fit correlations X,Y by applying least square method to volatility smile of another mixed log-normal model associated with Z = X-Y.
 * The algorithm is modified such that model constraints are satisfied in every iteration step of fitting.
 */
public class MixedBivariateLogNormalCorrelationFinder {
  private static final Logger s_logger = LoggerFactory.getLogger(MixedBivariateLogNormalCorrelationFinder.class);
  private static final int ITRMAX = 10000;
  private static final double EPS_1 = 1.E-14;
  private static final double EPS_2 = 1.E-14;
  private static final double TAU = 1.E-3;

  private double _shift;
  private double[] _rhosGuess;

  private double _finalSqu;
  private double _iniSqu;

  private final Random _randObj = new Random();
  private final Decomposition<LUDecompositionResult> _luObj = new LUDecompositionCommons();
  private final MixedLogNormalVolatilityFunction _volfunc = MixedLogNormalVolatilityFunction.getInstance();

  /**
   * Find a set of correlations such that sum ( (_dataStrikes - exactFunctionValue)^2 ) is minimum.
   * @param rhosGuess  Initial geuss rhos
   * @param dataStrikes  Strikes (market data) of Z
   * @param dataVolatilities  Volatilities (market data) of Z
   * @param timeToExpiry  The time to expiry
   * @param weights  The weights  <b>These weights must sum to 1</b>
   * @param sigmasX  The standard deviation of the normal distributions in X
   * @param sigmasY  The standard deviation of the normal distributions in Y
   * @param relativePartialForwardsX  The expectation of each distribution in X is rpf_i*forward
   * @param relativePartialForwardsY  The expectation of each distribution in Y is rpf_i*forward
   * (rpf_i is the ith relativePartialForwards)
   * <b>Must have sum w_i*rpf_i = 1.0</b>
   * @param forwardX  The forward of X
   * @param forwardY  The forward of Y
   */
  public void doFit(final double[] rhosGuess, final double[] dataStrikes, final double[] dataVolatilities, final double timeToExpiry, final double[] weights, final double[] sigmasX,
      final double[] sigmasY,
      final double[] relativePartialForwardsX, final double[] relativePartialForwardsY, final double forwardX, final double forwardY) {

    ArgumentChecker.notNull(rhosGuess, "rhosGuess");
    ArgumentChecker.notNull(dataStrikes, "dataStrikes");
    ArgumentChecker.notNull(dataVolatilities, "dataVolatilities");
    ArgumentChecker.notNull(weights, "weights");
    ArgumentChecker.notNull(sigmasX, "sigmasX");
    ArgumentChecker.notNull(sigmasY, "sigmasY");
    ArgumentChecker.notNull(relativePartialForwardsX, "relativePartialForwardsX");
    ArgumentChecker.notNull(relativePartialForwardsY, "relativePartialForwardsY");

    final int nNormals = rhosGuess.length;
    final int nData = dataStrikes.length;

    ArgumentChecker.isTrue(dataStrikes.length == dataVolatilities.length, "dataStrikes not the same length as dataVols");
    ArgumentChecker.isTrue(weights.length == sigmasX.length, "weights not the same length as sigmasX");
    ArgumentChecker.isTrue(weights.length == sigmasY.length, "weights not the same length as sigmasY");
    ArgumentChecker.isTrue(weights.length == relativePartialForwardsX.length, "weights not the same length as relativePartialForwardsX");
    ArgumentChecker.isTrue(weights.length == relativePartialForwardsY.length, "weights not the same length as relativePartialForwardsY");

    for (int i = 0; i < nData; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(dataStrikes[i]), "dataStrikes containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(dataStrikes[i]), "dataStrikes containing Infinity");
      ArgumentChecker.isFalse(Double.isNaN(dataVolatilities[i]), "dataVolatilities containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(dataVolatilities[i]), "dataVolatilities containing Infinity");
    }
    for (int i = 0; i < nNormals; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(rhosGuess[i]), "rhosGuess containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(rhosGuess[i]), "rhosGuess containing Infinity");
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
    }
    ArgumentChecker.isFalse(Double.isNaN(timeToExpiry), "timeToExpiry containing NaN");
    ArgumentChecker.isFalse(Double.isInfinite(timeToExpiry), "timeToExpiry containing Infinity");
    ArgumentChecker.isFalse(Double.isNaN(forwardX), "forwardX containing NaN");
    ArgumentChecker.isFalse(Double.isInfinite(forwardX), "forwardX containing Infinity");
    ArgumentChecker.isFalse(Double.isNaN(forwardY), "forwardY containing NaN");
    ArgumentChecker.isFalse(Double.isInfinite(forwardY), "forwardY containing Infinity");

    ArgumentChecker.isTrue(timeToExpiry > 0, "timeToExpiry should be positive");
    ArgumentChecker.isTrue(forwardX > 0, "forwardX should be positive");
    ArgumentChecker.isTrue(forwardY > 0, "forwardY should be positive");

    double[] dataDerivedYDiff = new double[nData];
    double[][] gradM = new double[nData][nNormals];
    final double[] gradFunctionValueM = new double[nNormals];
    final double[][] hessian = new double[nNormals][nNormals];

    final double forwardZ = forwardX / forwardY;

    final double[] dataStrs = dataStrikes;
    final double[] dataVols = dataVolatilities;

    final double time = timeToExpiry;

    final double[] wghts = weights;
    final double[] sigsX = sigmasX;
    final double[] sigsY = sigmasY;
    final double[] rpfsX = relativePartialForwardsX;
    final double[] rpfsY = relativePartialForwardsY;
    _rhosGuess = rhosGuess;

    Arrays.fill(gradFunctionValueM, 0.);
    for (int i = 0; i < nNormals; ++i) {
      Arrays.fill(hessian[i], 0.);
    }

    int k = 0;
    double rho = 0.;
    _shift = 0;
    double shiftModFac = 2.;
    boolean done = false;
    double[] rhosJump = new double[nNormals];

    gradM = exactFunctionDerivative(_rhosGuess, dataStrs, time, wghts, sigsX, sigsY, rpfsX, rpfsY, forwardZ);
    dataDerivedYDiff = exactFunctionValue(_rhosGuess, dataStrs, dataVols, time, wghts, sigsX, sigsY, rpfsX, rpfsY, forwardZ);

    for (int i = 0; i < nNormals; ++i) {
      for (int j = 0; j < nData; ++j) {
        gradFunctionValueM[i] += -gradM[j][i] * dataDerivedYDiff[j];
      }
    }

    for (int i = 0; i < nNormals; ++i) {
      for (int j = 0; j < nNormals; ++j) {
        for (int l = 0; l < nData; ++l) {
          hessian[i][j] += gradM[l][i] * gradM[l][j];
        }
      }
    }

    for (int i = 0; i < nNormals; ++i) {
      if (hessian[i][i] > _shift) {
        _shift = hessian[i][i];
      }
    }
    _shift = TAU * _shift;

    _iniSqu = 0.5 * getVecNormSq(exactFunctionValue(_rhosGuess, dataStrs, dataVols, time, wghts, sigsX, sigsY, rpfsX, rpfsY, forwardZ));

    if (getVecNorm(gradFunctionValueM) <= EPS_1) {
      done = true;
      final double[] tmp = exactFunctionValue(_rhosGuess, dataStrs, dataVols, time, wghts, sigsX, sigsY, rpfsX, rpfsY, forwardZ);
      _finalSqu = 0.5 * getVecNormSq(tmp);
    }

    while (done == false && k < ITRMAX) {

      k = k + 1;

      ///confirming -1<= rhos <=1 and NotNaN
      boolean confRhos = false;
      while (confRhos == false) {

        rhosJump = theMatrixEqnSolver(dataStrs, dataVols, gradFunctionValueM, hessian, time, wghts, sigsX, sigsY, rpfsX, rpfsY, forwardZ);

        int nOutOfRange = 0;
        for (int i = 0; i < nNormals; ++i) {
          final double tmpGuess = _rhosGuess[i] + rhosJump[i];
          if (tmpGuess < -1. || tmpGuess > 1. || Double.isNaN(tmpGuess)) {
            ++nOutOfRange;
          }
        }
        if (nOutOfRange == 0) {
          confRhos = true;
        } else {

          for (int i = 0; i < nNormals; ++i) {
            _rhosGuess[i] = _randObj.nextDouble();
          }

          gradM = exactFunctionDerivative(_rhosGuess, dataStrs, time, wghts, sigsX, sigsY, rpfsX, rpfsY, forwardZ);
          dataDerivedYDiff = exactFunctionValue(_rhosGuess, dataStrs, dataVols, time, wghts, sigsX, sigsY, rpfsX, rpfsY, forwardZ);

          for (int i = 0; i < nNormals; ++i) {
            for (int j = 0; j < nData; ++j) {
              gradFunctionValueM[i] += -gradM[j][i] * dataDerivedYDiff[j];
            }
          }

          for (int i = 0; i < nNormals; ++i) {
            for (int j = 0; j < nNormals; ++j) {
              for (int l = 0; l < nData; ++l) {
                hessian[i][j] += gradM[l][i] * gradM[l][j];
              }
            }
          }

          _shift = 0.;
          for (int i = 0; i < nNormals; ++i) {
            if (hessian[i][i] > _shift) {
              _shift = hessian[i][i];
            }
          }
          _shift = TAU * _shift;
        }

      }
      ///

      if (getVecNorm(rhosJump) <= EPS_2 * (getVecNorm(_rhosGuess) + EPS_2)) {

        done = true;
        _rhosGuess = addVectors(_rhosGuess, rhosJump);
        _finalSqu = 0.5 * getVecNormSq(exactFunctionValue(_rhosGuess, dataStrs, dataVols, time, wghts, sigsX, sigsY, rpfsX, rpfsY, forwardZ));

      } else {

        rho = getGainRatio(rhosJump, dataStrs, dataVols, gradFunctionValueM, time, wghts, sigsX, sigsY, rpfsX, rpfsY, forwardZ);
        _rhosGuess = addVectors(_rhosGuess, rhosJump);

        if (rho > 0.) {

          Arrays.fill(gradFunctionValueM, 0.);
          for (int i = 0; i < nNormals; ++i) {
            Arrays.fill(hessian[i], 0.);
          }

          gradM = exactFunctionDerivative(_rhosGuess, dataStrs, time, wghts, sigsX, sigsY, rpfsX, rpfsY, forwardZ);
          dataDerivedYDiff = exactFunctionValue(_rhosGuess, dataStrs, dataVols, time, wghts, sigsX, sigsY, rpfsX, rpfsY, forwardZ);

          for (int i = 0; i < nNormals; ++i) {
            for (int j = 0; j < nData; ++j) {
              gradFunctionValueM[i] += -gradM[j][i] * dataDerivedYDiff[j];
            }
          }

          for (int i = 0; i < nNormals; ++i) {
            for (int j = 0; j < nNormals; ++j) {
              for (int l = 0; l < nData; ++l) {
                hessian[i][j] += gradM[l][i] * gradM[l][j];
              }
            }
          }

          if (getVecNorm(gradFunctionValueM) <= EPS_1) {
            _finalSqu = 0.5 * getVecNormSq(exactFunctionValue(_rhosGuess, dataStrs, dataVols, time, wghts, sigsX, sigsY, rpfsX, rpfsY, forwardZ));
            done = true;
          }

          _shift = _shift * Math.max(1. / 3., 1. - (2. * rho - 1.) * (2. * rho - 1.) * (2. * rho - 1.));
          shiftModFac = 2.;

        } else {

          _shift = _shift * shiftModFac;
          shiftModFac = 2. * shiftModFac;

        }

      }

      if (k == ITRMAX) {
        s_logger.error("Too many iterations");
        _finalSqu = 0.5 * getVecNormSq(exactFunctionValue(_rhosGuess, dataStrs, dataVols, time, wghts, sigsX, sigsY, rpfsX, rpfsY, forwardZ));
      }
    }

  }

  /**
   * @return Value of chi-square evaluated with initial guess
   */
  public double getInitialSq() {
    return _iniSqu;
  }

  /**
   * @return Final value of chi-square
   */
  public double getFinalSq() {
    return _finalSqu;
  }

  /**
   * @return Present value of correlations
   */
  public double[] getParams() {
    return _rhosGuess;
  }

  /**
   * @param jump Parameter jump
   * @param dataStrs Strike data
   * @param dataVols Volatility data
   * @param gradFunctionValueM Gradient of the exact function value
   * @param time Time to expiry
   * @param wghts Weights
   * @param sigsX Sigmas of X
   * @param sigsY Sigmas of Y
   * @param rpfsX Relative Partial forwards of X
   * @param rpfsY Relative Partial forwards of Y
   * @param forwardZ Forward of Z
   * @return Gain ratio which controls update of _shift
   */
  private double getGainRatio(final double[] jump, final double[] dataStrs, final double[] dataVols, final double[] gradFunctionValueM, final double time, final double[] wghts, final double[] sigsX,
      final double[] sigsY, final double[] rpfsX, final double[] rpfsY, final double forwardZ) {

    return exactFunctionDiff(jump, dataStrs, dataVols, time, wghts, sigsX, sigsY, rpfsX, rpfsY, forwardZ) / apprxFunctionDiff(jump, gradFunctionValueM);
  }

  /**
   * @return Denominator of gain ratio
   */
  private double apprxFunctionDiff(final double[] jump, final double[] gradFunctionValueM) {
    final int nNormals = gradFunctionValueM.length;

    double tmp = 0.;
    for (int i = 0; i < nNormals; ++i) {
      tmp += 0.5 * jump[i] * (_shift * jump[i] + gradFunctionValueM[i]);
    }

    return tmp;

  }

  /**
   * @return Numerator of gain ratio
   */
  private double exactFunctionDiff(final double[] jump, final double[] dataStrs, final double[] dataVols, final double time, final double[] wghts, final double[] sigsX, final double[] sigsY,
      final double[] rpfsX, final double[] rpfsY, final double forwardZ) {
    final int nNormals = jump.length;
    final int nData = dataStrs.length;

    final double[] tmp0 = exactFunctionValue(_rhosGuess, dataStrs, dataVols, time, wghts, sigsX, sigsY, rpfsX, rpfsY, forwardZ);
    double[] newParams = new double[nNormals];
    double[] tmp1 = new double[nData];

    newParams = addVectors(_rhosGuess, jump);

    tmp1 = exactFunctionValue(newParams, dataStrs, dataVols, time, wghts, sigsX, sigsY, rpfsX, rpfsY, forwardZ);

    return getVecNormSq(tmp0) / 2. - getVecNormSq(tmp1) / 2.;

  }

  /**
   * During the iterations of least-square fitting, a set of rhos sometimes breaks the condition 0 < targetPrice < Math.min(forward, strike).
   * Do not use getImpliedVolatilityZ method in MixedBivariateLogNormalModelVolatility.
   */
  private double getVolatility(final EuropeanVanillaOption option, final double forward, final MixedLogNormalModelData data) {

    final double price = _volfunc.getPrice(option, forward, data);
    final double strike = option.getStrike();
    final double t = option.getTimeToExpiry();
    final double k = option.getStrike();
    final boolean isCall = option.isCall();
    final double intrinsicPrice = Math.max(0, (isCall ? 1 : -1) * (forward - strike));
    double targetPrice = price - intrinsicPrice;

    if (targetPrice <= 0) {
      targetPrice = 0.;
    }

    if (targetPrice >= Math.min(forward, strike)) {
      targetPrice = 0.99 * Math.min(forward, strike);
    }

    final double sigmaGuess = 0.3;
    return BlackFormulaRepository.impliedVolatility(targetPrice, forward, k, t, sigmaGuess);
  }

  /**
   * @param rhos Present guess rho
   * @param dataStrs Strike data
   * @param dataVols Strike volatility
   * @param time Time to expiry
   * @param wghts Weights
   * @param sigsX Sigmas of X
   * @param sigsY Sigmas of Y
   * @param rpfsX Relative Partial forwards of X
   * @param rpfsY Relative Partial forwards of Y
   * @param forwardZ Forward of Z
   * @return Difference between a market value of volatility and implied volatility with guess parameters
   */
  private double[] exactFunctionValue(final double[] rhos, final double[] dataStrs, final double[] dataVols, final double time, final double[] wghts, final double[] sigsX, final double[] sigsY,
      final double[] rpfsX, final double[] rpfsY, final double forwardZ) {

    final int nData = dataStrs.length;
    final double[] res = new double[nData];
    Arrays.fill(res, 0.);

    final MixedBivariateLogNormalModelVolatility guessObjZ = new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rpfsX, rpfsY, rhos);
    final double[] sigmasZ = guessObjZ.getSigmasZ();
    final double[] relativePartialForwardsZ = guessObjZ.getRelativeForwardsZ();
    final double[] weightsZ = guessObjZ.getOrderedWeights();
    final MixedLogNormalModelData guessDataZ = new MixedLogNormalModelData(weightsZ, sigmasZ, relativePartialForwardsZ);

    for (int j = 0; j < nData; ++j) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(dataStrs[j], time, true);
      res[j] = dataVols[j] - getVolatility(option, forwardZ, guessDataZ);
    }

    return res;
  }

  /**
   * @param rhos Present guess rho
   * @param dataStrs Strike data
   * @param time Time to expiry
   * @param wghts Weights
   * @param sigsX Sigmas of X
   * @param sigsY Sigmas of Y
   * @param rpfsX Relative Partial forwards of X
   * @param rpfsY Relative Partial forwards of Y
   * @param forwardZ Forward of Z
   * @return First derivatives of exactFunctionValue in terms of rhos
   */
  private double[][] exactFunctionDerivative(final double[] rhos, final double[] dataStrs, final double time, final double[] wghts, final double[] sigsX, final double[] sigsY, final double[] rpfsX,
      final double[] rpfsY, final double forwardZ) {

    final int nNormals = rhos.length;
    final int nData = dataStrs.length;
    final double[][] res = new double[nData][nNormals];
    for (int i = 0; i < nData; ++i) {
      Arrays.fill(res[i], 0.);
    }

    final MixedBivariateLogNormalModelVolatility guessObjZ = new MixedBivariateLogNormalModelVolatility(wghts, sigsX, sigsY, rpfsX, rpfsY, rhos);
    final double[] sigmasZ = guessObjZ.getSigmasZ();
    final double[] relativePartialForwardsZ = guessObjZ.getRelativeForwardsZ();
    final double[] weightsZ = guessObjZ.getOrderedWeights();
    final double correction = guessObjZ.getInvExpDriftCorrection();
    final MixedLogNormalModelData guessDataZ = new MixedLogNormalModelData(weightsZ, sigmasZ, relativePartialForwardsZ);

    final double[] sigmasX = guessObjZ.getOrderedSigmasX();
    final double[] sigmasY = guessObjZ.getOrderedSigmasY();

    for (int j = 0; j < nData; ++j) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(dataStrs[j], time, true);
      final double impVolZ = getVolatility(option, forwardZ, guessDataZ);
      for (int i = 0; i < nNormals; ++i) {
        final double part1 = weightsZ[i] * forwardZ * BlackFormulaRepository.delta(relativePartialForwardsZ[i], dataStrs[j] / forwardZ, time, sigmasZ[i], true) * sigmasX[i] *
            sigmasY[i] * relativePartialForwardsZ[i] / BlackFormulaRepository.vega(forwardZ, dataStrs[j], time, impVolZ);
        final double part2 = forwardZ * weightsZ[i] * BlackFormulaRepository.vega(relativePartialForwardsZ[i], dataStrs[j] / forwardZ, time, sigmasZ[i]) * sigmasX[i] *
            sigmasY[i] / sigmasZ[i] / BlackFormulaRepository.vega(forwardZ, dataStrs[j], time, impVolZ);
        final double factor = weightsZ[i] * relativePartialForwardsZ[i] * sigmasX[i] * sigmasY[i] * correction * correction;
        double part3 = 0.;
        for (int l = 0; l < nNormals; ++l) {
          part3 += factor * weightsZ[l] * forwardZ * BlackFormulaRepository.delta(relativePartialForwardsZ[l], dataStrs[j] / forwardZ, time, sigmasZ[l], true) *
              relativePartialForwardsZ[l] /
              BlackFormulaRepository.vega(forwardZ, dataStrs[j], time, impVolZ);
        }
        res[j][i] = part1 + part2 - part3;
      }

    }

    return res;
  }

  /**
   * Solve the matrix equation ( hessian + shift (Id matrix) ) jump = gradFunctionValueM
   * @param dataStrs Strike data
   * @param dataVols Volatility data
   * @param gradFunctionValueM
   * @param hessian
   * @param time Time to expiry
   * @param wghts Weights
   * @param sigsX Sigmas of X
   * @param sigsY Sigmas of Y
   * @param rpfsX Relative Partial forwards of X
   * @param rpfsY Relative Partial forwards of Y
   * @param forwardZ Forward of Z
   * @return jump
   */
  private double[] theMatrixEqnSolver(final double[] dataStrs, final double[] dataVols, final double[] gradFunctionValueM, final double[][] hessian, final double time, final double[] wghts,
      final double[] sigsX, final double[] sigsY, final double[] rpfsX, final double[] rpfsY, final double forwardZ) {

    final int nNormals = gradFunctionValueM.length;
    final double[][] toBeInv = new double[nNormals][nNormals];
    for (int i = 0; i < nNormals; ++i) {
      toBeInv[i] = Arrays.copyOf(hessian[i], nNormals);
    }

    final double tmpDerivativeNorm = getVecNorm(gradFunctionValueM);
    final double tmpSquFact = 0.02 * 0.5 * getVecNormSq(exactFunctionValue(_rhosGuess, dataStrs, dataVols, time, wghts, sigsX, sigsY, rpfsX, rpfsY, forwardZ));
    if (tmpDerivativeNorm <= tmpSquFact) {
      _shift = TAU * _shift;
    }

    for (int i = 0; i < nNormals; ++i) {
      toBeInv[i][i] = toBeInv[i][i] + _shift;
    }

    return decompSol(toBeInv, gradFunctionValueM);

  }

  /**
   * Linear problem Ax=b where A is a square matrix and x,b are vector can be solved by LU decomposition
   * @param doubMat Matrix A
   * @param doubVec Vector B
   * @return Solution to the linear equation, x
   */
  protected double[] decompSol(final double[][] doubMat, final double[] doubVec) {
    final LUDecompositionResult result = _luObj.evaluate(new DoubleMatrix2D(doubMat));

    final double[][] lMat = result.getL().getData();
    final double[][] uMat = result.getU().getData();
    final double[] doubVecMod = ((DoubleMatrix1D) OG_ALGEBRA.multiply(result.getP(), new DoubleMatrix1D(doubVec))).getData();

    return backSubstitution(uMat, forwardSubstitution(lMat, doubVecMod));

  }

  /**
   * Linear problem Ax=b is solved by forward substitution if A is lower triangular
   * @param lMat Lower triangular matrix
   * @param doubVec Vector b
   * @return Solution to the linear equation, x
   */
  private double[] forwardSubstitution(final double[][] lMat, final double[] doubVec) {

    final int size = lMat.length;
    final double[] res = new double[size];

    for (int i = 0; i < size; ++i) {
      double tmp = doubVec[i] / lMat[i][i];
      for (int j = 0; j < i; ++j) {
        tmp -= lMat[i][j] * res[j] / lMat[i][i];
      }
      res[i] = tmp;
    }

    return res;
  }

  /**
   * Linear problem Ax=b is solved by backward substitution if A is upper triangular
   * @param uMat Upper triangular matrix
   * @param doubVec Vector b
   * @return Solution to the linear equation, x
   */
  private double[] backSubstitution(final double[][] uMat, final double[] doubVec) {

    final int size = uMat.length;
    final double[] res = new double[size];

    for (int i = size - 1; i > -1; --i) {
      double tmp = doubVec[i] / uMat[i][i];
      for (int j = i + 1; j < size; ++j) {
        tmp -= uMat[i][j] * res[j] / uMat[i][i];
      }
      res[i] = tmp;
    }

    return res;
  }

  /**
   *  Add vectorA to VectorB
   */
  private double[] addVectors(final double[] vecA, final double[] vecB) {
    final int dim = vecA.length;
    final double[] res = new double[dim];

    for (int i = 0; i < dim; ++i) {
      res[i] = vecA[i] + vecB[i];
    }

    return res;
  }

  /**
   * @param vector
   * @return norm of vector
   */
  private double getVecNorm(final double[] vec) {
    final int nVec = vec.length;
    double res = 0.;

    for (int i = 0; i < nVec; ++i) {
      res += vec[i] * vec[i];
    }

    return Math.sqrt(res);
  }

  /**
   * @param vector
   * @return Square of norm of vector
   */
  private double getVecNormSq(final double[] vec) {
    final int nVec = vec.length;
    double res = 0.;

    for (int i = 0; i < nVec; ++i) {
      res += (vec[i] * vec[i]);
    }

    return res;
  }
}
