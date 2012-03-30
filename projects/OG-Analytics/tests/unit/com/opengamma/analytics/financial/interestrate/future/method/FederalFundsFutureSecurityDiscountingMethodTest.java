/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.indexon.FEDFUND;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.method.FederalFundsFutureSecurityDiscountingMethod;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

public class FederalFundsFutureSecurityDiscountingMethodTest {

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 1, 30);

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexON INDEX_FEDFUND = new FEDFUND(NYC);
  private static final ZonedDateTime MARCH_1 = DateUtils.getUTCDate(2012, 3, 1);

  private static final FederalFundsFutureSecurityDefinition FUTURE_SECURITY_DEFINITION = FederalFundsFutureSecurityDefinition.fromFedFund(MARCH_1, INDEX_FEDFUND);

  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves2();
  private static final String[] CURVE_NAMES = TestsDataSetsSABR.curves2Names();

  private static final FederalFundsFutureSecurity FUTURE_SECURITY = FUTURE_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAMES);

  private static final FederalFundsFutureSecurityDiscountingMethod METHOD_SECURITY = FederalFundsFutureSecurityDiscountingMethod.getInstance();

  private static final double TOLERANCE_PRICE = 1.0E-6;

  @Test
  public void priceBeforeFixing() {
    double interest = 0.0;
    YieldAndDiscountCurve oisCurve = CURVES.getCurve(CURVE_NAMES[0]);
    double[] ratePeriod = new double[FUTURE_SECURITY_DEFINITION.getFixingPeriodAccrualFactor().length];
    for (int loopfix = 0; loopfix < FUTURE_SECURITY_DEFINITION.getFixingPeriodAccrualFactor().length; loopfix++) {
      ratePeriod[loopfix] = (oisCurve.getDiscountFactor(FUTURE_SECURITY.getFixingPeriodTime()[loopfix]) / oisCurve.getDiscountFactor(FUTURE_SECURITY.getFixingPeriodTime()[loopfix + 1]) - 1.0)
          / FUTURE_SECURITY_DEFINITION.getFixingPeriodAccrualFactor()[loopfix];
      interest += (oisCurve.getDiscountFactor(FUTURE_SECURITY.getFixingPeriodTime()[loopfix]) / oisCurve.getDiscountFactor(FUTURE_SECURITY.getFixingPeriodTime()[loopfix + 1]) - 1.0);
    }
    double rate = interest / FUTURE_SECURITY_DEFINITION.getFixingTotalAccrualFactor();
    double priceExpected = 1.0 - rate;
    double priceComputed = METHOD_SECURITY.price(FUTURE_SECURITY, CURVES);
    assertEquals("Federal Funds Future Security: price", priceExpected, priceComputed, TOLERANCE_PRICE);
  }

  @Test
  public void priceAfterFixing() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 3, 7);
    ZonedDateTime[] dateFixing = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 3, 1), DateUtils.getUTCDate(2012, 3, 2), DateUtils.getUTCDate(2012, 3, 5), DateUtils.getUTCDate(2012, 3, 6),
        DateUtils.getUTCDate(2012, 3, 7)};
    double[] rateFixing = new double[] {0.0010, 0.0011, 0.0012, 0.0013, 0.0014};
    DoubleTimeSeries<ZonedDateTime> fixingTS = new ArrayZonedDateTimeDoubleTimeSeries(dateFixing, rateFixing);
    FederalFundsFutureSecurity futureSecurity = FUTURE_SECURITY_DEFINITION.toDerivative(referenceDate, fixingTS, CURVE_NAMES);
    double interest = futureSecurity.getAccruedInterest();
    YieldAndDiscountCurve oisCurve = CURVES.getCurve(CURVE_NAMES[0]);
    double[] ratePeriod = new double[futureSecurity.getFixingPeriodAccrualFactor().length];
    for (int loopfix = 0; loopfix < futureSecurity.getFixingPeriodAccrualFactor().length; loopfix++) {
      ratePeriod[loopfix] = (oisCurve.getDiscountFactor(futureSecurity.getFixingPeriodTime()[loopfix]) / oisCurve.getDiscountFactor(futureSecurity.getFixingPeriodTime()[loopfix + 1]) - 1.0)
          / futureSecurity.getFixingPeriodAccrualFactor()[loopfix];
      interest += (oisCurve.getDiscountFactor(futureSecurity.getFixingPeriodTime()[loopfix]) / oisCurve.getDiscountFactor(futureSecurity.getFixingPeriodTime()[loopfix + 1]) - 1.0);
    }
    double rate = interest / FUTURE_SECURITY_DEFINITION.getFixingTotalAccrualFactor();
    double priceExpected = 1.0 - rate;
    double priceComputed = METHOD_SECURITY.price(futureSecurity, CURVES);
    assertEquals("Federal Funds Future Security: price", priceExpected, priceComputed, TOLERANCE_PRICE);
  }

}
