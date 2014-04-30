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

import com.opengamma.analytics.financial.curve.inflation.generator.GeneratorPriceIndexCurve;
import com.opengamma.analytics.financial.curve.inflation.generator.GeneratorPriceIndexCurveInterpolated;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedInflationMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedInflationZeroCoupon;
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
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.ParSpreadInflationMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.curve.inflation.InflationDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Build of inflation curve in several blocks with relevant Jacobian matrice.
 */
@Test(groups = TestGroup.UNIT)
public class InflationBuildingCurveSimpleTestEUR {

  private static final Interpolator1D INTERPOLATOR_LOG_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LOG_LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();
  private static final double TOLERANCE_ROOT = 1.0E-10;
  private static final int STEP_MAX = 100;

  private static final Currency EUR = Currency.EUR;

  private static final double NOTIONAL = 1.0;

  private static final GeneratorSwapFixedInflationZeroCoupon GENERATOR_INFLATION_SWAP = GeneratorSwapFixedInflationMaster.getInstance().getGenerator("EURHICP");
  private static final IndexPrice US_CPI = GENERATOR_INFLATION_SWAP.getIndexPrice();

  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2012, 9, 28);

  private static final ZonedDateTimeDoubleTimeSeries TS_PRICE_INDEX_EUR_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28), DateUtils.getUTCDate(2012, 6, 30), DateUtils.getUTCDate(2012, 7, 31), DateUtils.getUTCDate(2012, 8, 30) }, new double[] {200, 200, 200, 200, 200 });
  private static final String CURVE_NAME_DSC_EUR = "EUR Dsc";
  private static final String CURVE_NAME_CPI_EUR = "EUR CPI";
  private static final String[] CURVE_NAMES = {CURVE_NAME_CPI_EUR };
  /** Market values for the CPI EUR curve */
  public static final double[] CPI_EUR_MARKET_QUOTES = new double[] {0.0200, 0.0200, 0.0250, 0.0260, 0.0200, 0.0270, 0.0280, 0.0290, 0.0300, 0.0310, 0.0320, 0.0330, 0.0330, 0.0330, 0.0330 };
  /** Generators for the CPI EUR curve */
  public static final GeneratorInstrument<? extends GeneratorAttribute>[] CPI_EUR_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_INFLATION_SWAP, GENERATOR_INFLATION_SWAP,
    GENERATOR_INFLATION_SWAP,
    GENERATOR_INFLATION_SWAP, GENERATOR_INFLATION_SWAP, GENERATOR_INFLATION_SWAP, GENERATOR_INFLATION_SWAP, GENERATOR_INFLATION_SWAP, GENERATOR_INFLATION_SWAP,
    GENERATOR_INFLATION_SWAP, GENERATOR_INFLATION_SWAP, GENERATOR_INFLATION_SWAP, GENERATOR_INFLATION_SWAP, GENERATOR_INFLATION_SWAP, GENERATOR_INFLATION_SWAP };
  /** Tenors for the CPI EUR curve */
  public static final Period[] CPI_EUR_TENOR = new Period[] {Period.ofYears(1),
    Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(6), Period.ofYears(7),
    Period.ofYears(8), Period.ofYears(9), Period.ofYears(10), Period.ofYears(12), Period.ofYears(15), Period.ofYears(20),
    Period.ofYears(25), Period.ofYears(30) };
  public static final GeneratorAttributeIR[] CPI_EUR_ATTR = new GeneratorAttributeIR[CPI_EUR_TENOR.length];
  static {
    for (int loopins = 0; loopins < CPI_EUR_TENOR.length; loopins++) {
      CPI_EUR_ATTR[loopins] = new GeneratorAttributeIR(CPI_EUR_TENOR[loopins]);
    }
  }

  /** Standard EUR CPI curve instrument definitions */
  public static final InstrumentDefinition<?>[] DEFINITIONS_CPI_EUR;

  /** Units of curves */
  public static final int[] NB_UNITS = new int[] {1 };
  public static final int NB_BLOCKS = NB_UNITS.length;
  public static final InstrumentDefinition<?>[][][][] DEFINITIONS_UNITS = new InstrumentDefinition<?>[NB_BLOCKS][][][];
  public static final GeneratorPriceIndexCurve[][][] GENERATORS_UNITS = new GeneratorPriceIndexCurve[NB_BLOCKS][][];
  public static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];

  public static final LinkedHashMap<String, IndexPrice[]> US_CPI_MAP = new LinkedHashMap<>();

  static {
    DEFINITIONS_CPI_EUR = getDefinitions(CPI_EUR_MARKET_QUOTES, CPI_EUR_GENERATORS, CPI_EUR_ATTR);

    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      DEFINITIONS_UNITS[loopblock] = new InstrumentDefinition<?>[NB_UNITS[loopblock]][][];
      GENERATORS_UNITS[loopblock] = new GeneratorPriceIndexCurve[NB_UNITS[loopblock]][];
      NAMES_UNITS[loopblock] = new String[NB_UNITS[loopblock]][];
    }
    DEFINITIONS_UNITS[0][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_CPI_EUR };

    final GeneratorPriceIndexCurve genIntLin = new GeneratorPriceIndexCurveInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_LOG_LINEAR);
    GENERATORS_UNITS[0][0] = new GeneratorPriceIndexCurve[] {genIntLin };

    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_CPI_EUR };

    US_CPI_MAP.put(CURVE_NAME_CPI_EUR, new IndexPrice[] {US_CPI });
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
  private static final ParSpreadInflationMarketQuoteDiscountingCalculator PSIMQC = ParSpreadInflationMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator PSIMQCSC = ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();

  private static final InflationDiscountBuildingRepository CURVE_BUILDING_REPOSITORY = new InflationDiscountBuildingRepository(TOLERANCE_ROOT, TOLERANCE_ROOT, STEP_MAX);

  private static final double TOLERANCE_CAL = 1.0E-9;

  private static final InflationBuildingCurveWithDiscountTestEUR curveBuilder = new InflationBuildingCurveWithDiscountTestEUR();
  private static final List<Pair<InflationProviderDiscount, CurveBuildingBlockBundle>> CURVES_WITH_BLOCKS = curveBuilder.getCurvesWithBlock();
  private static final InflationProviderDiscount KNOWN_CURVES = new InflationProviderDiscount(CURVES_WITH_BLOCKS.get(0).getFirst().copy().getMulticurveProvider());
  private static final LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> BUNDLE = new LinkedHashMap<>();
  static {
    BUNDLE.put(CURVE_NAME_DSC_EUR, CURVES_WITH_BLOCKS.get(0).getSecond().getBlock(CURVE_NAME_DSC_EUR));
  }
  private static CurveBuildingBlockBundle KNOWN_BUNDLE = new CurveBuildingBlockBundle(BUNDLE);

  @BeforeSuite
  static void initClass() {
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.add(makeCurvesFromDefinitions(DEFINITIONS_UNITS[loopblock], GENERATORS_UNITS[loopblock], NAMES_UNITS[loopblock], KNOWN_CURVES, KNOWN_BUNDLE, PSIMQC,
          PSIMQCSC));
    }
  }

  @Test(enabled = true)
  public void blockBundle() {
    final CurveBuildingBlockBundle blockBundleFromOneCurveTest = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0).getSecond();
    final CurveBuildingBlockBundle blockBundleFromTwoCurveTest = CURVES_WITH_BLOCKS.get(0).getSecond();
    for (final String element : CURVE_NAMES) {
      for (int j = 0; j < blockBundleFromOneCurveTest.getBlock(element).getSecond().getData().length; j++) {
        for (int k = 0; k < blockBundleFromOneCurveTest.getBlock(element).getSecond().getData()[j].length; k++) {
          assertEquals("Curve construction: block " + element + ", column " + j + " - line " + k, blockBundleFromOneCurveTest.getBlock(element).getSecond().getData()[j][k],
              blockBundleFromTwoCurveTest.getBlock(element).getSecond().getData()[j][k], TOLERANCE_CAL);
        }
      }
    }
  }

  @Test(enabled = false)
  public void performance() {
    long startTime, endTime;
    final int nbTest = 1000;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      makeCurvesFromDefinitions(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_CURVES, KNOWN_BUNDLE, PSIMQC, PSIMQCSC);
    }
    endTime = System.currentTimeMillis();
    System.out.println("InflationBuildingCurveSimpleTestEUR - " + nbTest + " curve construction Price index EUR 1 units: " + (endTime - startTime) + " ms");
    // Performance note: curve construction Price index EUR 1 units: 27-Mar-13: On Dell Precision T1850 3.5 GHz Quad-Core Intel Xeon: 3564 ms for 1000 sets.
  }

  @Test
  public void curveConstructionGeneratorOtherBlocks() {
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      curveConstructionTest(DEFINITIONS_UNITS[loopblock], CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(loopblock).getFirst(), loopblock);
    }
  }

  private void curveConstructionTest(final InstrumentDefinition<?>[][][] definitions, final InflationProviderDiscount curves, final int block) {
    final int nbBlocks = definitions.length;
    for (int loopblock = 0; loopblock < nbBlocks; loopblock++) {
      final InstrumentDerivative[][] instruments = convert(definitions[loopblock]);
      final double[][] pv = new double[instruments.length][];
      for (int loopcurve = 0; loopcurve < instruments.length; loopcurve++) {
        pv[loopcurve] = new double[instruments[loopcurve].length];
        for (int loopins = 0; loopins < instruments[loopcurve].length; loopins++) {
          pv[loopcurve][loopins] = curves.getFxRates().convert(instruments[loopcurve][loopins].accept(PVIC, curves), EUR).getAmount();
          assertEquals("Curve construction: block " + block + ", unit " + loopblock + " - instrument " + loopins, 0, pv[loopcurve][loopins], TOLERANCE_CAL);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static Pair<InflationProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitions(final InstrumentDefinition<?>[][][] definitions,
      final GeneratorPriceIndexCurve[][] curveGenerators,
      final String[][] curveNames, final InflationProviderDiscount knownData, final CurveBuildingBlockBundle knownBundle,
      final InstrumentDerivativeVisitor<InflationProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<InflationProviderInterface, InflationSensitivity> sensitivityCalculator) {
    final int nUnits = definitions.length;
    final MultiCurveBundle<GeneratorPriceIndexCurve>[] curveBundles = new MultiCurveBundle[nUnits];
    for (int i = 0; i < nUnits; i++) {
      final int nCurves = definitions[i].length;
      final SingleCurveBundle<GeneratorPriceIndexCurve>[] singleCurves = new SingleCurveBundle[nCurves];
      for (int j = 0; j < nCurves; j++) {
        final int nInstruments = definitions[i][j].length;
        final InstrumentDerivative[] derivatives = new InstrumentDerivative[nInstruments];
        final double[] initialGuess = new double[nInstruments];
        for (int k = 0; k < nInstruments; k++) {
          derivatives[k] = convert(definitions[i][j][k]);
          initialGuess[k] = initialGuess(definitions[i][j][k]);
        }
        final GeneratorPriceIndexCurve generator = curveGenerators[i][j].finalGenerator(derivatives);
        singleCurves[j] = new SingleCurveBundle<>(curveNames[i][j], derivatives, initialGuess, generator);
      }
      curveBundles[i] = new MultiCurveBundle<>(singleCurves);
    }
    return CURVE_BUILDING_REPOSITORY.makeCurvesFromDerivatives(curveBundles, knownData, knownBundle, US_CPI_MAP, calculator,
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
          final Annuity<? extends Payment> ird2 = ((SwapFixedInflationZeroCouponDefinition) instrument).getSecondLeg().toDerivative(NOW, TS_PRICE_INDEX_EUR_WITH_TODAY);
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
      final Annuity<? extends Payment> ird2 = ((SwapFixedInflationZeroCouponDefinition) instrument).getSecondLeg().toDerivative(NOW, TS_PRICE_INDEX_EUR_WITH_TODAY);
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
        return 100.0;
      }
      if (((SwapFixedInflationZeroCouponDefinition) instrument).getFirstLeg().getNthPayment(0) instanceof CouponInflationZeroCouponInterpolationDefinition) {
        return 100.0;
      }
      return 100;
    }
    if (instrument instanceof ForwardRateAgreementDefinition) {
      return ((ForwardRateAgreementDefinition) instrument).getRate();
    }
    if (instrument instanceof CashDefinition) {
      return ((CashDefinition) instrument).getRate();
    }
    return 1;
  }

}
