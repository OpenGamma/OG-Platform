/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorSameValueAdapter;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponONSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;

/**
 * Returns all {@link IndexON} object for an instrument definition or an empty set if none exist.
 */
public final class OvernightIndexVisitor extends InstrumentDefinitionVisitorSameValueAdapter<Object, Collection<IndexON>> {
  /** Static instance */
  private static final InstrumentDefinitionVisitor<Object, Collection<IndexON>> INSTANCE = new OvernightIndexVisitor();

  /**
   * Returns an instance of this visitor
   * @return The instance
   */
  public static InstrumentDefinitionVisitor<Object, Collection<IndexON>> getInstance() {
    return INSTANCE;
  }

  private OvernightIndexVisitor() {
    super(Collections.<IndexON>emptySet());
  }

  @Override
  public Collection<IndexON> visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> definition) {
    final Collection<IndexON> result = new HashSet<>();
    for (final InstrumentDefinition<?> payment : definition.getPayments()) {
      result.addAll(payment.accept(this));
    }
    return result;
  }

  @Override
  public Collection<IndexON> visitCouponOISSimplifiedDefinition(final CouponONSimplifiedDefinition definition) {
    return Collections.singleton(definition.getIndex());
  }

  @Override
  public Collection<IndexON> visitSwapDefinition(final SwapDefinition definition) {
    final Collection<IndexON> indices = new HashSet<>();
    indices.addAll(definition.getFirstLeg().accept(this));
    indices.addAll(definition.getSecondLeg().accept(this));
    return indices;
  }
}
