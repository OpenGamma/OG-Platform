/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.tutorial.analysis.inflation;

import java.io.File;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedInflationMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedInflationZeroCoupon;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsInflationUSD;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.CurveCalibrationTestsUtils;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.analytics.util.export.ExportUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Examples of risk analysis for inflation swaps in USD.
 * Those examples can be used for tutorials. 
 */
public class SwapZeroCouponInflationUsdAnalysis {

  private static final ZonedDateTime CAlIBRATION_DATE = DateUtils.getUTCDate(2014, 10, 9);
  private static final Currency USD = Currency.USD;
  private static final GeneratorSwapFixedInflationZeroCoupon GENERATOR_ZCINFLATION_US = 
      GeneratorSwapFixedInflationMaster.getInstance().getGenerator("USCPI");
  private static final IndexPrice US_CPI = GENERATOR_ZCINFLATION_US.getIndexPrice();
  
  /** Curves */
  private static final Pair<InflationProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_INFL_1_PAIR = 
      StandardDataSetsInflationUSD.getCurvesUsdOisUsCpi(CAlIBRATION_DATE);
  private static final InflationProviderDiscount MULTICURVE_INFL_1 = MULTICURVE_INFL_1_PAIR.getFirst();
  private static final Pair<InflationProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_INFL_2_PAIR = 
      StandardDataSetsInflationUSD.getCurvesUsdOisUsCpiSeasonality(CAlIBRATION_DATE);
  private static final InflationProviderDiscount MULTICURVE_INFL_2 = MULTICURVE_INFL_2_PAIR.getFirst();
  private static final Pair<InflationProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_INFL_3_PAIR = 
      StandardDataSetsInflationUSD.getCurvesUsdOisUsCpiCurrent(CAlIBRATION_DATE);
  private static final InflationProviderDiscount MULTICURVE_INFL_3 = MULTICURVE_INFL_3_PAIR.getFirst();
  private static final Pair<InflationProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_INFL_4_PAIR = 
      StandardDataSetsInflationUSD.getCurvesUsdOisUsCpiCurrentSeasonality(CAlIBRATION_DATE);
  private static final InflationProviderDiscount MULTICURVE_INFL_4 = MULTICURVE_INFL_4_PAIR.getFirst();
  
  
  @Test(enabled = true)
  public void graphCurves() {
    int nbYear = 30;
    int nbStep = nbYear * 12;
    ZonedDateTime calibrationMinusEom = CAlIBRATION_DATE.minusMonths(7).with(TemporalAdjusters.lastDayOfMonth());
    CurveCalibrationTestsUtils.exportInflationCurve(CAlIBRATION_DATE, calibrationMinusEom, MULTICURVE_INFL_1, US_CPI,
        new File("demo-test-inflation-1.csv"), nbStep, 1);
    CurveCalibrationTestsUtils.exportInflationCurve(CAlIBRATION_DATE, calibrationMinusEom, MULTICURVE_INFL_2, US_CPI,
        new File("demo-test-inflation-2.csv"), nbStep, 1);
    CurveCalibrationTestsUtils.exportInflationCurve(CAlIBRATION_DATE, calibrationMinusEom, MULTICURVE_INFL_3, US_CPI,
        new File("demo-test-inflation-3.csv"), nbStep, 1);
    CurveCalibrationTestsUtils.exportInflationCurve(CAlIBRATION_DATE, calibrationMinusEom, MULTICURVE_INFL_4, US_CPI,
        new File("demo-test-inflation-4.csv"), nbStep, 1);
  }
  

  @Test(enabled = true)
  public void exportCurves() {
    ExportUtils.exportInflationProviderDiscount(MULTICURVE_INFL_1, "infl_1.csv");
  }
  
}
