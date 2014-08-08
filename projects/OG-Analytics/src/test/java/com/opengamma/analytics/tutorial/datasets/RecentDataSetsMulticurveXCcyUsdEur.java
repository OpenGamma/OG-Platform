/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.tutorial.datasets;

import java.util.LinkedHashMap;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.datasets.CalendarTarget;
import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
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
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
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
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Curve calibration in USD/USD
 * ONDSC-OIS/LIBOR3M-FRAIRS * 
 */
@Test(groups = TestGroup.UNIT)
public class RecentDataSetsMulticurveXCcyUsdEur {

  private static final Interpolator1D INTERPOLATOR_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();

  private static final Calendar TARGET = new CalendarTarget("TARGET");
  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final Currency EUR = Currency.EUR;
  private static final Currency USD = Currency.USD;
  private static final double FX_EURUSD = 1.35;
  private static final FXMatrix FX_MATRIX = new FXMatrix(USD);
  static {
    FX_MATRIX.addCurrency(EUR, USD, FX_EURUSD);
  }
  private static final MulticurveProviderDiscount KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);

  private static final double NOTIONAL = 1.0;

  private static final IndexIborMaster IBOR_MASTER = IndexIborMaster.getInstance();
  private static final GeneratorSwapFixedONMaster GENERATOR_OIS_MASTER = GeneratorSwapFixedONMaster.getInstance();
  private static final GeneratorSwapFixedIborMaster GENERATOR_IRS_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedON GENERATOR_OIS_EUR = GENERATOR_OIS_MASTER.getGenerator("EUR1YEONIA", TARGET);
  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GENERATOR_OIS_MASTER.getGenerator("USD1YFEDFUND", TARGET);
  private static final IndexON INDEX_ON_EUR = GENERATOR_OIS_EUR.getIndex();
  private static final IndexON INDEX_ON_USD = GENERATOR_OIS_USD.getIndex();
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_EUR = new GeneratorDepositON("EUR Deposit ON", EUR, TARGET, INDEX_ON_EUR.getDayCount());
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_IRS_MASTER.getGenerator("USD6MLIBOR3M", TARGET);
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = GENERATOR_IRS_MASTER.getGenerator("EUR1YEURIBOR3M", TARGET);
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GENERATOR_IRS_MASTER.getGenerator("EUR1YEURIBOR6M", TARGET);
  private static final IborIndex USDLIBOR3M = USD6MLIBOR3M.getIborIndex();
  private static final IborIndex EURIBOR3M = IBOR_MASTER.getIndex("EURIBOR3M");
  private static final IborIndex EURIBOR6M = IBOR_MASTER.getIndex("EURIBOR6M");
  private static final GeneratorFRA GENERATOR_FRA_3M = new GeneratorFRA("GENERATOR_FRA_3M", EURIBOR3M, TARGET);
  private static final GeneratorFRA GENERATOR_FRA_6M = new GeneratorFRA("GENERATOR_FRA_6M", EURIBOR6M, TARGET);
  private static final GeneratorDepositIbor GENERATOR_EURIBOR3M = new GeneratorDepositIbor("GENERATOR_EURIBOR3M", EURIBOR3M, TARGET);
  private static final GeneratorDepositIbor GENERATOR_EURIBOR6M = new GeneratorDepositIbor("GENERATOR_EURIBOR6M", EURIBOR6M, TARGET);
  private static final GeneratorSwapXCcyIborIbor EURIBOR3MUSDLIBOR3M = new GeneratorSwapXCcyIborIbor("EURIBOR3MUSDLIBOR3M", EURIBOR3M, USDLIBOR3M, TARGET, NYC); // Spread on EUR leg
  private static final GeneratorForexSwap GENERATOR_FX_EURUSD = new GeneratorForexSwap("EURUSD", EUR, USD, TARGET, EURIBOR3M.getSpotLag(), EURIBOR3M.getBusinessDayConvention(), true);

  private static final String CURVE_NAME_USD_DSC = "USD-DSCON-OIS";
  private static final String CURVE_NAME_USD_FWD3 = "USD-LIBOR3M-FRAIRS";
  private static final String CURVE_NAME_EUR_EONIA_DSC = "EUR-DSCON-OIS";
  private static final String CURVE_NAME_EUR_EONIA_FWD3 = "EUR-EURIBOR3M-FRAIRS";
  private static final String CURVE_NAME_EUR_EONIA_FWD6 = "EUR-EURIBOR6M-FRAIRS";
  private static final String CURVE_NAME_EUR_FEDFUND_1_DSC = "EUR-USDON-DSC-FXUSD";
  private static final String CURVE_NAME_EUR_FEDFUND_1_FWD3 = "EUR-USDON-EURIBOR3M-FRAXCCY";
  private static final String CURVE_NAME_EUR_FEDFUND_1_FWD6 = "EUR-USDON-EURIBOR6M-FRABS";

  /** Data as of 8-Aug-2014

  /**     ==========          USD - FEDFUND          ==========
  /** Market values for the dsc USD curve. 13 nodes */
  private static final double[] DSC_USD_MARKET_QUOTES = new double[] {0.0013, 0.0015,
    0.0009, 0.0009, 0.0009, 0.0010, 0.0012,
    0.0017, 0.0049, 0.0090, 0.0125, 0.0150,
    0.0230 };
  /** Generators for the dsc USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_USD_GENERATORS =
      CurveCalibrationConventionDataSets.generatorUsdOnOisFfs(2, 11, 0);
  /** Tenors for the dsc USD curve */
  private static final Period[] DSC_USD_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1),
    Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(10) };
  private static final GeneratorAttributeIR[] DSC_USD_ATTR = new GeneratorAttributeIR[DSC_USD_TENOR.length];
  static {
    for (int loopins = 0; loopins < 2; loopins++) {
      DSC_USD_ATTR[loopins] = new GeneratorAttributeIR(DSC_USD_TENOR[loopins], Period.ZERO);
    }
    for (int loopins = 2; loopins < DSC_USD_TENOR.length; loopins++) {
      DSC_USD_ATTR[loopins] = new GeneratorAttributeIR(DSC_USD_TENOR[loopins]);
    }
  }

  /** Market values for the Fwd 3M USD curve. 8 nodes */
  private static final double[] FWD3_USD_MARKET_QUOTES = new double[] {0.0023,
    0.0026,
    0.0032, 0.0066, 0.0108, 0.0145, 0.0174,
    0.0254 };
  /** Generators for the Fwd 3M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_USD_GENERATORS =
      CurveCalibrationConventionDataSets.generatorUsdIbor3Fra3Irs3(1, 1, 6);
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] FWD3_USD_TENOR = new Period[] {Period.ofMonths(0),
    Period.ofMonths(6),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(10) };
  private static final GeneratorAttributeIR[] FWD3_USD_ATTR = new GeneratorAttributeIR[FWD3_USD_TENOR.length];
  static {
    for (int loopins = 0; loopins < FWD3_USD_TENOR.length; loopins++) {
      FWD3_USD_ATTR[loopins] = new GeneratorAttributeIR(FWD3_USD_TENOR[loopins]);
    }
  }

  /**     ==========          EUR - EONIA          ==========
  
  /** Market values for the dsc EUR curve */
  private static final double[] EUR_EONIA_DSC_MARKET_QUOTES = new double[] {0.0006,
    0.0006, 0.0006, 0.0006, 0.0006, 0.0006,
    0.0005, 0.0005, 0.0010, 0.0018, 0.0029,
    0.0096 };
  /** Generators for the dsc USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] EUR_EONIA_DSC_GENERATORS =
      new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_EUR,
        GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR,
        GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR, GENERATOR_OIS_EUR,
        GENERATOR_OIS_EUR };
  /** Tenors for the dsc USD curve */
  private static final Period[] EUR_EONIA_DSC_TENOR = new Period[] {Period.ofDays(0),
    Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(10) };
  private static final GeneratorAttributeIR[] EUR_EONIA_DSC_ATTR = new GeneratorAttributeIR[EUR_EONIA_DSC_TENOR.length];
  static {
    for (int loopins = 0; loopins < EUR_EONIA_DSC_TENOR.length; loopins++) {
      EUR_EONIA_DSC_ATTR[loopins] = new GeneratorAttributeIR(EUR_EONIA_DSC_TENOR[loopins]);
    }
  }

  /** Market values for the Fwd 3M USD curve */
  private static final double[] EUR_EONIA_FWD3_MARKET_QUOTES = new double[] {0.0020,
    0.0020, 0.0019,
    0.0020, 0.0021, 0.0028, 0.0037, 0.0049,
    0.0117 };
  /** Generators for the Fwd 3M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] EUR_EONIA_FWD3_GENERATORS =
      new GeneratorInstrument<?>[] {GENERATOR_EURIBOR3M,
        GENERATOR_FRA_3M, GENERATOR_FRA_3M,
        EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M,
        EUR1YEURIBOR3M };
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] EUR_EONIA_FWD3_TENOR = new Period[] {Period.ofMonths(0),
    Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(10) };
  private static final GeneratorAttributeIR[] EUR_EONIA_FWD3_ATTR = new GeneratorAttributeIR[EUR_EONIA_FWD3_TENOR.length];
  static {
    for (int loopins = 0; loopins < EUR_EONIA_FWD3_TENOR.length; loopins++) {
      EUR_EONIA_FWD3_ATTR[loopins] = new GeneratorAttributeIR(EUR_EONIA_FWD3_TENOR[loopins]);
    }
  }

  /** Market values for the Fwd 6M USD curve */
  private static final double[] EUR_EONIA_FWD6_MARKET_QUOTES = new double[] {0.0030,
    0.0030, 0.0030,
    0.0033, 0.0040, 0.0050, 0.0062, 0.0129 };
  /** Generators for the Fwd 6M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] EUR_EONIA_FWD6_GENERATORS =
      new GeneratorInstrument<?>[] {GENERATOR_EURIBOR6M,
        GENERATOR_FRA_6M, GENERATOR_FRA_6M,
        EUR1YEURIBOR6M, EUR1YEURIBOR6M, EUR1YEURIBOR6M, EUR1YEURIBOR6M, EUR1YEURIBOR6M };
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] EUR_EONIA_FWD6_TENOR = new Period[] {Period.ofMonths(0),
    Period.ofMonths(9), Period.ofMonths(12),
    Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
  private static final GeneratorAttributeIR[] EUR_EONIA_FWD6_ATTR = new GeneratorAttributeIR[EUR_EONIA_FWD6_TENOR.length];
  static {
    for (int loopins = 0; loopins < EUR_EONIA_FWD6_TENOR.length; loopins++) {
      EUR_EONIA_FWD6_ATTR[loopins] = new GeneratorAttributeIR(EUR_EONIA_FWD6_TENOR[loopins]);
    }
  }

  /**     ==========          EUR - FED FUND - 1          ==========

  /** Market values for the dsc EUR curve. Depo ON / FX swaps */
  private static final double[] EUR_FEDFUND_1_DSC_MARKET_QUOTES = new double[] {0.0006, 0.0006,
    0.00015, 0.00033, 0.00050, 0.00108, 0.00125,
    0.0030, 0.0150, 0.0380, 0.0665, 0.0950 };
  /** Generators for the dsc EUR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] EUR_FEDFUND_1_DSC_GENERATORS =
      new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_EUR, GENERATOR_DEPOSIT_ON_EUR,
        GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD,
        GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD };
  /** Tenors for the dsc EUR curve */
  private static final Period[] EUR_FEDFUND_1_DSC_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1),
    Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5) };
  private static final GeneratorAttribute[] EUR_FEDFUND_1_DSC_ATTR = new GeneratorAttribute[EUR_FEDFUND_1_DSC_TENOR.length];
  static {
    for (int loopins = 0; loopins < 2; loopins++) {
      EUR_FEDFUND_1_DSC_ATTR[loopins] = new GeneratorAttributeIR(EUR_FEDFUND_1_DSC_TENOR[loopins], Period.ZERO);
    }
    for (int loopins = 2; loopins < EUR_FEDFUND_1_DSC_TENOR.length; loopins++) {
      EUR_FEDFUND_1_DSC_ATTR[loopins] = new GeneratorAttributeFX(EUR_FEDFUND_1_DSC_TENOR[loopins], FX_MATRIX);
    }
  }

  /** Market values for the Fwd 3M EUR curve. Fixing / XCcy Swaps*/
  private static final double[] EUR_FEDFUND_1_FWD3_MARKET_QUOTES = new double[] {0.0020,
    -0.0010, -0.0010, -0.0010, -0.0010, -0.0009,
    -0.0008 };
  /** Generators for the Fwd 3M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] EUR_FEDFUND_1_FWD3_GENERATORS =
      new GeneratorInstrument<?>[] {GENERATOR_EURIBOR3M,
        EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M,
        EURIBOR3MUSDLIBOR3M };
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] EUR_FEDFUND_1_FWD3_TENOR = new Period[] {Period.ofMonths(0),
    Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4),
    Period.ofYears(5) };
  private static final GeneratorAttribute[] EUR_FEDFUND_1_FWD3_ATTR = new GeneratorAttribute[EUR_FEDFUND_1_FWD3_TENOR.length];
  static {
    for (int loopins = 0; loopins < 1; loopins++) {
      EUR_FEDFUND_1_FWD3_ATTR[loopins] = new GeneratorAttributeIR(EUR_FEDFUND_1_DSC_TENOR[loopins]);
    }
    for (int loopins = 1; loopins < EUR_FEDFUND_1_FWD3_TENOR.length; loopins++) {
      EUR_FEDFUND_1_FWD3_ATTR[loopins] = new GeneratorAttributeFX(EUR_FEDFUND_1_FWD3_TENOR[loopins], FX_MATRIX);
    }
  }

  /** Market values for the Fwd 6M EUR curve. Fixing / Basis swaps*/
  private static final double[] EUR_FEDFUND_1_FWD6_MARKET_QUOTES = new double[] {0.0045,
    0.0045, 0.0045, 0.0045, 0.0050, 0.0060,
    0.0085 };
  /** Generators for the Fwd 6M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] EUR_FEDFUND_1_FWD6_GENERATORS =
      CurveCalibrationConventionDataSets.generatorEurIbor6Fra6Irs6(1, 0, 7);
  /** Tenors for the Fwd 6M USD curve */
  private static final Period[] EUR_FEDFUND_1_FWD6_TENOR = new Period[] {Period.ofMonths(0),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(10) };
  private static final GeneratorAttributeIR[] EUR_FEDFUND_1_FWD6_ATTR = new GeneratorAttributeIR[EUR_FEDFUND_1_FWD6_TENOR.length];
  static {
    for (int loopins = 0; loopins < EUR_FEDFUND_1_FWD6_TENOR.length; loopins++) {
      EUR_FEDFUND_1_FWD6_ATTR[loopins] = new GeneratorAttributeIR(EUR_FEDFUND_1_FWD6_TENOR[loopins]);
    }
  }

  /**     ==========          EUR - FED FUND - 2          ==========

  //  /** Market values for the dsc EUR curve. Deposit ON / FX Swaps / IRS */
  //  private static final double[] DSC_EUR_MARKET_QUOTES_2 = new double[] {0.0010, 0.0010, 0.0004, 0.0009, 0.0015, 0.0035, 0.0050, 0.0060, 0.0045, 0.0050, 0.0060, 0.0085, 0.0160 };
  //  /** Generators for the dsc EUR curve */
  //  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_EUR_GENERATORS_2 =
  //      new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_EUR, GENERATOR_DEPOSIT_ON_EUR,
  //        GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD,
  //        EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M };
  //  /** Tenors for the dsc EUR curve */
  //  private static final Period[] DSC_EUR_TENOR_2 = new Period[] {Period.ofDays(0), Period.ofDays(1), Period.ofMonths(1), Period.ofMonths(2),
  //    Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3),
  //    Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
  //  private static final GeneratorAttribute[] DSC_EUR_ATTR_2 = new GeneratorAttribute[DSC_EUR_TENOR_2.length];
  //  static {
  //    for (int loopins = 0; loopins < 2; loopins++) {
  //      DSC_EUR_ATTR_2[loopins] = new GeneratorAttributeIR(DSC_EUR_TENOR_2[loopins], Period.ZERO);
  //    }
  //    for (int loopins = 2; loopins < 8; loopins++) {
  //      DSC_EUR_ATTR_2[loopins] = new GeneratorAttributeFX(DSC_EUR_TENOR_2[loopins], FX_MATRIX);
  //    }
  //    for (int loopins = 8; loopins < DSC_EUR_TENOR_2.length; loopins++) {
  //      DSC_EUR_ATTR_2[loopins] = new GeneratorAttributeIR(DSC_EUR_TENOR_2[loopins]);
  //    }
  //  }
  //
  //  /** Market values for the Fwd 3M EUR curve. Fixing / IRS / XCcy */
  //  private static final double[] FWD3_EUR_MARKET_QUOTES_2 = new double[] {0.0045, 0.0045, 0.0045, -0.0050, -0.0050, -0.0050, -0.0045, -0.0040 };
  //  /** Generators for the Fwd 3M USD curve */
  //  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_EUR_GENERATORS_2 =
  //      new GeneratorInstrument<?>[] {GENERATOR_EURIBOR3M,
  //        EUR1YEURIBOR3M, EUR1YEURIBOR3M,
  //        EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M };
  //  /** Tenors for the Fwd 3M USD curve */
  //  private static final Period[] FWD3_EUR_TENOR_2 = new Period[] {Period.ofMonths(0), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2),
  //    Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
  //    Period.ofYears(10) };
  //  private static final GeneratorAttribute[] FWD3_EUR_ATTR_2 = new GeneratorAttribute[FWD3_EUR_TENOR_2.length];
  //  static {
  //    for (int loopins = 0; loopins < 3; loopins++) {
  //      FWD3_EUR_ATTR_2[loopins] = new GeneratorAttributeIR(FWD3_EUR_TENOR_2[loopins]);
  //    }
  //    for (int loopins = 3; loopins < FWD3_EUR_TENOR_2.length; loopins++) {
  //      FWD3_EUR_ATTR_2[loopins] = new GeneratorAttributeFX(FWD3_EUR_TENOR_2[loopins], FX_MATRIX);
  //    }
  //  }
  //
  //  /** Market values for the dsc EUR curve. Depo ON / FX Swap / IRS / XCcy  */
  //  private static final double[] DSC_EUR_MARKET_QUOTES_3 = new double[] {0.0010, 0.0010, 0.0004, 0.0009, 0.0015, 0.0035, 0.0050, 0.0060, 0.0045, 0.0050, 0.0060, 0.0085, -0.0040 };
  //  /** Generators for the dsc EUR curve */
  //  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_EUR_GENERATORS_3 =
  //      new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_EUR, GENERATOR_DEPOSIT_ON_EUR,
  //        GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD,
  //        EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EURIBOR3MUSDLIBOR3M };
  //  /** Tenors for the dsc EUR curve */
  //  private static final Period[] DSC_EUR_TENOR_3 = new Period[] {Period.ofDays(0), Period.ofDays(1), Period.ofMonths(1), Period.ofMonths(2),
  //    Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3),
  //    Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
  //  private static final GeneratorAttribute[] DSC_EUR_ATTR_3 = new GeneratorAttribute[DSC_EUR_TENOR_3.length];
  //  static {
  //    for (int loopins = 0; loopins < 2; loopins++) {
  //      DSC_EUR_ATTR_3[loopins] = new GeneratorAttributeIR(DSC_EUR_TENOR_3[loopins], Period.ZERO);
  //    }
  //    for (int loopins = 2; loopins < 8; loopins++) {
  //      DSC_EUR_ATTR_3[loopins] = new GeneratorAttributeFX(DSC_EUR_TENOR_3[loopins], FX_MATRIX);
  //    }
  //    for (int loopins = 8; loopins < DSC_EUR_TENOR_3.length - 1; loopins++) {
  //      DSC_EUR_ATTR_3[loopins] = new GeneratorAttributeIR(DSC_EUR_TENOR_3[loopins]);
  //    }
  //    DSC_EUR_ATTR_3[DSC_EUR_TENOR_3.length - 1] = new GeneratorAttributeFX(DSC_EUR_TENOR_3[DSC_EUR_TENOR_3.length - 1], FX_MATRIX);
  //  }
  //
  //  /** Market values for the Fwd 3M EUR curve. Fixing / IRS / XCcy / IRS */
  //  private static final double[] FWD3_EUR_MARKET_QUOTES_3 = new double[] {0.0045, 0.0045, 0.0045, -0.0050, -0.0050, -0.0050, -0.0045, 0.0160 };
  //  /** Generators for the Fwd 3M USD curve */
  //  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_EUR_GENERATORS_3 =
  //      new GeneratorInstrument<?>[] {GENERATOR_EURIBOR3M,
  //        EUR1YEURIBOR3M, EUR1YEURIBOR3M,
  //        EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M, EUR1YEURIBOR3M };
  //  /** Tenors for the Fwd 3M USD curve */
  //  private static final Period[] FWD3_EUR_TENOR_3 = new Period[] {Period.ofMonths(0), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2),
  //    Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
  //    Period.ofYears(10) };
  //  private static final GeneratorAttribute[] FWD3_EUR_ATTR_3 = new GeneratorAttribute[FWD3_EUR_TENOR_3.length];
  //  static {
  //    for (int loopins = 0; loopins < 3; loopins++) {
  //      FWD3_EUR_ATTR_3[loopins] = new GeneratorAttributeIR(FWD3_EUR_TENOR_3[loopins]);
  //    }
  //    for (int loopins = 3; loopins < FWD3_EUR_TENOR_3.length - 1; loopins++) {
  //      FWD3_EUR_ATTR_3[loopins] = new GeneratorAttributeFX(FWD3_EUR_TENOR_3[loopins], FX_MATRIX);
  //    }
  //    FWD3_EUR_ATTR_3[FWD3_EUR_TENOR_3.length - 1] = new GeneratorAttributeIR(FWD3_EUR_TENOR_3[FWD3_EUR_TENOR_3.length - 1]);
  //  }

  /** Units of curves */
  private static final int[] NB_UNITS = new int[] {5, 5 };
  private static final int NB_BLOCKS = NB_UNITS.length;
  private static final InstrumentDefinition<?>[][][][] DEFINITIONS_UNITS = new InstrumentDefinition<?>[NB_BLOCKS][][][];
  private static final GeneratorYDCurve[][][] GENERATORS_UNITS = new GeneratorYDCurve[NB_BLOCKS][][];
  private static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];

  @SuppressWarnings("unchecked")
  private static final LinkedHashMap<String, Currency>[] DSC_MAP = new LinkedHashMap[NB_BLOCKS];
  @SuppressWarnings("unchecked")
  private static final LinkedHashMap<String, IndexON[]>[] FWD_ON_MAP = new LinkedHashMap[NB_BLOCKS];
  @SuppressWarnings("unchecked")
  private static final LinkedHashMap<String, IborIndex[]>[] FWD_IBOR_MAP = new LinkedHashMap[NB_BLOCKS];

  static {
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      DEFINITIONS_UNITS[loopblock] = new InstrumentDefinition<?>[NB_UNITS[loopblock]][][];
      NAMES_UNITS[loopblock] = new String[NB_UNITS[loopblock]][];
    }
    final GeneratorYDCurve genIntLin = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_LINEAR);
    GENERATORS_UNITS[0] = new GeneratorYDCurve[NB_UNITS[0]][];
    GENERATORS_UNITS[1] = new GeneratorYDCurve[NB_UNITS[1]][];
    for (int loopblock = 0; loopblock < 2; loopblock++) { // Linear interpolation 
      for (int loopunit = 0; loopunit < NB_UNITS[loopblock]; loopunit++) {
        GENERATORS_UNITS[loopblock][loopunit] = new GeneratorYDCurve[] {genIntLin };
      }
    }
    NAMES_UNITS[0] = new String[NB_UNITS[0]][];
    NAMES_UNITS[1] = new String[NB_UNITS[1]][];
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_USD_DSC };
    NAMES_UNITS[0][1] = new String[] {CURVE_NAME_USD_FWD3 };
    NAMES_UNITS[0][2] = new String[] {CURVE_NAME_EUR_EONIA_DSC };
    NAMES_UNITS[0][3] = new String[] {CURVE_NAME_EUR_EONIA_FWD3 };
    NAMES_UNITS[0][4] = new String[] {CURVE_NAME_EUR_EONIA_FWD6 };
    NAMES_UNITS[1][0] = new String[] {CURVE_NAME_USD_DSC };
    NAMES_UNITS[1][1] = new String[] {CURVE_NAME_USD_FWD3 };
    NAMES_UNITS[1][2] = new String[] {CURVE_NAME_EUR_FEDFUND_1_DSC };
    NAMES_UNITS[1][3] = new String[] {CURVE_NAME_EUR_FEDFUND_1_FWD3 };
    NAMES_UNITS[1][4] = new String[] {CURVE_NAME_EUR_FEDFUND_1_FWD6 };
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      DSC_MAP[loopblock] = new LinkedHashMap<>();
      FWD_ON_MAP[loopblock] = new LinkedHashMap<>();
      FWD_IBOR_MAP[loopblock] = new LinkedHashMap<>();
    }
    DSC_MAP[0].put(CURVE_NAME_USD_DSC, USD);
    DSC_MAP[0].put(CURVE_NAME_EUR_EONIA_DSC, EUR);
    FWD_ON_MAP[0].put(CURVE_NAME_USD_DSC, new IndexON[] {INDEX_ON_USD });
    FWD_ON_MAP[0].put(CURVE_NAME_EUR_EONIA_DSC, new IndexON[] {INDEX_ON_EUR });
    FWD_IBOR_MAP[0].put(CURVE_NAME_USD_FWD3, new IborIndex[] {USDLIBOR3M });
    FWD_IBOR_MAP[0].put(CURVE_NAME_EUR_EONIA_FWD3, new IborIndex[] {EURIBOR3M });
    FWD_IBOR_MAP[0].put(CURVE_NAME_EUR_EONIA_FWD6, new IborIndex[] {EURIBOR6M });
    DSC_MAP[1].put(CURVE_NAME_USD_DSC, USD);
    DSC_MAP[1].put(CURVE_NAME_EUR_FEDFUND_1_DSC, EUR);
    FWD_ON_MAP[1].put(CURVE_NAME_USD_DSC, new IndexON[] {INDEX_ON_USD });
    FWD_IBOR_MAP[1].put(CURVE_NAME_USD_FWD3, new IborIndex[] {USDLIBOR3M });
    FWD_IBOR_MAP[1].put(CURVE_NAME_EUR_FEDFUND_1_FWD3, new IborIndex[] {EURIBOR3M });
    FWD_IBOR_MAP[1].put(CURVE_NAME_EUR_FEDFUND_1_FWD6, new IborIndex[] {EURIBOR6M });
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  public static InstrumentDefinition<?>[] getDefinitions(final double[] marketQuotes, final GeneratorInstrument[] generators,
      final GeneratorAttribute[] attribute, final ZonedDateTime referenceDate) {
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
   * Calibrate curves with hard-coded date and with calibration date the date provided. 
   * The curves are discounting/overnight forward, Libor3M forward, Libor1M forward and Libor6M forward.
   * OIS are used for the discounting curve from 1 month up to 30 years.
   * Libor3M curve uses FRA and OIS.
   * Libor1M and Libor6M use FRA and bsis swaps v 3M.
   * @param calibrationDate The calibration date.
   * @return The curves and the Jacobian matrices.
   */
  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getCurvesUsdOisL3EurOisE3E6(ZonedDateTime calibrationDate) {
    InstrumentDefinition<?>[][][] definitionsUnits = new InstrumentDefinition<?>[NB_UNITS[0]][][];
    InstrumentDefinition<?>[] definitionsUsdDsc = getDefinitions(DSC_USD_MARKET_QUOTES, DSC_USD_GENERATORS, DSC_USD_ATTR, calibrationDate);
    InstrumentDefinition<?>[] definitionsUsdFwd3 = getDefinitions(FWD3_USD_MARKET_QUOTES, FWD3_USD_GENERATORS, FWD3_USD_ATTR, calibrationDate);
    InstrumentDefinition<?>[] definitionsEurDsc = getDefinitions(EUR_EONIA_DSC_MARKET_QUOTES, EUR_EONIA_DSC_GENERATORS, EUR_EONIA_DSC_ATTR, calibrationDate);
    InstrumentDefinition<?>[] definitionsEurFwd3 = getDefinitions(EUR_EONIA_FWD3_MARKET_QUOTES, EUR_EONIA_FWD3_GENERATORS, EUR_EONIA_FWD3_ATTR, calibrationDate);
    InstrumentDefinition<?>[] definitionsEurFwd6 = getDefinitions(EUR_EONIA_FWD6_MARKET_QUOTES, EUR_EONIA_FWD6_GENERATORS, EUR_EONIA_FWD6_ATTR, calibrationDate);
    definitionsUnits[0] = new InstrumentDefinition<?>[][] {definitionsUsdDsc };
    definitionsUnits[1] = new InstrumentDefinition<?>[][] {definitionsUsdFwd3 };
    definitionsUnits[2] = new InstrumentDefinition<?>[][] {definitionsEurDsc };
    definitionsUnits[3] = new InstrumentDefinition<?>[][] {definitionsEurFwd3 };
    definitionsUnits[4] = new InstrumentDefinition<?>[][] {definitionsEurFwd6 };
    return CurveCalibrationTestsUtils.makeCurvesFromDefinitionsMulticurve(calibrationDate, definitionsUnits, GENERATORS_UNITS[0],
        NAMES_UNITS[0], KNOWN_DATA, PSMQC, PSMQCSC, false, DSC_MAP[0], FWD_ON_MAP[0], FWD_IBOR_MAP[0], CURVE_BUILDING_REPOSITORY,
        TS_FIXED_OIS_USD_WITH_TODAY, TS_FIXED_OIS_USD_WITHOUT_TODAY, TS_FIXED_IBOR_USD3M_WITH_LAST, TS_FIXED_IBOR_USD3M_WITHOUT_LAST);
  }

  /**
   * Calibrate curves with hard-coded date and with calibration date the date provided. 
   * The curves are discounting/overnight forward, Libor3M forward, Libor1M forward and Libor6M forward.
   * OIS are used for the discounting curve from 1 month up to 30 years.
   * Libor3M curve uses FRA and OIS.
   * Libor1M and Libor6M use FRA and bsis swaps v 3M.
   * @param calibrationDate The calibration date.
   * @return The curves and the Jacobian matrices.
   */
  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getCurvesUsdOisL3EurFxXCcy3Bs6(ZonedDateTime calibrationDate) {
    InstrumentDefinition<?>[][][] definitionsUnits = new InstrumentDefinition<?>[NB_UNITS[1]][][];
    InstrumentDefinition<?>[] definitionsUsdDsc = getDefinitions(DSC_USD_MARKET_QUOTES, DSC_USD_GENERATORS, DSC_USD_ATTR, calibrationDate);
    InstrumentDefinition<?>[] definitionsUsdFwd3 = getDefinitions(FWD3_USD_MARKET_QUOTES, FWD3_USD_GENERATORS, FWD3_USD_ATTR, calibrationDate);
    InstrumentDefinition<?>[] definitionsEurDsc = getDefinitions(EUR_FEDFUND_1_DSC_MARKET_QUOTES, EUR_FEDFUND_1_DSC_GENERATORS, EUR_FEDFUND_1_DSC_ATTR, calibrationDate);
    InstrumentDefinition<?>[] definitionsEurFwd3 = getDefinitions(EUR_FEDFUND_1_FWD3_MARKET_QUOTES, EUR_FEDFUND_1_FWD3_GENERATORS, EUR_FEDFUND_1_FWD3_ATTR, calibrationDate);
    InstrumentDefinition<?>[] definitionsEurFwd6 = getDefinitions(EUR_FEDFUND_1_FWD6_MARKET_QUOTES, EUR_FEDFUND_1_FWD6_GENERATORS, EUR_FEDFUND_1_FWD6_ATTR, calibrationDate);
    definitionsUnits[0] = new InstrumentDefinition<?>[][] {definitionsUsdDsc };
    definitionsUnits[1] = new InstrumentDefinition<?>[][] {definitionsUsdFwd3 };
    definitionsUnits[2] = new InstrumentDefinition<?>[][] {definitionsEurDsc };
    definitionsUnits[3] = new InstrumentDefinition<?>[][] {definitionsEurFwd3 };
    definitionsUnits[4] = new InstrumentDefinition<?>[][] {definitionsEurFwd6 };
    return CurveCalibrationTestsUtils.makeCurvesFromDefinitionsMulticurve(calibrationDate, definitionsUnits, GENERATORS_UNITS[1],
        NAMES_UNITS[1], KNOWN_DATA, PSMQC, PSMQCSC, false, DSC_MAP[1], FWD_ON_MAP[1], FWD_IBOR_MAP[1], CURVE_BUILDING_REPOSITORY,
        TS_FIXED_OIS_USD_WITH_TODAY, TS_FIXED_OIS_USD_WITHOUT_TODAY, TS_FIXED_IBOR_USD3M_WITH_LAST, TS_FIXED_IBOR_USD3M_WITHOUT_LAST);
  }

  private static final ZonedDateTimeDoubleTimeSeries TS_EMPTY = ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC();
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2014, 7, 1), DateUtils.getUTCDate(2014, 7, 2), DateUtils.getUTCDate(2014, 7, 3), DateUtils.getUTCDate(2014, 7, 4),
        DateUtils.getUTCDate(2014, 7, 7), DateUtils.getUTCDate(2014, 7, 8), DateUtils.getUTCDate(2014, 7, 9), DateUtils.getUTCDate(2014, 7, 10), DateUtils.getUTCDate(2014, 7, 11),
        DateUtils.getUTCDate(2014, 7, 14), DateUtils.getUTCDate(2014, 7, 15), DateUtils.getUTCDate(2014, 7, 16), DateUtils.getUTCDate(2014, 7, 17), DateUtils.getUTCDate(2014, 7, 18),
        DateUtils.getUTCDate(2014, 7, 21), DateUtils.getUTCDate(2014, 7, 22), DateUtils.getUTCDate(2014, 7, 23), DateUtils.getUTCDate(2014, 7, 24), DateUtils.getUTCDate(2014, 7, 25),
        DateUtils.getUTCDate(2014, 7, 28) },
      new double[] {0.002318, 0.002346, 0.002321, 0.002331,
        0.002341, 0.002336, 0.002341, 0.002336, 0.002336,
        0.002326, 0.002331, 0.002336, 0.002336, 0.002316,
        0.002331, 0.002326, 0.002341, 0.002351, 0.002341,
        0.002341 }); // TODO: replace by actual data
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2014, 7, 1), DateUtils.getUTCDate(2014, 7, 2), DateUtils.getUTCDate(2014, 7, 3), DateUtils.getUTCDate(2014, 7, 4),
        DateUtils.getUTCDate(2014, 7, 7), DateUtils.getUTCDate(2014, 7, 8), DateUtils.getUTCDate(2014, 7, 9), DateUtils.getUTCDate(2014, 7, 10), DateUtils.getUTCDate(2014, 7, 11),
        DateUtils.getUTCDate(2014, 7, 14), DateUtils.getUTCDate(2014, 7, 15), DateUtils.getUTCDate(2014, 7, 16), DateUtils.getUTCDate(2014, 7, 17), DateUtils.getUTCDate(2014, 7, 18),
        DateUtils.getUTCDate(2014, 7, 21), DateUtils.getUTCDate(2014, 7, 22), DateUtils.getUTCDate(2014, 7, 23), DateUtils.getUTCDate(2014, 7, 24), DateUtils.getUTCDate(2014, 7, 25) },
      new double[] {0.002318, 0.002346, 0.002321, 0.002331,
        0.002341, 0.002336, 0.002341, 0.002336, 0.002336,
        0.002326, 0.002331, 0.002336, 0.002336, 0.002316,
        0.002331, 0.002326, 0.002341, 0.002351, 0.002341 }); // TODO: replace by actual data
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_USD_WITH_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITH_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_USD_WITHOUT_TODAY = new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITHOUT_TODAY };

  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITH_LAST = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
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
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITHOUT_LAST = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
      new ZonedDateTime[] {DateUtils.getUTCDate(2014, 7, 1), DateUtils.getUTCDate(2014, 7, 2), DateUtils.getUTCDate(2014, 7, 3), DateUtils.getUTCDate(2014, 7, 4),
        DateUtils.getUTCDate(2014, 7, 7), DateUtils.getUTCDate(2014, 7, 8), DateUtils.getUTCDate(2014, 7, 9), DateUtils.getUTCDate(2014, 7, 10), DateUtils.getUTCDate(2014, 7, 11),
        DateUtils.getUTCDate(2014, 7, 14), DateUtils.getUTCDate(2014, 7, 15), DateUtils.getUTCDate(2014, 7, 16), DateUtils.getUTCDate(2014, 7, 17), DateUtils.getUTCDate(2014, 7, 18),
        DateUtils.getUTCDate(2014, 7, 21), DateUtils.getUTCDate(2014, 7, 22), DateUtils.getUTCDate(2014, 7, 23), DateUtils.getUTCDate(2014, 7, 24), DateUtils.getUTCDate(2014, 7, 25) },
      new double[] {0.002318, 0.002346, 0.002321, 0.002331,
        0.002341, 0.002336, 0.002341, 0.002336, 0.002336,
        0.002326, 0.002331, 0.002336, 0.002336, 0.002316,
        0.002331, 0.002326, 0.002341, 0.002351, 0.002341 });

  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_USD3M_WITH_LAST = new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_USD3M_WITH_LAST };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_USD3M_WITHOUT_LAST = new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_USD3M_WITHOUT_LAST };

}
