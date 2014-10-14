/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.curve.inflation.generator.GeneratorPriceIndexCurve;
import com.opengamma.analytics.financial.curve.inflation.generator.GeneratorPriceIndexCurveAddSeasonality;
import com.opengamma.analytics.financial.curve.inflation.generator.GeneratorPriceIndexCurveInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurve;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositON;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedInflationMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedInflationZeroCoupon;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationZeroCouponDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurveSimple;
import com.opengamma.analytics.financial.model.interestrate.curve.SeasonalCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.calculator.generic.LastFixingEndTimeCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.MarketQuoteInflationSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.ParSpreadInflationMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueCurveSensitivityDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.curve.inflation.InflationDiscountBuildingRepositoryWithDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterInflationSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
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
 *  Build of inflation curve(including seasonality) and discount curve simultaneously in several blocks with relevant Jacobian matrices.
 */
@Test(groups = TestGroup.UNIT)
public class InflationBuildingCurveWithDiscountAndSeasonalityTestUS {

  private static final Interpolator1D INTERPOLATOR_LOG_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LOG_LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final LastFixingEndTimeCalculator MATURITY_CALCULATOR_INFLATION = LastFixingEndTimeCalculator.getInstance();
  private static final LastTimeCalculator MATURITY_CALCULATOR_DISCOUNT = LastTimeCalculator.getInstance();
  private static final double TOLERANCE_ROOT = 1.0E-10;
  private static final int STEP_MAX = 100;

  private static final Currency USD = Currency.USD;
  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final FXMatrix FX_MATRIX = new FXMatrix(USD);

  private static final double NOTIONAL = 1.0;

  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GeneratorSwapFixedONMaster.getInstance().getGenerator("USD1YFEDFUND", NYC);
  private static final IndexON INDEX_ON_USD = GENERATOR_OIS_USD.getIndex();
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_USD = new GeneratorDepositON("USD Deposit ON", USD, NYC, INDEX_ON_USD.getDayCount());

  private static final GeneratorSwapFixedInflationZeroCoupon GENERATOR_INFLATION_SWAP = GeneratorSwapFixedInflationMaster.getInstance().getGenerator("USCPI");
  private static final IndexPrice US_CPI = GENERATOR_INFLATION_SWAP.getIndexPrice();

  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2012, 9, 28);

  private static final ZonedDateTimeDoubleTimeSeries TS_PRICE_INDEX_USD_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28), DateUtils.getUTCDate(2012, 6, 30), DateUtils.getUTCDate(2012, 7, 31) }, new double[] {200, 200, 200, 200 });
  private static final String CURVE_NAME_DSC_USD = "USD Dsc";
  private static final String CURVE_NAME_CPI_USD = "USD CPI";

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

  /** Market values for the CPI USD curve */
  public static final double[] CPI_USD_MARKET_QUOTES = new double[] {0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.0200 };
  /** Generators for the CPI USD curve */
  public static final GeneratorInstrument<? extends GeneratorAttribute>[] CPI_USD_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_INFLATION_SWAP, GENERATOR_INFLATION_SWAP,
    GENERATOR_INFLATION_SWAP,
    GENERATOR_INFLATION_SWAP, GENERATOR_INFLATION_SWAP, GENERATOR_INFLATION_SWAP, GENERATOR_INFLATION_SWAP, GENERATOR_INFLATION_SWAP, GENERATOR_INFLATION_SWAP,
    GENERATOR_INFLATION_SWAP, GENERATOR_INFLATION_SWAP, GENERATOR_INFLATION_SWAP, GENERATOR_INFLATION_SWAP, GENERATOR_INFLATION_SWAP, GENERATOR_INFLATION_SWAP };
  /** Tenors for the CPI USD curve */
  public static final Period[] CPI_USD_TENOR = new Period[] {Period.ofYears(1),
    Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(6), Period.ofYears(7),
    Period.ofYears(8), Period.ofYears(9), Period.ofYears(10), Period.ofYears(12), Period.ofYears(15), Period.ofYears(20),
    Period.ofYears(25), Period.ofYears(30) };
  public static final GeneratorAttributeIR[] CPI_USD_ATTR = new GeneratorAttributeIR[CPI_USD_TENOR.length];
  static {
    for (int loopins = 0; loopins < CPI_USD_TENOR.length; loopins++) {
      CPI_USD_ATTR[loopins] = new GeneratorAttributeIR(CPI_USD_TENOR[loopins]);
    }
  }
  public static final ZonedDateTime NOW_MINUS_3MONTH = DateUtils.getUTCDate(2012, 6, 28);
  public static final ZonedDateTime[] seasonalityDate = ScheduleCalculator.getUnadjustedDateSchedule(NOW.withDayOfMonth(1), NOW.withDayOfMonth(1).plusYears(30), Period.ofMonths(1), true, false);
  public static final double[] seasonalStep = new double[seasonalityDate.length];
  static {
    for (int loopins = 0; loopins < seasonalityDate.length; loopins++) {
      seasonalStep[loopins] = TimeCalculator.getTimeBetween(NOW, seasonalityDate[loopins]);
    }
  }

  public static final double[] seasonalFactors = {1.005, 1.001, 1.01, .999, .998, .9997, 1.004, 1.006, .994, .993, .9991 };
  /*public static final double[] seasonalFactors = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 };*/
  final static SeasonalCurve SEASONAL_CURVE = new SeasonalCurve(seasonalStep, seasonalFactors, false);
  /** Standard USD discounting curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_DSC_USD;

  /** Standard USD CPI curve instrument definitions */
  public static final InstrumentDefinition<?>[] DEFINITIONS_CPI_USD;

  /** Units of curves */
  public static final int[] NB_UNITS = new int[] {2, 1 };
  public static final int NB_BLOCKS = NB_UNITS.length;
  public static final InstrumentDefinition<?>[][][][] DEFINITIONS_UNITS = new InstrumentDefinition<?>[NB_BLOCKS][][][];
  public static final GeneratorCurve[][][] GENERATORS_UNITS = new GeneratorCurve[NB_BLOCKS][][];
  public static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];

  private static final InflationProviderDiscount KNOWN_DATA = new InflationProviderDiscount(FX_MATRIX);

  private static final LinkedHashMap<String, Currency> DSC_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP = new LinkedHashMap<>();
  public static final LinkedHashMap<String, IndexPrice[]> US_CPI_MAP = new LinkedHashMap<>();

  static {
    DEFINITIONS_DSC_USD = getDefinitions(DSC_USD_MARKET_QUOTES, DSC_USD_GENERATORS, DSC_USD_ATTR);
    DEFINITIONS_CPI_USD = getDefinitions(CPI_USD_MARKET_QUOTES, CPI_USD_GENERATORS, CPI_USD_ATTR);

    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      DEFINITIONS_UNITS[loopblock] = new InstrumentDefinition<?>[NB_UNITS[loopblock]][][];
      GENERATORS_UNITS[loopblock] = new GeneratorCurve[NB_UNITS[loopblock]][];
      NAMES_UNITS[loopblock] = new String[NB_UNITS[loopblock]][];
    }

    DEFINITIONS_UNITS[0][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD };
    DEFINITIONS_UNITS[0][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_CPI_USD };
    DEFINITIONS_UNITS[1][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD, DEFINITIONS_CPI_USD };

    final GeneratorYDCurve genIntLinDiscount = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR_DISCOUNT, INTERPOLATOR_LOG_LINEAR);
    final GeneratorPriceIndexCurve gen = new GeneratorPriceIndexCurveInterpolated(MATURITY_CALCULATOR_INFLATION, INTERPOLATOR_LOG_LINEAR);
    final GeneratorPriceIndexCurve genIntLinInflation = new GeneratorPriceIndexCurveAddSeasonality(gen, SEASONAL_CURVE);

    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {genIntLinDiscount };
    GENERATORS_UNITS[0][1] = new GeneratorPriceIndexCurve[] {genIntLinInflation };
    GENERATORS_UNITS[1][0] = new GeneratorCurve[] {genIntLinDiscount, genIntLinInflation };

    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_USD };
    NAMES_UNITS[0][1] = new String[] {CURVE_NAME_CPI_USD };
    NAMES_UNITS[1][0] = new String[] {CURVE_NAME_DSC_USD, CURVE_NAME_CPI_USD };

    DSC_MAP.put(CURVE_NAME_DSC_USD, USD);
    FWD_ON_MAP.put(CURVE_NAME_DSC_USD, new IndexON[] {INDEX_ON_USD });
    US_CPI_MAP.put(CURVE_NAME_CPI_USD, new IndexPrice[] {US_CPI });
  }

  @SuppressWarnings({"rawtypes", "unchecked" })
  public static InstrumentDefinition<?>[] getDefinitions(final double[] marketQuotes, final GeneratorInstrument[] generators, final GeneratorAttribute[] attribute) {
    final InstrumentDefinition<?>[] definitions = new InstrumentDefinition<?>[marketQuotes.length];
    for (int loopmv = 0; loopmv < marketQuotes.length; loopmv++) {
      definitions[loopmv] = generators[loopmv].generateInstrument(NOW, marketQuotes[loopmv], NOTIONAL, attribute[loopmv]);
    }
    return definitions;
  }

  private static List<Pair<InflationProviderDiscount, CurveBuildingBlockBundle>> CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK = new ArrayList<>();

  // Calculator
  private static final PresentValueDiscountingInflationCalculator PVIC = PresentValueDiscountingInflationCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingInflationCalculator PVCSDIC = PresentValueCurveSensitivityDiscountingInflationCalculator.getInstance();
  private static final ParSpreadInflationMarketQuoteDiscountingCalculator PSIMQC = ParSpreadInflationMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator PSIMQCSC = ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();

  private static final InflationDiscountBuildingRepositoryWithDiscount CURVE_BUILDING_REPOSITORY = new InflationDiscountBuildingRepositoryWithDiscount(TOLERANCE_ROOT, TOLERANCE_ROOT, STEP_MAX);

  private static final double TOLERANCE_CAL = 1.0E-9;

  @BeforeSuite
  static void initClass() {
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.add(makeCurvesFromDefinitions(DEFINITIONS_UNITS[loopblock], GENERATORS_UNITS[loopblock], NAMES_UNITS[loopblock], KNOWN_DATA, PSIMQC, PSIMQCSC));
    }
  }

  @Test(enabled = true)
  public void comparison1Unit2Units() {
    final InflationProviderDiscount[] units = new InflationProviderDiscount[2];
    final CurveBuildingBlockBundle[] bb = new CurveBuildingBlockBundle[2];
    final YieldAndDiscountCurve[] curveDsc = new YieldAndDiscountCurve[2];
    final PriceIndexCurve[] curveInflation = new PriceIndexCurve[2];

    for (int loopblock = 0; loopblock < 2; loopblock++) {
      units[loopblock] = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(loopblock).getFirst();
      bb[loopblock] = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(loopblock).getSecond();
      curveDsc[loopblock] = units[loopblock].getCurve(USD);
      curveInflation[loopblock] = units[loopblock].getCurve(US_CPI);

    }
    assertEquals("Curve construction: 1 unit / 3 units ", curveDsc[0].getNumberOfParameters(), curveDsc[1].getNumberOfParameters());
    assertEquals("Curve construction: 1 unit / 3 units ", curveInflation[0].getNumberOfParameters(), curveInflation[1].getNumberOfParameters());

    assertArrayEquals("Curve construction: 1 unit / 3 units ", ArrayUtils.toPrimitive(((YieldCurve) curveDsc[0]).getCurve().getXData()),
        ArrayUtils.toPrimitive(((YieldCurve) curveDsc[1]).getCurve().getXData()), TOLERANCE_CAL);
    assertArrayEquals("Curve construction: 1 unit / 3 units ", ArrayUtils.toPrimitive(((YieldCurve) curveDsc[0]).getCurve().getYData()),
        ArrayUtils.toPrimitive(((YieldCurve) curveDsc[1]).getCurve().getYData()), TOLERANCE_CAL);
    assertArrayEquals("Curve construction: 1 unit / 3 units ", ArrayUtils.toPrimitive(((PriceIndexCurveSimple)curveInflation[0]).getCurve().getXData()),
        ArrayUtils.toPrimitive(((PriceIndexCurveSimple)curveInflation[1]).getCurve().getXData()), TOLERANCE_CAL);
    assertArrayEquals("Curve construction: 1 unit / 3 units ", ArrayUtils.toPrimitive(((PriceIndexCurveSimple)curveInflation[0]).getCurve().getYData()),
        ArrayUtils.toPrimitive(((PriceIndexCurveSimple)curveInflation[1]).getCurve().getYData()), TOLERANCE_CAL);
  }

  @Test(enabled = false)
  public void performance() {
    long startTime, endTime;
    final int nbTest = 1000;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      makeCurvesFromDefinitions(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSIMQC, PSIMQCSC);
    }
    endTime = System.currentTimeMillis();
    System.out.println("MulticurveBuildingDiscountingDiscountXCcyTest - " + nbTest + " curve construction / USD/EUR 3 units: " + (endTime - startTime) + " ms");
    // Performance note: curve construction Price index EUR and discount EUR 1 units: 27-Mar-13: On Dell Precision T1850 3.5 GHz Quad-Core Intel Xeon: 5869 ms for 1000 sets.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      makeCurvesFromDefinitions(DEFINITIONS_UNITS[1], GENERATORS_UNITS[1], NAMES_UNITS[1], KNOWN_DATA, PSIMQC, PSIMQCSC);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction / 1 unit: " + (endTime - startTime) + " ms");
    // Performance note: curve construction Price index EUR and discount EUR 1 units: 27-Mar-13: On Dell Precision T1850 3.5 GHz Quad-Core Intel Xeon: 9153 ms for 1000 sets.

  }

  @Test
  public void curveConstructionGeneratorOtherBlocks() {
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      curveConstructionTest(DEFINITIONS_UNITS[loopblock], CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(loopblock).getFirst(), loopblock);
    }
  }

  @Test(enabled = true)
  /**
   * Analyzes the shape of the forward curve.
   */
  public void marketQuoteSensitivityAnalysis() {

    final InflationProviderDiscount multicurves7 = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(1).getFirst();
    multicurves7.setAll(CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0).getFirst());
    final CurveBuildingBlockBundle blocks7 = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(1).getSecond();
    blocks7.addAll(CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0).getSecond());
    final double spreadJPYEUR = 0.0010; // 10bps
    final double notional = 100000;
    final GeneratorAttributeIR swapAttribute = new GeneratorAttributeIR(Period.ofYears(4));
    final SwapFixedInflationZeroCouponDefinition swapDefinition = GENERATOR_INFLATION_SWAP.generateInstrument(NOW, spreadJPYEUR, notional, swapAttribute);
    final InstrumentDerivative swap = swapDefinition.toDerivative(NOW, new ZonedDateTimeDoubleTimeSeries[] {TS_PRICE_INDEX_USD_WITH_TODAY, TS_PRICE_INDEX_USD_WITH_TODAY });
    final ParameterInflationSensitivityParameterCalculator<InflationProviderInterface> PSC = new ParameterInflationSensitivityParameterCalculator<>(PVCSDIC);
    final MarketQuoteInflationSensitivityBlockCalculator<InflationProviderInterface> MQSC = new MarketQuoteInflationSensitivityBlockCalculator<>(PSC);
    @SuppressWarnings("unused")
    final MultipleCurrencyParameterSensitivity mqs = MQSC.fromInstrument(swap, multicurves7, blocks7);
  }

  private void curveConstructionTest(final InstrumentDefinition<?>[][][] definitions, final InflationProviderDiscount curves, final int block) {
    final int nbBlocks = definitions.length;
    for (int loopblock = 0; loopblock < nbBlocks; loopblock++) {
      final InstrumentDerivative[][] instruments = convert(definitions[loopblock]);
      final double[][] pv = new double[instruments.length][];
      for (int loopcurve = 0; loopcurve < instruments.length; loopcurve++) {
        pv[loopcurve] = new double[instruments[loopcurve].length];
        for (int loopins = 0; loopins < instruments[loopcurve].length; loopins++) {
          pv[loopcurve][loopins] = curves.getFxRates().convert(instruments[loopcurve][loopins].accept(PVIC, curves), USD).getAmount();
          assertEquals("Curve construction: block " + block + ", unit " + loopblock + " - instrument " + loopins, 0, pv[loopcurve][loopins], TOLERANCE_CAL);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static Pair<InflationProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitions(final InstrumentDefinition<?>[][][] definitions,
      final GeneratorCurve[][] curveGenerators,
      final String[][] curveNames, final InflationProviderDiscount knownData, final InstrumentDerivativeVisitor<InflationProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<InflationProviderInterface, InflationSensitivity> sensitivityCalculator) {
    final int nUnits = definitions.length;
    final MultiCurveBundle<GeneratorCurve>[] curveBundles = new MultiCurveBundle[nUnits];
    for (int i = 0; i < nUnits; i++) {
      final int nCurves = definitions[i].length;
      final SingleCurveBundle<GeneratorCurve>[] singleCurves = new SingleCurveBundle[nCurves];
      for (int j = 0; j < nCurves; j++) {
        final int nInstruments = definitions[i][j].length;
        final InstrumentDerivative[] derivatives = new InstrumentDerivative[nInstruments];
        final double[] initialGuess = new double[nInstruments];
        for (int k = 0; k < nInstruments; k++) {
          derivatives[k] = convert(definitions[i][j][k]);
          initialGuess[k] = initialGuess(definitions[i][j][k]);
        }
        final GeneratorCurve generator = curveGenerators[i][j].finalGenerator(derivatives);
        singleCurves[j] = new SingleCurveBundle<>(curveNames[i][j], derivatives, initialGuess, generator);
      }
      curveBundles[i] = new MultiCurveBundle<>(singleCurves);
    }
    return CURVE_BUILDING_REPOSITORY.makeCurvesFromDerivatives(curveBundles, knownData, DSC_MAP, FWD_ON_MAP, US_CPI_MAP, calculator,
        sensitivityCalculator);
  }

  private static InstrumentDerivative[][] convert(final InstrumentDefinition<?>[][] definitions) {
    final InstrumentDerivative[][] instruments = new InstrumentDerivative[definitions.length][];
    for (int loopcurve = 0; loopcurve < definitions.length; loopcurve++) {
      instruments[loopcurve] = new InstrumentDerivative[definitions[loopcurve].length];
      int loopins = 0;
      for (final InstrumentDefinition<?> instrument : definitions[loopcurve]) {
        InstrumentDerivative ird;
        if (instrument instanceof SwapFixedInflationZeroCouponDefinition) {
          final Annuity<? extends Payment> ird1 = ((SwapFixedInflationZeroCouponDefinition) instrument).getFirstLeg().toDerivative(NOW);
          final Annuity<? extends Payment> ird2 = ((SwapFixedInflationZeroCouponDefinition) instrument).getSecondLeg().toDerivative(NOW, TS_PRICE_INDEX_USD_WITH_TODAY);
          ird = new Swap<>(ird1, ird2);
        }
        else {
          ird = instrument.toDerivative(NOW);
        }
        instruments[loopcurve][loopins++] = ird;
      }
    }
    return instruments;
  }

  private static InstrumentDerivative convert(final InstrumentDefinition<?> instrument) {
    InstrumentDerivative ird;
    if (instrument instanceof SwapFixedInflationZeroCouponDefinition) {
      final Annuity<? extends Payment> ird1 = ((SwapFixedInflationZeroCouponDefinition) instrument).getFirstLeg().toDerivative(NOW);
      final Annuity<? extends Payment> ird2 = ((SwapFixedInflationZeroCouponDefinition) instrument).getSecondLeg().toDerivative(NOW, TS_PRICE_INDEX_USD_WITH_TODAY);
      ird = new Swap<>(ird1, ird2);
    }
    else {
      ird = instrument.toDerivative(NOW);
    }
    return ird;
  }

  private static double initialGuess(final InstrumentDefinition<?> instrument) {
    if (instrument instanceof SwapFixedONDefinition) {
      return ((SwapFixedONDefinition) instrument).getFixedLeg().getNthPayment(0).getRate();
    }
    if (instrument instanceof SwapFixedIborDefinition) {
      return ((SwapFixedIborDefinition) instrument).getFixedLeg().getNthPayment(0).getRate();
    }
    if (instrument instanceof SwapFixedInflationZeroCouponDefinition) {

      if (((SwapFixedInflationZeroCouponDefinition) instrument).getFirstLeg().getNthPayment(0) instanceof CouponInflationZeroCouponMonthlyDefinition) {
        return 200.0;
      }
      if (((SwapFixedInflationZeroCouponDefinition) instrument).getFirstLeg().getNthPayment(0) instanceof CouponInflationZeroCouponInterpolationDefinition) {
        return 200.0;
      }
      return 200.0;
    }
    if (instrument instanceof ForwardRateAgreementDefinition) {
      return ((ForwardRateAgreementDefinition) instrument).getRate();
    }
    if (instrument instanceof CashDefinition) {
      return ((CashDefinition) instrument).getRate();
    }
    return 200.0;
  }

}
