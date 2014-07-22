/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveDiscountFactorInterpolatedNode;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorFRA;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexONMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
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
 * Two curves in EUR; no futures; EONIA curve with ECB meeting dates.
 */
@Test(groups = TestGroup.UNIT)
public class MulticurveBuildingDiscountingDiscountEURCommitteeSimpleTest {

  /** Curve calibration date */
  private static final ZonedDateTime CALIBRATION_DATE = DateUtils.getUTCDate(2013, 2, 4);

  /** Index and curve names */
  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final Currency EUR = Currency.EUR;
  private static final FXMatrix FX_MATRIX = new FXMatrix(EUR);
  private static final IndexON EONIA = IndexONMaster.getInstance().getIndex("EONIA");
  private static final IborIndex EURIBOR6M = IndexIborMaster.getInstance().getIndex("EURIBOR6M");
  private static final String CURVE_NAME_DSC_EUR = "EUR Dsc";
  private static final String CURVE_NAME_FWD6_EUR = "EUR Fwd 6M";

  private static final Interpolator1D INTERPOLATOR_LL = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LOG_LINEAR, Interpolator1DFactory.EXPONENTIAL_EXTRAPOLATOR,
      Interpolator1DFactory.EXPONENTIAL_EXTRAPOLATOR); // Log-linear on the discount factor = step on the instantaneous rates

  private static final GeneratorSwapFixedON GENERATOR_OIS_EUR = GeneratorSwapFixedONMaster.getInstance().getGenerator("EUR1YEONIA", TARGET);
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GENERATOR_SWAP_MASTER.getGenerator("EUR1YEURIBOR6M", TARGET);
  private static final IborIndex EUROLIBOR6M = new IborIndex(EUR, Period.ofMonths(6), 2, EURIBOR6M.getDayCount(), EURIBOR6M.getBusinessDayConvention(), true, "EUROLIBOR6M");
  private static final GeneratorFRA GENERATOR_FRA_6M = new GeneratorFRA("GENERATOR_FRA_6M", EURIBOR6M, TARGET);
  private static final GeneratorDepositIbor GENERATOR_EURIBOR6M = new GeneratorDepositIbor("GENERATOR_EURIBOR6M", EURIBOR6M, TARGET);
  // Test note: Curve building date selected such that ECB dates are in the same OIS month: 7-Mar and 4-Apr
  // Test note: Total of 12 dates
  private static final ZonedDateTime[] MEETING_ECB_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 3, 7), DateUtils.getUTCDate(2013, 4, 4), DateUtils.getUTCDate(2013, 5, 2),
    DateUtils.getUTCDate(2013, 6, 6), DateUtils.getUTCDate(2013, 7, 4), DateUtils.getUTCDate(2013, 8, 1), DateUtils.getUTCDate(2013, 9, 5), DateUtils.getUTCDate(2013, 10, 2),
    DateUtils.getUTCDate(2013, 11, 7), DateUtils.getUTCDate(2013, 12, 5), DateUtils.getUTCDate(2014, 1, 9), DateUtils.getUTCDate(2014, 2, 6) };
  private static final double[] MEETING_ECB_TIME = new double[MEETING_ECB_DATE.length];
  static {
    for (int loopdate = 0; loopdate < MEETING_ECB_DATE.length; loopdate++) {
      MEETING_ECB_TIME[loopdate] = TimeCalculator.getTimeBetween(CALIBRATION_DATE, MEETING_ECB_DATE[loopdate]);
    }
  }

  private static final ZonedDateTimeDoubleTimeSeries TS_EMPTY = ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC();
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_EUR_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_EUR_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_EUR_WITH_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_EUR_WITH_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_EUR_WITHOUT_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_EUR_WITHOUT_TODAY };

  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_EUR6M_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.0035, 0.0036 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_EUR6M_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27) },
      new double[] {0.0035 });

  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_EUR6M_WITH_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_EUR6M_WITH_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_EUR6M_WITHOUT_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_EUR6M_WITHOUT_TODAY };

  /** Market values for the dsc USD curve */
  private static final double[] DSC_EUR_MARKET_QUOTES = new double[] {0.0060, 0.0050, 0.0055, 0.0070, 0.0080, 0.0075, 0.0070, 0.0075, 0.0080, 0.0075, 0.0080, 0.0075 };
  /** Generators for the dsc USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_EUR_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_OIS_EUR, GENERATOR_OIS_EUR,
    GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR,
    GENERATOR_OIS_EUR, GENERATOR_OIS_EUR };
  /** Tenors for the dsc USD curve */
  private static final Period[] DSC_EUR_TENOR = new Period[] {Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(4), Period.ofMonths(5),
    Period.ofMonths(6), Period.ofMonths(7), Period.ofMonths(8), Period.ofMonths(9), Period.ofMonths(10), Period.ofMonths(11), Period.ofYears(1) };
  private static final GeneratorAttributeIR[] DSC_EUR_ATTR = new GeneratorAttributeIR[DSC_EUR_TENOR.length];
  static {
    for (int loopins = 0; loopins < DSC_EUR_TENOR.length; loopins++) {
      DSC_EUR_ATTR[loopins] = new GeneratorAttributeIR(DSC_EUR_TENOR[loopins]);
    }
  }

  /** Market values for the Fwd 3M USD curve */
  private static final double[] FWD6_EUR_MARKET_QUOTES = new double[] {0.0100, 0.0150, 0.0175, 0.0175, 0.0200, 0.00175, 0.0200, 0.00175 };
  /** Generators for the Fwd 3M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD6_EUR_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_EURIBOR6M, GENERATOR_FRA_6M, GENERATOR_FRA_6M, EUR1YEURIBOR6M,
    EUR1YEURIBOR6M, EUR1YEURIBOR6M, EUR1YEURIBOR6M, EUR1YEURIBOR6M };
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] FWD6_EUR_TENOR = new Period[] {Period.ofMonths(0), Period.ofMonths(9), Period.ofMonths(12), Period.ofYears(2), Period.ofYears(3), Period.ofYears(5), Period.ofYears(7),
    Period.ofYears(10) };
  private static final GeneratorAttributeIR[] FWD6_EUR_ATTR = new GeneratorAttributeIR[FWD6_EUR_TENOR.length];
  static {
    for (int loopins = 0; loopins < FWD6_EUR_TENOR.length; loopins++) {
      FWD6_EUR_ATTR[loopins] = new GeneratorAttributeIR(FWD6_EUR_TENOR[loopins]);
    }
  }

  /** Standard USD discounting curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_DSC_EUR;
  /** Standard USD Forward 3M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_FWD6_EUR;

  /** Units of curves */
  private static final int[] NB_UNITS = new int[] {2 };
  private static final int NB_BLOCKS = NB_UNITS.length;
  private static final InstrumentDefinition<?>[][][][] DEFINITIONS_UNITS = new InstrumentDefinition<?>[NB_BLOCKS][][][];
  private static final GeneratorYDCurve[][][] GENERATORS_UNITS = new GeneratorYDCurve[NB_BLOCKS][][];
  private static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];

  private static final MulticurveProviderDiscount MULTICURVE_KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);

  private static final LinkedHashMap<String, Currency> DSC_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IborIndex[]> FWD_IBOR_MAP = new LinkedHashMap<>();

  static {
    DEFINITIONS_DSC_EUR = getDefinitions(DSC_EUR_MARKET_QUOTES, DSC_EUR_GENERATORS, DSC_EUR_ATTR);
    DEFINITIONS_FWD6_EUR = getDefinitions(FWD6_EUR_MARKET_QUOTES, FWD6_EUR_GENERATORS, FWD6_EUR_ATTR);
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      DEFINITIONS_UNITS[loopblock] = new InstrumentDefinition<?>[NB_UNITS[loopblock]][][];
      GENERATORS_UNITS[loopblock] = new GeneratorYDCurve[NB_UNITS[loopblock]][];
      NAMES_UNITS[loopblock] = new String[NB_UNITS[loopblock]][];
    }
    DEFINITIONS_UNITS[0][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_EUR };
    DEFINITIONS_UNITS[0][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD6_EUR };
    final GeneratorYDCurve genIntLinMat = CurveCalibrationConventionDataSets.generatorYDMatLin();
    final GeneratorYDCurve genIntDFLL = new GeneratorCurveDiscountFactorInterpolatedNode(MEETING_ECB_TIME, INTERPOLATOR_LL);
    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {genIntDFLL };
    GENERATORS_UNITS[0][1] = new GeneratorYDCurve[] {genIntLinMat };
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_EUR };
    NAMES_UNITS[0][1] = new String[] {CURVE_NAME_FWD6_EUR };
    DSC_MAP.put(CURVE_NAME_DSC_EUR, EUR);
    FWD_ON_MAP.put(CURVE_NAME_DSC_EUR, new IndexON[] {EONIA });
    FWD_IBOR_MAP.put(CURVE_NAME_FWD6_EUR, new IborIndex[] {EURIBOR6M, EUROLIBOR6M });
  }

  @SuppressWarnings({"rawtypes", "unchecked" })
  public static InstrumentDefinition<?>[] getDefinitions(final double[] marketQuotes, final GeneratorInstrument[] generators, final GeneratorAttribute[] attribute) {
    final InstrumentDefinition<?>[] definitions = new InstrumentDefinition<?>[marketQuotes.length];
    for (int loopmv = 0; loopmv < marketQuotes.length; loopmv++) {
      definitions[loopmv] = generators[loopmv].generateInstrument(CALIBRATION_DATE, marketQuotes[loopmv], 1.0, attribute[loopmv]);
    }
    return definitions;
  }

  private static List<Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle>> CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK = new ArrayList<>();

  // Calculator
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQDC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSDC = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();

  private static final MulticurveDiscountBuildingRepository CURVE_BUILDING_REPOSITORY =
      CurveCalibrationConventionDataSets.curveBuildingRepositoryMulticurve();

  private static final double TOLERANCE_CAL = 1.0E-9;

  @BeforeSuite
  static void initClass() {
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.add(makeCurvesFromDefinitions(DEFINITIONS_UNITS[loopblock], GENERATORS_UNITS[loopblock], NAMES_UNITS[loopblock], MULTICURVE_KNOWN_DATA, PSMQDC,
          PSMQCSDC, false));
    }
  }

  @Test
  public void curveConstruction() {
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      curveConstructionTest(DEFINITIONS_UNITS[loopblock], CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(loopblock).getFirst(), false, loopblock);
    }
    assertEquals("Curve construction", CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0).getFirst().getCurve(EURIBOR6M), CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0).getFirst().getCurve(EUROLIBOR6M));
  }

  private void curveConstructionTest(final InstrumentDefinition<?>[][][] definitions, final MulticurveProviderDiscount curves, final boolean withToday, final int block) {
    final int nbBlocks = definitions.length;
    for (int loopblock = 0; loopblock < nbBlocks; loopblock++) {
      final InstrumentDerivative[][] instruments = convert(definitions[loopblock], withToday);
      final double[][] pv = new double[instruments.length][];
      for (int loopcurve = 0; loopcurve < instruments.length; loopcurve++) {
        pv[loopcurve] = new double[instruments[loopcurve].length];
        for (int loopins = 0; loopins < instruments[loopcurve].length; loopins++) {
          pv[loopcurve][loopins] = curves.getFxRates().convert(instruments[loopcurve][loopins].accept(PVDC, curves), EUR).getAmount();
          assertEquals("Curve construction: block " + block + ", unit " + loopblock + " - instrument " + loopins, 0, pv[loopcurve][loopins], TOLERANCE_CAL);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitions(final InstrumentDefinition<?>[][][] definitions, final GeneratorYDCurve[][] curveGenerators,
      final String[][] curveNames, final MulticurveProviderDiscount knownData, final InstrumentDerivativeVisitor<MulticurveProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<MulticurveProviderInterface, MulticurveSensitivity> sensitivityCalculator, final boolean withToday) {
    final int nUnits = definitions.length;
    final MultiCurveBundle<GeneratorYDCurve>[] curveBundles = new MultiCurveBundle[nUnits];
    for (int i = 0; i < nUnits; i++) {
      final int nCurves = definitions[i].length;
      final SingleCurveBundle<GeneratorYDCurve>[] singleCurves = new SingleCurveBundle[nCurves];
      for (int j = 0; j < nCurves; j++) {
        final int nInstruments = definitions[i][j].length;
        final InstrumentDerivative[] derivatives = new InstrumentDerivative[nInstruments];
        final double[] rates = new double[nInstruments];
        for (int k = 0; k < nInstruments; k++) {
          derivatives[k] = convert(definitions[i][j][k], withToday);
          rates[k] = initialRateGuess(definitions[i][j][k]);
        }
        final GeneratorYDCurve generator = curveGenerators[i][j].finalGenerator(derivatives);
        final double[] initialGuess = generator.initialGuess(rates);
        singleCurves[j] = new SingleCurveBundle<>(curveNames[i][j], derivatives, initialGuess, generator);
      }
      curveBundles[i] = new MultiCurveBundle<>(singleCurves);
    }
    return CURVE_BUILDING_REPOSITORY.makeCurvesFromDerivatives(curveBundles, knownData, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP, calculator, sensitivityCalculator);
  }

  private static InstrumentDerivative[][] convert(final InstrumentDefinition<?>[][] definitions, final boolean withToday) {
    final InstrumentDerivative[][] instruments = new InstrumentDerivative[definitions.length][];
    for (int loopcurve = 0; loopcurve < definitions.length; loopcurve++) {
      instruments[loopcurve] = new InstrumentDerivative[definitions[loopcurve].length];
      int loopins = 0;
      for (final InstrumentDefinition<?> instrument : definitions[loopcurve]) {
        InstrumentDerivative ird;
        if (instrument instanceof SwapFixedONDefinition) {
          ird = ((SwapFixedONDefinition) instrument).toDerivative(CALIBRATION_DATE, getTSSwapFixedON(withToday));
        } else {
          if (instrument instanceof SwapFixedIborDefinition) {
            ird = ((SwapFixedIborDefinition) instrument).toDerivative(CALIBRATION_DATE, getTSSwapFixedIbor(withToday));
          } else {
            if (instrument instanceof InterestRateFutureTransactionDefinition) {
              ird = ((InterestRateFutureTransactionDefinition) instrument).toDerivative(CALIBRATION_DATE, 0.0); // Trade date = today, reference price not used.
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

  private static InstrumentDerivative convert(final InstrumentDefinition<?> instrument, final boolean withToday) {
    InstrumentDerivative ird;
    if (instrument instanceof SwapFixedONDefinition) {
      ird = ((SwapFixedONDefinition) instrument).toDerivative(CALIBRATION_DATE, getTSSwapFixedON(withToday));
    } else {
      if (instrument instanceof SwapFixedIborDefinition) {
        ird = ((SwapFixedIborDefinition) instrument).toDerivative(CALIBRATION_DATE, getTSSwapFixedIbor(withToday));
      } else {
        if (instrument instanceof InterestRateFutureTransactionDefinition) {
          ird = ((InterestRateFutureTransactionDefinition) instrument).toDerivative(CALIBRATION_DATE, 0.0); // Trade date = today, reference price not used.
        } else {
          ird = instrument.toDerivative(CALIBRATION_DATE);
        }
      }
    }
    return ird;
  }

  private static ZonedDateTimeDoubleTimeSeries[] getTSSwapFixedON(final Boolean withToday) {
    return withToday ? TS_FIXED_OIS_EUR_WITH_TODAY : TS_FIXED_OIS_EUR_WITHOUT_TODAY;
  }

  private static ZonedDateTimeDoubleTimeSeries[] getTSSwapFixedIbor(final Boolean withToday) { // TODO: different fixing for 3 and 6 m
    return withToday ? TS_FIXED_IBOR_EUR6M_WITH_TODAY : TS_FIXED_IBOR_EUR6M_WITHOUT_TODAY;
  }

  private static double initialRateGuess(final InstrumentDefinition<?> instrument) {
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
    if (instrument instanceof InterestRateFutureTransactionDefinition) {
      return 1 - ((InterestRateFutureTransactionDefinition) instrument).getTradePrice();
    }
    return 0.01;
  }

}
