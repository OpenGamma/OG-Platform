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
import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorFRA;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborCompoundingIbor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationConventionDataSets;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationTestsUtils;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
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
 * Curves calibration in USD: 
 * ONDSC-OIS/LIBOR3M-FRAIRS/LIBOR1M-BS/LIBOR6M-BS
 * Recent market data. Standard instruments.
 */
public class RecentDataSetsMulticurveStandardUsd {

  private static final Interpolator1D INTERPOLATOR_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();

  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final Currency USD = Currency.USD;
  private static final FXMatrix FX_MATRIX = new FXMatrix(USD);

  private static final double NOTIONAL = 1.0;

  private static final IndexIborMaster IBOR_MASTER = IndexIborMaster.getInstance();
  private static final GeneratorSwapFixedONMaster GENERATOR_OIS_MASTER = GeneratorSwapFixedONMaster.getInstance();
  private static final GeneratorSwapFixedIborMaster GENERATOR_IRS_MASTER = GeneratorSwapFixedIborMaster.getInstance();

  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GENERATOR_OIS_MASTER.getGenerator("USD1YFEDFUND", NYC);
  private static final IndexON USDFEDFUND = GENERATOR_OIS_USD.getIndex();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_IRS_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  private static final IborIndex USDLIBOR3M = USD6MLIBOR3M.getIborIndex();
  private static final IborIndex USDLIBOR1M = IBOR_MASTER.getIndex("USDLIBOR1M");
  private static final IborIndex USDLIBOR6M = IBOR_MASTER.getIndex("USDLIBOR6M");
  private static final GeneratorDepositIbor GENERATOR_USDLIBOR1M = new GeneratorDepositIbor("GENERATOR_USDLIBOR1M", USDLIBOR1M, NYC);
  private static final GeneratorDepositIbor GENERATOR_USDLIBOR6M = new GeneratorDepositIbor("GENERATOR_USDLIBOR6M", USDLIBOR6M, NYC);
  private static final GeneratorFRA GENERATOR_FRA1M = new GeneratorFRA("GENERATOR_FRA", USDLIBOR1M, NYC);
  private static final GeneratorFRA GENERATOR_FRA6M = new GeneratorFRA("GENERATOR_FRA", USDLIBOR6M, NYC);
  private static final Period P6M = Period.ofMonths(6);
  private static final Period P3M = Period.ofMonths(3);
  private static final GeneratorSwapIborCompoundingIbor USD6MLIBOR3MLIBOR6M = new GeneratorSwapIborCompoundingIbor("USD6MLIBOR3MLIBOR6M", USDLIBOR3M,
      P6M, USDLIBOR6M, NYC, NYC);
  private static final GeneratorSwapIborCompoundingIbor USD3MLIBOR1MLIBOR3M = new GeneratorSwapIborCompoundingIbor("USD3MLIBOR1MLIBOR3M", USDLIBOR1M,
      P3M, USDLIBOR3M, NYC, NYC);

  private static final String CURVE_NAME_DSC_USD = "USD-DSCON-OIS";
  private static final String CURVE_NAME_FWD3_USD = "USD-LIBOR3M-FRAIRS";
  private static final String CURVE_NAME_FWD1_USD = "USD-LIBOR1M-FRABS";
  private static final String CURVE_NAME_FWD6_USD = "USD-LIBOR6M-FRABS";

  /** Data as of 28-Jul-2014 */
  /** Market values for the dsc USD curve */
  private static final double[] DSC_USD_MARKET_QUOTES = new double[] {0.00175, 0.0015,
    0.0009, 0.0009, 0.0010, 0.0011, 0.0013,
    0.0017, 0.0053, 0.0096, 0.0132, 0.0160,
    0.0181, 0.0199, 0.0199, 0.0213, 0.0236 }; //17
  /** Generators for the dsc USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_USD_GENERATORS =
      CurveCalibrationConventionDataSets.generatorUsdOnOisFfs(2, 15, 0);
  /** Tenors for the dsc USD curve */
  private static final Period[] DSC_2_USD_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1),
    Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(6), Period.ofYears(7), Period.ofYears(8), Period.ofYears(9), Period.ofYears(10) };
  private static final GeneratorAttributeIR[] DSC_USD_ATTR = new GeneratorAttributeIR[DSC_2_USD_TENOR.length];
  static {
    for (int loopins = 0; loopins < 2; loopins++) {
      DSC_USD_ATTR[loopins] = new GeneratorAttributeIR(DSC_2_USD_TENOR[loopins], Period.ofDays(0));
    }
    for (int loopins = 2; loopins < DSC_2_USD_TENOR.length; loopins++) {
      DSC_USD_ATTR[loopins] = new GeneratorAttributeIR(DSC_2_USD_TENOR[loopins]);
    }
  }

  /** Market values for the Fwd 3M USD curve */
  private static final double[] FWD3_USD_MARKET_QUOTES = new double[] {0.0023,
    0.0026, 0.0032,
    0.0033, 0.0070, 0.0115, 0.0153, 0.0181,
    0.0222, 0.0260, 0.0277, 0.0295, 0.0310, 0.0318, 0.0320 }; //15
  /** Generators for the Fwd 3M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_USD_GENERATORS =
      CurveCalibrationConventionDataSets.generatorUsdIbor3Fra3Irs3(1, 2, 12);
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] FWD3_2_USD_TENOR = new Period[] {Period.ofMonths(0),
    Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(7), Period.ofYears(10), Period.ofYears(12), Period.ofYears(15), Period.ofYears(20), Period.ofYears(25), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] FWD3_USD_ATTR = new GeneratorAttributeIR[FWD3_2_USD_TENOR.length];
  static {
    for (int loopins = 0; loopins < FWD3_2_USD_TENOR.length; loopins++) {
      FWD3_USD_ATTR[loopins] = new GeneratorAttributeIR(FWD3_2_USD_TENOR[loopins]);
    }
  }

  /** Market values for the Fwd 1M USD curve */
  private static final double[] FWD1_USD_MARKET_QUOTES = new double[] {0.00156,
    0.0015, 0.0015,
    0.0008, 0.0008,
    0.0008, 0.0009, 0.0009, 0.0010, 0.0010,
    0.0009, 0.0008, 0.0008, 0.0007, 0.0006,
    0.0006, 0.0005 }; //15
  /** Generators for the Fwd 3M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD1_USD_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_USDLIBOR1M,
    GENERATOR_FRA1M, GENERATOR_FRA1M,
    USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M,
    USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M,
    USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M,
    USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M };
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] FWD1_2_USD_TENOR = new Period[] {Period.ofMonths(0),
    Period.ofMonths(2), Period.ofMonths(3),
    Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(7), Period.ofYears(10), Period.ofYears(12), Period.ofYears(15), Period.ofYears(20),
    Period.ofYears(25), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] FWD1_USD_ATTR = new GeneratorAttributeIR[FWD1_2_USD_TENOR.length];
  static {
    for (int loopins = 0; loopins < FWD1_2_USD_TENOR.length; loopins++) {
      FWD1_USD_ATTR[loopins] = new GeneratorAttributeIR(FWD1_2_USD_TENOR[loopins]);
    }
  }

  /** Market values for the Fwd 6M USD curve */
  private static final double[] FWD6_USD_MARKET_QUOTES = new double[] {0.003281,
    0.0037,
    0.0008, 0.0008, 0.0008, 0.0008, 0.0008,
    0.0008, 0.0009, 0.0009, 0.0009, 0.0009, 0.0009, 0.0009 };
  /** Generators for the Fwd 3M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD6_USD_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_USDLIBOR6M,
    GENERATOR_FRA6M,
    USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M,
    USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M };
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] FWD6_2_USD_TENOR = new Period[] {Period.ofMonths(0),
    Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(7), Period.ofYears(10), Period.ofYears(12), Period.ofYears(15), Period.ofYears(20), Period.ofYears(25), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] FWD6_USD_ATTR = new GeneratorAttributeIR[FWD6_2_USD_TENOR.length];
  static {
    for (int loopins = 0; loopins < FWD6_2_USD_TENOR.length; loopins++) {
      FWD6_USD_ATTR[loopins] = new GeneratorAttributeIR(FWD6_2_USD_TENOR[loopins]);
    }
  }

  /** Units of curves */
  private static final int NB_UNITS = 4;
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
    GENERATORS_UNITS[0][2] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[0][3] = new GeneratorYDCurve[] {genIntLin };
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_USD };
    NAMES_UNITS[0][1] = new String[] {CURVE_NAME_FWD3_USD };
    NAMES_UNITS[0][2] = new String[] {CURVE_NAME_FWD1_USD };
    NAMES_UNITS[0][3] = new String[] {CURVE_NAME_FWD6_USD };
    DSC_MAP.put(CURVE_NAME_DSC_USD, USD);
    FWD_ON_MAP.put(CURVE_NAME_DSC_USD, new IndexON[] {USDFEDFUND });
    FWD_IBOR_MAP.put(CURVE_NAME_FWD3_USD, new IborIndex[] {USDLIBOR3M });
    FWD_IBOR_MAP.put(CURVE_NAME_FWD1_USD, new IborIndex[] {USDLIBOR1M });
    FWD_IBOR_MAP.put(CURVE_NAME_FWD6_USD, new IborIndex[] {USDLIBOR6M });
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
  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getCurvesUSDOisL1L3L6(ZonedDateTime calibrationDate) {
    InstrumentDefinition<?>[][][] definitionsUnits = new InstrumentDefinition<?>[NB_UNITS][][];
    InstrumentDefinition<?>[] definitionsDsc = getDefinitions(DSC_USD_MARKET_QUOTES, DSC_USD_GENERATORS, DSC_USD_ATTR, calibrationDate);
    InstrumentDefinition<?>[] definitionsFwd3 = getDefinitions(FWD3_USD_MARKET_QUOTES, FWD3_USD_GENERATORS, FWD3_USD_ATTR, calibrationDate);
    InstrumentDefinition<?>[] definitionsFwd1 = getDefinitions(FWD1_USD_MARKET_QUOTES, FWD1_USD_GENERATORS, FWD1_USD_ATTR, calibrationDate);
    InstrumentDefinition<?>[] definitionsFwd6 = getDefinitions(FWD6_USD_MARKET_QUOTES, FWD6_USD_GENERATORS, FWD6_USD_ATTR, calibrationDate);
    definitionsUnits[0] = new InstrumentDefinition<?>[][] {definitionsDsc };
    definitionsUnits[1] = new InstrumentDefinition<?>[][] {definitionsFwd3 };
    definitionsUnits[2] = new InstrumentDefinition<?>[][] {definitionsFwd1 };
    definitionsUnits[3] = new InstrumentDefinition<?>[][] {definitionsFwd6 };
    return makeCurvesFromDefinitions(definitionsUnits, calibrationDate, GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA,
        PSMQC, PSMQCSC, false);
  }

  /**
   * Returns the array of Ibor index used in the curve data set. 
   * @return The array: USDLIBOR1M, USDLIBOR3M, USDLIBOR6M
   */
  public static IborIndex[] indexIborArrayUSDOisL1L3L6() {
    return new IborIndex[] {USDLIBOR1M, USDLIBOR3M, USDLIBOR6M };
  }

  /**
   * Returns the array of overnight index used in the curve data set. 
   * @return The array: USDFEDFUND 
   */
  public static IndexON[] indexONArray() {
    return new IndexON[] {USDFEDFUND };
  }

  /**
   * Returns the array of calendars used in the curve data set. 
   * @return The array: NYC 
   */
  public static Calendar[] calendarArray() {
    return new Calendar[] {NYC };
  }

  /**
   * Returns an array with one time series corresponding to the USD LIBOR3M fixing up to and including the last date.
   * @return
   */
  public static ZonedDateTimeDoubleTimeSeries fixingUsdLibor3MWithLast() {
    return TS_IBOR_USD3M_WITH_LAST;
  }

  /**
   * Returns an array with one time series corresponding to the USD LIBOR3M fixing up to and including the last date.
   * @return
   */
  public static ZonedDateTimeDoubleTimeSeries fixingUsdLibor3MWithoutLast() {
    return TS_IBOR_USD3M_WITHOUT_LAST;
  }

  @SuppressWarnings("unchecked")
  private static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitions(final InstrumentDefinition<?>[][][] definitions,
      final ZonedDateTime calibrationDate, final GeneratorYDCurve[][] curveGenerators,
      final String[][] curveNames, final MulticurveProviderDiscount knownData, final InstrumentDerivativeVisitor<MulticurveProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<MulticurveProviderInterface, MulticurveSensitivity> sensitivityCalculator, final boolean withToday) {
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
          derivatives[k] = convert(definitions[i][j][k], calibrationDate, i, withToday);
          initialGuess[k] = CurveCalibrationTestsUtils.initialGuess(definitions[i][j][k]);
        }
        final GeneratorYDCurve generator = curveGenerators[i][j].finalGenerator(derivatives);
        singleCurves[j] = new SingleCurveBundle<>(curveNames[i][j], derivatives, initialGuess, generator);
      }
      curveBundles[i] = new MultiCurveBundle<>(singleCurves);
    }
    return CURVE_BUILDING_REPOSITORY.makeCurvesFromDerivatives(curveBundles, knownData, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP, calculator,
        sensitivityCalculator);
  }

  private static InstrumentDerivative convert(final InstrumentDefinition<?> definition, final ZonedDateTime date, final int unit, final boolean withToday) {
    InstrumentDerivative ird;
    if (definition instanceof SwapFixedONDefinition) {
      ird = ((SwapFixedONDefinition) definition).toDerivative(date, getTSSwapFixedON(withToday, unit));
    } else {
      if (definition instanceof SwapFixedIborDefinition) {
        ird = ((SwapFixedIborDefinition) definition).toDerivative(date, getTSSwapFixedIbor(withToday, unit));
      } else {
        ird = definition.toDerivative(date);
      }
    }
    return ird;
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
        return withToday ? TS_FIXED_IBOR_USD3M_WITH_LAST : TS_FIXED_IBOR_USD3M_WITHOUT_LAST;
      case 1:
        return withToday ? TS_FIXED_IBOR_USD3M_WITH_LAST : TS_FIXED_IBOR_USD3M_WITHOUT_LAST;
      default:
        throw new IllegalArgumentException(unit.toString());
    }
  }

  private static final ZonedDateTimeDoubleTimeSeries TS_EMPTY = ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC();
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27), DateUtils.getUTCDate(2011, 9, 28) },
      new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_USD_WITH_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITH_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_USD_WITHOUT_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITHOUT_TODAY };

  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITH_LAST = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2014, 7, 1), DateUtils.getUTCDate(2014, 7, 2), DateUtils.getUTCDate(2014, 7, 3), DateUtils.getUTCDate(2014, 7, 4),
        DateUtils.getUTCDate(2014, 7, 7), DateUtils.getUTCDate(2014, 7, 8), DateUtils.getUTCDate(2014, 7, 9), DateUtils.getUTCDate(2014, 7, 10), DateUtils.getUTCDate(2014, 7, 11),
        DateUtils.getUTCDate(2014, 7, 14), DateUtils.getUTCDate(2014, 7, 15), DateUtils.getUTCDate(2014, 7, 16), DateUtils.getUTCDate(2014, 7, 17), DateUtils.getUTCDate(2014, 7, 18),
        DateUtils.getUTCDate(2014, 7, 21), DateUtils.getUTCDate(2014, 7, 22), DateUtils.getUTCDate(2014, 7, 23), DateUtils.getUTCDate(2014, 7, 24), DateUtils.getUTCDate(2014, 7, 25),
        DateUtils.getUTCDate(2014, 7, 28) },
      new double[] {0.2318, 0.2346, 0.2321, 0.2331,
        0.2341, 0.2336, 0.2341, 0.2336, 0.2336,
        0.2326, 0.2331, 0.2336, 0.2336, 0.2316,
        0.2331, 0.2326, 0.2341, 0.2351, 0.2341,
        0.2341 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITHOUT_LAST = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2014, 7, 1), DateUtils.getUTCDate(2014, 7, 2), DateUtils.getUTCDate(2014, 7, 3), DateUtils.getUTCDate(2014, 7, 4),
        DateUtils.getUTCDate(2014, 7, 7), DateUtils.getUTCDate(2014, 7, 8), DateUtils.getUTCDate(2014, 7, 9), DateUtils.getUTCDate(2014, 7, 10), DateUtils.getUTCDate(2014, 7, 11),
        DateUtils.getUTCDate(2014, 7, 14), DateUtils.getUTCDate(2014, 7, 15), DateUtils.getUTCDate(2014, 7, 16), DateUtils.getUTCDate(2014, 7, 17), DateUtils.getUTCDate(2014, 7, 18),
        DateUtils.getUTCDate(2014, 7, 21), DateUtils.getUTCDate(2014, 7, 22), DateUtils.getUTCDate(2014, 7, 23), DateUtils.getUTCDate(2014, 7, 24), DateUtils.getUTCDate(2014, 7, 25) },
      new double[] {0.2318, 0.2346, 0.2321, 0.2331,
        0.2341, 0.2336, 0.2341, 0.2336, 0.2336,
        0.2326, 0.2331, 0.2336, 0.2336, 0.2316,
        0.2331, 0.2326, 0.2341, 0.2351, 0.2341 });

  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_USD3M_WITH_LAST = new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_USD3M_WITH_LAST };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_USD3M_WITHOUT_LAST = new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_USD3M_WITHOUT_LAST };

}
