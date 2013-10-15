/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fx;

import static com.opengamma.engine.value.ValuePropertyNames.FORWARD_CURVE_NAME;
import static com.opengamma.engine.value.ValueRequirementNames.CURRENCY_PAIRS;
import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.forex.provider.ForexForwardPointsMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
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
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public class FXForwardPointsPVFunction extends FXForwardPointsFunction {
  private static final ForexForwardPointsMethod CALCULATOR = ForexForwardPointsMethod.getInstance();

  public FXForwardPointsPVFunction() {
    super(PRESENT_VALUE);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new FXForwardPointsCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues,
          final Forex forex, final FXMatrix fxMatrix, final ZonedDateTime now) {
        final MulticurveProviderInterface data = getMergedProviders(inputs, fxMatrix);
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final ValueProperties properties = desiredValue.getConstraints().copy().get();
        final String fxForwardCurveName = desiredValue.getConstraint(FORWARD_CURVE_NAME);
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
        final MultipleCurrencyAmount mca = CALCULATOR.presentValue(forex, data, forwardPoints, ccyPair);
        if (mca.size() != 1) {
          throw new OpenGammaRuntimeException("Expecting a single value for present value");
        }
        final CurrencyAmount ca = mca.getCurrencyAmounts()[0];
        final ValueSpecification spec = new ValueSpecification(PRESENT_VALUE, target.toSpecification(), properties);
        return Collections.singleton(new ComputedValue(spec, ca.getAmount()));
      }
    };
  }

}
