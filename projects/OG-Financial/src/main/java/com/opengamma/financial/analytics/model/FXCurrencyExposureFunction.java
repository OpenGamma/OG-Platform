/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.FUNCTION;
import static com.opengamma.engine.value.ValueRequirementNames.FX_CURRENCY_EXPOSURE;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXVolatilitySwapSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.value.ValueRenamingFunction;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Produces FX currency exposures for all instruments except FX. This is not done via a {@link ValueRenamingFunction}
 * because the logic to produce currency exposure should eventually be moved into the analytics library.
 */
public class FXCurrencyExposureFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ComputedValue input = Iterables.getOnlyElement(inputs.getAllValues());
    final ValueProperties properties = input.getSpecification().getProperties().copy()
        .withoutAny(CURRENCY)
        .withoutAny(FUNCTION)
        .with(FUNCTION, getUniqueId())
        .get();
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    final double pv = (Double) input.getValue();
    final MultipleCurrencyAmount mca = MultipleCurrencyAmount.of(currency, pv);
    final ValueSpecification spec = new ValueSpecification(FX_CURRENCY_EXPOSURE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, mca));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getTrade().getSecurity();
    if (security instanceof FXForwardSecurity ||
        security instanceof NonDeliverableFXForwardSecurity ||
        security instanceof FXOptionSecurity ||
        security instanceof NonDeliverableFXOptionSecurity ||
        security instanceof FXDigitalOptionSecurity ||
        security instanceof NonDeliverableFXDigitalOptionSecurity ||
        security instanceof FXBarrierOptionSecurity ||
        security instanceof FXVolatilitySwapSecurity ||
        security instanceof FXFutureSecurity) {
      return false;
    }
    return true;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = ValueProperties.all();
    return Collections.singleton(new ValueSpecification(FX_CURRENCY_EXPOSURE, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode();
    final ValueProperties constraints = desiredValue.getConstraints().copy()
        .withoutAny(CURRENCY)
        .with(CURRENCY, currency)
        .get();
    return Collections.singleton(new ValueRequirement(PRESENT_VALUE, target.toSpecification(), constraints));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    if (inputs.size() != 1) {
      return null;
    }
    final Entry<ValueSpecification, ValueRequirement> input = Iterables.getOnlyElement(inputs.entrySet());
    final ValueProperties properties = input.getKey().getProperties().copy()
        .withoutAny(CURRENCY)
        .withoutAny(FUNCTION)
        .with(FUNCTION, getUniqueId())
        .get();
    return Collections.singleton(new ValueSpecification(FX_CURRENCY_EXPOSURE, target.toSpecification(), properties));
  }

}
