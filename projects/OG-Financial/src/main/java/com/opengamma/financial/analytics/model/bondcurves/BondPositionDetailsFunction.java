/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcurves;

import static com.opengamma.engine.value.ValueRequirementNames.BOND_DETAILS;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.fixedincome.FixedSwapLegDetails;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Produces information about cash-flows for a bond position.
 */
public class BondPositionDetailsFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final FixedSwapLegDetails details = (FixedSwapLegDetails) inputs.getValue(BOND_DETAILS);
    final double quantity = target.getPosition().getQuantity().doubleValue();
    final CurrencyAmount[] paymentAmounts = details.getPaymentAmounts();
    final CurrencyAmount[] notionals = details.getNotionals();
    final int length = paymentAmounts.length;
    final CurrencyAmount[] scaledPaymentAmounts = new CurrencyAmount[length];
    final CurrencyAmount[] scaledNotionals = new CurrencyAmount[length];
    for (int i = 0; i < length; i++) {
      scaledPaymentAmounts[i] = paymentAmounts[i].multipliedBy(quantity);
      scaledNotionals[i] = notionals[i].multipliedBy(quantity);
    }
    final FixedSwapLegDetails scaledDetails = new FixedSwapLegDetails(details.getAccrualStart(), details.getAccrualEnd(), details.getDiscountFactors(), details.getPaymentTimes(),
        details.getPaymentFractions(), scaledPaymentAmounts, scaledNotionals, details.getFixedRates());
    final ValueProperties properties = Iterables.getOnlyElement(desiredValues).getConstraints().copy().get();
    return Collections.singleton(new ComputedValue(new ValueSpecification(BOND_DETAILS, target.toSpecification(), properties), scaledDetails));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Collection<Trade> trades = target.getPosition().getTrades();
    if (trades.size() != 1) {
      return false;
    }
    return Iterables.getOnlyElement(trades).getSecurity() instanceof BondSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(BOND_DETAILS, target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Trade trade = Iterables.getOnlyElement(target.getPosition().getTrades());
    final ValueProperties properties = desiredValue.getConstraints().copy().get();
    return Collections.singleton(new ValueRequirement(BOND_DETAILS, ComputationTargetSpecification.of(trade), properties));
  }

}
