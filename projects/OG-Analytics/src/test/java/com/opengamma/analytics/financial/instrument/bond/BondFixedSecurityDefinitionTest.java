/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityPaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.ActualActualICMA;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the construction of fixed coupon bond security Definition and conversion to Derivative.
 */
@Test(groups = TestGroup.UNIT)
public class BondFixedSecurityDefinitionTest {

  //Semi-annual 2Y
  private static final Currency CUR = Currency.EUR;
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final int COUPON_PER_YEAR = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final String ISSUER_NAME = "Issuer";
  private static final DayCount DAY_COUNT = DayCounts.ACT_ACT_ISDA;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM = false;
  private static final Period BOND_TENOR = Period.ofYears(2);
  private static final int SETTLEMENT_DAYS = 2;
  private static final ZonedDateTime START_ACCRUAL_DATE = DateUtils.getUTCDate(2011, 7, 13);
  private static final ZonedDateTime MATURITY_DATE = START_ACCRUAL_DATE.plus(BOND_TENOR);
  private static final double RATE = 0.0325;
  private static final YieldConvention STREET_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final double NOTIONAL = 1.0;
  private static final BondFixedSecurityDefinition BOND_SECURITY_DEFINITION = BondFixedSecurityDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR,
      RATE, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, STREET_CONVENTION, IS_EOM, ISSUER_NAME);
  // to derivatives: common
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final String CREDIT_CURVE_NAME = "Credit";
  private static final String REPO_CURVE_NAME = "Repo";
  private static final String[] CURVES_NAME = {CREDIT_CURVE_NAME, REPO_CURVE_NAME };
  //  private static final YieldCurveBundle CURVES = TestsDataSets.createCurves1();
  private static final ZonedDateTime REFERENCE_DATE_1 = DateUtils.getUTCDate(2011, 8, 18);

  private static final double TOLERANCE_PRICE = 1.0E-10;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    BondFixedSecurityDefinition.from(null, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, STREET_CONVENTION,
        IS_EOM, ISSUER_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMaturity() {
    BondFixedSecurityDefinition.from(CUR, null, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, STREET_CONVENTION, IS_EOM,
        ISSUER_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStart() {
    BondFixedSecurityDefinition.from(CUR, MATURITY_DATE, null, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, STREET_CONVENTION, IS_EOM,
        ISSUER_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPeriod() {
    BondFixedSecurityDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, null, RATE, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, STREET_CONVENTION, IS_EOM,
        ISSUER_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendar() {
    BondFixedSecurityDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, null, DAY_COUNT, BUSINESS_DAY, STREET_CONVENTION,
        IS_EOM, ISSUER_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDayCount() {
    BondFixedSecurityDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR, null, BUSINESS_DAY, STREET_CONVENTION,
        IS_EOM, ISSUER_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBusinessDay() {
    BondFixedSecurityDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, null, STREET_CONVENTION, IS_EOM,
        ISSUER_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullYield() {
    BondFixedSecurityDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, null, IS_EOM,
        ISSUER_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPositiveNominal() {
    final AnnuityCouponFixedDefinition coupon = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, START_ACCRUAL_DATE, MATURITY_DATE, PAYMENT_TENOR, true, true,
        CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, 1.0, RATE, false);
    final AnnuityPaymentFixedDefinition nominal = new AnnuityPaymentFixedDefinition(new PaymentFixedDefinition[] {new PaymentFixedDefinition(CUR, MATURITY_DATE, -1.0) }, CALENDAR);
    new BondFixedSecurityDefinition(nominal, coupon, 0, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, STREET_CONVENTION, COUPON_PER_YEAR, IS_EOM, ISSUER_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPositiveCoupon() {
    final AnnuityCouponFixedDefinition coupon = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, START_ACCRUAL_DATE, MATURITY_DATE, PAYMENT_TENOR, true, true,
        CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, 1.0, RATE, true);
    final AnnuityPaymentFixedDefinition nominal = new AnnuityPaymentFixedDefinition(new PaymentFixedDefinition[] {new PaymentFixedDefinition(CUR, MATURITY_DATE, 1.0) }, CALENDAR);
    new BondFixedSecurityDefinition(nominal, coupon, 0, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, STREET_CONVENTION, COUPON_PER_YEAR, IS_EOM, ISSUER_NAME);
  }

  @Test
  public void testGetters() {
    assertEquals(SETTLEMENT_DAYS, BOND_SECURITY_DEFINITION.getSettlementDays());
    assertEquals(DAY_COUNT, BOND_SECURITY_DEFINITION.getDayCount());
    assertEquals(STREET_CONVENTION, BOND_SECURITY_DEFINITION.getYieldConvention());
    assertEquals(COUPON_PER_YEAR, BOND_SECURITY_DEFINITION.getCouponPerYear());
    assertEquals(0, BOND_SECURITY_DEFINITION.getExCouponDays()); //Default
    assertEquals(CUR, BOND_SECURITY_DEFINITION.getCurrency());
    final AnnuityCouponFixedDefinition coupon = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, START_ACCRUAL_DATE, MATURITY_DATE, PAYMENT_TENOR, true, true,
        CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, 1.0, RATE, false);
    assertEquals(coupon, BOND_SECURITY_DEFINITION.getCoupons());
    final AnnuityDefinition<PaymentFixedDefinition> nominal = new AnnuityDefinition<>(new PaymentFixedDefinition[] {new PaymentFixedDefinition(CUR,
        BUSINESS_DAY.adjustDate(CALENDAR, MATURITY_DATE), 1.0) }, CALENDAR);
    assertEquals(nominal.getCurrency(), BOND_SECURITY_DEFINITION.getNominal().getCurrency());
    assertEquals(nominal.getNthPayment(0).getPaymentDate(), BOND_SECURITY_DEFINITION.getNominal().getNthPayment(0).getPaymentDate());
    assertEquals(nominal.getNthPayment(0).getReferenceAmount(), BOND_SECURITY_DEFINITION.getNominal().getNthPayment(0).getReferenceAmount());
  }

  @Test
  public void testDates() {
    final BondFixedSecurityDefinition BOND_DEFINITION = BondFixedSecurityDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS,
        CALENDAR, DAY_COUNT, BUSINESS_DAY, STREET_CONVENTION, IS_EOM, ISSUER_NAME);
    final ZonedDateTime[] expectedPaymentDates = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 1, 13), DateUtils.getUTCDate(2012, 7, 13),
      DateUtils.getUTCDate(2013, 1, 14), DateUtils.getUTCDate(2013, 7, 15) };
    final ZonedDateTime[] expectedStartDates = new ZonedDateTime[] {DateUtils.getUTCDate(2011, 7, 13), DateUtils.getUTCDate(2012, 1, 13),
      DateUtils.getUTCDate(2012, 7, 13), DateUtils.getUTCDate(2013, 1, 13) };
    final ZonedDateTime[] expectedEndDates = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 1, 13), DateUtils.getUTCDate(2012, 7, 13),
      DateUtils.getUTCDate(2013, 1, 13), DateUtils.getUTCDate(2013, 7, 13) };
    for (int loopcpn = 0; loopcpn < BOND_DEFINITION.getCoupons().getNumberOfPayments(); loopcpn++) {
      assertEquals("Payment " + loopcpn, expectedPaymentDates[loopcpn], BOND_DEFINITION.getCoupons().getNthPayment(loopcpn).getPaymentDate());
      assertEquals("Start accrual " + loopcpn, expectedStartDates[loopcpn], BOND_DEFINITION.getCoupons().getNthPayment(loopcpn).getAccrualStartDate());
      assertEquals("End accrual " + loopcpn, expectedEndDates[loopcpn], BOND_DEFINITION.getCoupons().getNthPayment(loopcpn).getAccrualEndDate());
    }
  }

  @Test
  public void toDerivativeUST() {
    final BondFixedSecurity bondConverted = BOND_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE_1);
    AnnuityPaymentFixedDefinition nominalDefinition = (AnnuityPaymentFixedDefinition) BOND_SECURITY_DEFINITION.getNominal();
    AnnuityCouponFixedDefinition couponDefinition = BOND_SECURITY_DEFINITION.getCoupons();
    final ZonedDateTime spotDate1 = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_1, SETTLEMENT_DAYS, CALENDAR);
    nominalDefinition = nominalDefinition.trimBefore(spotDate1);
    couponDefinition = couponDefinition.trimBefore(spotDate1);

    final AnnuityPaymentFixed nominal = nominalDefinition.toDerivative(REFERENCE_DATE_1);
    final AnnuityCouponFixed coupon = couponDefinition.toDerivative(REFERENCE_DATE_1);
    final double spotTime1 = ACT_ACT.getDayCountFraction(REFERENCE_DATE_1, spotDate1);
    final double accruedInterest = DAY_COUNT.getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), spotDate1, couponDefinition.getNthPayment(0)
        .getAccrualEndDate(), RATE, COUPON_PER_YEAR)
        * NOTIONAL;
    final double factorSpot = DAY_COUNT.getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), spotDate1, couponDefinition.getNthPayment(0)
        .getAccrualEndDate(), RATE, COUPON_PER_YEAR);
    final double factorPeriod = DAY_COUNT.getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), couponDefinition.getNthPayment(0)
        .getAccrualEndDate(), couponDefinition.getNthPayment(0).getAccrualEndDate(), RATE, COUPON_PER_YEAR);
    final double factorToNextCoupon = (factorPeriod - factorSpot) / factorPeriod;
    final BondFixedSecurity bondExpected = new BondFixedSecurity(nominal, coupon, spotTime1, accruedInterest, factorToNextCoupon, STREET_CONVENTION, COUPON_PER_YEAR,
        "");
    assertEquals("Bond Fixed Security Definition to derivative", bondConverted.getFactorToNextCoupon(), bondExpected.getFactorToNextCoupon(), 1.0E-10);
    assertEquals("Bond Fixed Security Definition to derivative", bondConverted.getAccruedInterest(), bondExpected.getAccruedInterest(), 1.0E-10);
    assertEquals("Bond Fixed Security Definition to derivative", bondConverted.getYieldConvention(), bondExpected.getYieldConvention());
    assertTrue("Bond Fixed Security Definition to derivative", bondConverted.equals(bondExpected));
    final BondFixedSecurity bondConvertedDate = BOND_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE_1, spotDate1);
    assertTrue("Bond Fixed Security Definition to derivative", bondConverted.equals(bondConvertedDate));
  }

  // UKT 5 09/07/14 - ISIN-GB0031829509
  private static final String ISSUER = "UK";
  private static final String REPO_TYPE = "General collateral";
  private static final Currency CUR_G = Currency.GBP;
  private static final Period PAYMENT_TENOR_G = Period.ofMonths(6);
  private static final int COUPON_PER_YEAR_G = 2;
  private static final Calendar CALENDAR_G = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_G = DayCounts.ACT_ACT_ICMA; // To check
  private static final BusinessDayConvention BUSINESS_DAY_G = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM_G = false;
  private static final Period BOND_TENOR_G = Period.ofYears(12);
  private static final int SETTLEMENT_DAYS_G = 2;
  private static final int EX_DIVIDEND_DAYS_G = 7;
  private static final ZonedDateTime START_ACCRUAL_DATE_G = DateUtils.getUTCDate(2002, 9, 7);
  private static final ZonedDateTime MATURITY_DATE_G = START_ACCRUAL_DATE_G.plus(BOND_TENOR_G);
  private static final double RATE_G = 0.0500;
  private static final double NOTIONAL_G = 100;
  private static final YieldConvention YIELD_CONVENTION_G = YieldConventionFactory.INSTANCE.getYieldConvention("UK:BUMP/DMO METHOD");
  private static final BondFixedSecurityDefinition BOND_SECURITY_DEFINITION_G = BondFixedSecurityDefinition.from(CUR_G, MATURITY_DATE_G, START_ACCRUAL_DATE_G,
      PAYMENT_TENOR_G, RATE_G, SETTLEMENT_DAYS_G, NOTIONAL_G, EX_DIVIDEND_DAYS_G, CALENDAR_G, DAY_COUNT_G, BUSINESS_DAY_G, YIELD_CONVENTION_G, IS_EOM_G, ISSUER,
      REPO_TYPE);

  @Test
  public void testGettersUKT() {
    assertEquals(SETTLEMENT_DAYS_G, BOND_SECURITY_DEFINITION_G.getSettlementDays());
    assertEquals(NOTIONAL_G, BOND_SECURITY_DEFINITION_G.getNominal().getNthPayment(0).getReferenceAmount());
    assertEquals(EX_DIVIDEND_DAYS_G, BOND_SECURITY_DEFINITION_G.getExCouponDays());
  }

  @Test
  public void toDerivativeUKT() {
    final BondFixedSecurity bondConverted = BOND_SECURITY_DEFINITION_G.toDerivative(REFERENCE_DATE_1);
    AnnuityPaymentFixedDefinition nominalDefinition = (AnnuityPaymentFixedDefinition) BOND_SECURITY_DEFINITION_G.getNominal();
    AnnuityCouponFixedDefinition couponDefinition = BOND_SECURITY_DEFINITION_G.getCoupons();
    final ZonedDateTime spotDate1 = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_1, SETTLEMENT_DAYS_G, CALENDAR_G);
    nominalDefinition = nominalDefinition.trimBefore(spotDate1);
    couponDefinition = couponDefinition.trimBefore(spotDate1);

    final AnnuityPaymentFixed nominal = nominalDefinition.toDerivative(REFERENCE_DATE_1);
    final AnnuityCouponFixed coupon = couponDefinition.toDerivative(REFERENCE_DATE_1);
    final double spotTime1 = ACT_ACT.getDayCountFraction(REFERENCE_DATE_1, spotDate1);
    final double accruedInterest = DAY_COUNT_G.getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), spotDate1, couponDefinition.getNthPayment(0)
        .getAccrualEndDate(), RATE_G, COUPON_PER_YEAR_G)
        * NOTIONAL_G;
    final double factorSpot = DAY_COUNT_G.getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), spotDate1, couponDefinition.getNthPayment(0)
        .getAccrualEndDate(), RATE_G, COUPON_PER_YEAR_G);
    final double factorPeriod = DAY_COUNT_G.getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), couponDefinition.getNthPayment(0)
        .getAccrualEndDate(), couponDefinition.getNthPayment(0).getAccrualEndDate(), RATE_G, COUPON_PER_YEAR_G);
    final double factorToNextCoupon = (factorPeriod - factorSpot) / factorPeriod;
    final BondFixedSecurity bondExpected = new BondFixedSecurity(nominal, coupon, spotTime1, accruedInterest, factorToNextCoupon, YIELD_CONVENTION_G, COUPON_PER_YEAR_G,
        "");
    assertEquals("Bond Fixed Security Definition to derivative", bondExpected.getFactorToNextCoupon(), bondConverted.getFactorToNextCoupon(), 1.0E-10);
    assertEquals("Bond Fixed Security Definition to derivative", bondExpected.getAccruedInterest(), bondConverted.getAccruedInterest(), 1.0E-10);
    assertEquals("Bond Fixed Security Definition to derivative", bondExpected.getYieldConvention(), bondConverted.getYieldConvention());
  }

  @Test
  public void toDerivativeUKTExCoupon() {
    final ZonedDateTime referenceDate2 = DateUtils.getUTCDate(2011, 9, 2); // Ex-dividend is 30-Aug-2011
    final BondFixedSecurity bondConverted = BOND_SECURITY_DEFINITION_G.toDerivative(referenceDate2);
    AnnuityPaymentFixedDefinition nominalDefinition = (AnnuityPaymentFixedDefinition) BOND_SECURITY_DEFINITION_G.getNominal();
    AnnuityCouponFixedDefinition couponDefinition = BOND_SECURITY_DEFINITION_G.getCoupons();
    final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(referenceDate2, SETTLEMENT_DAYS_G, CALENDAR_G);
    nominalDefinition = nominalDefinition.trimBefore(spotDate);
    couponDefinition = couponDefinition.trimBefore(spotDate);
    final CouponFixedDefinition[] couponDefinitionExArray = new CouponFixedDefinition[couponDefinition.getNumberOfPayments()];
    System.arraycopy(couponDefinition.getPayments(), 1, couponDefinitionExArray, 1, couponDefinition.getNumberOfPayments() - 1);
    couponDefinitionExArray[0] = new CouponFixedDefinition(couponDefinition.getNthPayment(0), 0.0);
    final AnnuityCouponFixedDefinition couponDefinitionEx = new AnnuityCouponFixedDefinition(couponDefinitionExArray, CALENDAR_G);
    final AnnuityPaymentFixed nominal = nominalDefinition.toDerivative(referenceDate2);
    final AnnuityCouponFixed coupon = couponDefinitionEx.toDerivative(referenceDate2);
    final double spotTime = ACT_ACT.getDayCountFraction(referenceDate2, spotDate);
    final double accruedInterest = (DAY_COUNT_G.getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), spotDate, couponDefinition.getNthPayment(0)
        .getAccrualEndDate(), RATE_G, COUPON_PER_YEAR_G) - RATE_G / COUPON_PER_YEAR_G)
        * NOTIONAL_G;
    final double factorSpot = DAY_COUNT_G.getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), spotDate, couponDefinition.getNthPayment(0)
        .getAccrualEndDate(), RATE_G, COUPON_PER_YEAR_G);
    final double factorPeriod = DAY_COUNT_G.getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), couponDefinition.getNthPayment(0)
        .getAccrualEndDate(), couponDefinition.getNthPayment(0).getAccrualEndDate(), RATE_G, COUPON_PER_YEAR_G);
    final double factorToNextCoupon = (factorPeriod - factorSpot) / factorPeriod;
    final BondFixedSecurity bondExpected = new BondFixedSecurity(nominal, coupon, spotTime, accruedInterest, factorToNextCoupon, YIELD_CONVENTION_G, COUPON_PER_YEAR_G,
        "");
    assertEquals("Bond Fixed Security Definition to derivative", bondExpected.getFactorToNextCoupon(), bondConverted.getFactorToNextCoupon(), 1.0E-10);
    assertEquals("Bond Fixed Security Definition to derivative", bondExpected.getAccruedInterest(), bondConverted.getAccruedInterest(), 1.0E-10);
    assertEquals("Bond Fixed Security Definition to derivative", bondExpected.getYieldConvention(), bondConverted.getYieldConvention());
  }

  /** Test the toDerivative for each business date around the ex-dividend period. */
  @Test
  public void toDerivativeUKTAllDate() {
    ZonedDateTime referenceDate2 = DateUtils.getUTCDate(2011, 8, 1); // Ex-dividend is 30-Aug-2011
    int nbDateTest = 40; // 2M
    double[] accruedInterest = new double[nbDateTest];
    for (int loopref = 0; loopref < nbDateTest; loopref++) {
      referenceDate2 = ScheduleCalculator.getAdjustedDate(referenceDate2, 1, CALENDAR_G);
      final BondFixedSecurity bondConverted = BOND_SECURITY_DEFINITION_G.toDerivative(referenceDate2);
      accruedInterest[loopref] = bondConverted.getAccruedInterest();
    }
    int indexJump = 19;
    for (int loopref = 0; loopref < nbDateTest - 1; loopref++) {
      if (loopref != indexJump) {
        assertTrue("Bond Fixed Security Definition to derivative - " + loopref,
            accruedInterest[loopref + 1] - accruedInterest[loopref] < NOTIONAL_G * RATE_G / 360 * 3); // 3 days of accrued
      }
    }
  }

  // DBR 1 1/2 02/15/23 - ISIN: DE0001102309 - Check the long first coupon

  private static final Currency EUR = Currency.EUR;
  private static final ActualActualICMA DAY_COUNT_ACTACTICMA = new ActualActualICMA();
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventions.FOLLOWING;

  private static final String ISSUER_DE = "BUNDESREPUB. DEUTSCHLAND";
  private static final YieldConvention YIELD_CONVENTION_GERMANY = SimpleYieldConvention.GERMAN_BOND;
  private static final int SETTLEMENT_DAYS_DE = 3;
  private static final Period PAYMENT_TENOR_DE = Period.ofMonths(12);
  private static final int COUPON_PER_YEAR_DE = 1;
  private static final ZonedDateTime BOND_MATURITY_DE = DateUtils.getUTCDate(2023, 2, 15);
  private static final ZonedDateTime BOND_START_DE = DateUtils.getUTCDate(2013, 1, 18);
  private static final ZonedDateTime BOND_FIRSTCPN_DE = DateUtils.getUTCDate(2014, 2, 15);
  private static final double RATE_DE = 0.0150;
  private static final BondFixedSecurityDefinition BOND_DE_SECURITY_DEFINITION = BondFixedSecurityDefinition.from(EUR, BOND_START_DE, BOND_FIRSTCPN_DE, BOND_MATURITY_DE, PAYMENT_TENOR_DE, RATE_DE,
      SETTLEMENT_DAYS_DE, CALENDAR, DAY_COUNT_ACTACTICMA, FOLLOWING, YIELD_CONVENTION_GERMANY, IS_EOM, ISSUER_DE);

  @Test
  public void longFirstCouponDE() {
    final double accrualFirstComputed = BOND_DE_SECURITY_DEFINITION.getCoupons().getNthPayment(0).getPaymentYearFraction();
    assertTrue("Bond Fixed Security - long first coupon ActActICMA", accrualFirstComputed > 1);
    final double accrualFirstExpected = DAY_COUNT_ACTACTICMA.getAccruedInterest(BOND_START_DE, BOND_FIRSTCPN_DE, BOND_FIRSTCPN_DE, 1.0, COUPON_PER_YEAR_DE, StubType.LONG_START);
    assertEquals("Bond Fixed Security - long first coupon ActActICMA", accrualFirstExpected, accrualFirstComputed, TOLERANCE_PRICE);
  }

  // MDLZ 5 3/8 12/11/14 - ISIN: XS0417033007 - Check the long first coupon and accrued

  private static final Currency GBP = Currency.GBP;

  private static final String ISSUER_MON = " MONDELEZ INTERNATIONAL";
  private static final YieldConvention US_STREET = SimpleYieldConvention.US_STREET;
  private static final int SETTLEMENT_DAYS_MON = 3;
  private static final Period PAYMENT_TENOR_MON = Period.ofMonths(6);
  //  private static final int COUPON_PER_YEAR_MON = 2;
  private static final ZonedDateTime BOND_MATURITY_MON = DateUtils.getUTCDate(2014, 12, 11);
  private static final ZonedDateTime BOND_START_MON = DateUtils.getUTCDate(2009, 3, 11);
  private static final ZonedDateTime BOND_FIRSTCPN_MON = DateUtils.getUTCDate(2009, 12, 11);
  private static final double RATE_MON = 0.05375;
  private static final BondFixedSecurityDefinition BOND_MON_SECURITY_DEFINITION = BondFixedSecurityDefinition.from(GBP, BOND_START_MON, BOND_FIRSTCPN_MON, BOND_MATURITY_MON, PAYMENT_TENOR_MON,
      RATE_MON, SETTLEMENT_DAYS_MON, CALENDAR, DAY_COUNT_ACTACTICMA, FOLLOWING, US_STREET, IS_EOM, ISSUER_MON);

  @Test
  public void longFirstCouponMon() {
    final double notional = 1000000;
    final double accrualFirstExpected = 40460.17;
    final double accrualFirstComputed = BOND_MON_SECURITY_DEFINITION.getCoupons().getNthPayment(0).getAmount();
    assertEquals("Bond Fixed Security - XS0417033007 - accrued 1st cpn", accrualFirstExpected, accrualFirstComputed * notional, 1.0E-2);
    final double accrualSecondExpected = 26875.00;
    for (int loopcpn = 1; loopcpn < BOND_MON_SECURITY_DEFINITION.getCoupons().getNumberOfPayments(); loopcpn++) {
      assertEquals("Bond Fixed Security - XS0417033007 - accrued 1st cpn", accrualSecondExpected, BOND_MON_SECURITY_DEFINITION.getCoupons().getNthPayment(loopcpn).getAmount() * notional, 1.0E-2);
    }
  }

}
