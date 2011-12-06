/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

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
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class RateReplacingInterestRateDerivativeVisitorTest {
  private static final double R1 = 0.05;
  private static final double R2 = 0.04;
  private static final String N1 = "A";
  private static final String N2 = "B";
  private static final RateReplacingInterestRateDerivativeVisitor VISITOR = RateReplacingInterestRateDerivativeVisitor.getInstance();
  private static final Currency CUR = Currency.USD;

  private static final Period TENOR = Period.ofMonths(6);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);

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
    final String c1 = "a";
    final String c2 = "b";
    final BondFixedSecurity b1 = BondFixedSecurityDefinition.from(CUR, maturityDate, firstAccrualDate, paymentPeriod, R1, 0, calendar, dayCount, businessDay, yieldConvention, false).toDerivative(
        date, c1, c2);
    final BondFixedSecurity b2 = BondFixedSecurityDefinition.from(CUR, maturityDate, firstAccrualDate, paymentPeriod, R2, 0, calendar, dayCount, businessDay, yieldConvention, false).toDerivative(
        date, c1, c2);
    assertEquals(b2, VISITOR.visitBondFixedSecurity(b1, R2));
  }

  @Test
  public void testCash() {
    final Cash c1 = new Cash(CUR, 1, 1, R1, N1);
    final Cash c2 = new Cash(CUR, 1, 1, R2, N1);
    assertEquals(VISITOR.visit(c1, R2), c2);
  }

  @Test
  public void testForwardLiborAnnuity() {
    final AnnuityCouponIbor a1 = new AnnuityCouponIbor(CUR, new double[] {1, 2}, INDEX, N1, N2, true);
    final AnnuityCouponIbor a2 = a1.withSpread(R2);
    assertEquals(VISITOR.visit(a1, R2), a2);
  }

  @Test
  public void testFixedCouponAnnuity() {
    final AnnuityCouponFixed c1 = new AnnuityCouponFixed(CUR, new double[] {1, 2}, R1, N1, true);
    final AnnuityCouponFixed c2 = new AnnuityCouponFixed(CUR, new double[] {1, 2}, R2, N1, true);
    assertEquals(VISITOR.visit(c1, R2), c2);
  }

  @Test
  public void testFRA() {
    final IborIndex index = new IborIndex(CUR, Period.ofMonths(1), 2, new MondayToFridayCalendar("A"), DayCountFactory.INSTANCE.getDayCount("Actual/365"),
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), true);
    final ForwardRateAgreement fra1 = new ForwardRateAgreement(CUR, 0.5, N1, 0.5, 1, index, 0.5, 0.5, 1, 0.5, R1, N2);
    final ForwardRateAgreement fra2 = new ForwardRateAgreement(CUR, 0.5, N1, 0.5, 1, index, 0.5, 0.5, 1, 0.5, R2, N2);
    assertEquals(VISITOR.visit(fra1, R2), fra2);
  }

  @Test
  public void testIRFuture() {
    final IborIndex iborIndex = new IborIndex(CUR, Period.ofMonths(3), 2, new MondayToFridayCalendar("A"), DayCountFactory.INSTANCE.getDayCount("Actual/365"),
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), true);
    final double lastTradingTime = 1.473;
    final double fixingPeriodStartTime = 1.467;
    final double fixingPeriodEndTime = 1.75;
    final double fixingPeriodAccrualFactor = 0.267;
    final double paymentAccrualFactor = 0.25;
//    final double referencePrice = 0.0; // TODO CASE - Future refactor - referencePrice = 0.0
    final InterestRateFuture ir1 = new InterestRateFuture(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodAccrualFactor, 1 - R1, 1, paymentAccrualFactor, "K", N1,
        N2);
    final InterestRateFuture ir2 = new InterestRateFuture(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodAccrualFactor, 1 - R2, 1, paymentAccrualFactor, "K", N1,
        N2);
    assertEquals(VISITOR.visit(ir1, R2), ir2);
  }

}
