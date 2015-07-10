/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.tutorial.analysis.swap;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.tutorial.datasets.UsdDatasetAug21;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Examples of risk analysis for different swaps in USD.
 * Those examples can be used for tutorials. 
 */
@Test(groups = TestGroup.UNIT)
public class SwapRiskAnalysisAug21Usd {


  public SwapRiskAnalysisAug21Usd() {
  }
  
  @Test
  public void FraCurveCalibration() {
    ZonedDateTime evalDate = DateUtils.getUTCDate(2014, 8, 21);
    Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> curveBundle = 
        UsdDatasetAug21.getFraCurve(evalDate, true, 
            UsdDatasetAug21.INTERPOLATOR_LINEAR);

    MulticurveProviderDiscount curves = curveBundle.getFirst();
    for (String yieldCurveName : curves.getAllCurveNames()) {
      YieldAndDiscountCurve yieldAndDiscountCurve = curves.getCurve(yieldCurveName);
      Curve<Double, Double> yieldCurveValues = null;
      if (yieldAndDiscountCurve instanceof YieldCurve) {
        yieldCurveValues = ((YieldCurve)yieldAndDiscountCurve).getCurve();
      } else if (yieldAndDiscountCurve instanceof DiscountCurve) {
        yieldCurveValues = ((DiscountCurve)yieldAndDiscountCurve).getCurve();
      }
      Double[] dateFractions = yieldCurveValues.getXData();
      Double[] zeroRates = yieldCurveValues.getYData();
      
      System.out.println("Curve name,Curve X,Curve Y,Start date,End date,Forward"); 
      for(int i = 0; i < dateFractions.length; ++i) {
        System.out.println(
            yieldCurveName + "," +
                String.valueOf(dateFractions[i]) + "," +
                String.valueOf(100. * zeroRates[i]) + "," + 
                UsdDatasetAug21.s_startDates[i].toLocalDate().toString() + "," +
                UsdDatasetAug21.s_endDates[i].toLocalDate().toString() + "," +
                String.valueOf(100. * yieldAndDiscountCurve.getForwardRate(
                    dateFractions[i])) + "," +
                String.valueOf(100. * yieldAndDiscountCurve.getForwardRate(
                    DayCounts.ACT_360.getDayCountFraction(evalDate, UsdDatasetAug21.s_endDates[i]))) + "," +
                String.valueOf(100. * yieldAndDiscountCurve.getForwardRate(
                    DayCounts.ACT_360.getDayCountFraction(evalDate, UsdDatasetAug21.s_startDates[i].plusMonths(6)))) + "," +
                String.valueOf(100. * yieldAndDiscountCurve.getForwardRate(
                    DayCounts.ACT_360.getDayCountFraction(evalDate, UsdDatasetAug21.s_startDates[i]))));
      }
      
      double t1 = DayCounts.ACT_360.getDayCountFraction(evalDate, DateUtils.getUTCDate(2015, 7, 9));
      double t2 = DayCounts.ACT_360.getDayCountFraction(evalDate, DateUtils.getUTCDate(2016, 1, 9));
      System.out.println("t1,t2,forward");
      System.out.println(String.valueOf(t1) + "," + String.valueOf(t2) + "," + String.valueOf(yieldAndDiscountCurve.getForwardRate(t1) * 100));
    }
    
  }

}
