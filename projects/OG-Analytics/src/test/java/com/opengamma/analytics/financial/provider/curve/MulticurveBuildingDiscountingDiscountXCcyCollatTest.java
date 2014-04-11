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

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeFX;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositON;
import com.opengamma.analytics.financial.instrument.index.GeneratorFRA;
import com.opengamma.analytics.financial.instrument.index.GeneratorForexSwap;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapXCcyIborIbor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapXCcyIborIborDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
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
 * Two curves in USD, two curves in EUR and one EUR curve with USD collateral (from X-Ccy swaps)
 */
@Test(groups = TestGroup.UNIT)
public class MulticurveBuildingDiscountingDiscountXCcyCollatTest {

  private static final Interpolator1D INTERPOLATOR_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();
  private static final double TOLERANCE_ROOT = 1.0E-10;
  private static final int STEP_MAX = 100;

  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final Currency EUR = Currency.EUR;
  private static final Currency USD = Currency.USD;
  private static final double FX_EURUSD = 1.40;
  private static final FXMatrix FX_MATRIX = new FXMatrix(USD);
  static {
    FX_MATRIX.addCurrency(EUR, USD, FX_EURUSD);
  }

  private static final double NOTIONAL = 1.0;

  private static final GeneratorSwapFixedON GENERATOR_OIS_EUR = GeneratorSwapFixedONMaster.getInstance().getGenerator("EUR1YEONIA", TARGET);
  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GeneratorSwapFixedONMaster.getInstance().getGenerator("USD1YFEDFUND", TARGET);
  private static final IndexON INDEX_ON_EUR = GENERATOR_OIS_EUR.getIndex();
  private static final IndexON INDEX_ON_USD = GENERATOR_OIS_USD.getIndex();
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_EUR = new GeneratorDepositON("EUR Deposit ON", EUR, TARGET, INDEX_ON_EUR.getDayCount());
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_USD = new GeneratorDepositON("USD Deposit ON", USD, TARGET, INDEX_ON_USD.getDayCount());
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("EUR1YEURIBOR3M", TARGET);
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("USD6MLIBOR3M", TARGET);
  private static final IborIndex EURIBOR3M = EUR1YEURIBOR3M.getIborIndex();
  private static final IborIndex USDLIBOR3M = USD6MLIBOR3M.getIborIndex();
  // private static final IborIndex EUROLIBOR3M = new IborIndex(EUR, Period.ofMonths(3), 2, TARGET, EURIBOR3M.getDayCount(), EURIBOR3M.getBusinessDayConvention(), true, "EUROLIBOR3M");
  private static final GeneratorFRA GENERATOR_USD_FRA_3M = new GeneratorFRA("GENERATOR USD FRA 3M", USDLIBOR3M, NYC);
  private static final GeneratorDepositIbor GENERATOR_EURIBOR3M = new GeneratorDepositIbor("GENERATOR_EURIBOR3M", EURIBOR3M, TARGET);
  private static final GeneratorDepositIbor GENERATOR_USDLIBOR3M = new GeneratorDepositIbor("GENERATOR_USDLIBOR3M", USDLIBOR3M, NYC);
  private static final GeneratorSwapXCcyIborIbor EURIBOR3MUSDLIBOR3M = new GeneratorSwapXCcyIborIbor("EURIBOR3MUSDLIBOR3M", EURIBOR3M, USDLIBOR3M, TARGET, NYC); // Spread on EUR leg
  private static final GeneratorForexSwap GENERATOR_FX_EURUSD = new GeneratorForexSwap("EURUSD", EUR, USD, TARGET, EURIBOR3M.getSpotLag(), EURIBOR3M.getBusinessDayConvention(), true);

  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2011, 9, 28);

  private static final ZonedDateTimeDoubleTimeSeries TS_EMPTY = ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC();
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
      DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
      DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_USD_WITH_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITH_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_USD_WITHOUT_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITHOUT_TODAY };

  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
      DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.0035, 0.0036 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27) },
      new double[] {0.0035 });

  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_EUR3M_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
      DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.0060, 0.0061 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_EUR3M_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27) },
      new double[] {0.0060 });

  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_EUR3M_WITH_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_EUR3M_WITH_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_EUR3M_WITHOUT_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_EUR3M_WITHOUT_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_EURUSD3M_WITH_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_EUR3M_WITH_TODAY, TS_IBOR_USD3M_WITH_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_EURUSD3M_WITHOUT_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_EUR3M_WITHOUT_TODAY, TS_IBOR_USD3M_WITHOUT_TODAY };

  private static final String CURVE_NAME_USD_DSC_FF = "USD Dsc FedFund";
  private static final String CURVE_NAME_USD_FWD_L3 = "USD Fwd Libor3M";
  private static final String CURVE_NAME_EUR_DSC_EO = "EUR Dsc Eonia";
  private static final String CURVE_NAME_EUR_FWD_E3 = "EUR Fwd Euribor3M";
  private static final String CURVE_NAME_EUR_DSC_USDFF = "EUR Dsc USD FedFund";

  /** Market values for the dsc USD FF curve */
  private static final double[] USD_DSC_FF_MARKET_QUOTES = new double[] {0.0015, 0.0015, 0.0010, 0.0010, 0.0010, 0.0010, 0.0010, 0.0010, 0.0015, 0.0020, 0.0035, 0.0050, 0.0130 };
  /** Generators for the dsc USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] USD_DSC_FF_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_USD, GENERATOR_DEPOSIT_ON_USD, GENERATOR_OIS_USD,
    GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD };
  /** Tenors for the dsc USD curve */
  private static final Period[] USD_DSC_FF_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1), Period.ofMonths(1), Period.ofMonths(2),
    Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
  private static final GeneratorAttributeIR[] USD_DSC_FF_ATTR = new GeneratorAttributeIR[USD_DSC_FF_TENOR.length];
  static {
    for (int loopins = 0; loopins < 2; loopins++) {
      USD_DSC_FF_ATTR[loopins] = new GeneratorAttributeIR(USD_DSC_FF_TENOR[loopins], Period.ZERO);
    }
    for (int loopins = 2; loopins < USD_DSC_FF_TENOR.length; loopins++) {
      USD_DSC_FF_ATTR[loopins] = new GeneratorAttributeIR(USD_DSC_FF_TENOR[loopins]);
    }
  }

  /** Market values for the Fwd 3M USD curve */
  private static final double[] USD_FWD_L3_MARKET_QUOTES = new double[] {0.0045, 0.0045, 0.0045, 0.0045, 0.0060, 0.0070, 0.0080, 0.0160 };
  /** Generators for the Fwd 3M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] USD_FWD_L3_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_USDLIBOR3M, GENERATOR_USD_FRA_3M, USD6MLIBOR3M,
    USD6MLIBOR3M,
    USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M };
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] USD_FWD_L3_TENOR = new Period[] {Period.ofMonths(0), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2),
    Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(10) };
  private static final GeneratorAttributeIR[] USD_FWD_L3_ATTR = new GeneratorAttributeIR[USD_FWD_L3_TENOR.length];
  static {
    for (int loopins = 0; loopins < USD_FWD_L3_TENOR.length; loopins++) {
      USD_FWD_L3_ATTR[loopins] = new GeneratorAttributeIR(USD_FWD_L3_TENOR[loopins]);
    }
  }

  /** Market values for the dsc EUR Eonia curve */
  private static final double[] EUR_DSC_EO_MARKET_QUOTES = new double[] {0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400, 0.0400 };
  /** Generators for the dsc USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] EUR_DSC_EO_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR,
    GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR };
  /** Tenors for the dsc USD curve */
  private static final Period[] EUR_DSC_EO_TENOR = new Period[] {Period.ofDays(0), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3),
    Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1),
    Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
  private static final GeneratorAttributeIR[] EUR_DSC_EO_ATTR = new GeneratorAttributeIR[EUR_DSC_EO_TENOR.length];
  static {
    for (int loopins = 0; loopins < EUR_DSC_EO_TENOR.length; loopins++) {
      EUR_DSC_EO_ATTR[loopins] = new GeneratorAttributeIR(EUR_DSC_EO_TENOR[loopins]);
    }
  }

  /** Market values for the Fwd 3M EUR curve */
  private static final double[] EUR_FWD_E3_MARKET_QUOTES = new double[] {0.0045, 0.0045, 0.0045, 0.0045, 0.0050, 0.0060, 0.0085, 0.0160 };
  /** Generators for the Fwd 3M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] EUR_FWD_E3_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_EURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M,
    EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M };
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] EUR_FWD_E3_TENOR = new Period[] {Period.ofMonths(0), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2),
    Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(10) };
  private static final GeneratorAttributeIR[] EUR_FWD_E3_ATTR = new GeneratorAttributeIR[EUR_FWD_E3_TENOR.length];
  static {
    for (int loopins = 0; loopins < EUR_FWD_E3_TENOR.length; loopins++) {
      EUR_FWD_E3_ATTR[loopins] = new GeneratorAttributeIR(EUR_FWD_E3_TENOR[loopins]);
    }
  }

  /** Market values for the dsc EUR for USD FF collateral curve */
  private static final double[] EUR_DSC_USDFF_MARKET_QUOTES = new double[] {0.0010, 0.0010, 0.00025, 0.0005, 0.00075, 0.0020, 0.0030, 0.0038, -0.0050, -0.0050, -0.0050, -0.0045, -0.0040 };
  /** Generators for the dsc EUR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] EUR_DSC_USDFF_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_EUR, GENERATOR_DEPOSIT_ON_EUR,
    GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M,
    EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M };
  /** Tenors for the dsc EUR curve */
  private static final Period[] EUR_DSC_USDFF_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6),
    Period.ofMonths(9), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
  private static final GeneratorAttribute[] EUR_DSC_USDFF_ATTR = new GeneratorAttribute[EUR_DSC_USDFF_TENOR.length];
  static {
    for (int loopins = 0; loopins < 2; loopins++) {
      EUR_DSC_USDFF_ATTR[loopins] = new GeneratorAttributeIR(EUR_DSC_USDFF_TENOR[loopins], Period.ZERO);
    }
    for (int loopins = 2; loopins < EUR_DSC_USDFF_TENOR.length; loopins++) {
      EUR_DSC_USDFF_ATTR[loopins] = new GeneratorAttributeFX(EUR_DSC_USDFF_TENOR[loopins], FX_MATRIX);
    }
  }

  /** Standard USD discounting curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_USD_DSC_FF;
  /** Standard USD Forward 3M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_USD_FWD_L3;
  /** Standard EUR discounting curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_EUR_DSC_EO;
  /** Standard EUR Forward 3M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_EUR_FWD_E3;
  /** Standard EUR Forward 3M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_EUR_DSC_USDFF;

  /** Units of curves */
  private static final int[] NB_UNITS = new int[] {4, 1 };
  private static final int NB_BLOCKS = NB_UNITS.length;
  private static final InstrumentDefinition<?>[][][][] DEFINITIONS_UNITS = new InstrumentDefinition<?>[NB_BLOCKS][][][];
  private static final GeneratorYDCurve[][][] GENERATORS_UNITS = new GeneratorYDCurve[NB_BLOCKS][][];
  private static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];

  private static final MulticurveProviderDiscount MULTICURVE_KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);

  private static final LinkedHashMap<String, Currency> DSC_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IborIndex[]> FWD_IBOR_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, Currency> DSC_MAP_2 = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP_2 = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IborIndex[]> FWD_IBOR_MAP_2 = new LinkedHashMap<>();

  static {
    DEFINITIONS_USD_DSC_FF = getDefinitions(USD_DSC_FF_MARKET_QUOTES, USD_DSC_FF_GENERATORS, USD_DSC_FF_ATTR);
    DEFINITIONS_USD_FWD_L3 = getDefinitions(USD_FWD_L3_MARKET_QUOTES, USD_FWD_L3_GENERATORS, USD_FWD_L3_ATTR);
    DEFINITIONS_EUR_DSC_EO = getDefinitions(EUR_DSC_EO_MARKET_QUOTES, EUR_DSC_EO_GENERATORS, EUR_DSC_EO_ATTR);
    DEFINITIONS_EUR_FWD_E3 = getDefinitions(EUR_FWD_E3_MARKET_QUOTES, EUR_FWD_E3_GENERATORS, EUR_FWD_E3_ATTR);
    DEFINITIONS_EUR_DSC_USDFF = getDefinitions(EUR_DSC_USDFF_MARKET_QUOTES, EUR_DSC_USDFF_GENERATORS, EUR_DSC_USDFF_ATTR);

    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      DEFINITIONS_UNITS[loopblock] = new InstrumentDefinition<?>[NB_UNITS[loopblock]][][];
      GENERATORS_UNITS[loopblock] = new GeneratorYDCurve[NB_UNITS[loopblock]][];
      NAMES_UNITS[loopblock] = new String[NB_UNITS[loopblock]][];
    }
    DEFINITIONS_UNITS[0] = new InstrumentDefinition<?>[NB_UNITS[0]][][];
    DEFINITIONS_UNITS[0][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_USD_DSC_FF };
    DEFINITIONS_UNITS[0][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_USD_FWD_L3 };
    DEFINITIONS_UNITS[0][2] = new InstrumentDefinition<?>[][] {DEFINITIONS_EUR_DSC_EO };
    DEFINITIONS_UNITS[0][3] = new InstrumentDefinition<?>[][] {DEFINITIONS_EUR_FWD_E3 };
    DEFINITIONS_UNITS[1][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_EUR_DSC_USDFF };
    final GeneratorYDCurve genIntLin = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_LINEAR);
    GENERATORS_UNITS[0] = new GeneratorYDCurve[NB_UNITS[0]][];
    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[0][1] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[0][2] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[0][3] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[1][0] = new GeneratorYDCurve[] {genIntLin };
    NAMES_UNITS[0] = new String[NB_UNITS[0]][];
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_USD_DSC_FF };
    NAMES_UNITS[0][1] = new String[] {CURVE_NAME_USD_FWD_L3 };
    NAMES_UNITS[0][2] = new String[] {CURVE_NAME_EUR_DSC_EO };
    NAMES_UNITS[0][3] = new String[] {CURVE_NAME_EUR_FWD_E3 };
    NAMES_UNITS[1][0] = new String[] {CURVE_NAME_EUR_DSC_USDFF };
    DSC_MAP.put(CURVE_NAME_USD_DSC_FF, USD);
    DSC_MAP.put(CURVE_NAME_EUR_DSC_EO, EUR);
    FWD_ON_MAP.put(CURVE_NAME_USD_DSC_FF, new IndexON[] {INDEX_ON_USD });
    FWD_ON_MAP.put(CURVE_NAME_EUR_DSC_EO, new IndexON[] {INDEX_ON_EUR });
    FWD_IBOR_MAP.put(CURVE_NAME_USD_FWD_L3, new IborIndex[] {USDLIBOR3M });
    FWD_IBOR_MAP.put(CURVE_NAME_EUR_FWD_E3, new IborIndex[] {EURIBOR3M });
    DSC_MAP_2.put(CURVE_NAME_EUR_DSC_USDFF, EUR);
  }

  @SuppressWarnings("unchecked")
  public static InstrumentDefinition<?>[] getDefinitions(final double[] marketQuotes, @SuppressWarnings("rawtypes") final GeneratorInstrument[] generators, final GeneratorAttribute[] attribute) {
    final InstrumentDefinition<?>[] definitions = new InstrumentDefinition<?>[marketQuotes.length];
    for (int loopmv = 0; loopmv < marketQuotes.length; loopmv++) {
      definitions[loopmv] = generators[loopmv].generateInstrument(NOW, marketQuotes[loopmv], NOTIONAL, attribute[loopmv]);
    }
    return definitions;
  }

  private static List<Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle>> CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK = new ArrayList<>();

  // Calculators
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQDC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSDC = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();

  private static final MulticurveDiscountBuildingRepository CURVE_BUILDING_REPOSITORY = new MulticurveDiscountBuildingRepository(TOLERANCE_ROOT, TOLERANCE_ROOT, STEP_MAX);

  private static final double TOLERANCE_CAL = 1.0E-9;

  @BeforeSuite
  static void initClass() {
    CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.add(makeCurvesFromDefinitions(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], MULTICURVE_KNOWN_DATA, PSMQDC,
        PSMQCSDC, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP, false)); // USD and EUR curves
    final MulticurveProviderDiscount knownCurves = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0).getFirst().copy();
    knownCurves.removeCurve(EUR);
    final CurveBuildingBlockBundle knownBundle = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0).getSecond();
    CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.add(makeCurvesFromDefinitions(DEFINITIONS_UNITS[1], GENERATORS_UNITS[1], NAMES_UNITS[1], knownCurves, knownBundle, PSMQDC,
        PSMQCSDC, DSC_MAP_2, FWD_IBOR_MAP_2, FWD_ON_MAP_2, false)); // EUR with USD FF collateral dsc curve
  }

  @Test
  public void curveConstruction() {
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      curveConstructionTest(DEFINITIONS_UNITS[loopblock], CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(loopblock).getFirst(), false, loopblock);
    }
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
      final String[][] curveNames, final MulticurveProviderDiscount knownData,
      final InstrumentDerivativeVisitor<MulticurveProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<MulticurveProviderInterface, MulticurveSensitivity> sensitivityCalculator, final LinkedHashMap<String, Currency> dscMap,
      final LinkedHashMap<String, IborIndex[]> fwdIborMap, final LinkedHashMap<String, IndexON[]> fwdOnMap, final boolean withToday) {
    final int nUnits = definitions.length;
    final MultiCurveBundle<GeneratorYDCurve>[] curveBundles = new MultiCurveBundle[nUnits];
    for (int i = 0; i < nUnits; i++) {
      final int nCurves = definitions[i].length;
      final SingleCurveBundle<GeneratorYDCurve>[] singleCurves = new SingleCurveBundle[nCurves];
      for (int j = 0; j < nCurves; j++) {
        final int nInstruments = definitions[i][j].length;
        final InstrumentDerivative[] derivatives = new InstrumentDerivative[nInstruments];
        final double[] rates = new double[nInstruments];
        for(int k = 0; k < nInstruments; k++) {
          derivatives[k] = convert(definitions[i][j][k], withToday);
          rates[k] = initialGuess(definitions[i][j][k]);
        }
        final GeneratorYDCurve generator = curveGenerators[i][j].finalGenerator(derivatives);
        final double[] initialGuess = generator.initialGuess(rates);
        singleCurves[j] = new SingleCurveBundle<>(curveNames[i][j], derivatives, initialGuess, generator);
      }
      curveBundles[i] = new MultiCurveBundle<>(singleCurves);
    }

    return CURVE_BUILDING_REPOSITORY.makeCurvesFromDerivatives(curveBundles, knownData, dscMap, fwdIborMap, fwdOnMap, calculator, sensitivityCalculator);
  }

  @SuppressWarnings("unchecked")
  private static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitions(final InstrumentDefinition<?>[][][] definitions, final GeneratorYDCurve[][] curveGenerators,
      final String[][] curveNames, final MulticurveProviderDiscount knownData, final CurveBuildingBlockBundle knownBundle,
      final InstrumentDerivativeVisitor<MulticurveProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<MulticurveProviderInterface, MulticurveSensitivity> sensitivityCalculator, final LinkedHashMap<String, Currency> dscMap,
      final LinkedHashMap<String, IborIndex[]> fwdIborMap, final LinkedHashMap<String, IndexON[]> fwdOnMap, final boolean withToday) {
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
          rates[k] = initialGuess(definitions[i][j][k]);
        }
        final GeneratorYDCurve generator = curveGenerators[i][j].finalGenerator(derivatives);
        final double[] initialGuess = generator.initialGuess(rates);
        singleCurves[j] = new SingleCurveBundle<>(curveNames[i][j], derivatives, initialGuess, generator);
      }
      curveBundles[i] = new MultiCurveBundle<>(singleCurves);
    }

    return CURVE_BUILDING_REPOSITORY.makeCurvesFromDerivatives(curveBundles, knownData, knownBundle, dscMap, fwdIborMap, fwdOnMap, calculator, sensitivityCalculator);
  }

  private static InstrumentDerivative[][] convert(final InstrumentDefinition<?>[][] definitions, final boolean withToday) {
    final InstrumentDerivative[][] instruments = new InstrumentDerivative[definitions.length][];
    for (int loopcurve = 0; loopcurve < definitions.length; loopcurve++) {
      instruments[loopcurve] = new InstrumentDerivative[definitions[loopcurve].length];
      int loopins = 0;
      for (final InstrumentDefinition<?> instrument : definitions[loopcurve]) {
        InstrumentDerivative ird;
        if (instrument instanceof SwapFixedONDefinition) {
          ird = ((SwapFixedONDefinition) instrument).toDerivative(NOW, getTSSwapFixedON(withToday));
        } else {
          if (instrument instanceof SwapFixedIborDefinition) {
            ird = ((SwapFixedIborDefinition) instrument).toDerivative(NOW, getTSSwapFixedIbor(withToday));
          } else {
            if (instrument instanceof SwapXCcyIborIborDefinition) {
              ird = ((SwapXCcyIborIborDefinition) instrument).toDerivative(NOW, getTSSwapXCcyIborIbor(withToday));
            } else {
              ird = instrument.toDerivative(NOW);
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
      ird = ((SwapFixedONDefinition) instrument).toDerivative(NOW, getTSSwapFixedON(withToday));
    } else {
      if (instrument instanceof SwapFixedIborDefinition) {
        ird = ((SwapFixedIborDefinition) instrument).toDerivative(NOW, getTSSwapFixedIbor(withToday));
      } else {
        if (instrument instanceof SwapXCcyIborIborDefinition) {
          ird = ((SwapXCcyIborIborDefinition) instrument).toDerivative(NOW, getTSSwapXCcyIborIbor(withToday));
        } else {
          ird = instrument.toDerivative(NOW);
        }
      }
    }
    return ird;
  }

  private static ZonedDateTimeDoubleTimeSeries[] getTSSwapFixedON(final Boolean withToday) {
    return withToday ? TS_FIXED_OIS_USD_WITH_TODAY : TS_FIXED_OIS_USD_WITHOUT_TODAY;
  }

  private static ZonedDateTimeDoubleTimeSeries[] getTSSwapFixedIbor(final Boolean withToday) { // TODO: different fixing by currency and for 3 and 6 m
    return withToday ? TS_FIXED_IBOR_EUR3M_WITH_TODAY : TS_FIXED_IBOR_EUR3M_WITHOUT_TODAY;
  }

  private static ZonedDateTimeDoubleTimeSeries[] getTSSwapXCcyIborIbor(final Boolean withToday) { // TODO: different currencies
    return withToday ? TS_FIXED_IBOR_EURUSD3M_WITH_TODAY : TS_FIXED_IBOR_EURUSD3M_WITHOUT_TODAY;
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
    if (instrument instanceof InterestRateFutureTransactionDefinition) {
      return 1 - ((InterestRateFutureTransactionDefinition) instrument).getTransactionPrice();
    }
    return 0.01;
  }

}
