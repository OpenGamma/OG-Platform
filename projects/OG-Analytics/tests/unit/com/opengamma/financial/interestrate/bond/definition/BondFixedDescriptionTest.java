/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityPaymentFixedDefinition;
import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

public class BondFixedDescriptionTest {

  //Semi-annual 2Y
  private static final Currency CUR = Currency.USD;
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final int PAYMENT_PER_YEAR = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final boolean IS_EOM = false;
  private static final Period BOND_TENOR = Period.ofYears(2);
  private static final ZonedDateTime START_ACCRUAL_DATE = DateUtil.getUTCDate(2011, 7, 13);
  private static final ZonedDateTime MATURITY_DATE = START_ACCRUAL_DATE.plus(BOND_TENOR);
  private static final double RATE = 0.0325;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final AnnuityCouponFixedDefinition COUPON_DEFINITION = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, START_ACCRUAL_DATE, MATURITY_DATE, PAYMENT_TENOR, PAYMENT_PER_YEAR,
      CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, 1.0, RATE, false);
  private static final AnnuityPaymentFixedDefinition NOMINAL_DEFINITION = new AnnuityPaymentFixedDefinition(new PaymentFixedDefinition[] {new PaymentFixedDefinition(CUR, MATURITY_DATE, 1.0)});
  // to derivatives: common
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  YieldCurveBundle CURVES = TestsDataSets.createCurves1();
  // to derivatives: first coupon
  private static final ZonedDateTime REFERENCE_DATE_1 = DateUtil.getUTCDate(2011, 8, 18);
  private static final AnnuityCouponFixed COUPON_1 = COUPON_DEFINITION.toDerivative(REFERENCE_DATE_1, CURVES_NAME);
  private static final AnnuityPaymentFixed NOMINAL_1 = NOMINAL_DEFINITION.toDerivative(REFERENCE_DATE_1, CURVES_NAME);
  private static final BondFixedDescription BOND_DESCRIPTION_1 = new BondFixedDescription(NOMINAL_1, COUPON_1, YIELD_CONVENTION);
  // to derivatives: second coupon
  private static final ZonedDateTime REFERENCE_DATE_2 = DateUtil.getUTCDate(2012, 2, 16);
  private static final AnnuityCouponFixed COUPON_2 = COUPON_DEFINITION.toDerivative(REFERENCE_DATE_2, CURVES_NAME);
  private static final AnnuityPaymentFixed NOMINAL_2 = NOMINAL_DEFINITION.toDerivative(REFERENCE_DATE_2, CURVES_NAME);
  private static final BondFixedDescription BOND_DESCRIPTION_2 = new BondFixedDescription(NOMINAL_2, COUPON_2, YIELD_CONVENTION);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNominal() {
    new BondFixedDescription(null, COUPON_1, YIELD_CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCoupon() {
    new BondFixedDescription(NOMINAL_1, null, YIELD_CONVENTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullYield() {
    new BondFixedDescription(NOMINAL_1, COUPON_1, null);
  }

  @Test
  public void testGetters1() {
    assertEquals(NOMINAL_1, BOND_DESCRIPTION_1.getNominal());
    assertEquals(COUPON_1, BOND_DESCRIPTION_1.getCoupon());
  }

  @Test
  public void testGetters2() {
    assertEquals(NOMINAL_2, BOND_DESCRIPTION_2.getNominal());
    assertEquals(COUPON_2, BOND_DESCRIPTION_2.getCoupon());
  }
}
