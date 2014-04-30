/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.hullwhite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.hullwhite.ParameterSensitivityHullWhiteMatrixCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.hullwhite.ParameterSensitivityHullWhiteUnderlyingMatrixCalculator;
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
import com.opengamma.util.tuple.Pairs;

/**
 * Functions to build curves.
 */
// TODO: REVIEW: Embed in a better object.
// TODO: This class should be re-factored with ProviderDiscountBuildingRepository.
public class HullWhiteProviderDiscountBuildingRepository {

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
  public HullWhiteProviderDiscountBuildingRepository(final double toleranceAbs, final double toleranceRel, final int stepMaximum) {
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
   * @param knownData The known data (fx rates, other curves, model parameters, ...)
   * @param discountingMap The discounting curves names map.
   * @param forwardIborMap The forward curves names map.
   * @param forwardONMap The forward curves names map.
   * @param generatorsMap The generators map.
   * @param calculator The calculator of the value on which the calibration is done (usually ParSpreadMarketQuoteCalculator (recommended) or converted present value).
   * @param sensitivityCalculator The parameter sensitivity calculator.
   * @return The new curves and the calibrated parameters.
   */
  private HullWhiteOneFactorProviderDiscount makeUnit(final InstrumentDerivative[] instruments, final double[] initGuess, final HullWhiteOneFactorProviderDiscount knownData,
      final LinkedHashMap<String, Currency> discountingMap, final LinkedHashMap<String, IborIndex[]> forwardIborMap, final LinkedHashMap<String, IndexON[]> forwardONMap,
      final LinkedHashMap<String, GeneratorYDCurve> generatorsMap, final InstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, MulticurveSensitivity> sensitivityCalculator) {
    final GeneratorHullWhiteProviderDiscount generator = new GeneratorHullWhiteProviderDiscount(knownData, discountingMap, forwardIborMap, forwardONMap, generatorsMap);
    final HullWhiteProviderDiscountBuildingData data = new HullWhiteProviderDiscountBuildingData(instruments, generator);
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new HullWhiteProviderDiscountFinderFunction(calculator, data);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new HullWhiteProviderDiscountFinderJacobian(new ParameterSensitivityHullWhiteMatrixCalculator(sensitivityCalculator), data);
    final double[] parameters = _rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initGuess)).getData();
    final HullWhiteOneFactorProviderDiscount newCurves = data.getGeneratorMarket().evaluate(new DoubleMatrix1D(parameters));
    return newCurves;
  }

  /**
   * Construct the CurveBuildingBlock associated to all the curve built so far and updates the CurveBuildingBlockBundle.
   * @param instruments The instruments used for the block calibration.
   * @param hullWhite The known curves including the current unit.
   * @param currentCurvesArray
   * @param blockBundle
   * @param sensitivityCalculator The parameter sensitivity calculator for the value on which the calibration is done
  (usually ParSpreadMarketQuoteDiscountingProviderCalculator (recommended) or converted present value).
   * @return The part of the inverse Jacobian matrix associated to each curve. Only the part for the curve in the current unit (not the previous units).
   * The Jacobian matrix is the transition matrix between the curve parameters and the par spread.
   */
  private void updateBlockBundle(final InstrumentDerivative[] instruments, final HullWhiteOneFactorProviderDiscount hullWhite, final List<String> currentCurvesList,
      final CurveBuildingBlockBundle blockBundle, final InstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, MulticurveSensitivity> sensitivityCalculator) {
    // Sensitivity calculator
    final ParameterSensitivityHullWhiteUnderlyingMatrixCalculator parameterSensitivityCalculator = new ParameterSensitivityHullWhiteUnderlyingMatrixCalculator(sensitivityCalculator);
    int loopc;
    final MulticurveProviderDiscount multicurves = hullWhite.getMulticurveProvider();
    final LinkedHashMap<String, Pair<Integer, Integer>> mapBlockOut = new LinkedHashMap<>();
    // Curve names manipulation
    final Set<String> allCurveName1 = multicurves.getAllNames();
    final int nbCurrentCurves = currentCurvesList.size();
    final LinkedHashSet<String> currentCurves = new LinkedHashSet<>(currentCurvesList);
    final LinkedHashSet<String> beforeCurveName = new LinkedHashSet<>(allCurveName1);
    beforeCurveName.removeAll(currentCurves);
    final LinkedHashSet<String> allCurveName = new LinkedHashSet<>(beforeCurveName);
    allCurveName.addAll(currentCurves); // Manipulation to ensure that the new curves are at the end.
    //Implementation note : if blockBundle don't contain a block for a specific curve then we remove this curve from  beforeCurveName. 
    //Because we can't compute the total bundle without the block for each curve. So we are computing a total bundle without this curve.
    for (final String name : beforeCurveName) {
      if (!(blockBundle.getData().containsKey(name))) {
        beforeCurveName.remove(name);
      }
    }
    final int nbAllCurve = allCurveName.size();
    final int nbBeforeCurves = nbAllCurve - nbCurrentCurves;
    // Current curves size and nb parameters
    int nbParametersCurrentTotal = 0;
    final int[] nbParametersCurrent = new int[nbCurrentCurves];
    final int[] startIndexCurrent = new int[nbCurrentCurves];
    loopc = 0;
    final LinkedHashSet<String> currentBeforeCurveNames = new LinkedHashSet<>(beforeCurveName);
    for (final String name : currentCurves) {
      startIndexCurrent[loopc] = nbParametersCurrentTotal;
      nbParametersCurrent[loopc] = multicurves.getCurve(name).getNumberOfIntrinsicParameters(currentBeforeCurveNames);
      currentBeforeCurveNames.add(name);
      nbParametersCurrentTotal += nbParametersCurrent[loopc];
      loopc++;
    }
    // Sensitivity to parameters
    final int nbIns = instruments.length;
    final double[][] res = new double[nbParametersCurrentTotal][];
    for (int loopinstrument = 0; loopinstrument < nbIns; loopinstrument++) {
      res[loopinstrument] = parameterSensitivityCalculator.calculateSensitivity(instruments[loopinstrument], hullWhite, allCurveName).getData();
      // The sensitivity is to all parameters in the order provided by the allCurveName
    }

    final int nbParametersAllCurvesTotal = res[0].length;
    // Jacobian direct
    final int nbParametersBeforeTotal = res[0].length - nbParametersCurrentTotal;
    final double[][] direct = new double[nbParametersCurrentTotal][nbParametersCurrentTotal];
    for (int loopp = 0; loopp < nbIns; loopp++) {
      System.arraycopy(res[loopp], nbParametersBeforeTotal, direct[loopp], 0, nbParametersCurrentTotal);
    }
    final DoubleMatrix2D pDmCurrentMatrix = MATRIX_ALGEBRA.getInverse(new DoubleMatrix2D(direct));
    // Jacobian indirect: when nbBefor
    double[][] pDmBeforeArray = new double[0][0];
    if (nbParametersBeforeTotal > 0) {
      final double[][] nonDirect = new double[nbParametersCurrentTotal][nbParametersBeforeTotal];
      for (int loopp = 0; loopp < nbIns; loopp++) {
        System.arraycopy(res[loopp], 0, nonDirect[loopp], 0, nbParametersBeforeTotal);
      }
      final DoubleMatrix2D pDpBeforeMatrix;
      pDpBeforeMatrix = (DoubleMatrix2D) MATRIX_ALGEBRA.scale(MATRIX_ALGEBRA.multiply(pDmCurrentMatrix, new DoubleMatrix2D(nonDirect)), -1.0);
      // All curves: order and size
      final int[] nbParametersBefore = new int[nbBeforeCurves];
      final int[] startIndexBefore = new int[nbBeforeCurves];
      int tempNbParam = 0;
      loopc = 0;
      for (final String name : beforeCurveName) {
        nbParametersBefore[loopc] = multicurves.getNumberOfParameters(name);
        startIndexBefore[loopc] = tempNbParam;
        tempNbParam += nbParametersBefore[loopc];
        loopc++;
      }
      // Transition Matrix: all curves before current
      final double[][] transition = new double[nbParametersBeforeTotal][nbParametersBeforeTotal];
      loopc = 0;
      int loopc2 = 0;
      for (final String name : beforeCurveName) { // l
        final Pair<CurveBuildingBlock, DoubleMatrix2D> thisPair = blockBundle.getBlock(name);
        final CurveBuildingBlock thisBlock = thisPair.getFirst();
        final Set<String> thisBlockCurves = thisBlock.getAllNames();
        final double[][] thisMatrix = thisPair.getSecond().getData();
        loopc2 = 0;
        for (final String name2 : beforeCurveName) { // k
          if (thisBlockCurves.contains(name2)) { // If not, the matrix stay with 0
            final Integer start = thisBlock.getStart(name2);
            for (int loopp = 0; loopp < nbParametersBefore[loopc]; loopp++) {
              System.arraycopy(thisMatrix[loopp], start, transition[startIndexBefore[loopc] + loopp], startIndexBefore[loopc2], thisBlock.getNbParameters(name2));
            }
          }
          loopc2++;
        }
        loopc++;
      }
      final DoubleMatrix2D transitionMatrix = new DoubleMatrix2D(transition);
      DoubleMatrix2D pDmBeforeMatrix;
      pDmBeforeMatrix = (DoubleMatrix2D) MATRIX_ALGEBRA.multiply(pDpBeforeMatrix, transitionMatrix);
      pDmBeforeArray = pDmBeforeMatrix.getData();
      loopc = 0;
      for (final String name : beforeCurveName) {
        mapBlockOut.put(name, ObjectsPair.of((Integer) startIndexBefore[loopc], (Integer) nbParametersBefore[loopc]));
        loopc++;
      }
    }
    loopc = 0;
    for (final String name : currentCurves) {
      mapBlockOut.put(name, ObjectsPair.of((Integer) (nbParametersBeforeTotal + startIndexCurrent[loopc]), (Integer) nbParametersCurrent[loopc]));
      loopc++;
    }
    final CurveBuildingBlock blockOut = new CurveBuildingBlock(mapBlockOut);
    final double[][] pDmCurrentArray = pDmCurrentMatrix.getData();
    loopc = 0;
    for (final String name : currentCurves) {
      final double[][] pDmCurveArray = new double[nbParametersCurrent[loopc]][nbParametersAllCurvesTotal];
      for (int loopp = 0; loopp < nbParametersCurrent[loopc]; loopp++) {
        System.arraycopy(pDmCurrentArray[startIndexCurrent[loopc] + loopp], 0, pDmCurveArray[loopp], nbParametersBeforeTotal, nbParametersCurrentTotal);
      }
      if (nbParametersBeforeTotal > 0) {
        for (int loopp = 0; loopp < nbParametersCurrent[loopc]; loopp++) {
          System.arraycopy(pDmBeforeArray[startIndexCurrent[loopc] + loopp], 0, pDmCurveArray[loopp], 0, nbParametersBeforeTotal);
        }
      }
      final DoubleMatrix2D pDmCurveMatrix = new DoubleMatrix2D(pDmCurveArray);
      blockBundle.add(name, blockOut, pDmCurveMatrix);
      loopc++;
    }
  }

  /**
   * Build a block of curves.
   * @param curveBundles The curve bundles, not null
   * @param knownData The known data (fx rates, other curves, model parameters, ...)
   * @param discountingMap The discounting curves names map.
   * @param forwardIborMap The forward curves names map.
   * @param forwardONMap The forward curves names map.
   * @param calculator The calculator of the value on which the calibration is done (usually ParSpreadMarketQuoteCalculator (recommended) or converted present value).
   * @param sensitivityCalculator The parameter sensitivity calculator.
   * @return A pair with the calibrated yield curve bundle (including the known data) and the CurveBuildingBlckBundle with the relevant inverse Jacobian Matrix.
   */
  public Pair<HullWhiteOneFactorProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDerivatives(final MultiCurveBundle<GeneratorYDCurve>[] curveBundles,
      final HullWhiteOneFactorProviderDiscount knownData, final LinkedHashMap<String, Currency> discountingMap,
      final LinkedHashMap<String, IborIndex[]> forwardIborMap, final LinkedHashMap<String, IndexON[]> forwardONMap,
      final InstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, MulticurveSensitivity> sensitivityCalculator) {
    return makeCurvesFromDerivatives(curveBundles, knownData, new CurveBuildingBlockBundle(), discountingMap, forwardIborMap, forwardONMap, calculator, sensitivityCalculator);
  }

  /**
   * Build a block of curves.
   * @param curveBundles The curve bundles, not null
   * @param knownData The known data (fx rates, other curves, model parameters, ...)
   * @param knownBlockBundle The already build CurveBuildingBlockBundle.
   * @param discountingMap The discounting curves names map.
   * @param forwardIborMap The forward curves names map.
   * @param forwardONMap The forward curves names map.
   * @param calculator The calculator of the value on which the calibration is done (usually ParSpreadMarketQuoteCalculator (recommended) or converted present value).
   * @param sensitivityCalculator The parameter sensitivity calculator.
   * @return A pair with the calibrated yield curve bundle (including the known data) and the CurveBuildingBlckBundle with the relevant inverse Jacobian Matrix.
   */
  public Pair<HullWhiteOneFactorProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDerivatives(final MultiCurveBundle<GeneratorYDCurve>[] curveBundles,
      final HullWhiteOneFactorProviderDiscount knownData, final CurveBuildingBlockBundle knownBlockBundle,
      final LinkedHashMap<String, Currency> discountingMap, final LinkedHashMap<String, IborIndex[]> forwardIborMap, final LinkedHashMap<String, IndexON[]> forwardONMap,
      final InstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, MulticurveSensitivity> sensitivityCalculator) {
    ArgumentChecker.notNull(curveBundles, "curve bundles");
    ArgumentChecker.notNull(knownData, "known data");
    ArgumentChecker.notNull(discountingMap, "discounting map");
    ArgumentChecker.notNull(forwardIborMap, "forward ibor map");
    ArgumentChecker.notNull(forwardONMap, "forward overnight map");
    ArgumentChecker.notNull(calculator, "calculator");
    ArgumentChecker.notNull(sensitivityCalculator, "sensitivity calculator");
    final int nbUnits = curveBundles.length;
    HullWhiteOneFactorProviderDiscount knownSoFarData = knownData.copy();
    final CurveBuildingBlockBundle totalBundle = new CurveBuildingBlockBundle();
    totalBundle.addAll(knownBlockBundle);
    final List<InstrumentDerivative> instrumentsSoFar = new ArrayList<>();
    final LinkedHashMap<String, GeneratorYDCurve> generatorsSoFar = new LinkedHashMap<>();
    final LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> unitBundleSoFar = new LinkedHashMap<>();
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
      for (int iCurve = 0; iCurve < nbCurve; iCurve++) {
        final SingleCurveBundle<GeneratorYDCurve> singleCurve = curveBundle.getCurveBundle(iCurve);
        final InstrumentDerivative[] derivatives = singleCurve.getDerivatives();
        System.arraycopy(derivatives, 0, instrumentsUnit, startCurve[iCurve], nbIns[iCurve]);
        System.arraycopy(singleCurve.getStartingPoint(), 0, parametersGuess, startCurve[iCurve], nbIns[iCurve]);
        final GeneratorYDCurve tmp = singleCurve.getCurveGenerator().finalGenerator(derivatives);
        final String curveName = singleCurve.getCurveName();
        gen.put(curveName, tmp);
        generatorsSoFar.put(curveName, tmp);
        unitMap.put(curveName, Pairs.of(startUnit + startCurve[iCurve], nbIns[iCurve]));
      }

      knownSoFarData = makeUnit(instrumentsUnit, parametersGuess, knownSoFarData,
          discountingMap, forwardIborMap, forwardONMap, gen, calculator, sensitivityCalculator);
      updateBlockBundle(instrumentsUnit, knownSoFarData, curveBundle.getNames(), totalBundle, sensitivityCalculator);
      startUnit = startUnit + nbInsUnit;
    }
    return Pairs.of(knownSoFarData, new CurveBuildingBlockBundle(unitBundleSoFar));
  }

}
