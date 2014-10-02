/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.datasets;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeFX;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorFRA;
import com.opengamma.analytics.financial.instrument.index.GeneratorForexForward;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborCompoundingIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapXCcyIborIbor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationConventionDataSets;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationTestsUtils;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

public class StandardDataSetsMulticurveUSDGBP {

/**
 * Curves calibration in USD and GBP: 
 * 0) USD_DSCON-OISFFS_L3M-FRAIRS_L1M-FRABSxL3M_L6M-FRABSL3Mx / FRABSL3Mx__GBP_DSC-FXxUSD-XCCYxL3MUSDL3M_L3M-FRAIRS
 * Potential curve config name: USD_DSCON-OISFFS_L3M-FRAIRS_L1M-FRABSxL3M_L6M-FRABSL3Mx__GBP_DSC-FXxUSD-XCCYxL3MUSDL3M_L3M-FRAIRS
 */

  private static final Calendar NYC = new MondayToFridayCalendar("NYC"); //CalendarUSD("NYC");
  private static final Calendar LON = new MondayToFridayCalendar("LON"); //CalendarGBP("LON");
  private static final Currency USD = Currency.USD;
  private static final Currency GBP = Currency.GBP;
  private static final double FX_GBPUSD = 1.65785;
  private static final FXMatrix FX_MATRIX_SPOT = new FXMatrix(GBP, USD, FX_GBPUSD);

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
  private static final IborIndex GBPLIBOR3M = IBOR_MASTER.getIndex("GBPLIBOR3M");
  private static final GeneratorDepositIbor GENERATOR_USDLIBOR1M = 
      new GeneratorDepositIbor("GENERATOR_USDLIBOR1M", USDLIBOR1M, NYC);
  private static final GeneratorDepositIbor GENERATOR_USDLIBOR6M = 
      new GeneratorDepositIbor("GENERATOR_USDLIBOR6M", USDLIBOR6M, NYC);
  private static final GeneratorFRA GENERATOR_FRA6M = new GeneratorFRA("GENERATOR_FRA", USDLIBOR6M, NYC);
  private static final GeneratorSwapFixedIbor USD1YLIBOR1M = 
      GENERATOR_IRS_MASTER.getGenerator(GeneratorSwapFixedIborMaster.USD1YLIBOR1M, NYC);
  private static final Period P6M = Period.ofMonths(6);
  private static final Period P3M = Period.ofMonths(3);
  private static final GeneratorSwapIborCompoundingIbor USD6MLIBOR3MLIBOR6M = 
      new GeneratorSwapIborCompoundingIbor("USD6MLIBOR3MLIBOR6M", USDLIBOR3M, P6M, USDLIBOR6M, NYC, NYC);
  private static final GeneratorSwapIborCompoundingIbor USD3MLIBOR1MLIBOR3M = 
      new GeneratorSwapIborCompoundingIbor("USD3MLIBOR1MLIBOR3M", USDLIBOR1M, P3M, USDLIBOR3M, NYC, NYC);
  private static final GeneratorSwapXCcyIborIbor GBPLIBOR3MUSDLIBOR3M = 
      new GeneratorSwapXCcyIborIbor("GBPLIBOR3MUSDLIBOR3M", GBPLIBOR3M, USDLIBOR3M, 
          BusinessDayConventions.MODIFIED_FOLLOWING, true, 2, LON, NYC); // Spread on GBP leg
      
  private static final GeneratorForexForward GENERATOR_FXFWD_GBPUSD = new GeneratorForexForward("GBPUSD", 
      GBP, USD, NYC, USDLIBOR3M.getSpotLag(), USDLIBOR3M.getBusinessDayConvention(), true);

  private static final String NAME_INPUT_DSC_USD = "USD-OISFFS";
  private static final String NAME_INPUT_FWD3_USD = "USD-FRAL3M-IRSL3M";
  private static final String NAME_INPUT_FWD1_USD = "USD-FRAL3M-BSL1ML3M";
  private static final String NAME_INPUT_FWD6_USD = "USD-FRAL6M-BSL3ML6M";
  private static final String NAME_INPUT_DSC_GBP = "GBPUSD-FXGBPUSD-XCCYGBPL3MUSDL3M";
  private static final String NAME_INPUT_FWD3_GBP = "GBP-FRAL3MIRSL3M";
  private static final String NAME_MULTICURVE_SHORT =
      "USD_GBP_FFCol";
  private static final String NAME_OUTPUT_DSC_USD = NAME_MULTICURVE_SHORT + "." + NAME_INPUT_DSC_USD;
  private static final String NAME_OUTPUT_FWD3_USD = NAME_MULTICURVE_SHORT + "." + NAME_INPUT_FWD3_USD;
  private static final String NAME_OUTPUT_FWD1_USD = NAME_MULTICURVE_SHORT + "." + NAME_INPUT_FWD1_USD;
  private static final String NAME_OUTPUT_FWD6_USD = NAME_MULTICURVE_SHORT + "." + NAME_INPUT_FWD6_USD;
  private static final String NAME_OUTPUT_DSC_GBP = NAME_MULTICURVE_SHORT + "." + NAME_INPUT_DSC_GBP;
  private static final String NAME_OUTPUT_FWD3_GBP = NAME_MULTICURVE_SHORT + "." + NAME_INPUT_FWD3_GBP;

  /** Data for 2014-01-22 **/
  private static final ZonedDateTime DATA_DATE_1 = DateUtils.getUTCDate(2014, 1, 22);
  /** Market values for the USD-OISFFS curve */
  private static final double[] USD_OISFFS_1_MARKET_QUOTES = new double[] {0.0015500000,
    0.0009000000, 0.0009100000, 0.0009150000, 0.0010000000, 0.0012450000, 0.0018050000,
    0.0016300600, 0.0017810300, 0.0019273500, 0.0020548700, 0.0021300000,
    0.0021972300, 0.0022500000, 0.0022900000, 0.0023218600, 0.0023700000,
    0.0023848500, 0.0024500000, 0.0024400000, 0.0024126500};
  /** Market values for the USD-FRAL3M-IRSL3M curve */
  private static final double[] USD_FWD3_1_MARKET_QUOTES = new double[] {0.0023810000,
    0.0026000000, 0.0030000000,
    0.0033050000, 0.0071175000, 0.0114285000, 0.0150500000, 0.0177025000,
    0.0214500000, 0.0250500000, 0.0267200000, 0.0284250000, 0.0299700000,
    0.0306825000, 0.0310250000};
  /** Market values for the USD-IRSL1M-BSL1ML3M curve */
  private static final double[] USD_FWD1_1_MARKET_QUOTES = new double[] {0.0015600000,
    0.0019000000, 0.0022000000,
    0.0008250000, 0.0008500000, 0.0008812500, 0.0009687500, 0.0010187500,
    0.0010562500, 0.0010687500, 0.0010312500, 0.0009062500, 0.0008175000,
    0.0007312500, 0.0006562500, 0.0005937500, 0.0005562500};
  /** Market values for the USD-FRAL6M-BSL3ML6M curve */
  private static final double[] USD_FWD6_1_MARKET_QUOTES = new double[] {0.0032990000,
    0.0040000000,
    0.0008937500, 0.0009000000, 0.0009000000, 0.0009000000, 0.0009000000,
    0.0009062500, 0.0009062500, 0.0009187500, 0.0009450000, 0.0009187500,
    0.0009187500, 0.0009312500};
  /** Market values for the GBPUSD-FXGBPUSD-XCCYGBPL3MUSDL3M curve */
  private static final double[] GBP_DSC_1_MARKET_QUOTES = new double[] {
    -0.0004, -0.0008155, -0.0012575, -0.002976, -0.005201,
    0.0000400000, 0.0000750000, 0.0000000000, -0.0001000000, -0.0002000000,
    -0.0002750000, -0.0003000000, -0.0003800000, -0.0004400000, -0.0005000000,
    -0.0006450000, -0.0008500000, -0.0009000000, -0.0007350000, -0.0005000000};
//1.65745, 1.6570345, 1.6565925, 1.654874, 1.652649,
  /** Market values for the GBP-FRAL3MIRSL3M curve */
  private static final double[] GBP_FWD3_1_MARKET_QUOTES = new double[] {
    0.0056400000,
    0.0067900000, 0.0084700000,
    0.0078400000, 0.0113900000, 0.0143300000, 0.0164100000, 0.0179400000,
    0.0192000000, 0.0202700000, 0.0211700000, 0.0219200000, 0.0226400000,
    0.0238800000, 0.0252400000, 0.0265600000, 0.0269400000, 0.0270700000};
  
  /** Generators for the USD-OISFFS curve */
  private static final int NB_ONDEPO_1 = 1;
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] USD_OISFFS_1_GENERATORS = 
      CurveCalibrationConventionDataSets.generatorUsdOnOisFfs(NB_ONDEPO_1, 6, 14);
  /** Tenors for the USD-OISFFS curve */
  private static final Period[] USD_OISFFS_1_TENOR = new Period[] {Period.ofDays(0),
    Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1), 
    Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(6), 
    Period.ofYears(7), Period.ofYears(8), Period.ofYears(9), Period.ofYears(10), Period.ofYears(12), 
    Period.ofYears(15), Period.ofYears(20), Period.ofYears(25), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] USD_OISFFS_1_ATTR = new GeneratorAttributeIR[USD_OISFFS_1_TENOR.length];
  static {
    for (int loopins = 0; loopins < NB_ONDEPO_1; loopins++) {
      USD_OISFFS_1_ATTR[loopins] = new GeneratorAttributeIR(USD_OISFFS_1_TENOR[loopins], Period.ofDays(0));
    }
    for (int loopins = NB_ONDEPO_1; loopins < USD_OISFFS_1_TENOR.length; loopins++) {
      USD_OISFFS_1_ATTR[loopins] = new GeneratorAttributeIR(USD_OISFFS_1_TENOR[loopins]);
    }
  }
  
  /** Generators for the USD-FRAL3M-IRSL3M curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] USD_FWD3_1_GENERATORS = 
      CurveCalibrationConventionDataSets.generatorUsdIbor3Fra3Irs3(1, 2, 12);
  /** Tenors for the USD-FRAL3M-IRSL3M curve */
  private static final Period[] USD_FWD3_1_TENOR = new Period[] {Period.ofMonths(0),
    Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(7), Period.ofYears(10), Period.ofYears(12), Period.ofYears(15), Period.ofYears(20), 
    Period.ofYears(25), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] USD_FWD3_1_ATTR = new GeneratorAttributeIR[USD_FWD3_1_TENOR.length];
  static {
    for (int loopins = 0; loopins < USD_FWD3_1_TENOR.length; loopins++) {
      USD_FWD3_1_ATTR[loopins] = new GeneratorAttributeIR(USD_FWD3_1_TENOR[loopins]);
    }
  }

  /** Generators for the USD-IRSL1M-BSL1ML3M curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] USD_FWD1_1_GENERATORS = 
      new GeneratorInstrument<?>[] {GENERATOR_USDLIBOR1M,
    USD1YLIBOR1M, USD1YLIBOR1M,
    USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, 
    USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, 
    USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M, USD3MLIBOR1MLIBOR3M };
  /** Tenors for the USD-FRAL1M-BSL1ML3M curve */
  private static final Period[] USD_FWD1_1_TENOR = new Period[] {Period.ofMonths(0),
    Period.ofMonths(2), Period.ofMonths(3),
    Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), 
    Period.ofYears(4), Period.ofYears(5), Period.ofYears(7), Period.ofYears(10), Period.ofYears(12), 
    Period.ofYears(15), Period.ofYears(20), Period.ofYears(25), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] USD_FWD1_1_ATTR = new GeneratorAttributeIR[USD_FWD1_1_TENOR.length];
  static {
    for (int loopins = 0; loopins < USD_FWD1_1_TENOR.length; loopins++) {
      USD_FWD1_1_ATTR[loopins] = new GeneratorAttributeIR(USD_FWD1_1_TENOR[loopins]);
    }
  }

  /** Generators for the USD-FRAL6M-BSL3ML6M curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] USD_FWD6_1_GENERATORS = 
      new GeneratorInstrument<?>[] {GENERATOR_USDLIBOR6M,
    GENERATOR_FRA6M, 
    USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M,
    USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M, 
    USD6MLIBOR3MLIBOR6M, USD6MLIBOR3MLIBOR6M };
  /** Tenors for the USD-FRAL6M-BSL3ML6M curve */
  private static final Period[] USD_FWD6_1_TENOR = new Period[] {Period.ofMonths(0),
    Period.ofMonths(9), 
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(7), Period.ofYears(10), Period.ofYears(12), Period.ofYears(15), Period.ofYears(20), 
    Period.ofYears(25), Period.ofYears(30) };
  private static final GeneratorAttributeIR[] USD_FWD6_1_ATTR = new GeneratorAttributeIR[USD_FWD6_1_TENOR.length];
  static {
    for (int loopins = 0; loopins < USD_FWD6_1_TENOR.length; loopins++) {
      USD_FWD6_1_ATTR[loopins] = new GeneratorAttributeIR(USD_FWD6_1_TENOR[loopins]);
    }
  }
  
  /** Generators for the GBPUSD-FXGBPUSD-XCCYGBPL3MUSDL3M curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] GBP_DSC_1_GENERATORS =
      new GeneratorInstrument<?>[] {GENERATOR_FXFWD_GBPUSD, GENERATOR_FXFWD_GBPUSD, GENERATOR_FXFWD_GBPUSD, GENERATOR_FXFWD_GBPUSD, GENERATOR_FXFWD_GBPUSD,
        GBPLIBOR3MUSDLIBOR3M, GBPLIBOR3MUSDLIBOR3M, GBPLIBOR3MUSDLIBOR3M, GBPLIBOR3MUSDLIBOR3M, GBPLIBOR3MUSDLIBOR3M,
        GBPLIBOR3MUSDLIBOR3M, GBPLIBOR3MUSDLIBOR3M, GBPLIBOR3MUSDLIBOR3M, GBPLIBOR3MUSDLIBOR3M, GBPLIBOR3MUSDLIBOR3M,
        GBPLIBOR3MUSDLIBOR3M, GBPLIBOR3MUSDLIBOR3M, GBPLIBOR3MUSDLIBOR3M, GBPLIBOR3MUSDLIBOR3M, GBPLIBOR3MUSDLIBOR3M };
  /** Tenors for the GBPUSD-FXGBPUSD-XCCYGBPL3MUSDL3M curve */
  private static final Period[] GBP_DSC_1_TENOR = new Period[] {
    Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(6), Period.ofYears(7), Period.ofYears(8), Period.ofYears(9), Period.ofYears(10),
    Period.ofYears(12), Period.ofYears(15), Period.ofYears(20), Period.ofYears(25), Period.ofYears(30) };
  private static final GeneratorAttribute[] GBP_DSC_1_ATTR = new GeneratorAttribute[GBP_DSC_1_TENOR.length];
  static {
    for (int loopins = 0; loopins < GBP_DSC_1_TENOR.length; loopins++) {
      GBP_DSC_1_ATTR[loopins] = new GeneratorAttributeFX(GBP_DSC_1_TENOR[loopins], FX_MATRIX_SPOT);
    }
  }

  /** Generators for the GBP-FRAL3MIRSL3M curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] GBP_FWD3_1_GENERATORS =
      CurveCalibrationConventionDataSets.generatorGbpIbor3Fra3Irs3(1, 2, 15);
  /** Tenors for the GBP-FRAL3MIRSL3M curve */
  private static final Period[] GBP_FWD3_1_TENOR = new Period[] {Period.ofMonths(0),
    Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(6), Period.ofYears(7), Period.ofYears(8), Period.ofYears(9), Period.ofYears(10),
    Period.ofYears(12), Period.ofYears(15), Period.ofYears(20), Period.ofYears(25), Period.ofYears(30) };
  private static final GeneratorAttribute[] GBP_FWD3_1_ATTR = new GeneratorAttribute[GBP_FWD3_1_TENOR.length];
  static {
    for (int loopins = 0; loopins < GBP_FWD3_1_TENOR.length; loopins++) {
      GBP_FWD3_1_ATTR[loopins] = new GeneratorAttributeIR(GBP_FWD3_1_TENOR[loopins]);
    }
  }

  /** Units of curves */
  private static final int[] NB_UNITS = new int[] {4 };
  private static final int NB_BLOCKS = NB_UNITS.length;
  private static final InstrumentDefinition<?>[][][][] DEFINITIONS_UNITS = new InstrumentDefinition<?>[NB_BLOCKS][][][];
  private static final GeneratorYDCurve[][][] GENERATORS_UNITS = new GeneratorYDCurve[NB_BLOCKS][][];
  private static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];
  private static final MulticurveProviderDiscount KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX_SPOT);
  private static final LinkedHashMap<String, Currency> DSC_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IborIndex[]> FWD_IBOR_MAP = new LinkedHashMap<>();

  static {
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      DEFINITIONS_UNITS[loopblock] = new InstrumentDefinition<?>[NB_UNITS[loopblock]][][];
      GENERATORS_UNITS[loopblock] = new GeneratorYDCurve[NB_UNITS[loopblock]][];
      NAMES_UNITS[loopblock] = new String[NB_UNITS[loopblock]][];
    }
//    final GeneratorYDCurve genIntLin = CurveCalibrationConventionDataSets.generatorYDMatLin();
    final GeneratorYDCurve genIntNcs = CurveCalibrationConventionDataSets.generatorYDMatNcs();
    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {genIntNcs, genIntNcs };
    GENERATORS_UNITS[0][1] = new GeneratorYDCurve[] {genIntNcs };
    GENERATORS_UNITS[0][2] = new GeneratorYDCurve[] {genIntNcs };
    GENERATORS_UNITS[0][3] = new GeneratorYDCurve[] {genIntNcs, genIntNcs };
    NAMES_UNITS[0][0] = new String[] {NAME_OUTPUT_DSC_USD, NAME_OUTPUT_FWD3_USD };
    NAMES_UNITS[0][1] = new String[] {NAME_OUTPUT_FWD1_USD };
    NAMES_UNITS[0][2] = new String[] {NAME_OUTPUT_FWD6_USD};
    NAMES_UNITS[0][3] = new String[] {NAME_OUTPUT_DSC_GBP, NAME_OUTPUT_FWD3_GBP };
    DSC_MAP.put(NAME_OUTPUT_DSC_USD, USD);
    FWD_ON_MAP.put(NAME_OUTPUT_DSC_USD, new IndexON[] {USDFEDFUND });
    FWD_IBOR_MAP.put(NAME_OUTPUT_FWD3_USD, new IborIndex[] {USDLIBOR3M });
    FWD_IBOR_MAP.put(NAME_OUTPUT_FWD1_USD, new IborIndex[] {USDLIBOR1M });
    FWD_IBOR_MAP.put(NAME_OUTPUT_FWD6_USD, new IborIndex[] {USDLIBOR6M });
    DSC_MAP.put(NAME_OUTPUT_DSC_GBP, GBP);
    FWD_IBOR_MAP.put(NAME_OUTPUT_FWD3_GBP, new IborIndex[] {GBPLIBOR3M });
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
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQC = 
      ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSC = 
      ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();
  private static final MulticurveDiscountBuildingRepository CURVE_BUILDING_REPOSITORY = 
      CurveCalibrationConventionDataSets.curveBuildingRepositoryMulticurve();

  /**
   * Calibrate curves with hard-coded date and with calibration date provided.
   * The curves are 
   *  - USD discounting/overnight forward, USDIbor3M forward, USDIbor1M forward and USDIbor6M forward.
   *  - GBP discounting, GBPIbor3M forward.
   * OIS are used for the discounting curve from 2 years up to 30 years.
   * USDIbor3M curve uses FRA and IRS.
   * USDIbor1M and Libor6M use FRA and basis swaps v 3M.
   * @param calibrationDate The calibration date.
   * @return The curves and the Jacobian matrices.
   */
  public static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> getCurvesUsdDscL1L3L6GbpDscL3(ZonedDateTime calibrationDate) {
    InstrumentDefinition<?>[][][] definitionsUnits = new InstrumentDefinition<?>[NB_UNITS[0]][][];
    InstrumentDefinition<?>[] definitionsUsdDsc = getDefinitions(USD_OISFFS_1_MARKET_QUOTES, USD_OISFFS_1_GENERATORS, 
        USD_OISFFS_1_ATTR, calibrationDate);
    InstrumentDefinition<?>[] definitionsUsdFwd3 = getDefinitions(USD_FWD3_1_MARKET_QUOTES, USD_FWD3_1_GENERATORS, 
        USD_FWD3_1_ATTR, calibrationDate);
    InstrumentDefinition<?>[] definitionsUsdFwd1 = getDefinitions(USD_FWD1_1_MARKET_QUOTES, USD_FWD1_1_GENERATORS, 
        USD_FWD1_1_ATTR, calibrationDate);
    InstrumentDefinition<?>[] definitionsUsdFwd6 = getDefinitions(USD_FWD6_1_MARKET_QUOTES, USD_FWD6_1_GENERATORS, 
        USD_FWD6_1_ATTR, calibrationDate);
    InstrumentDefinition<?>[] definitionsGbpDsc = getDefinitions(GBP_DSC_1_MARKET_QUOTES, GBP_DSC_1_GENERATORS, 
        GBP_DSC_1_ATTR, calibrationDate);
    InstrumentDefinition<?>[] definitionsGbpFwd3 = getDefinitions(GBP_FWD3_1_MARKET_QUOTES, GBP_FWD3_1_GENERATORS, 
        GBP_FWD3_1_ATTR, calibrationDate);
    definitionsUnits[0] = new InstrumentDefinition<?>[][] {definitionsUsdDsc, definitionsUsdFwd3 };
    definitionsUnits[1] = new InstrumentDefinition<?>[][] {definitionsUsdFwd1 };
    definitionsUnits[2] = new InstrumentDefinition<?>[][] {definitionsUsdFwd6 };
    definitionsUnits[3] = new InstrumentDefinition<?>[][] {definitionsGbpDsc, definitionsGbpFwd3 };
    return CurveCalibrationTestsUtils.makeCurvesFromDefinitionsMulticurve(calibrationDate, definitionsUnits, 
        GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQC, PSMQCSC, DSC_MAP, FWD_ON_MAP, FWD_IBOR_MAP, 
        CURVE_BUILDING_REPOSITORY, TS_ON_WITHOUT_TODAY, TS_IBOR_WITHOUT_TODAY);
  }
  
  private static final ZonedDateTimeDoubleTimeSeries TS_USD_ON_WITHOUT_TODAY = 
      StandardTimeSeriesDataSets.timeSeriesUsdOn2014Jan(DATA_DATE_1);
  private static final Map<IndexON,ZonedDateTimeDoubleTimeSeries> TS_ON_WITHOUT_TODAY = 
      new HashMap<>();
      static {
        TS_ON_WITHOUT_TODAY.put(USDFEDFUND, TS_USD_ON_WITHOUT_TODAY);        
      }

  private static final ZonedDateTimeDoubleTimeSeries TS_USD_IBOR3M_WITHOUT_LAST = 
      StandardTimeSeriesDataSets.timeSeriesUsdIbor3M2014Jan(DATA_DATE_1);
  private static final ZonedDateTimeDoubleTimeSeries TS_GBP_IBOR3M_WITHOUT_LAST = 
      StandardTimeSeriesDataSets.timeSeriesGbpIbor3M2014Jan(DATA_DATE_1);
  private static final Map<IborIndex,ZonedDateTimeDoubleTimeSeries> TS_IBOR_WITHOUT_TODAY = 
      new HashMap<>();
      static {
        TS_IBOR_WITHOUT_TODAY.put(USDLIBOR1M, TS_USD_IBOR3M_WITHOUT_LAST); // TODO: Change not 1M ts when available
        TS_IBOR_WITHOUT_TODAY.put(USDLIBOR3M, TS_USD_IBOR3M_WITHOUT_LAST);
        TS_IBOR_WITHOUT_TODAY.put(USDLIBOR6M, TS_USD_IBOR3M_WITHOUT_LAST); // TODO: Change not 6M ts when available
        TS_IBOR_WITHOUT_TODAY.put(GBPLIBOR3M, TS_GBP_IBOR3M_WITHOUT_LAST);
      }

}
