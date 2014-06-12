/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.discounting;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValueRequirementNames.ALL_PV01S;
import static com.opengamma.engine.value.ValueRequirementNames.PV01;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.DISCOUNTING;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.core.security.Security;
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
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Gets the PV01 of an instrument to a named curve using curves constructed with the discounting method.
 */
public class DiscountingPV01Function extends DiscountingFunction {

  /**
   * Sets the value requirements to {@link ValueRequirementNames#PV01}
   */
  public DiscountingPV01Function() {
    super(PV01);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new DiscountingCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final String desiredCurveName = desiredValue.getConstraint(CURVE);
        final ValueProperties properties = desiredValue.getConstraints();
        final Object allPV01s = inputs.getValue(ALL_PV01S);
        if (allPV01s == null) {
          throw new OpenGammaRuntimeException("Could not get requirement: ALL_PV01S");
        }
        @SuppressWarnings("unchecked")
        final Map<Pair<String, Currency>, Double> pv01s = (Map<Pair<String, Currency>, Double>) allPV01s;
        final Set<ComputedValue> results = new HashSet<>();
        for (final Map.Entry<Pair<String, Currency>, Double> entry : pv01s.entrySet()) {
          final String curveName = entry.getKey().getFirst();
          if (desiredCurveName.equals(curveName)) {
            final ValueProperties curveSpecificProperties = properties.copy().withoutAny(CURVE).with(CURVE, curveName).get();
            final ValueSpecification spec = new ValueSpecification(PV01, target.toSpecification(), curveSpecificProperties);
            results.add(new ComputedValue(spec, entry.getValue()));
            return results;
          }
        }
        final ValueProperties curveSpecificProperties = properties.copy().withoutAny(CURVE).with(CURVE, desiredCurveName).get();
        final ValueSpecification spec = new ValueSpecification(PV01, target.toSpecification(), curveSpecificProperties);
        results.add(new ComputedValue(spec, 0.));
        return results;
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext compilationContext, final ComputationTarget target, final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final Set<String> curveNames = constraints.getValues(CURVE);
        if (curveNames == null || curveNames.size() != 1) {
          return null;
        }
        final Set<String> curveExposureConfigs = constraints.getValues(CURVE_EXPOSURES);
        if (curveExposureConfigs == null) {
          return null;
        }
        final ValueProperties properties = ValueProperties
            .with(PROPERTY_CURVE_TYPE, DISCOUNTING)
            .with(CURVE_EXPOSURES, curveExposureConfigs)
            .get();
        return Collections.singleton(new ValueRequirement(ALL_PV01S, target.toSpecification(), properties));
      }

      @SuppressWarnings("synthetic-access")
      @Override
      protected Collection<ValueProperties.Builder> getResultProperties(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
        final ValueProperties.Builder properties = createValueProperties()
            .with(PROPERTY_CURVE_TYPE, DISCOUNTING)
            .withAny(CURVE_EXPOSURES)
            .withAny(CURVE);
        final Security security = target.getTrade().getSecurity();
        if (security instanceof SwapSecurity && InterestRateInstrumentType.isFixedIncomeInstrumentType((SwapSecurity) security)) {
          if ((InterestRateInstrumentType.getInstrumentTypeFromSecurity((SwapSecurity) security) != InterestRateInstrumentType.SWAP_CROSS_CURRENCY)) {
            final SwapSecurity swapSecurity = (SwapSecurity) security;
            if (swapSecurity.getPayLeg().getNotional() instanceof InterestRateNotional) {
              final String currency = ((InterestRateNotional) swapSecurity.getPayLeg().getNotional()).getCurrency().getCode();
              properties.with(CURRENCY, currency);
              return Collections.singleton(properties);
            }
          }
          properties.withAny(CURRENCY);
          return Collections.singleton(properties);
        } else if (security instanceof FXForwardSecurity || security instanceof NonDeliverableFXForwardSecurity) {
          properties.with(CURRENCY, ((FinancialSecurity) security).accept(ForexVisitors.getPayCurrencyVisitor()).getCode());
        } else {
          properties.with(CURRENCY, FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode());
        }
        // TODO: Handle instruments with multiple currencies correctly
        return Collections.singleton(properties);
      }

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues,
          final InstrumentDerivative derivative, final FXMatrix fxMatrix) {
        return null;
      }
    };
  }
}
