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
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveAddYield;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveAddYieldExisiting;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveAddYieldFixed;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveDiscountFactorInterpolatedAnchorNode;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveDiscountFactorInterpolatedNumber;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolatedAnchor;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
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
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
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
 * Two curves in EUR; no futures; EONIA curve with ECB meting dates.
 * Two version: without and with TOY jump.
 */
@Test(groups = TestGroup.UNIT)
public class MulticurveBuildingDiscountingDiscountEURCommitteeSpreadTest {

  /** Curve calibration date */
  private static final ZonedDateTime CALIBRATION_DATE = DateUtils.getUTCDate(2012, 11, 14);

  private static final Interpolator1D INTERPOLATOR_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final Interpolator1D INTERPOLATOR_LL = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LOG_LINEAR, Interpolator1DFactory.EXPONENTIAL_EXTRAPOLATOR,
      Interpolator1DFactory.EXPONENTIAL_EXTRAPOLATOR); // Log-linear on the discount factor = step on the instantaneous rates
  private static final Interpolator1D INTERPOLATOR_DQ = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();

  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final Currency EUR = Currency.EUR;
  private static final FXMatrix FX_MATRIX = new FXMatrix(EUR);

  private static final double NOTIONAL = 1.0;

  private static final GeneratorSwapFixedON GENERATOR_OIS_EUR = GeneratorSwapFixedONMaster.getInstance().getGenerator("EUR1YEONIA", TARGET);
  private static final IndexON EONIA = GENERATOR_OIS_EUR.getIndex();
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GENERATOR_SWAP_MASTER.getGenerator("EUR1YEURIBOR6M", TARGET);
  private static final IborIndex EURIBOR6M = EUR1YEURIBOR6M.getIborIndex();

  private static final ZonedDateTime[] MEETING_ECB_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 12, 6), DateUtils.getUTCDate(2013, 1, 10), DateUtils.getUTCDate(2013, 2, 7),
    DateUtils.getUTCDate(2013, 3, 7), DateUtils.getUTCDate(2013, 4, 4), DateUtils.getUTCDate(2013, 5, 2), DateUtils.getUTCDate(2013, 6, 6), DateUtils.getUTCDate(2013, 7, 4),
    DateUtils.getUTCDate(2013, 8, 1), DateUtils.getUTCDate(2013, 9, 5), DateUtils.getUTCDate(2013, 10, 2), DateUtils.getUTCDate(2013, 11, 7) };
  private static final double[] MEETING_ECB_TIME = new double[MEETING_ECB_DATE.length];
  static {
    for (int loopdate = 0; loopdate < MEETING_ECB_DATE.length; loopdate++) {
      MEETING_ECB_TIME[loopdate] = TimeCalculator.getTimeBetween(CALIBRATION_DATE, MEETING_ECB_DATE[loopdate]);
    }
  }

  private static final String CURVE_NAME_DSC_EUR = "EUR Dsc";
  private static final String CURVE_NAME_FWD6_EUR = "EUR Fwd 6M";

  /** Market values for the dsc USD curve */
  private static final double[] DSC_EUR_MARKET_QUOTES = new double[] {0.0050, 0.0050,
    0.0050, 0.0051, 0.0051, 0.0051, 0.0054,
    0.0062, 0.0069, 0.0071, 0.0072, 0.0070,
    0.0074, 0.0076, 0.0100, 0.0110, 0.0120, 0.0110, 0.0150 };
  /** Generators for the dsc USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_EUR_GENERATORS =
      CurveCalibrationConventionDataSets.generatorEurOnOis(2, 17);
  /** Tenors for the dsc USD curve */
  private static final Period[] DSC_EUR_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1),
    Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(4), Period.ofMonths(5),
    Period.ofMonths(6), Period.ofMonths(7), Period.ofMonths(8), Period.ofMonths(9), Period.ofMonths(10),
    Period.ofMonths(11), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
  private static final GeneratorAttributeIR[] DSC_EUR_ATTR = new GeneratorAttributeIR[DSC_EUR_TENOR.length];
  static {
    for (int loopins = 0; loopins < 2; loopins++) {
      DSC_EUR_ATTR[loopins] = new GeneratorAttributeIR(DSC_EUR_TENOR[loopins], Period.ZERO);
    }
    for (int loopins = 2; loopins < DSC_EUR_TENOR.length; loopins++) {
      DSC_EUR_ATTR[loopins] = new GeneratorAttributeIR(DSC_EUR_TENOR[loopins]);
    }
  }

  /** Market values for the Fwd 3M USD curve */
  private static final double[] FWD6_EUR_MARKET_QUOTES = new double[] {0.0150, 0.0150, 0.0150, 0.0150, 0.0150, 0.0150, 0.0175, 0.0175 };
  /** Generators for the Fwd 3M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD6_EUR_GENERATORS =
      CurveCalibrationConventionDataSets.generatorEurIbor6Fra6Irs6(1, 2, 5);
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] FWD6_EUR_TENOR = new Period[] {Period.ofMonths(0), Period.ofMonths(9), Period.ofMonths(12),
    Period.ofYears(2), Period.ofYears(3), Period.ofYears(5), Period.ofYears(7), Period.ofYears(10) };
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
  private static final int[] NB_UNITS = new int[] {2, 2 };
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
    DEFINITIONS_UNITS[1][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_EUR };
    DEFINITIONS_UNITS[1][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD6_EUR };
    final int nbNode1 = 2;
    final GeneratorYDCurve genIntLin = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_LINEAR);
    final GeneratorYDCurve genIntNumDFLL = new GeneratorCurveDiscountFactorInterpolatedNumber(MATURITY_CALCULATOR, nbNode1, INTERPOLATOR_LL);
    final GeneratorYDCurve genInt0DFLL = new GeneratorCurveDiscountFactorInterpolatedAnchorNode(MEETING_ECB_TIME, TimeCalculator.getTimeBetween(CALIBRATION_DATE,
        ScheduleCalculator.getAdjustedDate(CALIBRATION_DATE, GENERATOR_OIS_EUR.getSpotLag(), TARGET)), INTERPOLATOR_LL);
    final GeneratorYDCurve genInt0DQ = new GeneratorCurveYieldInterpolatedAnchor(MATURITY_CALCULATOR, INTERPOLATOR_DQ);
    final GeneratorYDCurve[] genCompArray = new GeneratorYDCurve[] {genIntNumDFLL, genInt0DFLL, genInt0DQ };
    final GeneratorYDCurve genComp = new GeneratorCurveAddYield(genCompArray, false);
    // Describing exogenous bumps (turn-of-year and month)
    final LocalDate[] startExoDates = new LocalDate[] {LocalDate.of(2012, 12, 31), LocalDate.of(2013, 1, 31),
      LocalDate.of(2013, 2, 28), LocalDate.of(2013, 3, 29) };
    final LocalDate[] endExoDates = new LocalDate[] {LocalDate.of(2013, 1, 2), LocalDate.of(2013, 2, 1),
      LocalDate.of(2013, 3, 1), LocalDate.of(2013, 4, 1) };
    final double[] spreadExo = {0.0030, 0.0010, 0.0010, 0.0015 }; // Turn-of-year spread: 30bps
    final double[] times = new double[2 * startExoDates.length];
    final double[] df = new double[2 * startExoDates.length];
    double[] dfExo = new double[startExoDates.length + 1];
    dfExo[0] = 1.0;
    for (int loopdate = 0; loopdate < startExoDates.length; loopdate++) {
      dfExo[loopdate + 1] = dfExo[loopdate] *
          1.0 / (1 + EONIA.getDayCount().getDayCountFraction(startExoDates[loopdate], endExoDates[loopdate]) * spreadExo[loopdate]);
      times[2 * loopdate] = TimeCalculator.getTimeBetween(CALIBRATION_DATE, startExoDates[loopdate]);
      times[2 * loopdate + 1] = TimeCalculator.getTimeBetween(CALIBRATION_DATE, endExoDates[loopdate]);
      df[2 * loopdate] = dfExo[loopdate];
      df[2 * loopdate + 1] = dfExo[loopdate + 1];
    }
    final YieldAndDiscountCurve curveTOY = new DiscountCurve("TOY", new InterpolatedDoublesCurve(times, df, INTERPOLATOR_LINEAR, true));
    final GeneratorYDCurve genAddFixed = new GeneratorCurveAddYieldFixed(genComp, false, curveTOY);

    //    final GeneratorYDCurve genIntDQ = CurveCalibrationConventionDataSets.generatorYDMatDq();
    //    final GeneratorYDCurve genIntNCS = CurveCalibrationConventionDataSets.generatorYDMatNcs();
    final GeneratorYDCurve genIntCCS = CurveCalibrationConventionDataSets.generatorYDMatCcs();
    final GeneratorYDCurve genAddExistDsc = new GeneratorCurveAddYieldExisiting(genIntCCS, false, CURVE_NAME_DSC_EUR);

    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {genComp };
    GENERATORS_UNITS[0][1] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[1][0] = new GeneratorYDCurve[] {genAddFixed };
    GENERATORS_UNITS[1][1] = new GeneratorYDCurve[] {genAddExistDsc };
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_EUR };
    NAMES_UNITS[0][1] = new String[] {CURVE_NAME_FWD6_EUR };
    NAMES_UNITS[1][0] = new String[] {CURVE_NAME_DSC_EUR };
    NAMES_UNITS[1][1] = new String[] {CURVE_NAME_FWD6_EUR };
    DSC_MAP.put(CURVE_NAME_DSC_EUR, EUR);
    FWD_ON_MAP.put(CURVE_NAME_DSC_EUR, new IndexON[] {EONIA });
    FWD_IBOR_MAP.put(CURVE_NAME_FWD6_EUR, new IborIndex[] {EURIBOR6M });
  }

  @SuppressWarnings({"rawtypes", "unchecked" })
  public static InstrumentDefinition<?>[] getDefinitions(final double[] marketQuotes, final GeneratorInstrument[] generators, final GeneratorAttribute[] attribute) {
    final InstrumentDefinition<?>[] definitions = new InstrumentDefinition<?>[marketQuotes.length];
    for (int loopmv = 0; loopmv < marketQuotes.length; loopmv++) {
      definitions[loopmv] = generators[loopmv].generateInstrument(CALIBRATION_DATE, marketQuotes[loopmv], NOTIONAL, attribute[loopmv]);
    }
    return definitions;
  }

  private static List<Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle>> CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK = new ArrayList<>();

  /** Calculators used in curve calibration and testing */
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQDC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSDC = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();
  private static final MulticurveDiscountBuildingRepository CURVE_BUILDING_REPOSITORY =
      CurveCalibrationConventionDataSets.curveBuildingRepositoryMulticurve();

  private static final double TOLERANCE_CAL = 1.0E-9;

  @BeforeSuite
  static void initClass() {
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.add(
          CurveCalibrationTestsUtils.makeCurvesFromDefinitionsMulticurve(CALIBRATION_DATE, DEFINITIONS_UNITS[loopblock],
              GENERATORS_UNITS[loopblock], NAMES_UNITS[loopblock], MULTICURVE_KNOWN_DATA, PSMQDC, PSMQCSDC, false,
              DSC_MAP, FWD_ON_MAP, FWD_IBOR_MAP, CURVE_BUILDING_REPOSITORY,
              TS_FIXED_OIS_EUR_WITH_TODAY, TS_FIXED_OIS_EUR_WITHOUT_TODAY,
              TS_FIXED_IBOR_EUR6M_WITH_TODAY, TS_FIXED_IBOR_EUR6M_WITHOUT_TODAY));
    }
  }

  @Test
  public void curveConstruction() {
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      curveConstructionTest(DEFINITIONS_UNITS[loopblock], CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(loopblock).getFirst(), false, loopblock);
    }
  }

  @Test(enabled = true)
  public void performance() {
    long startTime, endTime;
    final int nbTest = 100;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      CurveCalibrationTestsUtils.makeCurvesFromDefinitionsMulticurve(CALIBRATION_DATE,
          DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], MULTICURVE_KNOWN_DATA, PSMQDC, PSMQCSDC, false,
          DSC_MAP, FWD_ON_MAP, FWD_IBOR_MAP, CURVE_BUILDING_REPOSITORY,
          TS_FIXED_OIS_EUR_WITH_TODAY, TS_FIXED_OIS_EUR_WITHOUT_TODAY,
          TS_FIXED_IBOR_EUR6M_WITH_TODAY, TS_FIXED_IBOR_EUR6M_WITHOUT_TODAY);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction / 2 units: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 1 units: 07-Jan-2013: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 685 ms for 100 sets.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      CurveCalibrationTestsUtils.makeCurvesFromDefinitionsMulticurve(CALIBRATION_DATE,
          DEFINITIONS_UNITS[1], GENERATORS_UNITS[1], NAMES_UNITS[1], MULTICURVE_KNOWN_DATA, PSMQDC, PSMQCSDC, false,
          DSC_MAP, FWD_ON_MAP, FWD_IBOR_MAP, CURVE_BUILDING_REPOSITORY,
          TS_FIXED_OIS_EUR_WITH_TODAY, TS_FIXED_OIS_EUR_WITHOUT_TODAY,
          TS_FIXED_IBOR_EUR6M_WITH_TODAY, TS_FIXED_IBOR_EUR6M_WITHOUT_TODAY);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction / 1 unit + spread: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 1 unit: 07-Jan-2013: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 740 ms for 100 sets.

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

  @Test(enabled = false)
  /** Exports the ON rates computed from the EONIA and EURIBOR6M curves. */
  /** Exports the Ibor rates computed from the EURIBOR6M curve. */
  public void exportForwardRates() {
    int indexBlock = 1;
    CurveCalibrationTestsUtils.exportONForwardONCurve(CALIBRATION_DATE, CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(indexBlock).getFirst(),
        EONIA, TARGET, "demo-test-fwd-eur-committee-on-eonia-" + indexBlock + ".csv", 500, 1);
    CurveCalibrationTestsUtils.exportONForwardIborCurve(CALIBRATION_DATE, CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(indexBlock).getFirst(),
        EURIBOR6M, TARGET, "demo-test-fwd-eur-committee-on-euribor-" + indexBlock + ".csv", 500, 1);
    CurveCalibrationTestsUtils.exportIborForwardIborCurve(CALIBRATION_DATE, CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(indexBlock).getFirst(),
        EURIBOR6M, TARGET, "demo-test-fwd-eur-committee-ibor-euribor-" + indexBlock + ".csv", 0, 500, 1);
    CurveCalibrationTestsUtils.exportZCRatesONCurve(CALIBRATION_DATE, CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(indexBlock).getFirst(),
        EONIA, TARGET, "demo-test-fwd-eur-committee-zc-eonia-" + indexBlock + ".csv", 500, 1);
    CurveCalibrationTestsUtils.exportZCRatesIborCurve(CALIBRATION_DATE, CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(indexBlock).getFirst(),
        EURIBOR6M, TARGET, "demo-test-fwd-eur-committee-zc-euribor-" + indexBlock + ".csv", 500, 1);
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

  private static ZonedDateTimeDoubleTimeSeries[] getTSSwapFixedON(final Boolean withToday) {
    return withToday ? TS_FIXED_OIS_EUR_WITH_TODAY : TS_FIXED_OIS_EUR_WITHOUT_TODAY;
  }

  private static ZonedDateTimeDoubleTimeSeries[] getTSSwapFixedIbor(final Boolean withToday) { // TODO: different fixing for 3 and 6 m
    return withToday ? TS_FIXED_IBOR_EUR6M_WITH_TODAY : TS_FIXED_IBOR_EUR6M_WITHOUT_TODAY;
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

}
