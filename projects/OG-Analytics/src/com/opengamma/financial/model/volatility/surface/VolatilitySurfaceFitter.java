/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.volatility.smile.function.SmileModelData;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.math.curve.Curve;
import com.opengamma.math.curve.InterpolatedCurveBuildingFunction;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.TransformedInterpolator1D;
import com.opengamma.math.linearalgebra.DecompositionFactory;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.MatrixAlgebra;
import com.opengamma.math.matrix.OGMatrixAlgebra;
import com.opengamma.math.minimization.ParameterLimitsTransform;
import com.opengamma.math.minimization.UncoupledParameterTransforms;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.math.statistics.leastsquare.NonLinearLeastSquare;

/**
 * @param <T> The type of data (i.e. model parameters) used by the smile model
 */
public abstract class VolatilitySurfaceFitter<T extends SmileModelData> {

  private static final MatrixAlgebra MA = new OGMatrixAlgebra();
  private static final NonLinearLeastSquare SOLVER = new NonLinearLeastSquare(DecompositionFactory.SV_COLT, MA, 1e-6);

  private final InterpolatedCurveBuildingFunction _curveBuilder;
  private final double[] _expiries;
  private final double[][] _strikes; //the strikes at each expiry
  private final DoubleMatrix1D _vols;
  private final DoubleMatrix1D _errors;

  private final int _nSmileModelParameters;
  private final int _nKnotPoints;

  private final int _nOptions;
  private final int _nExpiries;
  private final int[] _struture;

  private final Set<String> _parameterNames;
  private final List<Function1D<T, double[]>> _volFuncs;
  private final List<Function1D<T, double[][]>> _volAdjointFuncs;

  /**
   * @param forwards Forward values of the underlying at the (increasing) expiry times
   * @param strikes An array of arrays that gives a set of strikes at each maturity (the outer array corresponds to the expiries and the
   *  inner arrays to the set of strikes at a particular expiry)
   * @param expiries The set of (increasing) expiry times
   * @param impliedVols An array of arrays that gives a set of implied volatilities at each maturity (with the same structure as strikes)
   * @param errors An array of arrays that gives a set of 'measurement' errors at each maturity (with the same structure as strikes)
   * @param model A smile model
   * @param nodePoints The time position of the nodes on each model parameter curve
   * @param interpolators The base interpolator used for each model parameter curve
   */
  public VolatilitySurfaceFitter(final double[] forwards, final double[][] strikes, final double[] expiries, final double[][] impliedVols,
      final double[][] errors, final VolatilityFunctionProvider<T> model, final LinkedHashMap<String, double[]> nodePoints, LinkedHashMap<String, Interpolator1D> interpolators) {

    Validate.notNull(forwards, "null forwards");
    Validate.notNull(strikes, "null strikes");
    Validate.notNull(expiries, "null expiries");
    Validate.notNull(impliedVols, "null implied vols");
    Validate.notNull(errors, "null error");
    Validate.notNull(model, "null model");

    _nExpiries = expiries.length;
    Validate.isTrue(forwards.length == _nExpiries, "#forwards != #expiries");
    Validate.isTrue(strikes.length == _nExpiries, "#strike sets != #expiries");
    Validate.isTrue(impliedVols.length == _nExpiries, "#vol sets != #expiries");
    Validate.isTrue(errors.length == _nExpiries, "#error sets != #expiries");

    _volFuncs = new ArrayList<Function1D<T, double[]>>(_nExpiries);
    _volAdjointFuncs = new ArrayList<Function1D<T, double[][]>>(_nExpiries);

    _struture = new int[_nExpiries];
    //check structure of common expiry strips
    int sum = 0;
    for (int i = 0; i < _nExpiries; i++) {
      int n = strikes[i].length;
      Validate.isTrue(impliedVols[i].length == n, "#vols in strip " + i + " is wrong");
      Validate.isTrue(errors[i].length == n, "#vols in strip " + i + " is wrong");

      Function1D<T, double[]> func = model.getVolatilityFunction(forwards[i], strikes[i], expiries[i]);
      _volFuncs.add(func);
      Function1D<T, double[][]> funcAdjoint = model.getModelAdjointFunction(forwards[i], strikes[i], expiries[i]);
      _volAdjointFuncs.add(funcAdjoint);
      _struture[i] = n;
      sum += n;
    }
    _nOptions = sum;

    _expiries = expiries;
    _strikes = strikes;

    double[] volsTemp = new double[_nOptions];
    double[] errorsTemp = new double[_nOptions];
    int index = 0;
    for (int i = 0; i < _nExpiries; i++) {
      for (int j = 0; j < _struture[i]; j++) {
        volsTemp[index] = impliedVols[i][j];
        errorsTemp[index] = errors[i][j];
        index++;
      }
    }
    _vols = new DoubleMatrix1D(volsTemp);
    _errors = new DoubleMatrix1D(errorsTemp);

    ParameterLimitsTransform[] transforms = getTransforms();

    _parameterNames = nodePoints.keySet();
    _nSmileModelParameters = _parameterNames.size();

    LinkedHashMap<String, Interpolator1D> transformedInterpolators = new LinkedHashMap<String, Interpolator1D>(_nSmileModelParameters);
    sum = 0;
    index = 0;
    for (String name : _parameterNames) {
      sum += nodePoints.get(name).length;
      Interpolator1D tInter = new TransformedInterpolator1D(interpolators.get(name), transforms[index++]);
      transformedInterpolators.put(name, tInter);
    }

    _curveBuilder = new InterpolatedCurveBuildingFunction(nodePoints, transformedInterpolators);
    _nKnotPoints = sum;

  }

  public LeastSquareResultsWithTransform solve(final DoubleMatrix1D start) {
    LeastSquareResults lsRes = SOLVER.solve(_vols, _errors, getModelValueFunction(), getModelJacobianFunction(), start);
    return new LeastSquareResultsWithTransform(lsRes, new UncoupledParameterTransforms(start, getTransforms(), new BitSet()));
  }

  /**
   * @return Returns a function that takes the fitting parameters (node values in the transformed fitting space) and returned the set of (model) volatilities
   */
  protected Function1D<DoubleMatrix1D, DoubleMatrix1D> getModelValueFunction() {

    return new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
        LinkedHashMap<String, InterpolatedDoublesCurve> curves = _curveBuilder.evaluate(x);

        Validate.isTrue(x.getNumberOfElements() == _nKnotPoints); //TODO remove when working properly

        double[] res = new double[_nOptions];
        int index = 0;

        for (int i = 0; i < _nExpiries; i++) {
          double t = _expiries[i];
          double[] theta = new double[_nSmileModelParameters];
          int p = 0;
          for (String name : _parameterNames) {
            Curve<Double, Double> curve = curves.get(name);
            theta[p++] = curve.getYValue(t);
          }
          T data = toSmileModelData(theta);
          double[] temp = _volFuncs.get(i).evaluate(data);
          int l = temp.length;
          System.arraycopy(temp, 0, res, index, l);
          index += l;
        }
        return new DoubleMatrix1D(res);
      }
    };
  }

  /**
   * @return Returns a function that takes the fitting parameters (node values in the transformed fitting space) and returned the
   * model Jacobian (i.e. the sensitivity of the model vols to the fitting parameters).
   */
  protected Function1D<DoubleMatrix1D, DoubleMatrix2D> getModelJacobianFunction() {

    final ParameterLimitsTransform[] transform = getTransforms();

    return new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {
      @Override
      public DoubleMatrix2D evaluate(DoubleMatrix1D x) {
        LinkedHashMap<String, InterpolatedDoublesCurve> curves = _curveBuilder.evaluate(x);

        double[][] res = new double[_nOptions][_nKnotPoints];
        int optionOffset = 0;

        for (int i = 0; i < _nExpiries; i++) {
          double t = _expiries[i];
          double[] theta = new double[_nSmileModelParameters]; //the model parameters
          double[] thetaHat = new double[_nSmileModelParameters]; //the fitting parameters
          double[][] sense = new double[_nSmileModelParameters][];
          int p = 0;
          for (String name : _parameterNames) {
            InterpolatedDoublesCurve curve = curves.get(name);
            theta[p] = curve.getYValue(t);
            thetaHat[p] = transform[p].transform(theta[p]);
            sense[p] = curve.getInterpolator().getNodeSensitivitiesForValue(curve.getDataBundle(), t);
            p++;
          }
          T data = toSmileModelData(theta);
          //this thing will be (#strikes/vols) x (# model Params)
          double[][] temp = _volAdjointFuncs.get(i).evaluate(data);
          final int l = temp.length;
          Validate.isTrue(l == _strikes[i].length); //TODO remove when working properly
          for (int j = 0; j < l; j++) {
            int paramOffset = 0;
            for (p = 0; p < _nSmileModelParameters; p++) {
              final int nSense = sense[p].length;
              final double paramSense = temp[j][p] * transform[p].inverseTransformGradient(thetaHat[p]);
              final double[] nodeSense = sense[p];
              for (int q = 0; q < nSense; q++) {
                res[j + optionOffset][q + paramOffset] = paramSense * nodeSense[q];
              }
              paramOffset += nSense;
            }
          }
          optionOffset += l;
        }
        return new DoubleMatrix2D(res);
      }
    };
  }

  protected abstract T toSmileModelData(final double[] modelParameters);

  protected abstract ParameterLimitsTransform[] getTransforms();

}
