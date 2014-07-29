/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.definition;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
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
public class BondFixedTransactionTest {

  //Semi-annual 2Y
  private static final Currency CUR = Currency.EUR;
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final int COUPON_PER_YEAR = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final String ISSUER_NAME = "Issuer";
  private static final DayCount DAY_COUNT = DayCounts.ACT_ACT_ICMA;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM = false;
  private static final Period BOND_TENOR = Period.ofYears(2);
  private static final int SETTLEMENT_DAYS = 3;
  private static final ZonedDateTime START_ACCRUAL_DATE = DateUtils.getUTCDate(2011, 7, 13);
  private static final ZonedDateTime MATURITY_DATE = START_ACCRUAL_DATE.plus(BOND_TENOR);
  private static final double RATE = 0.0325;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final BondFixedSecurityDefinition BOND_SECURITY_DEFINITION = BondFixedSecurityDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR,
      DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM, ISSUER_NAME);
  // to derivatives: common
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  // to derivatives: first coupon
  private static final ZonedDateTime REFERENCE_DATE_Z_1 = DateUtils.getUTCDate(2011, 8, 18);
  // Transaction
  private static final double PRICE = 0.90;
  private static final ZonedDateTime BOND_SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 8, 24);
  private static final double BOND_SETTLEMENT_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_Z_1, BOND_SETTLEMENT_DATE);
  private static final ZonedDateTime STANDARD_SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_Z_1, SETTLEMENT_DAYS, CALENDAR);
  private static final double STANDARD_SETTLEMENT_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_Z_1, STANDARD_SETTLEMENT_DATE);
  private static final double QUANTITY = 100000000; //100m
  private static final AnnuityCouponFixedDefinition COUPON_DEFINITION = BOND_SECURITY_DEFINITION.getCoupons();
  private static final AnnuityCouponFixedDefinition COUPON_DEFINITION_TRIM = COUPON_DEFINITION.trimBefore(STANDARD_SETTLEMENT_DATE);
  private static final AnnuityCouponFixed COUPON = BOND_SECURITY_DEFINITION.getCoupons().toDerivative(REFERENCE_DATE_Z_1);
  private static final AnnuityPaymentFixed NOMINAL = (AnnuityPaymentFixed) BOND_SECURITY_DEFINITION.getNominal().toDerivative(REFERENCE_DATE_Z_1);
  private static final AnnuityCouponFixed COUPON_TR = COUPON.trimBefore(BOND_SETTLEMENT_TIME);
  private static final AnnuityPaymentFixed NOMINAL_TR = NOMINAL.trimBefore(BOND_SETTLEMENT_TIME);
  private static final AnnuityCouponFixed COUPON_STD = COUPON.trimBefore(STANDARD_SETTLEMENT_TIME);
  private static final AnnuityPaymentFixed NOMINAL_STD = NOMINAL.trimBefore(STANDARD_SETTLEMENT_TIME);
  private static final BondFixedTransactionDefinition BOND_TRANSACTION_DEFINITION = new BondFixedTransactionDefinition(BOND_SECURITY_DEFINITION, QUANTITY, BOND_SETTLEMENT_DATE, PRICE);
  private static final double ACCRUED_AT_SPOT = BOND_SECURITY_DEFINITION.accruedInterest(STANDARD_SETTLEMENT_DATE);
  private static final double FACTOR_SPOT = DAY_COUNT.getAccruedInterest(COUPON_DEFINITION_TRIM.getNthPayment(0).getAccrualStartDate(), STANDARD_SETTLEMENT_DATE,
      COUPON_DEFINITION_TRIM.getNthPayment(0).getAccrualEndDate(), 1.0, COUPON_PER_YEAR);
  private static final double FACTOR_PERIOD = DAY_COUNT.getAccruedInterest(COUPON_DEFINITION_TRIM.getNthPayment(0).getAccrualStartDate(),
      COUPON_DEFINITION_TRIM.getNthPayment(0).getAccrualStartDate(), COUPON_DEFINITION_TRIM.getNthPayment(0).getAccrualEndDate(), 1.0, COUPON_PER_YEAR);
  private static final double FACTOR_TO_NEXT = (FACTOR_PERIOD - FACTOR_SPOT) / FACTOR_PERIOD;
  private static final BondFixedSecurity BOND_TR_DESCRIPTION = new BondFixedSecurity(NOMINAL_TR, COUPON_TR, BOND_SETTLEMENT_TIME, BOND_TRANSACTION_DEFINITION.getAccruedInterestAtSettlement(), 0.0,
      YIELD_CONVENTION, COUPON_PER_YEAR, "Issuer");
  private static final BondFixedSecurity BOND_STD_DESCRIPTION = new BondFixedSecurity(NOMINAL_STD, COUPON_STD, STANDARD_SETTLEMENT_TIME, ACCRUED_AT_SPOT, FACTOR_TO_NEXT, YIELD_CONVENTION,
      COUPON_PER_YEAR, "Issuer");
  private static final BondFixedTransaction BOND_TRANSACTION = new BondFixedTransaction(BOND_TR_DESCRIPTION, QUANTITY, PRICE, BOND_STD_DESCRIPTION, 1.0);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBondPurchase() {
    new BondFixedTransaction(null, QUANTITY, PRICE, BOND_STD_DESCRIPTION, 1.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBondStandard() {
    new BondFixedTransaction(BOND_TR_DESCRIPTION, QUANTITY, PRICE, null, 1.0);
  }

  @Test
  public void testGetters1() {
    assertEquals(BOND_TR_DESCRIPTION, BOND_TRANSACTION.getBondTransaction());
    assertEquals(QUANTITY, BOND_TRANSACTION.getQuantity());
    //    assertEquals(-PRICE * QUANTITY, BOND_TRANSACTION.getSettlementAmount());
    assertEquals(BOND_STD_DESCRIPTION, BOND_TRANSACTION.getBondStandard());
    assertEquals(STANDARD_SETTLEMENT_TIME, BOND_TRANSACTION.getBondStandard().getSettlementTime());
    assertEquals(BOND_SETTLEMENT_TIME, BOND_TRANSACTION.getBondTransaction().getSettlementTime());
    assertEquals(ACCRUED_AT_SPOT, BOND_TRANSACTION.getBondStandard().getAccruedInterest());
  }

}
