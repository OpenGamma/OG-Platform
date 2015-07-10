/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.datasets;

import java.util.LinkedHashMap;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.datasets.CalendarTarget;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.ParSpreadMarketQuoteCurveSensitivityHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.ParSpreadMarketQuoteHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationConventionDataSets;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationTestsUtils;
import com.opengamma.analytics.financial.provider.curve.hullwhite.HullWhiteProviderDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.HullWhiteDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Curves calibration in EUR: 
 * 0) ONDSC-OIS/EURIBOR3M-FUTIRS
 * Data stored in snapshots for comparison with platform. Demo EUR ON-OIS EURIBOR3M-IRFIRS EURIBOR6M-FRAIRS
 * Calibration can be done with Discounting (no convexity adjustment) or Hull-White one-factor model.
 */
public class StandardDataSetsMulticurveFuturesEUR {

  private static final Interpolator1D INTERPOLATOR_LINEAR = 
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, 
          Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();
  private static final double TOLERANCE_ROOT = 1.0E-10;
  private static final int STEP_MAX = 100;

  private static final Calendar TARGET = new CalendarTarget("TARGET");
  private static final Currency EUR = Currency.EUR;
  private static final FXMatrix FX_MATRIX = new FXMatrix(EUR);

  private static final double NOTIONAL = 1.0;

  private static final GeneratorSwapFixedONMaster GENERATOR_OIS_MASTER = GeneratorSwapFixedONMaster.getInstance();
  private static final GeneratorSwapFixedIborMaster GENERATOR_IRS_MASTER = GeneratorSwapFixedIborMaster.getInstance();

  private static final GeneratorSwapFixedON GENERATOR_OIS_EUR = GENERATOR_OIS_MASTER.getGenerator("EUR1YEONIA", TARGET);
  private static final IndexON EUREONIA = GENERATOR_OIS_EUR.getIndex();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = GENERATOR_IRS_MASTER.getGenerator("EUR1YEURIBOR3M", TARGET);
  private static final IborIndex EUREURIBOR3M = EUR1YEURIBOR3M.getIborIndex();
  private static final IborIndex EUREURIBOR6M = IndexIborMaster.getInstance().getIndex("EURIBOR6M");

  private static final ZonedDateTimeDoubleTimeSeries TS_EMPTY = ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC();
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_EUR_WITH_TODAY = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27), DateUtils.getUTCDate(2011, 9, 28) }, 
          new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_EUR_WITHOUT_TODAY = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),  DateUtils.getUTCDate(2011, 9, 28) }, 
          new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_EUR_WITH_TODAY = 
      new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_EUR_WITH_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_EUR_WITHOUT_TODAY = 
      new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_EUR_WITHOUT_TODAY };

  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_EUR3M_WITH_TODAY = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27), DateUtils.getUTCDate(2011, 9, 28) }, 
          new double[] {0.0035, 0.0036 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_EUR3M_WITHOUT_TO = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27) },
      new double[] {0.0035 });

  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_EUR3M_WITH_TODAY = 
      new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_EUR3M_WITH_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_EUR3M_WITHOUT_TODAY = 
      new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_EUR3M_WITHOUT_TO };

  private static final String CURVE_NAME_DSC_EUR = "EUR-OIS";
  private static final String CURVE_NAME_FWD3_EUR = "EUR-FUTE3M-IRSE3M";
  private static final String CURVE_NAME_FWD6_EUR = "EUR-FRAE6M-IRSE6M";

  /** Data for 2014-02-18 **/
  /** Market values for the dsc EUR curve */
  private static final double[] DSC_1_EUR_MARKET_QUOTES = new double[] {0.00015, 0.00015,
    -0.000045, -0.00011, -0.00012, -0.00015, -0.000217,
    -0.00027, -0.00018, 0.0002, 0.00086, 0.0018,
    0.00419, 0.00792, 0.009915, 0.01213, 0.01436};
  /** Generators for the dsc EUR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_1_EUR_GENERATORS = 
      CurveCalibrationConventionDataSets.generatorEurOnOis(2, 15);
  /** Tenors for the dsc EUR curve */
  private static final Period[] DSC_1_EUR_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1),
    Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(7), Period.ofYears(10), Period.ofYears(12), Period.ofYears(15), Period.ofYears(20) };
  private static final GeneratorAttributeIR[] DSC_1_EUR_ATTR = new GeneratorAttributeIR[DSC_1_EUR_TENOR.length];
  static {
    for (int loopins = 0; loopins < 2; loopins++) {
      DSC_1_EUR_ATTR[loopins] = new GeneratorAttributeIR(DSC_1_EUR_TENOR[loopins], Period.ofDays(0));
    }
    for (int loopins = 2; loopins < DSC_1_EUR_TENOR.length; loopins++) {
      DSC_1_EUR_ATTR[loopins] = new GeneratorAttributeIR(DSC_1_EUR_TENOR[loopins]);
    }
  }

  /** Market values for the Fwd 3M EUR curve */
  private static final double[] FWD3_1_EUR_MARKET_QUOTES = new double[] {0.00159,
    0.998675, 0.998825, 0.998975, 0.998975, 0.998825, 0.998625,
    0.0013475, 0.0018375, 0.002643, 0.003665, 0.004845, 
    0.00614, 0.007475, 0.008755, 0.009935, 0.0119065, 
    0.01407, 0.016141, 0.01765};
  /** Tenors for the Fwd 3M EUR curve */
  private static final Period[] FWD3_1_EUR_TENOR = new Period[] {Period.ofMonths(0),
    Period.ofMonths(0), Period.ofMonths(0), Period.ofMonths(0), Period.ofMonths(0), Period.ofMonths(0), Period.ofMonths(0),
    Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(6), 
    Period.ofYears(7), Period.ofYears(8), Period.ofYears(9), Period.ofYears(10), Period.ofYears(12), 
    Period.ofYears(15), Period.ofYears(20), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] FWD3_1_USD_ATTR = new GeneratorAttributeIR[FWD3_1_EUR_TENOR.length];
  static {
    for (int loopins = 0; loopins < FWD3_1_EUR_TENOR.length; loopins++) {
      FWD3_1_USD_ATTR[loopins] = new GeneratorAttributeIR(FWD3_1_EUR_TENOR[loopins]);
    }
  }

  /** Market values for the Fwd 6M EUR curve */
  private static final double[] FWD6_1_EUR_MARKET_QUOTES = new double[] {0.00259,
    0.00218, 0.002125,
    0.002545, 0.003085, 0.00392, 0.00497, 0.0074475,
    0.01116, 0.013065, 0.01508, 0.016976, 0.01832};
  /** Generators for the Fwd 3M EUR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD6_1_EUR_GENERATORS = 
      CurveCalibrationConventionDataSets.generatorEurIbor6Fra6Irs6(1, 2, 10);
  /** Tenors for the Fwd 3M EUR curve */
  private static final Period[] FWD6_1_EUR_TENOR = new Period[] {Period.ofMonths(0),
    Period.ofMonths(9), Period.ofMonths(12),
    Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(7), 
    Period.ofYears(10), Period.ofYears(12), Period.ofYears(15), Period.ofYears(20), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] FWD6_1_USD_ATTR = new GeneratorAttributeIR[FWD6_1_EUR_TENOR.length];
  static {
    for (int loopins = 0; loopins < FWD6_1_EUR_TENOR.length; loopins++) {
      FWD6_1_USD_ATTR[loopins] = new GeneratorAttributeIR(FWD6_1_EUR_TENOR[loopins]);
    }
  }

  /** Units of curves */
  private static final int[] NB_UNITS = new int[] {3 };
  private static final int NB_BLOCKS = NB_UNITS.length;
  private static final GeneratorYDCurve[][][] GENERATORS_UNITS = new GeneratorYDCurve[NB_BLOCKS][][];
  private static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];
  private static final MulticurveProviderDiscount KNOWN_DATA_DSC = new MulticurveProviderDiscount(FX_MATRIX);
  private static final HullWhiteOneFactorPiecewiseConstantParameters PARAMETERS_HW = 
      HullWhiteDataSets.createHullWhiteParameters();
  private static final HullWhiteOneFactorProviderDiscount KNOWN_DATA_HW = 
      new HullWhiteOneFactorProviderDiscount(KNOWN_DATA_DSC, PARAMETERS_HW, EUR);  
  private static final LinkedHashMap<String, Currency> DSC_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IborIndex[]> FWD_IBOR_MAP = new LinkedHashMap<>();

  static {
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      GENERATORS_UNITS[loopblock] = new GeneratorYDCurve[NB_UNITS[loopblock]][];
      NAMES_UNITS[loopblock] = new String[NB_UNITS[loopblock]][];
    }
    final GeneratorYDCurve genIntLin = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_LINEAR);
    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[0][1] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[0][2] = new GeneratorYDCurve[] {genIntLin };
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_EUR };
    NAMES_UNITS[0][1] = new String[] {CURVE_NAME_FWD3_EUR };
    NAMES_UNITS[0][2] = new String[] {CURVE_NAME_FWD6_EUR };
    DSC_MAP.put(CURVE_NAME_DSC_EUR, EUR);
    FWD_ON_MAP.put(CURVE_NAME_DSC_EUR, new IndexON[] {EUREONIA });
    FWD_IBOR_MAP.put(CURVE_NAME_FWD3_EUR, new IborIndex[] {EUREURIBOR3M });
    FWD_IBOR_MAP.put(CURVE_NAME_FWD6_EUR, new IborIndex[] {EUREURIBOR6M });
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  public static InstrumentDefinition<?>[] getDefinitions(final double[] marketQuotes, 
      final GeneratorInstrument[] generators, final GeneratorAttribute[] attribute, final ZonedDateTime referenceDate) {
    final InstrumentDefinition<?>[] definitions = new InstrumentDefinition<?>[marketQuotes.length];
    for (int loopmv = 0; loopmv < marketQuotes.length; loopmv++) {
      definitions[loopmv] = generators[loopmv].generateInstrument(referenceDate, marketQuotes[loopmv], 
          NOTIONAL, attribute[loopmv]);
    }
    return definitions;
  }

  /** Calculators */
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQDC = 
      ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSDC = 
      ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteHullWhiteCalculator PSMQHWC = 
      ParSpreadMarketQuoteHullWhiteCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityHullWhiteCalculator PSMQCSHWC = 
      ParSpreadMarketQuoteCurveSensitivityHullWhiteCalculator.getInstance();

  private static final MulticurveDiscountBuildingRepository CURVE_BUILDING_REPOSITORY_DSC = 
      new MulticurveDiscountBuildingRepository(TOLERANCE_ROOT, TOLERANCE_ROOT, STEP_MAX);
  private static final HullWhiteProviderDiscountBuildingRepository CURVE_BUILDING_REPOSITORY_HW = 
      new HullWhiteProviderDiscountBuildingRepository(TOLERANCE_ROOT, TOLERANCE_ROOT, STEP_MAX);


  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getCurvesUSDOisFutL3Discounting(
      ZonedDateTime valuationDate) {
    GeneratorInstrument<? extends GeneratorAttribute>[] generatorFwd3 = 
        CurveCalibrationConventionDataSets.generatorEurIbor3Fut3Irs3(valuationDate, 1, 6, 13);
    InstrumentDefinition<?>[] definitionDsc = getDefinitions(DSC_1_EUR_MARKET_QUOTES, DSC_1_EUR_GENERATORS, 
        DSC_1_EUR_ATTR, valuationDate);
    InstrumentDefinition<?>[] definitionFwd3 = getDefinitions(FWD3_1_EUR_MARKET_QUOTES, generatorFwd3, 
        FWD3_1_USD_ATTR, valuationDate);
    InstrumentDefinition<?>[] definitionFwd6 = getDefinitions(FWD6_1_EUR_MARKET_QUOTES, FWD6_1_EUR_GENERATORS, 
        FWD6_1_USD_ATTR, valuationDate);
    InstrumentDefinition<?>[][][] definitionUnit = new InstrumentDefinition<?>[NB_UNITS[0]][][];
    definitionUnit[0] = new InstrumentDefinition<?>[][] {definitionDsc };
    definitionUnit[1] = new InstrumentDefinition<?>[][] {definitionFwd3 };
    definitionUnit[2] = new InstrumentDefinition<?>[][] {definitionFwd6 };
    return CurveCalibrationTestsUtils.makeCurvesFromDefinitionsMulticurve(valuationDate, definitionUnit, 
        GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA_DSC, PSMQDC, PSMQCSDC, false, DSC_MAP, FWD_ON_MAP, FWD_IBOR_MAP, 
        CURVE_BUILDING_REPOSITORY_DSC, TS_FIXED_OIS_EUR_WITH_TODAY, TS_FIXED_OIS_EUR_WITHOUT_TODAY, 
        TS_FIXED_IBOR_EUR3M_WITH_TODAY, TS_FIXED_IBOR_EUR3M_WITHOUT_TODAY);
  }

  public static Pair<HullWhiteOneFactorProviderDiscount, CurveBuildingBlockBundle> getCurvesUSDOisFutL3HullWhite(
      ZonedDateTime valuationDate) {
    GeneratorInstrument<? extends GeneratorAttribute>[] generatorFwd3 = 
        CurveCalibrationConventionDataSets.generatorEurIbor3Fut3Irs3(valuationDate, 1, 6, 13);
    InstrumentDefinition<?>[] definitionDsc = getDefinitions(DSC_1_EUR_MARKET_QUOTES, DSC_1_EUR_GENERATORS, 
        DSC_1_EUR_ATTR, valuationDate);
    InstrumentDefinition<?>[] definitionFwd3 = getDefinitions(FWD3_1_EUR_MARKET_QUOTES, generatorFwd3, 
        FWD3_1_USD_ATTR, valuationDate);
    InstrumentDefinition<?>[] definitionFwd6 = getDefinitions(FWD6_1_EUR_MARKET_QUOTES, FWD6_1_EUR_GENERATORS, 
        FWD6_1_USD_ATTR, valuationDate);
    InstrumentDefinition<?>[][][] definitionUnit = new InstrumentDefinition<?>[NB_UNITS[0]][][];
    definitionUnit[0] = new InstrumentDefinition<?>[][] {definitionDsc };
    definitionUnit[1] = new InstrumentDefinition<?>[][] {definitionFwd3 };
    definitionUnit[2] = new InstrumentDefinition<?>[][] {definitionFwd6 };
    return CurveCalibrationTestsUtils.makeCurvesFromDefinitionsHullWhite(valuationDate, definitionUnit, 
        GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA_HW, PSMQHWC, PSMQCSHWC, false, DSC_MAP, FWD_ON_MAP, FWD_IBOR_MAP, 
        CURVE_BUILDING_REPOSITORY_HW, TS_FIXED_OIS_EUR_WITH_TODAY, TS_FIXED_OIS_EUR_WITHOUT_TODAY, 
        TS_FIXED_IBOR_EUR3M_WITH_TODAY, TS_FIXED_IBOR_EUR3M_WITHOUT_TODAY);
  }
  
  public static double[] oisMarketData() {
    return DSC_1_EUR_MARKET_QUOTES;
  }
  
  public static double[] futIrs3MMarketData() {
    return FWD3_1_EUR_MARKET_QUOTES;
  }

  /**
   * Returns the array of Ibor index used in the curve data set. 
   * @return The array: EUREURIBOR3M 
   */
  public static IborIndex[] indexIborArrayEUROisE3() {
    return new IborIndex[] {EUREURIBOR3M };
  }

  /**
   * Returns the array of overnight index used in the curve data set. 
   * @return The array: EUREONIA 
   */
  public static IndexON[] indexONArray() {
    return new IndexON[] {EUREONIA };
  }

  /**
   * Returns the array of calendars used in the curve data set. 
   * @return The array: TARGET
   */
  public static Calendar[] calendarArray() {
    return new Calendar[] {TARGET };
  }

}
