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
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalModelData;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalVolatilityFunction;
import com.opengamma.analytics.math.linearalgebra.Decomposition;
import com.opengamma.analytics.math.linearalgebra.LUDecompositionCommons;
import com.opengamma.analytics.math.linearalgebra.LUDecompositionResult;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 * Fitting volatility smiles of two mixed log-normal models with mixed normal variables X,Y.
 * As model constraints are eventually violated starting with some initial guess parameters,
 * the LM algorithm is modified such that model parameters satisfy all of the constraints in every step of the fitting iteration.
 * X,Y should have the same number of normal distributions and share all of the weights.
 *
 * For a mixture of N log-normal distributions, a mixed log-normal model contains 3 * N -2 free parameters.
 * Because sets of weights of X,Y are identical, total number of free parameters of X,Y is 5 * N -3.
 */
public class MixedBivariateLogNormalFitter {
  private static final Logger s_logger = LoggerFactory.getLogger(MixedBivariateLogNormalFitter.class);
  private static final int ITRMAX = 10000;
  private static final double EPS_1 = 1.E-14; //EPS_1 and EPS_2 should be chosen by users
  private static final double EPS_2 = 1.E-14; //
  private static final double TAU = 1.E-3;

  private double _shift;
  private double[] _paramsGuess;

  private double _finalSqu;
  private double _iniSqu;

  private final Random _randObj = new Random();
  private final Decomposition<LUDecompositionResult> _luObj = new LUDecompositionCommons();

  private final MixedLogNormalVolatilityFunction _volfunc = MixedLogNormalVolatilityFunction.getInstance();

  /**
   * Find a set of parameters such that sum ( (_dataStrikes - exactFunctionValue)^2 ) is minimum
   *
   * @param paramsGuess  Initial (unconstrained) guess parameters of X,Y to be chosen randomly
   * @param dataStrikes  Strike (market data). All the data of X should be before those of Y
   * @param dataVolatilities  Volatility (market data). all the data of X should be before those of Y
   * @param timeToExpiry  Time to Expiry
   * @param forwardX  Forward value of mixed log-normal model with X
   * @param forwardY  Forward value of mixed log-normal model with Y
   * @param nNormals  The number of normal distributions (X,Y have the same number of log-normal distributions)
   * @param nDataX  The number of sets of data (strike, vol) of X
   * @param paramsGuessCorrection  Set to be larger value for long expiry
   *
   */
  public void doFit(final double[] paramsGuess, final double[] dataStrikes, final double[] dataVolatilities, final double timeToExpiry, final double forwardX, final double forwardY,
      final int nNormals,
      final int nDataX, final double paramsGuessCorrection) {

    ArgumentChecker.notNull(paramsGuess, "paramsGuess");
    ArgumentChecker.notNull(dataStrikes, "dataStrikes");
    ArgumentChecker.notNull(dataVolatilities, "dataVolatilities");

    final int nParams = paramsGuess.length;
    final int nData = dataStrikes.length;

    ArgumentChecker.isTrue(nDataX < nData, "(dataX length) < (dataX length + dataY length)");
    ArgumentChecker.isTrue(dataStrikes.length == dataVolatilities.length, "dataStrikes not the same length as dataVols");
    ArgumentChecker.isTrue(nParams == 5 * nNormals - 3, "5 * N -3 free parameters");

    for (int i = 0; i < nData; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(dataStrikes[i]), "dataStrikes containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(dataStrikes[i]), "dataStrikes containing Infinity");
      ArgumentChecker.isFalse(Double.isNaN(dataVolatilities[i]), "dataVolatilities containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(dataVolatilities[i]), "dataVolatilities containing Infinity");
    }
    for (int i = 0; i < nParams; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(paramsGuess[i]), "paramsGuess containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(paramsGuess[i]), "paramsGuess containing Infinity");
    }
    ArgumentChecker.isFalse(Double.isNaN(timeToExpiry), "timeToExpiry containing NaN");
    ArgumentChecker.isFalse(Double.isInfinite(timeToExpiry), "timeToExpiry containing Infinity");
    ArgumentChecker.isFalse(Double.isNaN(forwardX), "forwardX containing NaN");
    ArgumentChecker.isFalse(Double.isInfinite(forwardX), "forwardX containing Infinity");
    ArgumentChecker.isFalse(Double.isNaN(forwardY), "forwardY containing NaN");
    ArgumentChecker.isFalse(Double.isInfinite(forwardY), "forwardY containing Infinity");
    ArgumentChecker.isFalse(Double.isNaN(paramsGuessCorrection), "paramsGuessCorrection containing NaN");
    ArgumentChecker.isFalse(Double.isInfinite(paramsGuessCorrection), "paramsGuessCorrection containing Infinity");

    ArgumentChecker.isTrue(timeToExpiry > 0, "timeToExpiry should be positive");
    ArgumentChecker.isTrue(forwardX > 0, "forwardX should be positive");
    ArgumentChecker.isTrue(forwardY > 0, "forwardY should be positive");
    ArgumentChecker.isTrue(paramsGuessCorrection > 0, "paramsGuessCorrection should be positive");

    double[] dataStrs = new double[nData];
    double[] dataVols = new double[nData];
    double[] dataDerivedYDiff = new double[nData];

    double[][] gradM = new double[nData][nParams];
    final double[] gradFunctionValueM = new double[nParams];
    final double[][] hessian = new double[nParams][nParams];
    _paramsGuess = new double[nParams];

    _paramsGuess = paramsGuess;
    final double time = timeToExpiry;
    final double fwdX = forwardX;
    final double fwdY = forwardY;
    final int nNorms = nNormals;
    final int nX = nDataX;
    final double pGuessCrrt = paramsGuessCorrection;

    dataStrs = Arrays.copyOf(dataStrikes, nData);
    dataVols = Arrays.copyOf(dataVolatilities, nData);

    Arrays.fill(gradFunctionValueM, 0.);
    for (int i = 0; i < nParams; ++i) {
      Arrays.fill(hessian[i], 0.);
    }

    int k = 0;
    double rho = 0.;
    _shift = 0.;
    double shiftModFac = 2.;
    boolean done = false;
    double[] paramsJump = new double[nParams];

    gradM = exactFunctionDerivative(_paramsGuess, dataStrs, time, fwdX, fwdY, nNorms, nX);
    dataDerivedYDiff = exactFunctionValue(_paramsGuess, dataStrs, dataVols, time, fwdX, fwdY, nNorms, nX);

    for (int i = 0; i < nParams; ++i) {
      for (int j = 0; j < nData; ++j) {
        gradFunctionValueM[i] += -gradM[j][i] * dataDerivedYDiff[j];
      }
    }

    for (int i = 0; i < nParams; ++i) {
      for (int j = 0; j < nParams; ++j) {
        for (int l = 0; l < nData; ++l) {
          hessian[i][j] += gradM[l][i] * gradM[l][j];
        }
      }
    }

    for (int i = 0; i < nParams; ++i) {
      if (hessian[i][i] > _shift) {
        _shift = hessian[i][i];
      }
    }
    _shift = TAU * _shift;

    _iniSqu = 0.5 * getVecNormSq(exactFunctionValue(_paramsGuess, dataStrs, dataVols, time, fwdX, fwdY, nNorms, nX));

    if (getVecNorm(gradFunctionValueM) <= EPS_1) {
      done = true;
      final double[] tmp = exactFunctionValue(_paramsGuess, dataStrs, dataVols, time, fwdX, fwdY, nNorms, nX);
      _finalSqu = 0.5 * getVecNormSq(tmp);
    }

    while (done == false && k < ITRMAX) {

      k = k + 1;

      ///confirming positive parameters and NotNaN
      boolean confPositiveParams = false;
      while (confPositiveParams == false) {

        paramsJump = theMatrixEqnSolver(dataStrs, dataVols, gradFunctionValueM, hessian, time, fwdX, fwdY, nNorms, nX);

        int nWarnings = 0;
        for (int i = 0; i < nParams; ++i) {
          final double tmpGuess = _paramsGuess[i] + paramsJump[i];
          if (tmpGuess <= 0. || Double.isNaN(paramsJump[i])) {
            ++nWarnings;
          }
        }
        if (nWarnings == 0) {

          confPositiveParams = true;
        } else {

          Arrays.fill(gradFunctionValueM, 0.);
          for (int i = 0; i < nParams; ++i) {
            Arrays.fill(hessian[i], 0.);
          }

          for (int i = 0; i < nParams; ++i) {
            _paramsGuess[i] = pGuessCrrt * (1e-2 + _randObj.nextDouble());
          }

          gradM = exactFunctionDerivative(_paramsGuess, dataStrs, time, fwdX, fwdY, nNorms, nX);
          dataDerivedYDiff = exactFunctionValue(_paramsGuess, dataStrs, dataVols, time, fwdX, fwdY, nNorms, nX);

          for (int i = 0; i < nParams; ++i) {
            for (int j = 0; j < nData; ++j) {
              gradFunctionValueM[i] += -gradM[j][i] * dataDerivedYDiff[j];
            }
          }

          for (int i = 0; i < nParams; ++i) {
            for (int j = 0; j < nParams; ++j) {
              for (int l = 0; l < nData; ++l) {
                hessian[i][j] += gradM[l][i] * gradM[l][j];
              }
            }
          }
          _shift = 0.;
          for (int i = 0; i < nParams; ++i) {
            if (hessian[i][i] > _shift) {
              _shift = hessian[i][i];
            }
          }
          _shift = TAU * _shift;

        }
      }
      ///

      if (getVecNorm(paramsJump) <= EPS_2 * (getVecNorm(_paramsGuess) + EPS_2)) {

        done = true;
        _paramsGuess = addVectors(_paramsGuess, paramsJump);
        _finalSqu = 0.5 * getVecNormSq(exactFunctionValue(_paramsGuess, dataStrs, dataVols, time, fwdX, fwdY, nNorms, nX));

      } else {

        rho = getGainRatio(paramsJump, dataStrs, dataVols, gradFunctionValueM, time, fwdX, fwdY, nNorms, nX);
        _paramsGuess = addVectors(_paramsGuess, paramsJump);

        if (rho > 0.) {

          Arrays.fill(gradFunctionValueM, 0.);
          for (int i = 0; i < nParams; ++i) {
            Arrays.fill(hessian[i], 0.);
          }

          gradM = exactFunctionDerivative(_paramsGuess, dataStrs, time, fwdX, fwdY, nNorms, nX);
          dataDerivedYDiff = exactFunctionValue(_paramsGuess, dataStrs, dataVols, time, fwdX, fwdY, nNorms, nX);

          for (int i = 0; i < nParams; ++i) {
            for (int j = 0; j < nData; ++j) {
              gradFunctionValueM[i] += -gradM[j][i] * dataDerivedYDiff[j];
            }
          }

          for (int i = 0; i < nParams; ++i) {
            for (int j = 0; j < nParams; ++j) {
              for (int l = 0; l < nData; ++l) {
                hessian[i][j] += gradM[l][i] * gradM[l][j];
              }
            }
          }

          if (getVecNorm(gradFunctionValueM) <= EPS_1) {
            _finalSqu = 0.5 * getVecNormSq(exactFunctionValue(_paramsGuess, dataStrs, dataVols, time, fwdX, fwdY, nNorms, nX));
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
        s_logger.error("Too Many Iterations");
        _finalSqu = 0.5 * getVecNormSq(exactFunctionValue(_paramsGuess, dataStrs, dataVols, time, fwdX, fwdY, nNorms, nX));
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
   * @return Present value of (unconstrained) model parameters in order of (sigmasX, sigmasY, unconstrained weights, unconstrained prfX, unconstrained prfY)
   */
  public double[] getParams() {
    return _paramsGuess;
  }

  /**
   * @param jump Parameter jump
   * @param dataStrs Strike data
   * @param dataVols Volatility data
   * @param gradFunctionValueM Gradient of the exact function value
   * @param time Time to expiry
   * @param fwdX Forward of X
   * @param fwdY Forward of Y
   * @param nNorms Number of Normals
   * @param nX Number of data in the X part
   * @return Gain ratio which controls update of _shift
   */
  private double getGainRatio(final double[] jump, final double[] dataStrs, final double[] dataVols, final double[] gradFunctionValueM, final double time, final double fwdX, final double fwdY,
      final int nNorms,
      final int nX) {

    return exactFunctionDiff(jump, dataStrs, dataVols, time, fwdX, fwdY, nNorms, nX) / apprxFunctionDiff(jump, gradFunctionValueM);
  }

  /**
   * @return Denominator of gain ratio
   */
  private double apprxFunctionDiff(final double[] jump, final double[] gradFunctionValueM) {
    final int nParams = jump.length;
    double tmp = 0.;
    for (int i = 0; i < nParams; ++i) {
      tmp += 0.5 * jump[i] * (_shift * jump[i] + gradFunctionValueM[i]);
    }

    return tmp;

  }

  /**
   * @return Numerator of gain ratio
   */
  private double exactFunctionDiff(final double[] jump, final double[] dataStrs, final double[] dataVols, final double time, final double fwdX, final double fwdY, final int nNorms,
      final int nX) {
    final int nParams = jump.length;
    final int nData = dataStrs.length;

    final double[] tmp0 = exactFunctionValue(_paramsGuess, dataStrs, dataVols, time, fwdX, fwdY, nNorms, nX);
    double[] newParams = new double[nParams];
    double[] tmp1 = new double[nData];

    newParams = addVectors(_paramsGuess, jump);

    tmp1 = exactFunctionValue(newParams, dataStrs, dataVols, time, fwdX, fwdY, nNorms, nX);

    return getVecNormSq(tmp0) / 2. - getVecNormSq(tmp1) / 2.;

  }

  /**
   * During the iterations of least-square fitting, a set of parameters sometimes breaks the condition 0 < targetPrice < Math.min(forward, strike).
   * Do not use getImpliedVolatilityZ method in MixedLogNormalModel2DVolatility.
   * @param option
   * @param forward
   * @param data
   * @return The volatility
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
   * @param params Present guess parameters
   * @param dataStrs Strike data
   * @param dataVols Volatility data
   * @param time Time to expiry
   * @param fwdX Forward of X
   * @param fwdY Forward of Y
   * @param nNorms Number of Normals
   * @param nX Number of data in the X part
   * @return Difference between a market value of volatility and implied volatility with guess parameters
   */
  private double[] exactFunctionValue(final double[] params, final double[] dataStrs, final double[] dataVols, final double time, final double fwdX, final double fwdY,
      final int nNorms, final int nX) {

    final int nData = dataStrs.length;
    final int dof = 3 * nNorms - 2;

    final double[] paramsX = new double[dof];
    final double[] paramsY = new double[dof];

    for (int i = 0; i < nNorms; ++i) {
      paramsX[i] = params[i];
    }
    for (int i = 0; i < nNorms - 1; ++i) {
      paramsX[i + nNorms] = params[i + 2 * nNorms];
      paramsX[i + 2 * nNorms - 1] = params[i + 3 * nNorms - 1];
    }

    for (int i = 0; i < nNorms; ++i) {
      paramsY[i] = params[i + nNorms];
    }
    for (int i = 0; i < nNorms - 1; ++i) {
      paramsY[i + nNorms] = params[i + 2 * nNorms];
      paramsY[i + 2 * nNorms - 1] = params[i + 4 * nNorms - 2];
    }

    final MixedLogNormalModelData dataX = new MixedLogNormalModelData(paramsX, true);
    final MixedLogNormalModelData dataY = new MixedLogNormalModelData(paramsY, true);

    final double[] res = new double[nData];
    Arrays.fill(res, 0.);

    for (int j = 0; j < nX; ++j) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(dataStrs[j], time, true);
      res[j] = dataVols[j] - getVolatility(option, fwdX, dataX);
    }

    for (int j = nX; j < nData; ++j) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(dataStrs[j], time, true);
      res[j] = dataVols[j] - getVolatility(option, fwdY, dataY);
    }

    return res;
  }

  /**
   * @param params Present guess parameters
   * @param dataStrs Strike data
   * @param time Time to expiry
   * @param fwdX Forward of X
   * @param fwdY Forward of Y
   * @param nNorms Number of Normals
   * @param nX Number of data in the X part
   * @return First derivatives of exactFunctionValue in terms of unconstrained model parameters
   */
  private double[][] exactFunctionDerivative(final double[] params, final double[] dataStrs, final double time, final double fwdX, final double fwdY, final int nNorms, final int nX) {

    final int nData = dataStrs.length;
    final int nParams = params.length;
    final int dof = 3 * nNorms - 2;
    final double[] paramsX = new double[dof];
    final double[] paramsY = new double[dof];

    for (int i = 0; i < nNorms; ++i) {
      paramsX[i] = params[i];
    }
    for (int i = 0; i < nNorms - 1; ++i) {
      paramsX[i + nNorms] = params[i + 2 * nNorms];
      paramsX[i + 2 * nNorms - 1] = params[i + 3 * nNorms - 1];
    }

    for (int i = 0; i < nNorms; ++i) {
      paramsY[i] = params[i + nNorms];
    }
    for (int i = 0; i < nNorms - 1; ++i) {
      paramsY[i + nNorms] = params[i + 2 * nNorms];
      paramsY[i + 2 * nNorms - 1] = params[i + 4 * nNorms - 2];
    }

    final MixedLogNormalModelData dataX = new MixedLogNormalModelData(paramsX, true);
    final MixedLogNormalModelData dataY = new MixedLogNormalModelData(paramsY, true);

    final double[][] res = new double[nData][nParams];
    for (int i = 0; i < nData; ++i) {
      Arrays.fill(res[i], 0.);
    }

    final double[] weightsX = dataX.getWeights();
    final double[] weightsY = dataY.getWeights();
    final double[] sigmasX = dataX.getVolatilities();
    final double[] sigmasY = dataY.getVolatilities();
    final double[] relativeForwardsX = dataX.getRelativeForwards();
    final double[] relativeForwardsY = dataY.getRelativeForwards();
    final double[][] weightsJacobianX = dataX.getWeightsJacobian();
    final double[][] weightsJacobianY = dataY.getWeightsJacobian();
    final double[][] relativeForwardsJacobianX = dataX.getRelativeForwardsJacobian();
    final double[][] relativeForwardsJacobianY = dataY.getRelativeForwardsJacobian();

    for (int j = 0; j < nX; ++j) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(dataStrs[j], time, true);
      final double impVolX = getVolatility(option, fwdX, dataX);
      for (int i = 0; i < nNorms; ++i) {
        for (int l = i; l < nNorms; ++l) {
          res[j][i] += -fwdX * weightsX[l] * BlackFormulaRepository.vega(relativeForwardsX[l], dataStrs[j] / fwdX, time, sigmasX[l]) /
              BlackFormulaRepository.vega(fwdX, dataStrs[j], time, impVolX);
        }
      }
      for (int i = 2 * nNorms; i < 3 * nNorms - 1; ++i) {
        for (int l = 0; l < nNorms; ++l) {
          res[j][i] += -fwdX *
              (BlackFormulaRepository.price(relativeForwardsX[l], dataStrs[j] / fwdX, time, sigmasX[l], true) - relativeForwardsX[l] *
                  BlackFormulaRepository.delta(relativeForwardsX[l], dataStrs[j] / fwdX, time, sigmasX[l], true)) * weightsJacobianX[l][i - 2 * nNorms] /
                  BlackFormulaRepository.vega(fwdX, dataStrs[j], time, impVolX);
        }
      }
      for (int i = 3 * nNorms - 1; i < 4 * nNorms - 2; ++i) {
        for (int l = 0; l < nNorms; ++l) {
          res[j][i] += -fwdX * BlackFormulaRepository.delta(relativeForwardsX[l], dataStrs[j] / fwdX, time, sigmasX[l], true) *
              relativeForwardsJacobianX[l][i - (3 * nNorms - 1)] /
              BlackFormulaRepository.vega(fwdX, dataStrs[j], time, impVolX);
        }
      }
    }

    for (int j = nX; j < nData; ++j) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(dataStrs[j], time, true);
      final double impVolY = getVolatility(option, fwdY, dataY);
      for (int i = nNorms; i < 2 * nNorms; ++i) {
        for (int l = i; l < 2 * nNorms; ++l) {
          res[j][i] += -fwdY * weightsY[l - nNorms] * BlackFormulaRepository.vega(relativeForwardsY[l - nNorms], dataStrs[j] / fwdY, time, sigmasY[l - nNorms]) /
              BlackFormulaRepository.vega(fwdY, dataStrs[j], time, impVolY);
        }
      }
      for (int i = 2 * nNorms; i < 3 * nNorms - 1; ++i) {
        for (int l = 0; l < nNorms; ++l) {
          res[j][i] += -fwdY * (BlackFormulaRepository.price(relativeForwardsY[l], dataStrs[j] / fwdY, time, sigmasY[l], true) - relativeForwardsY[l] *
              BlackFormulaRepository.delta(relativeForwardsY[l], dataStrs[j] / fwdY, time, sigmasY[l], true)) * weightsJacobianY[l][i - 2 * nNorms] /
              BlackFormulaRepository.vega(fwdY, dataStrs[j], time, impVolY);
        }
      }
      for (int i = 4 * nNorms - 2; i < 5 * nNorms - 3; ++i) {
        for (int l = 0; l < nNorms; ++l) {
          res[j][i] += -fwdY * BlackFormulaRepository.delta(relativeForwardsY[l], dataStrs[j] / fwdY, time, sigmasY[l], true) *
              relativeForwardsJacobianY[l][i - (4 * nNorms - 2)] /
              BlackFormulaRepository.vega(fwdY, dataStrs[j], time, impVolY);
        }
      }
    }

    return res;
  }

  /**
   *  Solve the matrix equation ( hessian + shift (Id matrix) ) jump = gradFunctionValueM
   * @param dataStrs Strike data
   * @param dataVols Volatility data
   * @param gradFunctionValueM
   * @param hessian
   * @param time Time to expiry
   * @param fwdX Forward of X
   * @param fwdY Forward of Y
   * @param nNorms Number of Normals
   * @param nX Number of data in the X part
   * @return jump
   */
  private double[] theMatrixEqnSolver(final double[] dataStrs, final double[] dataVols, final double[] gradFunctionValueM, final double[][] hessian, final double time, final double fwdX,
      final double fwdY,
      final int nNorms,
      final int nX) {

    final int nParams = gradFunctionValueM.length;

    final double[][] toBeInv = new double[nParams][nParams];
    for (int i = 0; i < nParams; ++i) {
      toBeInv[i] = Arrays.copyOf(hessian[i], nParams);
    }

    final double tmpDerivativeNorm = getVecNorm(gradFunctionValueM);
    final double tmpSquFact = 0.02 * 0.5 * getVecNormSq(exactFunctionValue(_paramsGuess, dataStrs, dataVols, time, fwdX, fwdY, nNorms, nX));
    if (tmpDerivativeNorm <= tmpSquFact) {
      _shift = TAU * _shift;
    }

    for (int i = 0; i < nParams; ++i) {
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
   * @param vector
   * @return Norm of vector
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
}
