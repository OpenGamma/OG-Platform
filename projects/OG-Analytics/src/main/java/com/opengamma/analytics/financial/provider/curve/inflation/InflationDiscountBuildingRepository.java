/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.inflation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.analytics.financial.curve.inflation.generator.GeneratorPriceIndexCurve;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurve;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.ParameterSensitivityInflationMatrixCalculator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.linearalgebra.DecompositionFactory;
import com.opengamma.analytics.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Functions to build inflation curves.
 */
// TODO: REVIEW: Embed in a better object.
public class InflationDiscountBuildingRepository {

  /**
   * The absolute tolerance for the root finder.
   */
  private final double _toleranceAbs;
  /**
   * The relative tolerance for the root finder.
   */
  private final double _toleranceRel;
  /**
   * The mximum number of steps for the root finder.
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

  /**.+-
   * Constructor.
   * @param toleranceAbs The absolute tolerance for the root finder.
   * @param toleranceRel The relative tolerance for the root finder.
   * @param stepMaximum The maximum number of step for the root finder.
   */
  public InflationDiscountBuildingRepository(final double toleranceAbs, final double toleranceRel, final int stepMaximum) {
    _toleranceAbs = toleranceAbs;
    _toleranceRel = toleranceRel;
    _stepMaximum = stepMaximum;
    _rootFinder = new BroydenVectorRootFinder(_toleranceAbs, _toleranceRel, _stepMaximum, DecompositionFactory.getDecomposition(DecompositionFactory.SV_COLT_NAME));
    // TODO: make the root finder flexible.
    // TODO: create a way to select the SensitivityMatrixMulticurve calculator (with underlying curve or not)
  }

  /**
   * Build a unit of curves without the discount curve.
   * @param instruments The instruments used for the unit calibration.
   * @param initGuess The initial parameters guess.
   * @param knownData The known data (fx rates, other curves, model parameters, ...)
   * @param discountingMap The discounting curves names map.
   * @param forwardIborMap The forward curves names map.
   * @param forwardONMap The forward curves names map.
   * @param generatorsMap The generators map.
   * @param calculator The calculator of the value on which the calibration is done (usually ParSpreadMarketQuoteCalculator (recommended) or converted present value).
   * @param sensitivityCalculator The parameter sensitivity calculator.
   * @return The new curves and the calibrated parameters.
   */
  private Pair<InflationProviderDiscount, Double[]> makeUnit(final InstrumentDerivative[] instruments, final double[] initGuess, final InflationProviderDiscount knownData,
      final LinkedHashMap<String, IndexPrice[]> inflationMap, final LinkedHashMap<String, GeneratorPriceIndexCurve> generatorsMap,
      final InstrumentDerivativeVisitor<InflationProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<InflationProviderInterface, InflationSensitivity> sensitivityCalculator) {
    final GeneratorInflationProviderDiscount generator = new GeneratorInflationProviderDiscount(knownData, inflationMap, generatorsMap);
    final InflationDiscountBuildingData data = new InflationDiscountBuildingData(instruments, generator);
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new InflationDiscountFinderFunction(calculator, data);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new InflationDiscountFinderJacobian(new ParameterSensitivityInflationMatrixCalculator(sensitivityCalculator),
        data);
    final double[] parameters = _rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initGuess)).getData();
    final InflationProviderDiscount newCurves = data.getGeneratorMarket().evaluate(new DoubleMatrix1D(parameters));
    return Pairs.of(newCurves, ArrayUtils.toObject(parameters));
  }

  /**
   * Build a unit of curves with the discount curve.
   * @param instruments The instruments used for the unit calibration.
   * @param initGuess The initial parameters guess.
   * @param knownData The known data (fx rates, other curves, model parameters, ...)
   * @param discountingMap The discounting curves names map.
   * @param forwardIborMap The forward curves names map.
   * @param forwardONMap The forward curves names map.
   * @param generatorsMap The generators map.
   * @param calculator The calculator of the value on which the calibration is done (usually ParSpreadInflationMarketQuoteDiscountingCalculator (recommended) or converted present value).
   * @param sensitivityCalculator The parameter sensitivity calculator.
   * @return The new curves and the calibrated parameters.
   */
  private Pair<InflationProviderDiscount, Double[]> makeUnit(final InstrumentDerivative[] instruments, final double[] initGuess, final InflationProviderDiscount knownData,
      final LinkedHashMap<String, Currency> discountingMap, final LinkedHashMap<String, IndexON[]> forwardONMap, final LinkedHashMap<String, IndexPrice[]> inflationMap,
      final LinkedHashMap<String, GeneratorCurve> generatorsMap, final InstrumentDerivativeVisitor<InflationProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<InflationProviderInterface, InflationSensitivity> sensitivityCalculator)
  {
    final GeneratorInflationProviderDiscount generator = new GeneratorInflationProviderDiscount(knownData, discountingMap, forwardONMap, inflationMap, generatorsMap);
    final InflationDiscountBuildingData data = new InflationDiscountBuildingData(instruments, generator);
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new InflationDiscountFinderFunction(calculator, data);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new InflationDiscountFinderJacobian(new ParameterSensitivityInflationMatrixCalculator(sensitivityCalculator),
        data);
    final double[] parameters = _rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initGuess)).getData();
    final InflationProviderDiscount newCurves = data.getGeneratorMarket().evaluate(new DoubleMatrix1D(parameters));
    return Pairs.of(newCurves, ArrayUtils.toObject(parameters));
  }

  /**
   * Build the Jacobian matrixes associated to a unit of curves without the discount curve.
   * @param instruments The instruments used for the block calibration.
   * @param startBlock The index of the first parameter of the unit in the block.
   * @param nbParameters The number of parameters for each curve in the unit.
   * @param parameters The parameters used to build each curve in the block.
   * @param knownData The known data (FX rates, other curves, model parameters, ...) for the block calibration.
   * @param discountingMap The discounting curves names map.
   * @param forwardIborMap The forward curves names map.
   * @param forwardONMap The forward curves names map.
   * @param generatorsMap The generators map.
   * @param sensitivityCalculator The parameter sensitivity calculator for the value on which the calibration is done
  (usually ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator (recommended) or converted present value).
   * @return The part of the inverse Jacobian matrix associated to each curve.
   * The Jacobian matrix is the transition matrix between the curve parameters and the par spread.
   */
  // TODO: Currently only for the ParSpreadInflationMarketQuoteDiscountingCalculator.
  private DoubleMatrix2D[] makeCurveMatrix(final InstrumentDerivative[] instruments, final int startBlock, final int[] nbParameters, final Double[] parameters,
      final InflationProviderDiscount knownData,
      final LinkedHashMap<String, IndexPrice[]> inflationMap, final LinkedHashMap<String, GeneratorPriceIndexCurve> generatorsMap,
      final InstrumentDerivativeVisitor<InflationProviderInterface, InflationSensitivity> sensitivityCalculator) {
    final GeneratorInflationProviderDiscount generator = new GeneratorInflationProviderDiscount(knownData, inflationMap, generatorsMap);
    final InflationDiscountBuildingData data = new InflationDiscountBuildingData(instruments, generator);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new InflationDiscountFinderJacobian(new ParameterSensitivityInflationMatrixCalculator(sensitivityCalculator),
        data);
    final DoubleMatrix2D jacobian = jacobianCalculator.evaluate(new DoubleMatrix1D(parameters));
    final DoubleMatrix2D inverseJacobian = MATRIX_ALGEBRA.getInverse(jacobian);
    final double[][] matrixTotal = inverseJacobian.getData();
    final DoubleMatrix2D[] result = new DoubleMatrix2D[nbParameters.length];
    int startCurve = 0;
    for (int loopmat = 0; loopmat < nbParameters.length; loopmat++) {
      final double[][] matrixCurve = new double[nbParameters[loopmat]][matrixTotal.length];
      for (int loopparam = 0; loopparam < nbParameters[loopmat]; loopparam++) {
        matrixCurve[loopparam] = matrixTotal[startBlock + startCurve + loopparam].clone();
      }
      result[loopmat] = new DoubleMatrix2D(matrixCurve);
      startCurve += nbParameters[loopmat];
    }
    return result;
  }

  /**
   * Build the Jacobian matrixes associated to a unit of curves without the discount curve.
   * @param instruments The instruments used for the block calibration.
   * @param startBlock The index of the first parameter of the unit in the block.
   * @param nbParameters The number of parameters for each curve in the unit.
   * @param parameters The parameters used to build each curve in the block.
   * @param knownData The known data (FX rates, other curves, model parameters, ...) for the block calibration.
   * @param discountingMap The discounting curves names map.
   * @param forwardIborMap The forward curves names map.
   * @param forwardONMap The forward curves names map.
   * @param generatorsMap The generators map.
   * @param sensitivityCalculator The parameter sensitivity calculator for the value on which the calibration is done
  (usually ParSpreadInflationMarketQuoteDiscountingCalculator (recommended) or converted present value).
   * @return The part of the inverse Jacobian matrix associated to each curve.
   * The Jacobian matrix is the transition matrix between the curve parameters and the par spread.
   */
  private DoubleMatrix2D[] makeCurveMatrix(final InstrumentDerivative[] instruments, final int startBlock, final int[] nbParameters, final Double[] parameters,
      final InflationProviderDiscount knownData, final LinkedHashMap<String, Currency> discountingMap, final LinkedHashMap<String, IndexON[]> forwardONMap,
      final LinkedHashMap<String, IndexPrice[]> inflationMap, final LinkedHashMap<String, GeneratorCurve> generatorsMap,
      final InstrumentDerivativeVisitor<InflationProviderInterface, InflationSensitivity> sensitivityCalculator) {
    final GeneratorInflationProviderDiscount generator = new GeneratorInflationProviderDiscount(knownData, discountingMap, forwardONMap, inflationMap, generatorsMap);
    final InflationDiscountBuildingData data = new InflationDiscountBuildingData(instruments, generator);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new InflationDiscountFinderJacobian(new ParameterSensitivityInflationMatrixCalculator(sensitivityCalculator),
        data);
    final DoubleMatrix2D jacobian = jacobianCalculator.evaluate(new DoubleMatrix1D(parameters));
    final DoubleMatrix2D inverseJacobian = MATRIX_ALGEBRA.getInverse(jacobian);
    final double[][] matrixTotal = inverseJacobian.getData();
    final DoubleMatrix2D[] result = new DoubleMatrix2D[nbParameters.length];
    int startCurve = 0;
    for (int loopmat = 0; loopmat < nbParameters.length; loopmat++) {
      final double[][] matrixCurve = new double[nbParameters[loopmat]][matrixTotal.length];
      for (int loopparam = 0; loopparam < nbParameters[loopmat]; loopparam++) {
        matrixCurve[loopparam] = matrixTotal[startBlock + startCurve + loopparam].clone();
      }
      result[loopmat] = new DoubleMatrix2D(matrixCurve);
      startCurve += nbParameters[loopmat];
    }
    return result;
  }

  /**
   * Build a block of curves without the discount curve.
   * @param curveBundles The curve bundles, not null
   * @param knownData The known data (fx rates, other curves, model parameters, ...)
   * @param inflationMap The inflation curves names map.
   * @param calculator The calculator of the value on which the calibration is done (usually ParSpreadInflationMarketQuoteDiscountingCalculator (recommended) or converted present value).
   * @param sensitivityCalculator The parameter sensitivity calculator.
   * @return A pair with the calibrated yield curve bundle (including the known data) and the CurveBuildingBlckBundle with the relevant inverse Jacobian Matrix.
   */
  public Pair<InflationProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDerivatives(final MultiCurveBundle<GeneratorPriceIndexCurve>[] curveBundles,
      final InflationProviderDiscount knownData, final LinkedHashMap<String, IndexPrice[]> inflationMap,
      final InstrumentDerivativeVisitor<InflationProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<InflationProviderInterface, InflationSensitivity> sensitivityCalculator) {
    ArgumentChecker.notNull(curveBundles, "curve bundles");
    ArgumentChecker.notNull(knownData, "known data");
    ArgumentChecker.notNull(inflationMap, "inflation map");
    ArgumentChecker.notNull(calculator, "calculator");
    ArgumentChecker.notNull(sensitivityCalculator, "sensitivity calculator");
    final int nbUnits = curveBundles.length;
    final InflationProviderDiscount knownSoFarData = knownData.copy();
    final List<InstrumentDerivative> instrumentsSoFar = new ArrayList<>();
    final LinkedHashMap<String, GeneratorPriceIndexCurve> generatorsSoFar = new LinkedHashMap<>();
    final LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> unitBundleSoFar = new LinkedHashMap<>();
    final List<Double> parametersSoFar = new ArrayList<>();
    final LinkedHashMap<String, Pair<Integer, Integer>> unitMap = new LinkedHashMap<>();
    int startUnit = 0;

    for (int iUnits = 0; iUnits < nbUnits; iUnits++) {
      final MultiCurveBundle<GeneratorPriceIndexCurve> curveBundle = curveBundles[iUnits];
      final int nbCurve = curveBundle.size();
      final int[] startCurve = new int[nbCurve]; // First parameter index of the curve in the unit.
      final LinkedHashMap<String, GeneratorPriceIndexCurve> generators = new LinkedHashMap<>();
      final int[] nbIns = new int[curveBundle.getNumberOfInstruments()];
      int nbInsUnit = 0; // Number of instruments in the unit.
      for (int iCurve = 0; iCurve < nbCurve; iCurve++) {
        final SingleCurveBundle<GeneratorPriceIndexCurve> singleCurve = curveBundle.getCurveBundle(iCurve);
        startCurve[iCurve] = nbInsUnit;
        nbIns[iCurve] = singleCurve.size();
        nbInsUnit += nbIns[iCurve];
        instrumentsSoFar.addAll(Arrays.asList(singleCurve.getDerivatives()));
      }
      final InstrumentDerivative[] instrumentsUnit = new InstrumentDerivative[nbInsUnit];
      final double[] parametersGuess = new double[nbInsUnit];
      final InstrumentDerivative[] instrumentsSoFarArray = instrumentsSoFar.toArray(new InstrumentDerivative[instrumentsSoFar.size()]);
      for (int iCurve = 0; iCurve < nbCurve; iCurve++) {
        final SingleCurveBundle<GeneratorPriceIndexCurve> singleCurve = curveBundle.getCurveBundle(iCurve);
        final InstrumentDerivative[] derivatives = singleCurve.getDerivatives();
        System.arraycopy(derivatives, 0, instrumentsUnit, startCurve[iCurve], nbIns[iCurve]);
        System.arraycopy(singleCurve.getStartingPoint(), 0, parametersGuess, startCurve[iCurve], nbIns[iCurve]);
        final GeneratorPriceIndexCurve tmp = singleCurve.getCurveGenerator().finalGenerator(derivatives);
        final String curveName = singleCurve.getCurveName();
        generators.put(curveName, tmp);
        generatorsSoFar.put(curveName, tmp);
        unitMap.put(curveName, Pairs.of(startUnit + startCurve[iCurve], nbIns[iCurve]));
      }
      final Pair<InflationProviderDiscount, Double[]> unitCal = makeUnit(instrumentsUnit, parametersGuess, knownSoFarData, inflationMap, generators, calculator,
          sensitivityCalculator);

      parametersSoFar.addAll(Arrays.asList(unitCal.getSecond()));
      final DoubleMatrix2D[] mat = makeCurveMatrix(instrumentsSoFarArray, startUnit, nbIns, parametersSoFar.toArray(new Double[parametersSoFar.size()]),
          knownData, inflationMap, generatorsSoFar, sensitivityCalculator);

      for (int iCurve = 0; iCurve < nbCurve; iCurve++) {
        final SingleCurveBundle<GeneratorPriceIndexCurve> singleCurve = curveBundle.getCurveBundle(iCurve);
        unitBundleSoFar.put(singleCurve.getCurveName(), Pairs.of(new CurveBuildingBlock(unitMap), mat[iCurve]));
      }
      knownSoFarData.setAll(unitCal.getFirst());
      startUnit = startUnit + nbInsUnit;
    }

    return Pairs.of(knownSoFarData, new CurveBuildingBlockBundle(unitBundleSoFar));
  }

  /**
   * Build a block of curves with the discount curve.
   * @param curveBundles The curve bundles, notn ull
   * @param knownData The known data (fx rates, other curves, model parameters, ...)
   * @param discountingMap The discount curves names map.
   * @param forwardONMap The ON curves names map.
   * @param inflationMap The inflation curves names map.
   * @param calculator The calculator of the value on which the calibration is done (usually ParSpreadInflationMarketQuoteDiscountingCalculator (recommended) or converted present value).
   * @param sensitivityCalculator The parameter sensitivity calculator.
   * @return A pair with the calibrated yield curve bundle (including the known data) and the CurveBuildingBlckBundle with the relevant inverse Jacobian Matrix.
   */
  public Pair<InflationProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDerivatives(final MultiCurveBundle<GeneratorCurve>[] curveBundles,
      final InflationProviderDiscount knownData, final LinkedHashMap<String, Currency> discountingMap,
      final LinkedHashMap<String, IndexON[]> forwardONMap, final LinkedHashMap<String, IndexPrice[]> inflationMap,
      final InstrumentDerivativeVisitor<InflationProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<InflationProviderInterface, InflationSensitivity> sensitivityCalculator) {
    ArgumentChecker.notNull(curveBundles, "curve bundles");
    ArgumentChecker.notNull(knownData, "known data");
    ArgumentChecker.notNull(discountingMap, "discounting map");
    ArgumentChecker.notNull(forwardONMap, "forward overnight map");
    ArgumentChecker.notNull(calculator, "calculator");
    ArgumentChecker.notNull(sensitivityCalculator, "sensitivity calculator");
    final int nbUnits = curveBundles.length;
    final InflationProviderDiscount knownSoFarData = knownData.copy();
    final List<InstrumentDerivative> instrumentsSoFar = new ArrayList<>();
    final LinkedHashMap<String, GeneratorCurve> generatorsSoFar = new LinkedHashMap<>();
    final LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> unitBundleSoFar = new LinkedHashMap<>();
    final List<Double> parametersSoFar = new ArrayList<>();
    final LinkedHashMap<String, Pair<Integer, Integer>> unitMap = new LinkedHashMap<>();
    int startUnit = 0;
    for (int iUnits = 0; iUnits < nbUnits; iUnits++) {
      final MultiCurveBundle<GeneratorCurve> curveBundle = curveBundles[iUnits];
      final int nbCurve = curveBundle.size();
      final int[] startCurve = new int[nbCurve]; // First parameter index of the curve in the unit.
      final LinkedHashMap<String, GeneratorCurve> generators = new LinkedHashMap<>();
      final int[] nbIns = new int[curveBundle.getNumberOfInstruments()];
      int nbInsUnit = 0; // Number of instruments in the unit.
      for (int iCurve = 0; iCurve < nbCurve; iCurve++) {
        final SingleCurveBundle<GeneratorCurve> singleCurve = curveBundle.getCurveBundle(iCurve);
        startCurve[iCurve] = nbInsUnit;
        nbIns[iCurve] = singleCurve.size();
        nbInsUnit += nbIns[iCurve];
        instrumentsSoFar.addAll(Arrays.asList(singleCurve.getDerivatives()));
      }
      final InstrumentDerivative[] instrumentsUnit = new InstrumentDerivative[nbInsUnit];
      final double[] parametersGuess = new double[nbInsUnit];
      final InstrumentDerivative[] instrumentsSoFarArray = instrumentsSoFar.toArray(new InstrumentDerivative[instrumentsSoFar.size()]);
      for (int iCurve = 0; iCurve < nbCurve; iCurve++) {
        final SingleCurveBundle<GeneratorCurve> singleCurve = curveBundle.getCurveBundle(iCurve);
        final InstrumentDerivative[] derivatives = singleCurve.getDerivatives();
        System.arraycopy(derivatives, 0, instrumentsUnit, startCurve[iCurve], nbIns[iCurve]);
        System.arraycopy(singleCurve.getStartingPoint(), 0, parametersGuess, startCurve[iCurve], nbIns[iCurve]);
        final GeneratorCurve tmp = singleCurve.getCurveGenerator().finalGenerator(derivatives);
        final String curveName = singleCurve.getCurveName();
        generators.put(curveName, tmp);
        generatorsSoFar.put(curveName, tmp);
        unitMap.put(curveName, Pairs.of(startUnit + startCurve[iCurve], nbIns[iCurve]));
      }
      final Pair<InflationProviderDiscount, Double[]> unitCal = makeUnit(instrumentsUnit, parametersGuess, knownSoFarData, discountingMap, forwardONMap, inflationMap, generators,
          calculator, sensitivityCalculator);
      parametersSoFar.addAll(Arrays.asList(unitCal.getSecond()));
      final DoubleMatrix2D[] mat = makeCurveMatrix(instrumentsSoFarArray, startUnit, nbIns, parametersSoFar.toArray(new Double[parametersSoFar.size()]), knownData,
          discountingMap, forwardONMap, inflationMap, generatorsSoFar, sensitivityCalculator);

      for (int iCurve = 0; iCurve < nbCurve; iCurve++) {
        final SingleCurveBundle<GeneratorCurve> singleCurve = curveBundle.getCurveBundle(iCurve);
        unitBundleSoFar.put(singleCurve.getCurveName(), Pairs.of(new CurveBuildingBlock(unitMap), mat[iCurve]));
      }
      knownSoFarData.setAll(unitCal.getFirst());
      startUnit = startUnit + nbInsUnit;
    }

    return Pairs.of(knownSoFarData, new CurveBuildingBlockBundle(unitBundleSoFar));
  }
}
