/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedAccruedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageFixingDatesCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageFixingDatesCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageFixingDatesDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageIndexDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSimpleSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSpreadSimplifiedDefinition;
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

  @Override
  public Tenor visitCouponIborCompoundingDefinition(final CouponIborCompoundingDefinition definition) {
    return Tenor.of(definition.getIndex().getTenor());
  }

  @Override
  public Tenor visitCouponIborCompoundingFlatSpreadDefinition(final CouponIborCompoundingFlatSpreadDefinition definition) {
    return Tenor.of(definition.getIndex().getTenor());
  }
  
  @Override
  public Tenor visitCouponIborCompoundingSimpleSpreadDefinition(CouponIborCompoundingSimpleSpreadDefinition payment) {
    return Tenor.of(payment.getIndex().getTenor());
  }
  
  @Override
  public Tenor visitCouponIborRatchetDefinition(CouponIborRatchetDefinition payment) {
    return Tenor.of(payment.getIndex().getTenor());
  }
  
  @Override
  public Tenor visitCouponIborAverageDefinition(CouponIborAverageIndexDefinition payment) {
    return null;
  }
  
  @Override
  public Tenor visitCouponIborAverageCompoundingDefinition(CouponIborAverageFixingDatesCompoundingDefinition payment) {
    return Tenor.of(payment.getIndex().getTenor());
  }
  
  @Override
  public Tenor visitCouponIborAverageFixingDatesDefinition(CouponIborAverageFixingDatesDefinition payment) {
    return Tenor.of(payment.getIndex().getTenor());
  }
  
  @Override
  public Tenor visitCouponIborAverageFlatCompoundingSpreadDefinition(CouponIborAverageFixingDatesCompoundingFlatSpreadDefinition payment) {
    return Tenor.of(payment.getIndex().getTenor());
  }

  @Override
  public Tenor visitCouponOISDefinition(final CouponONDefinition definition) {
    return Tenor.ON;
  }
  
  @Override
  public Tenor visitCouponOISSimplifiedDefinition(CouponONSimplifiedDefinition payment) {
    return Tenor.ON;
  }
  
  @Override
  public Tenor visitCouponArithmeticAverageONDefinition(CouponONArithmeticAverageDefinition payment) {
    return Tenor.ON;
  }

  @Override
  public Tenor visitCouponArithmeticAverageONSpreadDefinition(final CouponONArithmeticAverageSpreadDefinition definition) {
    return Tenor.ON;
  }
  
  @Override
  public Tenor visitCouponArithmeticAverageONSpreadSimplifiedDefinition(CouponONArithmeticAverageSpreadSimplifiedDefinition payment) {
    return Tenor.ON;
  }

  @Override
  public Tenor visitCouponONSpreadDefinition(final CouponONSpreadDefinition definition) {
    return Tenor.ON;
  }
  
  @Override
  public Tenor visitCouponONSpreadSimplifiedDefinition(CouponONSpreadSimplifiedDefinition payment) {
    return Tenor.ON;
  }
  
  @Override
  public Tenor visitCouponONCompoundedDefinition(CouponONCompoundedDefinition payment) {
    return Tenor.ON;
  }

  @Override
  public Tenor visitCouponFixedDefinition(final CouponFixedDefinition definition) {
    return null;
  }
  
  @Override
  public Tenor visitCouponIborCompoundingSpreadDefinition(CouponIborCompoundingSpreadDefinition payment) {
    return Tenor.of(payment.getIndex().getTenor());
  }
  
  @Override
  public Tenor visitCouponFixedAccruedCompoundingDefinition(CouponFixedAccruedCompoundingDefinition payment) {
    return null;
  }
  
  @Override
  public Tenor visitCouponFixedCompoundingDefinition(CouponFixedCompoundingDefinition payment) {
    return null;
  }
}
