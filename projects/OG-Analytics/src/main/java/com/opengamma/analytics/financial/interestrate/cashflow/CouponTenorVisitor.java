/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cashflow;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.util.time.Tenor;

/**
 * Gets the index tenor of each floating coupon.
 */
public final class CouponTenorVisitor extends InstrumentDefinitionVisitorAdapter<Void, Tenor> {
  /** The singleton instance */
  private static final InstrumentDefinitionVisitor<Void, Tenor> INSTANCE = new CouponTenorVisitor();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static InstrumentDefinitionVisitor<Void, Tenor> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponTenorVisitor() {
  }

  @Override
  public Tenor visitCouponIborDefinition(final CouponIborDefinition definition) {
    return Tenor.of(definition.getIndex().getTenor());
  }

  @Override
  public Tenor visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition definition) {
    return Tenor.of(definition.getIndex().getTenor());
  }

  @Override
  public Tenor visitCouponIborGearingDefinition(final CouponIborGearingDefinition definition) {
    return Tenor.of(definition.getIndex().getTenor());
  }
}
