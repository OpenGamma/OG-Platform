/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fx;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.FORWARD_CURVE_NAME;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_DEFINITION;
import static com.opengamma.engine.value.ValueRequirementNames.FX_FORWARD_POINTS_NODE_SENSITIVITIES;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.forex.provider.ForexForwardPointsMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.curve.DoublesCurve;
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
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class FXForwardPointsFCNSFunction extends FXForwardPointsFunction {
  private static final ForexForwardPointsMethod CALCULATOR = ForexForwardPointsMethod.getInstance();

  public FXForwardPointsFCNSFunction() {
    super(FX_FORWARD_POINTS_NODE_SENSITIVITIES);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new FXForwardPointsCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues,
          final InstrumentDerivative derivative, final FXMatrix fxMatrix, final ZonedDateTime now) {
        final MulticurveProviderInterface data = getMergedProviders(inputs, fxMatrix);
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final ValueProperties properties = desiredValue.getConstraints().copy().get();
        final String fxForwardCurveName = desiredValue.getConstraint(FORWARD_CURVE_NAME);
        final DoublesCurve forwardPoints = getForwardPoints(inputs, target, fxForwardCurveName, now);
        //TODO: Review this line
        final Pair<Currency, Currency> ccyPair = new ObjectsPair<>(Currency.USD, Currency.JPY);
        final double[] sensitivities = CALCULATOR.presentValueForwardPointsSensitivity((Forex) derivative, data, forwardPoints, ccyPair);
        final CurveDefinition definition = (CurveDefinition) inputs.getValue(
            new ValueRequirement(CURVE_DEFINITION, ComputationTargetSpecification.NULL, ValueProperties.with(CURVE, fxForwardCurveName).get()));
        final DoubleLabelledMatrix1D matrix = MultiCurveUtils.getLabelledMatrix(new DoubleMatrix1D(sensitivities), definition);
        final ValueSpecification spec = new ValueSpecification(FX_FORWARD_POINTS_NODE_SENSITIVITIES, target.toSpecification(), properties);
        return Collections.singleton(new ComputedValue(spec, matrix));
      }
    };
  }
}
