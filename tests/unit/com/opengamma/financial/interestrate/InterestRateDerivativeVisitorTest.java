/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.libor.Libor;
import com.opengamma.financial.interestrate.swap.definition.Swap;

/**
 * 
 */
public class InterestRateDerivativeVisitorTest {
  private static final InterestRateDerivativeVisitor<Class<?>> VISITOR = new InterestRateDerivativeVisitor<Class<?>>() {

    private Class<?> visit(final InterestRateDerivative derivative) {
      return derivative.getClass();
    }

    @Override
    public Class<?> visitSwap(final Swap swap) {
      return visit(swap);
    }

    @Override
    public Class<?> visitLibor(final Libor libor) {
      return visit(libor);
    }

    @Override
    public Class<?> visitInterestRateFuture(final InterestRateFuture future) {
      return visit(future);
    }

    @Override
    public Class<?> visitForwardRateAgreement(final ForwardRateAgreement fra) {
      return visit(fra);
    }

    @Override
    public Class<?> visitCash(final Cash cash) {
      return visit(cash);
    }
  };

  @Test
  public void test() {
    final Cash cash = new Cash(1);
    final ForwardRateAgreement fra = new ForwardRateAgreement(0, 1);
    final InterestRateFuture future = new InterestRateFuture(0, 1);
    final Libor libor = new Libor(1);
    final Swap swap = new Swap(new double[] {1}, new double[] {1}, new double[] {0}, new double[] {0});
    assertEquals(cash.accept(VISITOR), Cash.class);
    assertEquals(fra.accept(VISITOR), ForwardRateAgreement.class);
    assertEquals(future.accept(VISITOR), InterestRateFuture.class);
    assertEquals(libor.accept(VISITOR), Libor.class);
    assertEquals(swap.accept(VISITOR), Swap.class);
  }
}
