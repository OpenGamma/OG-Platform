/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BondFutureConversionFactorMethodTest {

  private static final BondFutureConversionFactorMethod METHOD_CONVERSION = new BondFutureConversionFactorMethod();

  // ===== LIFFE Gilt =====
  private static final Currency G_CUR = Currency.GBP;
  private static final Period G_PAYMENT_TENOR = Period.ofMonths(6);
  private static final Calendar G_CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount G_DAY_COUNT = DayCounts.ACT_ACT_ICMA;
  private static final BusinessDayConvention G_BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final boolean G_IS_EOM = false;
  private static final int G_SETTLEMENT_DAYS = 2;
  private static final int G_EX_COUPON = 7;
  private static final YieldConvention G_YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("UK:BUMP/DMO METHOD");
  private static final Period[] G_BOND_TENOR = new Period[] {Period.ofYears(4), Period.ofYears(4), Period.ofYears(4)};
  private static final ZonedDateTime[] G_START_ACCRUAL_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2010, 3, 7), DateUtils.getUTCDate(2010, 9, 7), DateUtils.getUTCDate(2011, 1, 22)};
  private static final double[] G_RATE = new double[] {0.0225, 0.0500, 0.0275};
  private static final int G_NB_BOND = G_BOND_TENOR.length;
  private static final ZonedDateTime[] G_MATURITY_DATE = new ZonedDateTime[G_NB_BOND];
  private static final BondFixedSecurityDefinition[] G_BASKET_DEFINITION = new BondFixedSecurityDefinition[G_NB_BOND];
  static {
    for (int loopbasket = 0; loopbasket < G_NB_BOND; loopbasket++) {
      G_MATURITY_DATE[loopbasket] = G_START_ACCRUAL_DATE[loopbasket].plus(G_BOND_TENOR[loopbasket]);
      G_BASKET_DEFINITION[loopbasket] = BondFixedSecurityDefinition.from(G_CUR, G_MATURITY_DATE[loopbasket], G_START_ACCRUAL_DATE[loopbasket], G_PAYMENT_TENOR, G_RATE[loopbasket], G_SETTLEMENT_DAYS,
          1.0, G_EX_COUPON, G_CALENDAR, G_DAY_COUNT, G_BUSINESS_DAY, G_YIELD_CONVENTION, G_IS_EOM, "UK Govt", "UK Govt");
    }
  }
  private static final ZonedDateTime[] G_FIRST_DELIVERY_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2011, 12, 1), DateUtils.getUTCDate(2012, 3, 1)};
  // Note: March contract involves ex-coupon periods.
  private static final double TOLERANCE_FACTOR_LIFFE = 1.0E-7;
  private static final double TOLERANCE_FACTOR_EURONEXT = 1.0E-6;

  @Test
  public void conversionFactorLiffe() {
    double notionalCoupon = 0.03; // 3% notional coupon on short Gilt as of Dec 11 contract.
    double[][] conversionFactorExternal = new double[][] { {0.9836635, 1.0526713, 0.9925377}, {0.9854333, 1.0481364, 0.9931048}}; // Mar12
    double[][] conversionFactorComputed = new double[G_FIRST_DELIVERY_DATE.length][G_NB_BOND];
    double[][] diff = new double[G_FIRST_DELIVERY_DATE.length][G_NB_BOND];
    for (int loopexp = 0; loopexp < G_FIRST_DELIVERY_DATE.length; loopexp++) {
      for (int loopbnd = 0; loopbnd < G_NB_BOND; loopbnd++) {
        conversionFactorComputed[loopexp][loopbnd] = METHOD_CONVERSION.conversionFactorLiffe(G_BASKET_DEFINITION[loopbnd], G_FIRST_DELIVERY_DATE[loopexp], notionalCoupon);
        diff[loopexp][loopbnd] = conversionFactorComputed[loopexp][loopbnd] - conversionFactorExternal[loopexp][loopbnd];
      }
      assertArrayEquals("Bond futures conversion factor: LIFFE", conversionFactorExternal[loopexp], conversionFactorComputed[loopexp], TOLERANCE_FACTOR_LIFFE);
    }
  }

  // ===== EURONEXT Buxl =====
  private static final Currency EUR = Currency.EUR;
  private static final Period PAYMENT_TENOR_GER = Period.ofMonths(12);
  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final DayCount DAY_COUNT_GER = DayCounts.ACT_ACT_ICMA;
  private static final BusinessDayConvention BUSINESS_DAY_GER = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM_GER = false;
  private static final int SETTLEMENT_DAYS_GER = 3;
  private static final int EX_COUPON_GER = 0;
  private static final YieldConvention YIELD_CONVENTION_GER = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION"); //???

  private static final ZonedDateTime[] MATURITY_DATE_BUXL = new ZonedDateTime[] {DateUtils.getUTCDate(2037, 1, 4), DateUtils.getUTCDate(2039, 7, 4), DateUtils.getUTCDate(2040, 7, 4),
      DateUtils.getUTCDate(2042, 7, 4)};
  private static final int NB_BOND_BUXL = MATURITY_DATE_BUXL.length;
  private static final ZonedDateTime[] START_ACCURAL_DATE_BUXL = new ZonedDateTime[] {DateUtils.getUTCDate(2005, 1, 4), DateUtils.getUTCDate(2007, 1, 26), DateUtils.getUTCDate(2008, 7, 4),
      DateUtils.getUTCDate(2010, 7, 4)};
  private static final double[] RATE_BUXL = new double[] {0.04, 0.0425, 0.0475, 0.0325};
  private static final BondFixedSecurityDefinition[] BASKET_DEFINITION_BUXL = new BondFixedSecurityDefinition[NB_BOND_BUXL];
  static {
    for (int loopbasket = 0; loopbasket < NB_BOND_BUXL; loopbasket++) {
      BASKET_DEFINITION_BUXL[loopbasket] = BondFixedSecurityDefinition.from(EUR, MATURITY_DATE_BUXL[loopbasket], START_ACCURAL_DATE_BUXL[loopbasket], PAYMENT_TENOR_GER, RATE_BUXL[loopbasket],
          SETTLEMENT_DAYS_GER, 1.0, EX_COUPON_GER, TARGET, DAY_COUNT_GER, BUSINESS_DAY_GER, YIELD_CONVENTION_GER, IS_EOM_GER, "GERMANY Govt", "Dsc");
    }
  }

  private static final ZonedDateTime[] FIRST_DELIVERY_DATE_UNADJUSTED_BUXL = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 3, 10), DateUtils.getUTCDate(2012, 6, 10)};
  private static final ZonedDateTime[] FIRST_DELIVERY_DATE_BUXL = new ZonedDateTime[] {BUSINESS_DAY_GER.adjustDate(TARGET, FIRST_DELIVERY_DATE_UNADJUSTED_BUXL[0]),
      BUSINESS_DAY_GER.adjustDate(TARGET, FIRST_DELIVERY_DATE_UNADJUSTED_BUXL[1])};

  @Test
  public void conversionFactorEuronext() {
    double notionalCoupon = 0.04; // 4% notional coupon on BUXL
    double[][] conversionFactorExternal = new double[][] { {0.999882, 1.040908, 1.125532, 0.869471}, {0.999807, 1.040828, 1.125072, 0.870130}}; // Mar12 - Jun12 
    double[][] conversionFactorComputed = new double[FIRST_DELIVERY_DATE_BUXL.length][NB_BOND_BUXL];
    double[][] diff = new double[FIRST_DELIVERY_DATE_BUXL.length][NB_BOND_BUXL];
    for (int loopexp = 0; loopexp < FIRST_DELIVERY_DATE_BUXL.length; loopexp++) {
      for (int loopbnd = 0; loopbnd < NB_BOND_BUXL; loopbnd++) {
        conversionFactorComputed[loopexp][loopbnd] = METHOD_CONVERSION.conversionFactorLiffe(BASKET_DEFINITION_BUXL[loopbnd], FIRST_DELIVERY_DATE_BUXL[loopexp], notionalCoupon);
        diff[loopexp][loopbnd] = conversionFactorComputed[loopexp][loopbnd] - conversionFactorExternal[loopexp][loopbnd];
      }
      assertArrayEquals("Bond futures conversion factor: EURONEXT", conversionFactorExternal[loopexp], conversionFactorComputed[loopexp], TOLERANCE_FACTOR_EURONEXT);
    }
  }

}
