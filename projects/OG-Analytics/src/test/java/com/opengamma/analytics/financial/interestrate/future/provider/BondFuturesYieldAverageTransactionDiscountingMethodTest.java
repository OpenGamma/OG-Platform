/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesYieldAverageSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesYieldAverageTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesYieldAverageSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesYieldAverageTransaction;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing of Yield average bond futures transaction (in particular for AUD-SFE futures) with discounting method, i.e. without convexity adjustments.
 */
@Test(groups = TestGroup.UNIT)
public class BondFuturesYieldAverageTransactionDiscountingMethodTest {

  // Bonds: Delivery basket SFE 10Y
  private static final Currency AUD = Currency.AUD;
  // AUD defaults
  private static final LegalEntity ISSUER_LEGAL_ENTITY = IssuerProviderDiscountDataSets.getIssuersAUS();
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT = DayCounts.ACT_ACT_ICMA;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM = false;
  private static final int SETTLEMENT_DAYS = 3;
  private static final int EX_DIVIDEND_DAYS = 7;
  private static final YieldConvention YIELD_CONVENTION = SimpleYieldConvention.AUSTRALIA_EX_DIVIDEND;
  private static final double NOTIONAL_BOND = 100;
  private static final double NOTIONAL_FUTURES = 10000;

  private static final ZonedDateTime LAST_TRADING_DATE = DateUtils.getUTCDate(2014, 3, 17);
  // ASX 10 Year Bond Contract - March 14
  private static final double[] UNDERLYING_COUPON = {0.0575, 0.0550, 0.0275, 0.0325 };
  private static final ZonedDateTime[] UNDERLYING_MATURITY_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2022, 7, 15), DateUtils.getUTCDate(2023, 4, 15),
    DateUtils.getUTCDate(2024, 4, 15), DateUtils.getUTCDate(2025, 4, 15) };
  private static final int NB_BOND = UNDERLYING_COUPON.length;
  private static final ZonedDateTime[] START_ACCRUAL_DATE = new ZonedDateTime[NB_BOND];
  private static final BondFixedSecurityDefinition[] BASKET_SECURITY_DEFINITION = new BondFixedSecurityDefinition[NB_BOND];
  static {
    for (int loopbond = 0; loopbond < NB_BOND; loopbond++) {
      START_ACCRUAL_DATE[loopbond] = UNDERLYING_MATURITY_DATE[loopbond].minusYears(12);
      BASKET_SECURITY_DEFINITION[loopbond] = BondFixedSecurityDefinition.from(AUD, START_ACCRUAL_DATE[loopbond], UNDERLYING_MATURITY_DATE[loopbond], PAYMENT_TENOR,
          UNDERLYING_COUPON[loopbond], SETTLEMENT_DAYS, NOTIONAL_BOND, EX_DIVIDEND_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM, ISSUER_LEGAL_ENTITY, "Repo");
    }
  }
  private static final double SYNTHETIC_COUPON = 0.06;
  private static final int TENOR = 10;

  private static final BondFuturesYieldAverageSecurityDefinition FUT_SEC_DEFINITION = new BondFuturesYieldAverageSecurityDefinition(LAST_TRADING_DATE,
      BASKET_SECURITY_DEFINITION, SYNTHETIC_COUPON, TENOR, NOTIONAL_FUTURES);
  // Transation
  private static final int QUANTITY = 1234;
  private static final double TRADE_PRICE = 0.95;
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2014, 1, 10);
  private static final BondFuturesYieldAverageTransactionDefinition FUT_TRA_DEFINITION = new BondFuturesYieldAverageTransactionDefinition(FUT_SEC_DEFINITION,
      QUANTITY, TRADE_DATE, TRADE_PRICE);

  private static final double LAST_MARGIN_PRICE = 0.955;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2014, 1, 10);
  private static final BondFuturesYieldAverageSecurity FUT_SEC = FUT_SEC_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final BondFuturesYieldAverageTransaction FUT_TRA = FUT_TRA_DEFINITION.toDerivative(REFERENCE_DATE, LAST_MARGIN_PRICE);

  private static final IssuerProviderInterface ISSUER_MULTICURVE = IssuerProviderDiscountDataSets.getIssuerSpecificProviderAus();
  private static final FuturesSecurityIssuerMethod METHOD_FI_SEC = new FuturesSecurityIssuerMethod();
  private static final FuturesTransactionIssuerMethod METHOD_FI_TRA = new FuturesTransactionIssuerMethod();

  private static final double TOLERANCE_PV = 1.0E-2;

  @Test
  public void presentValueFromPrice() {
    final double quotedPrice = 0.96;
    final double marginIndexPrice = METHOD_FI_SEC.marginIndex(FUT_SEC, quotedPrice);
    final double marginIndexReference = METHOD_FI_SEC.marginIndex(FUT_SEC, TRADE_PRICE);
    final double pvExpected = (marginIndexPrice - marginIndexReference) * QUANTITY;
    final MultipleCurrencyAmount pvComputed = METHOD_FI_TRA.presentValueFromPrice(FUT_TRA, quotedPrice);
    assertEquals("YieldAverageBondFuturesTransactionDiscountingMethod: presentValueFromPrice", pvExpected, pvComputed.getAmount(AUD), TOLERANCE_PV);
  }

  @Test
  /** Tests present value from the curves **/
  public void presentValue() {
    final MultipleCurrencyAmount pvComputed = METHOD_FI_TRA.presentValue(FUT_TRA, ISSUER_MULTICURVE);
    final double priceFromCurves = METHOD_FI_SEC.price(FUT_SEC, ISSUER_MULTICURVE);
    final MultipleCurrencyAmount pvExpected = METHOD_FI_TRA.presentValueFromPrice(FUT_TRA, priceFromCurves);
    assertEquals("YieldAverageBondFuturesTransactionDiscountingMethod: presentValue", pvExpected.getAmount(AUD), pvComputed.getAmount(AUD), TOLERANCE_PV);
  }

}
