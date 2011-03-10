/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;

/**
 * 
 */
public class RateReplacingInterestRateDerivativeVisitorTest {
  private static final double R1 = 0.05;
  private static final double R2 = 0.04;
  private static final String N1 = "A";
  private static final String N2 = "B";
  private static final RateReplacingInterestRateDerivativeVisitor VISITOR = RateReplacingInterestRateDerivativeVisitor.getInstance();

  @Test
  public void testBond() {
    Bond b1 = new Bond(new double[] {1, 2}, R1, N1);
    Bond b2 = new Bond(new double[] {1, 2}, R2, N1);
    assertEquals(VISITOR.visit(b1, R2), b2);
  }

  @Test
  public void testCash() {
    Cash c1 = new Cash(1, R1, N1);
    Cash c2 = new Cash(1, R2, N1);
    assertEquals(VISITOR.visit(c1, R2), c2);
  }

  @Test
  public void testForwardLiborAnnuity() {
    AnnuityCouponIbor a1 = new AnnuityCouponIbor(new double[] {1, 2}, N1, N2);
    AnnuityCouponIbor a2 = a1.withSpread(R2);
    assertEquals(VISITOR.visit(a1, R2), a2);
  }

  @Test
  public void testFixedCouponAnnuity() {
    AnnuityCouponFixed c1 = new AnnuityCouponFixed(new double[] {1, 2}, R1, N1);
    AnnuityCouponFixed c2 = new AnnuityCouponFixed(new double[] {1, 2}, R2, N1);
    assertEquals(VISITOR.visit(c1, R2), c2);
  }

  @Test
  public void testFRA() {
    ForwardRateAgreement fra1 = new ForwardRateAgreement(1, 2, R1, N1, N2);
    ForwardRateAgreement fra2 = new ForwardRateAgreement(1, 2, R2, N1, N2);
    assertEquals(VISITOR.visit(fra1, R2), fra2);
  }

  @Test
  public void testIRFuture() {
    InterestRateFuture f1 = new InterestRateFuture(1, 2, 1, 100 * (1 - R1), N1);
    InterestRateFuture f2 = new InterestRateFuture(1, 2, 1, 100 * (1 - R2), N1);
    assertEquals(VISITOR.visit(f1, R2), f2);
  }

}
