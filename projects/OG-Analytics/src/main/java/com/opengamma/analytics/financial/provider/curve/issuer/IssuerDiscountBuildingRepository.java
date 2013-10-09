/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.issuer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.issuer.ParameterSensitivityIssuerMatrixCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.linearalgebra.DecompositionFactory;
import com.opengamma.analytics.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Functions to build curves.
 */
// TODO: REVIEW: Embed in a better object.
public class IssuerDiscountBuildingRepository {

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
  public IssuerDiscountBuildingRepository(final double toleranceAbs, final double toleranceRel, final int stepMaximum) {
    _toleranceAbs = toleranceAbs;
    _toleranceRel = toleranceRel;
    _stepMaximum = stepMaximum;
    _rootFinder = new BroydenVectorRootFinder(_toleranceAbs, _toleranceRel, _stepMaximum, DecompositionFactory.getDecomposition(DecompositionFactory.SV_COLT_NAME));
    // TODO: make the root finder flexible.
    // TODO: create a way to select the SensitivityMatrixMulticurve calculator (with underlying curve or not)
  }

  /**
   * Build a unit of curves.
   * @param instruments The instruments used for the unit calibration.
   * @param initGuess The initial parameters guess.
   * @param knownData The known data (fx rates, other curves, model parameters, ...)
   * @param discountingMap The discounting curves names map.
   * @param forwardIborMap The forward curves names map.
   * @param forwardONMap The forward curves names map.
   * @param issuerMap The issuer curves names map.
   * @param generatorsMap The generators map.
   * @param calculator The calculator of the value on which the calibration is done (usually ParSpreadMarketQuoteCalculator (recommended) or converted present value).
   * @param sensitivityCalculator The parameter sensitivity calculator.
   * @return The new curves and the calibrated parameters.
   */
  private Pair<IssuerProviderDiscount, Double[]> makeUnit(final InstrumentDerivative[] instruments, final double[] initGuess, final IssuerProviderDiscount knownData,
      final LinkedHashMap<String, Currency> discountingMap, final LinkedHashMap<String, IborIndex[]> forwardIborMap, final LinkedHashMap<String, IndexON[]> forwardONMap,
      final LinkedHashMap<String, Pair<String, Currency>> issuerMap, final LinkedHashMap<String, GeneratorYDCurve> generatorsMap,
      final InstrumentDerivativeVisitor<IssuerProviderInterface, Double> calculator, final InstrumentDerivativeVisitor<IssuerProviderInterface, MulticurveSensitivity> sensitivityCalculator) {
    final GeneratorIssuerProviderDiscount generator = new GeneratorIssuerProviderDiscount(knownData, discountingMap, forwardIborMap, forwardONMap, issuerMap, generatorsMap);
    final IssuerDiscountBuildingData data = new IssuerDiscountBuildingData(instruments, generator);
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new IssuerDiscountFinderFunction(calculator, data);
    //    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new IssuerDiscountFinderJacobian(new ParameterSensitivityIssuerUnderlyingMatrixCalculator(sensitivityCalculator), data);
    // TODO
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new IssuerDiscountFinderJacobian(new ParameterSensitivityIssuerMatrixCalculator(sensitivityCalculator), data);
    final double[] parameters = _rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initGuess)).getData();
    final IssuerProviderDiscount newCurves = data.getGeneratorMarket().evaluate(new DoubleMatrix1D(parameters));
    return new ObjectsPair<>(newCurves, ArrayUtils.toObject(parameters));
  }

  /**
   * Build the Jacobian matrixes associated to a unit of curves.
   * @param instruments The instruments used for the block calibration.
   * @param startBlock The index of the first parameter of the unit in the block.
   * @param nbParameters The number of parameters for each curve in the unit.
   * @param parameters The parameters used to build each curve in the block.
   * @param knownData The known data (FX rates, other curves, model parameters, ...) for the block calibration.
   * @param discountingMap The discounting curves names map.
   * @param forwardIborMap The forward curves names map.
   * @param forwardONMap The forward curves names map.
   * @param issuerMap The issuer curves names map.
   * @param generatorsMap The generators map.
   * @param sensitivityCalculator The parameter sensitivity calculator for the value on which the calibration is done
  (usually ParSpreadMarketQuoteDiscountingProviderCalculator (recommended) or converted present value).
   * @return The part of the inverse Jacobian matrix associated to each curve.
   * The Jacobian matrix is the transition matrix between the curve parameters and the par spread.
   */
  // TODO: Currently only for the ParSpreadMarketQuoteDiscountingProviderCalculator.
  private DoubleMatrix2D[] makeCurveMatrix(final InstrumentDerivative[] instruments, final int startBlock, final int[] nbParameters, final Double[] parameters, final IssuerProviderDiscount knownData,
      final LinkedHashMap<String, Currency> discountingMap, final LinkedHashMap<String, IborIndex[]> forwardIborMap, final LinkedHashMap<String, IndexON[]> forwardONMap,
      final LinkedHashMap<String, Pair<String, Currency>> issuerMap, final LinkedHashMap<String, GeneratorYDCurve> generatorsMap,
      final InstrumentDerivativeVisitor<IssuerProviderInterface, MulticurveSensitivity> sensitivityCalculator) {
    final GeneratorIssuerProviderDiscount generator = new GeneratorIssuerProviderDiscount(knownData, discountingMap, forwardIborMap, forwardONMap, issuerMap, generatorsMap);
    final IssuerDiscountBuildingData data = new IssuerDiscountBuildingData(instruments, generator);
    //    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MulticurveDiscountFinderJacobian(new
    //        ParameterSensitivityMulticurveUnderlyingMatrixCalculator(sensitivityCalculator), data);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new IssuerDiscountFinderJacobian(new ParameterSensitivityIssuerMatrixCalculator(sensitivityCalculator), data); // TODO
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

//  /**
//   * Build a block of curves.
//   * @param instruments The instruments used for the block calibration.
//   * @param curveGenerators The curve generators (final version). As an array of arrays, representing the units and the curves within the units.
//   * @param curveNames The names of the different curves. As an array of arrays, representing the units and the curves within the units.
//   * @param parametersGuess The initial guess for the parameters. As an array of arrays, representing the units and the parameters for one unit (all the curves of the unit concatenated).
//   * @param knownData The known data (fx rates, other curves, model parameters, ...)
//   * @param discountingMap The discounting curves names map.
//   * @param forwardIborMap The forward curves names map.
//   * @param forwardONMap The forward curves names map.
//   * @param issuerMap The issuer curves names map.
//   * @param calculator The calculator of the value on which the calibration is done (usually ParSpreadMarketQuoteCalculator (recommended) or converted present value).
//   * @param sensitivityCalculator The parameter sensitivity calculator.
//   * @return A pair with the calibrated yield curve bundle (including the known data) and the CurveBuildingBlckBundle with the relevant inverse Jacobian Matrix.
//   */
//  public Pair<IssuerProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDerivatives(final InstrumentDerivative[][][] instruments, final GeneratorYDCurve[][] curveGenerators,
//      final String[][] curveNames, final double[][] parametersGuess, final IssuerProviderDiscount knownData, final LinkedHashMap<String, Currency> discountingMap,
//      final LinkedHashMap<String, IborIndex[]> forwardIborMap, final LinkedHashMap<String, IndexON[]> forwardONMap, final LinkedHashMap<String, Pair<String, Currency>> issuerMap,
//      final InstrumentDerivativeVisitor<IssuerProviderInterface, Double> calculator, final InstrumentDerivativeVisitor<IssuerProviderInterface, MulticurveSensitivity> sensitivityCalculator) {
//    final int nbUnits = curveGenerators.length;
//    final IssuerProviderDiscount knownSoFarData = knownData.copy();
//    final List<InstrumentDerivative> instrumentsSoFar = new ArrayList<>();
//    final LinkedHashMap<String, GeneratorYDCurve> generatorsSoFar = new LinkedHashMap<>();
//    final LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> unitBundleSoFar = new LinkedHashMap<>();
//    final List<Double> parametersSoFar = new ArrayList<>();
//    final LinkedHashMap<String, Pair<Integer, Integer>> unitMap = new LinkedHashMap<>();
//    int startUnit = 0;
//    for (int loopunit = 0; loopunit < nbUnits; loopunit++) {
//      final int nbCurve = curveGenerators[loopunit].length;
//      final int[] startCurve = new int[nbCurve]; // First parameter index of the curve in the unit.
//      final LinkedHashMap<String, GeneratorYDCurve> gen = new LinkedHashMap<>();
//      final int[] nbIns = new int[curveGenerators[loopunit].length];
//      int nbInsUnit = 0; // Number of instruments in the unit.
//      for (int loopcurve = 0; loopcurve < nbCurve; loopcurve++) {
//        startCurve[loopcurve] = nbInsUnit;
//        nbIns[loopcurve] = instruments[loopunit][loopcurve].length;
//        nbInsUnit += nbIns[loopcurve];
//        instrumentsSoFar.addAll(Arrays.asList(instruments[loopunit][loopcurve]));
//      }
//      final InstrumentDerivative[] instrumentsUnit = new InstrumentDerivative[nbInsUnit];
//      final InstrumentDerivative[] instrumentsSoFarArray = instrumentsSoFar.toArray(new InstrumentDerivative[instrumentsSoFar.size()]);
//      for (int loopcurve = 0; loopcurve < nbCurve; loopcurve++) {
//        System.arraycopy(instruments[loopunit][loopcurve], 0, instrumentsUnit, startCurve[loopcurve], nbIns[loopcurve]);
//      }
//      for (int loopcurve = 0; loopcurve < nbCurve; loopcurve++) {
//        final GeneratorYDCurve tmp = curveGenerators[loopunit][loopcurve].finalGenerator(instruments[loopunit][loopcurve]);
//        gen.put(curveNames[loopunit][loopcurve], tmp);
//        generatorsSoFar.put(curveNames[loopunit][loopcurve], tmp);
//        unitMap.put(curveNames[loopunit][loopcurve], new ObjectsPair<>(startUnit + startCurve[loopcurve], nbIns[loopcurve]));
//      }
//      final Pair<IssuerProviderDiscount, Double[]> unitCal = makeUnit(instrumentsUnit, parametersGuess[loopunit], knownSoFarData, discountingMap, forwardIborMap,
//          forwardONMap, issuerMap, gen, calculator, sensitivityCalculator);
//      parametersSoFar.addAll(Arrays.asList(unitCal.getSecond()));
//      final DoubleMatrix2D[] mat = makeCurveMatrix(instrumentsSoFarArray, startUnit, nbIns, parametersSoFar.toArray(new Double[parametersSoFar.size()]), knownData,
//          discountingMap, forwardIborMap, forwardONMap, issuerMap, generatorsSoFar, sensitivityCalculator);
//      // TODO: should curve matrix be computed only once at the end? To save time
//      for (int loopcurve = 0; loopcurve < curveGenerators[loopunit].length; loopcurve++) {
//        unitBundleSoFar.put(curveNames[loopunit][loopcurve], new ObjectsPair<>(new CurveBuildingBlock(unitMap), mat[loopcurve]));
//      }
//      knownSoFarData.setAll(unitCal.getFirst());
//      startUnit = startUnit + nbInsUnit;
//    }
//    return new ObjectsPair<>(knownSoFarData, new CurveBuildingBlockBundle(unitBundleSoFar));
//  }

  /**
   * Build a block of curves.
   * @param curveBundles The bundles of curve data used in construction.
   * @param knownData The known data (fx rates, other curves, model parameters, ...)
   * @param discountingMap The discounting curves names map.
   * @param forwardIborMap The forward curves names map.
   * @param forwardONMap The forward curves names map.
   * @param issuerMap The issuer curves names map.
   * @param calculator The calculator of the value on which the calibration is done (usually ParSpreadMarketQuoteCalculator (recommended) or converted present value).
   * @param sensitivityCalculator The parameter sensitivity calculator.
   * @return A pair with the calibrated yield curve bundle (including the known data) and the CurveBuildingBlckBundle with the relevant inverse Jacobian Matrix.
   */
  public Pair<IssuerProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDerivatives(final MultiCurveBundle<GeneratorYDCurve>[] curveBundles,
      final IssuerProviderDiscount knownData, final LinkedHashMap<String, Currency> discountingMap,
      final LinkedHashMap<String, IborIndex[]> forwardIborMap, final LinkedHashMap<String, IndexON[]> forwardONMap, final LinkedHashMap<String, Pair<String, Currency>> issuerMap,
      final InstrumentDerivativeVisitor<IssuerProviderInterface, Double> calculator, final InstrumentDerivativeVisitor<IssuerProviderInterface, MulticurveSensitivity> sensitivityCalculator) {
    ArgumentChecker.notNull(curveBundles, "curve bundles");
    ArgumentChecker.notNull(knownData, "known data");
    ArgumentChecker.notNull(discountingMap, "discounting map");
    ArgumentChecker.notNull(forwardIborMap, "forward ibor map");
    ArgumentChecker.notNull(forwardONMap, "forward overnight map");
    ArgumentChecker.notNull(issuerMap, "issuer map");
    ArgumentChecker.notNull(calculator, "calculator");
    ArgumentChecker.notNull(sensitivityCalculator, "sensitivity calculator");
    final int nbUnits = curveBundles.length;
    final IssuerProviderDiscount knownSoFarData = knownData.copy();
    final List<InstrumentDerivative> instrumentsSoFar = new ArrayList<>();
    final LinkedHashMap<String, GeneratorYDCurve> generatorsSoFar = new LinkedHashMap<>();
    final LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> unitBundleSoFar = new LinkedHashMap<>();
    final List<Double> parametersSoFar = new ArrayList<>();
    final LinkedHashMap<String, Pair<Integer, Integer>> unitMap = new LinkedHashMap<>();
    int startUnit = 0;
    for (int iUnits = 0; iUnits < nbUnits; iUnits++) {
      final MultiCurveBundle<GeneratorYDCurve> curveBundle = curveBundles[iUnits];
      final int nbCurve = curveBundle.size();
      final int[] startCurve = new int[nbCurve]; // First parameter index of the curve in the unit.
      final LinkedHashMap<String, GeneratorYDCurve> gen = new LinkedHashMap<>();
      final int[] nbIns = new int[curveBundle.getNumberOfInstruments()];
      int nbInsUnit = 0; // Number of instruments in the unit.
      for (int iCurve = 0; iCurve < nbCurve; iCurve++) {
        final SingleCurveBundle<GeneratorYDCurve> singleCurve = curveBundle.getCurveBundle(iCurve);
        startCurve[iCurve] = nbInsUnit;
        nbIns[iCurve] = singleCurve.size();
        nbInsUnit += nbIns[iCurve];
        instrumentsSoFar.addAll(Arrays.asList(singleCurve.getDerivatives()));
      }
      final InstrumentDerivative[] instrumentsUnit = new InstrumentDerivative[nbInsUnit];
      final double[] parametersGuess = new double[nbInsUnit];
      final InstrumentDerivative[] instrumentsSoFarArray = instrumentsSoFar.toArray(new InstrumentDerivative[instrumentsSoFar.size()]);
      for (int iCurve = 0; iCurve < nbCurve; iCurve++) {
        final SingleCurveBundle<GeneratorYDCurve> singleCurve = curveBundle.getCurveBundle(iCurve);
        final InstrumentDerivative[] derivatives = singleCurve.getDerivatives();
        System.arraycopy(derivatives, 0, instrumentsUnit, startCurve[iCurve], nbIns[iCurve]);
        System.arraycopy(singleCurve.getStartingPoint(), 0, parametersGuess, startCurve[iCurve], nbIns[iCurve]);
        final GeneratorYDCurve tmp = singleCurve.getCurveGenerator().finalGenerator(derivatives);
        final String curveName = singleCurve.getCurveName();
        gen.put(curveName, tmp);
        generatorsSoFar.put(curveName, tmp);
        unitMap.put(curveName, new ObjectsPair<>(startUnit + startCurve[iCurve], nbIns[iCurve]));
      }
      final Pair<IssuerProviderDiscount, Double[]> unitCal = makeUnit(instrumentsUnit, parametersGuess, knownSoFarData,
          discountingMap, forwardIborMap, forwardONMap, issuerMap, gen, calculator, sensitivityCalculator);
      parametersSoFar.addAll(Arrays.asList(unitCal.getSecond()));
      final DoubleMatrix2D[] mat = makeCurveMatrix(instrumentsSoFarArray, startUnit, nbIns, parametersSoFar.toArray(new Double[parametersSoFar.size()]), knownData, discountingMap,
          forwardIborMap, forwardONMap, issuerMap, generatorsSoFar, sensitivityCalculator);
      // TODO: should curve matrix be computed only once at the end? To save time
      for (int iCurve = 0; iCurve < nbCurve; iCurve++) {
        final SingleCurveBundle<GeneratorYDCurve> singleCurve = curveBundle.getCurveBundle(iCurve);
        unitBundleSoFar.put(singleCurve.getCurveName(), new ObjectsPair<>(new CurveBuildingBlock(unitMap), mat[iCurve]));
      }
      knownSoFarData.setAll(unitCal.getFirst());
      startUnit = startUnit + nbInsUnit;
    }
    return new ObjectsPair<>(knownSoFarData, new CurveBuildingBlockBundle(unitBundleSoFar));
  }
}
