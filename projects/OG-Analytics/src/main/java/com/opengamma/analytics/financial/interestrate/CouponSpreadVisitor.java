/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorSameValueAdapter;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;

/**
 * Gets the spreads on each coupon. The default value returned is zero.
 */
public final class CouponSpreadVisitor extends InstrumentDefinitionVisitorSameValueAdapter<Void, Double> {
  /** The singleton instance */
  private static final InstrumentDefinitionVisitor<Void, Double> INSTANCE = new CouponSpreadVisitor();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static InstrumentDefinitionVisitor<Void, Double> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor setting the default spread value to zero.
   */
  private CouponSpreadVisitor() {
    super(0.);
  }

  @Override
  public Double visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition definition) {
    return definition.getSpread();
  }
}
