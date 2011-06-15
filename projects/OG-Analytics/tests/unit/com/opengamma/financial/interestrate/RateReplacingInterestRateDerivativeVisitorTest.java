/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.util.money.Currency;

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

  @Test
  public void testBond() {
    final Bond b1 = new Bond(CUR, new double[] {1, 2}, R1, N1);
    final Bond b2 = new Bond(CUR, new double[] {1, 2}, R2, N1);
    assertEquals(VISITOR.visit(b1, R2), b2);
  }

  @Test
  public void testCash() {
    final Cash c1 = new Cash(CUR, 1, R1, N1);
    final Cash c2 = new Cash(CUR, 1, R2, N1);
    assertEquals(VISITOR.visit(c1, R2), c2);
  }

  @Test
  public void testForwardLiborAnnuity() {
    final AnnuityCouponIbor a1 = new AnnuityCouponIbor(CUR, new double[] {1, 2}, N1, N2, true);
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
    final InterestRateFuture f1 = new InterestRateFuture(1, 2, 1, 100 * (1 - R1), N1);
    final InterestRateFuture f2 = new InterestRateFuture(1, 2, 1, 100 * (1 - R2), N1);
    assertEquals(VISITOR.visit(f1, R2), f2);
  }

}
