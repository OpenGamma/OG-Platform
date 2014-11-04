/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve;

import static org.testng.AssertJUnit.assertEquals;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
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
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Build of curve in several blocks with relevant Jacobian matrices.
 */
@Test(groups = TestGroup.UNIT)
public class MulticurveBuildingDiscountingDiscountSimpleTest {

  private static final Interpolator1D INTERPOLATOR_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();
  private static final double TOLERANCE_ROOT = 1.0E-10;
  private static final int STEP_MAX = 100;

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final Currency USD = Currency.USD;
  private static final FXMatrix FX_MATRIX = new FXMatrix(USD);

  private static final double NOTIONAL = 1.0;

  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GeneratorSwapFixedONMaster.getInstance().getGenerator("USD1YFEDFUND", NYC);
  private static final IndexON INDEX_ON_USD = GENERATOR_OIS_USD.getIndex();
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_USD = new GeneratorDepositON("USD Deposit ON", USD, NYC, INDEX_ON_USD.getDayCount());
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  private static final IborIndex USDLIBOR3M = USD6MLIBOR3M.getIborIndex();
  private static final GeneratorDepositIbor GENERATOR_USDLIBOR3M = new GeneratorDepositIbor("GENERATOR_USDLIBOR3M", USDLIBOR3M, NYC);

  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2011, 9, 28);

  private static final ZonedDateTimeDoubleTimeSeries TS_EMPTY = ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC();
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_USD_WITH_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITH_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_USD_WITHOUT_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITHOUT_TODAY };

  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.0035, 0.0036 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27) },
      new double[] {0.0035 });

  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_USD3M_WITH_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_USD3M_WITH_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_USD3M_WITHOUT_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_USD3M_WITHOUT_TODAY };

  private static final String CURVE_NAME_DSC_USD = "USD Dsc";
  private static final String CURVE_NAME_FWD3_USD = "USD Fwd 3M";

  /** Market values for the dsc USD curve */
  private static final double[] DSC_USD_MARKET_QUOTES = new double[] {0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 };
  /** Generators for the dsc USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_USD_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD,
    GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD };
  /** Tenors for the dsc USD curve */
  private static final Period[] DSC_USD_TENOR = new Period[] {Period.ofDays(0), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3),
    Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1),
    Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
  private static final GeneratorAttributeIR[] DSC_USD_ATTR = new GeneratorAttributeIR[DSC_USD_TENOR.length];
  static {
    for (int loopins = 0; loopins < DSC_USD_TENOR.length; loopins++) {
      DSC_USD_ATTR[loopins] = new GeneratorAttributeIR(DSC_USD_TENOR[loopins]);
    }
  }

  /** Market values for the Fwd 3M USD curve */
  private static final double[] FWD3_USD_MARKET_QUOTES = new double[] {0.0420, 0.0420, 0.0420, 0.0430, 0.0470, 0.0540, 0.0570, 0.0600 };
  /** Generators for the Fwd 3M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_USD_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_USDLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M,
    USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M };
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] FWD3_USD_TENOR = new Period[] {Period.ofMonths(0), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2),
    Period.ofYears(3), Period.ofYears(5), Period.ofYears(7),
    Period.ofYears(10) };
  private static final GeneratorAttributeIR[] FWD3_USD_ATTR = new GeneratorAttributeIR[FWD3_USD_TENOR.length];
  static {
    for (int loopins = 0; loopins < FWD3_USD_TENOR.length; loopins++) {
      FWD3_USD_ATTR[loopins] = new GeneratorAttributeIR(FWD3_USD_TENOR[loopins]);
    }
  }

  /** Standard USD discounting curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_DSC_USD;
  /** Standard USD Forward 3M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_FWD3_USD;

  /** Units of curves */
  private static final int[] NB_UNITS = new int[] {2 };
  private static final int NB_BLOCKS = NB_UNITS.length;
  private static final InstrumentDefinition<?>[][][][] DEFINITIONS_UNITS = new InstrumentDefinition<?>[NB_BLOCKS][][][];
  private static final GeneratorYDCurve[][][] GENERATORS_UNITS = new GeneratorYDCurve[NB_BLOCKS][][];
  private static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];
  private static final MulticurveProviderDiscount KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);
  private static final LinkedHashMap<String, Currency> DSC_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IborIndex[]> FWD_IBOR_MAP = new LinkedHashMap<>();

  static {
    DEFINITIONS_DSC_USD = getDefinitions(DSC_USD_MARKET_QUOTES, DSC_USD_GENERATORS, DSC_USD_ATTR);
    DEFINITIONS_FWD3_USD = getDefinitions(FWD3_USD_MARKET_QUOTES, FWD3_USD_GENERATORS, FWD3_USD_ATTR);
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      DEFINITIONS_UNITS[loopblock] = new InstrumentDefinition<?>[NB_UNITS[loopblock]][][];
      GENERATORS_UNITS[loopblock] = new GeneratorYDCurve[NB_UNITS[loopblock]][];
      NAMES_UNITS[loopblock] = new String[NB_UNITS[loopblock]][];
    }
    DEFINITIONS_UNITS[0][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD };
    DEFINITIONS_UNITS[0][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_USD };
    final GeneratorYDCurve genIntLin = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_LINEAR);
    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[0][1] = new GeneratorYDCurve[] {genIntLin };
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_USD };
    NAMES_UNITS[0][1] = new String[] {CURVE_NAME_FWD3_USD };
    DSC_MAP.put(CURVE_NAME_DSC_USD, USD);
    FWD_ON_MAP.put(CURVE_NAME_DSC_USD, new IndexON[] {INDEX_ON_USD });
    FWD_IBOR_MAP.put(CURVE_NAME_FWD3_USD, new IborIndex[] {USDLIBOR3M });
  }

  @SuppressWarnings({"rawtypes", "unchecked" })
  public static InstrumentDefinition<?>[] getDefinitions(final double[] marketQuotes, final GeneratorInstrument[] generators, final GeneratorAttribute[] attribute) {
    final InstrumentDefinition<?>[] definitions = new InstrumentDefinition<?>[marketQuotes.length];
    for (int loopmv = 0; loopmv < marketQuotes.length; loopmv++) {
      definitions[loopmv] = generators[loopmv].generateInstrument(NOW, marketQuotes[loopmv], NOTIONAL, attribute[loopmv]);
    }
    return definitions;
  }

  private static List<Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle>> CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK = new ArrayList<>();

  // Calculator
  private static final PresentValueDiscountingCalculator PVC = PresentValueDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSC = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();

  private static final MulticurveDiscountBuildingRepository CURVE_BUILDING_REPOSITORY = new MulticurveDiscountBuildingRepository(TOLERANCE_ROOT, TOLERANCE_ROOT, STEP_MAX);

  private static final double TOLERANCE_CAL = 1.0E-9;

  @BeforeSuite
  static void initClass() {
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.add(makeCurvesFromDefinitions(DEFINITIONS_UNITS[loopblock], GENERATORS_UNITS[loopblock], NAMES_UNITS[loopblock], KNOWN_DATA, PSMQC, PSMQCSC, false));
    }
  }

  public List<Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle>> getCurvesWithBlock() {
    initClass();
    return CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK;
  }

  public MulticurveProviderDiscount getCurvesWithOnlyDiscount() {
    initClass();
    final MulticurveProviderDiscount curves = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0).getFirst().copy();
    final Map<IborIndex, YieldAndDiscountCurve> iborCurves = new LinkedHashMap<>();
    final MulticurveProviderDiscount curve = new MulticurveProviderDiscount(curves.getDiscountingCurves(), iborCurves, curves.getForwardONCurves(), curves.getFxRates());
    return curve;
  }

  public CurveBuildingBlockBundle getBundleWithOnlyDiscount() {
    initClass();
    final Map<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> bundle = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0).getSecond().getData();
    final Map<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> bundleWithoutFwd3M = new LinkedHashMap<>();
    final Set<String> keySet = bundle.keySet();
    for (final String name : keySet) {
      if (name.equals(CURVE_NAME_DSC_USD)) {
        bundleWithoutFwd3M.put(name, bundle.get(name));
      }
    }
    final LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> bundleToLinkedMap = new LinkedHashMap<>(bundleWithoutFwd3M);
    return new CurveBuildingBlockBundle(bundleToLinkedMap);
  }

  @Test
  public void curveConstructionGeneratorOtherBlocks() {
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      curveConstructionTest(DEFINITIONS_UNITS[loopblock], CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(loopblock).getFirst(), false, loopblock);
    }
  }

  private void curveConstructionTest(final InstrumentDefinition<?>[][][] definitions, final MulticurveProviderDiscount curves, final boolean withToday, final int block) {
    final int nbBlocks = definitions.length;
    for (int loopblock = 0; loopblock < nbBlocks; loopblock++) {
      final InstrumentDerivative[][] instruments = convert(definitions[loopblock], loopblock, withToday);
      final double[][] pv = new double[instruments.length][];
      for (int loopcurve = 0; loopcurve < instruments.length; loopcurve++) {
        pv[loopcurve] = new double[instruments[loopcurve].length];
        for (int loopins = 0; loopins < instruments[loopcurve].length; loopins++) {
          pv[loopcurve][loopins] = curves.getFxRates().convert(instruments[loopcurve][loopins].accept(PVC, curves), USD).getAmount();
          assertEquals("Curve construction: block " + block + ", unit " + loopblock + " - instrument " + loopins, 0, pv[loopcurve][loopins], TOLERANCE_CAL);
        }
      }
    }
  }

  @Test(enabled = true)
  public void blockBundleDscFiniteDifferenceTest() {
    final CurveBuildingBlockBundle blockBundles = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0).getSecond();
    final double[] DSC_USD_MARKET_QUOTES_BUMPED_PLUS = new double[] {0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 };
    final double[] DSC_USD_MARKET_QUOTES_BUMPED_MINUS = new double[] {0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 };
    final double bump = 10e-8;

    for (int k = 0; k < DSC_USD_MARKET_QUOTES_BUMPED_MINUS.length; k++) {
      DSC_USD_MARKET_QUOTES_BUMPED_PLUS[k] += bump;
      DSC_USD_MARKET_QUOTES_BUMPED_MINUS[k] -= bump;
      final List<Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle>> blockBundlesPlus = new ArrayList<>();
      final List<Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle>> blockBundlesMinus = new ArrayList<>();
      final InstrumentDefinition<?>[] DEFINITIONS_DSC_USD_PLUS = getDefinitions(DSC_USD_MARKET_QUOTES_BUMPED_PLUS, DSC_USD_GENERATORS, DSC_USD_ATTR);
      final InstrumentDefinition<?>[] DEFINITIONS_DSC_USD_MINUS = getDefinitions(DSC_USD_MARKET_QUOTES_BUMPED_MINUS, DSC_USD_GENERATORS, DSC_USD_ATTR);
      final InstrumentDefinition<?>[][][] DEFINITIONS_UNITS_PLUS = new InstrumentDefinition<?>[2][][];
      final InstrumentDefinition<?>[][][] DEFINITIONS_UNITS_MINUS = new InstrumentDefinition<?>[2][][];
      DEFINITIONS_UNITS_PLUS[1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_USD };
      DEFINITIONS_UNITS_MINUS[1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_USD };
      DEFINITIONS_UNITS_PLUS[0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD_PLUS };
      DEFINITIONS_UNITS_MINUS[0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD_MINUS };
      blockBundlesPlus.add(makeCurvesFromDefinitions(DEFINITIONS_UNITS_PLUS, GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQC, PSMQCSC, false));
      final Double[] parametersPlus = ((YieldCurve) blockBundlesPlus.get(0).getFirst().getCurve(CURVE_NAME_DSC_USD)).getCurve().getYData();

      blockBundlesMinus.add(makeCurvesFromDefinitions(DEFINITIONS_UNITS_MINUS, GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQC, PSMQCSC, false));
      final Double[] parametersMinus = ((YieldCurve) blockBundlesMinus.get(0).getFirst().getCurve(CURVE_NAME_DSC_USD)).getCurve().getYData();
      final Double[] parametersSensi = new Double[parametersMinus.length];
      DSC_USD_MARKET_QUOTES_BUMPED_PLUS[k] -= bump;
      DSC_USD_MARKET_QUOTES_BUMPED_MINUS[k] += bump;
      for (int j = 0; j < blockBundles.getBlock(CURVE_NAME_DSC_USD).getSecond().getData().length; j++) {
        parametersSensi[j] = (parametersPlus[j] - parametersMinus[j]) / (2 * bump);
        assertEquals("Curve construction: block " + CURVE_NAME_DSC_USD + ", column " + j + " - line " + k, blockBundles.getBlock(CURVE_NAME_DSC_USD).getSecond().getData()[j][k],
            parametersSensi[j], 10e-6);
      }
    }

  }

  @Test(enabled = true)
  public void blockBundleDFwd3MFiniteDifferenceTest() {
    final double[] DSC_USD_MARKET_QUOTES_BUMPED_PLUS = new double[] {0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 };
    final double[] DSC_USD_MARKET_QUOTES_BUMPED_MINUS = new double[] {0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 };
    final double[] FWD3_USD_MARKET_QUOTES_PLUS = new double[] {0.0420, 0.0420, 0.0420, 0.0430, 0.0470, 0.0540, 0.0570, 0.0600 };
    final double[] FWD3_USD_MARKET_QUOTES_MINUS = new double[] {0.0420, 0.0420, 0.0420, 0.0430, 0.0470, 0.0540, 0.0570, 0.0600 };
    final CurveBuildingBlockBundle blockBundles = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0).clone().getSecond();
    final double bump = 10e-8;

    for (int k = 0; k < DSC_USD_MARKET_QUOTES_BUMPED_MINUS.length; k++) {
      DSC_USD_MARKET_QUOTES_BUMPED_PLUS[k] += bump;
      DSC_USD_MARKET_QUOTES_BUMPED_MINUS[k] -= bump;
      final List<Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle>> blockBundlesPlus = new ArrayList<>();
      final List<Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle>> blockBundlesMinus = new ArrayList<>();
      final InstrumentDefinition<?>[] DEFINITIONS_DSC_USD_PLUS = getDefinitions(DSC_USD_MARKET_QUOTES_BUMPED_PLUS, DSC_USD_GENERATORS, DSC_USD_ATTR);
      final InstrumentDefinition<?>[] DEFINITIONS_DSC_USD_MINUS = getDefinitions(DSC_USD_MARKET_QUOTES_BUMPED_MINUS, DSC_USD_GENERATORS, DSC_USD_ATTR);
      final InstrumentDefinition<?>[][][] DEFINITIONS_UNITS_PLUS = new InstrumentDefinition<?>[2][][];
      final InstrumentDefinition<?>[][][] DEFINITIONS_UNITS_MINUS = new InstrumentDefinition<?>[2][][];
      DEFINITIONS_UNITS_PLUS[1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_USD };
      DEFINITIONS_UNITS_MINUS[1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_USD };
      DEFINITIONS_UNITS_PLUS[0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD_PLUS };
      DEFINITIONS_UNITS_MINUS[0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD_MINUS };
      blockBundlesPlus.add(makeCurvesFromDefinitions(DEFINITIONS_UNITS_PLUS, GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQC, PSMQCSC, false));
      final Double[] parametersPlus = ((YieldCurve) blockBundlesPlus.get(0).getFirst().getCurve(CURVE_NAME_FWD3_USD)).getCurve().getYData();
      blockBundlesMinus.add(makeCurvesFromDefinitions(DEFINITIONS_UNITS_MINUS, GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQC, PSMQCSC, false));
      final Double[] parametersMinus = ((YieldCurve) blockBundlesMinus.get(0).getFirst().getCurve(CURVE_NAME_FWD3_USD)).getCurve().getYData();
      final Double[] parametersSensi = new Double[parametersMinus.length];
      DSC_USD_MARKET_QUOTES_BUMPED_PLUS[k] -= bump;
      DSC_USD_MARKET_QUOTES_BUMPED_MINUS[k] += bump;
      for (int j = 0; j < blockBundles.getBlock(CURVE_NAME_FWD3_USD).getSecond().getData().length; j++) {
        parametersSensi[j] = (parametersPlus[j] - parametersMinus[j]) / (2 * bump);
        assertEquals("Curve construction: block " + CURVE_NAME_FWD3_USD + ", column " + j + " - line " + k, blockBundles.getBlock(CURVE_NAME_FWD3_USD).getSecond().getData()[j][k],
            parametersSensi[j], 10e-6);
      }
    }
    for (int k = 0; k < FWD3_USD_MARKET_QUOTES_PLUS.length; k++) {
      FWD3_USD_MARKET_QUOTES_PLUS[k] += bump;
      FWD3_USD_MARKET_QUOTES_MINUS[k] -= bump;
      final List<Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle>> blockBundlesPlus = new ArrayList<>();
      final List<Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle>> blockBundlesMinus = new ArrayList<>();
      final InstrumentDefinition<?>[] DEFINITIONS_FWD_USD_PLUS = getDefinitions(FWD3_USD_MARKET_QUOTES_PLUS, FWD3_USD_GENERATORS, FWD3_USD_ATTR);
      final InstrumentDefinition<?>[] DEFINITIONS_FWD_USD_MINUS = getDefinitions(FWD3_USD_MARKET_QUOTES_MINUS, FWD3_USD_GENERATORS, FWD3_USD_ATTR);
      final InstrumentDefinition<?>[][][] DEFINITIONS_UNITS_PLUS = new InstrumentDefinition<?>[2][][];
      final InstrumentDefinition<?>[][][] DEFINITIONS_UNITS_MINUS = new InstrumentDefinition<?>[2][][];
      DEFINITIONS_UNITS_PLUS[0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD };
      DEFINITIONS_UNITS_MINUS[0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD };
      DEFINITIONS_UNITS_PLUS[1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD_USD_PLUS };
      DEFINITIONS_UNITS_MINUS[1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD_USD_MINUS };
      blockBundlesPlus.add(makeCurvesFromDefinitions(DEFINITIONS_UNITS_PLUS, GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQC, PSMQCSC, false));
      final Double[] parametersPlus = ((YieldCurve) blockBundlesPlus.get(0).getFirst().getCurve(CURVE_NAME_FWD3_USD)).getCurve().getYData();
      blockBundlesMinus.add(makeCurvesFromDefinitions(DEFINITIONS_UNITS_MINUS, GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQC, PSMQCSC, false));
      final Double[] parametersMinus = ((YieldCurve) blockBundlesMinus.get(0).getFirst().getCurve(CURVE_NAME_FWD3_USD)).getCurve().getYData();
      final Double[] parametersSensi = new Double[parametersMinus.length];
      FWD3_USD_MARKET_QUOTES_PLUS[k] -= bump;
      FWD3_USD_MARKET_QUOTES_MINUS[k] += bump;
      for (int j = 0; j < blockBundles.getBlock(CURVE_NAME_FWD3_USD).getSecond().getData().length; j++) {
        parametersSensi[j] = (parametersPlus[j] - parametersMinus[j]) / (2 * bump);
        assertEquals("Curve construction: block " + CURVE_NAME_FWD3_USD + ", column " + j + " - line " + k, blockBundles.getBlock(CURVE_NAME_FWD3_USD).getSecond().getData()[j][k +
            DSC_USD_MARKET_QUOTES_BUMPED_MINUS.length],
            parametersSensi[j], 10e-6);
      }
    }
  }

  @Test(enabled = false)
  /**
   * Analyzes the shape of the forward curve.
   */
  public void forwardAnalysis() {
    final MulticurveProviderInterface marketDsc = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0).getFirst();
    final int jump = 1;
    final int startIndex = 0;
    final int nbDate = 2750;
    ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(NOW, USDLIBOR3M.getSpotLag() + startIndex * jump, NYC);
    final double[] rateDsc = new double[nbDate];
    final double[] startTime = new double[nbDate];
    try {
      final FileWriter writer = new FileWriter("fwd-dsc.csv");
      for (int loopdate = 0; loopdate < nbDate; loopdate++) {
        startTime[loopdate] = TimeCalculator.getTimeBetween(NOW, startDate);
        final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, USDLIBOR3M, NYC);
        final double endTime = TimeCalculator.getTimeBetween(NOW, endDate);
        final double accrualFactor = USDLIBOR3M.getDayCount().getDayCountFraction(startDate, endDate, NYC);
        rateDsc[loopdate] = marketDsc.getSimplyCompoundForwardRate(USDLIBOR3M, startTime[loopdate], endTime, accrualFactor);
        startDate = ScheduleCalculator.getAdjustedDate(startDate, jump, NYC);
        writer.append(0.0 + "," + startTime[loopdate] + "," + rateDsc[loopdate] + "\n");
      }
      writer.flush();
      writer.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unchecked")
  private static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitions(final InstrumentDefinition<?>[][][] definitions, final GeneratorYDCurve[][] curveGenerators,
      final String[][] curveNames, final MulticurveProviderDiscount knownData, final InstrumentDerivativeVisitor<ParameterProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<ParameterProviderInterface, MulticurveSensitivity> sensitivityCalculator, final boolean withToday) {
    final int nUnits = definitions.length;
    final MultiCurveBundle<GeneratorYDCurve>[] curveBundles = new MultiCurveBundle[nUnits];
    for (int i = 0; i < nUnits; i++) {
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
    return CURVE_BUILDING_REPOSITORY.makeCurvesFromDerivatives(curveBundles, knownData, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP, calculator,
        sensitivityCalculator);
  }

  private static InstrumentDerivative convert(final InstrumentDefinition<?> instrument, final int unit, final boolean withToday) {
    InstrumentDerivative ird;
    if (instrument instanceof SwapFixedONDefinition) {
      ird = ((SwapFixedONDefinition) instrument).toDerivative(NOW, getTSSwapFixedON(withToday, unit));
    } else {
      if (instrument instanceof SwapFixedIborDefinition) {
        ird = ((SwapFixedIborDefinition) instrument).toDerivative(NOW, getTSSwapFixedIbor(withToday, unit));
      } else {
        ird = instrument.toDerivative(NOW);
      }
    }
    return ird;
  }

  private static InstrumentDerivative[][] convert(final InstrumentDefinition<?>[][] definitions, final int unit, final boolean withToday) {
    final InstrumentDerivative[][] instruments = new InstrumentDerivative[definitions.length][];
    for (int loopcurve = 0; loopcurve < definitions.length; loopcurve++) {
      instruments[loopcurve] = new InstrumentDerivative[definitions[loopcurve].length];
      int loopins = 0;
      for (final InstrumentDefinition<?> instrument : definitions[loopcurve]) {
        InstrumentDerivative ird;
        if (instrument instanceof SwapFixedONDefinition) {
          ird = ((SwapFixedONDefinition) instrument).toDerivative(NOW, getTSSwapFixedON(withToday, unit));
        } else {
          if (instrument instanceof SwapFixedIborDefinition) {
            ird = ((SwapFixedIborDefinition) instrument).toDerivative(NOW, getTSSwapFixedIbor(withToday, unit));
          } else {
            ird = instrument.toDerivative(NOW);
          }
        }
        instruments[loopcurve][loopins++] = ird;
      }
    }
    return instruments;
  }

  private static ZonedDateTimeDoubleTimeSeries[] getTSSwapFixedON(final Boolean withToday, final Integer unit) {
    switch (unit) {
      case 0:
        return withToday ? TS_FIXED_OIS_USD_WITH_TODAY : TS_FIXED_OIS_USD_WITHOUT_TODAY;
      default:
        throw new IllegalArgumentException(unit.toString());
    }
  }

  private static ZonedDateTimeDoubleTimeSeries[] getTSSwapFixedIbor(final Boolean withToday, final Integer unit) {
    switch (unit) {
      case 0:
        return withToday ? TS_FIXED_IBOR_USD3M_WITH_TODAY : TS_FIXED_IBOR_USD3M_WITHOUT_TODAY;
      case 1:
        return withToday ? TS_FIXED_IBOR_USD3M_WITH_TODAY : TS_FIXED_IBOR_USD3M_WITHOUT_TODAY;
      default:
        throw new IllegalArgumentException(unit.toString());
    }
  }

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
}
