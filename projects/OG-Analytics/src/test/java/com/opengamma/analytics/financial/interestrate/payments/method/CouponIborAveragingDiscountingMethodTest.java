  /**
   * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
   *
   * Please see distribution for license.
   */
  package com.opengamma.analytics.financial.interestrate.payments.method;


  import org.testng.Assert;
  import org.testng.annotations.Test;
  import org.threeten.bp.LocalDateTime;
  import org.threeten.bp.ZoneOffset;
  import org.threeten.bp.ZonedDateTime;

  import com.opengamma.analytics.financial.instrument.index.IborIndex;
  import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
  import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageIndexDefinition;
  import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverage;
  import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborAverageDiscountingMethod;
  import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
  import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
  import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
  import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
  import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
  import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
  import com.opengamma.analytics.math.interpolation.Interpolator1D;
  import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
  import com.opengamma.financial.convention.calendar.Calendar;
  import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
  import com.opengamma.util.money.Currency;
  import com.opengamma.util.money.MultipleCurrencyAmount;
  import com.opengamma.util.test.TestGroup;
  import com.opengamma.util.time.DateUtils;

  /**
   * Tests the methods related to Ibor Averaging coupons - utilised for interpolated stubs.
   */
  @Test(groups = TestGroup.UNIT)
  public class CouponIborAveragingDiscountingMethodTest {

    private static final Calendar EUR_CALENDAR = new MondayToFridayCalendar("TARGET");
    private static final IborIndex[] IBOR_INDICES = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();
    private static final IndexIborMaster MASTER_IBOR_INDEX = IndexIborMaster.getInstance();
    
    private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR);

    private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2014, 5, 15);

    private static final double NOTIONAL = 1.0;
    private static final ZonedDateTime PAYMENT_DATE = ZonedDateTime.of(LocalDateTime.of(2014, 6, 18, 0, 0), ZoneOffset.UTC);
    private static final ZonedDateTime ACCRUAL_START_DATE = ZonedDateTime.of(LocalDateTime.of(2014, 5, 18, 0, 0), ZoneOffset.UTC);
    private static final ZonedDateTime ACCRUAL_END_DATE = ZonedDateTime.of(LocalDateTime.of(2014, 6, 18, 0, 0), ZoneOffset.UTC);
    private static final double ACCRUAL_YF = 0.08611111111111111;
    private static final ZonedDateTime FIXING_DATE = ZonedDateTime.of(LocalDateTime.of(2014, 05, 15, 0, 0), ZoneOffset.UTC);
    
    private static final IborIndex INDEX1 = IBOR_INDICES[0];
    private static final IborIndex INDEX2 = IBOR_INDICES[1];
    private static final double WEIGHT1 = 0.5;
    private static final double WEIGHT2 = 1 - WEIGHT1;
    
   
    private static CouponIborAverageIndexDefinition CPN_AVE_DEFINITION = CouponIborAverageIndexDefinition.from(PAYMENT_DATE,ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_YF, NOTIONAL, FIXING_DATE, INDEX1, INDEX2, WEIGHT1, WEIGHT2, EUR_CALENDAR, EUR_CALENDAR);
    private static final CouponIborAverage CPN_IBOR_AVERAGE = (CouponIborAverage) CPN_AVE_DEFINITION.toDerivative(REFERENCE_DATE);
    
    private static final CouponIborAverageDiscountingMethod METHOD = CouponIborAverageDiscountingMethod.getInstance();

    @Test
    /**
     * Tests the value of a stub coupon - utilises the average coupon
     */
    public void presentValue() {
      
      MulticurveProviderDiscount curveSet = createAndGetRequiredMulticurve();
      
      final MultipleCurrencyAmount pvStubCoupon = METHOD.presentValue(CPN_IBOR_AVERAGE, curveSet);
      
      final double forward1 = curveSet.getSimplyCompoundForwardRate(CPN_IBOR_AVERAGE.getIndex1(), CPN_IBOR_AVERAGE.getFixingPeriodStartTime1(), CPN_IBOR_AVERAGE.getFixingPeriodEndTime1(),
          CPN_IBOR_AVERAGE.getFixingAccrualFactor1());
      final double forward2 = curveSet.getSimplyCompoundForwardRate(CPN_IBOR_AVERAGE.getIndex2(), CPN_IBOR_AVERAGE.getFixingPeriodStartTime2(), CPN_IBOR_AVERAGE.getFixingPeriodEndTime2(),
          CPN_IBOR_AVERAGE.getFixingAccrualFactor2());
      
      double pmtYF = CPN_IBOR_AVERAGE.getPaymentYearFraction();
      double fwdRate = forward1 * WEIGHT1 + forward2 * WEIGHT2;
      double df = curveSet.getDiscountFactor(Currency.EUR, CPN_IBOR_AVERAGE.getPaymentTime());
      double flow = NOTIONAL * pmtYF * fwdRate * df;
      
      Assert.assertEquals(flow, pvStubCoupon.getAmount(Currency.EUR));
      
    }
    
    private MulticurveProviderDiscount createAndGetRequiredMulticurve() {
      
      double[] EUR_DSC_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0 };
      double[] EUR_DSC_RATE = new double[] {0.01, 0.01, 0.01, 0.01, 0.01, 0.01 };
      String EUR_DSC_NAME = "EUR";
      YieldAndDiscountCurve EUR_DSC = new YieldCurve(EUR_DSC_NAME, new InterpolatedDoublesCurve(EUR_DSC_TIME, EUR_DSC_RATE, LINEAR_FLAT, true, EUR_DSC_NAME));
     
      double[] EUR_FWD3M_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 10.0};
      double[] EUR_FWD3M_RATE = new double[] {0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02};
      String EUR_FWD3M_NAME = "EURIBOR3M";
      YieldAndDiscountCurve EUR_FWD3 = new YieldCurve(EUR_FWD3M_NAME, new InterpolatedDoublesCurve(EUR_FWD3M_TIME, EUR_FWD3M_RATE, LINEAR_FLAT, true, EUR_FWD3M_NAME));
      double[] EUR_FWD6_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 10.0 };
      double[] EUR_FWD6_RATE = new double[] {0.04, 0.04, 0.04, 0.04, 0.04, 0.04, 0.04, 0.04 };
      String EUR_FWD6_NAME = "EURIBOR6M";
      YieldAndDiscountCurve EUR_FWD6 = new YieldCurve(EUR_FWD6_NAME, new InterpolatedDoublesCurve(EUR_FWD6_TIME, EUR_FWD6_RATE, LINEAR_FLAT, true, EUR_FWD6_NAME));
          
      IborIndex EURIBOR3M = MASTER_IBOR_INDEX.getIndex("EURIBOR3M");
      IborIndex EURIBOR6M = MASTER_IBOR_INDEX.getIndex("EURIBOR6M");
      
      MulticurveProviderDiscount curveContainer = new MulticurveProviderDiscount();
      curveContainer.setCurve(Currency.EUR, EUR_DSC);
      curveContainer.setCurve(EURIBOR3M, EUR_FWD3);
      curveContainer.setCurve(EURIBOR6M, EUR_FWD6);
      
      
      return curveContainer;    
    }
  }
