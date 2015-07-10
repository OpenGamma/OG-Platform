/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.datasets;

import java.util.LinkedHashMap;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.datasets.CalendarGBP;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationConventionDataSets;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationTestsUtils;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Curves calibration in GBP: 
 * 0) ONDSC-OIS
 * Data stored in snapshots for comparison with platform.
 */
public class StandardDataSetsMulticurveGBP {
  private static final ZonedDateTime[] REFERENCE_DATE = new ZonedDateTime[2];
  static {
    REFERENCE_DATE[0] = DateUtils.getUTCDate(2014, 4, 11);
    REFERENCE_DATE[1] = DateUtils.getUTCDate(2014, 4, 11);
  }

  private static final Calendar LONDON = new CalendarGBP("LONDON");
  private static final Currency GBP = Currency.GBP;
  private static final FXMatrix FX_MATRIX = new FXMatrix(GBP);

  private static final double NOTIONAL = 1.0;

  private static final GeneratorSwapFixedONMaster GENERATOR_OIS_MASTER = GeneratorSwapFixedONMaster.getInstance();

  private static final GeneratorSwapFixedON GENERATOR_OIS_GBP = GENERATOR_OIS_MASTER.getGenerator("GBP1YSONIA", LONDON);
  private static final IndexON GBPSONIA = GENERATOR_OIS_GBP.getIndex();
  private static final ZonedDateTimeDoubleTimeSeries TS_EMPTY = ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC();
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_GBP_WITH_TODAY = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_GBP_WITHOUT_TODAY = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_GBP_WITH_TODAY = 
      new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_GBP_WITH_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_GBP_WITHOUT_TODAY = 
      new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_GBP_WITHOUT_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_GBP3M_WITH_TODAY = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.0035, 0.0036 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_GBP3M_WITHOUT_TODAY = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27) },
      new double[] {0.0035 });

  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_GBP3M_WITH_TODAY = 
      new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_GBP3M_WITH_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_GBP3M_WITHOUT_TODAY = 
      new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_GBP3M_WITHOUT_TODAY };

  private static final String CURVE_NAME_DSC_GBP = "GBP-DSCON-OIS";

  /** Data for 2014-01-22 **/
  /** Market values for the dsc GBP curve */
  private static final double[] DSC_1_GBP_MARKET_QUOTES = new double[] {0.004225, 0.004215,
    0.00424, 0.00422, 0.004226, 0.004303, 0.0045095,
    0.0049, 0.0076675, 0.010975, 0.0136605, 0.01583,
    0.01768, 0.019249, 0.020603, 0.0218265, 0.022898,
    0.024726, 0.026638, 0.028471, 0.029667 }; //21
  /** Generators for the dsc GBP curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_1_GBP_GENERATORS = 
      CurveCalibrationConventionDataSets.generatorGbpOnOis(2, 19);
  /** Tenors for the dsc GBP curve */
  private static final Period[] DSC_1_GBP_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1),
    Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(6), Period.ofYears(7), Period.ofYears(8), Period.ofYears(9), Period.ofYears(10),
    Period.ofYears(12), Period.ofYears(15), Period.ofYears(20), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] DSC_1_GBP_ATTR = new GeneratorAttributeIR[DSC_1_GBP_TENOR.length];
  static {
    for (int loopins = 0; loopins < 2; loopins++) {
      DSC_1_GBP_ATTR[loopins] = new GeneratorAttributeIR(DSC_1_GBP_TENOR[loopins], Period.ofDays(0));
    }
    for (int loopins = 2; loopins < DSC_1_GBP_TENOR.length; loopins++) {
      DSC_1_GBP_ATTR[loopins] = new GeneratorAttributeIR(DSC_1_GBP_TENOR[loopins]);
    }
  }

  /** Standard GBP discounting curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_DSC_1_GBP;

  /** Units of curves */
  private static final int[] NB_UNITS = new int[] {1 };
  private static final int NB_BLOCKS = NB_UNITS.length;
  private static final InstrumentDefinition<?>[][][][] DEFINITIONS_UNITS = new InstrumentDefinition<?>[NB_BLOCKS][][][];
  private static final GeneratorYDCurve[][][] GENERATORS_UNITS = new GeneratorYDCurve[NB_BLOCKS][][];
  private static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];
  private static final MulticurveProviderDiscount KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);
  private static final LinkedHashMap<String, Currency> DSC_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IborIndex[]> FWD_IBOR_MAP = new LinkedHashMap<>();

  static {
    DEFINITIONS_DSC_1_GBP = getDefinitions(DSC_1_GBP_MARKET_QUOTES, DSC_1_GBP_GENERATORS, DSC_1_GBP_ATTR, REFERENCE_DATE[0]);
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      DEFINITIONS_UNITS[loopblock] = new InstrumentDefinition<?>[NB_UNITS[loopblock]][][];
      GENERATORS_UNITS[loopblock] = new GeneratorYDCurve[NB_UNITS[loopblock]][];
      NAMES_UNITS[loopblock] = new String[NB_UNITS[loopblock]][];
    }
    DEFINITIONS_UNITS[0][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_1_GBP };
    final GeneratorYDCurve genIntLin = CurveCalibrationConventionDataSets.generatorYDMatLin();
    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {genIntLin };
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_GBP };
    DSC_MAP.put(CURVE_NAME_DSC_GBP, GBP);
    FWD_ON_MAP.put(CURVE_NAME_DSC_GBP, new IndexON[] {GBPSONIA });
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  public static InstrumentDefinition<?>[] getDefinitions(final double[] marketQuotes, 
      final GeneratorInstrument[] generators, final GeneratorAttribute[] attribute, final ZonedDateTime referenceDate) {
    final InstrumentDefinition<?>[] definitions = new InstrumentDefinition<?>[marketQuotes.length];
    for (int loopmv = 0; loopmv < marketQuotes.length; loopmv++) {
      definitions[loopmv] = generators[loopmv].generateInstrument(referenceDate, marketQuotes[loopmv], NOTIONAL, attribute[loopmv]);
    }
    return definitions;
  }

  // Calculator
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQC = 
      ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSC = 
      ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();

  private static final MulticurveDiscountBuildingRepository CURVE_BUILDING_REPOSITORY = 
      CurveCalibrationConventionDataSets.curveBuildingRepositoryMulticurve();

  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getCurvesGBPSonia(ZonedDateTime calibrationDate) {
    InstrumentDefinition<?>[] dscDefinition = 
        getDefinitions(DSC_1_GBP_MARKET_QUOTES, DSC_1_GBP_GENERATORS, DSC_1_GBP_ATTR, calibrationDate);
    InstrumentDefinition<?>[][][] unitsDefinition = new InstrumentDefinition<?>[1][][];
    unitsDefinition[0] = new InstrumentDefinition<?>[][] {dscDefinition };
    return CurveCalibrationTestsUtils.makeCurvesFromDefinitionsMulticurve(calibrationDate, 
        unitsDefinition, GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQC, 
        PSMQCSC, false, DSC_MAP, FWD_ON_MAP, FWD_IBOR_MAP, CURVE_BUILDING_REPOSITORY, 
        TS_FIXED_OIS_GBP_WITH_TODAY, TS_FIXED_OIS_GBP_WITHOUT_TODAY, TS_FIXED_IBOR_GBP3M_WITH_TODAY, TS_FIXED_IBOR_GBP3M_WITHOUT_TODAY);
  }

  /**
   * Returns the array of overnight index used in the curve data set. 
   * @return The array: GBPFEDFUND 
   */
  public static IndexON[] indexONArray() {
    return new IndexON[] {GBPSONIA };
  }

  /**
   * Returns the array of calendars used in the curve data set. 
   * @return The array: NYC 
   */
  public static Calendar[] calendarArray() {
    return new Calendar[] {LONDON };
  }
  
}
