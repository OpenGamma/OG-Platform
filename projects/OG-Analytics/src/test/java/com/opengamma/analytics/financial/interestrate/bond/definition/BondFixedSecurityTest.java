/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityPaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BondFixedSecurityTest {

  //Semi-annual 2Y
  private static final Currency CUR = Currency.EUR;
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final int PAYMENT_PER_YEAR = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final String ISSUER_NAME = "Issuer";
  private static final DayCount DAY_COUNT = DayCounts.ACT_ACT_ICMA;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM = false;
  private static final Period BOND_TENOR = Period.ofYears(2);
  private static final int SETTLEMENT_DAYS = 2;
  private static final ZonedDateTime START_ACCRUAL_DATE = DateUtils.getUTCDate(2011, 7, 13);
  private static final ZonedDateTime MATURITY_DATE = START_ACCRUAL_DATE.plus(BOND_TENOR);
  private static final double RATE = 0.0325;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final AnnuityCouponFixedDefinition COUPON_DEFINITION = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, START_ACCRUAL_DATE, MATURITY_DATE, PAYMENT_TENOR, PAYMENT_PER_YEAR,
      true, true, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, 1.0, RATE, false);
  private static final AnnuityPaymentFixedDefinition NOMINAL_DEFINITION = new AnnuityPaymentFixedDefinition(new PaymentFixedDefinition[] {new PaymentFixedDefinition(CUR, MATURITY_DATE, 1.0)}, CALENDAR);
  private static final BondFixedSecurityDefinition BOND_SECURITY_DEFINITION = BondFixedSecurityDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR,
      DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM, ISSUER_NAME);
  // to derivatives: first coupon
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final ZonedDateTime REFERENCE_DATE_1 = DateUtils.getUTCDate(2011, 8, 18);
  private static final ZonedDateTime SPOT_1 = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_1, SETTLEMENT_DAYS, CALENDAR);
  private static final double SETTLEMENT_TIME_1 = ACT_ACT.getDayCountFraction(REFERENCE_DATE_1, SPOT_1);
  private static final AnnuityCouponFixedDefinition COUPON_DEFINITION_TRIM_1 = COUPON_DEFINITION.trimBefore(SPOT_1);
  private static final AnnuityCouponFixed COUPON_1 = COUPON_DEFINITION.toDerivative(REFERENCE_DATE_1);
  private static final AnnuityPaymentFixed NOMINAL_1 = NOMINAL_DEFINITION.toDerivative(REFERENCE_DATE_1);
  private static final double ACCRUED_AT_SPOT_1 = BOND_SECURITY_DEFINITION.accruedInterest(SPOT_1);
  private static final double FACTOR_SPOT_1 = DAY_COUNT.getAccruedInterest(COUPON_DEFINITION_TRIM_1.getNthPayment(0).getAccrualStartDate(), SPOT_1, COUPON_DEFINITION_TRIM_1.getNthPayment(0)
      .getAccrualEndDate(), 1.0, PAYMENT_PER_YEAR);
  private static final double FACTOR_PERIOD_1 = DAY_COUNT.getAccruedInterest(COUPON_DEFINITION_TRIM_1.getNthPayment(0).getAccrualStartDate(), COUPON_DEFINITION_TRIM_1.getNthPayment(0)
      .getAccrualEndDate(), COUPON_DEFINITION_TRIM_1.getNthPayment(0).getAccrualEndDate(), 1.0, PAYMENT_PER_YEAR);
  private static final double FACTOR_TO_NEXT_1 = (FACTOR_PERIOD_1 - FACTOR_SPOT_1) / FACTOR_PERIOD_1;
  private static final BondFixedSecurity BOND_DESCRIPTION_1 = new BondFixedSecurity(NOMINAL_1, COUPON_1, SETTLEMENT_TIME_1, ACCRUED_AT_SPOT_1, FACTOR_TO_NEXT_1, YIELD_CONVENTION, PAYMENT_PER_YEAR,
      "Issuer");
  // to derivatives: second coupon
  private static final ZonedDateTime REFERENCE_DATE_2 = DateUtils.getUTCDate(2012, 2, 16);
  private static final ZonedDateTime SPOT_2 = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_2, SETTLEMENT_DAYS, CALENDAR);
  private static final double SETTLEMENT_TIME_2 = ACT_ACT.getDayCountFraction(REFERENCE_DATE_2, SPOT_2);
  private static final AnnuityCouponFixedDefinition COUPON_DEFINITION_TRIM_2 = COUPON_DEFINITION.trimBefore(SPOT_2);
  private static final AnnuityCouponFixed COUPON_2 = COUPON_DEFINITION.toDerivative(REFERENCE_DATE_2);
  private static final AnnuityPaymentFixed NOMINAL_2 = NOMINAL_DEFINITION.toDerivative(REFERENCE_DATE_2);
  private static final double ACCRUED_AT_SPOT_2 = BOND_SECURITY_DEFINITION.accruedInterest(SPOT_2);
  private static final double FACTOR_SPOT_2 = DAY_COUNT.getAccruedInterest(COUPON_DEFINITION_TRIM_2.getNthPayment(0).getAccrualStartDate(), SPOT_2, COUPON_DEFINITION_TRIM_2.getNthPayment(0)
      .getAccrualEndDate(), 1.0, PAYMENT_PER_YEAR);
  private static final double FACTOR_PERIOD_2 = DAY_COUNT.getAccruedInterest(COUPON_DEFINITION_TRIM_2.getNthPayment(0).getAccrualStartDate(), COUPON_DEFINITION_TRIM_2.getNthPayment(0)
      .getAccrualStartDate(), COUPON_DEFINITION_TRIM_2.getNthPayment(0).getAccrualEndDate(), 1.0, PAYMENT_PER_YEAR);
  private static final double FACTOR_TO_NEXT_2 = (FACTOR_PERIOD_2 - FACTOR_SPOT_2) / FACTOR_PERIOD_2;
  private static final BondFixedSecurity BOND_DESCRIPTION_2 = new BondFixedSecurity(NOMINAL_2, COUPON_2, SETTLEMENT_TIME_2, ACCRUED_AT_SPOT_2, FACTOR_TO_NEXT_2, YIELD_CONVENTION, PAYMENT_PER_YEAR,
      "Issuer");

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNominal() {
    new BondFixedSecurity(null, COUPON_1, SETTLEMENT_TIME_1, ACCRUED_AT_SPOT_1, FACTOR_TO_NEXT_1, YIELD_CONVENTION, PAYMENT_PER_YEAR, "Issuer");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCoupon() {
    new BondFixedSecurity(NOMINAL_1, null, SETTLEMENT_TIME_1, ACCRUED_AT_SPOT_1, FACTOR_TO_NEXT_1, YIELD_CONVENTION, PAYMENT_PER_YEAR, "Issuer");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullYield() {
    new BondFixedSecurity(NOMINAL_1, COUPON_1, SETTLEMENT_TIME_1, ACCRUED_AT_SPOT_1, FACTOR_TO_NEXT_1, null, PAYMENT_PER_YEAR, "Issuer");
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalStateException.class)
  public void testGetDiscountingName() {
    BOND_DESCRIPTION_1.getDiscountingCurveName();
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalStateException.class)
  public void testRepoCurveName() {
    BOND_DESCRIPTION_1.getRepoCurveName();
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

  @Test
  public void testHashCodeEquals() {
    final BondFixedSecurity bond = new BondFixedSecurity(NOMINAL_1, COUPON_1, SETTLEMENT_TIME_1, ACCRUED_AT_SPOT_1, FACTOR_TO_NEXT_1, YIELD_CONVENTION, PAYMENT_PER_YEAR,
        "Issuer");
    BondFixedSecurity other = new BondFixedSecurity(NOMINAL_1, COUPON_1, SETTLEMENT_TIME_1, ACCRUED_AT_SPOT_1, FACTOR_TO_NEXT_1, YIELD_CONVENTION, PAYMENT_PER_YEAR,
        "Issuer");
    assertEquals(bond, other);
    assertEquals(bond.hashCode(), other.hashCode());
    other = new BondFixedSecurity(NOMINAL_DEFINITION.toDerivative(REFERENCE_DATE_1.minusDays(1)), COUPON_1, SETTLEMENT_TIME_1, ACCRUED_AT_SPOT_1, FACTOR_TO_NEXT_1, YIELD_CONVENTION, PAYMENT_PER_YEAR,
        "Issuer");
    assertFalse(other.equals(bond));
    other = new BondFixedSecurity(NOMINAL_1, COUPON_DEFINITION.toDerivative(REFERENCE_DATE_1.minusDays(1)), SETTLEMENT_TIME_1, ACCRUED_AT_SPOT_1, FACTOR_TO_NEXT_1, YIELD_CONVENTION, PAYMENT_PER_YEAR,
        "Issuer");
    assertFalse(other.equals(bond));
    other = new BondFixedSecurity(NOMINAL_1, COUPON_1, SETTLEMENT_TIME_1, ACCRUED_AT_SPOT_1 + 1, FACTOR_TO_NEXT_1, YIELD_CONVENTION, PAYMENT_PER_YEAR,
        "Issuer");
    assertFalse(other.equals(bond));
    other = new BondFixedSecurity(NOMINAL_1, COUPON_1, SETTLEMENT_TIME_1, ACCRUED_AT_SPOT_1, FACTOR_TO_NEXT_1 + 1, YIELD_CONVENTION, PAYMENT_PER_YEAR,
        "Issuer");
    assertFalse(other.equals(bond));
    other = new BondFixedSecurity(NOMINAL_1, COUPON_1, SETTLEMENT_TIME_1, ACCRUED_AT_SPOT_1, FACTOR_TO_NEXT_1, SimpleYieldConvention.AUSTRIA_ISMA_METHOD, PAYMENT_PER_YEAR,
        "Issuer");
    assertFalse(other.equals(bond));
  }
}
