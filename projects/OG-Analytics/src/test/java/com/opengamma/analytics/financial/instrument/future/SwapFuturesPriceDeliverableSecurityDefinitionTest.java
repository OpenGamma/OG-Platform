/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableSecurity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the description of Deliverable Interest Rate Swap Futures as traded on CME.
 */
@Test(groups = TestGroup.UNIT)
public class SwapFuturesPriceDeliverableSecurityDefinitionTest {

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("USD6MLIBOR3M", NYC);
  private static final ZonedDateTime EFFECTIVE_DATE = DateUtils.getUTCDate(2012, 12, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(EFFECTIVE_DATE, -USD6MLIBOR3M.getSpotLag(), NYC);
  private static final Period TENOR = Period.ofYears(10);
  private static final double NOTIONAL = 100000;
  private static final double RATE = 0.0175;
  private static final SwapFixedIborDefinition SWAP_DEFINITION = SwapFixedIborDefinition.from(EFFECTIVE_DATE, TENOR, USD6MLIBOR3M, 1.0, RATE, false);
  private static final SwapFuturesPriceDeliverableSecurityDefinition SWAP_FUTURES_SECURITY_DEFINITION = new SwapFuturesPriceDeliverableSecurityDefinition(LAST_TRADING_DATE, SWAP_DEFINITION, NOTIONAL);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullLastTrading() {
    new SwapFuturesPriceDeliverableSecurityDefinition(null, SWAP_DEFINITION, NOTIONAL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSwap() {
    new SwapFuturesPriceDeliverableSecurityDefinition(LAST_TRADING_DATE, null, NOTIONAL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongSwap1() {
    new SwapFuturesPriceDeliverableSecurityDefinition(LAST_TRADING_DATE, SwapFixedIborDefinition.from(EFFECTIVE_DATE, TENOR, USD6MLIBOR3M, 2.0, RATE, false), NOTIONAL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongSwap2() {
    new SwapFuturesPriceDeliverableSecurityDefinition(LAST_TRADING_DATE, SwapFixedIborDefinition.from(EFFECTIVE_DATE, TENOR, USD6MLIBOR3M, 1.0, RATE, true), NOTIONAL);
  }

  @Test
  /**
   * Tests the getter methods.
   */
  public void getter() {
    assertEquals("DeliverableSwapFuturesSecurityDefinition: getter", LAST_TRADING_DATE, SWAP_FUTURES_SECURITY_DEFINITION.getLastTradingDate());
    assertEquals("DeliverableSwapFuturesSecurityDefinition: getter", EFFECTIVE_DATE, SWAP_FUTURES_SECURITY_DEFINITION.getDeliveryDate());
    assertEquals("DeliverableSwapFuturesSecurityDefinition: getter", SWAP_DEFINITION, SWAP_FUTURES_SECURITY_DEFINITION.getUnderlyingSwap());
    assertEquals("DeliverableSwapFuturesSecurityDefinition: getter", NOTIONAL, SWAP_FUTURES_SECURITY_DEFINITION.getNotional());
  }

  @Test
  /**
   * Tests the from builder.
   */
  public void from() {
    final SwapFuturesPriceDeliverableSecurityDefinition futuresDefinition = SwapFuturesPriceDeliverableSecurityDefinition.from(EFFECTIVE_DATE, USD6MLIBOR3M, TENOR, NOTIONAL, RATE);
    assertEquals("DeliverableSwapFuturesSecurityDefinition: from", SWAP_FUTURES_SECURITY_DEFINITION, futuresDefinition);
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeDeprecated() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 9, 21);
    final String dscName = "USD Discounting";
    final String fwd3Name = "USD Forward 3M";
    final String[] curveNames = {dscName, fwd3Name };
    final SwapFixedCoupon<? extends Coupon> underlying = SWAP_DEFINITION.toDerivative(referenceDate, curveNames);
    final double expiryTime = TimeCalculator.getTimeBetween(referenceDate, LAST_TRADING_DATE);
    final double deliveryTime = TimeCalculator.getTimeBetween(referenceDate, EFFECTIVE_DATE);
    final SwapFuturesPriceDeliverableSecurity futuresExpected = new SwapFuturesPriceDeliverableSecurity(expiryTime, deliveryTime, underlying, NOTIONAL);
    final SwapFuturesPriceDeliverableSecurity futuresConverted = SWAP_FUTURES_SECURITY_DEFINITION.toDerivative(referenceDate, curveNames);
    assertEquals("DeliverableSwapFuturesSecurityDefinition: toDerivative", futuresExpected, futuresConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivative() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 9, 21);
    final SwapFixedCoupon<? extends Coupon> underlying = SWAP_DEFINITION.toDerivative(referenceDate);
    final double expiryTime = TimeCalculator.getTimeBetween(referenceDate, LAST_TRADING_DATE);
    final double deliveryTime = TimeCalculator.getTimeBetween(referenceDate, EFFECTIVE_DATE);
    final SwapFuturesPriceDeliverableSecurity futuresExpected = new SwapFuturesPriceDeliverableSecurity(expiryTime, deliveryTime, underlying, NOTIONAL);
    final SwapFuturesPriceDeliverableSecurity futuresConverted = SWAP_FUTURES_SECURITY_DEFINITION.toDerivative(referenceDate);
    assertEquals("DeliverableSwapFuturesSecurityDefinition: toDerivative", futuresExpected, futuresConverted);
  }
}
