/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.datasets;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.datasets.CalendarTarget;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositON;
import com.opengamma.analytics.financial.instrument.index.GeneratorFRA;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
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
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
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
 * Curves calibration in EUR: 
 * 0) ONDSC-OIS/EURIBOR3M-FRAIRS
 * Data stored in snapshots for comparison with platform.
 */
public class StandardDataSetsMulticurveEUR {

  private static final ZonedDateTime[] REFERENCE_DATE = new ZonedDateTime[2];
  static {
    REFERENCE_DATE[0] = DateUtils.getUTCDate(2014, 2, 18);
  }

  private static final Interpolator1D INTERPOLATOR_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();
  private static final double TOLERANCE_ROOT = 1.0E-10;
  private static final int STEP_MAX = 100;

  private static final Calendar TARGET = new CalendarTarget("TARGET");
  private static final Currency EUR = Currency.EUR;
  private static final FXMatrix FX_MATRIX = new FXMatrix(EUR);

  private static final double NOTIONAL = 1.0;

  private static final IndexIborMaster IBOR_MASTER = IndexIborMaster.getInstance();
  private static final GeneratorSwapFixedONMaster GENERATOR_OIS_MASTER = GeneratorSwapFixedONMaster.getInstance();
  private static final GeneratorSwapFixedIborMaster GENERATOR_IRS_MASTER = GeneratorSwapFixedIborMaster.getInstance();

  private static final GeneratorSwapFixedON GENERATOR_OIS_EUR = GENERATOR_OIS_MASTER.getGenerator("EUR1YEONIA", TARGET);
  private static final IndexON EUREONIA = GENERATOR_OIS_EUR.getIndex();
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_EUR = new GeneratorDepositON("EUR Deposit ON", EUR, TARGET, EUREONIA.getDayCount());
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = GENERATOR_IRS_MASTER.getGenerator("EUR1YEURIBOR3M", TARGET);
  private static final IborIndex EUREURIBOR3M = EUR1YEURIBOR3M.getIborIndex();
  private static final IborIndex EUREURIBOR6M = IBOR_MASTER.getIndex("EURIBOR6M");
  private static final GeneratorDepositIbor GENERATOR_EUREURIBOR3M = new GeneratorDepositIbor("GENERATOR_EUREURIBOR3M", EUREURIBOR3M, TARGET);
  //  private static final GeneratorDepositIbor GENERATOR_EUREURIBOR6M = new GeneratorDepositIbor("GENERATOR_EUREURIBOR6M", EUREURIBOR6M, TARGET);
  private static final GeneratorFRA GENERATOR_FRA3M = new GeneratorFRA("GENERATOR_FRA", EUREURIBOR3M, TARGET);
  //  private static final GeneratorFRA GENERATOR_FRA6M = new GeneratorFRA("GENERATOR_FRA", EUREURIBOR6M, TARGET);

  private static final ZonedDateTimeDoubleTimeSeries TS_EMPTY = ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC();
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_EUR_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_EUR_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_EUR_WITH_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_EUR_WITH_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_EUR_WITHOUT_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_EUR_WITHOUT_TODAY };

  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_EUR3M_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.0035, 0.0036 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_EUR3M_WITHOUT_TO = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27) },
      new double[] {0.0035 });

  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_EUR3M_WITH_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_EUR3M_WITH_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_EUR3M_WITHOUT_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_EUR3M_WITHOUT_TO };

  private static final String CURVE_NAME_DSC_EUR = "EUR-DSCON-OIS";
  private static final String CURVE_NAME_FWD3_EUR = "EUR-EURIBOR3M-FRAIRS";
  private static final String CURVE_NAME_FWD6_EUR = "EUR-EURIBOR6M-FRAIRS";

  /** Data for 2014-02-18 **/
  /** Market values for the dsc EUR curve */
  private static final double[] DSC_1_EUR_MARKET_QUOTES = new double[] {0.000975, 0.00225,
    0.001665, 0.001535, 0.001425, 0.00119, 0.00112,
    0.0010555, 0.00143, 0.00249, 0.00428, 0.006325,
    0.01037, 0.01549, 0.0178, 0.020365 }; //16
  /** Generators for the dsc EUR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_1_EUR_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_EUR, GENERATOR_DEPOSIT_ON_EUR,
    GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR,
    GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR,
    GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR };
  /** Tenors for the dsc EUR curve */
  private static final Period[] DSC_1_EUR_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1),
    Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(7), Period.ofYears(10), Period.ofYears(12), Period.ofYears(15) };
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
  private static final double[] FWD3_1_EUR_MARKET_QUOTES = new double[] {0.00287,
    0.00242, 0.00244,
    0.002604, 0.0030775, 0.0044555, 0.0064095, 0.0085545,
    0.010655, 0.012655, 0.01448, 0.016122, 0.017565,
    0.0199725, 0.022365, 0.0241245, 0.024807 }; // 17
  /** Generators for the Fwd 3M EUR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_1_EUR_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_EUREURIBOR3M,
    GENERATOR_FRA3M, GENERATOR_FRA3M,
    EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M,
    EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M,
    EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M };
  /** Tenors for the Fwd 3M EUR curve */
  private static final Period[] FWD3_1_EUR_TENOR = new Period[] {Period.ofMonths(0),
    Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(6), Period.ofYears(7), Period.ofYears(8), Period.ofYears(9), Period.ofYears(10),
    Period.ofYears(12), Period.ofYears(15), Period.ofYears(20), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] FWD3_1_USD_ATTR = new GeneratorAttributeIR[FWD3_1_EUR_TENOR.length];
  static {
    for (int loopins = 0; loopins < FWD3_1_EUR_TENOR.length; loopins++) {
      FWD3_1_USD_ATTR[loopins] = new GeneratorAttributeIR(FWD3_1_EUR_TENOR[loopins]);
    }
  }

  /** Standard USD discounting curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_DSC_1_EUR;
  /** Standard USD Forward 3M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_FWD3_1_EUR;

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
    DEFINITIONS_DSC_1_EUR = getDefinitions(DSC_1_EUR_MARKET_QUOTES, DSC_1_EUR_GENERATORS, DSC_1_EUR_ATTR, REFERENCE_DATE[0]);
    DEFINITIONS_FWD3_1_EUR = getDefinitions(FWD3_1_EUR_MARKET_QUOTES, FWD3_1_EUR_GENERATORS, FWD3_1_USD_ATTR, REFERENCE_DATE[0]);
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      DEFINITIONS_UNITS[loopblock] = new InstrumentDefinition<?>[NB_UNITS[loopblock]][][];
      GENERATORS_UNITS[loopblock] = new GeneratorYDCurve[NB_UNITS[loopblock]][];
      NAMES_UNITS[loopblock] = new String[NB_UNITS[loopblock]][];
    }
    DEFINITIONS_UNITS[0][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_1_EUR };
    DEFINITIONS_UNITS[0][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_1_EUR };
    final GeneratorYDCurve genIntLin = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_LINEAR);
    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[0][1] = new GeneratorYDCurve[] {genIntLin };
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_EUR };
    NAMES_UNITS[0][1] = new String[] {CURVE_NAME_FWD3_EUR };
    DSC_MAP.put(CURVE_NAME_DSC_EUR, EUR);
    FWD_ON_MAP.put(CURVE_NAME_DSC_EUR, new IndexON[] {EUREONIA });
    FWD_IBOR_MAP.put(CURVE_NAME_FWD3_EUR, new IborIndex[] {EUREURIBOR3M });
    FWD_IBOR_MAP.put(CURVE_NAME_FWD6_EUR, new IborIndex[] {EUREURIBOR6M });
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

  // Calculator
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSC = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();

  private static final MulticurveDiscountBuildingRepository CURVE_BUILDING_REPOSITORY = new MulticurveDiscountBuildingRepository(TOLERANCE_ROOT, TOLERANCE_ROOT, STEP_MAX);

  private static final List<Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle>> CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK = new ArrayList<>();
  static {
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.add(makeCurvesFromDefinitions(DEFINITIONS_UNITS[loopblock], REFERENCE_DATE[loopblock], GENERATORS_UNITS[loopblock], NAMES_UNITS[loopblock], KNOWN_DATA,
          PSMQC, PSMQCSC, false));
    }
  }

  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getCurvesUSDOisL3() {
    return CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0);
  }

  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getCurvesUSDOisL1L3L6() {
    return CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(1);
  }

  /**
   * Returns the array of Ibor index used in the curve data set. 
   * @return The array: EUREURIBOR3M 
   */
  public static IborIndex[] indexIborArrayEUROisE3() {
    return new IborIndex[] {EUREURIBOR3M };
  }

  /**
   * Returns the array of Ibor index used in the curve data set. 
   * @return The array: EUREURIBOR3M, EUREURIBOR6M
   */
  public static IborIndex[] indexIborArrayEUROisE3E6() {
    return new IborIndex[] {EUREURIBOR3M, EUREURIBOR6M };
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

  @SuppressWarnings("unchecked")
  private static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitions(final InstrumentDefinition<?>[][][] definitions,
      final ZonedDateTime calibrationDate, final GeneratorYDCurve[][] curveGenerators,
      final String[][] curveNames, final MulticurveProviderDiscount knownData, final InstrumentDerivativeVisitor<ParameterProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<ParameterProviderInterface, MulticurveSensitivity> sensitivityCalculator, final boolean withToday) {
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
        return withToday ? TS_FIXED_OIS_EUR_WITH_TODAY : TS_FIXED_OIS_EUR_WITHOUT_TODAY;
      default:
        throw new IllegalArgumentException(unit.toString());
    }
  }

  private static ZonedDateTimeDoubleTimeSeries[] getTSSwapFixedIbor(final Boolean withToday, final Integer unit) {
    switch (unit) {
      case 0:
        return withToday ? TS_FIXED_IBOR_EUR3M_WITH_TODAY : TS_FIXED_IBOR_EUR3M_WITHOUT_TODAY;
      case 1:
        return withToday ? TS_FIXED_IBOR_EUR3M_WITH_TODAY : TS_FIXED_IBOR_EUR3M_WITHOUT_TODAY;
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
