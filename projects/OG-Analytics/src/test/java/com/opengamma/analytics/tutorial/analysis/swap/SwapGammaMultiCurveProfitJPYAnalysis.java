/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.tutorial.analysis.swap;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.calculator.discounting.CrossGammaMultiCurveCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.tutorial.datasets.AnalysisMarketDataJPYSets;
import com.opengamma.financial.convention.calendar.ExceptionCalendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Analysis of cross-gamma to zero-coupon rate - Multi-curve settings.
 */
public class SwapGammaMultiCurveProfitJPYAnalysis {

  private static final ExceptionCalendar TYO = new MondayToFridayCalendar("TYO");

  private static final ZonedDateTime CALIBRATION_DATE = DateUtils.getUTCDate(2014, 8, 2);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(CALIBRATION_DATE, 2, TYO);

  private static final GeneratorSwapFixedIborMaster MASTER_IRS = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapIborIborMaster MASTER_BS = GeneratorSwapIborIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor JPY6MLIBOR6M = MASTER_IRS.getGenerator("JPY6MLIBOR6M", TYO);
  private static final GeneratorSwapIborIbor JPYLIBOR3MLIBOR6M = MASTER_BS.getGenerator("JPYLIBOR3MLIBOR6M", TYO);
  private static final IndexIborMaster MASTER_IBOR_INDEX = IndexIborMaster.getInstance();
  private static final IborIndex JPYLIBOR6M = MASTER_IBOR_INDEX.getIndex("JPYLIBOR6M");
  private static final Currency JPY = JPYLIBOR6M.getCurrency();

  private static final Period TENOR_START = Period.ofMonths(60);
  private static final Period TENOR_SWAP = Period.ofYears(5);
  private static final boolean IS_PAYER = true;

  private static final double NOTIONAL = 1.0E6; // 1m
  private static final ZonedDateTime START_DATE = ScheduleCalculator.getAdjustedDate(SPOT_DATE, TENOR_START, JPYLIBOR6M, TYO);
  private static final SwapFixedIborDefinition IRS_JPY_DEFINITION = SwapFixedIborDefinition.from(START_DATE, TENOR_SWAP, JPY6MLIBOR6M, NOTIONAL, 0.02, IS_PAYER);
  private static final SwapFixedCoupon<Coupon> IRS_JPY = IRS_JPY_DEFINITION.toDerivative(CALIBRATION_DATE);
  private static final double SPREAD_BS = 0.0005;
  private static final SwapIborIborDefinition BS_JPY_DEFINITION =
      JPYLIBOR3MLIBOR6M.generateInstrument(CALIBRATION_DATE, SPREAD_BS, NOTIONAL, new GeneratorAttributeIR(TENOR_START, TENOR_SWAP));
  private static final Swap<?,?> BS_JPY = BS_JPY_DEFINITION.toDerivative(CALIBRATION_DATE);
  
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final CrossGammaMultiCurveCalculator CGMCC = new CrossGammaMultiCurveCalculator(PVCSDC);
  
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_PAIR_0 = 
      AnalysisMarketDataJPYSets.getMulticurveJPY();
  private static final MulticurveProviderDiscount MULTICURVE = MULTICURVE_PAIR_0.getFirst();
  
  private static final String[] CURVE_NAME = new String[2];
  static {
    CURVE_NAME[0] = MULTICURVE_PAIR_0.getFirst().getName(JPY);
    CURVE_NAME[1] = MULTICURVE_PAIR_0.getFirst().getName(JPYLIBOR6M);
  }

  @SuppressWarnings("unused")
  @Test(enabled = false)
  public void crossGammaMulticurveIntraCurve() {
    HashMap<String, DoubleMatrix2D> crossGammaIntraIrs = CGMCC.calculateCrossGammaIntraCurve(IRS_JPY, MULTICURVE);
    HashMap<String, DoubleMatrix2D> crossGammaIntraBs = CGMCC.calculateCrossGammaIntraCurve(BS_JPY, MULTICURVE);
    for(String name : crossGammaIntraIrs.keySet()) {
      exportMatrix(crossGammaIntraIrs.get(name).getData(), "cross-gamma-jpy-irs-"+ name + ".csv");
    }
    for(String name : crossGammaIntraBs.keySet()) {
      exportMatrix(crossGammaIntraBs.get(name).getData(), "cross-gamma-jpy-bs-"+ name + ".csv");
    }
    int t=0;
  }
  
private void exportMatrix(double[][] matrix, String fileName) {
  try {
    final FileWriter writer = new FileWriter(fileName);
    for (int loop1 = 0; loop1 < matrix.length; loop1++) {
      String line = "";
      for (int loop2 = 0; loop2 < matrix[loop1].length; loop2++) {
        line = line + "," + matrix[loop1][loop2];
      }
      writer.append(line + "0 \n");
    }
    writer.flush();
    writer.close();
  } catch (final IOException e) {
    e.printStackTrace();
  }
}

@SuppressWarnings("unused")
@Test(enabled = false)
public void performanceGamma() {
  long startTime, endTime;
  final int nbTest = 1000;

  startTime = System.currentTimeMillis();
  for (int looptest = 0; looptest < nbTest; looptest++) {
    MultipleCurrencyAmount pv = IRS_JPY.accept(PVDC, MULTICURVE);
  }
  endTime = System.currentTimeMillis();
  System.out.println("CrossGammaMultiCurveCalculator - " + nbTest + " pv - 3 curves: " + (endTime - startTime) + " ms");
  // Performance note: PVD: 04-Aug-2014: On Mac Book Pro 2.6 GHz Intel Core i7: 20 ms for 1000 sets.

  startTime = System.currentTimeMillis();
  for (int looptest = 0; looptest < nbTest; looptest++) {
    MultipleCurrencyMulticurveSensitivity pvcs = IRS_JPY.accept(PVCSDC, MULTICURVE);
  }
  endTime = System.currentTimeMillis();
  System.out.println("CrossGammaMultiCurveCalculator - " + nbTest + " pvcs - 3 curves: " + (endTime - startTime) + " ms");
  // Performance note: PVCSD: 04-Aug-2014: On Mac Book Pro 2.6 GHz Intel Core i7: 30 ms for 1000 sets.
  
  startTime = System.currentTimeMillis();
  for (int looptest = 0; looptest < nbTest; looptest++) {
    HashMap<String, DoubleMatrix2D> crossGammaIntra = CGMCC.calculateCrossGammaIntraCurve(IRS_JPY, MULTICURVE);
  }
  endTime = System.currentTimeMillis();
  System.out.println("CrossGammaMultiCurveCalculator - " + nbTest + " intro-curve x-gamma - 3 curves: " + (endTime - startTime) + " ms");
  // Performance note: Cross-gamma intra-curve 2 curves: 07-Nov-12: On Mac Book Pro 2.6 GHz Intel Core i7: 2000 ms for 1000 sets.
  
}


@SuppressWarnings("unused")
@Test(enabled = true)
public void performanceCalibration() {
  long startTime, endTime;
  final int nbTest = 100;
  
  double[] dscQuotes = new double[] {0.0001,
      0.0006, 0.0006, 0.0006, 0.0005, 0.0006, 0.0007, 0.0007, 0.0006, 0.0007, 0.0006, 
      0.0006, 0.0006, 0.0010, 0.0010, 0.0014, 0.0021, 0.0027, 0.0034, 0.0041, 0.0057,
      0.0083, 0.0115, 0.0131, 0.0141, 0.0154};
  
  double[] fwd6Quotes = new double[] {0.0017786, 
      0.0029, 0.0029, 0.0029, 
      0.0018, 0.0017, 0.0018, 0.0021, 0.0025, 0.0025, 0.0031, 0.0040, 0.0048, 0.0057, 
      0.0065, 0.0084, 0.0111, 0.0143, 0.0160, 0.0170, 0.0183};
  
  double[] fwd3Quotes = new double[] {0.0013, 
      0.0020, 0.0020, 0.0020, 0.0019, 0.0019, 0.0019, 
      0.0005, 0.0005, 0.0005, 0.0005, 0.0006, 0.0007, 0.0008, 0.0009, 0.0009, 0.0010, 
      0.0011, 0.0012, 0.0013, 0.0013, 0.0013, 0.0013, 0.0014};

  startTime = System.currentTimeMillis();
  for (int looptest = 0; looptest < nbTest; looptest++) {
    Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> multicurve3Pair = 
        AnalysisMarketDataJPYSets.getMulticurveJPYOisL6L3(CALIBRATION_DATE, dscQuotes, fwd6Quotes, fwd3Quotes);
  }
  endTime = System.currentTimeMillis();
  System.out.println("CrossGammaMultiCurveCalculator - " + nbTest + " 3 curves - 3 units calibrations: " + (endTime - startTime) + " ms");
  // Performance note: 3 Curve calibration: 04-Aug-2014: On Mac Book Pro 2.6 GHz Intel Core i7: 3900 ms for 100 sets.

  startTime = System.currentTimeMillis();
  for (int looptest = 0; looptest < nbTest; looptest++) {
    Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> multicurve2Pair = 
        AnalysisMarketDataJPYSets.getMulticurveJPYOisL6(CALIBRATION_DATE, dscQuotes, fwd6Quotes);
  }
  endTime = System.currentTimeMillis();
  System.out.println("CrossGammaMultiCurveCalculator - " + nbTest + " 2 curves - 2 units calibrations: " + (endTime - startTime) + " ms");
  // Performance note: 2 Curve calibration: 04-Aug-2014: On Mac Book Pro 2.6 GHz Intel Core i7: 1900 ms for 100 sets.

  startTime = System.currentTimeMillis();
  for (int looptest = 0; looptest < nbTest; looptest++) {
    Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> multicurve3Pair = 
        AnalysisMarketDataJPYSets.getMulticurveJPYOisL6L3OneUnit(CALIBRATION_DATE, dscQuotes, fwd6Quotes, fwd3Quotes);
  }
  endTime = System.currentTimeMillis();
  System.out.println("CrossGammaMultiCurveCalculator - " + nbTest + " 3 curves - 1 unit calibrations: " + (endTime - startTime) + " ms");
  // Performance note: 3 Curve calibration: 04-Aug-2014: On Mac Book Pro 2.6 GHz Intel Core i7: 6000 ms for 100 sets.

  startTime = System.currentTimeMillis();
  for (int looptest = 0; looptest < nbTest; looptest++) {
    Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> multicurve3Pair = 
        AnalysisMarketDataJPYSets.getMulticurveJPYOisL6OneUnit(CALIBRATION_DATE, dscQuotes, fwd6Quotes);
  }
  endTime = System.currentTimeMillis();
  System.out.println("CrossGammaMultiCurveCalculator - " + nbTest + " 2 curves - 1 unit calibrations: " + (endTime - startTime) + " ms");
  // Performance note: 3 Curve calibration: 04-Aug-2014: On Mac Book Pro 2.6 GHz Intel Core i7: xxx ms for 100 sets.
  
  startTime = System.currentTimeMillis();
  for (int looptest = 0; looptest < nbTest; looptest++) {
    Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> multicurve3Pair = 
        AnalysisMarketDataJPYSets.getMulticurveJPYOisL6L3(CALIBRATION_DATE, dscQuotes, fwd6Quotes, fwd3Quotes);
  }
  endTime = System.currentTimeMillis();
  System.out.println("CrossGammaMultiCurveCalculator - " + nbTest + " 3 curves - 3 units calibrations: " + (endTime - startTime) + " ms");
  // Performance note: 3 Curve calibration: 04-Aug-2014: On Mac Book Pro 2.6 GHz Intel Core i7: 3900 ms for 100 sets.

  startTime = System.currentTimeMillis();
  for (int looptest = 0; looptest < nbTest; looptest++) {
    Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> multicurve2Pair = 
        AnalysisMarketDataJPYSets.getMulticurveJPYOisL6(CALIBRATION_DATE, dscQuotes, fwd6Quotes);
  }
  endTime = System.currentTimeMillis();
  System.out.println("CrossGammaMultiCurveCalculator - " + nbTest + " 2 curves - 2 units calibrations: " + (endTime - startTime) + " ms");
  // Performance note: 2 Curve calibration: 04-Aug-2014: On Mac Book Pro 2.6 GHz Intel Core i7: 1900 ms for 100 sets.

  startTime = System.currentTimeMillis();
  for (int looptest = 0; looptest < nbTest; looptest++) {
    Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> multicurve3Pair = 
        AnalysisMarketDataJPYSets.getMulticurveJPYOisL6L3OneUnit(CALIBRATION_DATE, dscQuotes, fwd6Quotes, fwd3Quotes);
  }
  endTime = System.currentTimeMillis();
  System.out.println("CrossGammaMultiCurveCalculator - " + nbTest + " 3 curves - 1 unit calibrations: " + (endTime - startTime) + " ms");
  // Performance note: 3 Curve calibration: 04-Aug-2014: On Mac Book Pro 2.6 GHz Intel Core i7: 6000 ms for 100 sets.

  startTime = System.currentTimeMillis();
  for (int looptest = 0; looptest < nbTest; looptest++) {
    Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> multicurve3Pair = 
        AnalysisMarketDataJPYSets.getMulticurveJPYOisL6OneUnit(CALIBRATION_DATE, dscQuotes, fwd6Quotes);
  }
  endTime = System.currentTimeMillis();
  System.out.println("CrossGammaMultiCurveCalculator - " + nbTest + " 2 curves - 1 unit calibrations: " + (endTime - startTime) + " ms");
  // Performance note: 3 Curve calibration: 04-Aug-2014: On Mac Book Pro 2.6 GHz Intel Core i7: xxx ms for 100 sets.
  
}

}
