/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import org.threeten.bp.LocalDate;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;

/**
 * 
 */
public class CreditDefaultSwapOptionCS01PnLFunction extends CreditInstrumentCS01PnLFunction {

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getPosition().getSecurity() instanceof CreditDefaultSwapOptionSecurity;
  }

  @Override
  protected NavigableSet<CurveNodeWithIdentifier> getNodes(final LocalDate now, final FinancialSecurity security, final Set<CurveNodeWithIdentifier> allNodes) {
    final NavigableSet<CurveNodeWithIdentifier> nodes = new TreeSet<>();
    final LocalDate expiry = ((CreditDefaultSwapOptionSecurity) security).getMaturityDate().toLocalDate();
    for (final CurveNodeWithIdentifier node : allNodes) {
      final LocalDate nodeDate = now.plus(node.getCurveNode().getResolvedMaturity().getPeriod());
      if (nodeDate.isAfter(expiry)) {
        nodes.add(node);
      }
    }
    return nodes;
  }

}
