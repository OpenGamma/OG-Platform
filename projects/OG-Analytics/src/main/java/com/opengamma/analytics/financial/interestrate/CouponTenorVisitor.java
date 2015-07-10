/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
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
public final class CouponTenorVisitor extends InstrumentDefinitionVisitorAdapter<Void, Set<Tenor>> {
  /** The singleton instance */
  private static final InstrumentDefinitionVisitor<Void, Set<Tenor>> INSTANCE = new CouponTenorVisitor();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static InstrumentDefinitionVisitor<Void, Set<Tenor>> getInstance() {
    return INSTANCE;
  }

  @Override
  public Set<Tenor> visitCouponIborDefinition(CouponIborDefinition definition) {
    return Sets.newHashSet(Tenor.of(definition.getIndex().getTenor()));
  }

  @Override
  public Set<Tenor> visitCouponIborSpreadDefinition(CouponIborSpreadDefinition definition) {
    return Sets.newHashSet(Tenor.of(definition.getIndex().getTenor()));
  }

  @Override
  public Set<Tenor> visitCouponIborGearingDefinition(CouponIborGearingDefinition definition) {
    return Sets.newHashSet(Tenor.of(definition.getIndex().getTenor()));
  }

  @Override
  public Set<Tenor> visitCouponIborCompoundingDefinition(CouponIborCompoundingDefinition definition) {
    return Sets.newHashSet(Tenor.of(definition.getIndex().getTenor()));
  }

  @Override
  public Set<Tenor> visitCouponIborCompoundingFlatSpreadDefinition(
      CouponIborCompoundingFlatSpreadDefinition definition) {
    return Sets.newHashSet(Tenor.of(definition.getIndex().getTenor()));
  }
  
  @Override
  public Set<Tenor> visitCouponIborCompoundingSimpleSpreadDefinition(
      CouponIborCompoundingSimpleSpreadDefinition payment) {
    return Sets.newHashSet(Tenor.of(payment.getIndex().getTenor()));
  }
  
  @Override
  public Set<Tenor> visitCouponIborRatchetDefinition(CouponIborRatchetDefinition payment) {
    return Sets.newHashSet(Tenor.of(payment.getIndex().getTenor()));
  }
  
  @Override
  public Set<Tenor> visitCouponIborAverageDefinition(CouponIborAverageIndexDefinition payment) {
    return Sets.newHashSet(Tenor.of(payment.getIndex1().getTenor()), Tenor.of(payment.getIndex2().getTenor()));
  }
  
  @Override
  public Set<Tenor> visitCouponIborAverageCompoundingDefinition(
        CouponIborAverageFixingDatesCompoundingDefinition payment) {
    return Sets.newHashSet(Tenor.of(payment.getIndex().getTenor()));
  }
  
  @Override
  public Set<Tenor> visitCouponIborAverageFixingDatesDefinition(CouponIborAverageFixingDatesDefinition payment) {
    return Sets.newHashSet(Tenor.of(payment.getIndex().getTenor()));
  }
  
  @Override
  public Set<Tenor> visitCouponIborAverageFlatCompoundingSpreadDefinition(
        CouponIborAverageFixingDatesCompoundingFlatSpreadDefinition payment) {
    return Sets.newHashSet(Tenor.of(payment.getIndex().getTenor()));
  }

  @Override
  public Set<Tenor> visitCouponOISDefinition(CouponONDefinition definition) {
    return Sets.newHashSet(Tenor.ON);
  }
  
  @Override
  public Set<Tenor> visitCouponOISSimplifiedDefinition(CouponONSimplifiedDefinition payment) {
    return Sets.newHashSet(Tenor.ON);
  }
  
  @Override
  public Set<Tenor> visitCouponArithmeticAverageONDefinition(CouponONArithmeticAverageDefinition payment) {
    return Sets.newHashSet(Tenor.ON);
  }

  @Override
  public Set<Tenor> visitCouponArithmeticAverageONSpreadDefinition(
      CouponONArithmeticAverageSpreadDefinition definition) {
    return Sets.newHashSet(Tenor.ON);
  }
  
  @Override
  public Set<Tenor> visitCouponArithmeticAverageONSpreadSimplifiedDefinition(
      CouponONArithmeticAverageSpreadSimplifiedDefinition payment) {
    return Sets.newHashSet(Tenor.ON);
  }

  @Override
  public Set<Tenor> visitCouponONSpreadDefinition(final CouponONSpreadDefinition definition) {
    return Sets.newHashSet(Tenor.ON);
  }
  
  @Override
  public Set<Tenor> visitCouponONSpreadSimplifiedDefinition(CouponONSpreadSimplifiedDefinition payment) {
    return Sets.newHashSet(Tenor.ON);
  }
  
  @Override
  public Set<Tenor> visitCouponONCompoundedDefinition(CouponONCompoundedDefinition payment) {
    return Sets.newHashSet(Tenor.ON);
  }

  @Override
  public Set<Tenor> visitCouponFixedDefinition(final CouponFixedDefinition definition) {
    return Collections.emptySet();
  }
  
  @Override
  public Set<Tenor> visitCouponIborCompoundingSpreadDefinition(CouponIborCompoundingSpreadDefinition payment) {
    return Sets.newHashSet(Tenor.of(payment.getIndex().getTenor()));
  }
  
  @Override
  public Set<Tenor> visitCouponFixedAccruedCompoundingDefinition(CouponFixedAccruedCompoundingDefinition payment) {
    return Collections.emptySet();
  }
  
  @Override
  public Set<Tenor> visitCouponFixedCompoundingDefinition(CouponFixedCompoundingDefinition payment) {
    return Collections.emptySet();
  }

  @Override
  public Set<Tenor> visitForwardRateAgreementDefinition(ForwardRateAgreementDefinition fra) {
    return Sets.newHashSet(Tenor.of(fra.getIndex().getTenor()));
  }
}
