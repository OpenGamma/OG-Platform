package com.opengamma.analytics.financial.credit.cds;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.money.CurrencyAmount;

public class CDSApproxISDAMethodTest extends CDSTestSetup {

  /**
   * Test with the same data as the simple CDS method for comparison of the results
   */
//  @Test
//  public void testComparisonWithSimpleCalculation() {
//    final CDSApproxISDAMethod method = new CDSApproxISDAMethod();
//    final CurrencyAmount result = method.presentValue(_simpleTestCDS, _simpleTestCurveBundle, _simpleTestPricingDate);
//    double differenceToreferenceModel = Math.abs(result.getAmount() - 0.05973453601495030000);
//    double tolerance = 0.01;
//    Assert.assertTrue(differenceToreferenceModel < tolerance, "Difference between results was " + differenceToreferenceModel + " which is more than the tolerance of " + tolerance);
//  }

  /**
   * Test against the same data as in the ISDA example (main.c)
   */
  @Test
  public void testPresentValue() {

    final CDSApproxISDAMethod method = new CDSApproxISDAMethod();
    final CurrencyAmount result = method.presentValue(_isdaTestCDS, _isdaTestCurveBundle, _isdaTestPricingDate);
    System.out.println(result);
  }

//  @Test
//  public void testRealTimePointsForCurves() {
//
//    final ZonedDateTime pricingDate = ZonedDateTime.of(2008, 2, 1, 0, 0, 0, 0, TimeZone.UTC);
//    Double[] timePoints = CDSApproxISDAMethod.realTimePointsForCurves(_isdaTestCDS, _isdaTestCdsCcyYieldCurve, _isdaTestSpreadCurve);
//    Double[] expectedTimePoints = {
//      0.0,
//      TimeCalculator.getTimeBetween(pricingDate, pricingDate.plusDays(2)),
//      TimeCalculator.getTimeBetween(pricingDate, pricingDate.plusDays(11))
//    };
//
//    Assert.assertEquals(timePoints, expectedTimePoints);
//  }
}
