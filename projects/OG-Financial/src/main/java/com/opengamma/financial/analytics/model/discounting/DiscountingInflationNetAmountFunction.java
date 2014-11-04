/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.discounting;

import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.INFLATION_NET_AMOUNT;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.inflation.NetAmountInflationCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationProviderInterface;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the net amount of inflation swaps using curves constructed
 * using the discounting method.
 */
public class DiscountingInflationNetAmountFunction extends DiscountingInflationFunction {
  /** The net amount calculator */
  private static final InstrumentDerivativeVisitor<ParameterInflationProviderInterface, MultipleCurrencyAmount> CALCULATOR =
      NetAmountInflationCalculator.getInstance();

  /**
   * Sets the value requirements to {@link ValueRequirementNames#INFLATION_NET_AMOUNT}
   */
  public DiscountingInflationNetAmountFunction() {
    super(INFLATION_NET_AMOUNT);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new DiscountingInflationCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
          final ComputationTarget target, final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative, final FXMatrix fxMatrix) {
        final InflationProviderInterface data = (InflationProviderInterface) inputs.getValue(CURVE_BUNDLE);
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final ValueProperties properties = desiredValue.getConstraints().copy().get();
        final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
        final MultipleCurrencyAmount mca = derivative.accept(CALCULATOR, data);
        final ValueSpecification spec = new ValueSpecification(INFLATION_NET_AMOUNT, target.toSpecification(), properties);
        return Collections.singleton(new ComputedValue(spec, mca.getAmount(currency)));
      }
    };
  }

}
