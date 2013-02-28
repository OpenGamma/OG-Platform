/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import java.util.Arrays;
import java.util.Random;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedBivariateLogNormalModelVolatility;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalModelData;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalVolatilityFunction;
import com.opengamma.analytics.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.MatrixAlgebraFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * Once given a set of parameters of two log-normal models with normal variables X,Y, 
 * find the best-fit correlations X,Y by applying least square method to volatility smile of another mixed log-normal model associated with Z = X-Y. 
 * The LM algorithm is modified such that model constraints are satisfied in every iteration step of fitting. 
 */
public class MixedBivariateLogNormalCorrelationFinder {

  private static final int ITRMAX = 20000;
  private static final double EPS_1 = 1.E-10;
  private static final double EPS_2 = 1.E-10;

  private double _tau = 1.E-3;
  private double _shiftModFac = 2.;
  private double _shift;

  private double[] _rhosGuess;
  private double[] _dataStrikes;
  private double[] _dataVols;

  private double[] _dataDerivedYDiff;
  private double[][] _gradM;
  private double[] _gradFunctionValueM;
  private double[][] _hessian;

  private final int _nData;
  private final int _nNormals;

  private double[] _weights;
  private double[] _sigmasX;
  private double[] _sigmasY;
  private double[] _relativePartialForwardsX;
  private double[] _relativePartialForwardsY;
  private double _timeToExpiry;

  private double _forwardZ;

  private double _finalSqu;
  private double _iniSqu;

  private Random _randObj = new Random();
  private CommonsMatrixAlgebra _algComObj = new CommonsMatrixAlgebra();
  private final MatrixAlgebra _algOgObj = MatrixAlgebraFactory.getMatrixAlgebra("OG");

  private MixedLogNormalVolatilityFunction _volfunc = MixedLogNormalVolatilityFunction.getInstance();

  /**
   * 
   * @param rhosGuess  Initial geuss rhos
   * @param dataStrikes  Strikes (market data) of Z
   * @param dataVols  Volatilities (market data) of Z
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
  public MixedBivariateLogNormalCorrelationFinder(final double[] rhosGuess, final double[] dataStrikes, final double[] dataVols, final double timeToExpiry, final double[] weights, double[] sigmasX,
      final double[] sigmasY,
      final double[] relativePartialForwardsX, final double[] relativePartialForwardsY, final double forwardX, final double forwardY) {
    _nNormals = rhosGuess.length;
    _nData = dataStrikes.length;
    ArgumentChecker.isTrue(_nData == dataVols.length, "dataStrikes not the same length as dataVols");

    _dataDerivedYDiff = new double[_nData];
    _gradM = new double[_nData][_nNormals];
    _gradFunctionValueM = new double[_nNormals];
    _hessian = new double[_nNormals][_nNormals];

    _forwardZ = forwardX / forwardY;

    _dataStrikes = dataStrikes;
    _dataVols = dataVols;

    _timeToExpiry = timeToExpiry;

    _weights = weights;
    _sigmasX = sigmasX;
    _sigmasY = sigmasY;
    _relativePartialForwardsX = relativePartialForwardsX;
    _relativePartialForwardsY = relativePartialForwardsY;
    _rhosGuess = rhosGuess;

    Arrays.fill(_gradFunctionValueM, 0.);
    for (int i = 0; i < _nNormals; ++i) {
      Arrays.fill(_hessian[i], 0.);
    }

  }

  /**
   * Find a set of correlations such that sum ( (_dataStrikes - exactFunctionValue)^2 ) is minimum. 
   */
  public void doFit() {

    int k = 0;
    double rho = 0.;
    _shift = 0;
    boolean done = false;
    double[] rhosJump = new double[_nNormals];

    _gradM = exactFunctionDerivative(_rhosGuess);
    _dataDerivedYDiff = exactFunctionValue(_rhosGuess);

    for (int i = 0; i < _nNormals; ++i) {
      for (int j = 0; j < _nData; ++j) {
        _gradFunctionValueM[i] += -_gradM[j][i] * _dataDerivedYDiff[j];
      }
    }

    for (int i = 0; i < _nNormals; ++i) {
      for (int j = 0; j < _nNormals; ++j) {
        for (int l = 0; l < _nData; ++l) {
          _hessian[i][j] += _gradM[l][i] * _gradM[l][j];
        }
      }
    }

    for (int i = 0; i < _nNormals; ++i) {
      if (_hessian[i][i] > _shift) {
        _shift = _hessian[i][i];
      }
    }
    _shift = _tau * _shift;

    _iniSqu = 0.5 * getVecNormSq(exactFunctionValue(_rhosGuess));

    if (getVecNorm(_gradFunctionValueM) <= EPS_1) {
      done = true;
      double[] tmp = exactFunctionValue(_rhosGuess);
      _finalSqu = 0.5 * getVecNormSq(tmp);
    }

    while (done == false && k < ITRMAX) {

      k = k + 1;

      ///confirming -1<= rhos <=1
      boolean confRhos = false;
      while (confRhos == false) {

        rhosJump = theMatrixEqnSolver();

        int nOutOfRange = 0;
        for (int i = 0; i < _nNormals; ++i) {
          final double tmpGuess = _rhosGuess[i] + rhosJump[i];
          if (tmpGuess <= -1.) {
            ++nOutOfRange;
          }
          if (tmpGuess >= 1.) {
            ++nOutOfRange;
          }
        }
        if (nOutOfRange == 0) {
          confRhos = true;
        } else {

          for (int i = 0; i < _nNormals; ++i) {
            _rhosGuess[i] = _randObj.nextDouble();
          }

          _gradM = exactFunctionDerivative(_rhosGuess);
          _dataDerivedYDiff = exactFunctionValue(_rhosGuess);

          for (int i = 0; i < _nNormals; ++i) {
            for (int j = 0; j < _nData; ++j) {
              _gradFunctionValueM[i] += -_gradM[j][i] * _dataDerivedYDiff[j];
            }
          }

          for (int i = 0; i < _nNormals; ++i) {
            for (int j = 0; j < _nNormals; ++j) {
              for (int l = 0; l < _nData; ++l) {
                _hessian[i][j] += _gradM[l][i] * _gradM[l][j];
              }
            }
          }

          for (int i = 0; i < _nNormals; ++i) {
            if (_hessian[i][i] > _shift) {
              _shift = _hessian[i][i];
            }
          }
          _shift = _tau * _shift;
        }

      }
      ///

      if (getVecNorm(rhosJump) <= EPS_2 * (getVecNorm(_rhosGuess) + EPS_2)) {

        done = true;
        _rhosGuess = addVectors(_rhosGuess, rhosJump);
        _finalSqu = 0.5 * getVecNormSq(exactFunctionValue(_rhosGuess));

      } else {

        int tmpCntr = 0;
        boolean confNoNan = false;

        while (confNoNan == false) {
          for (int i = 0; i < _nNormals; ++i) {
            if (Double.isNaN(rhosJump[i])) {
              ++tmpCntr;
            }
          }

          if (tmpCntr == 0) {
            confNoNan = true;
          }
          if (tmpCntr != 0) {
            for (int i = 0; i < _nNormals; ++i) {
              _rhosGuess[i] = 1e-2 + _randObj.nextDouble();
            }
            _gradM = exactFunctionDerivative(_rhosGuess);
            _dataDerivedYDiff = exactFunctionValue(_rhosGuess);

            for (int i = 0; i < _nNormals; ++i) {
              for (int j = 0; j < _nData; ++j) {
                _gradFunctionValueM[i] += -_gradM[j][i] * _dataDerivedYDiff[j];
              }
            }

            for (int i = 0; i < _nNormals; ++i) {
              for (int j = 0; j < _nNormals; ++j) {
                for (int l = 0; l < _nData; ++l) {
                  _hessian[i][j] += _gradM[l][i] * _gradM[l][j];
                }
              }
            }

            for (int i = 0; i < _nNormals; ++i) {
              if (_hessian[i][i] > _shift) {
                _shift = _hessian[i][i];
              }
            }
            _shift = _tau * _shift;

          }

        }

        rho = getGainRatio(rhosJump);
        _rhosGuess = addVectors(_rhosGuess, rhosJump);

        if (rho > 0.) {

          Arrays.fill(_gradFunctionValueM, 0.);
          for (int i = 0; i < _nNormals; ++i) {
            Arrays.fill(_hessian[i], 0.);
          }

          _gradM = exactFunctionDerivative(_rhosGuess);
          _dataDerivedYDiff = exactFunctionValue(_rhosGuess);

          for (int i = 0; i < _nNormals; ++i) {
            for (int j = 0; j < _nData; ++j) {
              _gradFunctionValueM[i] += -_gradM[j][i] * _dataDerivedYDiff[j];
            }
          }

          for (int i = 0; i < _nNormals; ++i) {
            for (int j = 0; j < _nNormals; ++j) {
              for (int l = 0; l < _nData; ++l) {
                _hessian[i][j] += _gradM[l][i] * _gradM[l][j];
              }
            }
          }

          if (getVecNorm(_gradFunctionValueM) <= EPS_1) {
            _finalSqu = 0.5 * getVecNormSq(exactFunctionValue(_rhosGuess));
            done = true;
          }

          _shift = _shift * Math.max(1. / 3., 1. - (2. * rho - 1.) * (2. * rho - 1.) * (2. * rho - 1.));
          _shiftModFac = 2.;

        } else {

          _shift = _shift * _shiftModFac;
          _shiftModFac = 2. * _shiftModFac;

        }

      }

      if (k == ITRMAX) {
        System.out.println("Too Many Iterations");
        _finalSqu = 0.5 * getVecNormSq(exactFunctionValue(_rhosGuess));
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

  /**
   * Compute gain ratio which controls update of _shift 
   * @param difference between updated rhos and old rhos 
   */
  private double getGainRatio(final double[] jump) {

    return exactFunctionDiff(jump) / apprxFunctionDiff(jump);
  }

  /**
   * @return Denominator of gain ratio
   */
  private double apprxFunctionDiff(final double[] jump) {
    double tmp = 0.;
    for (int i = 0; i < _nNormals; ++i) {
      tmp += 0.5 * jump[i] * (_shift * jump[i] + _gradFunctionValueM[i]);
    }

    return tmp;

  }

  /**
   * @return Numerator of gain ratio
   */
  private double exactFunctionDiff(final double[] jump) {
    double[] tmp0 = exactFunctionValue(_rhosGuess);
    double[] newParams = new double[_nNormals];
    double[] tmp1 = new double[_nData];

    newParams = addVectors(_rhosGuess, jump);

    tmp1 = exactFunctionValue(newParams);

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

    double sigmaGuess = 0.3;
    return BlackFormulaRepository.impliedVolatility(targetPrice, forward, k, t, sigmaGuess);
  }

  /**
   * @param guess rhos
   * @return Difference between a market value of volatility and implied volatility with guess parameters
   */
  private double[] exactFunctionValue(final double[] rhos) {

    double[] res = new double[_nData];
    Arrays.fill(res, 0.);

    final MixedBivariateLogNormalModelVolatility guessObjZ = new MixedBivariateLogNormalModelVolatility(_weights, _sigmasX, _sigmasY, _relativePartialForwardsX, _relativePartialForwardsY, rhos);
    final double[] sigmasZ = guessObjZ.getSigmasZ();
    final double[] relativePartialForwardsZ = guessObjZ.getRelativeForwardsZ();
    final double[] weightsZ = guessObjZ.getOrderedWeights();
    final MixedLogNormalModelData guessDataZ = new MixedLogNormalModelData(weightsZ, sigmasZ, relativePartialForwardsZ);

    for (int j = 0; j < _nData; ++j) {
      EuropeanVanillaOption option = new EuropeanVanillaOption(_dataStrikes[j], _timeToExpiry, true);
      res[j] = _dataVols[j] - getVolatility(option, _forwardZ, guessDataZ);
    }

    return res;
  }

  /**
   * @return first derivatives of exactFunctionValue in terms of rhos
   */
  private double[][] exactFunctionDerivative(final double[] rhos) {

    double[][] res = new double[_nData][_nNormals];
    for (int i = 0; i < _nData; ++i) {
      Arrays.fill(res[i], 0.);
    }

    final MixedBivariateLogNormalModelVolatility guessObjZ = new MixedBivariateLogNormalModelVolatility(_weights, _sigmasX, _sigmasY, _relativePartialForwardsX, _relativePartialForwardsY, rhos);
    final double[] sigmasZ = guessObjZ.getSigmasZ();
    final double[] relativePartialForwardsZ = guessObjZ.getRelativeForwardsZ();
    final double[] weightsZ = guessObjZ.getOrderedWeights();
    final double correction = guessObjZ.getInvExpDriftCorrection();
    final MixedLogNormalModelData guessDataZ = new MixedLogNormalModelData(weightsZ, sigmasZ, relativePartialForwardsZ);

    final double[] sigmasX = guessObjZ.getOrderedSigmasX();
    final double[] sigmasY = guessObjZ.getOrderedSigmasY();

    for (int j = 0; j < _nData; ++j) {
      EuropeanVanillaOption option = new EuropeanVanillaOption(_dataStrikes[j], _timeToExpiry, true);
      double impVolZ = getVolatility(option, _forwardZ, guessDataZ);
      for (int i = 0; i < _nNormals; ++i) {
        final double part1 = weightsZ[i] * _forwardZ * BlackFormulaRepository.delta(relativePartialForwardsZ[i], _dataStrikes[j] / _forwardZ, _timeToExpiry, sigmasZ[i], true) * sigmasX[i] *
            sigmasY[i] * relativePartialForwardsZ[i] / BlackFormulaRepository.vega(_forwardZ, _dataStrikes[j], _timeToExpiry, impVolZ);
        final double part2 = _forwardZ * weightsZ[i] * BlackFormulaRepository.vega(relativePartialForwardsZ[i], _dataStrikes[j] / _forwardZ, _timeToExpiry, sigmasZ[i]) * sigmasX[i] *
            sigmasY[i] / sigmasZ[i] / BlackFormulaRepository.vega(_forwardZ, _dataStrikes[j], _timeToExpiry, impVolZ);
        final double factor = weightsZ[i] * relativePartialForwardsZ[i] * sigmasX[i] * sigmasY[i] * correction * correction;
        double part3 = 0.;
        for (int l = 0; l < _nNormals; ++l) {
          part3 += factor * weightsZ[l] * _forwardZ * BlackFormulaRepository.delta(relativePartialForwardsZ[l], _dataStrikes[j] / _forwardZ, _timeToExpiry, sigmasZ[l], true) *
              relativePartialForwardsZ[l] /
              BlackFormulaRepository.vega(_forwardZ, _dataStrikes[j], _timeToExpiry, impVolZ);
        }
        res[j][i] = part1 + part2 - part3;
      }

    }

    return res;
  }

  /**
   *  Add vectorA to VectorB
   */
  private double[] addVectors(final double[] vecA, final double[] vecB) {
    final int dim = vecA.length;
    double[] res = new double[dim];

    for (int i = 0; i < dim; ++i) {
      res[i] = vecA[i] + vecB[i];
    }

    return res;
  }

  /**
   * Solve the matrix equation ( _hessian + _shift (Id matrix) ) jump = _gradFunctionValueM
   * @return jump
   */
  private double[] theMatrixEqnSolver() {

    double[][] toBeInv = new double[_nNormals][_nNormals];
    toBeInv = _hessian;

    double tmpDerivativeNorm = getVecNorm(_gradFunctionValueM);
    double tmpSquFact = 0.02 * 0.5 * getVecNormSq(exactFunctionValue(_rhosGuess));
    if (tmpDerivativeNorm <= tmpSquFact) {
      _shift = _tau * _shift;
    }

    for (int i = 0; i < _nNormals; ++i) {
      toBeInv[i][i] = toBeInv[i][i] + _shift;
    }

    double[] soln = new double[_nNormals];

    final DoubleMatrix2D toBeInvMatrix = new DoubleMatrix2D(toBeInv);
    final DoubleMatrix1D gradMatrixM = new DoubleMatrix1D(_gradFunctionValueM);

    final DoubleMatrix2D toBeInvMatrixInv = _algComObj.getInverse(toBeInvMatrix);
    final DoubleMatrix1D solnMatrix = (DoubleMatrix1D) _algOgObj.multiply(toBeInvMatrixInv, gradMatrixM);

    soln = solnMatrix.getData();

    return soln;

  }

}
