package com.opengamma.analytics.financial.provider.calculator.generic;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarTarget;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.analytics.financial.interestrate.ContinuousInterestRate;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

public class LastTimeIndexCalculatorTest {

  /** Generators */
  private static final Calendar TOK = new CalendarTarget("TARGET");
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor JPY6MLIBOR3M = GENERATOR_SWAP_MASTER.getGenerator(GeneratorSwapFixedIborMaster.JPY6MLIBOR3M, TOK);
  private static final GeneratorSwapFixedIbor JPY6MLIBOR6M = GENERATOR_SWAP_MASTER.getGenerator(GeneratorSwapFixedIborMaster.JPY6MLIBOR6M, TOK);
  private static final IborIndex JPYLIBOR3M = JPY6MLIBOR3M.getIborIndex();
  private static final IborIndex JPYLIBOR6M = JPY6MLIBOR6M.getIborIndex();
  private static final GeneratorSwapIborIborMaster GENERATOR_BS_MASTER = GeneratorSwapIborIborMaster.getInstance();
  private static final GeneratorSwapIborIbor JPYLIBOR6MLIBOR3M = GENERATOR_BS_MASTER.getGenerator(GeneratorSwapIborIborMaster.JPYLIBOR6MLIBOR3M, TOK);
  /** Dates */
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2014, 7, 1);
  /** Instruments */
  private static final GeneratorAttributeIR ATTRIBUTE_1 = new GeneratorAttributeIR(Period.ofYears(40));
  private static final SwapFixedIborDefinition IRS_1_DEFINIITON = JPY6MLIBOR6M.generateInstrument(REFERENCE_DATE, 0.01, 1, ATTRIBUTE_1);
  private static final Swap<?, ?> IRS_1 = IRS_1_DEFINIITON.toDerivative(REFERENCE_DATE);
  private static final SwapIborIborDefinition BS_1_DEFINITION = JPYLIBOR6MLIBOR3M.generateInstrument(REFERENCE_DATE, 0.0010, 1, ATTRIBUTE_1);
  private static final Swap<?, ?> BS_1 = BS_1_DEFINITION.toDerivative(REFERENCE_DATE);
  /** Calculators with different indexes **/
  private static final LastTimeIndexCalculator LAST_TIME_JPYLIBOR3M = new LastTimeIndexCalculator(JPYLIBOR3M);
  private static final LastTimeIndexCalculator LAST_TIME_JPYLIBOR6M = new LastTimeIndexCalculator(JPYLIBOR6M);

  private static final double TOLERANCE_TIME = 1.0E-10;
  
  @Test
  public void testNonBasisSwap() {
    double lastTimeComputed = IRS_1.accept(LAST_TIME_JPYLIBOR6M);
    double lastTimeExpected = ((CouponIbor) (IRS_1.getSecondLeg().getNthPayment(IRS_1.getSecondLeg().getNumberOfPayments() - 1))).getFixingPeriodEndTime();
    assertEquals("LastFixingTimeIndexCalculator: IRS", lastTimeComputed, lastTimeExpected, TOLERANCE_TIME);
  }
  
  @Test
  public void testBasisSwap() {
    // The reference date has been chosen such that the last date of the two legs are different.
    double lastTimeComputedE3Swap = BS_1.accept(LAST_TIME_JPYLIBOR3M);
    double lastTimeComputedE3Leg = BS_1.getFirstLeg().accept(LAST_TIME_JPYLIBOR3M);
    double lastTimeComputedE6Swap = BS_1.accept(LAST_TIME_JPYLIBOR6M);
    double lastTimeComputedE6Leg = BS_1.getSecondLeg().accept(LAST_TIME_JPYLIBOR6M);
    assertEquals("LastFixingTimeIndexCalculator: basis swap", lastTimeComputedE3Leg, lastTimeComputedE3Swap, TOLERANCE_TIME);
    assertEquals("LastFixingTimeIndexCalculator: basis swap", lastTimeComputedE6Leg, lastTimeComputedE6Swap, TOLERANCE_TIME);
    assertFalse("LastFixingTimeIndexCalculator: basis swap", Math.abs(lastTimeComputedE3Swap - lastTimeComputedE6Swap) < TOLERANCE_TIME);
  }

  private static LastTimeCalculator LDC = LastTimeCalculator.getInstance();
  private static final Currency CUR = Currency.EUR;

  @Test
  public void testCash() {
    final double t = 7 / 365.0;
    final Cash cash = new Cash(CUR, 1 / 365.0, t, 100, 0.0445, 5.0 / 365);
    assertEquals(t, cash.accept(LDC), 1e-12);
  }

  @Test
  public void testFRA() {
    final double paymentTime = 0.5;
    final double paymentYearFraction = 30. / 360;
    final double fixingTime = paymentTime - 2. / 365;
    final double fixingPeriodStartTime = paymentTime;
    final double fixingPeriodEndTime = 7. / 12;
    final double fixingYearFraction = 31. / 365;
    final IborIndex index = new IborIndex(CUR, Period.ofMonths(1), 2, DayCounts.ACT_365,
        BusinessDayConventions.FOLLOWING, true, "Ibor");
    final ForwardRateAgreement fra = new ForwardRateAgreement(CUR, paymentTime, paymentYearFraction, 1, index, fixingTime, fixingPeriodStartTime, fixingPeriodEndTime, fixingYearFraction,
        0.05);

    assertEquals(fixingPeriodEndTime, fra.accept(LDC), 1e-12);
  }

  @Test
  public void testFutures() {
    final IborIndex iborIndex = new IborIndex(CUR, Period.ofMonths(3), 2, DayCounts.ACT_365,
        BusinessDayConventions.FOLLOWING, true, "Ibor");
    final double lastTradingTime = 1.473;
    final double fixingPeriodStartTime = 1.467;
    final double fixingPeriodEndTime = 1.75;
    final double fixingPeriodAccrualFactor = 0.267;
    final double paymentAccrualFactor = 0.25;
    final double refrencePrice = 0.0;
    final InterestRateFutureSecurity sec = new InterestRateFutureSecurity(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodAccrualFactor, 1.0, paymentAccrualFactor,
        "S");
    final InterestRateFutureTransaction ir = new InterestRateFutureTransaction(sec, refrencePrice, 1);
    assertEquals(fixingPeriodEndTime, ir.accept(LDC), 1e-12);
  }

  @Test
  public void testFixedCouponAnnuity() {
    final AnnuityCouponFixed annuity = new AnnuityCouponFixed(CUR, new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }, 1.0, 1.0, true);
    assertEquals(10, annuity.accept(LDC), 1e-12);
  }

  @Test
  public void testBond() {
    final double mat = 1.0;
    final AnnuityPaymentFixed nominal = new AnnuityPaymentFixed(new PaymentFixed[] {new PaymentFixed(CUR, mat, 1.0) });
    final AnnuityCouponFixed coupon = new AnnuityCouponFixed(CUR, new double[] {0.5, mat }, 0.03, false);
    final BondFixedSecurity bond = new BondFixedSecurity(nominal, coupon, 0, 0, 0.5, SimpleYieldConvention.TRUE, 2, "Issuer");
    assertEquals(mat, bond.accept(LDC), 1e-12);
  }

  @Test
  public void testDepositZero() {
    final double endTime = 0.03;
    final DepositZero deposit = new DepositZero(Currency.USD, 0, endTime, 100, 100, 0.25, new ContinuousInterestRate(0.03), 2);
    assertEquals(deposit.accept(LDC), endTime, 0);
  }

  @Test
  public void testForex() {
    final double t = 0.124;
    final Forex fx = new Forex(new PaymentFixed(Currency.AUD, t, -100), new PaymentFixed(Currency.USD, t, 100));
    assertEquals(fx.accept(LDC), t, 0);
  }
}
