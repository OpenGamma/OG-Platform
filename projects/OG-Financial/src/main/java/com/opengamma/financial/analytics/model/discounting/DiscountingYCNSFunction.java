/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.discounting;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValueRequirementNames.BLOCK_CURVE_SENSITIVITIES;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_DEFINITION;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.DISCOUNTING;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.analytics.model.multicurve.MultiCurveUtils;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Calculates the yield curve node sensitivities of instruments using curves constructed using the discounting method.
 */
public class DiscountingYCNSFunction extends DiscountingFunction {
  
  /** The constraint name to select the currency for which the sensitivity is returned */
  public static final String SENSITIVITY_CURRENCY_PROPERTY = "SensitivityCurrency";

  /**
   * Sets the value requirements to {@link ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES}
   */
  public DiscountingYCNSFunction() {
    super(YIELD_CURVE_NODE_SENSITIVITIES);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new DiscountingCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative, final FXMatrix fxMatrix) {
        final MultipleCurrencyParameterSensitivity sensitivities = (MultipleCurrencyParameterSensitivity) inputs.getValue(BLOCK_CURVE_SENSITIVITIES);
        final Map<Pair<String, Currency>, DoubleMatrix1D> sensitivityEntries = sensitivities.getSensitivities();
        final Set<ComputedValue> results = Sets.newHashSetWithExpectedSize(desiredValues.size());
        for (ValueRequirement desiredValue : desiredValues) {
          final String curveName = desiredValue.getConstraint(CURVE);
          final CurveDefinition curveDefinition = (CurveDefinition) inputs.getValue(new ValueRequirement(CURVE_DEFINITION, ComputationTargetSpecification.NULL, ValueProperties.builder()
              .with(CURVE, curveName).get()));
          final String currency = desiredValue.getConstraints().getSingleValue(SENSITIVITY_CURRENCY_PROPERTY);
          DoubleMatrix1D sensitivityMatrix = null;
          if (currency != null) {
            // Currency is specified - lookup directly
            sensitivityMatrix = sensitivityEntries.get(Pairs.of(curveName, Currency.of(currency)));
          } else {
            // No currency constraint so make an arbitrary choice.
            for (Map.Entry<Pair<String, Currency>, DoubleMatrix1D> sensitivityEntry : sensitivityEntries.entrySet()) {
              if (curveName.equals(sensitivityEntry.getKey().getFirst())) {
                sensitivityMatrix = sensitivityEntry.getValue();
                break;
              }
            }
          }
          if (sensitivityMatrix == null) {
            final double[] zeroes = new double[curveDefinition.getNodes().size()];
            sensitivityMatrix = new DoubleMatrix1D(zeroes);
          }
          final ValueSpecification valueSpec = new ValueSpecification(YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), desiredValue.getConstraints());
          final DoubleLabelledMatrix1D ycns = MultiCurveUtils.getLabelledMatrix(sensitivityMatrix, curveDefinition);
          results.add(new ComputedValue(valueSpec, ycns));
        }
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
        final ValueProperties properties = ValueProperties.with(PROPERTY_CURVE_TYPE, DISCOUNTING).with(CURVE_EXPOSURES, curveExposureConfigs).get();
        final ValueProperties curveProperties = ValueProperties.with(CURVE, curveNames).get();
        final Set<ValueRequirement> requirements = new HashSet<>();
        final FinancialSecurity security = (FinancialSecurity) target.getTrade().getSecurity();
        final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
        requirements.add(new ValueRequirement(CURVE_DEFINITION, ComputationTargetSpecification.NULL, curveProperties));
        requirements.add(new ValueRequirement(BLOCK_CURVE_SENSITIVITIES, target.toSpecification(), properties));
        requirements.addAll(getFXRequirements(security, securitySource));
        final Set<ValueRequirement> tsRequirements = getTimeSeriesRequirements(context, target);
        if (tsRequirements == null) {
          return null;
        }
        requirements.addAll(tsRequirements);
        return requirements;
      }

      private Currency getLegCurrency(final SwapLeg leg) {
        return (leg.getNotional() instanceof InterestRateNotional) ? ((InterestRateNotional) leg.getNotional()).getCurrency() : null;
      }      private Collection<ValueProperties.Builder> addCurrencies(final ValueProperties.Builder properties, final Currency c1, final Currency c2) {
        if (c1 != null) {
          if (c2 != null) {
            final List<ValueProperties.Builder> result = new ArrayList<ValueProperties.Builder>();
            result.add(properties.copy().with(SENSITIVITY_CURRENCY_PROPERTY, c1.getCode()).with(CURRENCY, c1.getCode()));
            result.add(properties.with(SENSITIVITY_CURRENCY_PROPERTY, c2.getCode()).with(CURRENCY, c2.getCode()));
            return result;
          } else {
            return Collections.singleton(properties.with(SENSITIVITY_CURRENCY_PROPERTY, c1.getCode()).with(CURRENCY, c1.getCode()));
          }
        } else {
          if (c2 != null) {
            return Collections.singleton(properties.with(SENSITIVITY_CURRENCY_PROPERTY, c2.getCode()).with(CURRENCY, c2.getCode()));
          } else {
            return Collections.singleton(properties.withAny(SENSITIVITY_CURRENCY_PROPERTY).withAny(CURRENCY));
          }
        }
      }

      @SuppressWarnings("synthetic-access")
      @Override
      protected Collection<ValueProperties.Builder> getResultProperties(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
        final ValueProperties.Builder properties = createValueProperties().with(PROPERTY_CURVE_TYPE, DISCOUNTING).withAny(CURVE_EXPOSURES).withAny(CURVE);
        final Security security = target.getTrade().getSecurity();
        if (security instanceof SwapSecurity && InterestRateInstrumentType.isFixedIncomeInstrumentType((SwapSecurity) security)) {
          final SwapSecurity swapSecurity = (SwapSecurity) security;
          final Currency pay = getLegCurrency(swapSecurity.getPayLeg());
          final Currency receive = getLegCurrency(swapSecurity.getReceiveLeg());
          return addCurrencies(properties, pay, receive);
        } else if (security instanceof FXForwardSecurity || security instanceof NonDeliverableFXForwardSecurity) {
          final Currency pay = ((FinancialSecurity) security).accept(ForexVisitors.getPayCurrencyVisitor());
          final Currency receive = ((FinancialSecurity) security).accept(ForexVisitors.getReceiveCurrencyVisitor());
          return addCurrencies(properties, pay, receive);
        } else {
          final String ccy = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode();
          return Collections.singleton(properties.with(SENSITIVITY_CURRENCY_PROPERTY, ccy).with(CURRENCY, ccy));
        }
      }

    };
  }
}
