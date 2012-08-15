package com.opengamma.analytics.financial.credit.cds;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.util.money.CurrencyAmount;

public class CDSApproxISDAMethodTest extends CDSTestSetup {

//  /**
//   * Test with the same data as the simple CDS method for comparison of the results
//   */
//  @Test
//  public void testComparisonWithSimpleCalculation() {
//    final CDSApproxISDAMethod method = new CDSApproxISDAMethod();
//    final CurrencyAmount result = method.presentValue(_simpleTestCDS, _simpleTestCurveBundle, _simpleTestPricingDate, _simpleTestStepinDate, /*clean price*/ false);
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
    final CurrencyAmount result = method.presentValue(_isdaTestCDS, _isdaTestCurveBundle, _isdaTestPricingDate, _isdaTestStepinDate, _isdaTestPricingDate, /*clean price*/ false);
    Assert.assertEquals(result.getAmount(), -60016.98295364380500000000);
  }
  
  /**
   * Test against the same data as in the ISDA example (main.c) with spread of 3600 bps
   */
  @Test
  public void testPresentValue3600() {

    setupIsdaTestData(3600.0);
    final CDSApproxISDAMethod method = new CDSApproxISDAMethod();
    final CurrencyAmount result = method.presentValue(_isdaTestCDS, _isdaTestCurveBundle, _isdaTestPricingDate, _isdaTestStepinDate, _isdaTestPricingDate, /*clean price*/ false);
    Assert.assertEquals(result.getAmount(), -10682.51917248596700000000);
  }

  
  /**
   * Test against the same data as in the ISDA example (main.c) with spread of 0 bps
   */
  @Test
  public void testPresentValue0() {

    setupIsdaTestData(0.0);
    final CDSApproxISDAMethod method = new CDSApproxISDAMethod();
    final CurrencyAmount result = method.presentValue(_isdaTestCDS, _isdaTestCurveBundle, _isdaTestPricingDate, _isdaTestStepinDate, _isdaTestPricingDate, /*clean price*/ false);
    Assert.assertEquals(result.getAmount(), 38651.94460867186700000000);
  }
  
  @Test
  public void testISDAExcelExampleCalculator_FlatIRCurve() {
    
    final ZonedDateTime pricingDate = ZonedDateTime.of(2008, 9, 18, 0, 0, 0, 0, TimeZone.UTC);
    final ZonedDateTime stepinDate = pricingDate.plusDays(1);
    final ZonedDateTime settlementDate = ZonedDateTime.of(2008, 9, 23, 0, 0, 0, 0, TimeZone.UTC);
    
    final CDSDerivative cds = loadCDS_ISDAExampleCDSCalcualtor().toDerivative(pricingDate, "IR_CURVE", "HAZARD_RATE_CURVE");   
    final YieldCurve irCurve = loadDiscountCurve_ISDAExampleCDSCalculator_FlatIRCurve();
    final YieldCurve hazardRateCurve = loadHazardRateCurve_ISDAExampleCDSCalculator_FlatIRCurve();
    
    final YieldCurveBundle curveBundle = new YieldCurveBundle();
    curveBundle.setCurve("IR_CURVE", irCurve);
    curveBundle.setCurve("HAZARD_RATE_CURVE", hazardRateCurve);
    
    final CDSApproxISDAMethod method = new CDSApproxISDAMethod();

    final CurrencyAmount cleanPrice = method.presentValue(cds, curveBundle, pricingDate, stepinDate, settlementDate, true);
    final CurrencyAmount dirtyPrice = method.presentValue(cds, curveBundle, pricingDate, stepinDate, settlementDate, false);
    final double cleanPriceError = Math.abs( (cleanPrice.getAmount() - 1679277.52264122) / cds.getNotional() );
    final double dirtyPriceError = Math.abs( (dirtyPrice.getAmount() - 1653999.74486344) / cds.getNotional() );
    
    Assert.assertTrue(cleanPriceError < 1E-15);
    Assert.assertTrue(dirtyPriceError < 1E-15);
  }
  
  @Test
  public void testISDAExcelExampleCalculator() {
    
    final ZonedDateTime pricingDate = ZonedDateTime.of(2008, 9, 18, 0, 0, 0, 0, TimeZone.UTC);
    final ZonedDateTime stepinDate = pricingDate.plusDays(1);
    final ZonedDateTime settlementDate = ZonedDateTime.of(2008, 9, 23, 0, 0, 0, 0, TimeZone.UTC);
    
    final CDSDerivative cds = loadCDS_ISDAExampleCDSCalcualtor().toDerivative(pricingDate, "IR_CURVE", "HAZARD_RATE_CURVE"); 
    final YieldCurve discountCurve = loadDiscountCurve_ISDAExampleCDSCalculator();
    final YieldCurve hazardRateCurve = loadHazardRateCurve_ISDAExampleCDSCalculator();
    
    final YieldCurveBundle curveBundle = new YieldCurveBundle();
    curveBundle.setCurve("IR_CURVE", discountCurve);
    curveBundle.setCurve("HAZARD_RATE_CURVE", hazardRateCurve);
    
    final CDSApproxISDAMethod method = new CDSApproxISDAMethod();

    final CurrencyAmount cleanPrice = method.presentValue(cds, curveBundle, pricingDate, stepinDate, settlementDate, true);
    final CurrencyAmount dirtyPrice = method.presentValue(cds, curveBundle, pricingDate, stepinDate, settlementDate, false);
    final double cleanPriceError = Math.abs( (cleanPrice.getAmount() - 1605993.21801408) / cds.getNotional() );
    final double dirtyPriceError = Math.abs( (dirtyPrice.getAmount() - 1580715.44023631) / cds.getNotional() );
    
    System.out.println("Clean price: " + cleanPrice + " (expected 1605993.21801408)");
    System.out.println("Dirty price: " + dirtyPrice + " (expected 1580715.44023631)");
    
    //Assert.assertTrue(cleanPriceError < 1E-15);
    //Assert.assertTrue(dirtyPriceError < 1E-15);
  }
  
}

























