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
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalModelData;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalVolatilityFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * Fitting volatility smiles of two mixed log-normal models with mixed normal variables X,Y by using Levenberg-Marquardt (LM) method. 
 * As model constraints are eventually violated starting with some initial guess parameters, 
 * the LM algorithm is modified such that model parameters satisfy all of the constraints in every step of the fitting iteration.  
 * X,Y should have the same number of normal distributions and share all of the weights.
 */
public class MixedBivariateLogNormalFitter {

  private static final int ITRMAX = 20000;
  private static final double EPS_1 = 1.E-10;
  private static final double EPS_2 = 1.E-10;

  private double _tau = 1.E-3;
  private double _shiftModFac = 2.;
  private double _shift;
  private double[] _paramsGuess;
  private double[] _dataStrikes;
  private double[] _dataVols;

  private double[] _dataDerivedYDiff;
  private double[][] _gradM;
  private double[] _gradFunctionValueM;
  private double[][] _hessian;

  private final int _nParams;
  private final int _nData;
  private final int _nNormals;
  private final int _nDataX;

  private double _forwardX, _forwardY, _timeToExpiry;
  private double _finalSqu;
  private double _iniSqu;
  private double _paramsGuessCorrection;

  private Random _randObj = new Random();

  private MixedLogNormalVolatilityFunction _volfunc = MixedLogNormalVolatilityFunction.getInstance();

  /**
   * For a mixture of N log-normal distributions, a mixed log-normal model contains 3 * N -2 free parameters. 
   * Because sets of weights of X,Y are identical, total number of free parameters of X,Y is 5 * N -3.
   * 
   * @param paramsGuess  Initial (unconstrained) guess parameters of X,Y to be chosen randomly 
   * @param dataStrikes  Strike (market data). All the data of X should be before those of Y
   * @param dataVols  Volatility (market data). all the data of X should be before those of Y
   * @param timeToExpiry  Time to Expiry
   * @param forwardX  Forward value of mixed log-normal model with X
   * @param forwardY  Forward value of mixed log-normal model with Y
   * @param nNormals  The number of normal distributions (X,Y have the same number of log-normal distributions)
   * @param nDataX  The number of sets of data (strike, vol) of X
   * @param paramsGuessCorrection  Set to be larger value for long expiry
   */

  public MixedBivariateLogNormalFitter(final double[] paramsGuess, final double[] dataStrikes, final double[] dataVols, final double timeToExpiry, final double forwardX, final double forwardY,
      final int nNormals,
      final int nDataX, final double paramsGuessCorrection) {
    _nParams = paramsGuess.length;
    _nData = dataStrikes.length;
    ArgumentChecker.isTrue(_nData == dataVols.length, "dataStrikes not the same length as dataVols");
    ArgumentChecker.isTrue(_nParams == 5 * nNormals - 3, "5 * N -3 free parameters");

    _dataStrikes = new double[_nData];
    _dataVols = new double[_nData];
    _dataDerivedYDiff = new double[_nData];

    _gradM = new double[_nData][_nParams];
    _gradFunctionValueM = new double[_nParams];
    _hessian = new double[_nParams][_nParams];
    _paramsGuess = new double[_nParams];

    _dataStrikes = dataStrikes;
    _dataVols = dataVols;
    _paramsGuess = paramsGuess;
    _timeToExpiry = timeToExpiry;
    _forwardX = forwardX;
    _forwardY = forwardY;
    _nNormals = nNormals;
    _nDataX = nDataX;
    _paramsGuessCorrection = paramsGuessCorrection;

    Arrays.fill(_gradFunctionValueM, 0.);
    for (int i = 0; i < _nParams; ++i) {
      Arrays.fill(_hessian[i], 0.);
    }
  }

  /**
   * Find a set of parameters such that sum ( (_dataStrikes - exactFunctionValue)^2 ) is minimum by using Levenberg-Marquardt (LM) method. 
   */
  public void doFit() {

    int k = 0;
    double rho = 0.;
    _shift = 0;
    boolean done = false;
    double[] paramsJump = new double[_nData];

    _gradM = exactFunctionDerivative(_paramsGuess);
    _dataDerivedYDiff = exactFunctionValue(_paramsGuess);

    for (int i = 0; i < _nParams; ++i) {
      for (int j = 0; j < _nData; ++j) {
        _gradFunctionValueM[i] += -_gradM[j][i] * _dataDerivedYDiff[j];
      }
    }

    for (int i = 0; i < _nParams; ++i) {
      for (int j = 0; j < _nParams; ++j) {
        for (int l = 0; l < _nData; ++l) {
          _hessian[i][j] += _gradM[l][i] * _gradM[l][j];
        }
      }
    }

    for (int i = 0; i < _nParams; ++i) {
      if (_hessian[i][i] > _shift) {
        _shift = _hessian[i][i];
      }
    }
    _shift = _tau * _shift;

    _iniSqu = 0.5 * getVecNormSq(exactFunctionValue(_paramsGuess));

    if (getVecNorm(_gradFunctionValueM) <= EPS_1) {
      done = true;
      double[] tmp = exactFunctionValue(_paramsGuess);
      _finalSqu = 0.5 * getVecNormSq(tmp);
    }

    while (done == false && k < ITRMAX) {

      k = k + 1;

      ///confirming positive parameters
      boolean confPositiveParams = false;
      while (confPositiveParams == false) {

        paramsJump = theMatrixEqnSolver();

        int nNegatives = 0;
        for (int i = 0; i < _nParams; ++i) {
          double tmpGuess = _paramsGuess[i] + paramsJump[i];
          if (tmpGuess <= 0.) {
            ++nNegatives;
          }
        }
        if (nNegatives == 0) {
          confPositiveParams = true;
        } else {

          for (int i = 0; i < _nParams; ++i) {
            _paramsGuess[i] = _paramsGuessCorrection * (1e-2 + _randObj.nextDouble());
          }

          _gradM = exactFunctionDerivative(_paramsGuess);
          _dataDerivedYDiff = exactFunctionValue(_paramsGuess);

          for (int i = 0; i < _nParams; ++i) {
            for (int j = 0; j < _nData; ++j) {
              _gradFunctionValueM[i] += -_gradM[j][i] * _dataDerivedYDiff[j];
            }
          }

          for (int i = 0; i < _nParams; ++i) {
            for (int j = 0; j < _nParams; ++j) {
              for (int l = 0; l < _nData; ++l) {
                _hessian[i][j] += _gradM[l][i] * _gradM[l][j];
              }
            }
          }

          for (int i = 0; i < _nParams; ++i) {
            if (_hessian[i][i] > _shift) {
              _shift = _hessian[i][i];
            }
          }
          _shift = _tau * _shift;

        }
      }
      ///

      if (getVecNorm(paramsJump) <= EPS_2 * (getVecNorm(_paramsGuess) + EPS_2)) {

        done = true;
        _paramsGuess = addVectors(_paramsGuess, paramsJump);
        _finalSqu = 0.5 * getVecNormSq(exactFunctionValue(_paramsGuess));

      } else {

        /// If theMatrixEqnSolver returns NaN (coming form infinite gradients due to Vega =0), start fitting with new guess parameters. 
        int tmpCntr = 0;
        boolean confNoNan = false;
        while (confNoNan == false) {
          for (int i = 0; i < _nParams; ++i) {
            if (Double.isNaN(paramsJump[i])) {
              ++tmpCntr;
            }
          }

          if (tmpCntr == 0) {
            confNoNan = true;
          } else {
            for (int i = 0; i < _nParams; ++i) {
              _paramsGuess[i] = _paramsGuessCorrection * (1e-2 + _randObj.nextDouble());
            }

            _gradM = exactFunctionDerivative(_paramsGuess);
            _dataDerivedYDiff = exactFunctionValue(_paramsGuess);

            for (int i = 0; i < _nParams; ++i) {
              for (int j = 0; j < _nData; ++j) {
                _gradFunctionValueM[i] += -_gradM[j][i] * _dataDerivedYDiff[j];
              }
            }

            for (int i = 0; i < _nParams; ++i) {
              for (int j = 0; j < _nParams; ++j) {
                for (int l = 0; l < _nData; ++l) {
                  _hessian[i][j] += _gradM[l][i] * _gradM[l][j];
                }
              }
            }

            for (int i = 0; i < _nParams; ++i) {
              if (_hessian[i][i] > _shift) {
                _shift = _hessian[i][i];
              }
            }
            _shift = _tau * _shift;

          }

        }
        ///

        rho = getGainRatio(paramsJump);
        _paramsGuess = addVectors(_paramsGuess, paramsJump);

        if (rho > 0.) {

          Arrays.fill(_gradFunctionValueM, 0.);
          for (int i = 0; i < _nParams; ++i) {
            Arrays.fill(_hessian[i], 0.);
          }

          _gradM = exactFunctionDerivative(_paramsGuess);
          _dataDerivedYDiff = exactFunctionValue(_paramsGuess);

          for (int i = 0; i < _nParams; ++i) {
            for (int j = 0; j < _nData; ++j) {
              _gradFunctionValueM[i] += -_gradM[j][i] * _dataDerivedYDiff[j];
            }
          }

          for (int i = 0; i < _nParams; ++i) {
            for (int j = 0; j < _nParams; ++j) {
              for (int l = 0; l < _nData; ++l) {
                _hessian[i][j] += _gradM[l][i] * _gradM[l][j];
              }
            }
          }

          if (getVecNorm(_gradFunctionValueM) <= EPS_1) {
            _finalSqu = 0.5 * getVecNormSq(exactFunctionValue(_paramsGuess));
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
        _finalSqu = 0.5 * getVecNormSq(exactFunctionValue(_paramsGuess));
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
   * @param difference between updated parameters and old parameters 
   */
  private double getGainRatio(final double[] jump) {

    return exactFunctionDiff(jump) / apprxFunctionDiff(jump);
  }

  /**
   * @return Denominator of gain ratio
   */
  private double apprxFunctionDiff(final double[] jump) {
    double tmp = 0.;
    for (int i = 0; i < _nParams; ++i) {
      tmp += 0.5 * jump[i] * (_shift * jump[i] + _gradFunctionValueM[i]);
    }

    return tmp;

  }

  /**
   * @return Numerator of gain ratio
   */
  private double exactFunctionDiff(final double[] jump) {
    double[] tmp0 = exactFunctionValue(_paramsGuess);
    double[] newParams = new double[_nParams];
    double[] tmp1 = new double[_nData];

    newParams = addVectors(_paramsGuess, jump);

    tmp1 = exactFunctionValue(newParams);

    return getVecNormSq(tmp0) / 2. - getVecNormSq(tmp1) / 2.;

  }

  /**
   * During the iterations of least-square fitting, a set of parameters sometimes breaks the condition 0 < targetPrice < Math.min(forward, strike). 
   * Do not use getImpliedVolatilityZ method in MixedLogNormalModel2DVolatility. 
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
   * @param guess parameters
   * @return Difference between a market value of volatility and implied volatility with guess parameters
   */
  private double[] exactFunctionValue(final double[] params) {

    final int dof = 3 * _nNormals - 2;

    double[] paramsX = new double[dof];
    double[] paramsY = new double[dof];

    for (int i = 0; i < _nNormals; ++i) {
      paramsX[i] = params[i];
    }
    for (int i = 0; i < _nNormals - 1; ++i) {
      paramsX[i + _nNormals] = params[i + 2 * _nNormals];
      paramsX[i + 2 * _nNormals - 1] = params[i + 3 * _nNormals - 1];
    }

    for (int i = 0; i < _nNormals; ++i) {
      paramsY[i] = params[i + _nNormals];
    }
    for (int i = 0; i < _nNormals - 1; ++i) {
      paramsY[i + _nNormals] = params[i + 2 * _nNormals];
      paramsY[i + 2 * _nNormals - 1] = params[i + 4 * _nNormals - 2];
    }

    MixedLogNormalModelData dataX = new MixedLogNormalModelData(paramsX, true);
    MixedLogNormalModelData dataY = new MixedLogNormalModelData(paramsY, true);

    double[] res = new double[_nData];
    Arrays.fill(res, 0.);

    for (int j = 0; j < _nDataX; ++j) {
      EuropeanVanillaOption option = new EuropeanVanillaOption(_dataStrikes[j], _timeToExpiry, true);
      res[j] = _dataVols[j] - getVolatility(option, _forwardX, dataX);
    }

    for (int j = _nDataX; j < _nData; ++j) {
      EuropeanVanillaOption option = new EuropeanVanillaOption(_dataStrikes[j], _timeToExpiry, true);
      res[j] = _dataVols[j] - getVolatility(option, _forwardY, dataY);
    }

    return res;
  }

  /**
   * @return first derivatives of exactFunctionValue in terms of unconstrained model parameters
   */
  private double[][] exactFunctionDerivative(final double[] params) {

    final int dof = 3 * _nNormals - 2;
    double[] paramsX = new double[dof];
    double[] paramsY = new double[dof];

    for (int i = 0; i < _nNormals; ++i) {
      paramsX[i] = params[i];
    }
    for (int i = 0; i < _nNormals - 1; ++i) {
      paramsX[i + _nNormals] = params[i + 2 * _nNormals];
      paramsX[i + 2 * _nNormals - 1] = params[i + 3 * _nNormals - 1];
    }

    for (int i = 0; i < _nNormals; ++i) {
      paramsY[i] = params[i + _nNormals];
    }
    for (int i = 0; i < _nNormals - 1; ++i) {
      paramsY[i + _nNormals] = params[i + 2 * _nNormals];
      paramsY[i + 2 * _nNormals - 1] = params[i + 4 * _nNormals - 2];
    }

    MixedLogNormalModelData dataX = new MixedLogNormalModelData(paramsX, true);
    MixedLogNormalModelData dataY = new MixedLogNormalModelData(paramsY, true);

    double[][] res = new double[_nData][_nParams];
    for (int i = 0; i < _nData; ++i) {
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

    for (int j = 0; j < _nDataX; ++j) {
      EuropeanVanillaOption option = new EuropeanVanillaOption(_dataStrikes[j], _timeToExpiry, true);
      double impVolX = getVolatility(option, _forwardX, dataX);
      for (int i = 0; i < _nNormals; ++i) {
        for (int l = i; l < _nNormals; ++l) {
          res[j][i] += -_forwardX * weightsX[l] * BlackFormulaRepository.vega(relativeForwardsX[l], _dataStrikes[j] / _forwardX, _timeToExpiry, sigmasX[l]) /
              BlackFormulaRepository.vega(_forwardX, _dataStrikes[j], _timeToExpiry, impVolX);
        }
      }
      for (int i = 2 * _nNormals; i < 3 * _nNormals - 1; ++i) {
        for (int l = 0; l < _nNormals; ++l) {
          res[j][i] += -_forwardX *
              (BlackFormulaRepository.price(relativeForwardsX[l], _dataStrikes[j] / _forwardX, _timeToExpiry, sigmasX[l], true) - relativeForwardsX[l] *
                  BlackFormulaRepository.delta(relativeForwardsX[l], _dataStrikes[j] / _forwardX, _timeToExpiry, sigmasX[l], true)) * weightsJacobianX[l][i - 2 * _nNormals] /
              BlackFormulaRepository.vega(_forwardX, _dataStrikes[j], _timeToExpiry, impVolX);
        }
      }
      for (int i = 3 * _nNormals - 1; i < 4 * _nNormals - 2; ++i) {
        for (int l = 0; l < _nNormals; ++l) {
          res[j][i] += -_forwardX * BlackFormulaRepository.delta(relativeForwardsX[l], _dataStrikes[j] / _forwardX, _timeToExpiry, sigmasX[l], true) *
              relativeForwardsJacobianX[l][i - (3 * _nNormals - 1)] /
              BlackFormulaRepository.vega(_forwardX, _dataStrikes[j], _timeToExpiry, impVolX);
        }
      }
    }

    for (int j = _nDataX; j < _nData; ++j) {
      EuropeanVanillaOption option = new EuropeanVanillaOption(_dataStrikes[j], _timeToExpiry, true);
      double impVolY = getVolatility(option, _forwardY, dataY);
      for (int i = _nNormals; i < 2 * _nNormals; ++i) {
        for (int l = i; l < 2 * _nNormals; ++l) {
          res[j][i] += -_forwardY * weightsY[l - _nNormals] * BlackFormulaRepository.vega(relativeForwardsY[l - _nNormals], _dataStrikes[j] / _forwardY, _timeToExpiry, sigmasY[l - _nNormals]) /
              BlackFormulaRepository.vega(_forwardY, _dataStrikes[j], _timeToExpiry, impVolY);
        }
      }
      for (int i = 2 * _nNormals; i < 3 * _nNormals - 1; ++i) {
        for (int l = 0; l < _nNormals; ++l) {
          res[j][i] += -_forwardY * (BlackFormulaRepository.price(relativeForwardsY[l], _dataStrikes[j] / _forwardY, _timeToExpiry, sigmasY[l], true) - relativeForwardsY[l] *
              BlackFormulaRepository.delta(relativeForwardsY[l], _dataStrikes[j] / _forwardY, _timeToExpiry, sigmasY[l], true)) * weightsJacobianY[l][i - 2 * _nNormals] /
              BlackFormulaRepository.vega(_forwardY, _dataStrikes[j], _timeToExpiry, impVolY);
        }
      }
      for (int i = 4 * _nNormals - 2; i < 5 * _nNormals - 3; ++i) {
        for (int l = 0; l < _nNormals; ++l) {
          res[j][i] += -_forwardY * BlackFormulaRepository.delta(relativeForwardsY[l], _dataStrikes[j] / _forwardY, _timeToExpiry, sigmasY[l], true) *
              relativeForwardsJacobianY[l][i - (4 * _nNormals - 2)] /
              BlackFormulaRepository.vega(_forwardY, _dataStrikes[j], _timeToExpiry, impVolY);
        }
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

    double[][] inverse = new double[_nParams][_nParams];
    inverse = _hessian;

    double tmpDerivativeNorm = getVecNorm(_gradFunctionValueM);
    double tmpSquFact = 0.02 * 0.5 * getVecNormSq(exactFunctionValue(_paramsGuess));
    if (tmpDerivativeNorm <= tmpSquFact) {
      _shift = _tau * _shift;
    }

    for (int i = 0; i < _nParams; ++i) {
      inverse[i][i] = inverse[i][i] + _shift;
    }

    double[] soln = new double[_nParams];
    soln = _gradFunctionValueM;

    int iRow = 0, iCol = 0;

    double bigElem, dumElem, pivInv;

    int[] indCol = new int[_nParams];
    int[] indRow = new int[_nParams];
    int[] iPiv = new int[_nParams];
    Arrays.fill(iPiv, 0);

    for (int i = 0; i < _nParams; ++i) {
      bigElem = 0.0;
      for (int j = 0; j < _nParams; ++j) {
        if (iPiv[j] != 1) {
          for (int k = 0; k < _nParams; ++k) {
            if (iPiv[k] == 0) {
              if (Math.abs(inverse[j][k]) >= bigElem) {
                bigElem = Math.abs(inverse[j][k]);
                iRow = j;
                iCol = k;
              }
            }
          }
        }
      }
      ++(iPiv[iCol]);
      if (iRow != iCol) {
        for (int l = 0; l < _nParams; ++l) {
          double tmp = inverse[iRow][l];
          inverse[iRow][l] = inverse[iCol][l];
          inverse[iCol][l] = tmp;
        }

        double tmp = soln[iRow];
        soln[iRow] = soln[iCol];
        soln[iCol] = tmp;
      }
      indRow[i] = iRow;
      indCol[i] = iCol;

      pivInv = 1. / inverse[iCol][iCol];
      inverse[iCol][iCol] = 1.;
      for (int l = 0; l < _nParams; ++l) {
        inverse[iCol][l] *= pivInv;
      }
      soln[iCol] *= pivInv;
      for (int l = 0; l < _nParams; ++l) {
        if (l != iCol) {
          dumElem = inverse[l][iCol];
          inverse[l][iCol] = 0.0;
          for (int m = 0; m < _nParams; ++m) {
            inverse[l][m] -= inverse[iCol][m] * dumElem;
          }
          soln[l] -= soln[iCol] * dumElem;
        }
      }
    }
    for (int l = _nParams - 1; l >= 0; --l) {
      if (indRow[l] != indCol[l]) {
        for (int k = 0; k < _nParams; ++k) {
          double tmp = inverse[k][indRow[l]];
          inverse[k][indRow[l]] = inverse[k][indCol[l]];
          inverse[k][indCol[l]] = tmp;
        }
      }
    }

    return soln;

  }
}
