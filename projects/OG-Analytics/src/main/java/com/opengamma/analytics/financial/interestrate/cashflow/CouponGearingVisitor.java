/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cashflow;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorSameValueAdapter;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;

/**
 * Gets the gearing of each coupon. The default value returned is one.
 */
public final class CouponGearingVisitor extends InstrumentDefinitionVisitorSameValueAdapter<Void, Double> {
  /** The singleton instance */
  private static final InstrumentDefinitionVisitor<Void, Double> INSTANCE = new CouponGearingVisitor();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static InstrumentDefinitionVisitor<Void, Double> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor setting the default spread value to one.
   */
  private CouponGearingVisitor() {
    super(1.);
  }

  @Override
  public Double visitCouponIborGearingDefinition(final CouponIborGearingDefinition definition) {
    return definition.getFactor();
  }
}
