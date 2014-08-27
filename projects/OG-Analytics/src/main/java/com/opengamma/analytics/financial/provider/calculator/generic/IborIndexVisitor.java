/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.generic;

import com.opengamma.analytics.financial.instrument.index.IndexDeposit;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameValueAdapter;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpreadSimplified;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONSpread;

/**
 * Returns the Ibor index of a coupon. For averaging 
 */
public final class IborIndexVisitor extends InstrumentDerivativeVisitorSameValueAdapter<Object, IndexDeposit> {
  
  private static final IborIndexVisitor SINGLETON = new IborIndexVisitor();
  
  public static IborIndexVisitor getInstance() {
    return SINGLETON;
  }
  
  private IborIndexVisitor() {
    super(null);
  }
  
  @Override
  public IndexDeposit visitCouponIbor(CouponIbor payment) {
    return payment.getIndex();
  }
  
  @Override
  public IndexDeposit visitCouponIborCompounding(CouponIborCompounding payment) {
    return payment.getIndex();
  }
  
  @Override
  public IndexDeposit visitCouponIborGearing(CouponIborGearing payment) {
    return payment.getIndex();
  }
  
  @Override
  public IndexDeposit visitCouponIborSpread(CouponIborSpread payment) {
    return payment.getIndex();
  }

  @Override
  public IndexDeposit visitCouponOIS(CouponON payment) {
    return payment.getIndex();
  }
  
  @Override
  public IndexDeposit visitCouponONArithmeticAverage(CouponONArithmeticAverage payment) {
    return payment.getIndex();
  }
  
  @Override
  public IndexDeposit visitCouponONArithmeticAverageSpread(CouponONArithmeticAverageSpread payment) {
    return payment.getIndex();
  }
  
  @Override
  public IndexDeposit visitCouponONArithmeticAverageSpreadSimplified(CouponONArithmeticAverageSpreadSimplified payment) {
    return payment.getIndex();
  }
  
  @Override
  public IndexDeposit visitCouponONCompounded(CouponONCompounded payment) {
    return payment.getIndex();
  }
  
  @Override
  public IndexDeposit visitCouponONSpread(CouponONSpread payment) {
    return payment.getIndex();
  }
}
