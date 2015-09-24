package com.opengamma.analytics.financial.interestrate.swap.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageIndexDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverage;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

public class CouponForwardRateVisitorTest {

  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2015, 9, 23);
  private static final MulticurveProviderDiscount MULTICURVE = 
      MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex[] INDEX_LIST = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = INDEX_LIST[0];
  private static final IborIndex EURIBOR6M = INDEX_LIST[1];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();
  

  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2015, 12, 14);
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2015, 12, 16);
  private static final ZonedDateTime FIXING_PERIOD_END_3M = 
      ScheduleCalculator.getAdjustedDate(START_DATE, EURIBOR3M, CALENDAR);
  private static final ZonedDateTime FIXING_PERIOD_END_6M = 
      ScheduleCalculator.getAdjustedDate(START_DATE, EURIBOR6M, CALENDAR);
  private static final ZonedDateTime END_DATE_STUB = DateUtils.getUTCDate(2016, 4, 28);
  private static final double NOTIONAL = 100000000.0; // 100m
  private static final double PAYMENT_AF = 0.40;
  private static final double WEIGHT_3M = 
      ((double) (FIXING_PERIOD_END_6M.toLocalDate().toEpochDay() - END_DATE_STUB.toLocalDate().toEpochDay()))
      / (FIXING_PERIOD_END_6M.toLocalDate().toEpochDay() - FIXING_PERIOD_END_3M.toLocalDate().toEpochDay());
  private static final double WEIGHT_6M = 
      ((double) (END_DATE_STUB.toLocalDate().toEpochDay() - FIXING_PERIOD_END_3M.toLocalDate().toEpochDay()))
      / (FIXING_PERIOD_END_6M.toLocalDate().toEpochDay() - FIXING_PERIOD_END_3M.toLocalDate().toEpochDay());
  private static final CouponIborAverageIndexDefinition CPN_STUB_DEFINITION = 
      new CouponIborAverageIndexDefinition(EUR, END_DATE_STUB, START_DATE, END_DATE_STUB, PAYMENT_AF, NOTIONAL, 
          FIXING_DATE, EURIBOR3M, EURIBOR6M, WEIGHT_3M, WEIGHT_6M, CALENDAR, CALENDAR);
  private static final CouponIborAverage CPN_STUB = (CouponIborAverage) CPN_STUB_DEFINITION.toDerivative(VALUATION_DATE);
  
  private static final CouponForwardRateVisitor FWD_CALCULATOR = new CouponForwardRateVisitor();
  
  private static final double TOLERANCE_RATE = 1.0E-8;
  
  @Test
  public void forward_ibor_average_index() {
    double fwdComputed = CPN_STUB.accept(FWD_CALCULATOR, MULTICURVE);
    double startTime = TimeCalculator.getTimeBetween(VALUATION_DATE, START_DATE);
    double endTime3M = TimeCalculator.getTimeBetween(VALUATION_DATE, FIXING_PERIOD_END_3M);
    double endTime6M = TimeCalculator.getTimeBetween(VALUATION_DATE, FIXING_PERIOD_END_6M);
    double af3M = EURIBOR3M.getDayCount().getDayCountFraction(START_DATE, FIXING_PERIOD_END_3M);
    double af6M = EURIBOR6M.getDayCount().getDayCountFraction(START_DATE, FIXING_PERIOD_END_6M);
    double fwd3M = MULTICURVE.getSimplyCompoundForwardRate(EURIBOR3M, startTime, endTime3M, af3M);
    double fwd6M = MULTICURVE.getSimplyCompoundForwardRate(EURIBOR6M, startTime, endTime6M, af6M);
    double fwdExpected = fwd3M * WEIGHT_3M + fwd6M * WEIGHT_6M;
    assertEquals(fwdExpected, fwdComputed, TOLERANCE_RATE);
  }
}
