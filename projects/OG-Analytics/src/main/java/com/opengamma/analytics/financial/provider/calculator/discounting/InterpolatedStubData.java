/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import com.opengamma.analytics.financial.instrument.index.IndexDeposit;
import com.opengamma.analytics.financial.interestrate.payments.derivative.DepositIndexCoupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.InterpolatedStubCoupon;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Parameter object for interpolated stub instrument derivative visitor
 */
public final class InterpolatedStubData {
  
  private final MulticurveProviderInterface _multicurve;
  
  private final InterpolatedStubCoupon<? extends DepositIndexCoupon<? extends IndexDeposit>, ? extends IndexDeposit> _interpolatedStubCoupon;
  
  private InterpolatedStubData(
      final MulticurveProviderInterface multicurve,
      final InterpolatedStubCoupon<? extends DepositIndexCoupon<? extends IndexDeposit>, ? extends IndexDeposit> interpolatedStubCoupon) {
    ArgumentChecker.notNull(multicurve, "multicurve");
    ArgumentChecker.notNull(interpolatedStubCoupon, "interpolatedStubCoupon");
    _multicurve = multicurve;
    _interpolatedStubCoupon = interpolatedStubCoupon;
  }
  
  public MulticurveProviderInterface getMulticurve() {
    return _multicurve;
  }
  
  public InterpolatedStubCoupon<? extends DepositIndexCoupon<? extends IndexDeposit>, ? extends IndexDeposit> getInterpolatedStubCoupon() {
    return _interpolatedStubCoupon;
  }
  
  public static InterpolatedStubData of(
      final MulticurveProviderInterface multicurve,
      final InterpolatedStubCoupon<? extends DepositIndexCoupon<? extends IndexDeposit>, ? extends IndexDeposit> interpolatedStubCoupon) {
    return new InterpolatedStubData(multicurve, interpolatedStubCoupon);
  }
}
