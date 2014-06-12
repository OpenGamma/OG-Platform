/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.trs;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValueRequirementNames.ASSET_LEG_PV;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.EquityTrsDataBundle;
import com.opengamma.analytics.financial.equity.trs.calculator.EqyTrsAssetLegPresentValueCalculator;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
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
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value of the asset leg of an equity total return swap security.
 */
public class EquityTotalReturnSwapAssetLegPVFunction extends EquityTotalReturnSwapFunction {
  /** The calculator */
  private static final InstrumentDerivativeVisitor<EquityTrsDataBundle, MultipleCurrencyAmount> CALCULATOR =
      EqyTrsAssetLegPresentValueCalculator.getInstance();

  /**
   * Sets the value requirement to {@link ValueRequirementNames#ASSET_LEG_PV}.
   */
  public EquityTotalReturnSwapAssetLegPVFunction() {
    super(ASSET_LEG_PV);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new EquityTotalReturnSwapCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @SuppressWarnings("synthetic-access")
      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative, final FXMatrix fxMatrix) {
        final ValueProperties properties = Iterables.getOnlyElement(desiredValues).getConstraints().copy().get();
        final ValueSpecification spec = new ValueSpecification(ASSET_LEG_PV, target.toSpecification(), properties);
        final EquityTrsDataBundle data = getDataBundle(inputs, fxMatrix);
        final MultipleCurrencyAmount pv = derivative.accept(CALCULATOR, data);
        final String expectedCurrency = spec.getProperty(CURRENCY);
        if (pv.size() != 1 || !(expectedCurrency.equals(pv.getCurrencyAmounts()[0].getCurrency().getCode()))) {
          throw new OpenGammaRuntimeException("Expecting a single result in " + expectedCurrency);
        }
        return Collections.singleton(new ComputedValue(spec, pv.getCurrencyAmounts()[0].getAmount()));
      }
    };
  }

}
