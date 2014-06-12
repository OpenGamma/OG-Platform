/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.trs;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_SENSITIVITY_CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.FUNCTION;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.PV01;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.DISCOUNTING;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.discounting.PV01CurveParametersCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueCurveSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.util.amount.ReferenceAmount;
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
import com.opengamma.financial.security.swap.BondTotalReturnSwapSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Calculates the PV01 of a bond total return swap security.
 */
public class BondTotalReturnSwapPV01Function extends BondTotalReturnSwapFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(BondTotalReturnSwapPV01Function.class);
  /** The calculator */
  private static final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, ReferenceAmount<Pair<String, Currency>>> CALCULATOR =
      new PV01CurveParametersCalculator<>(PresentValueCurveSensitivityIssuerCalculator.getInstance());

  /**
   * Sets the value requirement to {@link ValueRequirementNames#PV01}.
   */
  public BondTotalReturnSwapPV01Function() {
    super(PV01);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new BondTotalReturnSwapCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @SuppressWarnings("synthetic-access")
      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative, final FXMatrix fxMatrix) {
        final IssuerProviderInterface issuerCurves = getMergedWithIssuerProviders(inputs, fxMatrix);
        final ReferenceAmount<Pair<String, Currency>> pv01 = derivative.accept(CALCULATOR, issuerCurves);
        final Set<ComputedValue> results = new HashSet<>();
        for (final ValueRequirement desiredValue : desiredValues) {
          boolean desiredCurveFound = false;
          final ValueProperties properties = desiredValue.getConstraints().copy().get();
          final String desiredCurveName = properties.getStrictValue(CURVE);
          final String desiredSensitivityCurrency = properties.getStrictValue(CURVE_SENSITIVITY_CURRENCY);
          for (final Map.Entry<Pair<String, Currency>, Double> entry : pv01.getMap().entrySet()) {
            final String curveName = entry.getKey().getFirst();
            final String currency = entry.getKey().getSecond().getCode();
            if (desiredCurveName.equals(curveName) && desiredSensitivityCurrency.equals(currency)) {
              desiredCurveFound = true;
              final ValueProperties curveSpecificProperties = properties.copy()
                  .withoutAny(CURRENCY)
                  .with(CURRENCY, currency)
                  .withoutAny(CURVE_SENSITIVITY_CURRENCY)
                  .with(CURVE_SENSITIVITY_CURRENCY, currency)
                  .withoutAny(CURVE)
                  .with(CURVE, curveName)
                  .get();
              final ValueSpecification spec = new ValueSpecification(PV01, target.toSpecification(), curveSpecificProperties);
              results.add(new ComputedValue(spec, entry.getValue()));
            }
          }
          if (!desiredCurveFound) {
            final ValueSpecification spec = new ValueSpecification(PV01, target.toSpecification(), properties);
            results.add(new ComputedValue(spec, 0.));
          }
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
        final Set<String> curveSensitivityCurrencies = constraints.getValues(CURVE_SENSITIVITY_CURRENCY);
        if (curveSensitivityCurrencies == null || curveSensitivityCurrencies.size() != 1) {
          return null;
        }
        return super.getRequirements(context, target, desiredValue);
      }

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext compilationContext, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
        final Set<String> currencies = new HashSet<>();
        final Set<String> curveNames = new HashSet<>();
        final Set<String> functionNames = new HashSet<>();
        for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
          final ValueSpecification specification = entry.getKey();
          if (specification.getValueName().equals(CURVE_BUNDLE)) {
            final ValueProperties constraints = specification.getProperties();
            currencies.addAll(constraints.getValues(CURVE_SENSITIVITY_CURRENCY));
            curveNames.addAll(constraints.getValues(CURVE));
            functionNames.add(constraints.getSingleValue(FUNCTION));
          }
        }
        if (currencies.isEmpty() || curveNames.isEmpty()) {
          s_logger.error("Could not get currencies or curve name properties; have not been set in function(s) called {}", functionNames);
          return null;
        }
        final Set<ValueSpecification> results = new HashSet<>();
        for (final String currency : currencies) {
          for (final String curveName : curveNames) {
            final ValueProperties properties = createValueProperties()
                .with(PROPERTY_CURVE_TYPE, DISCOUNTING)
                .withAny(CURVE_EXPOSURES)
                .with(CURRENCY, currency)
                .with(CURVE_SENSITIVITY_CURRENCY, currency)
                .with(CURVE, curveName)
                .get();
            results.add(new ValueSpecification(PV01, target.toSpecification(), properties));
          }
        }
        return results;
      }

      @SuppressWarnings("synthetic-access")
      @Override
      protected Collection<ValueProperties.Builder> getResultProperties(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
        final ValueProperties.Builder properties = createValueProperties()
            .with(PROPERTY_CURVE_TYPE, DISCOUNTING)
            .withAny(CURVE_EXPOSURES)
            .withAny(CURVE_SENSITIVITY_CURRENCY)
            .withoutAny(CURRENCY)
            .withAny(CURRENCY)
            .withAny(CURVE);
        return Collections.singleton(properties);
      }

      @Override
      protected String getCurrencyOfResult(final BondTotalReturnSwapSecurity security) {
        throw new IllegalStateException("BondTotalReturnSwapPV01Function does not set the Currency property in this method");
      }

    };
  }

}
