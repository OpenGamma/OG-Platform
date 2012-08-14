package com.opengamma.analytics.financial.credit.cds;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityPaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.cds.CDSDefinition;
import com.opengamma.analytics.financial.instrument.cds.CDSPremiumDefinition;
import com.opengamma.analytics.financial.interestrate.PeriodicInterestRate;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.FollowingBusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.ActualThreeSixty;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.util.money.Currency;
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
    final CurrencyAmount result = method.presentValue(_isdaTestCDS, _isdaTestCurveBundle, _isdaTestPricingDate, _isdaTestStepinDate, /*clean price*/ false);
    Assert.assertEquals(result.getAmount(), -60016.98295364380500000000);
  }
  
  /**
   * Test against the same data as in the ISDA example (main.c) with spread of 3600 bps
   */
  @Test
  public void testPresentValue3600() {

    setupIsdaTestData(3600.0);
    final CDSApproxISDAMethod method = new CDSApproxISDAMethod();
    final CurrencyAmount result = method.presentValue(_isdaTestCDS, _isdaTestCurveBundle, _isdaTestPricingDate, _isdaTestStepinDate, /*clean price*/ false);
    Assert.assertEquals(result.getAmount(), -10682.51917248596700000000);
  }

  
  /**
   * Test against the same data as in the ISDA example (main.c) with spread of 0 bps
   */
  @Test
  public void testPresentValue0() {

    setupIsdaTestData(0.0);
    final CDSApproxISDAMethod method = new CDSApproxISDAMethod();
    final CurrencyAmount result = method.presentValue(_isdaTestCDS, _isdaTestCurveBundle, _isdaTestPricingDate, _isdaTestStepinDate, /*clean price*/ false);
    Assert.assertEquals(result.getAmount(), 38651.94460867186700000000);
  }
  
  
  private YieldCurve loadZeroCurve() {
    
    final ZonedDateTime pricingDate = ZonedDateTime.of(2008, 9, 18, 0, 0, 0, 0, TimeZone.UTC);
    
    double[] times = {
        //-s_act365.getDayCountFraction(ZonedDateTime.of(2008, 6, 30, 0, 0, 0, 0, TimeZone.UTC), pricingDate),
        //-s_act365.getDayCountFraction(ZonedDateTime.of(2008, 7, 29, 0, 0, 0, 0, TimeZone.UTC), pricingDate),
        //-s_act365.getDayCountFraction(ZonedDateTime.of(2008, 8, 29, 0, 0, 0, 0, TimeZone.UTC), pricingDate),
        0.0,
        s_act365.getDayCountFraction(pricingDate, ZonedDateTime.of(2008, 11, 28, 0, 0, 0, 0, TimeZone.UTC)),
        s_act365.getDayCountFraction(pricingDate, ZonedDateTime.of(2009, 2, 27, 0, 0, 0, 0, TimeZone.UTC)),
        s_act365.getDayCountFraction(pricingDate, ZonedDateTime.of(2009, 5, 29, 0, 0, 0, 0, TimeZone.UTC)),
        s_act365.getDayCountFraction(pricingDate, ZonedDateTime.of(2010, 5, 29, 0, 0, 0, 0, TimeZone.UTC)),
        s_act365.getDayCountFraction(pricingDate, ZonedDateTime.of(2011, 5, 29, 0, 0, 0, 0, TimeZone.UTC)),
        s_act365.getDayCountFraction(pricingDate, ZonedDateTime.of(2012, 5, 29, 0, 0, 0, 0, TimeZone.UTC)),
        s_act365.getDayCountFraction(pricingDate, ZonedDateTime.of(2013, 5, 29, 0, 0, 0, 0, TimeZone.UTC)),
        s_act365.getDayCountFraction(pricingDate, ZonedDateTime.of(2014, 5, 29, 0, 0, 0, 0, TimeZone.UTC)),
        s_act365.getDayCountFraction(pricingDate, ZonedDateTime.of(2015, 5, 29, 0, 0, 0, 0, TimeZone.UTC)),
        s_act365.getDayCountFraction(pricingDate, ZonedDateTime.of(2016, 5, 29, 0, 0, 0, 0, TimeZone.UTC)),
        s_act365.getDayCountFraction(pricingDate, ZonedDateTime.of(2017, 5, 29, 0, 0, 0, 0, TimeZone.UTC)),
        s_act365.getDayCountFraction(pricingDate, ZonedDateTime.of(2018, 5, 29, 0, 0, 0, 0, TimeZone.UTC)),
        s_act365.getDayCountFraction(pricingDate, ZonedDateTime.of(2019, 5, 29, 0, 0, 0, 0, TimeZone.UTC)),
        s_act365.getDayCountFraction(pricingDate, ZonedDateTime.of(2020, 5, 29, 0, 0, 0, 0, TimeZone.UTC)),
        s_act365.getDayCountFraction(pricingDate, ZonedDateTime.of(2023, 5, 29, 0, 0, 0, 0, TimeZone.UTC)),
        s_act365.getDayCountFraction(pricingDate, ZonedDateTime.of(2028, 5, 29, 0, 0, 0, 0, TimeZone.UTC)),
        s_act365.getDayCountFraction(pricingDate, ZonedDateTime.of(2033, 5, 29, 0, 0, 0, 0, TimeZone.UTC)),
        s_act365.getDayCountFraction(pricingDate, ZonedDateTime.of(2038, 5, 29, 0, 0, 0, 0, TimeZone.UTC)),
    };
    double[] rates = {/*0.0, 0.0,*/ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    return YieldCurve.from(InterpolatedDoublesCurve.fromSorted(times, rates, new LinearInterpolator1D()));
  }
  
  private YieldCurve loadHazardRateCurve() {
    
    final ZonedDateTime pricingDate = ZonedDateTime.of(2008, 9, 18, 0, 0, 0, 0, TimeZone.UTC);
    final double[] times = {
      0.0,
      s_act365.getDayCountFraction(pricingDate, ZonedDateTime.of(2013, 6, 20, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(pricingDate, ZonedDateTime.of(2015, 6, 20, 0, 0, 0, 0, TimeZone.UTC)),
      s_act365.getDayCountFraction(pricingDate, ZonedDateTime.of(2018, 6, 20, 0, 0, 0, 0, TimeZone.UTC)),
    };
    
    final double ccRate = (new PeriodicInterestRate(0.09740867310916100000, 1)).toContinuous().getRate();
    double[] rates = {
      ccRate, ccRate, ccRate, ccRate
    };
    
    return YieldCurve.from(InterpolatedDoublesCurve.fromSorted(times, rates, new LinearInterpolator1D()));
  }
  
  @Test
  public void testISDAExcelExampleCalculator() {
    
    final YieldCurve irCurve = loadZeroCurve();
    final YieldCurve hazardRateCurve = loadHazardRateCurve();
    
    final ZonedDateTime startDate = ZonedDateTime.of(2008, 3, 20, 0, 0, 0, 0, TimeZone.UTC);
    final ZonedDateTime maturity = ZonedDateTime.of(2013, 3, 20, 0, 0, 0, 0, TimeZone.UTC);
    final double notional = 10000000, spread = 0.01 /* 100bp */, recoveryRate = 0.4;
    
    final Frequency couponFrequency = SimpleFrequency.QUARTERLY;
    final Calendar calendar = new MondayToFridayCalendar("TestCalendar");
    final DayCount dayCount = new ActualThreeSixty();
    final BusinessDayConvention convention = new FollowingBusinessDayConvention();
    
    final CDSPremiumDefinition premiumDefinition = CDSPremiumDefinition.fromISDA(Currency.USD, startDate, maturity, couponFrequency, calendar, dayCount, convention, notional, spread, /* protect start */ true);
    final CDSDefinition cdsDefinition = new CDSDefinition(premiumDefinition, null, startDate, maturity, notional, spread, recoveryRate, /* accrualOnDefault */ true, /* payOnDefault */ true, /* protectStart */ true, dayCount);
    
    final ZonedDateTime pricingDate = ZonedDateTime.of(2008, 9, 18, 0, 0, 0, 0, TimeZone.UTC);
    final ZonedDateTime stepinDate = pricingDate.plusDays(1);
    
    final CDSDerivative cds = cdsDefinition.toDerivative(pricingDate, "IR_CURVE", "HAZARD_RATE_CURVE");
    
    final YieldCurveBundle curveBundle = new YieldCurveBundle();
    curveBundle.setCurve("IR_CURVE", irCurve);
    curveBundle.setCurve("HAZARD_RATE_CURVE", hazardRateCurve);
    
    final CDSApproxISDAMethod method = new CDSApproxISDAMethod();
    
    final CurrencyAmount dirtyPrice = method.presentValue(cds, curveBundle, pricingDate, stepinDate, false);
    final double dirtyPriceError = Math.abs( (dirtyPrice.getAmount() - 1653999.74486344000000000000) / notional );
    Assert.assertTrue(dirtyPriceError < 1E-15);
    
    final CurrencyAmount cleanPrice = method.presentValue(cds, curveBundle, pricingDate, stepinDate, true);
    final double cleanPriceError = Math.abs( (cleanPrice.getAmount() - 1679277.52264122000000000000) / notional );
    Assert.assertTrue(cleanPriceError < 1E-15);
  }
  
}

























