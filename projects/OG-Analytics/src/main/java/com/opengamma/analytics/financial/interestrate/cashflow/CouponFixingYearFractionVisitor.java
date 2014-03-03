/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cashflow;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;

/**
 * A class that gets the fixing period accrual factors.
 */
public final class CouponFixingYearFractionVisitor extends InstrumentDefinitionVisitorAdapter<Void, Double> {
  /** The singleton instance */
  private static final InstrumentDefinitionVisitor<Void, Double> INSTANCE = new CouponFixingYearFractionVisitor();

  /**
   * Gets the singleton instance.
   * @return The instance.
   */
  public static InstrumentDefinitionVisitor<Void, Double> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponFixingYearFractionVisitor() {
  }

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

  @Override
  public Double visitCouponFixedDefinition(CouponFixedDefinition payment) {
    return null;
  }
}
