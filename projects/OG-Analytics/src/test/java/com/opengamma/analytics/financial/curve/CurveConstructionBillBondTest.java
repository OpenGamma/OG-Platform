/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;


/**
 * Build curves using bills and bonds. 
 * TODO: This is old code that has been commented. It should be removed at some point.
 */
@Test(groups = TestGroup.UNIT)
public class CurveConstructionBillBondTest {

  //  private static final Interpolator1D INTERPOLATOR_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
  //      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  //  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();
  //  private static final double TOLERANCE_ROOT = 1.0E-10;
  //  private static final int STEP_MAX = 100;
  //  private static final CurveBuildingFunction CURVE_BUILDING_FUNCTION = new CurveBuildingFunction(TOLERANCE_ROOT, TOLERANCE_ROOT, STEP_MAX);
  //  private static final Currency CCY_USD = Currency.USD;
  //  private static final FXMatrix FX_MATRIX = new FXMatrix(CCY_USD);
  //  private static final Calendar CALENDAR = new MondayToFridayCalendar("CAL");
  //  private static final int SPOT_LAG_OIS = 2;
  //  private static final DayCount DAY_COUNT_CASH = DayCounts.ACT_360;
  //  private static final double NOTIONAL = 1.0;
  //  private static final BusinessDayConvention BDC = BusinessDayConventions.MODIFIED_FOLLOWING;
  //  private static final IndexON INDEX_FED_FUND = new IndexON("Fed Fund", CCY_USD, DAY_COUNT_CASH, 1, CALENDAR);
  //  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_USD = new GeneratorDepositON("USD Deposit ON", CCY_USD, CALENDAR, DAY_COUNT_CASH);
  //  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = new GeneratorSwapFixedON("USD1YFEDFUND", INDEX_FED_FUND, Period.ofMonths(12), DAY_COUNT_CASH, BDC, true, SPOT_LAG_OIS, SPOT_LAG_OIS);
  //
  //  private static final String NAME_COUNTERPART = "US GOVT";
  //  private static final GeneratorDepositONCounterpart GENERATOR_DEPOSIT_ON_USGOVT = new GeneratorDepositONCounterpart("US GOVT Deposit ON", CCY_USD, CALENDAR, DAY_COUNT_CASH, NAME_COUNTERPART);
  //
  //  private static final YieldConvention YIELD_BILL_USGOVT = YieldConventionFactory.INSTANCE.getYieldConvention("INTEREST@MTY");
  //  private static final DayCount DAY_COUNT_BILL_USGOVT = DayCounts.ACT_360;
  //  private static final int SPOT_LAG_BILL = 1;
  //  private static final ZonedDateTime[] BILL_MATURITY = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 9, 28), DateUtils.getUTCDate(2012, 11, 30), DateUtils.getUTCDate(2013, 2, 28) };
  //  private static final int NB_BILL = BILL_MATURITY.length;
  //  private static final BillSecurityDefinition[] BILL_SECURITY = new BillSecurityDefinition[NB_BILL];
  //  private static final GeneratorBill[] GENERATOR_BILL = new GeneratorBill[NB_BILL];
  //  static {
  //    for (int loopbill = 0; loopbill < BILL_MATURITY.length; loopbill++) {
  //      BILL_SECURITY[loopbill] = new BillSecurityDefinition(CCY_USD, BILL_MATURITY[loopbill], NOTIONAL, SPOT_LAG_BILL, CALENDAR, YIELD_BILL_USGOVT, DAY_COUNT_BILL_USGOVT, NAME_COUNTERPART);
  //      GENERATOR_BILL[loopbill] = new GeneratorBill("GeneratorBill" + loopbill, BILL_SECURITY[loopbill]);
  //    }
  //  }
  //
  //  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 8, 22);
  //
  //  private static final ArrayZonedDateTimeDoubleTimeSeries TS_EMPTY = new ArrayZonedDateTimeDoubleTimeSeries();
  //  private static final ArrayZonedDateTimeDoubleTimeSeries TS_ON_USD_WITH_TODAY = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2012, 8, 21),
  //      DateUtils.getUTCDate(2012, 8, 22) }, new double[] {0.07, 0.08 });
  //  private static final ArrayZonedDateTimeDoubleTimeSeries TS_ON_USD_WITHOUT_TODAY = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2012, 8, 21) },
  //      new double[] {0.07 });
  //  @SuppressWarnings("rawtypes")
  //  private static final DoubleTimeSeries[] TS_FIXED_OIS_USD_WITH_TODAY = new DoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITH_TODAY };
  //  @SuppressWarnings("rawtypes")
  //  private static final DoubleTimeSeries[] TS_FIXED_OIS_USD_WITHOUT_TODAY = new DoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITHOUT_TODAY };
  //
  //  private static final String CURVE_NAME_DSC_USD = "USD Dsc";
  //  private static final String CURVE_NAME_GOVTUS_USD = "USD GOVT US";
  //  private static final HashMap<String, Currency> CCY_MAP = new HashMap<String, Currency>();
  //  static {
  //    CCY_MAP.put(CURVE_NAME_DSC_USD, CCY_USD);
  //    CCY_MAP.put(CURVE_NAME_GOVTUS_USD, CCY_USD);
  //  }
  //
  //  /** Market values for the dsc USD curve */
  //  public static final double[] DSC_USD_MARKET_QUOTES = new double[] {0.0010, 0.0010, 0.0015, 0.0008, 0.0010, 0.0010, 0.0010, 0.0010, 0.0020, 0.0030, 0.0040, 0.0050, 0.0130 };
  //  /** Generators for the dsc USD curve */
  //  public static final GeneratorInstrument[] DSC_USD_GENERATORS = new GeneratorInstrument[] {GENERATOR_DEPOSIT_ON_USD, GENERATOR_DEPOSIT_ON_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD,
  //    GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD };
  //  /** Tenors for the dsc USD curve */
  //  public static final Period[] DSC_USD_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
  //    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
  //
  //  /** Market values for the govt USD curve */
  //  public static final double[] GOVTUS_USD_MARKET_QUOTES = new double[] {0.0010, 0.0015, 0.0020, 0.0015 };
  //  /** Generators for the govt USD curve */
  //  public static final GeneratorInstrument[] GOVTUS_USD_GENERATORS = new GeneratorInstrument[] {GENERATOR_DEPOSIT_ON_USGOVT, GENERATOR_BILL[0], GENERATOR_BILL[1], GENERATOR_BILL[2] };
  //  /** Tenors for the govt USD curve */
  //  public static final Period[] GOVTUS_USD_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(0), Period.ofDays(0), Period.ofDays(0) };
  //
  //  /** Standard USD discounting curve instrument definitions */
  //  public static final InstrumentDefinition<?>[] DEFINITIONS_DSC_USD;
  //  /** Standard USD Forward 3M curve instrument definitions */
  //  public static final InstrumentDefinition<?>[] DEFINITIONS_GOVTUS_USD;
  //  /** Units of curves */
  //  public static final int[] NB_UNITS = new int[] {2 };
  //  public static final int NB_BLOCKS = NB_UNITS.length;
  //  public static final InstrumentDefinition<?>[][][][] DEFINITIONS_UNITS = new InstrumentDefinition<?>[NB_BLOCKS][][][];
  //  public static final GeneratorYDCurve[][][] GENERATORS_UNITS = new GeneratorYDCurve[NB_BLOCKS][][];
  //  public static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];
  //  public static final YieldCurveBundle KNOWN_DATA = new YieldCurveBundle(FX_MATRIX, CCY_MAP);
  //
  //  static {
  //    DEFINITIONS_DSC_USD = getDefinitions(DSC_USD_MARKET_QUOTES, DSC_USD_GENERATORS, DSC_USD_TENOR);
  //    DEFINITIONS_GOVTUS_USD = getDefinitions(GOVTUS_USD_MARKET_QUOTES, GOVTUS_USD_GENERATORS, GOVTUS_USD_TENOR);
  //    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
  //      DEFINITIONS_UNITS[loopblock] = new InstrumentDefinition<?>[NB_UNITS[loopblock]][][];
  //      GENERATORS_UNITS[loopblock] = new GeneratorYDCurve[NB_UNITS[loopblock]][];
  //      NAMES_UNITS[loopblock] = new String[NB_UNITS[loopblock]][];
  //    }
  //    DEFINITIONS_UNITS[0][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD };
  //    DEFINITIONS_UNITS[0][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_GOVTUS_USD };
  //    final GeneratorYDCurve genIntLin = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_LINEAR);
  //    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {genIntLin };
  //    GENERATORS_UNITS[0][1] = new GeneratorYDCurve[] {genIntLin };
  //    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_USD };
  //    NAMES_UNITS[0][1] = new String[] {CURVE_NAME_GOVTUS_USD };
  //  }
  //
  //  // Calculators
  //  private static final PresentValueMCACalculator PV_CALCULATOR = PresentValueMCACalculator.getInstance();
  //  private static final ParSpreadMarketQuoteCalculator PSMQ_CALCULATOR = ParSpreadMarketQuoteCalculator.getInstance();
  //  private static final ParSpreadMarketQuoteCurveSensitivityCalculator PSMQCS_CALCULATOR = ParSpreadMarketQuoteCurveSensitivityCalculator.getInstance();
  //
  //  private static final double TOLERANCE_PV = 1.0E-10;
  //
  //  private static List<Pair<YieldCurveBundle, CurveBuildingBlockBundle>> CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK = new ArrayList<Pair<YieldCurveBundle, CurveBuildingBlockBundle>>();
  //
  //  @BeforeSuite
  //  static void initClass() {
  //    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
  //      CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.add(makeCurvesFromDefinitions(DEFINITIONS_UNITS[loopblock], GENERATORS_UNITS[loopblock], NAMES_UNITS[loopblock], KNOWN_DATA, PSMQ_CALCULATOR,
  //          PSMQCS_CALCULATOR, false));
  //    }
  //  }
  //
  //  @Test
  //  public void curveConstructionGeneratorBlocks() {
  //    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
  //      curveConstructionTest(NAMES_UNITS[loopblock], DEFINITIONS_UNITS[loopblock], CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(loopblock).getFirst(), false);
  //    }
  //    int t = 0;
  //    t++;
  //  }
  //
  //  public void curveConstructionTest(final String[][] curveNames, final InstrumentDefinition<?>[][][] definitions, final YieldCurveBundle curves, final boolean withToday) {
  //    final int nbBlocks = definitions.length;
  //    for (int loopblock = 0; loopblock < nbBlocks; loopblock++) {
  //      final InstrumentDerivative[][] instruments = convert(curveNames, definitions[loopblock], loopblock, withToday);
  //      final double[][] pv = new double[instruments.length][];
  //      for (int loopcurve = 0; loopcurve < instruments.length; loopcurve++) {
  //        pv[loopcurve] = new double[instruments[loopcurve].length];
  //        for (int loopins = 0; loopins < instruments[loopcurve].length; loopins++) {
  //          pv[loopcurve][loopins] = curves.getFxRates().convert(instruments[loopcurve][loopins].accept(PV_CALCULATOR, curves), CCY_USD).getAmount();
  //          assertEquals("Curve construction: node block " + loopblock + " - instrument " + loopins, 0, pv[loopcurve][loopins], TOLERANCE_PV);
  //        }
  //      }
  //    }
  //  }
  //
  //  @Test(enabled = true)
  //  /**
  //   * Code used to graph the curves
  //   */
  //  public void analysis() {
  //    final int nbPoints = 210;
  //    final double endTime = 21.0;
  //    final double[] x = new double[nbPoints + 1];
  //    for (int looppt = 0; looppt <= nbPoints; looppt++) {
  //      x[looppt] = looppt * endTime / nbPoints;
  //    }
  //    final int nbAnalysis = NB_BLOCKS;
  //    final YieldCurveBundle[] bundle = new YieldCurveBundle[nbAnalysis];
  //    final double[][][] rate = new double[nbAnalysis][][];
  //    for (int loopblock = 0; loopblock < nbAnalysis; loopblock++) {
  //      bundle[loopblock] = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(loopblock).getFirst();
  //    }
  //    for (int loopbundle = 0; loopbundle < nbAnalysis; loopbundle++) {
  //      final Set<String> curveNames = bundle[loopbundle].getAllNames();
  //      final int nbCurve = curveNames.size();
  //      int loopc = 0;
  //      rate[loopbundle] = new double[nbCurve][nbPoints + 1];
  //      for (final String name : curveNames) {
  //        for (int looppt = 0; looppt <= nbPoints; looppt++) {
  //          rate[loopbundle][loopc][looppt] = bundle[loopbundle].getCurve(name).getInterestRate(x[looppt]);
  //        }
  //        loopc++;
  //      }
  //    }
  //    int t = 0;
  //    t++;
  //  }
  //
  //  public static InstrumentDefinition<?>[] getDefinitions(final double[] marketQuotes, final GeneratorInstrument[] generators, final Period[] tenors) {
  //    final InstrumentDefinition<?>[] definitions = new InstrumentDefinition<?>[marketQuotes.length];
  //    for (int loopmv = 0; loopmv < marketQuotes.length; loopmv++) {
  //      definitions[loopmv] = generators[loopmv].generateInstrument(REFERENCE_DATE, tenors[loopmv], marketQuotes[loopmv], NOTIONAL);
  //    }
  //    return definitions;
  //  }
  //
  //  private static Pair<YieldCurveBundle, CurveBuildingBlockBundle> makeCurvesFromDefinitions(final InstrumentDefinition<?>[][][] definitions, final GeneratorYDCurve[][] curveGenerators,
  //      final String[][] curveNames, final YieldCurveBundle knownData, final InstrumentDerivativeVisitor<YieldCurveBundle, Double> calculator,
  //      final InstrumentDerivativeVisitor<YieldCurveBundle, InterestRateCurveSensitivity> sensitivityCalculator, final boolean withToday) {
  //    final int nbUnits = curveGenerators.length;
  //    final double[][] parametersGuess = new double[nbUnits][];
  //    final GeneratorYDCurve[][] generatorFinal = new GeneratorYDCurve[nbUnits][];
  //    final InstrumentDerivative[][][] instruments = new InstrumentDerivative[nbUnits][][];
  //    for (int loopunit = 0; loopunit < nbUnits; loopunit++) {
  //      generatorFinal[loopunit] = new GeneratorYDCurve[curveGenerators[loopunit].length];
  //      int nbInsUnit = 0;
  //      for (int loopcurve = 0; loopcurve < curveGenerators[loopunit].length; loopcurve++) {
  //        nbInsUnit += definitions[loopunit][loopcurve].length;
  //      }
  //      parametersGuess[loopunit] = new double[nbInsUnit];
  //      int startCurve = 0; // First parameter index of the curve in the unit.
  //      instruments[loopunit] = convert(curveNames, definitions[loopunit], loopunit, withToday);
  //      for (int loopcurve = 0; loopcurve < curveGenerators[loopunit].length; loopcurve++) {
  //        generatorFinal[loopunit][loopcurve] = curveGenerators[loopunit][loopcurve].finalGenerator(instruments[loopunit][loopcurve]);
  //        final double[] guessCurve = generatorFinal[loopunit][loopcurve].initialGuess(rate(definitions[loopunit][loopcurve]));
  //        System.arraycopy(guessCurve, 0, parametersGuess[loopunit], startCurve, instruments[loopunit][loopcurve].length);
  //        startCurve += instruments[loopunit][loopcurve].length;
  //      }
  //    }
  //    return CURVE_BUILDING_FUNCTION.makeCurvesFromDerivatives(instruments, generatorFinal, curveNames, parametersGuess, knownData, calculator, sensitivityCalculator);
  //  }
  //
  //  @SuppressWarnings("unchecked")
  //  private static InstrumentDerivative[][] convert(final String[][] curveNames, final InstrumentDefinition<?>[][] definitions, final int unit, final boolean withToday) {
  //    int nbDef = 0;
  //    for (final InstrumentDefinition<?>[] definition : definitions) {
  //      nbDef += definition.length;
  //    }
  //    final InstrumentDerivative[][] instruments = new InstrumentDerivative[definitions.length][];
  //    for (int loopcurve = 0; loopcurve < definitions.length; loopcurve++) {
  //      instruments[loopcurve] = new InstrumentDerivative[definitions[loopcurve].length];
  //      int loopins = 0;
  //      for (final InstrumentDefinition<?> instrument : definitions[loopcurve]) {
  //        InstrumentDerivative ird;
  //        if (instrument instanceof SwapFixedONDefinition) {
  //          final String[] names = getCurvesNameSwapFixedON(curveNames);
  //          ird = ((SwapFixedONDefinition) instrument).toDerivative(REFERENCE_DATE, getTSSwapFixedON(withToday, unit), names);
  //        } else {
  //          if (instrument instanceof BillTransactionDefinition) {
  //            final String[] names = getCurvesNameBill(curveNames);
  //            ird = ((BillTransactionDefinition) instrument).toDerivative(REFERENCE_DATE, names);
  //          } else {
  //            final String[] names;
  //            // Cash
  //            names = getCurvesNameCash(curveNames, unit);
  //            ird = instrument.toDerivative(REFERENCE_DATE, names);
  //          }
  //        }
  //        instruments[loopcurve][loopins++] = ird;
  //      }
  //    }
  //    return instruments;
  //  }
  //
  //  private static double rate(final InstrumentDefinition<?> instrument) {
  //    if (instrument instanceof SwapFixedONDefinition) {
  //      return ((SwapFixedONDefinition) instrument).getFixedLeg().getNthPayment(0).getRate();
  //    }
  //    if (instrument instanceof CashDefinition) {
  //      return ((CashDefinition) instrument).getRate();
  //    }
  //    if (instrument instanceof DepositCounterpartDefinition) {
  //      return ((DepositCounterpartDefinition) instrument).getRate();
  //    }
  //    return 0.01;
  //  }
  //
  //  private static double[] rate(final InstrumentDefinition<?>[] definitions) {
  //    final double[] result = new double[definitions.length];
  //    int loopr = 0;
  //    for (final InstrumentDefinition<?> definition : definitions) {
  //      result[loopr++] = rate(definition);
  //    }
  //    return result;
  //  }
  //
  //  private static String[] getCurvesNameSwapFixedON(final String[][] curveNames) {
  //    return new String[] {curveNames[0][0], curveNames[0][0] };
  //  }
  //
  //  private static String[] getCurvesNameBill(final String[][] curveNames) {
  //    return new String[] {curveNames[0][0], curveNames[1][0] };
  //  }
  //
  //  private static String[] getCurvesNameCash(final String[][] curveNames, final Integer unit) {
  //    switch (unit) {
  //      case 0:
  //        return new String[] {curveNames[0][0] };
  //      case 1:
  //        return new String[] {curveNames[1][0] };
  //      default:
  //        throw new IllegalArgumentException(unit.toString());
  //    }
  //  }
  //
  //  @SuppressWarnings("rawtypes")
  //  private static DoubleTimeSeries[] getTSSwapFixedON(final Boolean withToday, final Integer unit) {
  //    switch (unit) {
  //      case 0:
  //        return withToday ? TS_FIXED_OIS_USD_WITH_TODAY : TS_FIXED_OIS_USD_WITHOUT_TODAY;
  //      default:
  //        throw new IllegalArgumentException(unit.toString());
  //    }
  //  }
}
