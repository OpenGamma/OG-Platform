/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;

/**
 *
 */
public class CouponFixingYearFractionVisitor extends InstrumentDefinitionVisitorAdapter<Void, Double> {

  @Override
  public Double visitCouponIborDefinition(final CouponIborDefinition payment) {
    return payment.getFixingPeriodAccrualFactor();
  }

  @Override
  public Double visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition payment) {
    return payment.getFixingPeriodAccrualFactor();
  }

  @Override
  public Double visitCouponIborGearingDefinition(final CouponIborGearingDefinition payment) {
    return payment.getFixingPeriodAccrualFactor();
  }

  @Override
  public Double visitCouponIborRatchetDefinition(final CouponIborRatchetDefinition payment) {
    return payment.getFixingPeriodAccrualFactor();
  }

}
