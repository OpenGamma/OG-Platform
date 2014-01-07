/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.calculator.PresentValueMCACalculator;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureOptionPremiumTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityIRSCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsBlack;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeAndForwardBundle;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing methods for bond future options transaction with up-front premium payment.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class BondFutureOptionPremiumTransactionBlackSurfaceMethodTest {

  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final String ISSUER_NAME = "Issuer";
  private static final int SETTLEMENT_DAYS = 1;
  private static final int NB_BOND = 7;
  private static final double[] CONVERSION_FACTOR = new double[] {.8317, .8565, .8493, .8516, .8540, .8417, .8292 };
  private static final ZonedDateTime LAST_TRADING_DATE = DateUtils.getUTCDate(2011, 9, 30);
  private static final ZonedDateTime FIRST_NOTICE_DATE = DateUtils.getUTCDate(2011, 8, 31);
  private static final ZonedDateTime LAST_NOTICE_DATE = DateUtils.getUTCDate(2011, 10, 4);
  private static final double NOTIONAL = 100000;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 20);
  private static final String CREDIT_CURVE_NAME = "Credit";
  private static final String REPO_CURVE_NAME = "Repo";
  private static final String[] CURVES_NAME = {CREDIT_CURVE_NAME, REPO_CURVE_NAME };
  private static final YieldCurveWithBlackCubeBundle DATA = TestsDataSetsBlack.createCubesBondFutureOption();
  private static final BondFixedSecurityDefinition[] BASKET_DEFINITION = new BondFixedSecurityDefinition[NB_BOND];
  private static final Currency CUR = Currency.EUR;
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final DayCount DAY_COUNT = DayCounts.ACT_ACT_ICMA;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM = false;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final Period[] BOND_TENOR = new Period[] {Period.ofYears(5), Period.ofYears(5), Period.ofYears(5), Period.ofYears(8), Period.ofYears(5), Period.ofYears(5), Period.ofYears(5) };
  private static final ZonedDateTime[] START_ACCRUAL_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2010, 11, 30), DateUtils.getUTCDate(2010, 12, 31), DateUtils.getUTCDate(2011, 1, 31),
    DateUtils.getUTCDate(2008, 2, 29), DateUtils.getUTCDate(2011, 3, 31), DateUtils.getUTCDate(2011, 4, 30), DateUtils.getUTCDate(2011, 5, 31) };
  private static final double[] RATE = new double[] {0.01375, 0.02125, 0.0200, 0.02125, 0.0225, 0.0200, 0.0175 };
  private static final ZonedDateTime[] MATURITY_DATE = new ZonedDateTime[NB_BOND];
  static {
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      MATURITY_DATE[loopbasket] = START_ACCRUAL_DATE[loopbasket].plus(BOND_TENOR[loopbasket]);
      BASKET_DEFINITION[loopbasket] = BondFixedSecurityDefinition.from(CUR, MATURITY_DATE[loopbasket], START_ACCRUAL_DATE[loopbasket], PAYMENT_TENOR, RATE[loopbasket], SETTLEMENT_DAYS, CALENDAR,
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

  private static final BondFuture BOND_FUT = BOND_FUT_DEFINITION.toDerivative(REFERENCE_DATE, 0.0, CURVES_NAME);
  private static final BondFutureOptionPremiumSecurity BOND_FUTURE_OPTION_SEC_CALL = BOND_FUTURE_OPTION_SEC_CALL_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final BondFutureOptionPremiumTransaction BOND_FUTURE_OPTION_TRA_CALL = BOND_FUTURE_OPTION_TRA_CALL_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final BondFutureOptionPremiumTransaction BOND_FUTURE_OPTION_TRA_PUT = BOND_FUTURE_OPTION_TRA_PUT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);

  private static final double PRICE_FUTURE = 1.0325;
  private static final YieldCurveWithBlackCubeAndForwardBundle DATA_WITH_FUTURE = YieldCurveWithBlackCubeAndForwardBundle.from(DATA, PRICE_FUTURE);

  private static final BondFutureOptionPremiumSecurityBlackSurfaceMethod METHOD_OPTION_SECURITY = BondFutureOptionPremiumSecurityBlackSurfaceMethod.getInstance();
  private static final BondFutureOptionPremiumTransactionBlackSurfaceMethod METHOD_OPTION_TRANSACTION = BondFutureOptionPremiumTransactionBlackSurfaceMethod.getInstance();
  private static final BondFutureDiscountingMethod METHOD_FUTURES = BondFutureDiscountingMethod.getInstance();
  private static final PresentValueMCACalculator PVC = PresentValueMCACalculator.getInstance();
  private static final PresentValueCurveSensitivityIRSCalculator PVCSC = PresentValueCurveSensitivityIRSCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;

  private static final double TOLERANCE_PV_SENSI = 1.0E+0;

  @Test
  public void presentValueFromCurves() {
    final CurrencyAmount pvCallComputed = METHOD_OPTION_TRANSACTION.presentValue(BOND_FUTURE_OPTION_TRA_CALL, DATA);
    final double priceCall = METHOD_OPTION_SECURITY.optionPrice(BOND_FUTURE_OPTION_SEC_CALL, DATA);
    final MultipleCurrencyAmount pvCallPremium = BOND_FUTURE_OPTION_TRA_CALL.getPremium().accept(PVC, DATA);
    assertEquals("BondFutureOptionPremiumTransactionBlackSurfaceMethod: present vlaue", pvCallComputed.getAmount(), pvCallPremium.getAmount(CUR) + priceCall * QUANTITY * NOTIONAL, TOLERANCE_PV);
    final CurrencyAmount pvPutComputed = METHOD_OPTION_TRANSACTION.presentValue(BOND_FUTURE_OPTION_TRA_PUT, DATA);
    final MultipleCurrencyAmount pvPutPremium = BOND_FUTURE_OPTION_TRA_PUT.getPremium().accept(PVC, DATA);
    final CurrencyAmount pvFut = METHOD_FUTURES.presentValue(BOND_FUT, DATA);
    assertEquals("BondFutureOptionPremiumSecurityBlackSurfaceMethod: option price from future price", pvFut.getAmount() * QUANTITY - STRIKE * NOTIONAL * QUANTITY, pvCallComputed.getAmount()
        - pvCallPremium.getAmount(CUR) - (pvPutComputed.getAmount() - pvPutPremium.getAmount(CUR)), TOLERANCE_PV);
  }

  @Test
  public void presentValueFromFuturesPrice() {
    final CurrencyAmount pvComputed = METHOD_OPTION_TRANSACTION.presentValue(BOND_FUTURE_OPTION_TRA_CALL, DATA_WITH_FUTURE);
    final double priceCall = METHOD_OPTION_SECURITY.optionPrice(BOND_FUTURE_OPTION_SEC_CALL, DATA_WITH_FUTURE);
    final MultipleCurrencyAmount pvPremium = BOND_FUTURE_OPTION_TRA_CALL.getPremium().accept(PVC, DATA);
    assertEquals("BondFutureOptionPremiumTransactionBlackSurfaceMethod: present value", pvComputed.getAmount(), pvPremium.getAmount(CUR) + priceCall * QUANTITY * NOTIONAL, TOLERANCE_PV);
    final double priceFutures = METHOD_FUTURES.price(BOND_FUT, DATA);
    final CurrencyAmount pv2 = METHOD_OPTION_TRANSACTION.presentValue(BOND_FUTURE_OPTION_TRA_CALL, YieldCurveWithBlackCubeAndForwardBundle.from(DATA, priceFutures));
    final CurrencyAmount pv3 = METHOD_OPTION_TRANSACTION.presentValue(BOND_FUTURE_OPTION_TRA_CALL, DATA);
    assertEquals("BondFutureOptionPremiumTransactionBlackSurfaceMethod: present value", pv2.getAmount(), pv3.getAmount(), TOLERANCE_PV);
  }

  @Test
  public void presentValueCurveSensitivityFromCurves() {
    final InterestRateCurveSensitivity pvcsCallComputed = METHOD_OPTION_TRANSACTION.presentValueCurveSensitivity(BOND_FUTURE_OPTION_TRA_CALL, DATA).cleaned();
    final InterestRateCurveSensitivity pcsCall = METHOD_OPTION_SECURITY.priceCurveSensitivity(BOND_FUTURE_OPTION_SEC_CALL, DATA);
    final InterestRateCurveSensitivity pvcsCallPremium = PVCSC.visit(BOND_FUTURE_OPTION_TRA_CALL.getPremium(), DATA);
    final InterestRateCurveSensitivity pvcsCallExpected = pvcsCallPremium.plus(pcsCall.multipliedBy(NOTIONAL * QUANTITY)).cleaned();
    AssertSensivityObjects.assertEquals("BondFutureOptionPremiumTransactionBlackSurfaceMethod: present value curve sensitivity", pvcsCallExpected, pvcsCallComputed, TOLERANCE_PV_SENSI);
    final InterestRateCurveSensitivity pvcsPutComputed = METHOD_OPTION_TRANSACTION.presentValueCurveSensitivity(BOND_FUTURE_OPTION_TRA_PUT, DATA).cleaned();
    final InterestRateCurveSensitivity pvcsPutPremium = PVCSC.visit(BOND_FUTURE_OPTION_TRA_PUT.getPremium(), DATA);
    final InterestRateCurveSensitivity pvcsFutQu = METHOD_FUTURES.presentValueCurveSensitivity(BOND_FUT, DATA).multipliedBy(QUANTITY).cleaned(0.0, TOLERANCE_PV_SENSI);
    final InterestRateCurveSensitivity pvcsCallPut = pvcsCallComputed.plus(pvcsCallPremium.multipliedBy(-1)).plus(pvcsPutPremium.plus(pvcsPutComputed.multipliedBy(-1)))
        .cleaned(0.0, TOLERANCE_PV_SENSI);
    AssertSensivityObjects.assertEquals("BondFutureOptionPremiumTransactionBlackSurfaceMethod: present value curve sensitivity", pvcsFutQu, pvcsCallPut, TOLERANCE_PV_SENSI);
  }

  @Test
  public void presentValueCurveSensitivityFromFuturesPrice() {
    final InterestRateCurveSensitivity pvcsCallComputed = METHOD_OPTION_TRANSACTION.presentValueCurveSensitivity(BOND_FUTURE_OPTION_TRA_CALL, DATA_WITH_FUTURE).cleaned();
    final InterestRateCurveSensitivity pcsCall = METHOD_OPTION_SECURITY.priceCurveSensitivity(BOND_FUTURE_OPTION_SEC_CALL, DATA_WITH_FUTURE);
    final InterestRateCurveSensitivity pvcsCallPremium = PVCSC.visit(BOND_FUTURE_OPTION_TRA_CALL.getPremium(), DATA_WITH_FUTURE);
    final InterestRateCurveSensitivity pvcsCallExpected = pvcsCallPremium.plus(pcsCall.multipliedBy(NOTIONAL * QUANTITY)).cleaned();
    AssertSensivityObjects.assertEquals("BondFutureOptionPremiumTransactionBlackSurfaceMethod: present value curve sensitivity", pvcsCallExpected, pvcsCallComputed, TOLERANCE_PV_SENSI);
    final InterestRateCurveSensitivity pvcsPutComputed = METHOD_OPTION_TRANSACTION.presentValueCurveSensitivity(BOND_FUTURE_OPTION_TRA_PUT, DATA_WITH_FUTURE).cleaned();
    final InterestRateCurveSensitivity pvcsPutPremium = PVCSC.visit(BOND_FUTURE_OPTION_TRA_PUT.getPremium(), DATA_WITH_FUTURE);
    final InterestRateCurveSensitivity pvcsFutQu = METHOD_FUTURES.presentValueCurveSensitivity(BOND_FUT, DATA_WITH_FUTURE).multipliedBy(QUANTITY).cleaned(0.0, TOLERANCE_PV_SENSI);
    final InterestRateCurveSensitivity pvcsCallPut = pvcsCallComputed.plus(pvcsCallPremium.multipliedBy(-1)).plus(pvcsPutPremium.plus(pvcsPutComputed.multipliedBy(-1)))
        .cleaned(0.0, TOLERANCE_PV_SENSI);
    AssertSensivityObjects.assertEquals("BondFutureOptionPremiumTransactionBlackSurfaceMethod: present value curve sensitivity", pvcsFutQu, pvcsCallPut, TOLERANCE_PV_SENSI);
  }

}
