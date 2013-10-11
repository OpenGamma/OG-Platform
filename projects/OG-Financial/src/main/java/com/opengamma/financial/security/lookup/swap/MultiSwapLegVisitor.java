/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.lookup.swap;

import com.opengamma.financial.security.swap.FixedInflationSwapLeg;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FixedVarianceSwapLeg;
import com.opengamma.financial.security.swap.FloatingGearingIRLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.FloatingVarianceSwapLeg;
import com.opengamma.financial.security.swap.InflationIndexSwapLeg;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapLegVisitor;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
/* package */abstract class MultiSwapLegVisitor<T> {

  /**
   * Visits the fixed leg of a fixed/float swap. For float/float swaps {@link #visitFloatingPayLeg} will be called.
   * @param leg The leg
   * @return A value
   */
  /* package */ abstract T visitFixedLeg(FixedInterestRateLeg leg);

  /**
   * Visits the fixed leg of an inflation swap. For index / index inflation swaps {@link #visitFloatingInflationPayLeg}
   * will be called.
   * @param leg The leg
   * @return A value
   */
  /* package */ abstract T visitFixedInflationLeg(FixedInflationSwapLeg leg);

  /**
   * Visits the pay leg of a float/float swap. For fixed/float swaps {@link #visitFixedLeg} will be called.
   * @param leg The leg
   * @return A value
   */
  /* package */ abstract T visitFloatingPayLeg(FloatingInterestRateLeg leg);

  /**
   * Visits the pay leg of a index/index inflation swap. For fixed / index inflation swaps {@link #visitFixedInflationLeg} will be called.
   */
  /* package  */ abstract T visitInflationIndexPayLeg(InflationIndexSwapLeg leg);

  /**
   * Visits the floating leg of a fixed/float swap or the receive leg of a float/float swap.
   * @param leg The leg
   * @return A value
   */
  /* package */ abstract T visitOtherLeg(FloatingInterestRateLeg leg);

  /**
   * Visits the index leg of a fixed/float inflation swap or the receive leg of a float/float swap.
   * @param leg The leg
   * @return A value
   */
  /* package */ abstract T visitOtherIndexLeg(InflationIndexSwapLeg leg);

  public Pair<T, T> visit(final SwapSecurity swap) {
    final FixedFloatVisitor fixedFloatVisitor = new FixedFloatVisitor();
    final SwapLeg payLeg = swap.getPayLeg();
    final SwapLeg receiveLeg = swap.getReceiveLeg();
    final boolean payFixed = payLeg.accept(fixedFloatVisitor);
    final boolean receiveFixed = receiveLeg.accept(fixedFloatVisitor);
    T firstValue;
    T secondValue;
    if (payFixed && receiveFixed) {
      firstValue = payLeg.accept(new FixedVisitor());
      secondValue = receiveLeg.accept(new FixedVisitor());
      return Pairs.of(firstValue, secondValue);
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
    return Pairs.of(firstValue, secondValue);

  }

  private class OtherVisitor implements SwapLegVisitor<T> {

    @Override
    public T visitFixedInterestRateLeg(final FixedInterestRateLeg swapLeg) {
      return null;
    }

    @Override
    public T visitFloatingInterestRateLeg(final FloatingInterestRateLeg swapLeg) {
      return visitOtherLeg(swapLeg);
    }

    @Override
    public T visitFloatingSpreadIRLeg(final FloatingSpreadIRLeg swapLeg) {
      return visitOtherLeg(swapLeg);
    }

    @Override
    public T visitFloatingGearingIRLeg(final FloatingGearingIRLeg swapLeg) {
      return visitOtherLeg(swapLeg);
    }

    @Override
    public T visitFixedVarianceSwapLeg(final FixedVarianceSwapLeg swapLeg) {
      return null;
    }

    @Override
    public T visitFloatingVarianceSwapLeg(final FloatingVarianceSwapLeg swapLeg) {
      return null;
    }

    @Override
    public T visitFixedInflationSwapLeg(final FixedInflationSwapLeg swapLeg) {
      return null;
    }

    @Override
    public T visitInflationIndexSwapLeg(final InflationIndexSwapLeg swapLeg) {
      return visitOtherIndexLeg(swapLeg);
    }

  }
  private class FloatingPayVisitor implements SwapLegVisitor<T> {

    @Override
    public T visitFixedInterestRateLeg(final FixedInterestRateLeg swapLeg) {
      return null;
    }

    @Override
    public T visitFloatingInterestRateLeg(final FloatingInterestRateLeg swapLeg) {
      return visitFloatingPayLeg(swapLeg);
    }

    @Override
    public T visitFloatingSpreadIRLeg(final FloatingSpreadIRLeg swapLeg) {
      return visitFloatingPayLeg(swapLeg);
    }

    @Override
    public T visitFloatingGearingIRLeg(final FloatingGearingIRLeg swapLeg) {
      return visitFloatingPayLeg(swapLeg);
    }

    @Override
    public T visitFixedVarianceSwapLeg(final FixedVarianceSwapLeg swapLeg) {
      return null;
    }

    @Override
    public T visitFloatingVarianceSwapLeg(final FloatingVarianceSwapLeg swapLeg) {
      return null;
    }

    @Override
    public T visitFixedInflationSwapLeg(final FixedInflationSwapLeg swapLeg) {
      return null;
    }

    @Override
    public T visitInflationIndexSwapLeg(final InflationIndexSwapLeg swapLeg) {
      return visitInflationIndexPayLeg(swapLeg);
    }

  }

  private class FixedVisitor implements SwapLegVisitor<T> {

    @Override
    public T visitFixedInterestRateLeg(final FixedInterestRateLeg swapLeg) {
      return visitFixedLeg(swapLeg);
    }

    @Override
    public T visitFloatingInterestRateLeg(final FloatingInterestRateLeg swapLeg) {
      return null;
    }

    @Override
    public T visitFloatingSpreadIRLeg(final FloatingSpreadIRLeg swapLeg) {
      return null;
    }

    @Override
    public T visitFloatingGearingIRLeg(final FloatingGearingIRLeg swapLeg) {
      return null;
    }

    @Override
    public T visitFixedVarianceSwapLeg(final FixedVarianceSwapLeg swapLeg) {
      return null;
    }

    @Override
    public T visitFloatingVarianceSwapLeg(final FloatingVarianceSwapLeg swapLeg) {
      return null;
    }

    @Override
    public T visitFixedInflationSwapLeg(final FixedInflationSwapLeg swapLeg) {
      return visitFixedInflationLeg(swapLeg);
    }

    @Override
    public T visitInflationIndexSwapLeg(final InflationIndexSwapLeg swapLeg) {
      return null;
    }
  }

}
