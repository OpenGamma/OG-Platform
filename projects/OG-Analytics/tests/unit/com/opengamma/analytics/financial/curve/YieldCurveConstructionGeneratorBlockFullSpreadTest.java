/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.calculator.PresentValueConvertedCalculator;
import com.opengamma.analytics.financial.calculator.PresentValueCurveSensitivityConvertedCalculator;
import com.opengamma.analytics.financial.calculator.PresentValueCurveSensitivityMCSCalculator;
import com.opengamma.analytics.financial.calculator.PresentValueMCACalculator;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.Generator;
import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositON;
import com.opengamma.analytics.financial.instrument.index.GeneratorFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.generator.GeneratorSwapTestsMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.LastTimeCalculator;
import com.opengamma.analytics.financial.interestrate.ParSpreadMarketQuoteCalculator;
import com.opengamma.analytics.financial.interestrate.ParSpreadMarketQuoteCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.linearalgebra.DecompositionFactory;
import com.opengamma.analytics.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Build of curve in several currencies in several blocks with relevant Jacobian matrices.
 * Currencies: USD (2 curves), EUR (2 curves), JPY (3 curves)
 */
public class YieldCurveConstructionGeneratorBlockFullSpreadTest {

  private static final Interpolator1D INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  //  private static final Interpolator1D INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
  //      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final MatrixAlgebra MATRIX_ALGEBRA = new CommonsMatrixAlgebra();

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();
  private static final double TOLERANCE_ROOT = 1.0E-10;
  private static final BroydenVectorRootFinder ROOT_FINDER = new BroydenVectorRootFinder(TOLERANCE_ROOT, TOLERANCE_ROOT, 10000,
      DecompositionFactory.getDecomposition(DecompositionFactory.SV_COLT_NAME));
  private static final Currency CCY_USD = Currency.USD;
  private static final FXMatrix FX_MATRIX = new FXMatrix(CCY_USD);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("CAL");
  private static final int SPOT_LAG = 2;
  private static final DayCount DAY_COUNT_CASH = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final double NOTIONAL = 1.0;
  private static final BusinessDayConvention BDC = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final IndexON INDEX_ON_1 = new IndexON("Fed Fund", CCY_USD, DAY_COUNT_CASH, 1, CALENDAR);
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_USD = new GeneratorDepositON("USD Deposit ON", CCY_USD, CALENDAR, DAY_COUNT_CASH);
  private static final GeneratorFixedON GENERATOR_OIS_USD = new GeneratorFixedON("USD1YFEDFUND", INDEX_ON_1, Period.ofMonths(12), DAY_COUNT_CASH, BDC, true, SPOT_LAG, SPOT_LAG);
  private static final GeneratorDeposit GENERATOR_DEPOSIT_USD = new GeneratorDeposit("USD Deposit", CCY_USD, CALENDAR, SPOT_LAG, DAY_COUNT_CASH, BDC, true);
  private static final GeneratorSwapTestsMaster GENERATOR_SWAP_MASTER = GeneratorSwapTestsMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("USD6MLIBOR3M", CALENDAR);
  private static final GeneratorSwapFixedIbor USD6MLIBOR6M = GENERATOR_SWAP_MASTER.getGenerator("USD6MLIBOR6M", CALENDAR);

  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2011, 9, 28);

  private static final ArrayZonedDateTimeDoubleTimeSeries TS_EMPTY = new ArrayZonedDateTimeDoubleTimeSeries();
  private static final ArrayZonedDateTimeDoubleTimeSeries TS_ON_USD_WITH_TODAY = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
      DateUtils.getUTCDate(2011, 9, 28)}, new double[] {0.07, 0.08});
  private static final ArrayZonedDateTimeDoubleTimeSeries TS_ON_USD_WITHOUT_TODAY = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
      DateUtils.getUTCDate(2011, 9, 28)}, new double[] {0.07, 0.08});
  @SuppressWarnings("rawtypes")
  private static final DoubleTimeSeries[] TS_FIXED_OIS_USD_WITH_TODAY = new DoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITH_TODAY};
  @SuppressWarnings("rawtypes")
  private static final DoubleTimeSeries[] TS_FIXED_OIS_USD_WITHOUT_TODAY = new DoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITHOUT_TODAY};

  private static final ArrayZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITH_TODAY = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
      DateUtils.getUTCDate(2011, 9, 28)}, new double[] {0.0035, 0.0036});
  private static final ArrayZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITHOUT_TODAY = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27)},
      new double[] {0.0035});

  private static final ArrayZonedDateTimeDoubleTimeSeries TS_IBOR_USD6M_WITH_TODAY = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
      DateUtils.getUTCDate(2011, 9, 28)}, new double[] {0.0045, 0.0046});
  private static final ArrayZonedDateTimeDoubleTimeSeries TS_IBOR_USD6M_WITHOUT_TODAY = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27)},
      new double[] {0.0045});

  @SuppressWarnings("rawtypes")
  private static final DoubleTimeSeries[] TS_FIXED_IBOR_USD3M_WITH_TODAY = new DoubleTimeSeries[] {TS_IBOR_USD3M_WITH_TODAY};
  @SuppressWarnings("rawtypes")
  private static final DoubleTimeSeries[] TS_FIXED_IBOR_USD3M_WITHOUT_TODAY = new DoubleTimeSeries[] {TS_IBOR_USD3M_WITHOUT_TODAY};
  @SuppressWarnings("rawtypes")
  private static final DoubleTimeSeries[] TS_FIXED_IBOR_USD6M_WITH_TODAY = new DoubleTimeSeries[] {TS_IBOR_USD6M_WITH_TODAY};
  @SuppressWarnings("rawtypes")
  private static final DoubleTimeSeries[] TS_FIXED_IBOR_USD6M_WITHOUT_TODAY = new DoubleTimeSeries[] {TS_IBOR_USD6M_WITHOUT_TODAY};

  private static final String CURVE_NAME_DSC_USD = "USD Dsc";
  private static final String CURVE_NAME_FWD3_USD = "USD Fwd 3M";
  private static final String CURVE_NAME_FWD6_USD = "USD Fwd 6M";
  private static final HashMap<String, Currency> CCY_MAP = new HashMap<String, Currency>();
  static {
    CCY_MAP.put(CURVE_NAME_DSC_USD, CCY_USD);
    CCY_MAP.put(CURVE_NAME_FWD3_USD, CCY_USD);
    CCY_MAP.put(CURVE_NAME_FWD6_USD, CCY_USD);
  }

  /** Market values for the dsc USD curve */
  public static final double[] DSC_USD_MARKET_QUOTES = new double[] {0.0010, 0.0010, 0.0010, 0.0010, 0.0010, 0.0010, 0.0010, 0.0010, 0.0020, 0.0020, 0.0040, 0.0050, 0.0130};
  /** Generators for the dsc USD curve */
  public static final Generator[] DSC_USD_GENERATORS = new Generator[] {GENERATOR_DEPOSIT_ON_USD, GENERATOR_DEPOSIT_ON_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD,
      GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD};
  /** Tenors for the dsc USD curve */
  public static final Period[] DSC_USD_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
      Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10)};

  //  /** Market values for the Fwd 3M USD curve */
  //  public static final double[] FWD3_USD_MARKET_QUOTES = new double[] {0.0045, 0.0045, 0.0080, 0.0160, 0.0045, 0.0045, 0.0060, 0.0070};
  //  /** Generators for the Fwd 3M USD curve */
  //  public static final Generator[] FWD3_USD_GENERATORS = new Generator[] {GENERATOR_DEPOSIT_USD, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M};
  //  /** Tenors for the Fwd 3M USD curve */
  //  public static final Period[] FWD3_USD_TENOR = new Period[] {Period.ofMonths(3), Period.ofYears(1), Period.ofYears(5), Period.ofYears(10), Period.ofMonths(6), Period.ofYears(2), Period.ofYears(3),
  //      Period.ofYears(4)};

  /** Market values for the Fwd 3M USD curve */
  public static final double[] FWD3_USD_MARKET_QUOTES = new double[] {0.0045, 0.0045, 0.0045, 0.0045, 0.0060, 0.0070, 0.0080, 0.0160};
  /** Generators for the Fwd 3M USD curve */
  public static final Generator[] FWD3_USD_GENERATORS = new Generator[] {USD6MLIBOR3M, GENERATOR_DEPOSIT_USD, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M};
  /** Tenors for the Fwd 3M USD curve */
  public static final Period[] FWD3_USD_TENOR = new Period[] {Period.ofYears(1), Period.ofMonths(3), Period.ofMonths(6), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
      Period.ofYears(10)};

  /** Market values for the Fwd 3M USD curve */
  public static final double[] FWD3_USD_MARKET_QUOTES_2 = new double[] {0.0045, 0.0045, 0.0045, 0.0050, 0.0060, 0.0080, 0.0075, 0.0090, 0.0160};
  /** Generators for the Fwd 3M USD curve */
  public static final Generator[] FWD3_USD_GENERATORS_2 = new Generator[] {GENERATOR_DEPOSIT_USD, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M,
      USD6MLIBOR3M};
  /** Tenors for the Fwd 3M USD curve */
  public static final Period[] FWD3_USD_TENOR_2 = new Period[] {Period.ofMonths(3), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
      Period.ofYears(7), Period.ofYears(10)};

  /** Market values for the Fwd 3M USD curve */
  public static final double[] FWD3_USD_MARKET_QUOTES_3 = new double[] {0.0045, 0.0045, 0.0045, 0.0050, 0.0060, 0.0080, 0.0075, 0.0090, 0.0160, 0.0200, 0.0180};
  /** Generators for the Fwd 3M USD curve */
  public static final Generator[] FWD3_USD_GENERATORS_3 = new Generator[] {GENERATOR_DEPOSIT_USD, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M,
      USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M};
  /** Tenors for the Fwd 3M USD curve */
  public static final Period[] FWD3_USD_TENOR_3 = new Period[] {Period.ofMonths(3), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
      Period.ofYears(7), Period.ofYears(10), Period.ofYears(15), Period.ofYears(20)};

  /** Market values for the Fwd 3M USD curve */
  public static final double[] FWD6_USD_MARKET_QUOTES = new double[] {0.0065, 0.0055, 0.0080, 0.0170};
  /** Generators for the Fwd 3M USD curve */
  public static final Generator[] FWD6_USD_GENERATORS = new Generator[] {GENERATOR_DEPOSIT_USD, USD6MLIBOR6M, USD6MLIBOR6M, USD6MLIBOR6M};
  /** Tenors for the Fwd 3M USD curve */
  public static final Period[] FWD6_USD_TENOR = new Period[] {Period.ofMonths(6), Period.ofYears(2), Period.ofYears(5), Period.ofYears(10)};

  /** Standard USD discounting curve instrument definitions */
  public static final InstrumentDefinition<?>[] DEFINITIONS_DSC_USD;
  /** Standard USD Forward 3M curve instrument definitions */
  public static final InstrumentDefinition<?>[] DEFINITIONS_FWD3_USD;
  /** Standard USD Forward 3M curve instrument definitions */
  public static final InstrumentDefinition<?>[] DEFINITIONS_FWD3_USD_2;
  /** Standard USD Forward 3M curve instrument definitions */
  public static final InstrumentDefinition<?>[] DEFINITIONS_FWD3_USD_3;
  /** Standard USD Forward 6M curve instrument definitions */
  public static final InstrumentDefinition<?>[] DEFINITIONS_FWD6_USD;
  /** Units of curves */
  public static final int[] NB_UNITS = new int[] {2, 3, 3, 1};
  public static final int NB_BLOCKS = NB_UNITS.length;
  public static final InstrumentDefinition<?>[][][][] DEFINITIONS_UNITS = new InstrumentDefinition<?>[NB_BLOCKS][][][];
  public static final GeneratorCurve[][][] GENERATORS_UNITS = new GeneratorCurve[NB_BLOCKS][][];
  public static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];
  public static final YieldCurveBundle KNOWN_DATA = new YieldCurveBundle(FX_MATRIX, CCY_MAP);

  static {
    DEFINITIONS_DSC_USD = getDefinitions(DSC_USD_MARKET_QUOTES, DSC_USD_GENERATORS, DSC_USD_TENOR, new Double[DSC_USD_MARKET_QUOTES.length]);
    DEFINITIONS_FWD3_USD = getDefinitions(FWD3_USD_MARKET_QUOTES, FWD3_USD_GENERATORS, FWD3_USD_TENOR, new Double[FWD3_USD_MARKET_QUOTES.length]);
    DEFINITIONS_FWD3_USD_2 = getDefinitions(FWD3_USD_MARKET_QUOTES_2, FWD3_USD_GENERATORS_2, FWD3_USD_TENOR_2, new Double[FWD3_USD_MARKET_QUOTES_2.length]);
    DEFINITIONS_FWD3_USD_3 = getDefinitions(FWD3_USD_MARKET_QUOTES_3, FWD3_USD_GENERATORS_3, FWD3_USD_TENOR_3, new Double[FWD3_USD_MARKET_QUOTES_3.length]);
    DEFINITIONS_FWD6_USD = getDefinitions(FWD6_USD_MARKET_QUOTES, FWD6_USD_GENERATORS, FWD6_USD_TENOR, new Double[FWD6_USD_MARKET_QUOTES.length]);
    DEFINITIONS_UNITS[0] = new InstrumentDefinition<?>[NB_UNITS[0]][][];
    DEFINITIONS_UNITS[1] = new InstrumentDefinition<?>[NB_UNITS[1]][][];
    DEFINITIONS_UNITS[2] = new InstrumentDefinition<?>[NB_UNITS[2]][][];
    DEFINITIONS_UNITS[3] = new InstrumentDefinition<?>[NB_UNITS[3]][][];
    DEFINITIONS_UNITS[0][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD};
    DEFINITIONS_UNITS[0][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_USD};
    DEFINITIONS_UNITS[1][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD};
    DEFINITIONS_UNITS[1][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_USD_2};
    DEFINITIONS_UNITS[1][2] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD6_USD};
    DEFINITIONS_UNITS[2][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD};
    DEFINITIONS_UNITS[2][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_USD_2};
    DEFINITIONS_UNITS[2][2] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD6_USD};
    DEFINITIONS_UNITS[3][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_USD_3, DEFINITIONS_DSC_USD};
    GeneratorCurve genInt = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR);
    //    GeneratorCurve genNS = new GeneratorCurveYieldNelsonSiegel();
    GeneratorCurve genInt0 = new GeneratorCurveYieldInterpolatedAnchor(MATURITY_CALCULATOR, INTERPOLATOR);
    GeneratorCurve genAddExistFwd3 = new GeneratorCurveAddYieldExisiting(genInt, false, CURVE_NAME_FWD3_USD);
    GeneratorCurve genCst = new GeneratorCurveYieldConstant();
    GENERATORS_UNITS[0] = new GeneratorCurve[NB_UNITS[0]][];
    GENERATORS_UNITS[1] = new GeneratorCurve[NB_UNITS[1]][];
    GENERATORS_UNITS[2] = new GeneratorCurve[NB_UNITS[2]][];
    GENERATORS_UNITS[3] = new GeneratorCurve[NB_UNITS[3]][];
    GENERATORS_UNITS[0][0] = new GeneratorCurve[] {genInt};
    //    GENERATORS_UNITS[0][1] = new GeneratorCurve[] {genNS};
    GENERATORS_UNITS[0][1] = new GeneratorCurve[] {new GeneratorCurveAddYield(new GeneratorCurve[] {genCst, genInt0}, false)};
    GENERATORS_UNITS[1][0] = new GeneratorCurve[] {genInt};
    GENERATORS_UNITS[1][1] = new GeneratorCurve[] {genInt};
    GENERATORS_UNITS[1][2] = new GeneratorCurve[] {genAddExistFwd3};
    GENERATORS_UNITS[2][0] = new GeneratorCurve[] {genInt};
    GENERATORS_UNITS[2][1] = new GeneratorCurve[] {genInt};
    GENERATORS_UNITS[2][2] = new GeneratorCurve[] {genInt};
    GENERATORS_UNITS[3][0] = new GeneratorCurve[] {genInt, genAddExistFwd3};
    NAMES_UNITS[0] = new String[NB_UNITS[0]][];
    NAMES_UNITS[1] = new String[NB_UNITS[1]][];
    NAMES_UNITS[2] = new String[NB_UNITS[2]][];
    NAMES_UNITS[3] = new String[NB_UNITS[3]][];
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_USD};
    NAMES_UNITS[0][1] = new String[] {CURVE_NAME_FWD3_USD};
    NAMES_UNITS[1][0] = new String[] {CURVE_NAME_DSC_USD};
    NAMES_UNITS[1][1] = new String[] {CURVE_NAME_FWD3_USD};
    NAMES_UNITS[1][2] = new String[] {CURVE_NAME_FWD6_USD};
    NAMES_UNITS[2][0] = new String[] {CURVE_NAME_DSC_USD};
    NAMES_UNITS[2][1] = new String[] {CURVE_NAME_FWD3_USD};
    NAMES_UNITS[2][2] = new String[] {CURVE_NAME_FWD6_USD};
    NAMES_UNITS[3][0] = new String[] {CURVE_NAME_FWD3_USD, CURVE_NAME_DSC_USD};
  }

  // Present Value
  private static final PresentValueMCACalculator PV_CALCULATOR = PresentValueMCACalculator.getInstance();
  private static final PresentValueCurveSensitivityMCSCalculator PVCS_CALCULATOR = PresentValueCurveSensitivityMCSCalculator.getInstance();
  private static final Currency CCY_PV = CCY_USD;
  private static final PresentValueConvertedCalculator PV_CONVERTED_CALCULATOR = new PresentValueConvertedCalculator(CCY_PV, PV_CALCULATOR);
  private static final PresentValueCurveSensitivityConvertedCalculator PVCS_CONVERTED_CALCULATOR = new PresentValueCurveSensitivityConvertedCalculator(CCY_PV, PVCS_CALCULATOR);
  // Par spread market quote
  private static final ParSpreadMarketQuoteCalculator PSMQ_CALCULATOR = ParSpreadMarketQuoteCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityCalculator PSMQCS_CALCULATOR = ParSpreadMarketQuoteCurveSensitivityCalculator.getInstance();

  private static Pair<YieldCurveBundle, CurveBuildingBlockBundle> CURVES_PRESENT_VALUE_WITH_TODAY_BLOCK0;
  private static Pair<YieldCurveBundle, CurveBuildingBlockBundle> CURVES_PAR_SPREAD_MQ_WITH_TODAY_BLOCK0;
  private static Pair<YieldCurveBundle, CurveBuildingBlockBundle> CURVES_PRESENT_VALUE_WITHOUT_TODAY_BLOCK0;
  private static Pair<YieldCurveBundle, CurveBuildingBlockBundle> CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK0;
  private static Pair<YieldCurveBundle, CurveBuildingBlockBundle> CURVES_PAR_SPREAD_MQ_WITH_TODAY_BLOCK1;
  private static Pair<YieldCurveBundle, CurveBuildingBlockBundle> CURVES_PAR_SPREAD_MQ_WITH_TODAY_BLOCK2;
  private static Pair<YieldCurveBundle, CurveBuildingBlockBundle> CURVES_PAR_SPREAD_MQ_WITH_TODAY_BLOCK3;

  private static final double TOLERANCE_PV = 1.0E-10;

  @BeforeSuite
  static void initClass() {
    CURVES_PRESENT_VALUE_WITH_TODAY_BLOCK0 = makeCurves(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PV_CONVERTED_CALCULATOR, PVCS_CONVERTED_CALCULATOR, true, 0);
    CURVES_PAR_SPREAD_MQ_WITH_TODAY_BLOCK0 = makeCurves(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQ_CALCULATOR, PSMQCS_CALCULATOR, true, 0);
    CURVES_PRESENT_VALUE_WITHOUT_TODAY_BLOCK0 = makeCurves(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PV_CONVERTED_CALCULATOR, PVCS_CONVERTED_CALCULATOR, false, 0);
    CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK0 = makeCurves(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQ_CALCULATOR, PSMQCS_CALCULATOR, false, 0);
    CURVES_PAR_SPREAD_MQ_WITH_TODAY_BLOCK1 = makeCurves(DEFINITIONS_UNITS[1], GENERATORS_UNITS[1], NAMES_UNITS[1], KNOWN_DATA, PSMQ_CALCULATOR, PSMQCS_CALCULATOR, true, 1);
    CURVES_PAR_SPREAD_MQ_WITH_TODAY_BLOCK2 = makeCurves(DEFINITIONS_UNITS[2], GENERATORS_UNITS[2], NAMES_UNITS[2], KNOWN_DATA, PSMQ_CALCULATOR, PSMQCS_CALCULATOR, true, 2);
    CURVES_PAR_SPREAD_MQ_WITH_TODAY_BLOCK3 = makeCurves(DEFINITIONS_UNITS[3], GENERATORS_UNITS[3], NAMES_UNITS[3], KNOWN_DATA, PSMQ_CALCULATOR, PSMQCS_CALCULATOR, true, 3);
  }

  @Test
  public void curveConstructionGeneratorBlock0() {
    // Curve constructed with present value and today fixing
    curveConstructionTest(NAMES_UNITS[0], DEFINITIONS_UNITS[0], CURVES_PRESENT_VALUE_WITH_TODAY_BLOCK0.getFirst(), true, 0);
    // Curve constructed with par spread (market quote) and  today fixing
    curveConstructionTest(NAMES_UNITS[0], DEFINITIONS_UNITS[0], CURVES_PAR_SPREAD_MQ_WITH_TODAY_BLOCK0.getFirst(), true, 0);
    // Curve constructed with present value and no today fixing
    curveConstructionTest(NAMES_UNITS[0], DEFINITIONS_UNITS[0], CURVES_PRESENT_VALUE_WITHOUT_TODAY_BLOCK0.getFirst(), false, 0);
    // Curve constructed with par spread (market quote) and no today fixing
    curveConstructionTest(NAMES_UNITS[0], DEFINITIONS_UNITS[0], CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK0.getFirst(), false, 0);
  }

  @Test
  public void curveConstructionGeneratorBlock1() {
    // Curve constructed with par spread (market quote) and  today fixing
    curveConstructionTest(NAMES_UNITS[1], DEFINITIONS_UNITS[1], CURVES_PAR_SPREAD_MQ_WITH_TODAY_BLOCK1.getFirst(), true, 1);
  }

  @Test
  public void curveConstructionGeneratorBlock2() {
    // Curve constructed with par spread (market quote) and  today fixing
    curveConstructionTest(NAMES_UNITS[2], DEFINITIONS_UNITS[2], CURVES_PAR_SPREAD_MQ_WITH_TODAY_BLOCK2.getFirst(), true, 2);
  }

  @Test
  public void curveConstructionGeneratorBlock3() {
    // Curve constructed with par spread (market quote) and  today fixing
    curveConstructionTest(NAMES_UNITS[3], DEFINITIONS_UNITS[3], CURVES_PAR_SPREAD_MQ_WITH_TODAY_BLOCK3.getFirst(), true, 3);
  }

  public void curveConstructionTest(String[][] curveNames, final InstrumentDefinition<?>[][][] definitions, final YieldCurveBundle curves, final boolean withToday, int block) {
    int nbBlocks = definitions.length;
    for (int loopblock = 0; loopblock < nbBlocks; loopblock++) {
      InstrumentDerivative[] instruments = convert(curveNames, definitions[loopblock], loopblock, withToday, block);
      double[] pv = new double[instruments.length];
      for (int loopins = 0; loopins < instruments.length; loopins++) {
        pv[loopins] = curves.getFxRates().convert(PV_CALCULATOR.visit(instruments[loopins], curves), CCY_USD).getAmount();
        assertEquals("Curve construction: node block " + loopblock + " - instrument " + loopins, 0, pv[loopins], TOLERANCE_PV);
      }
    }
  }

  @Test(enabled = false)
  public void performance() {
    long startTime, endTime;
    final int nbTest = 100;
    @SuppressWarnings("unused")
    Pair<YieldCurveBundle, CurveBuildingBlockBundle> curvePresentValue;
    @SuppressWarnings("unused")
    Pair<YieldCurveBundle, CurveBuildingBlockBundle> curveParSpreadMQ0;
    @SuppressWarnings("unused")
    Pair<YieldCurveBundle, CurveBuildingBlockBundle> curveParSpreadMQ1;
    int nbIns0 = DEFINITIONS_DSC_USD.length + DEFINITIONS_FWD3_USD.length;
    int nbIns1 = DEFINITIONS_DSC_USD.length + DEFINITIONS_FWD3_USD.length + DEFINITIONS_FWD6_USD.length;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      curvePresentValue = makeCurves(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PV_CONVERTED_CALCULATOR, PVCS_CONVERTED_CALCULATOR, true, 1);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction Full 2 curves (Int / Cst+Int) and " + nbIns0 + " instruments (with present value): " + (endTime - startTime) + " ms");
    // Performance note: curve construction (with present value, 2 curves (Int / Cst+Int) and 21 instruments by units): 23-Jul-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 320 ms for 100 bundles.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      curveParSpreadMQ0 = makeCurves(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQ_CALCULATOR, PSMQCS_CALCULATOR, true, 1);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction Full 2 curves (Int / Cst+Int) and " + nbIns0 + " instruments (with par spread-market quote): " + (endTime - startTime) + " ms");
    // Performance note: curve construction (with par spread/market quote), 2 curves (Int / Cst+Int) and 21 instruments by units): 23-Jul-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 375 ms for 100 bundles.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      curveParSpreadMQ1 = makeCurves(DEFINITIONS_UNITS[1], GENERATORS_UNITS[1], NAMES_UNITS[1], KNOWN_DATA, PSMQ_CALCULATOR, PSMQCS_CALCULATOR, true, 1);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction Full 3 curves (Int / Int / Fwd3+int) and " + nbIns1 + " instruments (with par spread-market quote): " + (endTime - startTime) + " ms");
    // Performance note: curve construction (with par spread/market quote), 3 curves (Int / Int / Fwd3+int) and 25 instruments by units): 23-Jul-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 550 ms for 100 bundles.
  }

  @Test(enabled = true)
  /**
   * Code used to graph the curves
   */
  public void analysis() {
    int nbPoints = 110;
    double endTime = 11.0;
    double[] x = new double[nbPoints + 1];
    for (int looppt = 0; looppt <= nbPoints; looppt++) {
      x[looppt] = looppt * endTime / nbPoints;
    }

    YieldCurveBundle bundle1 = CURVES_PAR_SPREAD_MQ_WITH_TODAY_BLOCK1.getFirst();
    Set<String> curveNames1 = bundle1.getAllNames();
    int nbCurve1 = curveNames1.size();

    double[][] rate1 = new double[nbCurve1][nbPoints + 1];
    int loopc = 0;
    for (String name : curveNames1) {
      for (int looppt = 0; looppt <= nbPoints; looppt++) {
        rate1[loopc][looppt] = bundle1.getCurve(name).getInterestRate(x[looppt]);
      }
      loopc++;
    }
    YieldCurveBundle bundle2 = CURVES_PAR_SPREAD_MQ_WITH_TODAY_BLOCK2.getFirst();
    double[][] rate2 = new double[nbCurve1][nbPoints + 1];
    loopc = 0;
    for (String name : curveNames1) {
      for (int looppt = 0; looppt <= nbPoints; looppt++) {
        rate2[loopc][looppt] = bundle2.getCurve(name).getInterestRate(x[looppt]);
      }
      loopc++;
    }
    YieldCurveBundle bundle3 = CURVES_PAR_SPREAD_MQ_WITH_TODAY_BLOCK3.getFirst();
    Set<String> curveNames3 = bundle3.getAllNames();
    int nbCurve3 = curveNames3.size();
    double[][] rate3 = new double[nbCurve3][nbPoints + 1];
    loopc = 0;
    for (String name : curveNames3) {
      for (int looppt = 0; looppt <= nbPoints; looppt++) {
        rate3[loopc][looppt] = bundle3.getCurve(name).getInterestRate(x[looppt]);
      }
      loopc++;
    }
    int t = 0;
    t++;
  }

  public static InstrumentDefinition<?>[] getDefinitions(final double[] marketQuotes, final Generator[] generators, final Period[] tenors, final Double[] other) {
    final InstrumentDefinition<?>[] definitions = new InstrumentDefinition<?>[marketQuotes.length];
    for (int loopmv = 0; loopmv < marketQuotes.length; loopmv++) {
      definitions[loopmv] = generators[loopmv].generateInstrument(NOW, tenors[loopmv], marketQuotes[loopmv], NOTIONAL, other[loopmv]);
    }
    return definitions;
  }

  private static Pair<YieldCurveBundle, Double[]> makeUnit(InstrumentDerivative[] instruments, double[] initGuess, LinkedHashMap<String, GeneratorCurve> curveGenerators, YieldCurveBundle knownData,
      final AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Double> calculator, final AbstractInstrumentDerivativeVisitor<YieldCurveBundle, InterestRateCurveSensitivity> sensitivityCalculator) {
    final MultipleYieldCurveFinderGeneratorDataBundle data = new MultipleYieldCurveFinderGeneratorDataBundle(instruments, knownData, curveGenerators);
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new MultipleYieldCurveFinderGeneratorFunction(calculator, data);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MultipleYieldCurveFinderGeneratorJacobian(new ParameterUnderlyingSensitivityCalculator(sensitivityCalculator), data);
    //    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MultipleYieldCurveFinderGeneratorJacobian(new ParameterSensitivityCalculator(sensitivityCalculator), data);
    final double[] parameters = ROOT_FINDER.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initGuess)).getData();
    final YieldCurveBundle newCurves = data.getBuildingFunction().evaluate(new DoubleMatrix1D(parameters));
    return new ObjectsPair<YieldCurveBundle, Double[]>(newCurves, ArrayUtils.toObject(parameters));
  }

  private static DoubleMatrix2D[] makeCurveMatrix(InstrumentDerivative[] instrumentsTotal, LinkedHashMap<String, GeneratorCurve> curveGeneratorsTotal, int startBlock, int[] nbParameters,
      Double[] parametersTotal, YieldCurveBundle knownData, final AbstractInstrumentDerivativeVisitor<YieldCurveBundle, InterestRateCurveSensitivity> sensitivityCalculator) {
    final MultipleYieldCurveFinderGeneratorDataBundle data = new MultipleYieldCurveFinderGeneratorDataBundle(instrumentsTotal, knownData, curveGeneratorsTotal);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MultipleYieldCurveFinderGeneratorJacobian(new ParameterSensitivityCalculator(sensitivityCalculator), data);
    final DoubleMatrix2D jacobian = jacobianCalculator.evaluate(new DoubleMatrix1D(parametersTotal));
    final DoubleMatrix2D inverseJacobian = MATRIX_ALGEBRA.getInverse(jacobian);
    double[][] matrixTotal = inverseJacobian.getData();
    DoubleMatrix2D[] result = new DoubleMatrix2D[nbParameters.length];
    int startCurve = 0;
    for (int loopmat = 0; loopmat < nbParameters.length; loopmat++) {
      double[][] matrixCurve = new double[nbParameters[loopmat]][matrixTotal.length];
      for (int loopparam = 0; loopparam < nbParameters[loopmat]; loopparam++) {
        matrixCurve[loopparam] = matrixTotal[startBlock + startCurve + loopparam].clone();
      }
      result[loopmat] = new DoubleMatrix2D(matrixCurve);
      startCurve += nbParameters[loopmat];
    }
    return result;
  }

  private static Pair<YieldCurveBundle, CurveBuildingBlockBundle> makeCurves(final InstrumentDefinition<?>[][][] definitions, GeneratorCurve[][] curveGenerators, String[][] curveNames,
      YieldCurveBundle knownData, final AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Double> calculator,
      final AbstractInstrumentDerivativeVisitor<YieldCurveBundle, InterestRateCurveSensitivity> sensitivityCalculator, boolean withToday, int block) {
    int nbUnits = curveGenerators.length;
    YieldCurveBundle knownSoFarData = knownData.copy();
    List<InstrumentDerivative> instrumentsSoFar = new ArrayList<InstrumentDerivative>();
    LinkedHashMap<String, GeneratorCurve> generatorsSoFar = new LinkedHashMap<String, GeneratorCurve>();
    LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> unitBundleSoFar = new LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>>();
    List<Double> parametersSoFar = new ArrayList<Double>();
    LinkedHashMap<String, Pair<Integer, Integer>> unitMap = new LinkedHashMap<String, Pair<Integer, Integer>>();
    int start = 0;
    for (int loopunit = 0; loopunit < nbUnits; loopunit++) {
      int startBlock = 0;
      InstrumentDerivative[] instruments = convert(curveNames, definitions[loopunit], loopunit, withToday, block);
      instrumentsSoFar.addAll(Arrays.asList(instruments));
      InstrumentDerivative[] instrumentsSoFarArray = instrumentsSoFar.toArray(new InstrumentDerivative[0]);
      LinkedHashMap<String, GeneratorCurve> gen = new LinkedHashMap<String, GeneratorCurve>();
      int[] nbIns = new int[curveGenerators[loopunit].length];
      int nbInsTotal = 0;
      for (int loopcurve = 0; loopcurve < curveGenerators[loopunit].length; loopcurve++) {
        nbIns[loopcurve] = definitions[loopunit][loopcurve].length;
        nbInsTotal += nbIns[loopcurve];
      }
      double[] initGuess = new double[nbInsTotal];
      for (int loopcurve = 0; loopcurve < curveGenerators[loopunit].length; loopcurve++) {
        InstrumentDerivative[] insCurve = new InstrumentDerivative[nbIns[loopcurve]];
        System.arraycopy(instruments, startBlock, insCurve, 0, nbIns[loopcurve]);
        GeneratorCurve tmp = curveGenerators[loopunit][loopcurve].finalGenerator(insCurve);
        double[] guessCurve = tmp.initialGuess(initialGuess(definitions[loopunit][loopcurve]));
        System.arraycopy(guessCurve, 0, initGuess, startBlock, nbIns[loopcurve]);
        // Need a better initial guess for functional curves.
        gen.put(curveNames[loopunit][loopcurve], tmp);
        generatorsSoFar.put(curveNames[loopunit][loopcurve], tmp);
        unitMap.put(curveNames[loopunit][loopcurve], new ObjectsPair<Integer, Integer>(start + startBlock, nbIns[loopcurve]));
        startBlock += nbIns[loopcurve];
      }
      //      if (loopunit == 1) {
      //        initGuess = new double[] {0.018, -0.01, 0.001, 2.0, 0.0, 0.0, 0.0, 0.0};
      //      }
      Pair<YieldCurveBundle, Double[]> unitCal = makeUnit(instruments, initGuess, gen, knownSoFarData, calculator, sensitivityCalculator);
      parametersSoFar.addAll(Arrays.asList(unitCal.getSecond()));
      DoubleMatrix2D[] mat = makeCurveMatrix(instrumentsSoFarArray, generatorsSoFar, start, nbIns, parametersSoFar.toArray(new Double[0]), knownData, sensitivityCalculator);
      for (int loopcurve = 0; loopcurve < curveGenerators[loopunit].length; loopcurve++) {
        unitBundleSoFar.put(curveNames[loopunit][loopcurve], new ObjectsPair<CurveBuildingBlock, DoubleMatrix2D>(new CurveBuildingBlock(unitMap), mat[loopcurve]));
      }
      knownSoFarData.addAll(unitCal.getFirst());
      start = start + startBlock;
    }
    return new ObjectsPair<YieldCurveBundle, CurveBuildingBlockBundle>(knownSoFarData, new CurveBuildingBlockBundle(unitBundleSoFar));
  }

  @SuppressWarnings("unchecked")
  private static InstrumentDerivative[] convert(String[][] curveNames, InstrumentDefinition<?>[][] definitions, int unit, boolean withToday, int block) {
    int nbDef = 0;
    for (int loopdef1 = 0; loopdef1 < definitions.length; loopdef1++) {
      nbDef += definitions[loopdef1].length;
    }
    final InstrumentDerivative[] instruments = new InstrumentDerivative[nbDef];
    int loopins = 0;
    for (int loopcurve = 0; loopcurve < definitions.length; loopcurve++) {
      for (final InstrumentDefinition<?> instrument : definitions[loopcurve]) {
        InstrumentDerivative ird;
        if (instrument instanceof SwapFixedONDefinition) {
          final String[] names = getCurvesNameSwapFixedON(curveNames, block);
          ird = ((SwapFixedONDefinition) instrument).toDerivative(NOW, getTSSwapFixedON(withToday, unit), names);
        } else {
          if (instrument instanceof SwapFixedIborDefinition) {
            final String[] names = getCurvesNameSwapFixedIbor(curveNames, unit, block);
            ird = ((SwapFixedIborDefinition) instrument).toDerivative(NOW, getTSSwapFixedIbor(withToday, unit), names);
          } else {
            final String[] names;
            // Cash
            names = getCurvesNameCash(curveNames, loopcurve, unit, block);
            ird = instrument.toDerivative(NOW, names);
          }
        }
        instruments[loopins++] = ird;
      }
    }
    return instruments;
  }

  private static double initialGuess(InstrumentDefinition<?> instrument) {
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

  private static double[] initialGuess(InstrumentDefinition<?>[] definitions) {
    double[] result = new double[definitions.length];
    int loopr = 0;
    for (int loopcurve = 0; loopcurve < definitions.length; loopcurve++) {
      result[loopr++] = initialGuess(definitions[loopcurve]);
    }
    return result;
  }

  private static String[] getCurvesNameSwapFixedON(String[][] curveNames, Integer block) {
    switch (block) {
      case 0:
      case 1:
      case 2:
        return new String[] {curveNames[0][0], curveNames[0][0]};
      case 3:
        return new String[] {curveNames[0][1], curveNames[0][1]};
      default:
        throw new IllegalArgumentException(block.toString());
    }
  }

  private static String[] getCurvesNameSwapFixedIbor(String[][] curveNames, Integer unit, Integer block) {
    switch (unit) {
      case 0:
        switch (block) {
          case 0:
          case 1:
          case 2:
            return new String[] {curveNames[0][0], curveNames[1][0]};
          case 3:
            return new String[] {curveNames[0][1], curveNames[0][0]};
          default:
            throw new IllegalArgumentException(block.toString());
        }
      case 1:
        return new String[] {curveNames[0][0], curveNames[1][0]};
      case 2:
        return new String[] {curveNames[0][0], curveNames[2][0]};
      default:
        throw new IllegalArgumentException(unit.toString());
    }
  }

  private static String[] getCurvesNameCash(String[][] curveNames, Integer loopcurve, Integer unit, Integer block) {
    switch (unit) {
      case 0:
        switch (block) {
          case 0:
          case 1:
          case 2:
            return new String[] {curveNames[0][0]};
          case 3:
            return new String[] {curveNames[0][loopcurve]};
          default:
            throw new IllegalArgumentException(block.toString());
        }
      case 1:
        return new String[] {curveNames[1][0]};
      case 2:
        return new String[] {curveNames[2][0]};
      default:
        throw new IllegalArgumentException(unit.toString());
    }
  }

  @SuppressWarnings("rawtypes")
  private static DoubleTimeSeries[] getTSSwapFixedON(Boolean withToday, Integer unit) {
    switch (unit) {
      case 0:
        return withToday ? TS_FIXED_OIS_USD_WITH_TODAY : TS_FIXED_OIS_USD_WITHOUT_TODAY;
      default:
        throw new IllegalArgumentException(unit.toString());
    }
  }

  @SuppressWarnings("rawtypes")
  private static DoubleTimeSeries[] getTSSwapFixedIbor(Boolean withToday, Integer unit) {
    switch (unit) {
      case 0:
        return withToday ? TS_FIXED_IBOR_USD3M_WITH_TODAY : TS_FIXED_IBOR_USD3M_WITHOUT_TODAY;
      case 1:
        return withToday ? TS_FIXED_IBOR_USD3M_WITH_TODAY : TS_FIXED_IBOR_USD3M_WITHOUT_TODAY;
      case 2:
        return withToday ? TS_FIXED_IBOR_USD6M_WITH_TODAY : TS_FIXED_IBOR_USD6M_WITHOUT_TODAY;
      default:
        throw new IllegalArgumentException(unit.toString());
    }
  }

}
