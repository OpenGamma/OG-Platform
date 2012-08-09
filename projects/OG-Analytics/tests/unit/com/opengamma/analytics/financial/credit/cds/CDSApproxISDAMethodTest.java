package com.opengamma.analytics.financial.credit.cds;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.util.money.CurrencyAmount;

public class CDSApproxISDAMethodTest extends CDSTestSetup {

//  /**
//   * Test with the same data as the simple CDS method for comparison of the results
//   */
//  @Test
//  public void testComparisonWithSimpleCalculation() {
//    final CDSApproxISDAMethod method = new CDSApproxISDAMethod();
//    final CurrencyAmount result = method.presentValue(_simpleTestCDS, _simpleTestCurveBundle, _simpleTestPricingDate);
//    double differenceToreferenceModel = Math.abs(result.getAmount() - 0.05973453601495030000);
//    double tolerance = 0.01;
//    Assert.assertTrue(differenceToreferenceModel < tolerance, "Difference between results was " + differenceToreferenceModel + " which is more than the tolerance of " + tolerance);
//  }

  /**
   * Test against the same data as in the ISDA example (main.c) with spread of 7200 bps
   */
  @Test
  public void testPresentValue7200() {

    setupIsdaTestData(7200.0);
    final CDSApproxISDAMethod method = new CDSApproxISDAMethod();
    final CurrencyAmount result = method.presentValue(_isdaTestCDS, _isdaTestCurveBundle, _isdaTestPricingDate);
    Assert.assertEquals(result.getAmount(), -60016.98295364380500000000);
  }
  
  /**
   * Test against the same data as in the ISDA example (main.c) with spread of 3600 bps
   */
  @Test
  public void testPresentValue3600() {

    setupIsdaTestData(3600.0);
    final CDSApproxISDAMethod method = new CDSApproxISDAMethod();
    final CurrencyAmount result = method.presentValue(_isdaTestCDS, _isdaTestCurveBundle, _isdaTestPricingDate);
    Assert.assertEquals(result.getAmount(), -10682.51917248596700000000);
  }

  
  /**
   * Test against the same data as in the ISDA example (main.c) with spread of 0 bps
   */
  @Test
  public void testPresentValue0() {

    setupIsdaTestData(0.0);
    final CDSApproxISDAMethod method = new CDSApproxISDAMethod();
    final CurrencyAmount result = method.presentValue(_isdaTestCDS, _isdaTestCurveBundle, _isdaTestPricingDate);
    Assert.assertEquals(result.getAmount(), 38651.94460867186700000000);
  }
}
