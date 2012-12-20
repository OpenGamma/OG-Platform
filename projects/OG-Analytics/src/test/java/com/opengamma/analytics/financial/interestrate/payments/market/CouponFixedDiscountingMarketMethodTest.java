package com.opengamma.analytics.financial.interestrate.payments.market;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.interestrate.market.calculator.PresentValueCurveSensitivityMarketCalculator;
import com.opengamma.analytics.financial.interestrate.market.calculator.PresentValueMarketCalculator;
import com.opengamma.analytics.financial.interestrate.market.description.MarketDiscountBundle;
import com.opengamma.analytics.financial.interestrate.market.description.MarketDiscountDataSets;
import com.opengamma.analytics.financial.interestrate.market.description.MultipleCurrencyCurveSensitivityMarket;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

public class CouponFixedDiscountingMarketMethodTest {

  private static final IborIndex[] IBOR_INDEXES = MarketDiscountDataSets.getIndexesIbor();
  private static final IborIndex EURIBOR3M = IBOR_INDEXES[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();

  private static final DayCount DAY_COUNT_COUPON = DayCountFactory.INSTANCE.getDayCount("Actual/365");
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 5, 23);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 8, 22);
  private static final double ACCRUAL_FACTOR = DAY_COUNT_COUPON.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 100000000; //1m
  private static final double FIXED_RATE = 0.02;
  private static final CouponFixedDefinition CPN_FIXED_DEFINITION = new CouponFixedDefinition(EUR, ACCRUAL_END_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27);
  private static final String[] NOT_USED = new String[] {"Not used 1", "not used 2"};
  private static final CouponFixed CPN_FIXED = CPN_FIXED_DEFINITION.toDerivative(REFERENCE_DATE, NOT_USED);

  private static final MarketDiscountBundle MARKET = MarketDiscountDataSets.createMarket1();

  private static final CouponFixedDiscountingMarketMethod METHOD_CPN_FIXED = CouponFixedDiscountingMarketMethod.getInstance();
  private static final PresentValueMarketCalculator PVC = PresentValueMarketCalculator.getInstance();
  private static final PresentValueCurveSensitivityMarketCalculator PVCSC = PresentValueCurveSensitivityMarketCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_DELTA = 1.0E+2;

  @Test
  public void presentValue() {
    MultipleCurrencyAmount pvComputed = METHOD_CPN_FIXED.presentValue(CPN_FIXED, MARKET);
    double df = MARKET.getDiscountFactor(EUR, CPN_FIXED.getPaymentTime());
    double pvExpected = CPN_FIXED.getAmount() * df;
    assertEquals("CouponFixedDiscountingMarketMethod: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  public void presentValueMethodVsCalculator() {
    MultipleCurrencyAmount pvMethod = METHOD_CPN_FIXED.presentValue(CPN_FIXED, MARKET);
    MultipleCurrencyAmount pvCalculator = PVC.visit(CPN_FIXED, MARKET);
    assertEquals("CouponFixedDiscountingMarketMethod: present value", pvMethod.getAmount(EUR), pvCalculator.getAmount(EUR), TOLERANCE_PV);
  }

  // Testing note: the presentValueMarketSensitivity is tested in ParameterSensitivityMarketCalculatorTest (Quant-Sandbox)

  @Test
  public void presentValueMarketSensitivityMethodVsCalculator() {
    MultipleCurrencyCurveSensitivityMarket pvcsMethod = METHOD_CPN_FIXED.presentValueMarketSensitivity(CPN_FIXED, MARKET);
    MultipleCurrencyCurveSensitivityMarket pvcsCalculator = PVCSC.visit(CPN_FIXED, MARKET);
    AssertSensivityObjects.assertEquals("CouponFixedDiscountingMarketMethod: presentValueMarketSensitivity", pvcsMethod, pvcsCalculator, TOLERANCE_DELTA);
  }

}
