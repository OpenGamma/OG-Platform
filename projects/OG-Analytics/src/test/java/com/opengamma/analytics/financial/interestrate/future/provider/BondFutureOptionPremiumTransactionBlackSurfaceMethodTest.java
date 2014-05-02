/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureOptionPremiumTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.payments.provider.PaymentFixedDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.BlackDataSets;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesSmilePriceProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesSmileProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing methods for bond future options transaction with up-front premium payment.
 */
@Test(groups = TestGroup.UNIT)
public class BondFutureOptionPremiumTransactionBlackSurfaceMethodTest {

  private final static IssuerProviderDiscount ISSUER_MULTICURVES = IssuerProviderDiscountDataSets.getIssuerSpecificProvider();
  private final static String ISSUER_NAME = IssuerProviderDiscountDataSets.getIssuerNames()[0]; // US GOVT

  private static final InterpolatedDoublesSurface BLACK_PARAMETERS = BlackDataSets.createBlackSurfaceExpiryTenor();

  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final int SETTLEMENT_DAYS = 1;
  private static final int NB_BOND = 7;
  private static final double[] CONVERSION_FACTOR = new double[] {.8317, .8565, .8493, .8516, .8540, .8417, .8292};
  private static final ZonedDateTime LAST_TRADING_DATE = DateUtils.getUTCDate(2011, 9, 30);
  private static final ZonedDateTime FIRST_NOTICE_DATE = DateUtils.getUTCDate(2011, 8, 31);
  private static final ZonedDateTime LAST_NOTICE_DATE = DateUtils.getUTCDate(2011, 10, 4);
  private static final double NOTIONAL = 100000;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 20);
  private static final BondFixedSecurityDefinition[] BASKET_DEFINITION = new BondFixedSecurityDefinition[NB_BOND];
  private static final Currency USD = Currency.USD;
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final DayCount DAY_COUNT = DayCounts.ACT_ACT_ICMA;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM = false;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final Period[] BOND_TENOR = new Period[] {Period.ofYears(5), Period.ofYears(5), Period.ofYears(5), Period.ofYears(8), Period.ofYears(5), Period.ofYears(5), Period.ofYears(5)};
  private static final ZonedDateTime[] START_ACCRUAL_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2010, 11, 30), DateUtils.getUTCDate(2010, 12, 31), DateUtils.getUTCDate(2011, 1, 31),
    DateUtils.getUTCDate(2008, 2, 29), DateUtils.getUTCDate(2011, 3, 31), DateUtils.getUTCDate(2011, 4, 30), DateUtils.getUTCDate(2011, 5, 31)};
  private static final double[] RATE = new double[] {0.01375, 0.02125, 0.0200, 0.02125, 0.0225, 0.0200, 0.0175};
  private static final ZonedDateTime[] MATURITY_DATE = new ZonedDateTime[NB_BOND];
  static {
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      MATURITY_DATE[loopbasket] = START_ACCRUAL_DATE[loopbasket].plus(BOND_TENOR[loopbasket]);
      BASKET_DEFINITION[loopbasket] = BondFixedSecurityDefinition.from(USD, MATURITY_DATE[loopbasket], START_ACCRUAL_DATE[loopbasket], PAYMENT_TENOR, RATE[loopbasket], SETTLEMENT_DAYS, CALENDAR,
          DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM, ISSUER_NAME);
    }
  }
  private static final BondFutureDefinition BOND_FUT_DEFINITION = new BondFutureDefinition(LAST_TRADING_DATE, FIRST_NOTICE_DATE, LAST_NOTICE_DATE, NOTIONAL, BASKET_DEFINITION, CONVERSION_FACTOR);
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2011, 8, 26);
  private static final double STRIKE = 1.04;
  private static final BondFutureOptionPremiumSecurityDefinition BOND_FUTURE_OPTION_SEC_CALL_DEFINITION = new BondFutureOptionPremiumSecurityDefinition(BOND_FUT_DEFINITION, EXPIRY_DATE, STRIKE, true);
  private static final BondFutureOptionPremiumSecurityDefinition BOND_FUTURE_OPTION_SEC_PUT_DEFINITION = new BondFutureOptionPremiumSecurityDefinition(BOND_FUT_DEFINITION, EXPIRY_DATE, STRIKE, false);
  private static final int QUANTITY = 1234;
  private static final ZonedDateTime PREMIUM_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, 1, CALENDAR);
  private static final double PREMIUM_UNIT_CALL = 10.5;
  private static final double PREMIUM_UNIT_PUT = 15.6;
  private static final BondFutureOptionPremiumTransactionDefinition BOND_FUTURE_OPTION_TRA_CALL_DEFINITION = new BondFutureOptionPremiumTransactionDefinition(BOND_FUTURE_OPTION_SEC_CALL_DEFINITION,
      QUANTITY, PREMIUM_DATE, -QUANTITY * PREMIUM_UNIT_CALL);
  private static final BondFutureOptionPremiumTransactionDefinition BOND_FUTURE_OPTION_TRA_PUT_DEFINITION = new BondFutureOptionPremiumTransactionDefinition(BOND_FUTURE_OPTION_SEC_PUT_DEFINITION,
      QUANTITY, PREMIUM_DATE, -QUANTITY * PREMIUM_UNIT_PUT);

  private static final BondFuture BOND_FUT = BOND_FUT_DEFINITION.toDerivative(REFERENCE_DATE, 0.0);
  private static final BondFutureOptionPremiumSecurity BOND_FUTURE_OPTION_SEC_CALL = BOND_FUTURE_OPTION_SEC_CALL_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final BondFutureOptionPremiumTransaction BOND_FUTURE_OPTION_TRA_CALL = BOND_FUTURE_OPTION_TRA_CALL_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final BondFutureOptionPremiumTransaction BOND_FUTURE_OPTION_TRA_PUT = BOND_FUTURE_OPTION_TRA_PUT_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final double PRICE_FUTURES = 1.0325;
  private static final BlackBondFuturesSmileProvider BLACK_MULTICURVES = new BlackBondFuturesSmileProvider(ISSUER_MULTICURVES, BLACK_PARAMETERS);
  private static final BlackBondFuturesSmilePriceProvider BLACK_PRICE_MULTICURVES = new BlackBondFuturesSmilePriceProvider(BLACK_MULTICURVES, PRICE_FUTURES);

  private static final BondFutureDiscountingMethod METHOD_FUTURES = BondFutureDiscountingMethod.getInstance();
  private static final PaymentFixedDiscountingMethod METHOD_PAY_FIXED = PaymentFixedDiscountingMethod.getInstance();
  private static final BondFutureOptionPremiumSecurityBlackSmileMethod METHOD_BLACK_SEC = BondFutureOptionPremiumSecurityBlackSmileMethod.getInstance();
  private static final BondFutureOptionPremiumTransactionBlackSmileMethod METHOD_BLACK_TRA = BondFutureOptionPremiumTransactionBlackSmileMethod.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;

  private static final double TOLERANCE_PV_SENSI = 1.0E+0;

  @Test
  public void presentValueFromCurves() {
    final MultipleCurrencyAmount pvCallComputed = METHOD_BLACK_TRA.presentValue(BOND_FUTURE_OPTION_TRA_CALL, BLACK_MULTICURVES);
    final double priceCall = METHOD_BLACK_SEC.price(BOND_FUTURE_OPTION_SEC_CALL, BLACK_MULTICURVES);
    final MultipleCurrencyAmount pvCallPremium = METHOD_PAY_FIXED.presentValue(BOND_FUTURE_OPTION_TRA_CALL.getPremium(), ISSUER_MULTICURVES.getMulticurveProvider());
    assertEquals("BondFutureOptionPremiumTransactionBlackSurfaceMethod: present vlaue", pvCallComputed.getAmount(USD), pvCallPremium.getAmount(USD) + priceCall * QUANTITY * NOTIONAL, TOLERANCE_PV);
    final MultipleCurrencyAmount pvPutComputed = METHOD_BLACK_TRA.presentValue(BOND_FUTURE_OPTION_TRA_PUT, BLACK_MULTICURVES);
    final MultipleCurrencyAmount pvPutPremium = METHOD_PAY_FIXED.presentValue(BOND_FUTURE_OPTION_TRA_PUT.getPremium(), ISSUER_MULTICURVES.getMulticurveProvider());
    final MultipleCurrencyAmount pvFut = METHOD_FUTURES.presentValue(BOND_FUT, ISSUER_MULTICURVES);
    assertEquals("BondFutureOptionPremiumSecurityBlackSurfaceMethod: option price from future price", pvFut.getAmount(USD) * QUANTITY - STRIKE * NOTIONAL * QUANTITY, pvCallComputed.getAmount(USD)
        - pvCallPremium.getAmount(USD) - (pvPutComputed.getAmount(USD) - pvPutPremium.getAmount(USD)), TOLERANCE_PV);
  }

  @Test
  public void presentValueFromFuturesPrice() {
    final MultipleCurrencyAmount pvComputed = METHOD_BLACK_TRA.presentValue(BOND_FUTURE_OPTION_TRA_CALL, BLACK_PRICE_MULTICURVES);
    final double priceCall = METHOD_BLACK_SEC.price(BOND_FUTURE_OPTION_SEC_CALL, BLACK_PRICE_MULTICURVES);
    final MultipleCurrencyAmount pvPremium = METHOD_PAY_FIXED.presentValue(BOND_FUTURE_OPTION_TRA_CALL.getPremium(), ISSUER_MULTICURVES.getMulticurveProvider());
    assertEquals("BondFutureOptionPremiumTransactionBlackSurfaceMethod: present value", pvComputed.getAmount(USD), pvPremium.getAmount(USD) + priceCall * QUANTITY * NOTIONAL, TOLERANCE_PV);
    final double priceFutures = METHOD_FUTURES.price(BOND_FUT, ISSUER_MULTICURVES);
    final MultipleCurrencyAmount pv2 = METHOD_BLACK_TRA.presentValue(BOND_FUTURE_OPTION_TRA_CALL, new BlackBondFuturesSmilePriceProvider(BLACK_MULTICURVES, priceFutures));
    final MultipleCurrencyAmount pv3 = METHOD_BLACK_TRA.presentValue(BOND_FUTURE_OPTION_TRA_CALL, BLACK_MULTICURVES);
    assertEquals("BondFutureOptionPremiumTransactionBlackSurfaceMethod: present value", pv2.getAmount(USD), pv3.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  public void presentValueCurveSensitivityFromCurves() {
    final MultipleCurrencyMulticurveSensitivity pvcsCallComputed = METHOD_BLACK_TRA.presentValueCurveSensitivity(BOND_FUTURE_OPTION_TRA_CALL, BLACK_MULTICURVES).cleaned();
    final MulticurveSensitivity pcsCall = METHOD_BLACK_SEC.priceCurveSensitivity(BOND_FUTURE_OPTION_SEC_CALL, BLACK_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCallPremium = METHOD_PAY_FIXED.presentValueCurveSensitivity(BOND_FUTURE_OPTION_TRA_CALL.getPremium(), ISSUER_MULTICURVES.getMulticurveProvider());
    final MultipleCurrencyMulticurveSensitivity pvcsCallExpected = pvcsCallPremium.plus(MultipleCurrencyMulticurveSensitivity.of(USD, pcsCall.multipliedBy(NOTIONAL * QUANTITY))).cleaned();
    AssertSensitivityObjects.assertEquals("BondFutureOptionPremiumTransactionBlackSurfaceMethod: present value curve sensitivity", pvcsCallExpected, pvcsCallComputed, TOLERANCE_PV_SENSI);
    final MultipleCurrencyMulticurveSensitivity pvcsPutComputed = METHOD_BLACK_TRA.presentValueCurveSensitivity(BOND_FUTURE_OPTION_TRA_PUT, BLACK_MULTICURVES).cleaned();
    final MultipleCurrencyMulticurveSensitivity pvcsPutPremium = METHOD_PAY_FIXED.presentValueCurveSensitivity(BOND_FUTURE_OPTION_TRA_PUT.getPremium(), ISSUER_MULTICURVES.getMulticurveProvider());
    final MultipleCurrencyMulticurveSensitivity pvcsFutQu = METHOD_FUTURES.presentValueCurveSensitivity(BOND_FUT, ISSUER_MULTICURVES).multipliedBy(QUANTITY).cleaned(TOLERANCE_PV_SENSI);
    final MultipleCurrencyMulticurveSensitivity pvcsCallPut = pvcsCallComputed.plus(pvcsCallPremium.multipliedBy(-1)).plus(pvcsPutPremium.plus(pvcsPutComputed.multipliedBy(-1)))
        .cleaned(TOLERANCE_PV_SENSI);
    AssertSensitivityObjects.assertEquals("BondFutureOptionPremiumTransactionBlackSurfaceMethod: present value curve sensitivity", pvcsFutQu, pvcsCallPut, TOLERANCE_PV_SENSI);
  }

  @Test
  public void presentValueCurveSensitivityFromFuturesPrice() {
    final MultipleCurrencyMulticurveSensitivity pvcsCallComputed = METHOD_BLACK_TRA.presentValueCurveSensitivity(BOND_FUTURE_OPTION_TRA_CALL, BLACK_PRICE_MULTICURVES).cleaned();
    final MulticurveSensitivity pcsCall = METHOD_BLACK_SEC.priceCurveSensitivity(BOND_FUTURE_OPTION_SEC_CALL, BLACK_PRICE_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCallPremium = METHOD_PAY_FIXED.presentValueCurveSensitivity(BOND_FUTURE_OPTION_TRA_CALL.getPremium(), ISSUER_MULTICURVES.getMulticurveProvider());
    final MultipleCurrencyMulticurveSensitivity pvcsCallExpected = pvcsCallPremium.plus(MultipleCurrencyMulticurveSensitivity.of(USD, pcsCall.multipliedBy(NOTIONAL * QUANTITY))).cleaned();
    AssertSensitivityObjects.assertEquals("BondFutureOptionPremiumTransactionBlackSurfaceMethod: present value curve sensitivity", pvcsCallExpected, pvcsCallComputed, TOLERANCE_PV_SENSI);
    final MultipleCurrencyMulticurveSensitivity pvcsPutComputed = METHOD_BLACK_TRA.presentValueCurveSensitivity(BOND_FUTURE_OPTION_TRA_PUT, BLACK_PRICE_MULTICURVES).cleaned();
    final MultipleCurrencyMulticurveSensitivity pvcsPutPremium = METHOD_PAY_FIXED.presentValueCurveSensitivity(BOND_FUTURE_OPTION_TRA_PUT.getPremium(), ISSUER_MULTICURVES.getMulticurveProvider());
    final MultipleCurrencyMulticurveSensitivity pvcsFutQu = METHOD_FUTURES.presentValueCurveSensitivity(BOND_FUT, ISSUER_MULTICURVES).multipliedBy(QUANTITY).cleaned(TOLERANCE_PV_SENSI);
    final MultipleCurrencyMulticurveSensitivity pvcsCallPut = pvcsCallComputed.plus(pvcsCallPremium.multipliedBy(-1)).plus(pvcsPutPremium.plus(pvcsPutComputed.multipliedBy(-1)))
        .cleaned(TOLERANCE_PV_SENSI);
    AssertSensitivityObjects.assertEquals("BondFutureOptionPremiumTransactionBlackSurfaceMethod: present value curve sensitivity", pvcsFutQu, pvcsCallPut, TOLERANCE_PV_SENSI);
  }

}
