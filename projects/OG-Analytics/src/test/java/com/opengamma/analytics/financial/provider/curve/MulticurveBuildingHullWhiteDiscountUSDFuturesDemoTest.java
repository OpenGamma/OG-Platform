/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve;

import static org.testng.AssertJUnit.assertEquals;

import java.util.LinkedHashMap;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorInterestRateFutures;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFuturesDeliverable;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexONMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.ParSpreadMarketQuoteCurveSensitivityHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.ParSpreadMarketQuoteHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.PresentValueHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.curve.hullwhite.HullWhiteProviderDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
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
 * Two curves in USD; 
 * (1) Discounting/ON 
 * (2) 3M Libor Forward curve build with STIR futures and swap futures 
 * Curves are calibrated using simple discounting and Hull-White one-factor (HW parameters exogeneous).
 */
@Test(groups = TestGroup.UNIT)
public class MulticurveBuildingHullWhiteDiscountUSDFuturesDemoTest {

  /** Curve calibration date */
  private static final ZonedDateTime CALIBRATION_DATE = DateUtils.getUTCDate(2013, 4, 26);

  /** Index and curve names */
  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final Currency USD = Currency.USD;
  private static final FXMatrix FX_MATRIX = new FXMatrix(USD);
  private static final IndexON FEDFUND = IndexONMaster.getInstance().getIndex("FED FUND");
  private static final IborIndex USDLIBOR3M = IndexIborMaster.getInstance().getIndex("USDLIBOR3M");
  private static final String CURVE_NAME_DSC_USD = "USD Dsc";
  private static final String CURVE_NAME_FWD3_USD = "USD Fwd 3M";

  /** Instruments generators **/
  private static final double NOTIONAL = 1000000.0;
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  private static final ZonedDateTime EDM3_START_PERIOD = DateUtils.getUTCDate(2013, 6, 19);
  private static final InterestRateFutureSecurityDefinition EDM3_DEFINITION = InterestRateFutureSecurityDefinition
      .fromFixingPeriodStartDate(EDM3_START_PERIOD, USDLIBOR3M, NOTIONAL, 0.25, "EDM3", NYC);
  private static final ZonedDateTime EDU3_START_PERIOD = DateUtils.getUTCDate(2013, 9, 18);
  private static final InterestRateFutureSecurityDefinition EDU3_DEFINITION = InterestRateFutureSecurityDefinition
      .fromFixingPeriodStartDate(EDU3_START_PERIOD, USDLIBOR3M, NOTIONAL, 0.25, "EDU3", NYC);
  private static final ZonedDateTime EDZ3_START_PERIOD = DateUtils.getUTCDate(2013, 12, 18);
  private static final InterestRateFutureSecurityDefinition EDZ3_DEFINITION = InterestRateFutureSecurityDefinition
      .fromFixingPeriodStartDate(EDZ3_START_PERIOD, USDLIBOR3M, NOTIONAL, 0.25, "EDZ3", NYC);
  private static final ZonedDateTime EDH4_START_PERIOD = DateUtils.getUTCDate(2014, 3, 19);
  private static final InterestRateFutureSecurityDefinition EDH4_DEFINITION = InterestRateFutureSecurityDefinition
      .fromFixingPeriodStartDate(EDH4_START_PERIOD, USDLIBOR3M, NOTIONAL, 0.25, "EDH4", NYC);
  private static final ZonedDateTime EDM4_START_PERIOD = DateUtils.getUTCDate(2014, 6, 18);
  private static final InterestRateFutureSecurityDefinition EDM4_DEFINITION = InterestRateFutureSecurityDefinition
      .fromFixingPeriodStartDate(EDM4_START_PERIOD, USDLIBOR3M, NOTIONAL, 0.25, "EDM4", NYC);
  private static final ZonedDateTime EDU4_START_PERIOD = DateUtils.getUTCDate(2014, 9, 17);
  private static final InterestRateFutureSecurityDefinition EDU4_DEFINITION = InterestRateFutureSecurityDefinition
      .fromFixingPeriodStartDate(EDU4_START_PERIOD, USDLIBOR3M, NOTIONAL, 0.25, "EDM4", NYC);
  private static final Period CTPM3_TENOR = Period.ofYears(2);
  private static final double CTPM3_RATE = 0.0050;
  private static final SwapFuturesPriceDeliverableSecurityDefinition CTPM3_DEFINITION = SwapFuturesPriceDeliverableSecurityDefinition.from(EDM3_START_PERIOD, USD6MLIBOR3M, CTPM3_TENOR, NOTIONAL,
      CTPM3_RATE);
  private static final Period CFPM3_TENOR = Period.ofYears(5);
  private static final double CFPM3_RATE = 0.0100;
  private static final SwapFuturesPriceDeliverableSecurityDefinition CFPM3_DEFINITION = SwapFuturesPriceDeliverableSecurityDefinition.from(EDM3_START_PERIOD, USD6MLIBOR3M, CFPM3_TENOR, NOTIONAL,
      CFPM3_RATE);
  private static final Period CNPM3_TENOR = Period.ofYears(10);
  private static final double CNPM3_RATE = 0.0200;
  private static final SwapFuturesPriceDeliverableSecurityDefinition CNPM3_DEFINITION = SwapFuturesPriceDeliverableSecurityDefinition.from(EDM3_START_PERIOD, USD6MLIBOR3M, CNPM3_TENOR, NOTIONAL,
      CNPM3_RATE);
  private static final Period CBPM3_TENOR = Period.ofYears(30);
  private static final double CBPM3_RATE = 0.0275;
  private static final SwapFuturesPriceDeliverableSecurityDefinition CBPM3_DEFINITION = SwapFuturesPriceDeliverableSecurityDefinition.from(EDM3_START_PERIOD, USD6MLIBOR3M, CBPM3_TENOR, NOTIONAL,
      CBPM3_RATE);
  private static final GeneratorInterestRateFutures GENERATOR_EDM3 = new GeneratorInterestRateFutures("EDM3", EDM3_DEFINITION);
  private static final GeneratorInterestRateFutures GENERATOR_EDU3 = new GeneratorInterestRateFutures("EDU3", EDU3_DEFINITION);
  private static final GeneratorInterestRateFutures GENERATOR_EDZ3 = new GeneratorInterestRateFutures("EDZ3", EDZ3_DEFINITION);
  private static final GeneratorInterestRateFutures GENERATOR_EDH4 = new GeneratorInterestRateFutures("EDH4", EDH4_DEFINITION);
  private static final GeneratorInterestRateFutures GENERATOR_EDM4 = new GeneratorInterestRateFutures("EDM4", EDM4_DEFINITION);
  private static final GeneratorInterestRateFutures GENERATOR_EDU4 = new GeneratorInterestRateFutures("EDU4", EDU4_DEFINITION);
  private static final GeneratorSwapFuturesDeliverable GENERATOR_CTPM3 = new GeneratorSwapFuturesDeliverable("CTPM3", CTPM3_DEFINITION);
  private static final GeneratorSwapFuturesDeliverable GENERATOR_CFPM3 = new GeneratorSwapFuturesDeliverable("CFPM3", CFPM3_DEFINITION);
  private static final GeneratorSwapFuturesDeliverable GENERATOR_CNPM3 = new GeneratorSwapFuturesDeliverable("CNPM3", CNPM3_DEFINITION);
  private static final GeneratorSwapFuturesDeliverable GENERATOR_CBPM3 = new GeneratorSwapFuturesDeliverable("CBPM3", CBPM3_DEFINITION);
  private static final GeneratorDepositIbor GENERATOR_USDLIBOR3M = new GeneratorDepositIbor("GENERATOR_USDLIBOR3M", USDLIBOR3M, NYC);

  /** Market values for the dsc USD curve */
  private static final double[] DSC_USD_MARKET_QUOTES = new double[] {0.0022, 0.00127, 0.00125, 0.00126, 0.00126, 0.00125, 0.001315, 0.001615, 0.00243, 0.00393, 0.00594, 0.01586 };
  /** Generators for the dsc USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_USD_GENERATORS = CurveCalibrationConventionDataSets.generatorUsdOnOisFfs(1, 11, 0);
  /** Tenors for the dsc USD curve */
  private static final Period[] DSC_USD_TENOR = new Period[] {Period.ofDays(0),
    Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1),
    Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
  private static final GeneratorAttributeIR[] DSC_USD_ATTR = new GeneratorAttributeIR[DSC_USD_TENOR.length];
  static {
    for (int loopins = 0; loopins < DSC_USD_TENOR.length; loopins++) {
      DSC_USD_ATTR[loopins] = new GeneratorAttributeIR(DSC_USD_TENOR[loopins]);
    }
  }

  /** Market values for the Fwd LIBOR3M USD curve */
  private static final double[] FWD3_USD_MARKET_QUOTES =
      new double[] {0.0027560,
        0.99715, 0.99700, 0.99680, 0.99660, 0.99500,
        (100 + 7.0 / 32.0 + 3.0 / (32.0 * 4.0)) / 100.0, (100 + 17.0 / 32.0) / 100.0, (101 + 2.0 / 32.0) / 100.0, (98 + 21.0 / 32.0) / 100.0 };
  // Quoted in 32nd (by 1/4): 100-07 3/4, 100-17 +, 101-02, 98-21 };
  /** Generators for the Fwd 3M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_EUR_GENERATORS =
      new GeneratorInstrument<?>[] {GENERATOR_USDLIBOR3M,
        GENERATOR_EDM3, GENERATOR_EDU3, GENERATOR_EDZ3, GENERATOR_EDH4, GENERATOR_EDU4,
        GENERATOR_CTPM3, GENERATOR_CFPM3, GENERATOR_CNPM3, GENERATOR_CBPM3 };
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] FWD3_USD_TENOR = new Period[] {Period.ofMonths(0),
    Period.ofMonths(0), Period.ofMonths(0), Period.ofMonths(0), Period.ofMonths(0), Period.ofMonths(0),
    Period.ofMonths(0), Period.ofMonths(0), Period.ofMonths(0), Period.ofMonths(0) };
  private static final GeneratorAttribute[] FWD3_USD_ATTR = new GeneratorAttribute[FWD3_USD_TENOR.length];
  static {
    FWD3_USD_ATTR[0] = new GeneratorAttributeIR(FWD3_USD_TENOR[0], FWD3_USD_TENOR[0]);
    for (int loopins = 1; loopins < FWD3_USD_TENOR.length; loopins++) {
      FWD3_USD_ATTR[loopins] = new GeneratorAttribute();
    }
  }

  /** Hull-White one-factor (for futures)**/
  private static final double MEAN_REVERSION = 0.01;
  private static final double[] VOLATILITY = new double[] {0.01, 0.011, 0.012, 0.013, 0.014 };
  private static final double[] VOLATILITY_TIME = new double[] {0.5, 1.0, 2.0, 5.0 };
  private static final HullWhiteOneFactorPiecewiseConstantParameters MODEL_PARAMETERS = new HullWhiteOneFactorPiecewiseConstantParameters(MEAN_REVERSION, VOLATILITY, VOLATILITY_TIME);
  /** Units of curves */
  private static final int[] NB_UNITS = new int[] {2, 2 };
  private static final int NB_BLOCKS = NB_UNITS.length;
  private static final InstrumentDefinition<?>[][][][] DEFINITIONS_UNITS = new InstrumentDefinition<?>[NB_BLOCKS][][][];
  private static final GeneratorYDCurve[][][] GENERATORS_UNITS = new GeneratorYDCurve[NB_BLOCKS][][];
  private static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];

  private static final MulticurveProviderDiscount KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);
  private static final HullWhiteOneFactorProviderDiscount KNOWN_DATA_HW = new HullWhiteOneFactorProviderDiscount(KNOWN_DATA, MODEL_PARAMETERS, USD);

  private static final LinkedHashMap<String, Currency> DSC_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IborIndex[]> FWD_IBOR_MAP = new LinkedHashMap<>();

  /** Standard USD discounting curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_DSC_USD;
  /** Standard USD Forward 3M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_FWD3_USD;

  static {
    DEFINITIONS_DSC_USD = getDefinitions(DSC_USD_MARKET_QUOTES, DSC_USD_GENERATORS, DSC_USD_ATTR);
    DEFINITIONS_FWD3_USD = getDefinitions(FWD3_USD_MARKET_QUOTES, FWD3_EUR_GENERATORS, FWD3_USD_ATTR);
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      DEFINITIONS_UNITS[loopblock] = new InstrumentDefinition<?>[NB_UNITS[loopblock]][][];
      GENERATORS_UNITS[loopblock] = new GeneratorYDCurve[NB_UNITS[loopblock]][];
      NAMES_UNITS[loopblock] = new String[NB_UNITS[loopblock]][];
    }
    DEFINITIONS_UNITS[0][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD };
    DEFINITIONS_UNITS[0][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_USD };
    DEFINITIONS_UNITS[1][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD };
    DEFINITIONS_UNITS[1][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_USD };
    final GeneratorYDCurve genIntLin = CurveCalibrationConventionDataSets.generatorYDMatLin();
    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[0][1] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[1][0] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[1][1] = new GeneratorYDCurve[] {genIntLin };
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_USD };
    NAMES_UNITS[0][1] = new String[] {CURVE_NAME_FWD3_USD };
    NAMES_UNITS[1][0] = new String[] {CURVE_NAME_DSC_USD };
    NAMES_UNITS[1][1] = new String[] {CURVE_NAME_FWD3_USD };
    DSC_MAP.put(CURVE_NAME_DSC_USD, USD);
    FWD_ON_MAP.put(CURVE_NAME_DSC_USD, new IndexON[] {FEDFUND });
    FWD_IBOR_MAP.put(CURVE_NAME_FWD3_USD, new IborIndex[] {USDLIBOR3M });
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  private static InstrumentDefinition<?>[] getDefinitions(final double[] marketQuotes, final GeneratorInstrument[] generators, final GeneratorAttribute[] attribute) {
    final InstrumentDefinition<?>[] definitions = new InstrumentDefinition<?>[marketQuotes.length];
    for (int loopmv = 0; loopmv < marketQuotes.length; loopmv++) {
      definitions[loopmv] = generators[loopmv].generateInstrument(CALIBRATION_DATE, marketQuotes[loopmv], NOTIONAL, attribute[loopmv]);
    }
    return definitions;
  }

  /** Calculators used in curve calibration and testing */
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQDC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSDC = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();
  private static final PresentValueHullWhiteCalculator PVHWC = PresentValueHullWhiteCalculator.getInstance();
  private static final ParSpreadMarketQuoteHullWhiteCalculator PSMQHWC = ParSpreadMarketQuoteHullWhiteCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityHullWhiteCalculator PSMQCSHWC = ParSpreadMarketQuoteCurveSensitivityHullWhiteCalculator.getInstance();

  private static final MulticurveDiscountBuildingRepository CURVE_BUILDING_REPOSITORY_MULTICURVE =
      CurveCalibrationConventionDataSets.curveBuildingRepositoryMulticurve();
  private static final HullWhiteProviderDiscountBuildingRepository CURVE_BUILDING_REPOSITORY_HW =
      CurveCalibrationConventionDataSets.curveBuildingRepositoryHullWhite();

  private static final double TOLERANCE_CAL = 1.0E-10 * NOTIONAL; // 0.01 currency unit for 100m

  private static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> CURVE_BLOCK_MC;
  private static Pair<HullWhiteOneFactorProviderDiscount, CurveBuildingBlockBundle> CURVE_BLOCK_HW;

  @BeforeSuite
  static void initClass() {
    CURVE_BLOCK_MC = CurveCalibrationTestsUtils.makeCurvesFromDefinitionsMulticurve(
        CALIBRATION_DATE, DEFINITIONS_UNITS[0],
        GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQDC, PSMQCSDC, false,
        DSC_MAP, FWD_ON_MAP, FWD_IBOR_MAP,
        CURVE_BUILDING_REPOSITORY_MULTICURVE, TS_FIXED_OIS_USD_WITH_TODAY, TS_FIXED_OIS_USD_WITHOUT_TODAY,
        TS_EMPTY_ARRAY, TS_EMPTY_ARRAY); // Discounting

    CURVE_BLOCK_HW = CurveCalibrationTestsUtils.makeCurvesFromDefinitionsHullWhite(
        CALIBRATION_DATE, DEFINITIONS_UNITS[1],
        GENERATORS_UNITS[1], NAMES_UNITS[1], KNOWN_DATA_HW, PSMQHWC, PSMQCSHWC, false,
        DSC_MAP, FWD_ON_MAP, FWD_IBOR_MAP,
        CURVE_BUILDING_REPOSITORY_HW, TS_FIXED_OIS_USD_WITH_TODAY, TS_FIXED_OIS_USD_WITHOUT_TODAY,
        TS_EMPTY_ARRAY, TS_EMPTY_ARRAY); // Hull-White
  }

  @Test
  public void curveConstruction() {
    curveConstructionTestMc(DEFINITIONS_UNITS[0], CURVE_BLOCK_MC.getFirst(), false);
    curveConstructionTestHw(DEFINITIONS_UNITS[1], CURVE_BLOCK_HW.getFirst(), false);
  }

  private void curveConstructionTestHw(final InstrumentDefinition<?>[][][] definitions, final HullWhiteOneFactorProviderDiscount curves, final boolean withToday) {
    final int nbUnits = definitions.length;
    for (int loopunit = 0; loopunit < nbUnits; loopunit++) {
      final InstrumentDerivative[][] instruments = convert(definitions[loopunit], withToday);
      final double[][] pv = new double[instruments.length][];
      for (int loopcurve = 0; loopcurve < instruments.length; loopcurve++) {
        pv[loopcurve] = new double[instruments[loopcurve].length];
        for (int loopins = 0; loopins < instruments[loopcurve].length; loopins++) {
          pv[loopcurve][loopins] = curves.getMulticurveProvider().getFxRates().convert(instruments[loopcurve][loopins].accept(PVHWC, curves), USD).getAmount();
          assertEquals("Curve construction: unit " + loopunit + " - instrument " + loopins, 0, pv[loopcurve][loopins], TOLERANCE_CAL);
        }
      }
    }
  }

  private void curveConstructionTestMc(final InstrumentDefinition<?>[][][] definitions, final MulticurveProviderDiscount curves, final boolean withToday) {
    final int nbUnits = definitions.length;
    for (int loopunit = 0; loopunit < nbUnits; loopunit++) {
      final InstrumentDerivative[][] instruments = convert(definitions[loopunit], withToday);
      final double[][] pv = new double[instruments.length][];
      for (int loopcurve = 0; loopcurve < instruments.length; loopcurve++) {
        pv[loopcurve] = new double[instruments[loopcurve].length];
        for (int loopins = 0; loopins < instruments[loopcurve].length; loopins++) {
          pv[loopcurve][loopins] = curves.getMulticurveProvider().getFxRates().convert(instruments[loopcurve][loopins].accept(PVDC, curves), USD).getAmount();
          assertEquals("Curve construction: unit " + loopunit + " - instrument " + loopins, 0, pv[loopcurve][loopins], TOLERANCE_CAL);
        }
      }
    }
  }

  @Test(enabled = false)
  public void performance() {
    long startTime, endTime;
    final int nbTest = 100;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      CurveCalibrationTestsUtils.makeCurvesFromDefinitionsMulticurve(
          CALIBRATION_DATE, DEFINITIONS_UNITS[0],
          GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQDC, PSMQCSDC, false,
          DSC_MAP, FWD_ON_MAP, FWD_IBOR_MAP,
          CURVE_BUILDING_REPOSITORY_MULTICURVE, TS_FIXED_OIS_USD_WITH_TODAY, TS_FIXED_OIS_USD_WITHOUT_TODAY,
          TS_EMPTY_ARRAY, TS_EMPTY_ARRAY);
    }
    endTime = System.currentTimeMillis();
    System.out.println("MulticurveBuildingHullWhiteDiscountUSDFuturesDemoTest - Discounting:" + nbTest + " curve construction / 2 units: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 2 units Multicurve: 06-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 500 ms for 100 sets.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      CurveCalibrationTestsUtils.makeCurvesFromDefinitionsHullWhite(
          CALIBRATION_DATE, DEFINITIONS_UNITS[1],
          GENERATORS_UNITS[1], NAMES_UNITS[1], KNOWN_DATA_HW, PSMQHWC, PSMQCSHWC, false,
          DSC_MAP, FWD_ON_MAP, FWD_IBOR_MAP,
          CURVE_BUILDING_REPOSITORY_HW, TS_FIXED_OIS_USD_WITH_TODAY, TS_FIXED_OIS_USD_WITHOUT_TODAY,
          TS_EMPTY_ARRAY, TS_EMPTY_ARRAY);
    }
    endTime = System.currentTimeMillis();
    System.out.println("MulticurveBuildingHullWhiteDiscountUSDFuturesDemoTest - Hull-White:" + nbTest + " curve construction / 2 unit: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 2 units Hull-White: 06-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 1100 ms for 100 sets.

  }

  private static InstrumentDerivative[][] convert(final InstrumentDefinition<?>[][] definitions, final boolean withToday) {
    final InstrumentDerivative[][] instruments = new InstrumentDerivative[definitions.length][];
    for (int loopcurve = 0; loopcurve < definitions.length; loopcurve++) {
      instruments[loopcurve] = new InstrumentDerivative[definitions[loopcurve].length];
      int loopins = 0;
      for (final InstrumentDefinition<?> instrument : definitions[loopcurve]) {
        InstrumentDerivative ird;
        if (instrument instanceof SwapFixedONDefinition) {
          ird = ((SwapFixedONDefinition) instrument).toDerivative(CALIBRATION_DATE,
              CurveCalibrationTestsUtils.getTSSwapFixedON(withToday, TS_FIXED_OIS_USD_WITH_TODAY, TS_FIXED_OIS_USD_WITHOUT_TODAY));
        } else {
          if (instrument instanceof InterestRateFutureTransactionDefinition) {
            ird = ((InterestRateFutureTransactionDefinition) instrument).toDerivative(CALIBRATION_DATE, 0.0); // Trade date = today, reference price not used.
          } else {
            if (instrument instanceof SwapFuturesPriceDeliverableTransactionDefinition) {
              ird = ((SwapFuturesPriceDeliverableTransactionDefinition) instrument).toDerivative(CALIBRATION_DATE, 0.0); // Trade date = today, reference price not used.
            } else {
              ird = instrument.toDerivative(CALIBRATION_DATE);
            }
          }
        }
        instruments[loopcurve][loopins++] = ird;
      }
    }
    return instruments;
  }

  private static final ZonedDateTimeDoubleTimeSeries TS_EMPTY = ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC();
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2013, 4, 25),
    DateUtils.getUTCDate(2013, 4, 26) }, new double[] {0.0007, 0.0008 });
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2013, 4, 25) },
      new double[] {0.0007 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_USD_WITH_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITH_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_USD_WITHOUT_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITHOUT_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_EMPTY_ARRAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_EMPTY };

}
