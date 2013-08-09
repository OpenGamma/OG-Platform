/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.multicurve.discounting;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.BLOCK_CURVE_SENSITIVITIES;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_DEFINITION;
import static com.opengamma.engine.value.ValueRequirementNames.JACOBIAN_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.model.multicurve.MultiCurveUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Calculates the yield curve node sensitivities of bond futures, interest rate futures and
 * deliverable swap futures using curves constructed using the discounting method.
 */
public class DiscountingYCNSFunction extends DiscountingFunction {
  /** The curve sensitivity calculator */
  private static final InstrumentDerivativeVisitor<MulticurveProviderInterface, MultipleCurrencyMulticurveSensitivity> PVCSDC =
      PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  /** The parameter sensitivity calculator */
  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSC =
      new ParameterSensitivityParameterCalculator<>(PVCSDC);
  /** The market quote sensitivity calculator */
  private static final MarketQuoteSensitivityBlockCalculator<MulticurveProviderInterface> CALCULATOR =
      new MarketQuoteSensitivityBlockCalculator<>(PSC);

  /**
   * Sets the value requirements to {@link ValueRequirementNames#BLOCK_CURVE_SENSITIVITIES, ValueRequirementNames#YIELD_CURVE_NODE_SENSITIVITIES}
   */
  public DiscountingYCNSFunction() {
    super(BLOCK_CURVE_SENSITIVITIES, YIELD_CURVE_NODE_SENSITIVITIES);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new DiscountingCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative) {
        final MulticurveProviderInterface curves = (MulticurveProviderInterface) inputs.getValue(CURVE_BUNDLE);
        final CurveBuildingBlockBundle blocks = (CurveBuildingBlockBundle) inputs.getValue(JACOBIAN_BUNDLE);
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final String desiredCurveName = desiredValue.getConstraint(CURVE);
        final ValueProperties properties = desiredValue.getConstraints().copy().get();
        final MultipleCurrencyParameterSensitivity sensitivities = CALCULATOR.fromInstrument(derivative, curves, blocks);
        final Set<ComputedValue> results = new HashSet<>();
        boolean curveNameFound = false;
        final ValueProperties blockProperties = getResultProperties(target).get();
        final ValueSpecification spec = new ValueSpecification(BLOCK_CURVE_SENSITIVITIES, target.toSpecification(), blockProperties);
        results.add(new ComputedValue(spec, sensitivities));
        for (final Map.Entry<Pair<String, Currency>, DoubleMatrix1D> entry : sensitivities.getSensitivities().entrySet()) {
          final String curveName = entry.getKey().getFirst();
          if (desiredCurveName.equals(curveName)) {
            curveNameFound = true;
          }
          final ValueProperties curveSpecificProperties = properties.copy()
              .withoutAny(CURVE)
              .with(CURVE, curveName)
              .get();
          final CurveDefinition curveDefinition = (CurveDefinition) inputs.getValue(new ValueRequirement(CURVE_DEFINITION, ComputationTargetSpecification.NULL,
              ValueProperties.builder().with(CURVE, curveName).get()));
          final ValueSpecification ycnsSpec = new ValueSpecification(YIELD_CURVE_NODE_SENSITIVITIES, target.toSpecification(), curveSpecificProperties);
          final DoubleLabelledMatrix1D ycns = MultiCurveUtils.getLabelledMatrix(entry.getValue(), curveDefinition);
          results.add(new ComputedValue(ycnsSpec, ycns));
        }
        if (!curveNameFound) {
          throw new OpenGammaRuntimeException("Could not get yield curve node sensitivities for curve named " + desiredCurveName);
        }
        return results;
      }

      @Override
      protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
        final ValueProperties.Builder properties = super.getResultProperties(target);
        return properties.withAny(CURVE);
      }

      @Override
      protected boolean requirementsSet(final ValueProperties constraints) {
        if (super.requirementsSet(constraints)) {
          final Set<String> curves = constraints.getValues(CURVE);
          if (curves == null) {
            return false;
          }
          return true;
        }
        return false;
      }
    };
  }
}
