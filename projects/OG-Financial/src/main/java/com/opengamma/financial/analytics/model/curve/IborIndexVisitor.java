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
import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;

/**
 * Returns all {@link IborIndex} objects for an instrument definition or an empty set if none exist.
 */
public final class IborIndexVisitor extends InstrumentDefinitionVisitorSameValueAdapter<Object, Collection<IborIndex>> {
  /** Static instance */
  private static final InstrumentDefinitionVisitor<Object, Collection<IborIndex>> INSTANCE = new IborIndexVisitor();

  /**
   * Returns an instance of this visitor
   * @return The instance
   */
  public static InstrumentDefinitionVisitor<Object, Collection<IborIndex>> getInstance() {
    return INSTANCE;
  }

  private IborIndexVisitor() {
    super(Collections.<IborIndex>emptySet());
  }

  @Override
  public Collection<IborIndex> visitAnnuityDefinition(final AnnuityDefinition<? extends PaymentDefinition> definition) {
    final Collection<IborIndex> result = new HashSet<>();
    for (final InstrumentDefinition<?> payment : definition.getPayments()) {
      result.addAll(payment.accept(this));
    }
    return result;
  }

  @Override
  public Collection<IborIndex> visitCouponIborDefinition(final CouponIborDefinition definition) {
    return Collections.singleton(definition.getIndex());
  }

  @Override
  public Collection<IborIndex> visitCouponIborSpreadDefinition(final CouponIborSpreadDefinition definition) {
    return Collections.singleton(definition.getIndex());
  }

  @Override
  public Collection<IborIndex> visitDepositIborDefinition(final DepositIborDefinition definition) {
    return Collections.singleton(definition.getIndex());
  }

  @Override
  public Collection<IborIndex> visitForwardRateAgreementDefinition(final ForwardRateAgreementDefinition definition) {
    return Collections.singleton(definition.getIndex());
  }

  @Override
  public Collection<IborIndex> visitSwapDefinition(final SwapDefinition definition) {
    final Collection<IborIndex> indices = new HashSet<>();
    indices.addAll(definition.getFirstLeg().accept(this));
    indices.addAll(definition.getSecondLeg().accept(this));
    return indices;
  }
}
