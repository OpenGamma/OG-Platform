/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcurves.future;

import static com.opengamma.core.value.MarketDataRequirementNames.MARKET_VALUE;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.GROSS_BASIS;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.BondFuturesSecurityDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.StringLabelledMatrix1D;
import com.opengamma.financial.analytics.model.BondAndBondFutureFunctionUtils;
import com.opengamma.financial.analytics.model.bondcurves.BondAndBondFutureFromCurvesFunction;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Calculates the gross basis of all bonds in the deliverable basket using
 * the future price and issuer curves.
 */
public class BondFutureGrossBasisFromCurvesFunction extends BondAndBondFutureFromCurvesFunction<IssuerProviderInterface, Void> {

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#GROSS_BASIS} and
   * the calculator to null.
   */
  public BondFutureGrossBasisFromCurvesFunction() {
    super(GROSS_BASIS, null);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final double price = (Double) inputs.getValue(MARKET_VALUE);
    final ValueProperties properties = desiredValue.getConstraints();
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
    final BondFutureSecurity security = (BondFutureSecurity) target.getTrade().getSecurity();
    final BondFuturesTransaction transaction = (BondFuturesTransaction) BondAndBondFutureFunctionUtils.getBondOrBondFutureDerivative(executionContext, target, now, inputs);
    final IssuerProviderInterface issuerCurves = (IssuerProviderInterface) inputs.getValue(CURVE_BUNDLE);
    final ValueSpecification spec = new ValueSpecification(GROSS_BASIS, target.toSpecification(), properties);
    final double[] grossBasis = BondFuturesSecurityDiscountingMethod.getInstance().grossBasisFromCurves(transaction.getUnderlyingSecurity(), issuerCurves, price);
    final int n = grossBasis.length;
    final String[] keys = new String[n];
    for (int i = 0; i < n; i++) {
      keys[i] = security.getBasket().get(i).getIdentifiers().getExternalIds().toString(); //TODO what label do we want here?
    }
    final StringLabelledMatrix1D result = new StringLabelledMatrix1D(keys, grossBasis);
    return Collections.singleton(new ComputedValue(spec, result));
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getTrade().getSecurity();
    return security instanceof BondFutureSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    requirements.add(new ValueRequirement(MARKET_VALUE, ComputationTargetSpecification.of(target.getTrade().getSecurity()), ValueProperties.none()));
    return requirements;
  }
}
