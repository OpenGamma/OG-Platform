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
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationConventionDataSets;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationTestsUtils;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Curves calibration in JPY.
 * Hard-coded data as of 2-Aug-2014.
 */
public class AnalysisMarketDataJPYSets {

  private static final ZonedDateTime CALIBRATION_DATE = DateUtils.getUTCDate(2014, 8, 2);

  private static final Interpolator1D INTERPOLATOR_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

//  private static final Interpolator1D INTERPOLATOR_LOG_NCS = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LOG_NATURAL_CUBIC,
//      Interpolator1DFactory.EXPONENTIAL_EXTRAPOLATOR,
//      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();

  private static final Calendar TYO = new MondayToFridayCalendar("TYO");
  private static final Currency JPY = Currency.JPY;
  private static final FXMatrix FX_MATRIX = new FXMatrix(JPY);

  private static final double NOTIONAL = 1.0;

  private static final GeneratorSwapFixedON GENERATOR_OIS_JPY = GeneratorSwapFixedONMaster.getInstance().getGenerator("JPY1YTONAR", TYO);
  private static final IndexON INDEX_ON_JPY = GENERATOR_OIS_JPY.getIndex();
  private static final IndexIborMaster MASTER_IBOR_INDEX = IndexIborMaster.getInstance();
  private static final IborIndex JPYLIBOR6M = MASTER_IBOR_INDEX.getIndex("JPYLIBOR6M");
  private static final IborIndex JPYLIBOR3M = MASTER_IBOR_INDEX.getIndex("JPYLIBOR3M");

  private static final String CURVE_NAME_DSC_JPY = "JPY-DSCON-OIS";
  private static final String CURVE_NAME_FWD6_JPY = "JPY-LIBOR6M-FRAIRS";
  private static final String CURVE_NAME_FWD3_JPY = "JPY-LIBOR3M-FRABS";

  /** Market values for the dsc JPY curve */
  private static final double[] DSC_MARKET_QUOTES = new double[] {0.0001,
    0.0006, 0.0006, 0.0006, 0.0005, 0.0006,
    0.0007, 0.0007, 0.0006, 0.0007, 0.0006,
    0.0006, 0.0006, 0.0010, 0.0010, 0.0014,
    0.0021, 0.0027, 0.0034, 0.0041, 0.0057,
    0.0083, 0.0115, 0.0131, 0.0141, 0.0154 };
  /** Generators for the dsc JPY curve */
  private static final int NB_ON_DEPO = 1;
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_GENERATORS =
      CurveCalibrationConventionDataSets.generatorJpyOnOis(NB_ON_DEPO, 25);
  /** Tenors for the dsc JPY curve */
  private static final Period[] DSC_TENOR = new Period[] {Period.ofDays(0),
    Period.ofDays(7), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(4),
    Period.ofMonths(5), Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1), Period.ofMonths(18),
    Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(6),
    Period.ofYears(7), Period.ofYears(8), Period.ofYears(9), Period.ofYears(10), Period.ofYears(12),
    Period.ofYears(15), Period.ofYears(20), Period.ofYears(25), Period.ofYears(30), Period.ofYears(40) };
  private static final GeneratorAttributeIR[] DSC_ATTR = new GeneratorAttributeIR[DSC_TENOR.length];
  static {
    for (int loopins = 0; loopins < NB_ON_DEPO; loopins++) {
      DSC_ATTR[loopins] = new GeneratorAttributeIR(DSC_TENOR[loopins], Period.ofDays(0));
    }
    for (int loopins = NB_ON_DEPO; loopins < DSC_TENOR.length; loopins++) {
      DSC_ATTR[loopins] = new GeneratorAttributeIR(DSC_TENOR[loopins]);
    }
  }

  /** Market values for the Fwd 6M JPY curve */
  private static final double[] FWD6_MARKET_QUOTES = new double[] {0.0017786,
    0.0029, 0.0029, 0.0029,
    0.0018, 0.0017, 0.0018, 0.0021, 0.0025,
    0.0025, 0.0031, 0.0040, 0.0048, 0.0057,
    0.0065, 0.0084, 0.0111, 0.0143, 0.0160,
    0.0170, 0.0183 };
  /** Generators for the Fwd 6M JPY curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD6_GENERATORS =
      CurveCalibrationConventionDataSets.generatorJpyIbor6Fra6Irs6(1, 3, 17);
  /** Tenors for the Fwd 6M JPY curve */
  private static final Period[] FWD6_TENOR = new Period[] {Period.ofMonths(0),
    Period.ofMonths(7), Period.ofMonths(8), Period.ofMonths(9),
    Period.ofYears(1), Period.ofMonths(18), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4),
    Period.ofYears(5), Period.ofYears(6), Period.ofYears(7), Period.ofYears(8), Period.ofYears(9),
    Period.ofYears(10), Period.ofYears(12), Period.ofYears(15), Period.ofYears(20), Period.ofYears(25),
    Period.ofYears(30), Period.ofYears(40) };
  private static final GeneratorAttributeIR[] FWD6_ATTR = new GeneratorAttributeIR[FWD6_TENOR.length];
  static {
    for (int loopins = 0; loopins < FWD6_TENOR.length; loopins++) {
      FWD6_ATTR[loopins] = new GeneratorAttributeIR(FWD6_TENOR[loopins]);
    }
  }

  /** Market values for the Fwd 3M JPY curve */
  private static final double[] FWD3_MARKET_QUOTES = new double[] {0.0013,
    0.0020, 0.0020, 0.0020, 0.0019, 0.0019, 0.0019,
    0.0005, 0.0005, 0.0005, 0.0005, 0.0006,
    0.0007, 0.0008, 0.0009, 0.0009, 0.0010,
    0.0011, 0.0012, 0.0013, 0.0013, 0.0013,
    0.0013, 0.0014 };
  /** Generators for the Fwd 3M JPY curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_GENERATORS =
      CurveCalibrationConventionDataSets.generatorJpyIbor3Fra3Bs3(1, 6, 17);
  /** Tenors for the Fwd 6M JPY curve */
  private static final Period[] FWD3_TENOR = new Period[] {Period.ofMonths(0),
    Period.ofMonths(4), Period.ofMonths(5), Period.ofMonths(6), Period.ofMonths(7), Period.ofMonths(8), Period.ofMonths(9),
    Period.ofYears(1), Period.ofMonths(18), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4),
    Period.ofYears(5), Period.ofYears(6), Period.ofYears(7), Period.ofYears(8), Period.ofYears(9),
    Period.ofYears(10), Period.ofYears(12), Period.ofYears(15), Period.ofYears(20), Period.ofYears(25),
    Period.ofYears(30), Period.ofYears(40) };
  private static final GeneratorAttributeIR[] FWD3_ATTR = new GeneratorAttributeIR[FWD3_TENOR.length];
  static {
    for (int loopins = 0; loopins < FWD3_TENOR.length; loopins++) {
      FWD3_ATTR[loopins] = new GeneratorAttributeIR(FWD3_TENOR[loopins]);
    }
  }

  /** Standard JPY discounting curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_DSC;
  /** Standard JPY Forward 6M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_FWD6;
  /** Standard JPY Forward 3M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_FWD3;

  /** Units of curves */
  private static final int[] NB_UNITS = new int[] {3 };
  private static final int NB_BLOCKS = NB_UNITS.length;
  private static final InstrumentDefinition<?>[][][][] DEFINITIONS_UNITS = new InstrumentDefinition<?>[NB_BLOCKS][][][];
  private static final GeneratorYDCurve[][][] GENERATORS_UNITS = new GeneratorYDCurve[NB_BLOCKS][][];
  private static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];
  private static final MulticurveProviderDiscount KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);
  private static final LinkedHashMap<String, Currency> DSC_MAP_3C = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP_3C = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IborIndex[]> FWD_IBOR_MAP_3C = new LinkedHashMap<>();

  private static final GeneratorYDCurve GENERATOR_INTERPOLATED_YIELD_LINEAR = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_LINEAR);
//  private static final GeneratorYDCurve GENERATOR_INTERPOLATED_DF_LNCS = new GeneratorCurveDiscountFactorInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_LOG_NCS);
  static {
    DEFINITIONS_DSC = getDefinitions(CALIBRATION_DATE, DSC_MARKET_QUOTES, DSC_GENERATORS, DSC_ATTR);
    DEFINITIONS_FWD6 = getDefinitions(CALIBRATION_DATE, FWD6_MARKET_QUOTES, FWD6_GENERATORS, FWD6_ATTR);
    DEFINITIONS_FWD3 = getDefinitions(CALIBRATION_DATE, FWD3_MARKET_QUOTES, FWD3_GENERATORS, FWD3_ATTR);
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      DEFINITIONS_UNITS[loopblock] = new InstrumentDefinition<?>[NB_UNITS[loopblock]][][];
      GENERATORS_UNITS[loopblock] = new GeneratorYDCurve[NB_UNITS[loopblock]][];
      NAMES_UNITS[loopblock] = new String[NB_UNITS[loopblock]][];
    }
    DEFINITIONS_UNITS[0][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC };
    DEFINITIONS_UNITS[0][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD6 };
    DEFINITIONS_UNITS[0][2] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3 };
    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {GENERATOR_INTERPOLATED_YIELD_LINEAR };
    GENERATORS_UNITS[0][1] = new GeneratorYDCurve[] {GENERATOR_INTERPOLATED_YIELD_LINEAR };
    GENERATORS_UNITS[0][2] = new GeneratorYDCurve[] {GENERATOR_INTERPOLATED_YIELD_LINEAR };
    //    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {GENERATOR_INTERPOLATED_DF_LNCS };
    //    GENERATORS_UNITS[0][1] = new GeneratorYDCurve[] {GENERATOR_INTERPOLATED_DF_LNCS };
    //    GENERATORS_UNITS[0][2] = new GeneratorYDCurve[] {GENERATOR_INTERPOLATED_DF_LNCS };
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_JPY };
    NAMES_UNITS[0][1] = new String[] {CURVE_NAME_FWD6_JPY };
    NAMES_UNITS[0][2] = new String[] {CURVE_NAME_FWD3_JPY };
    DSC_MAP_3C.put(CURVE_NAME_DSC_JPY, JPY);
    FWD_ON_MAP_3C.put(CURVE_NAME_DSC_JPY, new IndexON[] {INDEX_ON_JPY });
    FWD_IBOR_MAP_3C.put(CURVE_NAME_FWD6_JPY, new IborIndex[] {JPYLIBOR6M });
    FWD_IBOR_MAP_3C.put(CURVE_NAME_FWD3_JPY, new IborIndex[] {JPYLIBOR3M });
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  public static InstrumentDefinition<?>[] getDefinitions(ZonedDateTime calibrationDate,
      final double[] marketQuotes, final GeneratorInstrument[] generators, final GeneratorAttribute[] attribute) {
    final InstrumentDefinition<?>[] definitions = new InstrumentDefinition<?>[marketQuotes.length];
    for (int loopmv = 0; loopmv < marketQuotes.length; loopmv++) {
      definitions[loopmv] = generators[loopmv].generateInstrument(calibrationDate, marketQuotes[loopmv], NOTIONAL, attribute[loopmv]);
    }
    return definitions;
  }

  // Calculator
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSC = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();

  private static final MulticurveDiscountBuildingRepository CURVE_BUILDING_REPOSITORY = CurveCalibrationConventionDataSets.curveBuildingRepositoryMulticurve();

  private static final ZonedDateTimeDoubleTimeSeries TS_EMPTY = ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC();
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_WITHOUT_TODAY =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {DateUtils.getUTCDate(2013, 8, 7), DateUtils.getUTCDate(2013, 8, 8) },
          new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_WITHOUT_TODAY =
      new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_WITHOUT_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_6M_WITHOUT_TODAY =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {DateUtils.getUTCDate(2013, 8, 8) },
          new double[] {0.0022 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_6M_WITHOUT_TODAY =
      new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_6M_WITHOUT_TODAY };

  private static final List<Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle>> CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK = new ArrayList<>();
  static {
    CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.add(
        CurveCalibrationTestsUtils.makeCurvesFromDefinitionsMulticurve(CALIBRATION_DATE, DEFINITIONS_UNITS[0], GENERATORS_UNITS[0],
            NAMES_UNITS[0], KNOWN_DATA, PSMQC, PSMQCSC, false, DSC_MAP_3C, FWD_ON_MAP_3C, FWD_IBOR_MAP_3C, CURVE_BUILDING_REPOSITORY,
            TS_FIXED_OIS_WITHOUT_TODAY, TS_FIXED_OIS_WITHOUT_TODAY, TS_FIXED_IBOR_6M_WITHOUT_TODAY, TS_FIXED_IBOR_6M_WITHOUT_TODAY));
  }

  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getMulticurveJPY() {
    return CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0);
  }

  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getMulticurveJPYOisL6L3(
      ZonedDateTime calibrationDate, double[] dscQuotes, double[] fwd6Quotes, double[] fwd3Quotes) {
    InstrumentDefinition<?>[] dscDefinition = getDefinitions(calibrationDate, dscQuotes, DSC_GENERATORS, DSC_ATTR);
    InstrumentDefinition<?>[] fwd6Definition = getDefinitions(calibrationDate, fwd6Quotes, FWD6_GENERATORS, FWD6_ATTR);
    InstrumentDefinition<?>[] fwd3Definition = getDefinitions(calibrationDate, fwd3Quotes, FWD3_GENERATORS, FWD3_ATTR);
    InstrumentDefinition<?>[][][] units = new InstrumentDefinition<?>[3][][];
    units[0] = new InstrumentDefinition<?>[][] {dscDefinition };
    units[1] = new InstrumentDefinition<?>[][] {fwd6Definition };
    units[2] = new InstrumentDefinition<?>[][] {fwd3Definition };
    return CurveCalibrationTestsUtils.makeCurvesFromDefinitionsMulticurve(CALIBRATION_DATE, units, GENERATORS_UNITS[0],
        NAMES_UNITS[0], KNOWN_DATA, PSMQC, PSMQCSC, false, DSC_MAP_3C, FWD_ON_MAP_3C, FWD_IBOR_MAP_3C, CURVE_BUILDING_REPOSITORY,
        TS_FIXED_OIS_WITHOUT_TODAY, TS_FIXED_OIS_WITHOUT_TODAY, TS_FIXED_IBOR_6M_WITHOUT_TODAY, TS_FIXED_IBOR_6M_WITHOUT_TODAY);
  }

  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getMulticurveJPYOisL6(
      ZonedDateTime calibrationDate, double[] dscQuotes, double[] fwd6Quotes) {
    InstrumentDefinition<?>[] dscDefinition = getDefinitions(calibrationDate, dscQuotes, DSC_GENERATORS, DSC_ATTR);
    InstrumentDefinition<?>[] fwd6Definition = getDefinitions(calibrationDate, fwd6Quotes, FWD6_GENERATORS, FWD6_ATTR);
    InstrumentDefinition<?>[][][] units = new InstrumentDefinition<?>[2][][];
    units[0] = new InstrumentDefinition<?>[][] {dscDefinition };
    units[1] = new InstrumentDefinition<?>[][] {fwd6Definition };
    GeneratorYDCurve[][] generators = new GeneratorYDCurve[2][];
    generators[0] = new GeneratorYDCurve[] {GENERATOR_INTERPOLATED_YIELD_LINEAR };
    generators[1] = new GeneratorYDCurve[] {GENERATOR_INTERPOLATED_YIELD_LINEAR };
    String[][] namesUnits = new String[2][];
    namesUnits[0] = new String[] {CURVE_NAME_DSC_JPY };
    namesUnits[1] = new String[] {CURVE_NAME_FWD6_JPY };
    return CurveCalibrationTestsUtils.makeCurvesFromDefinitionsMulticurve(CALIBRATION_DATE, units, generators,
        namesUnits, KNOWN_DATA, PSMQC, PSMQCSC, false, DSC_MAP_3C, FWD_ON_MAP_3C, FWD_IBOR_MAP_3C, CURVE_BUILDING_REPOSITORY,
        TS_FIXED_OIS_WITHOUT_TODAY, TS_FIXED_OIS_WITHOUT_TODAY, TS_FIXED_IBOR_6M_WITHOUT_TODAY, TS_FIXED_IBOR_6M_WITHOUT_TODAY);
  }

  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getMulticurveJPYOisL6L3OneUnit(
      ZonedDateTime calibrationDate, double[] dscQuotes, double[] fwd6Quotes, double[] fwd3Quotes) {
    InstrumentDefinition<?>[] dscDefinition = getDefinitions(calibrationDate, dscQuotes, DSC_GENERATORS, DSC_ATTR);
    InstrumentDefinition<?>[] fwd6Definition = getDefinitions(calibrationDate, fwd6Quotes, FWD6_GENERATORS, FWD6_ATTR);
    InstrumentDefinition<?>[] fwd3Definition = getDefinitions(calibrationDate, fwd3Quotes, FWD3_GENERATORS, FWD3_ATTR);
    InstrumentDefinition<?>[][][] units = new InstrumentDefinition<?>[1][][];
    units[0] = new InstrumentDefinition<?>[][] {dscDefinition, fwd6Definition, fwd3Definition };
    GeneratorYDCurve[][] generators = new GeneratorYDCurve[1][];
    generators[0] = new GeneratorYDCurve[] {GENERATOR_INTERPOLATED_YIELD_LINEAR, GENERATOR_INTERPOLATED_YIELD_LINEAR, GENERATOR_INTERPOLATED_YIELD_LINEAR };
    String[][] namesUnits = new String[1][];
    namesUnits[0] = new String[] {CURVE_NAME_DSC_JPY, CURVE_NAME_FWD6_JPY, CURVE_NAME_FWD3_JPY };
    return CurveCalibrationTestsUtils.makeCurvesFromDefinitionsMulticurve(CALIBRATION_DATE, units, generators,
        namesUnits, KNOWN_DATA, PSMQC, PSMQCSC, false, DSC_MAP_3C, FWD_ON_MAP_3C, FWD_IBOR_MAP_3C, CURVE_BUILDING_REPOSITORY,
        TS_FIXED_OIS_WITHOUT_TODAY, TS_FIXED_OIS_WITHOUT_TODAY, TS_FIXED_IBOR_6M_WITHOUT_TODAY, TS_FIXED_IBOR_6M_WITHOUT_TODAY);
  }

  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getMulticurveJPYOisL6OneUnit(
      ZonedDateTime calibrationDate, double[] dscQuotes, double[] fwd6Quotes) {
    InstrumentDefinition<?>[] dscDefinition = getDefinitions(calibrationDate, dscQuotes, DSC_GENERATORS, DSC_ATTR);
    InstrumentDefinition<?>[] fwd6Definition = getDefinitions(calibrationDate, fwd6Quotes, FWD6_GENERATORS, FWD6_ATTR);
    InstrumentDefinition<?>[][][] units = new InstrumentDefinition<?>[1][][];
    units[0] = new InstrumentDefinition<?>[][] {dscDefinition, fwd6Definition };
    GeneratorYDCurve[][] generators = new GeneratorYDCurve[1][];
    generators[0] = new GeneratorYDCurve[] {GENERATOR_INTERPOLATED_YIELD_LINEAR, GENERATOR_INTERPOLATED_YIELD_LINEAR };
    String[][] namesUnits = new String[1][];
    namesUnits[0] = new String[] {CURVE_NAME_DSC_JPY, CURVE_NAME_FWD6_JPY };
    return CurveCalibrationTestsUtils.makeCurvesFromDefinitionsMulticurve(CALIBRATION_DATE, units, generators,
        namesUnits, KNOWN_DATA, PSMQC, PSMQCSC, false, DSC_MAP_3C, FWD_ON_MAP_3C, FWD_IBOR_MAP_3C, CURVE_BUILDING_REPOSITORY,
        TS_FIXED_OIS_WITHOUT_TODAY, TS_FIXED_OIS_WITHOUT_TODAY, TS_FIXED_IBOR_6M_WITHOUT_TODAY, TS_FIXED_IBOR_6M_WITHOUT_TODAY);
  }

}
