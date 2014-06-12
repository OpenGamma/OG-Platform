package com.opengamma.analytics.financial.credit.cds;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.schedule.NoHolidayCalendar;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.test.TestGroup;

/**
 * Test cases taken from the ISDA example C code and Excel sheet
 */
@Test(groups = TestGroup.UNIT)
public class ISDAApproxCDSPricingMethodTest extends ISDAApproxCDSPricingMethodTestData {

  /**
   * Test against the same data as in the ISDA example (main.c)
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testISDAExampleMainC() {

    final ZonedDateTime pricingDate = LocalDateTime.of(2008, 2, 1, 0, 0, 0, 0).atZone(ZoneOffset.UTC);
    final ZonedDateTime stepinDate = LocalDateTime.of(2008, 2, 9, 0, 0, 0, 0).atZone(ZoneOffset.UTC);
    final ZonedDateTime settlementDate = LocalDateTime.of(2008, 2, 1, 0, 0, 0, 0).atZone(ZoneOffset.UTC);

    final ISDACurve discountCurve = loadDiscountCurve_ISDAExampleMainC();
    final ISDACurve hazardRateCurve = loadHazardRateCurve_ISDAExampleMainC();

    final ISDAApproxCDSPricingMethod method = new ISDAApproxCDSPricingMethod();

    final Map<Double, Double> testCases = new HashMap<>();
    testCases.put(0.0, 38651.94460867186700000000);
    testCases.put(3600.0, -10682.51917248596700000000);
    testCases.put(7200.0, -60016.98295364380500000000);

    ISDACDSDerivative cds;
    double upfrontCharge;

    for(final Entry<Double, Double> testCase : testCases.entrySet()) {
      cds = loadCDS_ISDAExampleMainC(testCase.getKey()).toDerivative(pricingDate, stepinDate, settlementDate, "IR_CURVE", "HAZARD_RATE_CURVE");
      upfrontCharge = method.calculateUpfrontCharge(cds, discountCurve, hazardRateCurve, false);
      Assert.assertEquals(upfrontCharge, testCase.getValue());
    }
  }

  @Test
  public void testISDAExcelExampleCalculator_FlatIRCurve() {

    final ZonedDateTime pricingDate = LocalDateTime.of(2008, 9, 18, 0, 0, 0, 0).atZone(ZoneOffset.UTC);

    final ISDACDSDerivative cds = loadCDS_ISDAExampleCDSCalcualtor().toDerivative(pricingDate, "IR_CURVE", "HAZARD_RATE_CURVE");
    final ISDACurve discountCurve = loadDiscountCurve_ISDAExampleCDSExcelFlat();
    final ISDACurve hazardRateCurve = loadHazardRateCurve_ISDAExampleCDSCalculator_FlatIRCurve();

    final ISDAApproxCDSPricingMethod method = new ISDAApproxCDSPricingMethod();

    final double cleanPrice = method.calculateUpfrontCharge(cds, discountCurve, hazardRateCurve, true);
    final double dirtyPrice = method.calculateUpfrontCharge(cds, discountCurve, hazardRateCurve, false);
    final double cleanPriceError = Math.abs( (cleanPrice - 1679277.52264122) / cds.getNotional() );
    final double dirtyPriceError = Math.abs( (dirtyPrice - 1653999.74486344) / cds.getNotional() );

    Assert.assertTrue(cleanPriceError < 1E-15);
    Assert.assertTrue(dirtyPriceError < 1E-15);
  }

  @Test
  public void testISDAExcelExampleCalculator() {

    final ZonedDateTime pricingDate = LocalDateTime.of(2008, 9, 18, 0, 0, 0, 0).atZone(ZoneOffset.UTC);

    final ISDACDSDerivative cds = loadCDS_ISDAExampleCDSCalcualtor().toDerivative(pricingDate, "IR_CURVE", "HAZARD_RATE_CURVE");
    final ISDACurve discountCurve = loadDiscountCurve_ISDAExampleExcel();
    final ISDACurve hazardRateCurve = loadHazardRateCurve_ISDAExampleCDSCalculator();

    final ISDAApproxCDSPricingMethod method = new ISDAApproxCDSPricingMethod();

    final double cleanPrice = method.calculateUpfrontCharge(cds, discountCurve, hazardRateCurve, true);
    final double dirtyPrice = method.calculateUpfrontCharge(cds, discountCurve, hazardRateCurve, false);
    final double cleanPriceError = Math.abs( (cleanPrice - 1605993.21801408) / cds.getNotional() );
    final double dirtyPriceError = Math.abs( (dirtyPrice - 1580715.44023631) / cds.getNotional() );

    Assert.assertTrue(cleanPriceError < 1E-15);
    Assert.assertTrue(dirtyPriceError < 1E-15);
  }

  @Test
  public void testISDAExcelCDSConverter() {

    final ZonedDateTime pricingDate = LocalDateTime.of(2008, 9, 18, 0, 0, 0, 0).atZone(ZoneOffset.UTC);
    final ZonedDateTime stepinDate = LocalDateTime.of(2008, 9, 19, 0, 0, 0, 0).atZone(ZoneOffset.UTC);
    final ZonedDateTime settlementDate = LocalDateTime.of(2008, 9, 23, 0, 0, 0, 0).atZone(ZoneOffset.UTC);

    final ISDACDSDerivative cds = loadCDS_ISDAExampleUpfrontConverter().toDerivative(pricingDate, "IR_CURVE", "HAZARD_RATE_CURVE");
    final ISDACurve discountCurve = loadDiscountCurve_ISDAExampleExcel();

    final ISDAApproxCDSPricingMethod method = new ISDAApproxCDSPricingMethod();
    final Calendar calendar = new NoHolidayCalendar();
    final double cleanPrice = method.calculateUpfrontCharge(cds, discountCurve, 0.055, true, pricingDate, stepinDate, settlementDate, calendar);
    final double dirtyPrice = method.calculateUpfrontCharge(cds, discountCurve, 0.055, false, pricingDate, stepinDate, settlementDate, calendar);
    final double cleanPriceError = Math.abs( (cleanPrice - 185852.587288133) / cds.getNotional() );
    final double dirtyPriceError = Math.abs( (dirtyPrice - 59463.6983992436) / cds.getNotional() );

    // The approximate method can't quite get 1E-15 accuracy when using the hazard rate solver
    // The tolerance of the solver in the ISDA C code is hard coded to 1E-10, so this is reasonable
    Assert.assertTrue(cleanPriceError < 1E-14);
    Assert.assertTrue(dirtyPriceError < 1E-14);
  }

}




