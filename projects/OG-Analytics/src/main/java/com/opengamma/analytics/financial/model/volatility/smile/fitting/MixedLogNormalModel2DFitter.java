/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import java.util.Arrays;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalModelData;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalVolatilityFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class MixedLogNormalModel2DFitter {

  private static int _ITRMAX = 1000;
  private static double _EPS_1 = 1.E-12;
  private static double _EPS_2 = 1.E-12;

  private final double _tau = 1.E-3;
  private double _shiftModFac = 2.;
  private double _shift;
  private double[] _paramsGuess;
  private double[] _dataStrikes;
  private double[] _dataVols;

  private double[] _dataDerivedYDiff;
  private double[][] _gradM;
  private final double[] _gradFunctionValueM;
  private final double[][] _hessian;

  final int _nParams;
  final int _nData;
  final int _nNormals;
  final int _nDataX;

  final double _forwardX, _forwardY, _timeToExpiry;
  double _chiSq = 0;

  double[][] copymatrix;
  double[] copyvector1;
  double[] copyvector2;
  double copydouble;

  final MixedLogNormalVolatilityFunction volfunc = MixedLogNormalVolatilityFunction.getInstance();

  public MixedLogNormalModel2DFitter(final double[] paramsGuess, final double[] dataStrikes, final double[] dataVols, final double timeToExpiry, final double forwardX, final double forwardY,
      final int nNormals,
      final int nDataX) {
    _nParams = paramsGuess.length;
    _nData = dataStrikes.length;
    ArgumentChecker.isTrue(_nData == dataVols.length, "dataStrikes not the same length as dataVols");
    ArgumentChecker.isTrue(_nData >= _nParams, "data should be more than parameters");

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

    Arrays.fill(_gradFunctionValueM, 0.);
    for (int i = 0; i < _nParams; ++i) {
      Arrays.fill(_hessian[i], 0.);
    }
  }

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

    final double[] tmpvec = exactFunctionValue(_paramsGuess);
    copydouble = 0.5 * getVecNormSq(tmpvec);

    if (getVecNorm(_gradFunctionValueM) <= _EPS_1) {
      done = true;
      final double[] tmp = exactFunctionValue(_paramsGuess);
      _chiSq = 0.5 * getVecNormSq(tmp);
    }

    while (done == false && k < _ITRMAX) {
      //   System.out.println(k);
      k = k + 1;

      paramsJump = theMatrixEqnSolver();

      if (getVecNorm(paramsJump) <= _EPS_2 * (getVecNorm(_paramsGuess) + _EPS_2)) {

        done = true;
        final double[] tmp = exactFunctionValue(_paramsGuess);
        _chiSq = 0.5 * getVecNormSq(tmp);

      } else {

        rho = getGainRatio(paramsJump);
        //    copyvector1 = _paramsGuess;
        _paramsGuess = addVectors(_paramsGuess, paramsJump);
        //     copyvector2 = _paramsGuess;

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

          if (getVecNorm(_gradFunctionValueM) <= _EPS_1) {
            done = true;
          }

          _shift = _shift * Math.max(1. / 3., 1. - (2. * rho - 1.) * (2. * rho - 1.) * (2. * rho - 1.));
          _shiftModFac = 2.;

        } else {

          _shift = _shift * _shiftModFac;
          _shiftModFac = 2. * _shiftModFac;

        }

      }

      if (k == _ITRMAX) {
        System.out.println("Too Many Iterations");
        final double[] tmp = exactFunctionValue(_paramsGuess);
        _chiSq = 0.5 * getVecNormSq(tmp);
      }
    }

  }

  public double getChiSq() {
    return _chiSq;
  }

  public double[] getParams() {
    return _paramsGuess;
  }

  private double getVecNorm(final double[] vec) {
    final int nVec = vec.length;
    double res = 0.;

    for (int i = 0; i < nVec; ++i) {
      res += vec[i] * vec[i];
    }

    return Math.sqrt(res);
  }

  private double getVecNormSq(final double[] vec) {
    final int nVec = vec.length;
    double res = 0.;

    for (int i = 0; i < nVec; ++i) {
      res += vec[i] * vec[i];
    }

    return res;
  }

  private double getGainRatio(final double[] jump) {

    return exactFunctionDiff(jump) / apprxFunctionDiff(jump);
  }

  private double apprxFunctionDiff(final double[] jump) {
    double tmp = 0.;
    for (int i = 0; i < _nParams; ++i) {
      tmp += 0.5 * jump[i] * (_shift * jump[i] + _gradFunctionValueM[i]);
    }

    return tmp;

  }

  private double exactFunctionDiff(final double[] jump) {
    final double[] tmp0 = exactFunctionValue(_paramsGuess);
    double[] newParams = new double[_nParams];
    double[] tmp1 = new double[_nData];

    newParams = addVectors(_paramsGuess, jump);

    tmp1 = exactFunctionValue(newParams);

    return getVecNormSq(tmp0) / 2. - getVecNormSq(tmp1) / 2.;

  }

  private double[] exactFunctionValue(final double[] params) {

    final int dof = 3 * _nNormals - 2;

    final double[] paramsX = new double[dof];
    final double[] paramsY = new double[dof];

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

    final MixedLogNormalModelData dataX = new MixedLogNormalModelData(paramsX, true);
    final MixedLogNormalModelData dataY = new MixedLogNormalModelData(paramsY, true);

    final double[] res = new double[_nData];
    Arrays.fill(res, 0.);

    for (int j = 0; j < _nDataX; ++j) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(_dataStrikes[j], _timeToExpiry, true);
      res[j] = _dataVols[j] - volfunc.getVolatility(option, _forwardX, dataX);
    }

    for (int j = _nDataX; j < _nData; ++j) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(_dataStrikes[j], _timeToExpiry, true);
      res[j] = _dataVols[j] - volfunc.getVolatility(option, _forwardY, dataY);
    }

    return res;
  }

  public double[][] exactFunctionDerivative(final double[] params) {

    final int dof = 3 * _nNormals - 2;
    final double[] paramsX = new double[dof];
    final double[] paramsY = new double[dof];

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

    final MixedLogNormalModelData dataX = new MixedLogNormalModelData(paramsX, true);
    final MixedLogNormalModelData dataY = new MixedLogNormalModelData(paramsY, true);

    final double[][] res = new double[_nData][_nParams];
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

    copydouble = params.length;

    for (int j = 0; j < _nDataX; ++j) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(_dataStrikes[j], _timeToExpiry, true);
      final double impVolX = volfunc.getVolatility(option, _forwardX, dataX);
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
      final EuropeanVanillaOption option = new EuropeanVanillaOption(_dataStrikes[j], _timeToExpiry, true);
      final double impVolY = volfunc.getVolatility(option, _forwardY, dataY);
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

  //  public double[][] exactFunctionDerivative(final double[] params) {
  //
  //    double[] upParams = new double[_nParams];
  //    double[] downParams = new double[_nParams];
  //
  //    upParams = Arrays.copyOfRange(params, 0, _nParams);
  //
  //    downParams = Arrays.copyOfRange(params, 0, _nParams);
  //
  //    double[][] res = new double[_nData][_nParams];
  //    for (int i = 0; i < _nData; ++i) {
  //      Arrays.fill(res[i], 0.);
  //    }
  //
  //    for (int i = 0; i < _nParams; ++i) {
  //
  //      double eps = params[i] * 1e-6;
  //      upParams[i] += eps;
  //      downParams[i] -= eps;
  //      double[] upRes = exactFunctionValue(upParams);
  //      double[] downRes = exactFunctionValue(downParams);
  //
  //      for (int j = 0; j < _nData; ++j) {
  //        res[j][i] = (upRes[j] - downRes[j]) / 2. / eps;
  //      }
  //      upParams[i] -= eps;
  //      downParams[i] += eps;
  //    }
  //
  //    return res;
  //  }

  //  private double[] exactFunctionValue(final double[] params) {
  //
  //    double[] res = new double[_nData];
  //    Arrays.fill(res, 0.);
  //
  //    for (int j = 0; j < _nData; ++j) {
  //      res[j] += _dataVols[j];
  //      for (int i = 0; i < _nParams - 1; i += 3) {
  //        res[j] += -params[i] * Math.exp(-(_dataStrikes[j] - params[i + 1]) * (_dataStrikes[j] - params[i + 1]) / params[i + 2] / params[i + 2]);
  //      }
  //    }
  //
  //    return res;
  //  }
  //
  //  private double[][] exactFunctionDerivative(final double[] params, final double[] dataX) {
  //
  //    double[][] res = new double[_nData][_nParams];
  //
  //    for (int j = 0; j < _nData; ++j) {
  //      for (int i = 0; i < _nParams - 1; i += 3) {
  //        res[j][i] = -Math.exp(-(dataX[j] - params[i + 1]) * (dataX[j] - params[i + 1]) / params[i + 2] / params[i + 2]);
  //        res[j][i + 1] = -params[i] * Math.exp(-(dataX[j] - params[i + 1]) * (dataX[j] - params[i + 1]) / params[i + 2] / params[i + 2]) *
  //            (2. * (dataX[j] - params[i + 1]) / params[i + 2] / params[i + 2]);
  //        res[j][i + 2] = -params[i] * Math.exp(-(dataX[j] - params[i + 1]) * (dataX[j] - params[i + 1]) / params[i + 2] / params[i + 2]) *
  //            (2. * (dataX[j] - params[i + 1]) * (dataX[j] - params[i + 1]) / params[i + 2] / params[i + 2] / params[i + 2]);
  //      }
  //    }
  //    return res;
  //  }

  private double[] addVectors(final double[] vecA, final double[] vecB) {
    final int dim = vecA.length;
    final double[] res = new double[dim];

    for (int i = 0; i < dim; ++i) {
      res[i] = vecA[i] + vecB[i];
    }

    return res;
  }

  private double[] theMatrixEqnSolver() {

    double[][] inverse = new double[_nParams][_nParams];
    inverse = _hessian;

    for (int i = 0; i < _nParams; ++i) {
      inverse[i][i] = inverse[i][i] + _shift;
    }

    double[] soln = new double[_nParams];
    soln = _gradFunctionValueM;

    int irow = 0, icol = 0;

    double big, dum, pivinv;

    final int[] indxc = new int[_nParams];
    final int[] indxr = new int[_nParams];
    final int[] ipiv = new int[_nParams];
    Arrays.fill(ipiv, 0);

    for (int i = 0; i < _nParams; i++) {
      big = 0.0;
      for (int j = 0; j < _nParams; j++) {
        if (ipiv[j] != 1) {
          for (int k = 0; k < _nParams; k++) {
            if (ipiv[k] == 0) {
              if (Math.abs(inverse[j][k]) >= big) {
                big = Math.abs(inverse[j][k]);
                irow = j;
                icol = k;
              }
            }
          }
        }
      }
      ++(ipiv[icol]);
      if (irow != icol) {
        for (int l = 0; l < _nParams; l++) {
          final double tmp = inverse[irow][l];
          inverse[irow][l] = inverse[icol][l];
          inverse[icol][l] = tmp;
        }

        final double tmp = soln[irow];
        soln[irow] = soln[icol];
        soln[icol] = tmp;

      }
      indxr[i] = irow;
      indxc[i] = icol;
      ArgumentChecker.isFalse(inverse[icol][icol] == 0.0, "gaussj: Singular Matrix");
      pivinv = 1.0 / inverse[icol][icol];
      inverse[icol][icol] = 1.0;
      for (int l = 0; l < _nParams; l++) {
        inverse[icol][l] *= pivinv;
      }
      soln[icol] *= pivinv;
      for (int ll = 0; ll < _nParams; ll++) {
        if (ll != icol) {
          dum = inverse[ll][icol];
          inverse[ll][icol] = 0.0;
          for (int l = 0; l < _nParams; l++) {
            inverse[ll][l] -= inverse[icol][l] * dum;
          }
          soln[ll] -= soln[icol] * dum;
        }
      }
    }
    for (int l = _nParams - 1; l >= 0; l--) {
      if (indxr[l] != indxc[l]) {
        for (int k = 0; k < _nParams; k++) {
          final double tmp = inverse[k][indxr[l]];
          inverse[k][indxr[l]] = inverse[k][indxc[l]];
          inverse[k][indxc[l]] = tmp;
        }
      }
    }

    return soln;

  }

}
