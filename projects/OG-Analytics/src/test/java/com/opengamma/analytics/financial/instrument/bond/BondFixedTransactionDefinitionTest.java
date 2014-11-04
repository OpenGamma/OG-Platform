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

import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.AccruedInterestCalculator;
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
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BondFixedTransactionDefinitionTest {
  //Issue: Semi-annual 2Y
  private static final Currency CUR = Currency.EUR;
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
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
  private static final BondFixedSecurityDefinition BOND_SECURITY_DEFINITION = BondFixedSecurityDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR,
      RATE, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM, ISSUER_NAME);
  // Transaction
  private static final double PRICE_DIRTY = 0.90;
  private static final double YIELD = 0.05;
  private static final double PRICE_CLEAN = 0.95;
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 8, 18);
  private static final double QUANTITY = 100000000; //100m
  private static final BondFixedTransactionDefinition BOND_TRANSACTION_DEFINITION = new BondFixedTransactionDefinition(BOND_SECURITY_DEFINITION, QUANTITY,
      SETTLEMENT_DATE, PRICE_DIRTY);
  // to derivatives: common
  private static final String CREDIT_CURVE_NAME = "Credit";
  private static final String REPO_CURVE_NAME = "Repo";
  private static final String[] CURVES_NAME = {CREDIT_CURVE_NAME, REPO_CURVE_NAME };
  private static final ZonedDateTime REFERENCE_DATE_1 = DateUtils.getUTCDate(2011, 8, 17);
  private static final BondSecurityDiscountingMethod METHOD_BOND_FIXED = BondSecurityDiscountingMethod.getInstance();
  private static final double TOLERANCE_PRICE = 1.0E-8;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying() {
    new BondFixedTransactionDefinition(null, QUANTITY, SETTLEMENT_DATE, PRICE_DIRTY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlyingYield() {
    BondFixedTransactionDefinition.fromYield(null, QUANTITY, SETTLEMENT_DATE, PRICE_DIRTY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSettle() {
    new BondFixedTransactionDefinition(BOND_SECURITY_DEFINITION, QUANTITY, null, PRICE_DIRTY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSettleYield() {
    BondFixedTransactionDefinition.fromYield(BOND_SECURITY_DEFINITION, QUANTITY, null, YIELD);
  }

  @Test
  public void testGetters() {
    assertEquals(PRICE_DIRTY, BOND_TRANSACTION_DEFINITION.getPrice());
    assertEquals(QUANTITY, BOND_TRANSACTION_DEFINITION.getQuantity());
    assertEquals(SETTLEMENT_DATE, BOND_TRANSACTION_DEFINITION.getSettlementDate());
    assertEquals(BOND_SECURITY_DEFINITION, BOND_TRANSACTION_DEFINITION.getUnderlyingBond());
    final double expectedAccrued = 0.195652174 * RATE / 2; //36 days out of 184 in Actual/Actual ICMA.
    assertEquals(expectedAccrued, BOND_TRANSACTION_DEFINITION.getAccruedInterestAtSettlement(), 1E-6);
    assertEquals(DateUtils.getUTCDate(2011, 7, 13), BOND_TRANSACTION_DEFINITION.getPreviousAccrualDate());
    assertEquals(DateUtils.getUTCDate(2012, 1, 13), BOND_TRANSACTION_DEFINITION.getNextAccrualDate());
  }

  @Test
  public void ofYield() {
    BondFixedTransactionDefinition bondOfYieldTransactionDefinition = BondFixedTransactionDefinition.fromYield(BOND_SECURITY_DEFINITION, QUANTITY, SETTLEMENT_DATE, YIELD);
    BondFixedSecurity bondOfYieldSecurity = bondOfYieldTransactionDefinition.getUnderlyingBond().toDerivative(REFERENCE_DATE_1, SETTLEMENT_DATE);
    double cleanPrice = METHOD_BOND_FIXED.cleanPriceFromYield(bondOfYieldSecurity, YIELD);
    assertEquals("Bond transaction: ofYield", cleanPrice, bondOfYieldTransactionDefinition.getPrice(), TOLERANCE_PRICE);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void toDerivativesDeprecated() {
    final BondFixedSecurity bondSecurityStandard = BOND_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE_1, CURVES_NAME);
    final BondFixedTransaction bondTransaction = BOND_TRANSACTION_DEFINITION.toDerivative(REFERENCE_DATE_1, CURVES_NAME);
    assertEquals("Bond transaction: toDerivative", bondSecurityStandard, bondTransaction.getBondStandard());
    final BondFixedSecurity bondSecurityPurchase = BOND_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE_1, SETTLEMENT_DATE, CURVES_NAME);
    assertEquals("Bond transaction: toDerivative", bondSecurityPurchase.getAccruedInterest(), bondTransaction.getBondTransaction().getAccruedInterest());
    assertEquals("Bond transaction: toDerivative", bondSecurityPurchase.getCouponPerYear(), bondTransaction.getBondTransaction().getCouponPerYear());
    assertEquals("Bond transaction: toDerivative", bondSecurityPurchase.getYieldConvention(), bondTransaction.getBondTransaction().getYieldConvention());
  }

  @Test
  public void toDerivatives() {
    final BondFixedSecurity bondSecurityStandard = BOND_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE_1);
    final BondFixedTransaction bondTransaction = BOND_TRANSACTION_DEFINITION.toDerivative(REFERENCE_DATE_1);
    assertEquals("Bond transaction: toDerivative", bondSecurityStandard, bondTransaction.getBondStandard());
    final BondFixedSecurity bondSecurityPurchase = BOND_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE_1, SETTLEMENT_DATE);
    assertEquals("Bond transaction: toDerivative", bondSecurityPurchase.getAccruedInterest(), bondTransaction.getBondTransaction().getAccruedInterest());
    assertEquals("Bond transaction: toDerivative", bondSecurityPurchase.getCouponPerYear(), bondTransaction.getBondTransaction().getCouponPerYear());
    assertEquals("Bond transaction: toDerivative", bondSecurityPurchase.getYieldConvention(), bondTransaction.getBondTransaction().getYieldConvention());
  }

  // MDLZ 5 3/8 12/11/14 - ISIN: XS0417033007 - Check the long first coupon and accrued

  private static final Currency GBP = Currency.GBP;

  private static final ActualActualICMA DAY_COUNT_ACTACTICMA = new ActualActualICMA();
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventions.FOLLOWING;
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

  private static final double TOLERANCE_ACCRUED = 1.0E-2;

  @Test
  public void longFirstCouponMon() {
    final double notional = 1000000;
    final ZonedDateTime settle090410 = DateUtils.getUTCDate(2009, 04, 10);
    final double accrual090410 = 4429.95;
    BondFixedSecurity bond090410 = BOND_MON_SECURITY_DEFINITION.toDerivative(settle090410, settle090410);
    assertEquals("Bond transaction: accrued", accrual090410, bond090410.getAccruedInterest() * notional, TOLERANCE_ACCRUED);
    final ZonedDateTime settle091010 = DateUtils.getUTCDate(2009, 10, 10);
    final double accrual091010 = 31354.97;
    BondFixedSecurity bond091010 = BOND_MON_SECURITY_DEFINITION.toDerivative(settle091010, settle091010);
    assertEquals("Bond transaction: accrued", accrual091010, bond091010.getAccruedInterest() * notional, TOLERANCE_ACCRUED);
    final ZonedDateTime settle101010 = DateUtils.getUTCDate(2010, 10, 10);
    final double accrual101010 = 17769.81;
    BondFixedSecurity bond101010 = BOND_MON_SECURITY_DEFINITION.toDerivative(settle101010, settle101010);
    assertEquals("Bond transaction: accrued", accrual101010, bond101010.getAccruedInterest() * notional, TOLERANCE_ACCRUED);
  }

  private static final BondFixedSecurityDefinition BOND_UKT_500_20140907 = BondDataSetsGbp.bondUKT5_20140907();

  /** Test the constructor with settlement date in the ex-coupon period. */
  @Test
  public void settlementExCoupon() {
    ZonedDateTime settleDate = DateUtils.getUTCDate(2011, 9, 2);
    BondFixedTransactionDefinition transaction = new BondFixedTransactionDefinition(BOND_UKT_500_20140907, QUANTITY,
        settleDate, PRICE_DIRTY);
    int cpnIndex = transaction.getCouponIndex();
    CouponFixedDefinition cpn = BOND_UKT_500_20140907.getCoupons().getNthPayment(cpnIndex);
    double cpnAccrued = cpn.getAmount() / cpn.getNotional();
    final int nbCoupon = BOND_UKT_500_20140907.getCoupons().getNumberOfPayments();
    double accruedUndajusted = AccruedInterestCalculator.getAccruedInterest(BOND_UKT_500_20140907.getDayCount(), 
        cpnIndex, nbCoupon, transaction.getPreviousAccrualDate(), settleDate, transaction.getNextAccrualDate(), 
        cpn.getRate(), BOND_UKT_500_20140907.getCouponPerYear(), BOND_UKT_500_20140907.isEOM());
    double accruedAtSettle = accruedUndajusted - cpnAccrued;
    double accruedComputed = transaction.getAccruedInterestAtSettlement();
    assertTrue("BondFixedTransactionDefinition: ex-coupon accrued negative", accruedComputed < 0.0);
    assertEquals("BondFixedTransactionDefinition", accruedAtSettle, accruedComputed, TOLERANCE_ACCRUED);
  }

  /** Test the constructor with date around coupon. */
  @Test
  public void settlementUKTAllDate() {
    ZonedDateTime settleDate = DateUtils.getUTCDate(2011, 8, 1);
    int nbSettle = 40;
    double[] accruedSettle = new double[nbSettle];
    for (int loopsettle = 0; loopsettle < nbSettle; loopsettle++) {
      settleDate = ScheduleCalculator.getAdjustedDate(settleDate, 1, CALENDAR);
      BondFixedTransactionDefinition transaction = new BondFixedTransactionDefinition(BOND_UKT_500_20140907, QUANTITY,
          settleDate, PRICE_DIRTY);
      accruedSettle[loopsettle] = transaction.getAccruedInterestAtSettlement();
    }
    int indexJump = 19;
    for (int loopref = 0; loopref < nbSettle - 1; loopref++) {
      if (loopref != indexJump) {
        assertTrue("Bond Fixed Security Definition to derivative - " + loopref,
            accruedSettle[loopref + 1] - accruedSettle[loopref] < 0.05 / 360 * 3); // 3 days of accrued
      }
    }
    @SuppressWarnings("unused")
    int t = 0;

  }

}
