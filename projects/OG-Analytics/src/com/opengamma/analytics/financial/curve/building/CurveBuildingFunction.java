/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.building;

import java.util.LinkedHashMap;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.analytics.financial.curve.generator.GeneratorCurve;
import com.opengamma.analytics.financial.curve.sensitivity.ParameterUnderlyingSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.linearalgebra.DecompositionFactory;
import com.opengamma.analytics.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Functions to build curves.
 * TODO: REVIEW: Change the static methods, embed then in an object.
 */
public class CurveBuildingFunction {

  /**
   * The absolute tolerance for the root finder.
   */
  private final double _toleranceAbs;
  /**
   * The relative tolerance for the root finder.
   */
  private final double _toleranceRel;
  /**
   * The relative tolerance for the root finder.
   */
  private final int _stepMaximum;
  /**
   * The root finder used for curve calibration.
   */
  private final BroydenVectorRootFinder _rootFinder;
  /**
   * The matrix algebra used for matrix inversion.
   */
  private static final MatrixAlgebra MATRIX_ALGEBRA = new CommonsMatrixAlgebra();

  /**
   * Constructor.
   * @param toleranceAbs The absolute tolerance for the root finder.
   * @param toleranceRel The relative tolerance for the root finder.
   * @param stepMaximum The maximum number of step for the root finder.
   */
  public CurveBuildingFunction(double toleranceAbs, double toleranceRel, int stepMaximum) {
    _toleranceAbs = toleranceAbs;
    _toleranceRel = toleranceRel;
    _stepMaximum = stepMaximum;
    _rootFinder = new BroydenVectorRootFinder(_toleranceAbs, _toleranceRel, _stepMaximum, DecompositionFactory.getDecomposition(DecompositionFactory.SV_COLT_NAME));
    // TODO: make the root finder flexible.
  }

  /**
   * Build a unit of curves.
   * @param instruments The instruments used for the unit calibration.
   * @param initGuess The initial parameters guess.
   * @param curveGenerators The map of curve names to curve generators used to build the unit.
   * @param knownData The known data (fx rates, other curves, model parameters, ...)
   * @param calculator The calculator of the value on which the calibration is done (usually ParSpreadMarketQuoteCalculator (recommended) or converted present value).
   * @param sensitivityCalculator The parameter sensitivity calculator.
   * @return The new curves and the calibrated parameters.
   */
  public Pair<YieldCurveBundle, Double[]> makeUnit(InstrumentDerivative[] instruments, double[] initGuess, LinkedHashMap<String, GeneratorCurve> curveGenerators, YieldCurveBundle knownData,
      final AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Double> calculator, final AbstractInstrumentDerivativeVisitor<YieldCurveBundle, InterestRateCurveSensitivity> sensitivityCalculator) {
    final MultipleYieldCurveFinderGeneratorDataBundle data = new MultipleYieldCurveFinderGeneratorDataBundle(instruments, knownData, curveGenerators);
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new MultipleYieldCurveFinderGeneratorFunction(calculator, data);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MultipleYieldCurveFinderGeneratorJacobian(new ParameterUnderlyingSensitivityCalculator(sensitivityCalculator), data);
    final double[] parameters = _rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initGuess)).getData();
    final YieldCurveBundle newCurves = data.getBuildingFunction().evaluate(new DoubleMatrix1D(parameters));
    return new ObjectsPair<YieldCurveBundle, Double[]>(newCurves, ArrayUtils.toObject(parameters));
  }

  /**
   * Build the Jacobian matrixes associated to a unit of curves.
   * @param instruments The instruments used for the block calibration.
   * @param curveGenerators The map of curve names to curve generators used to build the block.
   * @param startBlock The index of the first parameter of the unit in the block.
   * @param nbParameters The number of parameters for each curve in the unit.
   * @param parameters The parameters used to build each curve in the block.
   * @param knownData The known data (FX rates, other curves, model parameters, ...) for the block calibration.
   * @param sensitivityCalculator The parameter sensitivity calculator for the value on which the calibration is done (usually ParSpreadMarketQuoteCalculator (recommended) or converted present value).
   * @return The part of the inverse Jacobian matrix associated to each curve. 
   * The Jacobian matrix is the transition matrix between the curve parameters and the par spread.
   * TODO: Currently only for the ParSpreadMarketQuoteCalculator.
   */
  public DoubleMatrix2D[] makeCurveMatrix(InstrumentDerivative[] instruments, LinkedHashMap<String, GeneratorCurve> curveGenerators, int startBlock, int[] nbParameters, Double[] parameters,
      YieldCurveBundle knownData, final AbstractInstrumentDerivativeVisitor<YieldCurveBundle, InterestRateCurveSensitivity> sensitivityCalculator) {
    final MultipleYieldCurveFinderGeneratorDataBundle data = new MultipleYieldCurveFinderGeneratorDataBundle(instruments, knownData, curveGenerators);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MultipleYieldCurveFinderGeneratorJacobian(new ParameterUnderlyingSensitivityCalculator(sensitivityCalculator), data);
    final DoubleMatrix2D jacobian = jacobianCalculator.evaluate(new DoubleMatrix1D(parameters));
    final DoubleMatrix2D inverseJacobian = MATRIX_ALGEBRA.getInverse(jacobian);
    double[][] matrixTotal = inverseJacobian.getData();
    DoubleMatrix2D[] result = new DoubleMatrix2D[nbParameters.length];
    int startCurve = 0;
    for (int loopmat = 0; loopmat < nbParameters.length; loopmat++) {
      double[][] matrixCurve = new double[nbParameters[loopmat]][matrixTotal.length];
      for (int loopparam = 0; loopparam < nbParameters[loopmat]; loopparam++) {
        matrixCurve[loopparam] = matrixTotal[startBlock + startCurve + loopparam].clone();
      }
      result[loopmat] = new DoubleMatrix2D(matrixCurve);
      startCurve += nbParameters[loopmat];
    }
    return result;
  }

}
