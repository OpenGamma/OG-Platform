/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.horizon;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.FUNCTION;
import static com.opengamma.engine.value.ValueRequirementNames.CURRENCY_PAIRS;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.PROPERTY_DAYS_TO_MOVE_FORWARD;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.PROPERTY_THETA_CALCULATION_METHOD;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.currency.CurrencyMatrixSpotSourcingFunction;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Converts a the multi-valued value theta for FX Forward instruments into a single result.
 * @deprecated Deprecated
 */
@Deprecated
public class FXForwardConstantSpreadSingleThetaFunction extends FXForwardConstantSpreadThetaFunction {
  /** The calculation method property value */
  public static final String CONSTANT_SPREAD_COLLAPSED = "ConstantSpreadCollapsed";

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Set<ComputedValue> computedValues = super.execute(executionContext, inputs, target, desiredValues);
    if (computedValues.size() != 1) {
      throw new OpenGammaRuntimeException("Expecting only one computed value");
    }
    final ComputedValue computedValue = Iterables.getOnlyElement(computedValues);
    final ValueSpecification spec = computedValue.getSpecification();
    final Object thetaObject = computedValue.getValue();
    if (!(thetaObject instanceof MultipleCurrencyAmount)) {
      throw new OpenGammaRuntimeException("Value theta did not have expected type MultipleCurrencyAmount: have " + thetaObject.getClass());
    }
    final MultipleCurrencyAmount theta = (MultipleCurrencyAmount) thetaObject;
    if (theta.size() != 2) {
      throw new OpenGammaRuntimeException("Expected value theta to have two values; have " + theta.size());
    }
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    final CurrencyPairs currencyPairs = (CurrencyPairs) inputs.getValue(CURRENCY_PAIRS);
    final CurrencyPair currencyPair = currencyPairs.getCurrencyPair(payCurrency, receiveCurrency);
    final double scale;
    if (payCurrency.equals(currencyPair.getBase())) {
      scale = 1;
    } else {
      scale = -1;
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    int payIndex = -1;
    int receiveIndex = -1;
    final CurrencyAmount[] currencyAmounts = theta.getCurrencyAmounts();
    for (int i = 0; i < 2; i++) {
      final Currency currency = currencyAmounts[i].getCurrency();
      if (payCurrency.equals(currency)) {
        payIndex = i;
      } else if (receiveCurrency.equals(currency)) {
        receiveIndex = i;
      } else {
        throw new OpenGammaRuntimeException("Value theta contains unexpected currency " + currency + ". Expected " + payCurrency + " or " + receiveCurrency + ".");
      }
    }
    final double payValue = currencyAmounts[payIndex].getAmount();
    final double receiveValue = currencyAmounts[receiveIndex].getAmount();
    final double spot = (Double) inputs.getValue(ValueRequirementNames.SPOT_RATE);

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    final double singleTheta = scale * (payValue + spot * receiveValue);
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    final ValueProperties properties = spec.getProperties().copy()
        .withoutAny(FUNCTION)
        .with(FUNCTION, getUniqueId())
        .withoutAny(PROPERTY_THETA_CALCULATION_METHOD)
        .with(PROPERTY_THETA_CALCULATION_METHOD, CONSTANT_SPREAD_COLLAPSED)
        .with(CURRENCY, ((FinancialSecurity) target.getSecurity()).accept(ForexVisitors.getPayCurrencyVisitor()).getCode())
        .get();
    final ValueSpecification convertedSpec = new ValueSpecification(spec.getValueName(), spec.getTargetSpecification(), properties);
    return Collections.singleton(new ComputedValue(convertedSpec, singleTheta));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
    final ValueRequirement spotRateRequirement = CurrencyMatrixSpotSourcingFunction.getConversionRequirement(payCurrency, receiveCurrency);
    requirements.add(spotRateRequirement);
    return requirements;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    final ValueProperties.Builder properties = super.getResultProperties(target)
        .withoutAny(PROPERTY_THETA_CALCULATION_METHOD)
        .with(PROPERTY_THETA_CALCULATION_METHOD, CONSTANT_SPREAD_COLLAPSED)
        .with(CURRENCY, ((FinancialSecurity) target.getSecurity()).accept(ForexVisitors.getPayCurrencyVisitor()).getCode());
    return properties;
  }

  /**
   * Gets the result properties with property values set.
   *
   * @param target The target
   * @param payCurveName The name of the pay curve
   * @param payCurveCalculationConfig The name of the pay curve calculation configuration
   * @param receiveCurveName The name of the receive curve
   * @param receiveCurveCalculationConfig The name of the receive curve calculation configuration
   * @param baseQuotePair The base / counter information for the currency pair
   * @param daysForward The number of days forward
   * @return The result properties
   */
  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final String payCurveName, final String receiveCurveName,
      final String payCurveCalculationConfig, final String receiveCurveCalculationConfig, final CurrencyPair baseQuotePair, final String daysForward) {
    final ValueProperties.Builder properties = super.getResultProperties(target, payCurveName, receiveCurveName, payCurveCalculationConfig, receiveCurveCalculationConfig,
        baseQuotePair, daysForward)
        .withoutAny(PROPERTY_THETA_CALCULATION_METHOD)
        .with(PROPERTY_THETA_CALCULATION_METHOD, CONSTANT_SPREAD_COLLAPSED)
        .with(CURRENCY, ((FinancialSecurity) target.getSecurity()).accept(ForexVisitors.getPayCurrencyVisitor()).getCode());
    return properties;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final ValueRequirement desiredValue) {
    final String daysForward = desiredValue.getConstraint(PROPERTY_DAYS_TO_MOVE_FORWARD);
    final ValueProperties.Builder properties = super.getResultProperties(target, desiredValue)
        .withoutAny(PROPERTY_THETA_CALCULATION_METHOD)
        .with(PROPERTY_THETA_CALCULATION_METHOD, CONSTANT_SPREAD_COLLAPSED)
        .with(CURRENCY, ((FinancialSecurity) target.getSecurity()).accept(ForexVisitors.getPayCurrencyVisitor()).getCode());
    return properties;
  }
}
