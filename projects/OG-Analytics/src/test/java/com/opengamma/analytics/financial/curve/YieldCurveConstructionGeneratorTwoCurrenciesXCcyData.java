/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Data for the testing of curve construction with FX swaps and XCCy swaps
 * TODO: This is old code that has been commented. It should be removed at some point.
 */
@Test(groups = TestGroup.UNIT)
public class YieldCurveConstructionGeneratorTwoCurrenciesXCcyData {

  //  private static final Interpolator1D INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
  //      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  //  private static final MatrixAlgebra MATRIX_ALGEBRA = new CommonsMatrixAlgebra();
  //
  //  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();
  //  private static final PresentValueBasisPointCalculator PVBP_CALCULATOR = PresentValueBasisPointCalculator.getInstance();
  //  private static final double TOLERANCE_ROOT = 1.0E-10;
  //  private static final BroydenVectorRootFinder ROOT_FINDER = new BroydenVectorRootFinder(TOLERANCE_ROOT, TOLERANCE_ROOT, 10000,
  //      DecompositionFactory.getDecomposition(DecompositionFactory.SV_COLT_NAME));
  //  static final Currency CCY1 = Currency.EUR;
  //  private static final Currency CCY2 = Currency.USD;
  //  private static final double FX_EURUSD = 1.40;
  //  private static final FXMatrix FX_MATRIX = new FXMatrix(CCY2, CCY1, FX_EURUSD);
  //  private static final Calendar CALENDAR = new MondayToFridayCalendar("CAL");
  //  private static final int SPOT_LAG = 2;
  //  private static final DayCount DAY_COUNT_CASH = DayCounts.ACT_360;
  //  private static final double NOTIONAL = 1.0;
  //  private static final BusinessDayConvention BDC = BusinessDayConventions.MODIFIED_FOLLOWING;
  //  private static final IndexON INDEX_ON_1 = new IndexON("Fed Fund", CCY1, DAY_COUNT_CASH, 1, CALENDAR);
  //  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_1 = new GeneratorDepositON("USD Deposit ON", CCY1, CALENDAR, DAY_COUNT_CASH);
  //  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_2 = new GeneratorDepositON("EUR Deposit ON", CCY2, CALENDAR, DAY_COUNT_CASH);
  //  private static final GeneratorSwapFixedON GENERATOR_OIS_1 = new GeneratorSwapFixedON("USD1YFEDFUND", INDEX_ON_1, Period.ofMonths(12), DAY_COUNT_CASH, BDC, true, SPOT_LAG, SPOT_LAG);
  //  private static final GeneratorForexSwap GENERATOR_FX = new GeneratorForexSwap("EURUSD", CCY2, CCY1, CALENDAR, SPOT_LAG, BDC, true);
  //  private static final GeneratorDeposit GENERATOR_DEPOSIT_1 = new GeneratorDeposit("USD Deposit", CCY1, CALENDAR, SPOT_LAG, DAY_COUNT_CASH, BDC, true);
  //  private static final GeneratorDeposit GENERATOR_DEPOSIT_2 = new GeneratorDeposit("EuR Deposit", CCY2, CALENDAR, SPOT_LAG, DAY_COUNT_CASH, BDC, true);
  //  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  //  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("USD6MLIBOR3M", CALENDAR);
  //  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("EUR1YEURIBOR3M", CALENDAR);
  //  private static final IborIndex USDLIBOR3M = USD6MLIBOR3M.getIborIndex();
  //  private static final IborIndex EURIBOR3M = EUR1YEURIBOR3M.getIborIndex();
  //  private static final GeneratorSwapXCcyIborIbor GENERATOR_XCCY = new GeneratorSwapXCcyIborIbor("EURIBOR3MUSDLIBOR3M", EURIBOR3M, USDLIBOR3M); // Spread on EUR leg
  //
  //  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2011, 9, 28);
  //
  //  private static final ArrayZonedDateTimeDoubleTimeSeries TS_EMPTY = new ArrayZonedDateTimeDoubleTimeSeries();
  //  private static final ArrayZonedDateTimeDoubleTimeSeries TS_ON_USD_WITH_TODAY = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
  //      DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });
  //  private static final ArrayZonedDateTimeDoubleTimeSeries TS_ON_USD_WITHOUT_TODAY = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
  //      DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });
  //  @SuppressWarnings("rawtypes")
  //  static final DoubleTimeSeries[] TS_FIXED_OIS_USD_WITH_TODAY = new DoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITH_TODAY };
  //  @SuppressWarnings("rawtypes")
  //  static final DoubleTimeSeries[] TS_FIXED_OIS_USD_WITHOUT_TODAY = new DoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITHOUT_TODAY };
  //
  //  private static final ArrayZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITH_TODAY = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
  //      DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.0035, 0.0036 });
  //  private static final ArrayZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITHOUT_TODAY = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27) },
  //      new double[] {0.0035 });
  //
  //  private static final ArrayZonedDateTimeDoubleTimeSeries TS_IBOR_EUR3M_WITH_TODAY = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
  //      DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.0060, 0.0061 });
  //  private static final ArrayZonedDateTimeDoubleTimeSeries TS_IBOR_EUR3M_WITHOUT_TODAY = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27) },
  //      new double[] {0.0060 });
  //  @SuppressWarnings("rawtypes")
  //  static final DoubleTimeSeries[] TS_FIXED_IBOR_USD3M_WITH_TODAY = new DoubleTimeSeries[] {TS_IBOR_USD3M_WITH_TODAY };
  //  @SuppressWarnings("rawtypes")
  //  static final DoubleTimeSeries[] TS_FIXED_IBOR_USD3M_WITHOUT_TODAY = new DoubleTimeSeries[] {TS_IBOR_USD3M_WITHOUT_TODAY };
  //  @SuppressWarnings("rawtypes")
  //  static final DoubleTimeSeries[] TS_FIXED_IBOR_EUR3M_WITH_TODAY = new DoubleTimeSeries[] {TS_IBOR_EUR3M_WITH_TODAY };
  //  @SuppressWarnings("rawtypes")
  //  static final DoubleTimeSeries[] TS_FIXED_IBOR_EUR3M_WITHOUT_TODAY = new DoubleTimeSeries[] {TS_IBOR_EUR3M_WITHOUT_TODAY };
  //  @SuppressWarnings("rawtypes")
  //  static final DoubleTimeSeries[] TS_FIXED_IBOR_EURUSD3M_WITH_TODAY = new DoubleTimeSeries[] {TS_IBOR_EUR3M_WITH_TODAY, TS_IBOR_USD3M_WITH_TODAY };
  //  @SuppressWarnings("rawtypes")
  //  static final DoubleTimeSeries[] TS_FIXED_IBOR_EURUSD3M_WITHOUT_TODAY = new DoubleTimeSeries[] {TS_IBOR_EUR3M_WITHOUT_TODAY, TS_IBOR_USD3M_WITHOUT_TODAY };
  //
  //  static final String CURVE_NAME_DSC_1 = "USD Dsc";
  //  static final String CURVE_NAME_DSC_2 = "EUR Dsc";
  //  static final String CURVE_NAME_FWD_1 = "USD Fwd 3M";
  //  static final String CURVE_NAME_FWD_2 = "EUR Fwd 3M";
  //  private static final HashMap<String, Currency> CCY_MAP = new HashMap<String, Currency>();
  //
  //  /** Market values for the dsc USD curve */
  //  public static final double[] DSC_1_MARKET_QUOTES = new double[] {0.0045, 0.0050, 0.0075, 0.0080, 0.0085, 0.0090, 0.0095, 0.0110, 0.0120, 0.0130, 0.0140, 0.0150, 0.0200 };
  //  /** Generators for the dsc USD curve */
  //  public static final GeneratorInstrument[] DSC_1_GENERATORS = new GeneratorInstrument[] {GENERATOR_DEPOSIT_ON_1, GENERATOR_DEPOSIT_ON_1, GENERATOR_OIS_1, GENERATOR_OIS_1, GENERATOR_OIS_1,
  //    GENERATOR_OIS_1, GENERATOR_OIS_1, GENERATOR_OIS_1, GENERATOR_OIS_1, GENERATOR_OIS_1, GENERATOR_OIS_1, GENERATOR_OIS_1, GENERATOR_OIS_1};
  //  /** Tenors for the dsc USD curve */
  //  public static final Period[] DSC_1_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
  //    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
  //
  //  /** Market values for the Fwd 3M USD curve */
  //  public static final double[] FWD_1_MARKET_QUOTES = new double[] {0.0050, 0.0075, 0.0160, 0.0170, 0.0200, 0.0225 };
  //  /** Generators for the Fwd 3M USD curve */
  //  public static final GeneratorInstrument[] FWD_1_GENERATORS = new GeneratorInstrument[] {GENERATOR_DEPOSIT_1, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M };
  //  /** Tenors for the Fwd 3M USD curve */
  //  public static final Period[] FWD_1_TENOR = new Period[] {Period.ofMonths(3), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(5), Period.ofYears(10) };
  //
  //  /** Market values for the dsc EUR curve */
  //  public static final double[] DSC_2_MARKET_QUOTES = new double[] {0.0070, 0.0075, 0.0002, 0.0004, 0.0006, 0.0012, 0.0018, 0.0025, -0.0025, -0.0025, -0.0025, -0.0025, -0.0025 };
  //  /** Generators for the dsc EUR curve */
  //  public static final GeneratorInstrument[] DSC_2_GENERATORS = new GeneratorInstrument[] {GENERATOR_DEPOSIT_ON_2, GENERATOR_DEPOSIT_ON_2, GENERATOR_FX, GENERATOR_FX, GENERATOR_FX, GENERATOR_FX,
  //    GENERATOR_FX, GENERATOR_FX, GENERATOR_XCCY, GENERATOR_XCCY, GENERATOR_XCCY, GENERATOR_XCCY, GENERATOR_XCCY};
  //  /** Tenors for the dsc EUR curve */
  //  public static final Period[] DSC_2_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
  //    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
  //  //  /** FX rqtes for the dsc EUR curve */
  //  //  public static final Double[] DSC_2_FX_RATE = new Double[] {FX_EURUSD, FX_EURUSD, FX_EURUSD, FX_EURUSD, FX_EURUSD, FX_EURUSD, FX_EURUSD, FX_EURUSD, FX_EURUSD, FX_EURUSD, FX_EURUSD, FX_EURUSD,
  //  //      FX_EURUSD};
  //
  //  /** Market values for the Fwd 3M EUR curve */
  //  public static final double[] FWD_2_MARKET_QUOTES = new double[] {0.0075, 0.0100, 0.0185, 0.0195, 0.0205, 0.0215, 0.0225, 0.0250 };
  //  /** Generators for the Fwd 3M USD curve */
  //  public static final GeneratorInstrument[] FWD_2_GENERATORS = new GeneratorInstrument[] {GENERATOR_DEPOSIT_2, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M,
  //    EUR1YEURIBOR3M, EUR1YEURIBOR3M};
  //  /** Tenors for the Fwd 3M USD curve */
  //  public static final Period[] FWD_2_TENOR = new Period[] {Period.ofMonths(3), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
  //    Period.ofYears(10) };
  //
  //  /** Standard USD discounting curve instrument definitions */
  //  public static final List<InstrumentDefinition<?>> DEFINITIONS_DSC_1;
  //  /** Standard USD Forward 3M curve instrument definitions */
  //  public static final List<InstrumentDefinition<?>> DEFINITIONS_FWD_1;
  //  /** Standard EUR discounting curve instrument definitions */
  //  public static final List<InstrumentDefinition<?>> DEFINITIONS_DSC_2;
  //  /** Standard EUR Forward 3M curve instrument definitions */
  //  public static final List<InstrumentDefinition<?>> DEFINITIONS_FWD_2;
  //
  //  static {
  //    DEFINITIONS_DSC_1 = getDefinitions(DSC_1_MARKET_QUOTES, DSC_1_GENERATORS, DSC_1_TENOR);
  //    DEFINITIONS_FWD_1 = getDefinitions(FWD_1_MARKET_QUOTES, FWD_1_GENERATORS, FWD_1_TENOR);
  //    DEFINITIONS_DSC_2 = getDefinitions(DSC_2_MARKET_QUOTES, DSC_2_GENERATORS, DSC_2_TENOR);
  //    DEFINITIONS_FWD_2 = getDefinitions(FWD_2_MARKET_QUOTES, FWD_2_GENERATORS, FWD_2_TENOR);
  //    CCY_MAP.put(CURVE_NAME_DSC_1, CCY1);
  //    CCY_MAP.put(CURVE_NAME_FWD_1, CCY1);
  //    CCY_MAP.put(CURVE_NAME_DSC_2, CCY2);
  //    CCY_MAP.put(CURVE_NAME_FWD_2, CCY2);
  //  }
  //
  //  /**
  //   * Curve types used in the curve construction
  //   */
  //  public enum CurveType {
  //    /** USD Discounting **/
  //    DSC_1,
  //    /** EUR Discounting **/
  //    DSC_2,
  //    /** USD Forward 3M **/
  //    FWD_1,
  //    /** EUR Forward 3M **/
  //    FWD_2;
  //  }
  //
  //  public static List<InstrumentDefinition<?>> getDefinitions(final double[] marketQuotes, final GeneratorInstrument[] generators, final Period[] tenors) {
  //    final List<InstrumentDefinition<?>> definitions = new ArrayList<InstrumentDefinition<?>>();
  //    for (int loopmv = 0; loopmv < marketQuotes.length; loopmv++) {
  //      definitions.add(generators[loopmv].generateInstrument(NOW, tenors[loopmv], marketQuotes[loopmv], NOTIONAL, FX_MATRIX));
  //    }
  //    return definitions;
  //  }
  //
  //  private static double initialGuess(final InstrumentDefinition<?> instrument) {
  //    if (instrument instanceof SwapFixedONDefinition) {
  //      return ((SwapFixedONDefinition) instrument).getFixedLeg().getNthPayment(0).getRate();
  //    }
  //    if (instrument instanceof SwapFixedIborDefinition) {
  //      return ((SwapFixedIborDefinition) instrument).getFixedLeg().getNthPayment(0).getRate();
  //    }
  //    if (instrument instanceof ForwardRateAgreementDefinition) {
  //      return ((ForwardRateAgreementDefinition) instrument).getRate();
  //    }
  //    if (instrument instanceof CashDefinition) {
  //      return ((CashDefinition) instrument).getRate();
  //    }
  //    return 0.01;
  //  }
  //
  //  @SuppressWarnings({"unchecked"})
  //  /**
  //   * The returned pair contains 1) the yield curve bundle and 2) the inverse Jacobian (derivative of the curve parameters with respect to the market quotes).
  //   */
  //  public static Pair<YieldCurveBundle, DoubleMatrix2D> makeCurves(final List<InstrumentDefinition<?>> dsc1Definitions, final List<InstrumentDefinition<?>> fwd1Definitions,
  //      final List<InstrumentDefinition<?>> dsc2Definitions, final List<InstrumentDefinition<?>> fwd2Definitions,
  //      final InstrumentDerivativeVisitor<YieldCurveBundle, Double> calculator,
  //      final InstrumentDerivativeVisitor<YieldCurveBundle, InterestRateCurveSensitivity> sensitivityCalculator, final boolean withToday, final boolean isPV) {
  //    final int nbDsc1 = dsc1Definitions.size();
  //    final int nbFwd1 = fwd1Definitions.size();
  //    final int nbDsc2 = dsc2Definitions.size();
  //    final int nbFwd2 = fwd2Definitions.size();
  //    final InstrumentDerivative[] instruments = new InstrumentDerivative[nbDsc1 + nbFwd1 + nbDsc2 + nbFwd2];
  //    final double[] dsc1Node = new double[nbDsc1];
  //    final double[] fwd1Node = new double[nbFwd1];
  //    final double[] dsc2Node = new double[nbDsc2];
  //    final double[] fwd2Node = new double[nbFwd2];
  //    final double[] initGuess = new double[nbDsc1 + nbFwd1 + nbDsc2 + nbFwd2];
  //    int i = 0;
  //    for (final InstrumentDefinition<?> instrument : dsc1Definitions) {
  //      InstrumentDerivative ird;
  //      if (instrument instanceof SwapFixedONDefinition) {
  //        final String[] curveNames = new String[] {CURVE_NAME_DSC_1, CURVE_NAME_DSC_1 };
  //        ird = ((SwapFixedONDefinition) instrument).toDerivative(NOW, withToday ? TS_FIXED_OIS_USD_WITH_TODAY : TS_FIXED_OIS_USD_WITHOUT_TODAY, curveNames);
  //      } else {
  //        final String[] curveNames = new String[] {CURVE_NAME_DSC_1 };
  //        ird = instrument.toDerivative(NOW, curveNames);
  //      }
  //      instruments[i] = ird;
  //      initGuess[i] = initialGuess(instrument);
  //      dsc1Node[i++] = ird.accept(MATURITY_CALCULATOR);
  //    }
  //    int j = 0;
  //    for (final InstrumentDefinition<?> instrument : fwd1Definitions) {
  //      InstrumentDerivative ird;
  //      if (instrument instanceof SwapFixedIborDefinition) {
  //        final String[] curveNames = new String[] {CURVE_NAME_DSC_1, CURVE_NAME_FWD_1 };
  //        ird = ((SwapFixedIborDefinition) instrument).toDerivative(NOW, withToday ? TS_FIXED_IBOR_USD3M_WITH_TODAY : TS_FIXED_IBOR_USD3M_WITHOUT_TODAY, curveNames);
  //      } else {
  //        final String[] curveNames = new String[] {CURVE_NAME_FWD_1 };
  //        ird = instrument.toDerivative(NOW, curveNames);
  //      }
  //      instruments[i] = ird;
  //      initGuess[i++] = initialGuess(instrument);
  //      fwd1Node[j++] = ird.accept(MATURITY_CALCULATOR);
  //    }
  //    j = 0;
  //    for (final InstrumentDefinition<?> instrument : dsc2Definitions) {
  //      InstrumentDerivative ird;
  //      if (instrument instanceof SwapXCcyIborIborDefinition) {
  //        final String[] curveNames = new String[] {CURVE_NAME_DSC_2, CURVE_NAME_FWD_2, CURVE_NAME_DSC_1, CURVE_NAME_FWD_1 };
  //        ird = ((SwapXCcyIborIborDefinition) instrument).toDerivative(NOW, withToday ? TS_FIXED_IBOR_EURUSD3M_WITH_TODAY : TS_FIXED_IBOR_EURUSD3M_WITHOUT_TODAY, curveNames);
  //      } else {
  //        if (instrument instanceof CashDefinition) {
  //          final String[] curveNames = new String[] {CURVE_NAME_DSC_2 };
  //          ird = instrument.toDerivative(NOW, curveNames);
  //        } else {
  //          final String[] curveNames = new String[] {CURVE_NAME_DSC_2, CURVE_NAME_DSC_1 };
  //          ird = instrument.toDerivative(NOW, curveNames);
  //        }
  //      }
  //      instruments[i] = ird;
  //      initGuess[i++] = initialGuess(instrument);
  //      dsc2Node[j++] = ird.accept(MATURITY_CALCULATOR);
  //    }
  //    j = 0;
  //    for (final InstrumentDefinition<?> instrument : fwd2Definitions) {
  //      InstrumentDerivative ird;
  //      if (instrument instanceof SwapFixedIborDefinition) {
  //        final String[] curveNames = new String[] {CURVE_NAME_DSC_2, CURVE_NAME_FWD_2 };
  //        ird = ((SwapFixedIborDefinition) instrument).toDerivative(NOW, withToday ? TS_FIXED_IBOR_EUR3M_WITH_TODAY : TS_FIXED_IBOR_EUR3M_WITHOUT_TODAY, curveNames);
  //      } else {
  //        final String[] curveNames = new String[] {CURVE_NAME_FWD_2 };
  //        ird = instrument.toDerivative(NOW, curveNames);
  //      }
  //      instruments[i] = ird;
  //      initGuess[i++] = initialGuess(instrument);
  //      fwd2Node[j++] = ird.accept(MATURITY_CALCULATOR);
  //    }
  //
  //    final LinkedHashMap<String, GeneratorYDCurve> curveGenerators = new LinkedHashMap<String, GeneratorYDCurve>();
  //    curveGenerators.put(CURVE_NAME_DSC_1, new GeneratorCurveYieldInterpolatedNode(dsc1Node, INTERPOLATOR));
  //    curveGenerators.put(CURVE_NAME_FWD_1, new GeneratorCurveYieldInterpolatedNode(fwd1Node, INTERPOLATOR));
  //    curveGenerators.put(CURVE_NAME_DSC_2, new GeneratorCurveYieldInterpolatedNode(dsc2Node, INTERPOLATOR));
  //    curveGenerators.put(CURVE_NAME_FWD_2, new GeneratorCurveYieldInterpolatedNode(fwd2Node, INTERPOLATOR));
  //    final YieldCurveBundle knowData = new YieldCurveBundle(FX_MATRIX, CCY_MAP);
  //    final MultipleYieldCurveFinderGeneratorDataBundle data = new MultipleYieldCurveFinderGeneratorDataBundle(instruments, knowData, curveGenerators);
  //    final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new MultipleYieldCurveFinderGeneratorFunction(calculator, data);
  //    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MultipleYieldCurveFinderGeneratorJacobian(new ParameterSensitivityCalculator(sensitivityCalculator), data);
  //    final double[] parameters = ROOT_FINDER.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initGuess)).getData();
  //    final YieldCurveBundle newCurves = data.getBuildingFunction().evaluate(new DoubleMatrix1D(parameters));
  //    final YieldCurveBundle bundle = knowData.copy();
  //    bundle.addAll(newCurves);
  //    final DoubleMatrix2D inverseJacobian;
  //    if (isPV) {
  //      final DoubleMatrix2D jacobianPV = jacobianCalculator.evaluate(new DoubleMatrix1D(parameters));
  //      final DoubleMatrix2D inverseJacobianPV = MATRIX_ALGEBRA.getInverse(jacobianPV);
  //      final double[][] inverseJacobianPVArray = inverseJacobianPV.getData();
  //      final int dim = inverseJacobianPV.getNumberOfColumns();
  //      final double[] pvmqs = new double[dim];
  //      // Implementation note: pvmqs: Present Value Sensitivity with respect to Market Quote.
  //      //   To be coherent, the pv calculator should provide a pv in the first currency of FX swaps and XCcy swaps.
  //      i = 0;
  //      for (final InstrumentDerivative ird : instruments) {
  //        pvmqs[i++] = ird.accept(PVBP_CALCULATOR, bundle);
  //      }
  //      final double[][] inverseJacobianArray = new double[dim][dim];
  //      for (int loopi = 0; loopi < dim; loopi++) {
  //        for (int loopj = 0; loopj < dim; loopj++) {
  //          inverseJacobianArray[loopi][loopj] = -inverseJacobianPVArray[loopi][loopj] * pvmqs[loopj];
  //        }
  //      }
  //      inverseJacobian = new DoubleMatrix2D(inverseJacobianArray);
  //    } else {
  //      final DoubleMatrix2D jacobian = jacobianCalculator.evaluate(new DoubleMatrix1D(parameters));
  //      inverseJacobian = MATRIX_ALGEBRA.getInverse(jacobian);
  //    }
  //    return new ObjectsPair<YieldCurveBundle, DoubleMatrix2D>(bundle, inverseJacobian);
  //  }
  //
  //  @SuppressWarnings({"unchecked"})
  //  /**
  //   * Build the curves and returns a pair containing
  //   * 1) the yield curve bundle (4 curves) and
  //   * 2) the inverse Jacobian (derivative of the curve parameters with respect to the market quotes) for the full bundle.
  //   * The curves are build in two blocks: first USD and then EUR.
  //   */
  //  public static Pair<YieldCurveBundle, DoubleMatrix2D> makeCurves2Blocks(final List<InstrumentDefinition<?>> dsc1Definitions, final List<InstrumentDefinition<?>> fwd1Definitions,
  //      final List<InstrumentDefinition<?>> dsc2Definitions, final List<InstrumentDefinition<?>> fwd2Definitions, final InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> calculator,
  //      final InstrumentDerivativeVisitorAdapter<YieldCurveBundle, InterestRateCurveSensitivity> sensitivityCalculator, final boolean withToday, final boolean isPV) {
  //
  //    // First block
  //    final int nbDsc1 = dsc1Definitions.size();
  //    final int nbFwd1 = fwd1Definitions.size();
  //    final InstrumentDerivative[] instruments1 = new InstrumentDerivative[nbDsc1 + nbFwd1];
  //    final double[] dsc1Node = new double[nbDsc1];
  //    final double[] fwd1Node = new double[nbFwd1];
  //    final double[] initGuess1 = new double[nbDsc1 + nbFwd1];
  //    int i1 = 0;
  //    for (final InstrumentDefinition<?> instrument : dsc1Definitions) {
  //      InstrumentDerivative ird;
  //      if (instrument instanceof SwapFixedONDefinition) {
  //        final String[] curveNames = new String[] {CURVE_NAME_DSC_1, CURVE_NAME_DSC_1 };
  //        ird = ((SwapFixedONDefinition) instrument).toDerivative(NOW, withToday ? TS_FIXED_OIS_USD_WITH_TODAY : TS_FIXED_OIS_USD_WITHOUT_TODAY, curveNames);
  //      } else {
  //        final String[] curveNames = new String[] {CURVE_NAME_DSC_1 };
  //        ird = instrument.toDerivative(NOW, curveNames);
  //      }
  //      instruments1[i1] = ird;
  //      initGuess1[i1] = initialGuess(instrument);
  //      dsc1Node[i1++] = ird.accept(MATURITY_CALCULATOR);
  //    }
  //    int j1 = 0;
  //    for (final InstrumentDefinition<?> instrument : fwd1Definitions) {
  //      InstrumentDerivative ird;
  //      if (instrument instanceof SwapFixedIborDefinition) {
  //        final String[] curveNames = new String[] {CURVE_NAME_DSC_1, CURVE_NAME_FWD_1 };
  //        ird = ((SwapFixedIborDefinition) instrument).toDerivative(NOW, withToday ? TS_FIXED_IBOR_USD3M_WITH_TODAY : TS_FIXED_IBOR_USD3M_WITHOUT_TODAY, curveNames);
  //      } else {
  //        final String[] curveNames = new String[] {CURVE_NAME_FWD_1 };
  //        ird = instrument.toDerivative(NOW, curveNames);
  //      }
  //      instruments1[i1] = ird;
  //      initGuess1[i1++] = initialGuess(instrument);
  //      fwd1Node[j1++] = ird.accept(MATURITY_CALCULATOR);
  //    }
  //    final LinkedHashMap<String, GeneratorYDCurve> curveGenerators1 = new LinkedHashMap<String, GeneratorYDCurve>();
  //    curveGenerators1.put(CURVE_NAME_DSC_1, new GeneratorCurveYieldInterpolatedNode(dsc1Node, INTERPOLATOR));
  //    curveGenerators1.put(CURVE_NAME_FWD_1, new GeneratorCurveYieldInterpolatedNode(fwd1Node, INTERPOLATOR));
  //    final YieldCurveBundle knowData = new YieldCurveBundle(FX_MATRIX, CCY_MAP);
  //    final MultipleYieldCurveFinderGeneratorDataBundle data1 = new MultipleYieldCurveFinderGeneratorDataBundle(instruments1, knowData, curveGenerators1);
  //    final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator1 = new MultipleYieldCurveFinderGeneratorFunction(calculator, data1);
  //    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator1 = new MultipleYieldCurveFinderGeneratorJacobian(new ParameterSensitivityCalculator(sensitivityCalculator), data1);
  //    final double[] parameters1 = ROOT_FINDER.getRoot(curveCalculator1, jacobianCalculator1, new DoubleMatrix1D(initGuess1)).getData();
  //    final YieldCurveBundle newCurves = data1.getBuildingFunction().evaluate(new DoubleMatrix1D(parameters1));
  //    final YieldCurveBundle bundle1 = knowData.copy();
  //    bundle1.addAll(newCurves);
  //
  //    // Second block
  //    final int nbDsc2 = dsc2Definitions.size();
  //    final int nbFwd2 = fwd2Definitions.size();
  //    final InstrumentDerivative[] instruments2 = new InstrumentDerivative[nbDsc2 + nbFwd2];
  //    final double[] dsc2Node = new double[nbDsc2];
  //    final double[] fwd2Node = new double[nbFwd2];
  //    final double[] initGuess2 = new double[nbDsc2 + nbFwd2];
  //    int i2 = 0;
  //    for (final InstrumentDefinition<?> instrument : dsc2Definitions) {
  //      InstrumentDerivative ird;
  //      if (instrument instanceof SwapXCcyIborIborDefinition) {
  //        final String[] curveNames = new String[] {CURVE_NAME_DSC_2, CURVE_NAME_FWD_2, CURVE_NAME_DSC_1, CURVE_NAME_FWD_1 };
  //        ird = ((SwapXCcyIborIborDefinition) instrument).toDerivative(NOW, withToday ? TS_FIXED_IBOR_EURUSD3M_WITH_TODAY : TS_FIXED_IBOR_EURUSD3M_WITHOUT_TODAY, curveNames);
  //      } else {
  //        if (instrument instanceof CashDefinition) {
  //          final String[] curveNames = new String[] {CURVE_NAME_DSC_2 };
  //          ird = instrument.toDerivative(NOW, curveNames);
  //        } else {
  //          final String[] curveNames = new String[] {CURVE_NAME_DSC_2, CURVE_NAME_DSC_1 };
  //          ird = instrument.toDerivative(NOW, curveNames);
  //        }
  //      }
  //      instruments2[i2] = ird;
  //      initGuess2[i2] = initialGuess(instrument);
  //      dsc2Node[i2++] = ird.accept(MATURITY_CALCULATOR);
  //    }
  //    int j2 = 0;
  //    for (final InstrumentDefinition<?> instrument : fwd2Definitions) {
  //      InstrumentDerivative ird;
  //      if (instrument instanceof SwapFixedIborDefinition) {
  //        final String[] curveNames = new String[] {CURVE_NAME_DSC_2, CURVE_NAME_FWD_2 };
  //        ird = ((SwapFixedIborDefinition) instrument).toDerivative(NOW, withToday ? TS_FIXED_IBOR_EUR3M_WITH_TODAY : TS_FIXED_IBOR_EUR3M_WITHOUT_TODAY, curveNames);
  //      } else {
  //        final String[] curveNames = new String[] {CURVE_NAME_FWD_2 };
  //        ird = instrument.toDerivative(NOW, curveNames);
  //      }
  //      instruments2[i2] = ird;
  //      initGuess2[i2++] = initialGuess(instrument);
  //      fwd2Node[j2++] = ird.accept(MATURITY_CALCULATOR);
  //    }
  //
  //    final LinkedHashMap<String, GeneratorYDCurve> curveGenerators2 = new LinkedHashMap<String, GeneratorYDCurve>();
  //    curveGenerators2.put(CURVE_NAME_DSC_2, new GeneratorCurveYieldInterpolatedNode(dsc2Node, INTERPOLATOR));
  //    curveGenerators2.put(CURVE_NAME_FWD_2, new GeneratorCurveYieldInterpolatedNode(fwd2Node, INTERPOLATOR));
  //    final MultipleYieldCurveFinderGeneratorDataBundle data2 = new MultipleYieldCurveFinderGeneratorDataBundle(instruments2, bundle1, curveGenerators2);
  //    final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator2 = new MultipleYieldCurveFinderGeneratorFunction(calculator, data2);
  //    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator2 = new MultipleYieldCurveFinderGeneratorJacobian(new ParameterSensitivityCalculator(sensitivityCalculator), data2);
  //    final double[] parameters2 = ROOT_FINDER.getRoot(curveCalculator2, jacobianCalculator2, new DoubleMatrix1D(initGuess2)).getData();
  //    final YieldCurveBundle newCurves2 = data2.getBuildingFunction().evaluate(new DoubleMatrix1D(parameters2));
  //    final YieldCurveBundle bundle2 = bundle1.copy();
  //    bundle2.addAll(newCurves2);
  //
  //    final InstrumentDerivative[] instrumentsT = new InstrumentDerivative[nbDsc1 + nbFwd1 + nbDsc2 + nbFwd2];
  //    System.arraycopy(instruments1, 0, instrumentsT, 0, nbDsc1 + nbFwd1);
  //    System.arraycopy(instruments2, 0, instrumentsT, nbDsc1 + nbFwd1, nbDsc2 + nbFwd2);
  //    final LinkedHashMap<String, GeneratorYDCurve> curveGeneratorsT = new LinkedHashMap<String, GeneratorYDCurve>();
  //    curveGeneratorsT.put(CURVE_NAME_DSC_1, new GeneratorCurveYieldInterpolatedNode(dsc1Node, INTERPOLATOR));
  //    curveGeneratorsT.put(CURVE_NAME_FWD_1, new GeneratorCurveYieldInterpolatedNode(fwd1Node, INTERPOLATOR));
  //    curveGeneratorsT.put(CURVE_NAME_DSC_2, new GeneratorCurveYieldInterpolatedNode(dsc2Node, INTERPOLATOR));
  //    curveGeneratorsT.put(CURVE_NAME_FWD_2, new GeneratorCurveYieldInterpolatedNode(fwd2Node, INTERPOLATOR));
  //    final MultipleYieldCurveFinderGeneratorDataBundle dataT = new MultipleYieldCurveFinderGeneratorDataBundle(instrumentsT, knowData, curveGeneratorsT);
  //    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculatorT = new MultipleYieldCurveFinderGeneratorJacobian(new ParameterSensitivityCalculator(sensitivityCalculator), dataT);
  //    final double[] parametersT = new double[parameters1.length + parameters2.length];
  //    System.arraycopy(parameters1, 0, parametersT, 0, parameters1.length);
  //    System.arraycopy(parameters2, 0, parametersT, parameters1.length, parameters2.length);
  //    final DoubleMatrix2D inverseJacobian;
  //    if (isPV) {
  //      final DoubleMatrix2D jacobianPV = jacobianCalculatorT.evaluate(new DoubleMatrix1D(parametersT));
  //      final DoubleMatrix2D inverseJacobianPV = MATRIX_ALGEBRA.getInverse(jacobianPV);
  //      final double[][] inverseJacobianPVArray = inverseJacobianPV.getData();
  //      final int dim = inverseJacobianPV.getNumberOfColumns();
  //      final double[] pvmqs = new double[dim];
  //      // Implementation note: pvmqs: Present Value Sensitivity with respect to Market Quote.
  //      //   To be coherent, the pv calculator should provide a pv in the first currency of FX swaps and XCcy swaps.
  //      int i = 0;
  //      for (final InstrumentDerivative ird : instrumentsT) {
  //        pvmqs[i++] = ird.accept(PVBP_CALCULATOR, bundle2);
  //      }
  //      final double[][] inverseJacobianArray = new double[dim][dim];
  //      for (int loopi = 0; loopi < dim; loopi++) {
  //        for (int loopj = 0; loopj < dim; loopj++) {
  //          inverseJacobianArray[loopi][loopj] = -inverseJacobianPVArray[loopi][loopj] * pvmqs[loopj];
  //        }
  //      }
  //      inverseJacobian = new DoubleMatrix2D(inverseJacobianArray);
  //    } else {
  //      final DoubleMatrix2D jacobian = jacobianCalculatorT.evaluate(new DoubleMatrix1D(parametersT));
  //      inverseJacobian = MATRIX_ALGEBRA.getInverse(jacobian);
  //    }
  //    return new ObjectsPair<YieldCurveBundle, DoubleMatrix2D>(bundle2, inverseJacobian);
  //  }

}
