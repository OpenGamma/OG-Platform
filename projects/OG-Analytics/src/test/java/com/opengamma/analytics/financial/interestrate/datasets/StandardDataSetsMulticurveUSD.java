/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.datasets;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveAddYieldExisiting;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.datasets.CalendarUSD;
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
 * 0) ONDSC-OIS/LIBOR3M-FRAIRS
 * 1) ONDSC-OIS/LIBOR3M-FRAIRS/LIBOR1M-BS/LIBOR6M-BS
 * 2) ONDSC-OIS/LIBOR3M-FRAIRS with ONDSC-OIS a spread to LIBOR3M-FRAIRS
 * 3) ONDSC-OISFFS/LIBOR3M-FRAIRS/LIBOR1M-BS/LIBOR6M-BS
 * Data stored in snapshots for comparison with platform.
 */
public class StandardDataSetsMulticurveUSD {

  private static final ZonedDateTime[] REFERENCE_DATE = new ZonedDateTime[4];
  static {
    REFERENCE_DATE[0] = DateUtils.getUTCDate(2014, 1, 22);
    REFERENCE_DATE[1] = DateUtils.getUTCDate(2014, 1, 22);
    REFERENCE_DATE[2] = DateUtils.getUTCDate(2014, 1, 22);
    REFERENCE_DATE[3] = DateUtils.getUTCDate(2014, 1, 22);
  }

  private static final Interpolator1D INTERPOLATOR_LINEAR = 
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, 
          Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final Interpolator1D INTERPOLATOR_NCS = 
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, 
          Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();
  private static final double TOLERANCE_ROOT = 1.0E-10;
  private static final int STEP_MAX = 100;

  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final Currency USD = Currency.USD;
  private static final FXMatrix FX_MATRIX = new FXMatrix(USD);

  private static final double NOTIONAL = 1.0;

  private static final IndexIborMaster IBOR_MASTER = IndexIborMaster.getInstance();
  private static final GeneratorSwapFixedONMaster GENERATOR_OIS_MASTER = GeneratorSwapFixedONMaster.getInstance();
  private static final GeneratorSwapFixedIborMaster GENERATOR_IRS_MASTER = GeneratorSwapFixedIborMaster.getInstance();

  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GENERATOR_OIS_MASTER.getGenerator("USD1YFEDFUND", NYC);
  private static final IndexON USDFEDFUND = GENERATOR_OIS_USD.getIndex();
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_USD = 
      new GeneratorDepositON("USD Deposit ON", USD, NYC, USDFEDFUND.getDayCount());
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_IRS_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  private static final IborIndex USDLIBOR3M = USD6MLIBOR3M.getIborIndex();
  private static final IborIndex USDLIBOR1M = IBOR_MASTER.getIndex("USDLIBOR1M");
  private static final IborIndex USDLIBOR6M = IBOR_MASTER.getIndex("USDLIBOR6M");
  private static final GeneratorDepositIbor GENERATOR_USDLIBOR1M = 
      new GeneratorDepositIbor("GENERATOR_USDLIBOR1M", USDLIBOR1M, NYC);
  private static final GeneratorDepositIbor GENERATOR_USDLIBOR3M = 
      new GeneratorDepositIbor("GENERATOR_USDLIBOR3M", USDLIBOR3M, NYC);
  private static final GeneratorDepositIbor GENERATOR_USDLIBOR6M = 
      new GeneratorDepositIbor("GENERATOR_USDLIBOR6M", USDLIBOR6M, NYC);
  private static final GeneratorFRA GENERATOR_FRA3M = new GeneratorFRA("GENERATOR_FRA", USDLIBOR3M, NYC);
  private static final GeneratorFRA GENERATOR_FRA6M = new GeneratorFRA("GENERATOR_FRA", USDLIBOR6M, NYC);
  private static final Period P6M = Period.ofMonths(6);
  private static final Period P3M = Period.ofMonths(3);
  private static final GeneratorSwapIborCompoundingIbor USD6MLIBOR3MLIBOR6M = 
      new GeneratorSwapIborCompoundingIbor("USD6MLIBOR3MLIBOR6M", USDLIBOR3M, P6M, USDLIBOR6M, NYC, NYC);
  private static final GeneratorSwapIborCompoundingIbor USD3MLIBOR1MLIBOR3M = 
      new GeneratorSwapIborCompoundingIbor("USD3MLIBOR1MLIBOR3M", USDLIBOR1M, P3M, USDLIBOR3M, NYC, NYC);

  private static final ZonedDateTimeDoubleTimeSeries TS_EMPTY = ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC();
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITH_TODAY = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27), DateUtils.getUTCDate(2011, 9, 28) }, 
          new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITHOUT_TODAY = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27), DateUtils.getUTCDate(2011, 9, 28) }, 
          new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_USD_WITH_TODAY = 
      new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITH_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_OIS_USD_WITHOUT_TODAY = 
      new ZonedDateTimeDoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITHOUT_TODAY };

  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITH_TODAY = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27), DateUtils.getUTCDate(2011, 9, 28) }, 
          new double[] {0.0035, 0.0036 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITHOUT_TODAY = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27) },
          new double[] {0.0035 });

  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_USD3M_WITH_TODAY = 
      new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_USD3M_WITH_TODAY };
  private static final ZonedDateTimeDoubleTimeSeries[] TS_FIXED_IBOR_USD3M_WITHOUT_TODAY = 
      new ZonedDateTimeDoubleTimeSeries[] {TS_IBOR_USD3M_WITHOUT_TODAY };

  private static final String CURVE_NAME_DSC_USD = "USD-DSCON-OIS";
  private static final String CURVE_NAME_FWD3_USD = "USD-LIBOR3M-FRAIRS";
  private static final String CURVE_NAME_FWD1_USD = "USD-LIBOR1M-FRABS";
  private static final String CURVE_NAME_FWD6_USD = "USD-LIBOR6M-FRABS";

  /** Data for 2014-01-22 **/
  /** Market values for the dsc USD curve */
  private static final double[] DSC_1_USD_MARKET_QUOTES = new double[] {0.0016, 0.0016,
    0.00072000, 0.00082000, 0.00093000, 0.00090000, 0.00105000,
    0.00118500, 0.00318650, 0.00704000, 0.01121500, 0.01515000,
    0.01845500, 0.02111000, 0.02332000, 0.02513500, 0.02668500 }; //17
  /** Generators for the dsc USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_1_USD_GENERATORS = 
      new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_USD, GENERATOR_DEPOSIT_ON_USD,
    GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD,
    GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD,
    GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD };
  /** Tenors for the dsc USD curve */
  private static final Period[] DSC_1_USD_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1),
    Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(6), Period.ofYears(7), Period.ofYears(8), Period.ofYears(9), Period.ofYears(10) };
  private static final GeneratorAttributeIR[] DSC_1_USD_ATTR = new GeneratorAttributeIR[DSC_1_USD_TENOR.length];
  static {
    for (int loopins = 0; loopins < 2; loopins++) {
      DSC_1_USD_ATTR[loopins] = new GeneratorAttributeIR(DSC_1_USD_TENOR[loopins], Period.ofDays(0));
    }
    for (int loopins = 2; loopins < DSC_1_USD_TENOR.length; loopins++) {
      DSC_1_USD_ATTR[loopins] = new GeneratorAttributeIR(DSC_1_USD_TENOR[loopins]);
    }
  }

  /** Market values for the Fwd 3M USD curve */
  private static final double[] FWD3_1_USD_MARKET_QUOTES = new double[] {0.00236600,
    0.00258250, 0.00296050,
    0.00294300, 0.00503000, 0.00939150, 0.01380800, 0.01732000,
    0.02396200, 0.02930000, 0.03195000, 0.03423500, 0.03615500, 
    0.03696850, 0.03734500 }; //15
  /** Generators for the Fwd 3M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_1_USD_GENERATORS = 
      new GeneratorInstrument<?>[] {GENERATOR_USDLIBOR3M,
    GENERATOR_FRA3M, GENERATOR_FRA3M,
    USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M,
    USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, 
    USD6MLIBOR3M, USD6MLIBOR3M };
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] FWD3_1_USD_TENOR = new Period[] {Period.ofMonths(0),
    Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(7), Period.ofYears(10), Period.ofYears(12), Period.ofYears(15), Period.ofYears(20), 
    Period.ofYears(25), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] FWD3_1_USD_ATTR = new GeneratorAttributeIR[FWD3_1_USD_TENOR.length];
  static {
    for (int loopins = 0; loopins < FWD3_1_USD_TENOR.length; loopins++) {
      FWD3_1_USD_ATTR[loopins] = new GeneratorAttributeIR(FWD3_1_USD_TENOR[loopins]);
    }
  }

  /** Data for 2014-02-18 **/
  /** Market values for the dsc USD curve */
  private static final double[] DSC_2_USD_MARKET_QUOTES = new double[] {0.00175, 0.0015,
    0.00079000, 0.00076000, 0.00075000, 0.00078000, 0.00083500,
    0.00099000, 0.00249000, 0.00582500, 0.00979000, 0.01357000,
    0.01687500, 0.01963000, 0.02192000, 0.02382000, 0.02539000 }; //17
  /** Generators for the dsc USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_2_USD_GENERATORS = 
      new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_USD, GENERATOR_DEPOSIT_ON_USD,
    GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD,
    GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD,
    GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD };
  /** Tenors for the dsc USD curve */
  private static final Period[] DSC_2_USD_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1),
    Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(6), Period.ofYears(7), Period.ofYears(8), Period.ofYears(9), Period.ofYears(10) };
  private static final GeneratorAttributeIR[] DSC_2_USD_ATTR = new GeneratorAttributeIR[DSC_2_USD_TENOR.length];
  static {
    for (int loopins = 0; loopins < 2; loopins++) {
      DSC_2_USD_ATTR[loopins] = new GeneratorAttributeIR(DSC_2_USD_TENOR[loopins], Period.ofDays(0));
    }
    for (int loopins = 2; loopins < DSC_2_USD_TENOR.length; loopins++) {
      DSC_2_USD_ATTR[loopins] = new GeneratorAttributeIR(DSC_2_USD_TENOR[loopins]);
    }
  }

  /** Market values for the Fwd 3M USD curve */
  private static final double[] FWD3_2_USD_MARKET_QUOTES = new double[] {0.0023455,
    0.0024275, 0.0026280,
    0.00265, 0.004487, 0.008125, 0.012333, 0.016305,
    0.02258, 0.02841, 0.030909, 0.033357, 0.035392, 
    0.036308, 0.036746 }; //15
  /** Generators for the Fwd 3M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_2_USD_GENERATORS = 
      new GeneratorInstrument<?>[] {GENERATOR_USDLIBOR3M,
    GENERATOR_FRA3M, GENERATOR_FRA3M,
    USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M,
    USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, 
    USD6MLIBOR3M, USD6MLIBOR3M };
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] FWD3_2_USD_TENOR = new Period[] {Period.ofMonths(0),
    Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(7), Period.ofYears(10), Period.ofYears(12), Period.ofYears(15), Period.ofYears(20), 
    Period.ofYears(25), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] FWD3_2_USD_ATTR = new GeneratorAttributeIR[FWD3_2_USD_TENOR.length];
  static {
    for (int loopins = 0; loopins < FWD3_2_USD_TENOR.length; loopins++) {
      FWD3_2_USD_ATTR[loopins] = new GeneratorAttributeIR(FWD3_2_USD_TENOR[loopins]);
    }
  }

  /** Market values for the Fwd 1M USD curve */
  private static final double[] FWD1_2_USD_MARKET_QUOTES = new double[] {0.00154,
    0.00073, 0.00069375,
    0.0007125, 0.00070, 0.00070, 0.00070, 0.00070,
    0.000675, 0.000575, 0.0005375, 0.00048750, 0.00042500, 0.00040, 0.00038750 }; //15
  /** Generators for the Fwd 3M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD1_2_USD_GENERATORS = 
      new GeneratorInstrument<?>[] {GENERATOR_USDLIBOR1M,
    USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M,
    USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M,
    USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, 
    USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M };
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] FWD1_2_USD_TENOR = new Period[] {Period.ofMonths(0),
    Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(7), Period.ofYears(10), Period.ofYears(12), Period.ofYears(15), Period.ofYears(20), 
    Period.ofYears(25), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] FWD1_2_USD_ATTR = new GeneratorAttributeIR[FWD1_2_USD_TENOR.length];
  static {
    for (int loopins = 0; loopins < FWD1_2_USD_TENOR.length; loopins++) {
      FWD1_2_USD_ATTR[loopins] = new GeneratorAttributeIR(FWD1_2_USD_TENOR[loopins]);
    }
  }

  /** Market values for the Fwd 6M USD curve */
  private static final double[] FWD6_2_USD_MARKET_QUOTES = new double[] {0.003297,
    0.0034,
    0.00090000, 0.00090000, 0.00090000, 0.0009125, 0.0009125,
    0.00095, 0.0009875, 0.001025, 0.0010475, 0.0010375, 0.0010375, 0.0010325 };
  /** Generators for the Fwd 3M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD6_2_USD_GENERATORS = 
      new GeneratorInstrument<?>[] {GENERATOR_USDLIBOR6M,
    GENERATOR_FRA6M,
    USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M,
    USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, 
    USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M };
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] FWD6_2_USD_TENOR = new Period[] {Period.ofMonths(0),
    Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(7), Period.ofYears(10), Period.ofYears(12), Period.ofYears(15), Period.ofYears(20), 
    Period.ofYears(25), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] FWD6_2_USD_ATTR = new GeneratorAttributeIR[FWD6_2_USD_TENOR.length];
  static {
    for (int loopins = 0; loopins < FWD6_2_USD_TENOR.length; loopins++) {
      FWD6_2_USD_ATTR[loopins] = new GeneratorAttributeIR(FWD6_2_USD_TENOR[loopins]);
    }
  }

  /** Data for 2014-xx-xx **/
  /** Market values for the dsc USD curve */
  private static final double[] DSC_3_USD_MARKET_QUOTES = new double[] {0.0015500000,
    0.0009000000, 0.0009100000, 0.0009150000, 0.0010000000, 0.0012450000, 0.0018050000,
    0.0016300600, 0.0017810300, 0.0019273500, 0.0020548700, 0.0021300000,
    0.0021972300, 0.0022500000, 0.0022900000, 0.0023218600, 0.0023700000,
    0.0023848500, 0.0024500000, 0.0024400000, 0.0024126500};
  /** Generators for the dsc USD curve */
  private static final int NB_ONDEPO_3 = 1;
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_3_USD_GENERATORS = 
      CurveCalibrationConventionDataSets.generatorUsdOnOisFfs(NB_ONDEPO_3, 6, 14);
  /** Tenors for the dsc USD curve */
  private static final Period[] DSC_3_USD_TENOR = new Period[] {Period.ofDays(0),
    Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1), 
    Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(6), 
    Period.ofYears(7), Period.ofYears(8), Period.ofYears(9), Period.ofYears(10), Period.ofYears(12), 
    Period.ofYears(15), Period.ofYears(20), Period.ofYears(25), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] DSC_3_USD_ATTR = new GeneratorAttributeIR[DSC_3_USD_TENOR.length];
  static {
    for (int loopins = 0; loopins < NB_ONDEPO_3; loopins++) {
      DSC_3_USD_ATTR[loopins] = new GeneratorAttributeIR(DSC_3_USD_TENOR[loopins], Period.ofDays(0));
    }
    for (int loopins = NB_ONDEPO_3; loopins < DSC_3_USD_TENOR.length; loopins++) {
      DSC_3_USD_ATTR[loopins] = new GeneratorAttributeIR(DSC_3_USD_TENOR[loopins]);
    }
  }

  /** Market values for the Fwd 3M USD curve */
  private static final double[] FWD3_3_USD_MARKET_QUOTES = new double[] {0.0023810000,
    0.0033050000, 0.0071175000, 0.0114285000, 0.0150500000, 0.0177025000,
    0.0214500000, 0.0250500000, 0.0267200000, 0.0284250000, 0.0299700000,
    0.0306825000, 0.0310250000};
  /** Generators for the Fwd 3M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_3_USD_GENERATORS = 
      CurveCalibrationConventionDataSets.generatorUsdIbor3Fra3Irs3(1, 0, 12);
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] FWD3_3_USD_TENOR = new Period[] {Period.ofMonths(0),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(7), Period.ofYears(10), Period.ofYears(12), Period.ofYears(15), Period.ofYears(20), 
    Period.ofYears(25), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] FWD3_3_USD_ATTR = new GeneratorAttributeIR[FWD3_3_USD_TENOR.length];
  static {
    for (int loopins = 0; loopins < FWD3_3_USD_TENOR.length; loopins++) {
      FWD3_3_USD_ATTR[loopins] = new GeneratorAttributeIR(FWD3_3_USD_TENOR[loopins]);
    }
  }

  /** Market values for the Fwd 1M USD curve */
  private static final double[] FWD1_3_USD_MARKET_QUOTES = new double[] {0.0015600000,
    0.0008250000, 0.0008500000, 0.0008812500, 0.0009687500, 0.0010187500,
    0.0010562500, 0.0010687500, 0.0010312500, 0.0009062500, 0.0008175000,
    0.0007312500, 0.0006562500, 0.0005937500, 0.0005562500};
  /** Generators for the Fwd 3M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD1_3_USD_GENERATORS = 
      new GeneratorInstrument<?>[] {GENERATOR_USDLIBOR1M,
    USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, 
    USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, 
    USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M };
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] FWD1_3_USD_TENOR = new Period[] {Period.ofMonths(0),
    Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), 
    Period.ofYears(4), Period.ofYears(5), Period.ofYears(7), Period.ofYears(10), Period.ofYears(12), 
    Period.ofYears(15), Period.ofYears(20), Period.ofYears(25), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] FWD1_3_USD_ATTR = new GeneratorAttributeIR[FWD1_3_USD_TENOR.length];
  static {
    for (int loopins = 0; loopins < FWD1_3_USD_TENOR.length; loopins++) {
      FWD1_3_USD_ATTR[loopins] = new GeneratorAttributeIR(FWD1_3_USD_TENOR[loopins]);
    }
  }

  /** Market values for the Fwd 6M USD curve */
  private static final double[] FWD6_3_USD_MARKET_QUOTES = new double[] {0.0032990000,
    0.0008937500, 0.0009000000, 0.0009000000, 0.0009000000, 0.0009000000,
    0.0009062500, 0.0009062500, 0.0009187500, 0.0009450000, 0.0009187500,
    0.0009187500, 0.0009312500};
  /** Generators for the Fwd 3M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD6_3_USD_GENERATORS = 
      new GeneratorInstrument<?>[] {GENERATOR_USDLIBOR6M,
    USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M,
    USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, 
    USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M };
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] FWD6_3_USD_TENOR = new Period[] {Period.ofMonths(0),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(7), Period.ofYears(10), Period.ofYears(12), Period.ofYears(15), Period.ofYears(20), 
    Period.ofYears(25), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] FWD6_3_USD_ATTR = new GeneratorAttributeIR[FWD6_3_USD_TENOR.length];
  static {
    for (int loopins = 0; loopins < FWD6_3_USD_TENOR.length; loopins++) {
      FWD6_3_USD_ATTR[loopins] = new GeneratorAttributeIR(FWD6_3_USD_TENOR[loopins]);
    }
  }

  /** Standard USD discounting curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_DSC_1_USD;
  /** Standard USD Forward 3M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_FWD3_1_USD;
  /** Standard USD discounting curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_DSC_2_USD;
  /** Standard USD Forward 3M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_FWD3_2_USD;
  /** Standard USD Forward 1M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_FWD1_2_USD;
  /** Standard USD Forward 6M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_FWD6_2_USD;
  /** Standard USD discounting curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_DSC_3_USD;
  /** Standard USD Forward 3M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_FWD3_3_USD;
  /** Standard USD Forward 1M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_FWD1_3_USD;
  /** Standard USD Forward 6M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_FWD6_3_USD;

  /** Units of curves */
  private static final int[] NB_UNITS = new int[] {2, 4, 1, 3 };
  private static final int NB_BLOCKS = NB_UNITS.length;
  private static final InstrumentDefinition<?>[][][][] DEFINITIONS_UNITS = new InstrumentDefinition<?>[NB_BLOCKS][][][];
  private static final GeneratorYDCurve[][][] GENERATORS_UNITS = new GeneratorYDCurve[NB_BLOCKS][][];
  private static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];
  private static final MulticurveProviderDiscount KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);
  private static final LinkedHashMap<String, Currency> DSC_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IborIndex[]> FWD_IBOR_MAP = new LinkedHashMap<>();

  static {
    DEFINITIONS_DSC_1_USD = getDefinitions(DSC_1_USD_MARKET_QUOTES, DSC_1_USD_GENERATORS, DSC_1_USD_ATTR, REFERENCE_DATE[0]);
    DEFINITIONS_FWD3_1_USD = getDefinitions(FWD3_1_USD_MARKET_QUOTES, FWD3_1_USD_GENERATORS, FWD3_1_USD_ATTR, REFERENCE_DATE[0]);
    DEFINITIONS_DSC_2_USD = getDefinitions(DSC_2_USD_MARKET_QUOTES, DSC_2_USD_GENERATORS, DSC_2_USD_ATTR, REFERENCE_DATE[1]);
    DEFINITIONS_FWD3_2_USD = getDefinitions(FWD3_2_USD_MARKET_QUOTES, FWD3_2_USD_GENERATORS, FWD3_2_USD_ATTR, REFERENCE_DATE[1]);
    DEFINITIONS_FWD1_2_USD = getDefinitions(FWD1_2_USD_MARKET_QUOTES, FWD1_2_USD_GENERATORS, FWD1_2_USD_ATTR, REFERENCE_DATE[1]);
    DEFINITIONS_FWD6_2_USD = getDefinitions(FWD6_2_USD_MARKET_QUOTES, FWD6_2_USD_GENERATORS, FWD6_2_USD_ATTR, REFERENCE_DATE[1]);
    DEFINITIONS_DSC_3_USD = getDefinitions(DSC_3_USD_MARKET_QUOTES, DSC_3_USD_GENERATORS, DSC_3_USD_ATTR, REFERENCE_DATE[2]);
    DEFINITIONS_FWD3_3_USD = getDefinitions(FWD3_3_USD_MARKET_QUOTES, FWD3_3_USD_GENERATORS, FWD3_3_USD_ATTR, REFERENCE_DATE[2]);
    DEFINITIONS_FWD1_3_USD = getDefinitions(FWD1_3_USD_MARKET_QUOTES, FWD1_3_USD_GENERATORS, FWD1_3_USD_ATTR, REFERENCE_DATE[2]);
    DEFINITIONS_FWD6_3_USD = getDefinitions(FWD6_3_USD_MARKET_QUOTES, FWD6_3_USD_GENERATORS, FWD6_3_USD_ATTR, REFERENCE_DATE[2]);
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      DEFINITIONS_UNITS[loopblock] = new InstrumentDefinition<?>[NB_UNITS[loopblock]][][];
      GENERATORS_UNITS[loopblock] = new GeneratorYDCurve[NB_UNITS[loopblock]][];
      NAMES_UNITS[loopblock] = new String[NB_UNITS[loopblock]][];
    }
    DEFINITIONS_UNITS[0][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_1_USD };
    DEFINITIONS_UNITS[0][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_1_USD };
    DEFINITIONS_UNITS[1][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_2_USD };
    DEFINITIONS_UNITS[1][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_2_USD };
    DEFINITIONS_UNITS[1][2] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD1_2_USD };
    DEFINITIONS_UNITS[1][3] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD6_2_USD };
    DEFINITIONS_UNITS[2][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_1_USD, DEFINITIONS_DSC_1_USD };
    DEFINITIONS_UNITS[3][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_3_USD, DEFINITIONS_FWD3_3_USD };
    DEFINITIONS_UNITS[3][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD1_3_USD };
    DEFINITIONS_UNITS[3][2] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD6_3_USD };
    final GeneratorYDCurve genIntLin = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_LINEAR);
    GeneratorYDCurve genAddExistFwd3 = new GeneratorCurveAddYieldExisiting(genIntLin, false, CURVE_NAME_FWD3_USD);
    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[0][1] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[1][0] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[1][1] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[1][2] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[1][3] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[2][0] = new GeneratorYDCurve[] {genIntLin, genAddExistFwd3 };
    final GeneratorYDCurve genIntNcs = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_NCS);
    GENERATORS_UNITS[3][0] = new GeneratorYDCurve[] {genIntNcs, genIntNcs };
    GENERATORS_UNITS[3][1] = new GeneratorYDCurve[] {genIntNcs };
    GENERATORS_UNITS[3][2] = new GeneratorYDCurve[] {genIntNcs };
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_USD };
    NAMES_UNITS[0][1] = new String[] {CURVE_NAME_FWD3_USD };
    NAMES_UNITS[1][0] = new String[] {CURVE_NAME_DSC_USD };
    NAMES_UNITS[1][1] = new String[] {CURVE_NAME_FWD3_USD };
    NAMES_UNITS[1][2] = new String[] {CURVE_NAME_FWD1_USD };
    NAMES_UNITS[1][3] = new String[] {CURVE_NAME_FWD6_USD };
    NAMES_UNITS[2][0] = new String[] {CURVE_NAME_FWD3_USD, CURVE_NAME_DSC_USD };
    NAMES_UNITS[3][0] = new String[] {CURVE_NAME_DSC_USD ,CURVE_NAME_FWD3_USD };
    NAMES_UNITS[3][1] = new String[] {CURVE_NAME_FWD1_USD };
    NAMES_UNITS[3][2] = new String[] {CURVE_NAME_FWD6_USD };
    DSC_MAP.put(CURVE_NAME_DSC_USD, USD);
    FWD_ON_MAP.put(CURVE_NAME_DSC_USD, new IndexON[] {USDFEDFUND });
    FWD_IBOR_MAP.put(CURVE_NAME_FWD3_USD, new IborIndex[] {USDLIBOR3M });
    FWD_IBOR_MAP.put(CURVE_NAME_FWD1_USD, new IborIndex[] {USDLIBOR1M });
    FWD_IBOR_MAP.put(CURVE_NAME_FWD6_USD, new IborIndex[] {USDLIBOR6M });
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
      new MulticurveDiscountBuildingRepository(TOLERANCE_ROOT, TOLERANCE_ROOT, STEP_MAX);

  private static final List<Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle>> CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK = new ArrayList<>();
  static {
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.add(makeCurvesFromDefinitions(DEFINITIONS_UNITS[loopblock], 
          REFERENCE_DATE[loopblock], GENERATORS_UNITS[loopblock], NAMES_UNITS[loopblock], KNOWN_DATA,
          PSMQC, PSMQCSC, false));
    }
  }

  /**
   * Returns the multi-curve and block for two curves (ONDSC-OIS/LIBOR3M-FRAIRS). 
   * ONDSC-OIS is calibrated on OIS up to 10Y and LIBOR3M-FRAIRS is calibrated on FRA and IRS up to 30Y.
   * The market quotes used for the calibration are the default quotes of the data set.
   * @return The curves.
   */
  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getCurvesUSDOisL3() {
    return CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0);
  }

  /**
   * Returns the multi-curve and block for two curves (ONDSC-OIS/LIBOR3M-FRAIRS) with the ONDSC-OIS curve constructed 
   * as a spread to the LIBOR3M-FRAIRS curve.
   * The market quotes used for the calibration are the default quotes of the data set.
   * @return The curves.
   */
  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getCurvesUSDOisSpreadL3() {
    return CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(2);
  }

  /**
   * Returns the multi-curve and block for two curves (ONDSC-OIS/LIBOR3M-FRAIRS) with the ONDSC-OIS curve constructed 
   * as a spread to the LIBOR3M-FRAIRS curve.
   * @param dscMarketQuotes The market quotes to be used for the discounting curve.
   * @param fwd3MarketQuotes The market quotes to be used for the forward LIBOR3M curve.
   * @return The curves.
   */
  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getCurvesUSDOisSpreadL3(
      double[] dscMarketQuotes, double[] fwd3MarketQuotes) {
    InstrumentDefinition<?>[] dscDefinition = getDefinitions(dscMarketQuotes, DSC_1_USD_GENERATORS, DSC_1_USD_ATTR, 
        REFERENCE_DATE[2]);
    InstrumentDefinition<?>[] fwd3Definition = getDefinitions(fwd3MarketQuotes, FWD3_1_USD_GENERATORS, FWD3_1_USD_ATTR, 
        REFERENCE_DATE[2]);
    InstrumentDefinition<?>[][][] unitDefinition = new InstrumentDefinition<?>[][][] {{fwd3Definition, dscDefinition } };
    return makeCurvesFromDefinitions(unitDefinition, REFERENCE_DATE[0], GENERATORS_UNITS[2], NAMES_UNITS[2], KNOWN_DATA,
        PSMQC, PSMQCSC, false);
  }

  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getCurvesUSDOisL1L3L6() {
    return CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(1);
  }

  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getCurvesUSDOisFFL1L3L6() {
    return CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(3);
  }

  /**
   * Returns the array of Ibor index used in the curve data set. 
   * @return The array: USDLIBOR3M 
   */
  public static IborIndex[] indexIborArrayUSDOisL3() {
    return new IborIndex[] {USDLIBOR3M };
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
   * Returns the market quotes for the discounting curve.
   * @return The market quotes.
   */
  public static double[] getMarketDataDsc1() {
    return DSC_1_USD_MARKET_QUOTES;
  }

  /**
   * Returns the market quotes for the forward 3M curve.
   * @return The market quotes.
   */
  public static double[] getMarketDataFwd31() {
    return FWD3_1_USD_MARKET_QUOTES;
  }

  /**
   * Returns the name of the discounting curve.
   * @return The name.
   */
  public static String getDscCurveName() {
    return CURVE_NAME_DSC_USD;
  }

  /**
   * Returns the name of the forward LIBOR3M curve.
   * @return The name.
   */
  public static String getFwd3CurveName() {
    return CURVE_NAME_FWD3_USD;
  }

  @SuppressWarnings("unchecked")
  private static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitions(
      final InstrumentDefinition<?>[][][] definitions, final ZonedDateTime calibrationDate, 
      final GeneratorYDCurve[][] curveGenerators, final String[][] curveNames, final MulticurveProviderDiscount knownData, 
      final InstrumentDerivativeVisitor<MulticurveProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<MulticurveProviderInterface, MulticurveSensitivity> sensitivityCalculator, 
      final boolean withToday) {
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
    return CURVE_BUILDING_REPOSITORY.makeCurvesFromDerivatives(curveBundles, knownData, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP, 
        calculator, sensitivityCalculator);
  }

  private static InstrumentDerivative convert(final InstrumentDefinition<?> definition, final ZonedDateTime date, 
      final int unit, final boolean withToday) {
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
        return withToday ? TS_FIXED_IBOR_USD3M_WITH_TODAY : TS_FIXED_IBOR_USD3M_WITHOUT_TODAY;
      case 1:
        return withToday ? TS_FIXED_IBOR_USD3M_WITH_TODAY : TS_FIXED_IBOR_USD3M_WITHOUT_TODAY;
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
