/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.bond;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

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
import com.opengamma.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityPaymentFixedDefinition;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the construction of fixed coupon bond security Definition and conversion to Derivative.
 */
public class BondFixedSecurityDefinitionTest {

  //Semi-annual 2Y
  private static final Currency CUR = Currency.USD;
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final int COUPON_PER_YEAR = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final boolean IS_EOM = false;
  private static final Period BOND_TENOR = Period.ofYears(2);
  private static final int SETTLEMENT_DAYS = 2;
  private static final ZonedDateTime START_ACCRUAL_DATE = DateUtils.getUTCDate(2011, 7, 13);
  private static final ZonedDateTime MATURITY_DATE = START_ACCRUAL_DATE.plus(BOND_TENOR);
  private static final double RATE = 0.0325;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final double NOTIONAL = 1.0;
  private static final BondFixedSecurityDefinition BOND_SECURITY_DEFINITION = BondFixedSecurityDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR,
      DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM);
  // to derivatives: common
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final String CREDIT_CURVE_NAME = "Credit";
  private static final String REPO_CURVE_NAME = "Repo";
  private static final String[] CURVES_NAME = {CREDIT_CURVE_NAME, REPO_CURVE_NAME};
  //  private static final YieldCurveBundle CURVES = TestsDataSets.createCurves1();
  private static final ZonedDateTime REFERENCE_DATE_1 = DateUtils.getUTCDate(2011, 8, 18);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    BondFixedSecurityDefinition.from(null, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMaturity() {
    BondFixedSecurityDefinition.from(CUR, null, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStart() {
    BondFixedSecurityDefinition.from(CUR, MATURITY_DATE, null, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPeriod() {
    BondFixedSecurityDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, null, RATE, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendar() {
    BondFixedSecurityDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, null, DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDayCount() {
    BondFixedSecurityDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR, null, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBusinessDay() {
    BondFixedSecurityDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, null, YIELD_CONVENTION, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullYield() {
    BondFixedSecurityDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, null, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPositiveNominal() {
    AnnuityCouponFixedDefinition coupon = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, START_ACCRUAL_DATE, MATURITY_DATE, PAYMENT_TENOR, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, 1.0,
        RATE, false);
    AnnuityPaymentFixedDefinition nominal = new AnnuityPaymentFixedDefinition(new PaymentFixedDefinition[] {new PaymentFixedDefinition(CUR, MATURITY_DATE, -1.0)});
    new BondFixedSecurityDefinition(nominal, coupon, 0, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, YIELD_CONVENTION, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPositiveCoupon() {
    AnnuityCouponFixedDefinition coupon = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, START_ACCRUAL_DATE, MATURITY_DATE, PAYMENT_TENOR, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, 1.0,
        RATE, true);
    AnnuityPaymentFixedDefinition nominal = new AnnuityPaymentFixedDefinition(new PaymentFixedDefinition[] {new PaymentFixedDefinition(CUR, MATURITY_DATE, 1.0)});
    new BondFixedSecurityDefinition(nominal, coupon, 0, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, YIELD_CONVENTION, IS_EOM);
  }

  @Test
  public void testGetters() {
    assertEquals(SETTLEMENT_DAYS, BOND_SECURITY_DEFINITION.getSettlementDays());
    assertEquals(DAY_COUNT, BOND_SECURITY_DEFINITION.getDayCount());
    assertEquals(YIELD_CONVENTION, BOND_SECURITY_DEFINITION.getYieldConvention());
    assertEquals(COUPON_PER_YEAR, BOND_SECURITY_DEFINITION.getCouponPerYear());
    assertEquals(0, BOND_SECURITY_DEFINITION.getExCouponDays()); //Default
    assertEquals(CUR, BOND_SECURITY_DEFINITION.getCurrency());
    AnnuityCouponFixedDefinition coupon = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, START_ACCRUAL_DATE, MATURITY_DATE, PAYMENT_TENOR, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, 1.0,
        RATE, false);
    assertEquals(coupon, BOND_SECURITY_DEFINITION.getCoupon());
    AnnuityDefinition<PaymentFixedDefinition> nominal = new AnnuityDefinition<PaymentFixedDefinition>(new PaymentFixedDefinition[] {new PaymentFixedDefinition(CUR, BUSINESS_DAY.adjustDate(CALENDAR,
        MATURITY_DATE), 1.0)});
    assertEquals(nominal.getCurrency(), BOND_SECURITY_DEFINITION.getNominal().getCurrency());
    assertEquals(nominal.getNthPayment(0).getPaymentDate(), BOND_SECURITY_DEFINITION.getNominal().getNthPayment(0).getPaymentDate());
    assertEquals(nominal.getNthPayment(0).getAmount(), BOND_SECURITY_DEFINITION.getNominal().getNthPayment(0).getAmount());
  }

  @Test
  public void testDates() {
    BondFixedSecurityDefinition BOND_DEFINITION = BondFixedSecurityDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY,
        YIELD_CONVENTION, IS_EOM);
    ZonedDateTime[] expectedPaymentDates = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 1, 13), DateUtils.getUTCDate(2012, 7, 13), DateUtils.getUTCDate(2013, 1, 14), DateUtils.getUTCDate(2013, 7, 15)};
    ZonedDateTime[] expectedStartDates = new ZonedDateTime[] {DateUtils.getUTCDate(2011, 7, 13), DateUtils.getUTCDate(2012, 1, 13), DateUtils.getUTCDate(2012, 7, 13), DateUtils.getUTCDate(2013, 1, 13)};
    ZonedDateTime[] expectedEndDates = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 1, 13), DateUtils.getUTCDate(2012, 7, 13), DateUtils.getUTCDate(2013, 1, 13), DateUtils.getUTCDate(2013, 7, 13)};
    for (int loopcpn = 0; loopcpn < BOND_DEFINITION.getCoupon().getNumberOfPayments(); loopcpn++) {
      assertEquals("Payment " + loopcpn, expectedPaymentDates[loopcpn], BOND_DEFINITION.getCoupon().getNthPayment(loopcpn).getPaymentDate());
      assertEquals("Start accrual " + loopcpn, expectedStartDates[loopcpn], BOND_DEFINITION.getCoupon().getNthPayment(loopcpn).getAccrualStartDate());
      assertEquals("End accrual " + loopcpn, expectedEndDates[loopcpn], BOND_DEFINITION.getCoupon().getNthPayment(loopcpn).getAccrualEndDate());
    }
  }

  @Test
  public void toDerivativeUST() {
    BondFixedSecurity bondConverted = BOND_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE_1, CURVES_NAME);
    AnnuityPaymentFixedDefinition nominalDefinition = (AnnuityPaymentFixedDefinition) BOND_SECURITY_DEFINITION.getNominal();
    AnnuityCouponFixedDefinition couponDefinition = BOND_SECURITY_DEFINITION.getCoupon();
    ZonedDateTime spotDate1 = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_1, SETTLEMENT_DAYS, CALENDAR);
    nominalDefinition = nominalDefinition.trimBefore(spotDate1);
    couponDefinition = couponDefinition.trimBefore(spotDate1);

    AnnuityPaymentFixed nominal = nominalDefinition.toDerivative(REFERENCE_DATE_1, CURVES_NAME);
    AnnuityCouponFixed coupon = couponDefinition.toDerivative(REFERENCE_DATE_1, CURVES_NAME);
    double spotTime1 = ACT_ACT.getDayCountFraction(REFERENCE_DATE_1, spotDate1);
    double accruedInterest = DAY_COUNT.getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), spotDate1, couponDefinition.getNthPayment(0).getAccrualEndDate(), RATE,
        COUPON_PER_YEAR) * NOTIONAL;
    double factorSpot = DAY_COUNT.getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), spotDate1, couponDefinition.getNthPayment(0).getAccrualEndDate(), RATE, COUPON_PER_YEAR);
    double factorPeriod = DAY_COUNT.getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), couponDefinition.getNthPayment(0).getAccrualEndDate(), couponDefinition
        .getNthPayment(0).getAccrualEndDate(), RATE, COUPON_PER_YEAR);
    double factorToNextCoupon = (factorPeriod - factorSpot) / factorPeriod;
    BondFixedSecurity bondExpected = new BondFixedSecurity(nominal, coupon, spotTime1, accruedInterest, factorToNextCoupon, YIELD_CONVENTION, COUPON_PER_YEAR, REPO_CURVE_NAME, "");
    assertEquals("Bond Fixed Security Definition to derivative", bondConverted.getAccrualFactorToNextCoupon(), bondExpected.getAccrualFactorToNextCoupon(), 1.0E-10);
    assertEquals("Bond Fixed Security Definition to derivative", bondConverted.getAccruedInterest(), bondExpected.getAccruedInterest(), 1.0E-10);
    assertEquals("Bond Fixed Security Definition to derivative", bondConverted.getYieldConvention(), bondExpected.getYieldConvention());
    assertTrue("Bond Fixed Security Definition to derivative", bondConverted.equals(bondExpected));
    BondFixedSecurity bondConvertedDate = BOND_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE_1, spotDate1, CURVES_NAME);
    assertTrue("Bond Fixed Security Definition to derivative", bondConverted.equals(bondConvertedDate));
  }

  // UKT 5 09/07/14 - ISIN-GB0031829509
  private static final String ISSUER = "UK";
  private static final String REPO_TYPE = "General collateral";
  private static final Currency CUR_G = Currency.GBP;
  private static final Period PAYMENT_TENOR_G = Period.ofMonths(6);
  private static final int COUPON_PER_YEAR_G = 2;
  private static final Calendar CALENDAR_G = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_G = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"); // To check
  private static final BusinessDayConvention BUSINESS_DAY_G = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final boolean IS_EOM_G = false;
  private static final Period BOND_TENOR_G = Period.ofYears(12);
  private static final int SETTLEMENT_DAYS_G = 2;
  private static final int EX_DIVIDEND_DAYS_G = 7;
  private static final ZonedDateTime START_ACCRUAL_DATE_G = DateUtils.getUTCDate(2002, 9, 7);
  private static final ZonedDateTime MATURITY_DATE_G = START_ACCRUAL_DATE_G.plus(BOND_TENOR_G);
  private static final double RATE_G = 0.0500;
  private static final double NOTIONAL_G = 100;
  private static final YieldConvention YIELD_CONVENTION_G = YieldConventionFactory.INSTANCE.getYieldConvention("UK:BUMP/DMO METHOD");
  private static final BondFixedSecurityDefinition BOND_SECURITY_DEFINITION_G = BondFixedSecurityDefinition.from(CUR_G, MATURITY_DATE_G, START_ACCRUAL_DATE_G, PAYMENT_TENOR_G, RATE_G,
      SETTLEMENT_DAYS_G, NOTIONAL_G, EX_DIVIDEND_DAYS_G, CALENDAR_G, DAY_COUNT_G, BUSINESS_DAY_G, YIELD_CONVENTION_G, IS_EOM_G, ISSUER, REPO_TYPE);

  @Test
  public void testGettersUKT() {
    assertEquals(SETTLEMENT_DAYS_G, BOND_SECURITY_DEFINITION_G.getSettlementDays());
    assertEquals(NOTIONAL_G, BOND_SECURITY_DEFINITION_G.getNominal().getNthPayment(0).getAmount());
    assertEquals(EX_DIVIDEND_DAYS_G, BOND_SECURITY_DEFINITION_G.getExCouponDays());
  }

  @Test
  public void toDerivativeUKT() {
    BondFixedSecurity bondConverted = BOND_SECURITY_DEFINITION_G.toDerivative(REFERENCE_DATE_1, CURVES_NAME);
    AnnuityPaymentFixedDefinition nominalDefinition = (AnnuityPaymentFixedDefinition) BOND_SECURITY_DEFINITION_G.getNominal();
    AnnuityCouponFixedDefinition couponDefinition = BOND_SECURITY_DEFINITION_G.getCoupon();
    ZonedDateTime spotDate1 = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_1, SETTLEMENT_DAYS_G, CALENDAR_G);
    nominalDefinition = nominalDefinition.trimBefore(spotDate1);
    couponDefinition = couponDefinition.trimBefore(spotDate1);

    AnnuityPaymentFixed nominal = nominalDefinition.toDerivative(REFERENCE_DATE_1, CURVES_NAME);
    AnnuityCouponFixed coupon = couponDefinition.toDerivative(REFERENCE_DATE_1, CURVES_NAME);
    double spotTime1 = ACT_ACT.getDayCountFraction(REFERENCE_DATE_1, spotDate1);
    double accruedInterest = DAY_COUNT_G.getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), spotDate1, couponDefinition.getNthPayment(0).getAccrualEndDate(), RATE_G,
        COUPON_PER_YEAR_G) * NOTIONAL_G;
    double factorSpot = DAY_COUNT_G.getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), spotDate1, couponDefinition.getNthPayment(0).getAccrualEndDate(), RATE_G,
        COUPON_PER_YEAR_G);
    double factorPeriod = DAY_COUNT_G.getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), couponDefinition.getNthPayment(0).getAccrualEndDate(), couponDefinition
        .getNthPayment(0).getAccrualEndDate(), RATE_G, COUPON_PER_YEAR_G);
    double factorToNextCoupon = (factorPeriod - factorSpot) / factorPeriod;
    BondFixedSecurity bondExpected = new BondFixedSecurity(nominal, coupon, spotTime1, accruedInterest, factorToNextCoupon, YIELD_CONVENTION_G, COUPON_PER_YEAR_G, REPO_CURVE_NAME, "");
    assertEquals("Bond Fixed Security Definition to derivative", bondExpected.getAccrualFactorToNextCoupon(), bondConverted.getAccrualFactorToNextCoupon(), 1.0E-10);
    assertEquals("Bond Fixed Security Definition to derivative", bondExpected.getAccruedInterest(), bondConverted.getAccruedInterest(), 1.0E-10);
    assertEquals("Bond Fixed Security Definition to derivative", bondExpected.getYieldConvention(), bondConverted.getYieldConvention());
  }

  @Test
  public void toDerivativeUKTExCoupon() {
    final ZonedDateTime referenceDate2 = DateUtils.getUTCDate(2011, 9, 2); // Ex-dividend is 30-Aug-2011
    BondFixedSecurity bondConverted = BOND_SECURITY_DEFINITION_G.toDerivative(referenceDate2, CURVES_NAME);
    AnnuityPaymentFixedDefinition nominalDefinition = (AnnuityPaymentFixedDefinition) BOND_SECURITY_DEFINITION_G.getNominal();
    AnnuityCouponFixedDefinition couponDefinition = BOND_SECURITY_DEFINITION_G.getCoupon();
    ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(referenceDate2, SETTLEMENT_DAYS_G, CALENDAR_G);
    nominalDefinition = nominalDefinition.trimBefore(spotDate);
    couponDefinition = couponDefinition.trimBefore(spotDate);
    CouponFixedDefinition[] couponDefinitionExArray = new CouponFixedDefinition[couponDefinition.getNumberOfPayments()];
    System.arraycopy(couponDefinition.getPayments(), 1, couponDefinitionExArray, 1, couponDefinition.getNumberOfPayments() - 1);
    couponDefinitionExArray[0] = new CouponFixedDefinition(couponDefinition.getNthPayment(0), 0.0);
    AnnuityCouponFixedDefinition couponDefinitionEx = new AnnuityCouponFixedDefinition(couponDefinitionExArray);
    AnnuityPaymentFixed nominal = nominalDefinition.toDerivative(referenceDate2, CURVES_NAME);
    AnnuityCouponFixed coupon = couponDefinitionEx.toDerivative(referenceDate2, CURVES_NAME);
    double spotTime = ACT_ACT.getDayCountFraction(referenceDate2, spotDate);
    double accruedInterest = (DAY_COUNT_G.getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), spotDate, couponDefinition.getNthPayment(0).getAccrualEndDate(), RATE_G,
        COUPON_PER_YEAR_G) - RATE_G / COUPON_PER_YEAR_G) * NOTIONAL_G;
    double factorSpot = DAY_COUNT_G.getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), spotDate, couponDefinition.getNthPayment(0).getAccrualEndDate(), RATE_G,
        COUPON_PER_YEAR_G);
    double factorPeriod = DAY_COUNT_G.getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), couponDefinition.getNthPayment(0).getAccrualEndDate(), couponDefinition
        .getNthPayment(0).getAccrualEndDate(), RATE_G, COUPON_PER_YEAR_G);
    double factorToNextCoupon = (factorPeriod - factorSpot) / factorPeriod;
    BondFixedSecurity bondExpected = new BondFixedSecurity(nominal, coupon, spotTime, accruedInterest, factorToNextCoupon, YIELD_CONVENTION_G, COUPON_PER_YEAR_G, REPO_CURVE_NAME, "");
    assertEquals("Bond Fixed Security Definition to derivative", bondExpected.getAccrualFactorToNextCoupon(), bondConverted.getAccrualFactorToNextCoupon(), 1.0E-10);
    assertEquals("Bond Fixed Security Definition to derivative", bondExpected.getAccruedInterest(), bondConverted.getAccruedInterest(), 1.0E-10);
    assertEquals("Bond Fixed Security Definition to derivative", bondExpected.getYieldConvention(), bondConverted.getYieldConvention());
  }

}
