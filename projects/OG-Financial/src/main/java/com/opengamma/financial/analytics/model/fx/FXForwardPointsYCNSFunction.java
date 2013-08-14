/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fx;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.FORWARD_CURVE_NAME;
import static com.opengamma.engine.value.ValueRequirementNames.BLOCK_CURVE_SENSITIVITIES;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_DEFINITION;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.forexpoints.PresentValueCurveSensitivityForexForwardPointsCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveForwardPointsProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveForwardPointsProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
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
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.analytics.model.multicurve.MultiCurveUtils;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class FXForwardPointsYCNSFunction extends FXForwardPointsFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(FXForwardPointsYCNSFunction.class);
  /** The curve sensitivity calculator */
  private static final InstrumentDerivativeVisitor<MulticurveForwardPointsProviderInterface, MultipleCurrencyMulticurveSensitivity> PVCSDC =
      PresentValueCurveSensitivityForexForwardPointsCalculator.getInstance();
  /** The parameter sensitivity calculator */
  private static final ParameterSensitivityParameterCalculator<MulticurveForwardPointsProviderInterface> PSC =
      new ParameterSensitivityParameterCalculator<>(PVCSDC);
  /** The market quote sensitivity calculator */
  private static final MarketQuoteSensitivityBlockCalculator<MulticurveForwardPointsProviderInterface> CALCULATOR =
      new MarketQuoteSensitivityBlockCalculator<>(PSC);

  public FXForwardPointsYCNSFunction() {
    super(YIELD_CURVE_NODE_SENSITIVITIES);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new FXForwardPointsCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues,
          final InstrumentDerivative derivative, final FXMatrix fxMatrix, final ZonedDateTime now) {
        final String fxForwardCurveName = desiredValues.iterator().next().getConstraint(FORWARD_CURVE_NAME);
        final DoublesCurve forwardPoints = getForwardPoints(inputs, target, fxForwardCurveName, now);
        final FinancialSecurity security = (FinancialSecurity) target.getTrade().getSecurity();
        final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
        final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
        final Pair<Currency, Currency> ccyPair = Pair.of(payCurrency, receiveCurrency);
        final MulticurveForwardPointsProviderInterface curves = new MulticurveForwardPointsProvider(getMergedProviders(inputs, fxMatrix), forwardPoints, ccyPair);
        final CurveBuildingBlockBundle blocks = getMergedCurveBuildingBlocks(inputs);
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
          s_logger.error("Could not get sensitivities to " + desiredCurveName + " for " + target.getName());
          return Collections.emptySet();
        }
        return results;
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext compilationContext, final ComputationTarget target,
          final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final Set<String> curveNames = constraints.getValues(CURVE);
        if (curveNames == null || curveNames.size() != 1) {
          return null;
        }
        return super.getRequirements(compilationContext, target, desiredValue);
      }

      @Override
      protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
        final ValueProperties.Builder properties = super.getResultProperties(target);
        return properties.withAny(CURVE);
      }

    };
  }

}
