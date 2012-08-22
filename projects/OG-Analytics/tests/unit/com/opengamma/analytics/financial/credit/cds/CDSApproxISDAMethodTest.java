package com.opengamma.analytics.financial.credit.cds;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CDSApproxISDAMethodTest extends CDSTestSetup {

  /**
   * Test against the same data as in the ISDA example (main.c)
   */
  @Test
  public void testISDAExampleMainC() {
    
    final ZonedDateTime pricingDate = ZonedDateTime.of(2008, 2, 1, 0, 0, 0, 0, TimeZone.UTC);
    final ZonedDateTime stepinDate = ZonedDateTime.of(2008, 2, 9, 0, 0, 0, 0, TimeZone.UTC);
    final ZonedDateTime settlementDate = ZonedDateTime.of(2008, 2, 1, 0, 0, 0, 0, TimeZone.UTC);
    
    final ISDACurve discountCurve = loadDiscountCurve_ISDAExampleMainC();
    final ISDACurve hazardRateCurve = loadHazardRateCurve_ISDAExampleMainC();
    
    final CDSApproxISDAMethod method = new CDSApproxISDAMethod();
    
    final Map<Double, Double> testCases = new HashMap<Double, Double>();
    testCases.put(0.0, 38651.94460867186700000000);
    testCases.put(3600.0, -10682.51917248596700000000);
    testCases.put(7200.0, -60016.98295364380500000000);
    
    CDSDerivative cds;
    double upfrontCharge;
    
    for(Entry<Double, Double> testCase : testCases.entrySet()) {
      cds = loadCDS_ISDAExampleMainC(testCase.getKey()).toDerivative(pricingDate, "IR_CURVE", "HAZARD_RATE_CURVE");
      upfrontCharge = method.calculateUpfrontCharge(cds, discountCurve, hazardRateCurve, pricingDate, stepinDate, settlementDate, false);
      Assert.assertEquals(upfrontCharge, testCase.getValue());
    }
  }
  
  @Test
  public void testISDAExcelExampleCalculator_FlatIRCurve() {
    
    final ZonedDateTime pricingDate = ZonedDateTime.of(2008, 9, 18, 0, 0, 0, 0, TimeZone.UTC);
    final ZonedDateTime stepinDate = pricingDate.plusDays(1);
    final ZonedDateTime settlementDate = ZonedDateTime.of(2008, 9, 23, 0, 0, 0, 0, TimeZone.UTC);
    
    final CDSDerivative cds = loadCDS_ISDAExampleCDSCalcualtor().toDerivative(pricingDate, "IR_CURVE", "HAZARD_RATE_CURVE");   
    final ISDACurve discountCurve = loadDiscountCurve_ISDAExampleCDSExcelFlat();
    final ISDACurve hazardRateCurve = loadHazardRateCurve_ISDAExampleCDSCalculator_FlatIRCurve();
    
    final CDSApproxISDAMethod method = new CDSApproxISDAMethod();

    final double cleanPrice = method.calculateUpfrontCharge(cds, discountCurve, hazardRateCurve, pricingDate, stepinDate, settlementDate, true);
    final double dirtyPrice = method.calculateUpfrontCharge(cds, discountCurve, hazardRateCurve, pricingDate, stepinDate, settlementDate, false);
    final double cleanPriceError = Math.abs( (cleanPrice - 1679277.52264122) / cds.getNotional() );
    final double dirtyPriceError = Math.abs( (dirtyPrice - 1653999.74486344) / cds.getNotional() );
    
    Assert.assertTrue(cleanPriceError < 1E-15);
    Assert.assertTrue(dirtyPriceError < 1E-15);
  }
  
  @Test
  public void testISDAExcelExampleCalculator() {
    
    final ZonedDateTime pricingDate = ZonedDateTime.of(2008, 9, 18, 0, 0, 0, 0, TimeZone.UTC);
    final ZonedDateTime stepinDate = pricingDate.plusDays(1);
    final ZonedDateTime settlementDate = ZonedDateTime.of(2008, 9, 23, 0, 0, 0, 0, TimeZone.UTC);
    
    final CDSDerivative cds = loadCDS_ISDAExampleCDSCalcualtor().toDerivative(pricingDate, "IR_CURVE", "HAZARD_RATE_CURVE"); 
    final ISDACurve discountCurve = loadDiscountCurve_ISDAExampleExcel();
    final ISDACurve hazardRateCurve = loadHazardRateCurve_ISDAExampleCDSCalculator();
    
    final CDSApproxISDAMethod method = new CDSApproxISDAMethod();

    final double cleanPrice = method.calculateUpfrontCharge(cds, discountCurve, hazardRateCurve, pricingDate, stepinDate, settlementDate, true);
    final double dirtyPrice = method.calculateUpfrontCharge(cds, discountCurve, hazardRateCurve, pricingDate, stepinDate, settlementDate, false);
    final double cleanPriceError = Math.abs( (cleanPrice - 1605993.21801408) / cds.getNotional() );
    final double dirtyPriceError = Math.abs( (dirtyPrice - 1580715.44023631) / cds.getNotional() );
    
    Assert.assertTrue(cleanPriceError < 1E-15);
    Assert.assertTrue(dirtyPriceError < 1E-15);
  }
  
  @Test
  public void testISDAExcelCDSConverter() {
    
    final ZonedDateTime pricingDate = ZonedDateTime.of(2008, 9, 18, 0, 0, 0, 0, TimeZone.UTC);
    final ZonedDateTime stepinDate = pricingDate.plusDays(1);
    final ZonedDateTime settlementDate = ZonedDateTime.of(2008, 9, 23, 0, 0, 0, 0, TimeZone.UTC);
    
    final CDSDerivative cds = loadCDS_ISDAExampleUpfrontConverter().toDerivative(pricingDate, "IR_CURVE", "HAZARD_RATE_CURVE"); 
    final ISDACurve discountCurve = loadDiscountCurve_ISDAExampleExcel();
    
    final CDSApproxISDAMethod method = new CDSApproxISDAMethod();

    final double cleanPrice = method.calculateUpfrontCharge(cds, discountCurve, 0.055, pricingDate, stepinDate, settlementDate, true);
    final double dirtyPrice = method.calculateUpfrontCharge(cds, discountCurve, 0.055, pricingDate, stepinDate, settlementDate, false);
    final double cleanPriceError = Math.abs( (cleanPrice - 185852.587288133) / cds.getNotional() );
    final double dirtyPriceError = Math.abs( (dirtyPrice - 59463.6983992436) / cds.getNotional() );
    
    Assert.assertTrue(cleanPriceError < 1E-15);
    Assert.assertTrue(dirtyPriceError < 1E-15);
  }
  
}

























