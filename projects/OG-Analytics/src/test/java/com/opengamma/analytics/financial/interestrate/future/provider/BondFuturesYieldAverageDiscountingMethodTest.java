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
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesYieldAverageSecurity;
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
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing of Yield average bond futures (in particular for AUD-SFE futures) with discounting method, i.e. without convexity adjustments.
 */
@Test(groups = TestGroup.UNIT)
public class BondFuturesYieldAverageDiscountingMethodTest {

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
  //  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2014, 1, 10);
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

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2014, 1, 10);
  private static final BondFuturesYieldAverageSecurity FUT_SEC = FUT_SEC_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final IssuerProviderInterface ISSUER_MULTICURVE = IssuerProviderDiscountDataSets.getIssuerSpecificProviderAus();
  private static final FuturesSecurityIssuerMethod METHOD_FUTI_SEC = new FuturesSecurityIssuerMethod();
  private static final BondSecurityDiscountingMethod METHOD_BOND = BondSecurityDiscountingMethod.getInstance();

  private static final double TOLERANCE_INDEX = 1.0E-5;
  private static final double TOLERANCE_PRICE = 1.0E-8;

  @Test
  /**
   * Tests the margin index (i.e. the figure used to compute the margin on one futures) from the quoted price.
   */
  public void marginIndex() {
    final double quotedPrice = 0.95;
    final double yield = 1.0d - quotedPrice;
    final double theoreticalPriceFromYield = dirtyPriceFromYield(yield, FUT_SEC.getCouponRate(), FUT_SEC.getTenor(), FUT_SEC.getNumberCouponPerYear());
    final double marginIndexExpected = theoreticalPriceFromYield * FUT_SEC.getNotional();
    final double marginIndexComputed = METHOD_FUTI_SEC.marginIndex(FUT_SEC, quotedPrice);
    assertEquals("YieldAverageBondFuturesDiscountingMethod: margin index", marginIndexExpected, marginIndexComputed, TOLERANCE_INDEX);
  }

  @Test
  /**
   * Tests the bond futures price from average yield from curves.
   */
  public void price() {
    final double priceComputed = METHOD_FUTI_SEC.price(FUT_SEC, ISSUER_MULTICURVE);
    final double[] yieldsAtDelivery = new double[NB_BOND];
    double yieldAverage = 0.0;
    for (int loopbond = 0; loopbond < NB_BOND; loopbond++) {
      yieldsAtDelivery[loopbond] = METHOD_BOND.yieldFromCurves(FUT_SEC.getDeliveryBasketAtDeliveryDate()[loopbond], ISSUER_MULTICURVE);
      yieldAverage += yieldsAtDelivery[loopbond];
    }
    yieldAverage /= NB_BOND;
    final double priceExpected = 1.0d - yieldAverage;
    ;
    assertEquals("YieldAverageBondFuturesDiscountingMethod: price", priceExpected, priceComputed, TOLERANCE_PRICE);
  }

  /**
   * Intermediary function to compute the dirty price from yield for an integer number of years.
   * The code is not optimized (compute each term of the series instead of its explicit sum).
   * @param yield The yield.
   * @param coupon The bond coupon.
   * @param tenor The bond tenor (in years).
   * @param couponPerYear The number of coupon per year.
   * @return The price.
   */
  private double dirtyPriceFromYield(final double yield, final double coupon, final int tenor, final int couponPerYear) {
    final double v = 1.0d + yield / couponPerYear;
    final int n = tenor * couponPerYear;
    double price = 0.0d;
    for (int loopcpn = 1; loopcpn <= n; loopcpn++) {
      price += coupon / couponPerYear / Math.pow(v, loopcpn);
    }
    price += 1.0d / Math.pow(v, n);
    return price;
  }

}
