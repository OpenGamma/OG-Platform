/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 *
 */
public class RateReplacingVisitorTest {
  private static final double R1 = 0.05;
  private static final double R2 = 0.04;
  private static final RateReplacingVisitor VISITOR = RateReplacingVisitor.getInstance();
  private static final Currency CUR = Currency.EUR;

  @Test
  public void testBondFixedSecurity() {
    final ZonedDateTime maturityDate = DateUtils.getUTCDate(2020, 1, 1);
    final ZonedDateTime firstAccrualDate = DateUtils.getUTCDate(2010, 1, 1);
    final Period paymentPeriod = Period.ofMonths(6);
    final Calendar calendar = new MondayToFridayCalendar("A");
    final DayCount dayCount = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final BusinessDayConvention businessDay = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    final YieldConvention yieldConvention = SimpleYieldConvention.TRUE;
    final ZonedDateTime date = DateUtils.getUTCDate(2011, 1, 1);
    final BondFixedSecurity b1 = BondFixedSecurityDefinition.from(CUR, maturityDate, firstAccrualDate, paymentPeriod, R1, 0, calendar, dayCount, businessDay, yieldConvention, false, "I").toDerivative(date);
    final BondFixedSecurity b2 = BondFixedSecurityDefinition.from(CUR, maturityDate, firstAccrualDate, paymentPeriod, R2, 0, calendar, dayCount, businessDay, yieldConvention, false, "I").toDerivative(date);
    assertEquals(b2, VISITOR.visitBondFixedSecurity(b1, R2));
  }

  @Test
  public void testCash() {
    final Cash c1 = new Cash(CUR, 0, 1, 1, R1, 1);
    final Cash c2 = new Cash(CUR, 0, 1, 1, R2, 1);
    assertEquals(c1.accept(VISITOR, R2), c2);
  }

  @Test
  public void testFixedCouponAnnuity() {
    final AnnuityCouponFixed c1 = new AnnuityCouponFixed(CUR, new double[] {1, 2 }, R1, true);
    final AnnuityCouponFixed c2 = new AnnuityCouponFixed(CUR, new double[] {1, 2 }, R2, true);
    assertEquals(c1.accept(VISITOR, R2), c2);
  }

  @Test
  public void testFRA() {
    final IborIndex index = new IborIndex(CUR, Period.ofMonths(1), 2, DayCountFactory.INSTANCE.getDayCount("Actual/365"),
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), true, "Ibor");
    final ForwardRateAgreement fra1 = new ForwardRateAgreement(CUR, 0.5, 0.5, 1, index, 0.5, 0.5, 1, 0.5, R1);
    final ForwardRateAgreement fra2 = new ForwardRateAgreement(CUR, 0.5, 0.5, 1, index, 0.5, 0.5, 1, 0.5, R2);
    assertEquals(fra1.accept(VISITOR, R2), fra2);
  }

  @Test
  public void testIRFuture() {
    final IborIndex iborIndex = new IborIndex(CUR, Period.ofMonths(3), 2, DayCountFactory.INSTANCE.getDayCount("Actual/365"),
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), true, "Ibor");
    final double lastTradingTime = 1.473;
    final double fixingPeriodStartTime = 1.467;
    final double fixingPeriodEndTime = 1.75;
    final double fixingPeriodAccrualFactor = 0.267;
    final double paymentAccrualFactor = 0.25;
    final int quantity = 123;
    final InterestRateFutureTransaction ir1 = new InterestRateFutureTransaction(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodAccrualFactor, 1 - R1, 1, paymentAccrualFactor, quantity,
        "K");
    final InterestRateFutureTransaction ir2 = new InterestRateFutureTransaction(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodAccrualFactor, 1 - R2, 1, paymentAccrualFactor, quantity,
        "K");
    assertEquals(ir1.accept(VISITOR, R2), ir2);
  }

}
