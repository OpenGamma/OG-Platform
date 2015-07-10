/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.tutorial.datasets;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositON;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Curves calibration in EUR.
 * Black ATM swaption volatility.
 * All data as of 9-Aug-2013.
 */
public class AnalysisMarketDataEURJun13Sets {

  private static final Interpolator1D INTERPOLATOR_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();
  private static final double TOLERANCE_ROOT = 1.0E-10;
  private static final int STEP_MAX = 100;

  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final Currency EUR = Currency.EUR;
  private static final FXMatrix FX_MATRIX = new FXMatrix(EUR);

  private static final double NOTIONAL = 1.0;

  private static final GeneratorSwapFixedON GENERATOR_OIS_EUR = GeneratorSwapFixedONMaster.getInstance().getGenerator("EUR1YEONIA", TARGET);
  private static final IndexON INDEX_ON_EUR = GENERATOR_OIS_EUR.getIndex();
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_EUR = new GeneratorDepositON("EUR Deposit ON", EUR, TARGET, INDEX_ON_EUR.getDayCount());
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("EUR1YEURIBOR3M", TARGET);
  private static final IborIndex EURIBOR3M = EUR1YEURIBOR3M.getIborIndex();
  private static final GeneratorDepositIbor GENERATOR_EURIBOR3M = new GeneratorDepositIbor("GENERATOR_EURIBOR3M", EURIBOR3M, TARGET);

  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2013, 8, 9);

  private static final ZonedDateTimeDoubleTimeSeries TS_EMPTY = ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC();
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_EUR_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2013, 8, 7),
    DateUtils.getUTCDate(2013, 8, 8) }, new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_EUR_WITHOUT_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_EUR_WITHOUT_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_EUR3M_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2013, 8, 8) },
      new double[] {0.0022 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_USD3M_WITHOUT_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_EUR3M_WITHOUT_TODAY };

  private static final String CURVE_NAME_DSC_EUR = "EUR Discounting";
  private static final String CURVE_NAME_FWD3_EUR = "EUR LIBOR3M";

  /** Market values for the dsc EUR curve */
  private static final double[] DSC_EUR_MARKET_QUOTES = new double[] {0.0010, 0.0010, 0.0010, 0.0010, 0.0010, 0.0011, 0.0012, 0.0015, 0.0025, 0.0040, 0.00625, 0.0085, 0.0125, 0.0168,
    0.0190, 0.0212, 0.0225, 0.0225 }; //18
  /** Generators for the dsc EUR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_EUR_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_EUR, GENERATOR_DEPOSIT_ON_EUR, GENERATOR_OIS_EUR,
    GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR,
    GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR };
  /** Tenors for the dsc EUR curve */
  private static final Period[] DSC_EUR_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3),
    Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(7), Period.ofYears(10),
    Period.ofYears(12), Period.ofYears(15), Period.ofYears(20), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] DSC_EUR_ATTR = new GeneratorAttributeIR[DSC_EUR_TENOR.length];
  static {
    for (int loopins = 0; loopins < 2; loopins++) {
      DSC_EUR_ATTR[loopins] = new GeneratorAttributeIR(DSC_EUR_TENOR[loopins], Period.ofDays(0));
    }
    for (int loopins = 2; loopins < DSC_EUR_TENOR.length; loopins++) {
      DSC_EUR_ATTR[loopins] = new GeneratorAttributeIR(DSC_EUR_TENOR[loopins]);
    }
  }

  /** Market values for the Fwd 3M EUR curve */
  private static final double[] FWD3_EUR_MARKET_QUOTES = new double[] {0.00227, 0.0025, 0.0025, 0.0030, 0.0044, 0.0060, 0.0084, 0.0105, 0.0145, 0.0190, 0.0211, 0.0230, 0.0245, 0.0245 }; //13
  /** Generators for the Fwd 3M EUR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_EUR_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_EURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M,
    EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M };
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] FWD3_EUR_TENOR = new Period[] {Period.ofMonths(0), Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1), Period.ofYears(2),
    Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(7), Period.ofYears(10), Period.ofYears(12), Period.ofYears(15), Period.ofYears(20), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] FWD3_EUR_ATTR = new GeneratorAttributeIR[FWD3_EUR_TENOR.length];
  static {
    for (int loopins = 0; loopins < FWD3_EUR_TENOR.length; loopins++) {
      FWD3_EUR_ATTR[loopins] = new GeneratorAttributeIR(FWD3_EUR_TENOR[loopins]);
    }
  }

  /** Standard EUR discounting curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_DSC_EUR;
  /** Standard EUR Forward 3M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_FWD3_EUR;

  /** Units of curves */
  private static final int[] NB_UNITS = new int[] {2, 1 };
  private static final int NB_BLOCKS = NB_UNITS.length;
  private static final InstrumentDefinition<?>[][][][] DEFINITIONS_UNITS = new InstrumentDefinition<?>[NB_BLOCKS][][][];
  private static final GeneratorYDCurve[][][] GENERATORS_UNITS = new GeneratorYDCurve[NB_BLOCKS][][];
  private static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];
  private static final MulticurveProviderDiscount KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);
  private static final LinkedHashMap<String, Currency> DSC_MAP_2C = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP_2C = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IborIndex[]> FWD_IBOR_MAP_2C = new LinkedHashMap<>();
  private static final LinkedHashMap<String, Currency> DSC_MAP_1C = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP_1C = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IborIndex[]> FWD_IBOR_MAP_1C = new LinkedHashMap<>();

  static {
    DEFINITIONS_DSC_EUR = getDefinitions(DSC_EUR_MARKET_QUOTES, DSC_EUR_GENERATORS, DSC_EUR_ATTR);
    DEFINITIONS_FWD3_EUR = getDefinitions(FWD3_EUR_MARKET_QUOTES, FWD3_EUR_GENERATORS, FWD3_EUR_ATTR);
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      DEFINITIONS_UNITS[loopblock] = new InstrumentDefinition<?>[NB_UNITS[loopblock]][][];
      GENERATORS_UNITS[loopblock] = new GeneratorYDCurve[NB_UNITS[loopblock]][];
      NAMES_UNITS[loopblock] = new String[NB_UNITS[loopblock]][];
    }
    DEFINITIONS_UNITS[0][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_EUR };
    DEFINITIONS_UNITS[0][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_EUR };
    DEFINITIONS_UNITS[1][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_EUR };
    final GeneratorYDCurve genIntLin = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_LINEAR);
    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[0][1] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[1][0] = new GeneratorYDCurve[] {genIntLin };
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_EUR };
    NAMES_UNITS[0][1] = new String[] {CURVE_NAME_FWD3_EUR };
    NAMES_UNITS[1][0] = new String[] {CURVE_NAME_FWD3_EUR };
    DSC_MAP_2C.put(CURVE_NAME_DSC_EUR, EUR);
    FWD_ON_MAP_2C.put(CURVE_NAME_DSC_EUR, new IndexON[] {INDEX_ON_EUR });
    FWD_IBOR_MAP_2C.put(CURVE_NAME_FWD3_EUR, new IborIndex[] {EURIBOR3M });
    DSC_MAP_1C.put(CURVE_NAME_FWD3_EUR, EUR);
    FWD_ON_MAP_1C.put(CURVE_NAME_FWD3_EUR, new IndexON[] {INDEX_ON_EUR });
    FWD_IBOR_MAP_1C.put(CURVE_NAME_FWD3_EUR, new IborIndex[] {EURIBOR3M });
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  public static InstrumentDefinition<?>[] getDefinitions(final double[] marketQuotes, final GeneratorInstrument[] generators, final GeneratorAttribute[] attribute) {
    final InstrumentDefinition<?>[] definitions = new InstrumentDefinition<?>[marketQuotes.length];
    for (int loopmv = 0; loopmv < marketQuotes.length; loopmv++) {
      definitions[loopmv] = generators[loopmv].generateInstrument(NOW, marketQuotes[loopmv], NOTIONAL, attribute[loopmv]);
    }
    return definitions;
  }

  // Calculator
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSC = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();

  private static final MulticurveDiscountBuildingRepository CURVE_BUILDING_REPOSITORY = new MulticurveDiscountBuildingRepository(TOLERANCE_ROOT, TOLERANCE_ROOT, STEP_MAX);

  private static final List<Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle>> CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK = new ArrayList<>();
  static {
    CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.add(makeCurvesFromDefinitions(DEFINITIONS_UNITS[0],
        GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQC, PSMQCSC, false, DSC_MAP_2C, FWD_IBOR_MAP_2C, FWD_ON_MAP_2C));
    CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.add(makeCurvesFromDefinitions(DEFINITIONS_UNITS[1],
        GENERATORS_UNITS[1], NAMES_UNITS[1], KNOWN_DATA, PSMQC, PSMQCSC, false, DSC_MAP_1C, FWD_IBOR_MAP_1C, FWD_ON_MAP_1C));
  }

  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getSingleCurveEUR() {
    return CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(1);
  }

  /**
   * Create a provider with shifted curve (shift of one parameter/zero-coupon point)
   * @param shift The size of the shift.
   * @param nodeIndex The index of the node to shift.
   * @return
   */
  public static MulticurveProviderDiscount getSinglecurveEURShiftParameterPoint(final double shift, final int nodeIndex) {
    Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> multicurvePair0 = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(1);
    MulticurveProviderDiscount singlecurve = multicurvePair0.getFirst(); // Shift the forward curve
    final YieldAndDiscountCurve curve = singlecurve.getCurve(EURIBOR3M);
    ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
    final YieldCurve curveYield = (YieldCurve) curve;
    ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
    final InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
    final double[] yieldBumped = curveInt.getYDataAsPrimitive().clone();
    yieldBumped[nodeIndex] += shift;
    final YieldAndDiscountCurve fwdBumped = new YieldCurve(curveInt.getName(),
        new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumped, curveInt.getInterpolator(), true));
    return singlecurve.withForward(EURIBOR3M, fwdBumped).withDiscountFactor(EUR, fwdBumped).withForward(INDEX_ON_EUR, fwdBumped);
  }

  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getMulticurveEUR() {
    return CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0);
  }

  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getCurvesEURShift(final double shift) {
    double[] marketQuotesDsc = DSC_EUR_MARKET_QUOTES.clone();
    for (int loopdsc = 0; loopdsc < marketQuotesDsc.length; loopdsc++) {
      marketQuotesDsc[loopdsc] += shift;
    }
    double[] marketQuotesFwd = FWD3_EUR_MARKET_QUOTES.clone();
    for (int loopfwd = 0; loopfwd < marketQuotesFwd.length; loopfwd++) {
      marketQuotesFwd[loopfwd] += shift;
    }
    InstrumentDefinition<?>[] definitionDsc = getDefinitions(marketQuotesDsc, DSC_EUR_GENERATORS, DSC_EUR_ATTR);
    InstrumentDefinition<?>[] definitionFwd = getDefinitions(marketQuotesFwd, FWD3_EUR_GENERATORS, FWD3_EUR_ATTR);
    final InstrumentDefinition<?>[][][] definitionUnits = new InstrumentDefinition<?>[NB_UNITS[0]][][];
    definitionUnits[0] = new InstrumentDefinition<?>[][] {definitionDsc };
    definitionUnits[1] = new InstrumentDefinition<?>[][] {definitionFwd };
    Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> multicurve = makeCurvesFromDefinitions(definitionUnits, GENERATORS_UNITS[0], NAMES_UNITS[0],
        KNOWN_DATA, PSMQC, PSMQCSC, false, DSC_MAP_2C, FWD_IBOR_MAP_2C, FWD_ON_MAP_2C);
    return multicurve;
  }

  /**
   * Create a provider with shifted curve (shift of one market quote point)
   * @param shift The size of the shift.
   * @param nodeIndex The index of the node to shift.
   * @param dscShift Flag to indicate if the discounting curve (true) or the forward curve (false) should be shifted.
   * @return
   */
  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getCurvesEURShiftMarketPoint(final double shift,
      final int nodeIndex, final boolean dscShift) {
    double[] marketQuotesDsc = DSC_EUR_MARKET_QUOTES.clone();
    double[] marketQuotesFwd = FWD3_EUR_MARKET_QUOTES.clone();
    if (dscShift) {
      marketQuotesDsc[nodeIndex] += shift;
    } else {
      marketQuotesFwd[nodeIndex] += shift;
    }
    InstrumentDefinition<?>[] definitionDsc = getDefinitions(marketQuotesDsc, DSC_EUR_GENERATORS, DSC_EUR_ATTR);
    InstrumentDefinition<?>[] definitionFwd = getDefinitions(marketQuotesFwd, FWD3_EUR_GENERATORS, FWD3_EUR_ATTR);
    final InstrumentDefinition<?>[][][] definitionUnits = new InstrumentDefinition<?>[NB_UNITS[0]][][];
    definitionUnits[0] = new InstrumentDefinition<?>[][] {definitionDsc };
    definitionUnits[1] = new InstrumentDefinition<?>[][] {definitionFwd };
    Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> multicurve = makeCurvesFromDefinitions(definitionUnits, GENERATORS_UNITS[0], NAMES_UNITS[0],
        KNOWN_DATA, PSMQC, PSMQCSC, false, DSC_MAP_2C, FWD_IBOR_MAP_2C, FWD_ON_MAP_2C);
    return multicurve;
  }

  /**
   * Create a provider with shifted curve (shift of one parameter/zero-coupon point)
   * @param shift The size of the shift.
   * @param nodeIndex The index of the node to shift.
   * @param dscShift Flag to indicate if the discounting curve (true) or the forward curve (false) should be shifted.
   * @return
   */
  public static MulticurveProviderDiscount getMulticurvesEURShiftParameterPoint(final double shift,
      final int nodeIndex, final boolean dscShift) {
    Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> multicurvePair0 = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0);
    MulticurveProviderDiscount multicurve = multicurvePair0.getFirst();
    if (dscShift) { // Shift the discounting curve
      final YieldAndDiscountCurve curve = multicurve.getCurve(EUR);
      ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
      final YieldCurve curveYield = (YieldCurve) curve;
      ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
      final InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
      final double[] yieldBumped = curveInt.getYDataAsPrimitive().clone();
      yieldBumped[nodeIndex] += shift;
      final YieldAndDiscountCurve dscBumped = new YieldCurve(curveInt.getName(), new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumped, curveInt.getInterpolator(), true));
      return multicurve.withDiscountFactor(EUR, dscBumped).withForward(INDEX_ON_EUR, dscBumped);
    } // Shift the forward curve
    final YieldAndDiscountCurve curve = multicurve.getCurve(EURIBOR3M);
    ArgumentChecker.isTrue(curve instanceof YieldCurve, "Curve should be a YieldCurve");
    final YieldCurve curveYield = (YieldCurve) curve;
    ArgumentChecker.isTrue(curveYield.getCurve() instanceof InterpolatedDoublesCurve, "Yield curve should be based on InterpolatedDoublesCurve");
    final InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) curveYield.getCurve();
    final double[] yieldBumped = curveInt.getYDataAsPrimitive().clone();
    yieldBumped[nodeIndex] += shift;
    final YieldAndDiscountCurve fwdBumped = new YieldCurve(curveInt.getName(),
        new InterpolatedDoublesCurve(curveInt.getXDataAsPrimitive(), yieldBumped, curveInt.getInterpolator(), true));
    return multicurve.withForward(EURIBOR3M, fwdBumped);
  }

  public static int getCurveEURNumberNodeDiscounting() {
    return DSC_EUR_MARKET_QUOTES.length;
  }

  public static int getCurveEURNumberNodeForward() {
    return FWD3_EUR_MARKET_QUOTES.length;
  }

  @SuppressWarnings("unchecked")
  private static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitions(final InstrumentDefinition<?>[][][] definitions, final GeneratorYDCurve[][] curveGenerators,
      final String[][] curveNames, final MulticurveProviderDiscount knownData, final InstrumentDerivativeVisitor<ParameterProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<ParameterProviderInterface, MulticurveSensitivity> sensitivityCalculator, final boolean withToday,
      LinkedHashMap<String, Currency> dscMap, LinkedHashMap<String, IborIndex[]> iborMap, LinkedHashMap<String, IndexON[]> onMap) {
    final int nbUnits = curveGenerators.length;
    final MultiCurveBundle<GeneratorYDCurve>[] curveBundles = new MultiCurveBundle[nbUnits];
    for (int i = 0; i < nbUnits; i++) {
      final int nCurves = definitions[i].length;
      final SingleCurveBundle<GeneratorYDCurve>[] singleCurves = new SingleCurveBundle[nCurves];
      for (int j = 0; j < nCurves; j++) {
        final int nInstruments = definitions[i][j].length;
        final InstrumentDerivative[] derivatives = new InstrumentDerivative[nInstruments];
        final double[] initialGuess = new double[nInstruments];
        for (int k = 0; k < nInstruments; k++) {
          derivatives[k] = convert(definitions[i][j][k], i, withToday);
          initialGuess[k] = initialGuess(definitions[i][j][k]);
        }
        final GeneratorYDCurve generator = curveGenerators[i][j].finalGenerator(derivatives);
        singleCurves[j] = new SingleCurveBundle<>(curveNames[i][j], derivatives, initialGuess, generator);
      }
      curveBundles[i] = new MultiCurveBundle<>(singleCurves);
    }
    return CURVE_BUILDING_REPOSITORY.makeCurvesFromDerivatives(curveBundles, knownData, dscMap, iborMap, onMap, calculator,
        sensitivityCalculator);
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
    //      instruments[loopunit] = convert(definitions[loopunit], loopunit, withToday);
    //      for (int loopcurve = 0; loopcurve < curveGenerators[loopunit].length; loopcurve++) {
    //        generatorFinal[loopunit][loopcurve] = curveGenerators[loopunit][loopcurve].finalGenerator(instruments[loopunit][loopcurve]);
    //        final double[] guessCurve = generatorFinal[loopunit][loopcurve].initialGuess(initialGuess(definitions[loopunit][loopcurve]));
    //        System.arraycopy(guessCurve, 0, parametersGuess[loopunit], startCurve, instruments[loopunit][loopcurve].length);
    //        startCurve += instruments[loopunit][loopcurve].length;
    //      }
    //    }
    //    return CURVE_BUILDING_REPOSITORY.makeCurvesFromDerivatives(instruments, generatorFinal, curveNames, parametersGuess, knownData, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP, calculator,
    //        sensitivityCalculator);
  }

  //  private static InstrumentDerivative[][] convert(final InstrumentDefinition<?>[][] definitions, final int unit, final boolean withToday) {
  //    //    int nbDef = 0;
  //    //    for (final InstrumentDefinition<?>[] definition : definitions) {
  //    //      nbDef += definition.length;
  //    //    }
  //    final InstrumentDerivative[][] instruments = new InstrumentDerivative[definitions.length][];
  //    for (int loopcurve = 0; loopcurve < definitions.length; loopcurve++) {
  //      instruments[loopcurve] = new InstrumentDerivative[definitions[loopcurve].length];
  //      int loopins = 0;
  //      for (final InstrumentDefinition<?> instrument : definitions[loopcurve]) {
  //        InstrumentDerivative ird;
  //        if (instrument instanceof SwapFixedONDefinition) {
  //          ird = ((SwapFixedONDefinition) instrument).toDerivative(NOW, getTSSwapFixedON(unit));
  //        } else {
  //          if (instrument instanceof SwapFixedIborDefinition) {
  //            ird = ((SwapFixedIborDefinition) instrument).toDerivative(NOW, getTSSwapFixedIbor(unit));
  //          } else {
  //            ird = instrument.toDerivative(NOW);
  //          }
  //        }
  //        instruments[loopcurve][loopins++] = ird;
  //      }
  //    }
  //    return instruments;
  //  }

  private static InstrumentDerivative convert(final InstrumentDefinition<?> definition, final int unit, final boolean withToday) {
    InstrumentDerivative ird;
    if (definition instanceof SwapFixedONDefinition) {
      ird = ((SwapFixedONDefinition) definition).toDerivative(NOW, getTSSwapFixedON(unit));
    } else {
      if (definition instanceof SwapFixedIborDefinition) {
        ird = ((SwapFixedIborDefinition) definition).toDerivative(NOW, getTSSwapFixedIbor(unit));
      } else {
        ird = definition.toDerivative(NOW);
      }
    }
    return ird;
  }

  private static ZonedDateTimeDoubleTimeSeries[] getTSSwapFixedON(final Integer unit) {
    switch (unit) {
      case 0:
        return TS_FIXED_OIS_EUR_WITHOUT_TODAY;
      default:
        throw new IllegalArgumentException(unit.toString());
    }
  }

  private static ZonedDateTimeDoubleTimeSeries[] getTSSwapFixedIbor(final Integer unit) {
    switch (unit) {
      case 0:
        return TS_FIXED_IBOR_USD3M_WITHOUT_TODAY;
      case 1:
        return TS_FIXED_IBOR_USD3M_WITHOUT_TODAY;
      default:
        throw new IllegalArgumentException(unit.toString());
    }
  }

  //  private static double[] initialGuess(final InstrumentDefinition<?>[] definitions) {
  //    final double[] result = new double[definitions.length];
  //    int loopr = 0;
  //    for (final InstrumentDefinition<?> definition : definitions) {
  //      result[loopr++] = initialGuess(definition);
  //    }
  //    return result;
  //  }

  private static double initialGuess(final InstrumentDefinition<?> instrument) {
    if (instrument instanceof SwapFixedONDefinition) {
      return ((SwapFixedONDefinition) instrument).getFixedLeg().getNthPayment(0).getRate();
    }
    if (instrument instanceof SwapFixedIborDefinition) {
      return ((SwapFixedIborDefinition) instrument).getFixedLeg().getNthPayment(0).getRate();
    }
    if (instrument instanceof ForwardRateAgreementDefinition) {
      return ((ForwardRateAgreementDefinition) instrument).getRate();
    }
    if (instrument instanceof CashDefinition) {
      return ((CashDefinition) instrument).getRate();
    }
    return 0.01;
  }

  //  /**
  //   * The linear interpolator/ flat extrapolator.
  //   */
  //  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
  //      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  //  private static final GridInterpolator2D INTERPOLATOR_2D = new GridInterpolator2D(LINEAR_FLAT, LINEAR_FLAT);
  //
  //  /** Data date: 19-Jun-2013 **/
  //  private static final InterpolatedDoublesSurface BLACK_SURFACE_EXP_TEN = InterpolatedDoublesSurface.from(
  //      new double[] {0.25, 0.5, 1.0, 0.25, 0.5, 1.0, 0.25, 0.5, 1.0, 0.25, 0.5, 1.0 },
  //      new double[] {2, 2, 2, 5, 5, 5, 10, 10, 10, 30, 30, 30 },
  //      new double[] {0.685, 0.661, 0.631, 0.509, 0.479, 0.438, 0.351, 0.339, 0.325, 0.256, 0.251, 0.252 },
  //      INTERPOLATOR_2D);
  //  private static final BlackFlatSwaptionParameters BLACK_SWAPTION_USD3 = new BlackFlatSwaptionParameters(BLACK_SURFACE_EXP_TEN, EUR1YEURIBOR3M);
  //
  //  // Normal: 35.6, x, 50.6, 73.6, x5, x5, x10, 88.5, x10, x30, x30, 86.4
  //
  //  public static BlackFlatSwaptionParameters createBlackSwaptionUSD3() {
  //    return BLACK_SWAPTION_USD3;
  //  }

}
