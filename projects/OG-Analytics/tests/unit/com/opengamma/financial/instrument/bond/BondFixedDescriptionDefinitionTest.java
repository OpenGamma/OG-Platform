package com.opengamma.financial.instrument.bond;

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
import com.opengamma.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityPaymentFixedDefinition;
import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

public class BondFixedDescriptionDefinitionTest {

  //Semi-annual 2Y
  private static final Currency CUR = Currency.USD;
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final int PAYMENT_PER_YEAR = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final boolean IS_EOM = false;
  private static final Period BOND_TENOR = Period.ofYears(2);
  private static final int SETTLEMENT_DAYS = 2;
  private static final ZonedDateTime START_ACCRUAL_DATE = DateUtil.getUTCDate(2011, 7, 13);
  private static final ZonedDateTime MATURITY_DATE = START_ACCRUAL_DATE.plus(BOND_TENOR);
  private static final double RATE = 0.0325;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    BondFixedDescriptionDefinition.from(null, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMaturity() {
    BondFixedDescriptionDefinition.from(CUR, null, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStart() {
    BondFixedDescriptionDefinition.from(CUR, MATURITY_DATE, null, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPeriod() {
    BondFixedDescriptionDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, null, RATE, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalendar() {
    BondFixedDescriptionDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, null, DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDayCount() {
    BondFixedDescriptionDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR, null, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBusinessDay() {
    BondFixedDescriptionDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, null, YIELD_CONVENTION, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullYield() {
    BondFixedDescriptionDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, null, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPositiveNominal() {
    AnnuityCouponFixedDefinition coupon = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, START_ACCRUAL_DATE, MATURITY_DATE, PAYMENT_TENOR, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, 1.0,
        RATE, false);
    AnnuityPaymentFixedDefinition nominal = new AnnuityPaymentFixedDefinition(new PaymentFixedDefinition[] {new PaymentFixedDefinition(CUR, MATURITY_DATE, -1.0)});
    new BondFixedDescriptionDefinition(nominal, coupon, 0, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, YIELD_CONVENTION, IS_EOM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPositiveCoupon() {
    AnnuityCouponFixedDefinition coupon = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, START_ACCRUAL_DATE, MATURITY_DATE, PAYMENT_TENOR, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, 1.0,
        RATE, true);
    AnnuityPaymentFixedDefinition nominal = new AnnuityPaymentFixedDefinition(new PaymentFixedDefinition[] {new PaymentFixedDefinition(CUR, MATURITY_DATE, 1.0)});
    new BondFixedDescriptionDefinition(nominal, coupon, 0, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, YIELD_CONVENTION, IS_EOM);
  }

  @Test
  public void testGetters() {
    BondFixedDescriptionDefinition bond = BondFixedDescriptionDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY,
        YIELD_CONVENTION, IS_EOM);
    assertEquals(SETTLEMENT_DAYS, bond.getSettlementDays());
    assertEquals(DAY_COUNT, bond.getDayCount());
    assertEquals(YIELD_CONVENTION, bond.getYieldConvention());
    assertEquals(PAYMENT_PER_YEAR, bond.getCouponPerYear());
    assertEquals(0, bond.getExCouponDays()); //Default
    assertEquals(CUR, bond.getCurrency());
    AnnuityCouponFixedDefinition coupon = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(CUR, START_ACCRUAL_DATE, MATURITY_DATE, PAYMENT_TENOR, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM, 1.0,
        RATE, false);
    assertEquals(coupon, bond.getCoupon());
    AnnuityDefinition<PaymentFixedDefinition> nominal = new AnnuityDefinition<PaymentFixedDefinition>(new PaymentFixedDefinition[] {new PaymentFixedDefinition(CUR, BUSINESS_DAY.adjustDate(CALENDAR,
        MATURITY_DATE), 1.0)});
    assertEquals(nominal.getCurrency(), bond.getNominal().getCurrency());
    assertEquals(nominal.getNthPayment(0).getPaymentDate(), bond.getNominal().getNthPayment(0).getPaymentDate());
    assertEquals(nominal.getNthPayment(0).getAmount(), bond.getNominal().getNthPayment(0).getAmount());
  }

  @Test
  public void testDates() {
    BondFixedDescriptionDefinition bond = BondFixedDescriptionDefinition.from(CUR, MATURITY_DATE, START_ACCRUAL_DATE, PAYMENT_TENOR, RATE, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY,
        YIELD_CONVENTION, IS_EOM);
    ZonedDateTime[] expectedPaymentDates = new ZonedDateTime[] {DateUtil.getUTCDate(2012, 1, 13), DateUtil.getUTCDate(2012, 7, 13), DateUtil.getUTCDate(2013, 1, 14), DateUtil.getUTCDate(2013, 7, 15)};
    ZonedDateTime[] expectedStartDates = new ZonedDateTime[] {DateUtil.getUTCDate(2011, 7, 13), DateUtil.getUTCDate(2012, 1, 13), DateUtil.getUTCDate(2012, 7, 13), DateUtil.getUTCDate(2013, 1, 13)};
    ZonedDateTime[] expectedEndDates = new ZonedDateTime[] {DateUtil.getUTCDate(2012, 1, 13), DateUtil.getUTCDate(2012, 7, 13), DateUtil.getUTCDate(2013, 1, 13), DateUtil.getUTCDate(2013, 7, 13)};
    for (int loopcpn = 0; loopcpn < bond.getCoupon().getNumberOfPayments(); loopcpn++) {
      assertEquals("Payment " + loopcpn, expectedPaymentDates[loopcpn], bond.getCoupon().getNthPayment(loopcpn).getPaymentDate());
      assertEquals("Start accrual " + loopcpn, expectedStartDates[loopcpn], bond.getCoupon().getNthPayment(loopcpn).getAccrualStartDate());
      assertEquals("End accrual " + loopcpn, expectedEndDates[loopcpn], bond.getCoupon().getNthPayment(loopcpn).getAccrualEndDate());
    }
  }
}
