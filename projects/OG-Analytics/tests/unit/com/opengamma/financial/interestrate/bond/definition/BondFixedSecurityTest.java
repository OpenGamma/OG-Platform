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
import com.opengamma.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

public class BondFixedSecurityTest {

  //Semi-annual 2Y
  private static final Currency CUR = Currency.USD;
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final int PAYMENT_PER_YEAR = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final boolean IS_EOM = false;
  private static final Period BOND_TENOR = Period.ofYears(2);
  private static final int SETTLEMENT_DAYS = 2;
  private static final ZonedDateTime START_ACCRUAL_DATE = DateUtils.getUTCDate(2011, 7, 13);
  private static final ZonedDateTime MATURITY_DATE = START_ACCRUAL_DATE.plus(BOND_TENOR);
  private static final double RATE = 0.0325;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final AnnuityCouponFixedDefinition COUPON_DEFINITION = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, START_ACCRUAL_DATE, MATURITY_DATE, PAYMENT_TENOR, PAYMENT_PER_YEAR,
      CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, 1.0, RATE, false);
  private static final AnnuityPaymentFixedDefinition NOMINAL_DEFINITION = new AnnuityPaymentFixedDefinition(new PaymentFixedDefinition[] {new PaymentFixedDefinition(CUR, MATURITY_DATE, 1.0)});
  private static final BondFixedSecurityDefinition BOND_SECURITY_DEFINITION = BondFixedSecurityDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR,
      DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM);
  // to derivatives: common
  private static final String CREDIT_CURVE_NAME = "Credit";
  private static final String REPO_CURVE_NAME = "Repo";
  private static final String[] CURVES_NAME = {CREDIT_CURVE_NAME, REPO_CURVE_NAME};
  YieldCurveBundle CURVES = TestsDataSets.createCurves1();
  // to derivatives: first coupon
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final ZonedDateTime REFERENCE_DATE_1 = DateUtils.getUTCDate(2011, 8, 18);
  private static final ZonedDateTime SPOT_1 = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_1, SETTLEMENT_DAYS, CALENDAR);
  private static final double SETTLEMENT_TIME_1 = ACT_ACT.getDayCountFraction(REFERENCE_DATE_1, SPOT_1);
  private static final AnnuityCouponFixedDefinition COUPON_DEFINITION_TRIM_1 = COUPON_DEFINITION.trimBefore(SPOT_1);
  private static final AnnuityCouponFixed COUPON_1 = COUPON_DEFINITION.toDerivative(REFERENCE_DATE_1, CURVES_NAME);
  private static final AnnuityPaymentFixed NOMINAL_1 = NOMINAL_DEFINITION.toDerivative(REFERENCE_DATE_1, CURVES_NAME);
  private static final double ACCRUED_AT_SPOT_1 = BOND_SECURITY_DEFINITION.accruedInterest(SPOT_1);
  private static final double FACTOR_SPOT_1 = DAY_COUNT.getAccruedInterest(COUPON_DEFINITION_TRIM_1.getNthPayment(0).getAccrualStartDate(), SPOT_1, COUPON_DEFINITION_TRIM_1.getNthPayment(0)
      .getAccrualEndDate(), 1.0, PAYMENT_PER_YEAR);
  private static final double FACTOR_PERIOD_1 = DAY_COUNT.getAccruedInterest(COUPON_DEFINITION_TRIM_1.getNthPayment(0).getAccrualStartDate(), COUPON_DEFINITION_TRIM_1.getNthPayment(0)
      .getAccrualStartDate(), COUPON_DEFINITION_TRIM_1.getNthPayment(0).getAccrualEndDate(), 1.0, PAYMENT_PER_YEAR);
  private static final double FACTOR_TO_NEXT_1 = (FACTOR_PERIOD_1 - FACTOR_SPOT_1) / FACTOR_PERIOD_1;
  private static final BondFixedSecurity BOND_DESCRIPTION_1 = new BondFixedSecurity(NOMINAL_1, COUPON_1, SETTLEMENT_TIME_1, ACCRUED_AT_SPOT_1, FACTOR_TO_NEXT_1, YIELD_CONVENTION, PAYMENT_PER_YEAR,
      REPO_CURVE_NAME, "");
  // to derivatives: second coupon
  private static final ZonedDateTime REFERENCE_DATE_2 = DateUtils.getUTCDate(2012, 2, 16);
  private static final ZonedDateTime SPOT_2 = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_2, SETTLEMENT_DAYS, CALENDAR);
  private static final double SETTLEMENT_TIME_2 = ACT_ACT.getDayCountFraction(REFERENCE_DATE_2, SPOT_2);
  private static final AnnuityCouponFixedDefinition COUPON_DEFINITION_TRIM_2 = COUPON_DEFINITION.trimBefore(SPOT_2);
  private static final AnnuityCouponFixed COUPON_2 = COUPON_DEFINITION.toDerivative(REFERENCE_DATE_2, CURVES_NAME);
  private static final AnnuityPaymentFixed NOMINAL_2 = NOMINAL_DEFINITION.toDerivative(REFERENCE_DATE_2, CURVES_NAME);
  private static final double ACCRUED_AT_SPOT_2 = BOND_SECURITY_DEFINITION.accruedInterest(SPOT_2);
  private static final double FACTOR_SPOT_2 = DAY_COUNT.getAccruedInterest(COUPON_DEFINITION_TRIM_2.getNthPayment(0).getAccrualStartDate(), SPOT_2, COUPON_DEFINITION_TRIM_2.getNthPayment(0)
      .getAccrualEndDate(), 1.0, PAYMENT_PER_YEAR);
  private static final double FACTOR_PERIOD_2 = DAY_COUNT.getAccruedInterest(COUPON_DEFINITION_TRIM_2.getNthPayment(0).getAccrualStartDate(), COUPON_DEFINITION_TRIM_2.getNthPayment(0)
      .getAccrualStartDate(), COUPON_DEFINITION_TRIM_2.getNthPayment(0).getAccrualEndDate(), 1.0, PAYMENT_PER_YEAR);
  private static final double FACTOR_TO_NEXT_2 = (FACTOR_PERIOD_2 - FACTOR_SPOT_2) / FACTOR_PERIOD_2;
  private static final BondFixedSecurity BOND_DESCRIPTION_2 = new BondFixedSecurity(NOMINAL_2, COUPON_2, SETTLEMENT_TIME_2, ACCRUED_AT_SPOT_2, FACTOR_TO_NEXT_2, YIELD_CONVENTION, PAYMENT_PER_YEAR,
      REPO_CURVE_NAME, "");

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNominal() {
    new BondFixedSecurity(null, COUPON_1, SETTLEMENT_TIME_1, ACCRUED_AT_SPOT_1, FACTOR_TO_NEXT_1, YIELD_CONVENTION, PAYMENT_PER_YEAR, REPO_CURVE_NAME, "");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCoupon() {
    new BondFixedSecurity(NOMINAL_1, null, SETTLEMENT_TIME_1, ACCRUED_AT_SPOT_1, FACTOR_TO_NEXT_1, YIELD_CONVENTION, PAYMENT_PER_YEAR, REPO_CURVE_NAME, "");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullYield() {
    new BondFixedSecurity(NOMINAL_1, COUPON_1, SETTLEMENT_TIME_1, ACCRUED_AT_SPOT_1, FACTOR_TO_NEXT_1, null, PAYMENT_PER_YEAR, REPO_CURVE_NAME, "");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDiscounting() {
    new BondFixedSecurity(NOMINAL_1, COUPON_1, SETTLEMENT_TIME_1, ACCRUED_AT_SPOT_1, FACTOR_TO_NEXT_1, YIELD_CONVENTION, PAYMENT_PER_YEAR, null, "");
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
