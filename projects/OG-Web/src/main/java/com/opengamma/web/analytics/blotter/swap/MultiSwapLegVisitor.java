/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter.swap;

import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FixedVarianceSwapLeg;
import com.opengamma.financial.security.swap.FloatingGearingIRLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.FloatingVarianceSwapLeg;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapLegVisitor;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
/* package */ abstract class MultiSwapLegVisitor<T> {

  /**
   * Visits the fixed leg of a fixed/float swap. For float/float swaps {@link #visitFloatingPayLeg} will be called.
   * @param leg The leg
   * @return A value
   */
  /* package */ abstract T visitFixedLeg(FixedInterestRateLeg leg);

  /**
   * Visits the pay leg of a float/float swap. For fixed/float swaps {@link #visitFixedLeg} will be called.
   * @param leg The leg
   * @return A value
   */
  /* package */ abstract T visitFloatingPayLeg(FloatingInterestRateLeg leg);

  /**
   * Visits the floating leg of a fixed/float swap or the receive leg of a float/float swap.
   * @param leg The leg
   * @return A value
   */
  /* package */ abstract T visitOtherLeg(FloatingInterestRateLeg leg);

  public Pair<T, T> visit(SwapSecurity swap) {
    FixedFloatVisitor fixedFloatVisitor = new FixedFloatVisitor();
    SwapLeg payLeg = swap.getPayLeg();
    SwapLeg receiveLeg = swap.getReceiveLeg();
    boolean payFixed = payLeg.accept(fixedFloatVisitor);
    boolean receiveFixed = receiveLeg.accept(fixedFloatVisitor);
    T firstValue;
    T secondValue;
    if (payFixed && receiveFixed) {
      firstValue = payLeg.accept(new FixedVisitor());
      secondValue = receiveLeg.accept(new FixedVisitor());
      return Pair.of(firstValue, secondValue);
    }

    if (payFixed) {
      firstValue = payLeg.accept(new FixedVisitor());
      secondValue = receiveLeg.accept(new OtherVisitor());
    } else if (receiveFixed) {
      firstValue = receiveLeg.accept(new FixedVisitor());
      secondValue = payLeg.accept(new OtherVisitor());
    } else {
      firstValue = payLeg.accept(new FloatingPayVisitor());
      secondValue = receiveLeg.accept(new OtherVisitor());
    }
    return Pair.of(firstValue, secondValue);

  }

  private class OtherVisitor implements SwapLegVisitor<T> {

    @Override
    public T visitFixedInterestRateLeg(FixedInterestRateLeg swapLeg) {
      return null;
    }

    @Override
    public T visitFloatingInterestRateLeg(FloatingInterestRateLeg swapLeg) {
      return visitOtherLeg(swapLeg);
    }

    @Override
    public T visitFloatingSpreadIRLeg(FloatingSpreadIRLeg swapLeg) {
      return visitOtherLeg(swapLeg);
    }

    @Override
    public T visitFloatingGearingIRLeg(FloatingGearingIRLeg swapLeg) {
      return visitOtherLeg(swapLeg);
    }

    @Override
    public T visitFixedVarianceSwapLeg(FixedVarianceSwapLeg swapLeg) {
      return null;
    }

    @Override
    public T visitFloatingVarianceSwapLeg(FloatingVarianceSwapLeg swapLeg) {
      return null;
    }

  }
  private class FloatingPayVisitor implements SwapLegVisitor<T> {

    @Override
    public T visitFixedInterestRateLeg(FixedInterestRateLeg swapLeg) {
      return null;
    }

    @Override
    public T visitFloatingInterestRateLeg(FloatingInterestRateLeg swapLeg) {
      return visitFloatingPayLeg(swapLeg);
    }

    @Override
    public T visitFloatingSpreadIRLeg(FloatingSpreadIRLeg swapLeg) {
      return visitFloatingPayLeg(swapLeg);
    }

    @Override
    public T visitFloatingGearingIRLeg(FloatingGearingIRLeg swapLeg) {
      return visitFloatingPayLeg(swapLeg);
    }

    @Override
    public T visitFixedVarianceSwapLeg(FixedVarianceSwapLeg swapLeg) {
      return null;
    }

    @Override
    public T visitFloatingVarianceSwapLeg(FloatingVarianceSwapLeg swapLeg) {
      return null;
    }

  }

  private class FixedVisitor implements SwapLegVisitor<T> {

    @Override
    public T visitFixedInterestRateLeg(FixedInterestRateLeg swapLeg) {
      return visitFixedLeg(swapLeg);
    }

    @Override
    public T visitFloatingInterestRateLeg(FloatingInterestRateLeg swapLeg) {
      return null;
    }

    @Override
    public T visitFloatingSpreadIRLeg(FloatingSpreadIRLeg swapLeg) {
      return null;
    }

    @Override
    public T visitFloatingGearingIRLeg(FloatingGearingIRLeg swapLeg) {
      return null;
    }

    @Override
    public T visitFixedVarianceSwapLeg(FixedVarianceSwapLeg swapLeg) {
      return null;
    }

    @Override
    public T visitFloatingVarianceSwapLeg(FloatingVarianceSwapLeg swapLeg) {
      return null;
    }
  }

}
