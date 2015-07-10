/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fx;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValuePropertyNames.FORWARD_CURVE_NAME;
import static com.opengamma.engine.value.ValueRequirementNames.BLOCK_CURVE_SENSITIVITIES;
import static com.opengamma.engine.value.ValueRequirementNames.CURRENCY_PAIRS;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_DEFINITION;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;
import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.FORWARD_POINTS;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.model.multicurve.MultiCurveUtils;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.security.CurrenciesVisitor;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class FXForwardPointsYCNSFunction extends FXForwardPointsFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(FXForwardPointsYCNSFunction.class);

  public FXForwardPointsYCNSFunction() {
    super(YIELD_CURVE_NODE_SENSITIVITIES);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new FXForwardPointsCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues,
          final Forex forex, final FXMatrix fxMatrix, final ZonedDateTime now) {
        final MultipleCurrencyParameterSensitivity sensitivities = (MultipleCurrencyParameterSensitivity) inputs.getValue(BLOCK_CURVE_SENSITIVITIES);
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final String curveName = desiredValue.getConstraint(CURVE);
        final Map<Pair<String, Currency>, DoubleMatrix1D> entries = sensitivities.getSensitivities();
        for (final Map.Entry<Pair<String, Currency>, DoubleMatrix1D> entry : entries.entrySet()) {
          if (curveName.equals(entry.getKey().getFirst())) {
            final ValueProperties properties = desiredValue.getConstraints().copy()
                .with(CURVE, curveName)
                .get();
            final CurveDefinition curveDefinition = (CurveDefinition) inputs.getValue(new ValueRequirement(CURVE_DEFINITION, ComputationTargetSpecification.NULL,
                ValueProperties.builder().with(CURVE, curveName).get()));
            final ValueSpecification spec = new ValueSpecification(YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), properties);
            final DoubleLabelledMatrix1D ycns = MultiCurveUtils.getLabelledMatrix(entry.getValue(), curveDefinition);
            return Collections.singleton(new ComputedValue(spec, ycns));
          }
        }
        s_logger.info("Could not get sensitivities to " + curveName + " for " + target.getName());
        return Collections.emptySet();
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
        final Set<String> fxForwardCurveNames = constraints.getValues(FORWARD_CURVE_NAME);
        if (fxForwardCurveNames == null || fxForwardCurveNames.size() != 1) {
          return null;
        }
        final ValueProperties properties = ValueProperties
            .with(PROPERTY_CURVE_TYPE, FORWARD_POINTS)
            .with(CURVE_EXPOSURES, curveExposureConfigs)
            .with(FORWARD_CURVE_NAME, fxForwardCurveNames)
            .get();
        final ValueProperties curveProperties = ValueProperties
            .with(CURVE, curveNames)
            .get();
        final Set<ValueRequirement> requirements = new HashSet<>();
        final FinancialSecurity security = (FinancialSecurity) target.getTrade().getSecurity();
        final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
        final Collection<Currency> currencies = CurrenciesVisitor.getCurrencies(security, securitySource);
        if (currencies.size() > 1) {
          final Iterator<Currency> iter = currencies.iterator();
          final Currency initialCurrency = iter.next();
          while (iter.hasNext()) {
            requirements.add(new ValueRequirement(ValueRequirementNames.SPOT_RATE, CurrencyPair.TYPE.specification(CurrencyPair.of(iter.next(), initialCurrency))));
          }
        }
        requirements.add(new ValueRequirement(CURVE_DEFINITION, ComputationTargetSpecification.NULL, curveProperties));
        requirements.add(new ValueRequirement(BLOCK_CURVE_SENSITIVITIES, target.toSpecification(), properties));
        requirements.add(new ValueRequirement(CURRENCY_PAIRS, ComputationTargetSpecification.NULL, ValueProperties.none()));
        return requirements;
      }

      @Override
      protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
        final ValueProperties.Builder properties = super.getResultProperties(target);
        return properties.withAny(CURVE);
      }
    };
  }

}
