/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
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
import com.opengamma.financial.instrument.bond.BondFixedDescriptionDefinition;
import com.opengamma.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

public class BondFixedTransactionTest {

  //Semi-annual 2Y
  private static final Currency CUR = Currency.USD;
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final boolean IS_EOM = false;
  private static final Period BOND_TENOR = Period.ofYears(2);
  private static final int SETTLEMENT_DAYS = 3;
  private static final ZonedDateTime START_ACCRUAL_DATE = DateUtil.getUTCDate(2011, 7, 13);
  private static final ZonedDateTime MATURITY_DATE = START_ACCRUAL_DATE.plus(BOND_TENOR);
  private static final double RATE = 0.0325;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final BondFixedDescriptionDefinition BOND_DESCRIPTION_DEFINITION = BondFixedDescriptionDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS,
      CALENDAR, DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM);
  // to derivatives: common
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final String CREDIT_CURVE_NAME = "Credit";
  private static final String DISCOUNTING_CURVE_NAME = "Discounting";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {CREDIT_CURVE_NAME, DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME};
  YieldCurveBundle CURVES = TestsDataSets.createCurvesBond1();
  // to derivatives: first coupon
  private static final LocalDate REFERENCE_DATE_1 = LocalDate.of(2011, 8, 18);
  private static final ZonedDateTime REFERENCE_DATE_Z_1 = ZonedDateTime.of(LocalDateTime.ofMidnight(REFERENCE_DATE_1), TimeZone.UTC);
  // Transaction
  private static final double PRICE = 0.90;
  private static final ZonedDateTime BOND_SETTLEMENT_DATE = DateUtil.getUTCDate(2011, 8, 24);
  private static final double BOND_SETTLEMENT_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_Z_1, BOND_SETTLEMENT_DATE);
  private static final ZonedDateTime STANDARD_SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_Z_1, CALENDAR, SETTLEMENT_DAYS);
  private static final double STANDARD_SETTLEMENT_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_Z_1, STANDARD_SETTLEMENT_DATE);
  private static final double QUANTITY = 100000000; //100m
  private static final AnnuityCouponFixed COUPON = BOND_DESCRIPTION_DEFINITION.getCoupon().toDerivative(REFERENCE_DATE_Z_1, CURVES_NAME);
  private static final AnnuityPaymentFixed NOMINAL = BOND_DESCRIPTION_DEFINITION.getNominal().toDerivative(REFERENCE_DATE_Z_1, CURVES_NAME);
  private static final AnnuityCouponFixed COUPON_TR = COUPON.trimBefore(BOND_SETTLEMENT_TIME);
  private static final AnnuityPaymentFixed NOMINAL_TR = NOMINAL.trimBefore(BOND_SETTLEMENT_TIME);
  private static final AnnuityCouponFixed COUPON_STD = COUPON.trimBefore(STANDARD_SETTLEMENT_TIME);
  private static final AnnuityPaymentFixed NOMINAL_STD = NOMINAL.trimBefore(STANDARD_SETTLEMENT_TIME);
  private static final BondFixedTransactionDefinition BOND_TRANSACTION_DEFINITION = new BondFixedTransactionDefinition(BOND_DESCRIPTION_DEFINITION, QUANTITY, BOND_SETTLEMENT_DATE, PRICE);
  private static final BondFixedDescription BOND_TR_DESCRIPTION = new BondFixedDescription(NOMINAL_TR, COUPON_TR, YIELD_CONVENTION);
  private static final BondFixedDescription BOND_STD_DESCRIPTION = new BondFixedDescription(NOMINAL_STD, COUPON_STD, YIELD_CONVENTION);
  private static final PaymentFixed BOND_SETTLEMENT = new PaymentFixed(CUR, BOND_SETTLEMENT_TIME, -PRICE * QUANTITY, DISCOUNTING_CURVE_NAME);
  private static final double ACCRUED_AT_SPOT = BOND_DESCRIPTION_DEFINITION.accruedInterest(STANDARD_SETTLEMENT_DATE);
  private static final BondFixedTransaction BOND_TRANSACTION = new BondFixedTransaction(BOND_TR_DESCRIPTION, QUANTITY, BOND_SETTLEMENT, BOND_STD_DESCRIPTION, STANDARD_SETTLEMENT_TIME,
      ACCRUED_AT_SPOT, 1.0);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBondPurchase() {
    new BondFixedTransaction(null, QUANTITY, BOND_SETTLEMENT, BOND_STD_DESCRIPTION, STANDARD_SETTLEMENT_TIME, ACCRUED_AT_SPOT, 1.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSettlement() {
    new BondFixedTransaction(BOND_TR_DESCRIPTION, QUANTITY, null, BOND_STD_DESCRIPTION, STANDARD_SETTLEMENT_TIME, ACCRUED_AT_SPOT, 1.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBondStandard() {
    new BondFixedTransaction(BOND_TR_DESCRIPTION, QUANTITY, BOND_SETTLEMENT, null, STANDARD_SETTLEMENT_TIME, ACCRUED_AT_SPOT, 1.0);
  }

  @Test
  public void testGetters1() {
    assertEquals(BOND_TR_DESCRIPTION, BOND_TRANSACTION.getBondTransaction());
    assertEquals(QUANTITY, BOND_TRANSACTION.getQuantity());
    assertEquals(BOND_SETTLEMENT, BOND_TRANSACTION.getSettlement());
    assertEquals(BOND_STD_DESCRIPTION, BOND_TRANSACTION.getBondStandard());
    assertEquals(STANDARD_SETTLEMENT_TIME, BOND_TRANSACTION.getSpotTime());
    assertEquals(ACCRUED_AT_SPOT, BOND_TRANSACTION.getAccruedInterestAtSpot());
  }

  @Test
  public void testToDerivative() {
    final BondFixedTransaction convertedTransaction = BOND_TRANSACTION_DEFINITION.toDerivative(REFERENCE_DATE_Z_1, CURVES_NAME);
    convertedTransaction.equals(BOND_TRANSACTION);
    assertEquals(convertedTransaction, BOND_TRANSACTION);

  }
}
