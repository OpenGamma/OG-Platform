/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.tutorial.datasets;

import java.util.Arrays;
import java.util.LinkedHashMap;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.datasets.CalendarGBP;
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
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Curves calibration in GBP: 
 * DSCON-OIS/LIBOR6M-FRAIRS
 * Recent market data. Standard instruments.
 */
public class RecentDataSetsMulticurveStandardGbp {

  private static final Interpolator1D INTERPOLATOR_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();

  private static final Calendar LON = new CalendarGBP("LON");
  private static final Currency GBP = Currency.GBP;
  private static final FXMatrix FX_MATRIX = new FXMatrix(GBP);

  private static final double NOTIONAL = 1.0;

  private static final GeneratorSwapFixedONMaster GENERATOR_OIS_MASTER = GeneratorSwapFixedONMaster.getInstance();
  private static final GeneratorSwapFixedIborMaster GENERATOR_IRS_MASTER = GeneratorSwapFixedIborMaster.getInstance();

  private static final GeneratorSwapFixedON GENERATOR_OIS_GBP = GENERATOR_OIS_MASTER.getGenerator("GBP1YSONIA", LON);
  private static final IndexON GBPSONIA = GENERATOR_OIS_GBP.getIndex();
  private static final GeneratorSwapFixedIbor GBP6MLIBOR6M = GENERATOR_IRS_MASTER.getGenerator("GBP6MLIBOR6M", LON);
  private static final IborIndex GBPLIBOR6M = GBP6MLIBOR6M.getIborIndex();

  private static final String CURVE_NAME_DSC_GBP = "GBP-DSCON-OIS";
  private static final String CURVE_NAME_FWD3_GBP = "GBP-LIBOR6M-FRAIRS";

  /** Data as of 16-Jul-2014 */
  /** Market values for the dsc GBP curve */
  private static final double[] DSC_GBP_MARKET_QUOTES = new double[] {0.004263,
    0.004275, 0.00431, 0.004375, 0.004375, 0.004507,
    0.004592, 0.004805, 0.005107, 0.005339, 0.00562,
    0.005936, 0.006231, 0.006543, 0.006879, 0.007185,
    0.0110, 0.0142, 0.0167, 0.0182 };
  /** Generators for the dsc GBP curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_GBP_GENERATORS =
      CurveCalibrationConventionDataSets.generatorGbpOnOis(1, 19);
  /** Tenors for the dsc GBP curve */
  private static final Period[] DSC_2_GBP_TENOR = new Period[] {Period.ofDays(0),
    Period.ofDays(7), Period.ofDays(14), Period.ofDays(21), Period.ofMonths(1), Period.ofMonths(2),
    Period.ofMonths(3), Period.ofMonths(4), Period.ofMonths(5), Period.ofMonths(6), Period.ofMonths(7),
    Period.ofMonths(8), Period.ofMonths(9), Period.ofMonths(10), Period.ofMonths(11), Period.ofMonths(12),
    Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5) };
  private static final GeneratorAttributeIR[] DSC_GBP_ATTR = new GeneratorAttributeIR[DSC_2_GBP_TENOR.length];
  static {
    for (int loopins = 0; loopins < 1; loopins++) {
      DSC_GBP_ATTR[loopins] = new GeneratorAttributeIR(DSC_2_GBP_TENOR[loopins], Period.ofDays(0));
    }
    for (int loopins = 1; loopins < DSC_2_GBP_TENOR.length; loopins++) {
      DSC_GBP_ATTR[loopins] = new GeneratorAttributeIR(DSC_2_GBP_TENOR[loopins]);
    }
  }

  /** Market values for the Fwd 6M GBP curve */
  private static final double[] FWD6_GBP_MARKET_QUOTES = new double[] {0.0023,
    0.0026, 0.0032,
    0.0033, 0.0070, 0.0115, 0.0153, 0.0181,
    0.0222, 0.0260, 0.0277, 0.0295, 0.0310,
    0.0318, 0.0320 };
  /** Generators for the Fwd 6M GBP curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD6_GBP_GENERATORS =
      CurveCalibrationConventionDataSets.generatorGbpIbor6Fra6Irs6(1, 2, 12);
  /** Tenors for the Fwd 6M GBP curve */
  private static final Period[] FWD6_GBP_TENOR = new Period[] {Period.ofMonths(0),
    Period.ofMonths(8), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(7), Period.ofYears(10), Period.ofYears(12), Period.ofYears(15), Period.ofYears(20),
    Period.ofYears(25), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] FWD6_GBP_ATTR = new GeneratorAttributeIR[FWD6_GBP_TENOR.length];
  static {
    for (int loopins = 0; loopins < FWD6_GBP_TENOR.length; loopins++) {
      FWD6_GBP_ATTR[loopins] = new GeneratorAttributeIR(FWD6_GBP_TENOR[loopins]);
    }
  }

  /** Units of curves */
  private static final int NB_UNITS = 2;
  private static final int NB_BLOCKS = 1;
  private static final GeneratorYDCurve[][][] GENERATORS_UNITS = new GeneratorYDCurve[NB_BLOCKS][][];
  private static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];
  private static final MulticurveProviderDiscount KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);
  private static final LinkedHashMap<String, Currency> DSC_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IborIndex[]> FWD_IBOR_MAP = new LinkedHashMap<>();

  static {
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      GENERATORS_UNITS[loopblock] = new GeneratorYDCurve[NB_UNITS][];
      NAMES_UNITS[loopblock] = new String[NB_UNITS][];
    }
    final GeneratorYDCurve genIntLin = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_LINEAR);
    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[0][1] = new GeneratorYDCurve[] {genIntLin };
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_GBP };
    NAMES_UNITS[0][1] = new String[] {CURVE_NAME_FWD3_GBP };
    DSC_MAP.put(CURVE_NAME_DSC_GBP, GBP);
    FWD_ON_MAP.put(CURVE_NAME_DSC_GBP, new IndexON[] {GBPSONIA });
    FWD_IBOR_MAP.put(CURVE_NAME_FWD3_GBP, new IborIndex[] {GBPLIBOR6M });
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  public static InstrumentDefinition<?>[] getDefinitions(final double[] marketQuotes, final GeneratorInstrument[] generators, final GeneratorAttribute[] attribute,
      final ZonedDateTime referenceDate) {
    final InstrumentDefinition<?>[] definitions = new InstrumentDefinition<?>[marketQuotes.length];
    for (int loopmv = 0; loopmv < marketQuotes.length; loopmv++) {
      definitions[loopmv] = generators[loopmv].generateInstrument(referenceDate, marketQuotes[loopmv], NOTIONAL, attribute[loopmv]);
    }
    return definitions;
  }

  /** Calculators */
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSC = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();

  private static final MulticurveDiscountBuildingRepository CURVE_BUILDING_REPOSITORY =
      CurveCalibrationConventionDataSets.curveBuildingRepositoryMulticurve();

  /**
   * Calibrate curves with hard-coded date and with calibration date the date provided. The curves are discounting/overnight forward,
   * Libor3M forward, Libor1M forward and Libor6M forward.
   * @param calibrationDate The calibration date.
   * @return The curves and the Jacobian matrices.
   */
  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getCurvesGbpOisL6(ZonedDateTime calibrationDate) {
    InstrumentDefinition<?>[][][] definitionsUnits = new InstrumentDefinition<?>[NB_UNITS][][];
    InstrumentDefinition<?>[] definitionsDsc = getDefinitions(DSC_GBP_MARKET_QUOTES, DSC_GBP_GENERATORS, DSC_GBP_ATTR, calibrationDate);
    InstrumentDefinition<?>[] definitionsFwd6 = getDefinitions(FWD6_GBP_MARKET_QUOTES, FWD6_GBP_GENERATORS, FWD6_GBP_ATTR, calibrationDate);
    definitionsUnits[0] = new InstrumentDefinition<?>[][] {definitionsDsc };
    definitionsUnits[1] = new InstrumentDefinition<?>[][] {definitionsFwd6 };
    return CurveCalibrationTestsUtils.makeCurvesFromDefinitionsMulticurve(calibrationDate, definitionsUnits, GENERATORS_UNITS[0], NAMES_UNITS[0],
        KNOWN_DATA, PSMQC, PSMQCSC, false, DSC_MAP, FWD_ON_MAP, FWD_IBOR_MAP, CURVE_BUILDING_REPOSITORY,
        TS_FIXED_OIS_GBP_WITH_TODAY, TS_FIXED_OIS_GBP_WITHOUT_TODAY, TS_FIXED_IBOR_GBP6M_WITH_LAST, TS_FIXED_IBOR_GBP6M_WITHOUT_LAST);
  }

  /**
   * Get the definitions for the first instruments of the standard curve to be used in another curve
   * @param calibrationDate The calibration date.
   * @param howMany The number of instruments to be returned
   * @return The InstrumentDefinition for the first howMany instruments of the curve
   */
  public static InstrumentDefinition<?>[] getDefinitionForFirstInstruments(ZonedDateTime calibrationDate, int howMany) {
    InstrumentDefinition<?>[] definitionsDsc = getDefinitions(DSC_GBP_MARKET_QUOTES, DSC_GBP_GENERATORS, DSC_GBP_ATTR, calibrationDate);
    return Arrays.copyOf(definitionsDsc, howMany);
  }


  /**
   * Returns the array of Ibor index used in the curve data set. 
   * @return The array: GBPLIBOR6M
   */
  public static IborIndex[] indexIborArrayGBPOisL6() {
    return new IborIndex[] {GBPLIBOR6M };
  }

  /**
   * Returns the array of overnight index used in the curve data set. 
   * @return The array: GBPSONIA 
   */
  public static IndexON[] indexONArray() {
    return new IndexON[] {GBPSONIA };
  }

  /**
   * Returns the array of calendars used in the curve data set. 
   * @return The array: NYC 
   */
  public static Calendar[] calendarArray() {
    return new Calendar[] {LON };
  }

  /**
   * Returns an array with one time series corresponding to the GBP LIBOR6M fixing up to and including the last date.
   * @return
   */
  public static ZonedDateTimeDoubleTimeSeries fixingGbpLibor6MWithLast() {
    return TS_IBOR_GBP6M_WITH_LAST;
  }

  /**
   * Returns an array with one time series corresponding to the GBP LIBOR6M fixing up to and including the last date.
   * @return
   */
  public static ZonedDateTimeDoubleTimeSeries fixingGbpLibor6MWithoutLast() {
    return TS_IBOR_GBP6M_WITHOUT_LAST;
  }

  /**
   * Returns an array with one time series corresponding to the GBP SONIA fixing up to and including the last date.
   * @return
   */
  public static ZonedDateTimeDoubleTimeSeries fixingGbpSoniaWithLast() {
    return TS_ON_GBP_WITH_TODAY;
  }

  /**
   * Returns an array with one time series corresponding to the GBP SONIA fixing up to and including the last date.
   * @return
   */
  public static ZonedDateTimeDoubleTimeSeries fixingGbpSoniaWithoutLast() {
    return TS_ON_GBP_WITHOUT_TODAY;
  }

  private static final ZonedDateTimeDoubleTimeSeries TS_EMPTY = ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC();
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_GBP_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2014, 7, 1), DateUtils.getUTCDate(2014, 7, 2), DateUtils.getUTCDate(2014, 7, 3), DateUtils.getUTCDate(2014, 7, 4),
        DateUtils.getUTCDate(2014, 7, 7), DateUtils.getUTCDate(2014, 7, 8), DateUtils.getUTCDate(2014, 7, 9), DateUtils.getUTCDate(2014, 7, 10), DateUtils.getUTCDate(2014, 7, 11),
        DateUtils.getUTCDate(2014, 7, 14), DateUtils.getUTCDate(2014, 7, 15), DateUtils.getUTCDate(2014, 7, 16), DateUtils.getUTCDate(2014, 7, 17), DateUtils.getUTCDate(2014, 7, 18),
        DateUtils.getUTCDate(2014, 7, 21), DateUtils.getUTCDate(2014, 7, 22), DateUtils.getUTCDate(2014, 7, 23), DateUtils.getUTCDate(2014, 7, 24), DateUtils.getUTCDate(2014, 7, 25),
        DateUtils.getUTCDate(2014, 7, 28) },
      new double[] {0.002318, 0.002346, 0.002321, 0.002331,
        0.002341, 0.002336, 0.002341, 0.002336, 0.002336,
        0.002326, 0.002331, 0.002336, 0.002336, 0.002316,
        0.002331, 0.002326, 0.002341, 0.002351, 0.002341,
        0.002341 });
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_GBP_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2014, 7, 1), DateUtils.getUTCDate(2014, 7, 2), DateUtils.getUTCDate(2014, 7, 3), DateUtils.getUTCDate(2014, 7, 4),
        DateUtils.getUTCDate(2014, 7, 7), DateUtils.getUTCDate(2014, 7, 8), DateUtils.getUTCDate(2014, 7, 9), DateUtils.getUTCDate(2014, 7, 10), DateUtils.getUTCDate(2014, 7, 11),
        DateUtils.getUTCDate(2014, 7, 14), DateUtils.getUTCDate(2014, 7, 15), DateUtils.getUTCDate(2014, 7, 16), DateUtils.getUTCDate(2014, 7, 17), DateUtils.getUTCDate(2014, 7, 18),
        DateUtils.getUTCDate(2014, 7, 21), DateUtils.getUTCDate(2014, 7, 22), DateUtils.getUTCDate(2014, 7, 23), DateUtils.getUTCDate(2014, 7, 24), DateUtils.getUTCDate(2014, 7, 25) },
      new double[] {0.002318, 0.002346, 0.002321, 0.002331,
        0.002341, 0.002336, 0.002341, 0.002336, 0.002336,
        0.002326, 0.002331, 0.002336, 0.002336, 0.002316,
        0.002331, 0.002326, 0.002341, 0.002351, 0.002341 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_GBP_WITH_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_GBP_WITH_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_GBP_WITHOUT_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_GBP_WITHOUT_TODAY };

  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_GBP6M_WITH_LAST = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2014, 7, 1), DateUtils.getUTCDate(2014, 7, 2), DateUtils.getUTCDate(2014, 7, 3), DateUtils.getUTCDate(2014, 7, 4),
        DateUtils.getUTCDate(2014, 7, 7), DateUtils.getUTCDate(2014, 7, 8), DateUtils.getUTCDate(2014, 7, 9), DateUtils.getUTCDate(2014, 7, 10), DateUtils.getUTCDate(2014, 7, 11),
        DateUtils.getUTCDate(2014, 7, 14), DateUtils.getUTCDate(2014, 7, 15), DateUtils.getUTCDate(2014, 7, 16), DateUtils.getUTCDate(2014, 7, 17), DateUtils.getUTCDate(2014, 7, 18),
        DateUtils.getUTCDate(2014, 7, 21), DateUtils.getUTCDate(2014, 7, 22), DateUtils.getUTCDate(2014, 7, 23), DateUtils.getUTCDate(2014, 7, 24), DateUtils.getUTCDate(2014, 7, 25),
        DateUtils.getUTCDate(2014, 7, 28) },
      new double[] {0.002318, 0.002346, 0.002321, 0.002331,
        0.002341, 0.002336, 0.002341, 0.002336, 0.002336,
        0.002326, 0.002331, 0.002336, 0.002336, 0.002316,
        0.002331, 0.002326, 0.002341, 0.002351, 0.002341,
        0.002341 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_GBP6M_WITHOUT_LAST = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2014, 7, 1), DateUtils.getUTCDate(2014, 7, 2), DateUtils.getUTCDate(2014, 7, 3), DateUtils.getUTCDate(2014, 7, 4),
        DateUtils.getUTCDate(2014, 7, 7), DateUtils.getUTCDate(2014, 7, 8), DateUtils.getUTCDate(2014, 7, 9), DateUtils.getUTCDate(2014, 7, 10), DateUtils.getUTCDate(2014, 7, 11),
        DateUtils.getUTCDate(2014, 7, 14), DateUtils.getUTCDate(2014, 7, 15), DateUtils.getUTCDate(2014, 7, 16), DateUtils.getUTCDate(2014, 7, 17), DateUtils.getUTCDate(2014, 7, 18),
        DateUtils.getUTCDate(2014, 7, 21), DateUtils.getUTCDate(2014, 7, 22), DateUtils.getUTCDate(2014, 7, 23), DateUtils.getUTCDate(2014, 7, 24), DateUtils.getUTCDate(2014, 7, 25) },
      new double[] {0.002318, 0.002346, 0.002321, 0.002331,
        0.002341, 0.002336, 0.002341, 0.002336, 0.002336,
        0.002326, 0.002331, 0.002336, 0.002336, 0.002316,
        0.002331, 0.002326, 0.002341, 0.002351, 0.002341 });

  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_GBP6M_WITH_LAST = new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_GBP6M_WITH_LAST };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_GBP6M_WITHOUT_LAST = new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_GBP6M_WITHOUT_LAST };

}
