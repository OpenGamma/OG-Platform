/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fx;

import static com.opengamma.engine.value.ValuePropertyNames.FORWARD_CURVE_NAME;
import static com.opengamma.engine.value.ValueRequirementNames.BLOCK_CURVE_SENSITIVITIES;
import static com.opengamma.engine.value.ValueRequirementNames.CURRENCY_PAIRS;

import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
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
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public class FXForwardPointsBCSFunction extends FXForwardPointsFunction {
  /** The curve sensitivity calculator */
  private static final InstrumentDerivativeVisitor<MulticurveForwardPointsProviderInterface, MultipleCurrencyMulticurveSensitivity> PVCSDC =
      PresentValueCurveSensitivityForexForwardPointsCalculator.getInstance();
  /** The parameter sensitivity calculator */
  private static final ParameterSensitivityParameterCalculator<MulticurveForwardPointsProviderInterface> PSC =
      new ParameterSensitivityParameterCalculator<>(PVCSDC);
  /** The market quote sensitivity calculator */
  private static final MarketQuoteSensitivityBlockCalculator<MulticurveForwardPointsProviderInterface> CALCULATOR =
      new MarketQuoteSensitivityBlockCalculator<>(PSC);

  public FXForwardPointsBCSFunction() {
    super(BLOCK_CURVE_SENSITIVITIES);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new FXForwardPointsCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), false) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues,
          final Forex forex, final FXMatrix fxMatrix, final ZonedDateTime now) {
        final String fxForwardCurveName = desiredValues.iterator().next().getConstraint(FORWARD_CURVE_NAME);
        final DoublesCurve forwardPoints = getForwardPoints(inputs, fxForwardCurveName, now);
        final CurrencyPairs pairs = (CurrencyPairs) inputs.getValue(CURRENCY_PAIRS);
        final Pair<Currency, Currency> ccyPair;
        final Currency currency1 = forex.getCurrency1();
        final Currency currency2 = forex.getCurrency2();
        if (currency1.equals(pairs.getCurrencyPair(currency1, currency2).getBase())) {
          ccyPair = Pairs.of(currency1, currency2);
        } else {
          ccyPair = Pairs.of(currency2, currency1);
        }
        final MulticurveForwardPointsProviderInterface curves = new MulticurveForwardPointsProvider(getMergedProviders(inputs, fxMatrix), forwardPoints, ccyPair);
        final CurveBuildingBlockBundle blocks = getMergedCurveBuildingBlocks(inputs);
        final MultipleCurrencyParameterSensitivity sensitivities = CALCULATOR.fromInstrument(forex, curves, blocks);
        final Set<Pair<String, Currency>> entries = sensitivities.getAllNamesCurrency();
        final Set<String> curveNames = new HashSet<>();
        for (final Pair<String, Currency> pair : entries) {
          curveNames.add(pair.getFirst());
        }
        final Set<ComputedValue> results = new HashSet<>();
        final ValueProperties properties = Iterables.getOnlyElement(desiredValues).getConstraints().copy().get();
        final ValueSpecification spec = new ValueSpecification(BLOCK_CURVE_SENSITIVITIES, target.toSpecification(), properties);
        results.add(new ComputedValue(spec, sensitivities));
        return results;
      }

    };
  }

}
