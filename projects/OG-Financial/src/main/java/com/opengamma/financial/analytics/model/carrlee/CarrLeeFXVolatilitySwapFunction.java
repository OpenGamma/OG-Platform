/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.carrlee;

import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;
import static com.opengamma.engine.value.ValueRequirementNames.REALIZED_VARIANCE;
import static com.opengamma.engine.value.ValueRequirementNames.SPOT_RATE;
import static com.opengamma.engine.value.ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA;
import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.HISTORICAL_REALIZED_VARIANCE;
import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.HISTORICAL_VARIANCE_END;
import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.HISTORICAL_VARIANCE_START;
import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.MARKET_REALIZED_VARIANCE;
import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.PROPERTY_REALIZED_VARIANCE_METHOD;
import static com.opengamma.financial.analytics.model.InstrumentTypeProperties.FOREX;
import static com.opengamma.financial.analytics.model.InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.X_INTERPOLATOR_NAME;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.volatilityswap.CarrLeeFXData;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.TradeConverter;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.security.fx.FXVolatilitySwapSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Base function for FX volatility swaps priced using the Carr-Lee method.
 */
public abstract class CarrLeeFXVolatilitySwapFunction extends CarrLeeVolatilitySwapFunction {

  /**
   * @param valueRequirementNames The value requirement names, not null
   */
  public CarrLeeFXVolatilitySwapFunction(final String... valueRequirementNames) {
    super(valueRequirementNames);
  }

  /**
   * Base compiled function for FX volatility swaps priced using the Carr-Lee method.
   */
  protected abstract class CarrLeeFXVolatilitySwapCompiledFunction extends CarrLeeVolatilitySwapCompiledFunction {

    /**
     * @param tradeToDefinitionConverter Converts targets to definitions, not null
     * @param definitionToDerivativeConverter Converts definitions to derivatives, not nul
     * @param withCurrency True if the {@link ValuePropertyNames#CURRENCY} result property is set
     */
    protected CarrLeeFXVolatilitySwapCompiledFunction(final TradeConverter tradeToDefinitionConverter,
        final FixedIncomeConverterDataProvider definitionToDerivativeConverter, final boolean withCurrency) {
      super(tradeToDefinitionConverter, definitionToDerivativeConverter, withCurrency);
    }

    @Override
    protected ValueRequirement getVolatilitySurfaceRequirement(final ValueRequirement desiredValue, final ComputationTarget target) {
      final ValueProperties constraints = desiredValue.getConstraints();
      final String interpolatorName = Iterables.getOnlyElement(constraints.getValues(X_INTERPOLATOR_NAME));
      final String leftExtrapolatorName = Iterables.getOnlyElement(constraints.getValues(LEFT_X_EXTRAPOLATOR_NAME));
      final String rightExtrapolatorName = Iterables.getOnlyElement(constraints.getValues(RIGHT_X_EXTRAPOLATOR_NAME));
      final String surface = Iterables.getOnlyElement(constraints.getValues(SURFACE));
      final FXVolatilitySwapSecurity swap = (FXVolatilitySwapSecurity) target.getTrade().getSecurity();
      final Currency counterCurrency = swap.getCounterCurrency();
      final Currency baseCurrency = swap.getBaseCurrency();
      final ValueProperties properties = ValueProperties.builder()
          .with(SURFACE, surface)
          .with(PROPERTY_SURFACE_INSTRUMENT_TYPE, FOREX)
          .with(X_INTERPOLATOR_NAME, interpolatorName)
          .with(LEFT_X_EXTRAPOLATOR_NAME, leftExtrapolatorName)
          .with(RIGHT_X_EXTRAPOLATOR_NAME, rightExtrapolatorName)
          .get();
      final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(counterCurrency, baseCurrency);
      return new ValueRequirement(STANDARD_VOLATILITY_SURFACE_DATA, ComputationTargetType.UNORDERED_CURRENCY_PAIR.specification(currencyPair), properties);
    }

    @Override
    protected ValueRequirement getSpotRequirement(final ComputationTarget target) {
      final FXVolatilitySwapSecurity swap = (FXVolatilitySwapSecurity) target.getTrade().getSecurity();
      final CurrencyPair currencyPair = CurrencyPair.of(swap.getBaseCurrency(), swap.getCounterCurrency());
      return new ValueRequirement(SPOT_RATE, CurrencyPair.TYPE.specification(currencyPair));
    }

    @Override
    protected ValueRequirement getRealizedVarianceRequirement(final ValueRequirement desiredValue, final ComputationTarget target) {
      final ValueProperties constraints = desiredValue.getConstraints();
      final ValueProperties.Builder properties = ValueProperties.builder();
      final String varianceCalculationMethod = Iterables.getOnlyElement(constraints.getValues(PROPERTY_REALIZED_VARIANCE_METHOD));
      if (MARKET_REALIZED_VARIANCE.equals(varianceCalculationMethod)) {
        properties.with(PROPERTY_REALIZED_VARIANCE_METHOD, MARKET_REALIZED_VARIANCE);
      } else if (HISTORICAL_REALIZED_VARIANCE.equals(varianceCalculationMethod)) {
        properties.with(PROPERTY_REALIZED_VARIANCE_METHOD, MARKET_REALIZED_VARIANCE)
          .with(HISTORICAL_VARIANCE_START, constraints.getValues(HISTORICAL_VARIANCE_START))
          .with(HISTORICAL_VARIANCE_END, constraints.getValues(HISTORICAL_VARIANCE_END));
      }
      final FXVolatilitySwapSecurity swap = (FXVolatilitySwapSecurity) target.getTrade().getSecurity();
      final Currency counterCurrency = swap.getCounterCurrency();
      final Currency baseCurrency = swap.getBaseCurrency();
      final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(counterCurrency, baseCurrency);
      return new ValueRequirement(REALIZED_VARIANCE, ComputationTargetType.UNORDERED_CURRENCY_PAIR.specification(currencyPair), properties.get());
    }

    @Override
    protected CarrLeeFXData getCarrLeeData(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final FXMatrix fxMatrix) {
      final FXVolatilitySwapSecurity security = (FXVolatilitySwapSecurity) target.getTrade().getSecurity();
      final MulticurveProviderInterface data = getMergedProviders(inputs, fxMatrix);
      final SmileDeltaTermStructureParametersStrikeInterpolation volatilitySurface = (SmileDeltaTermStructureParametersStrikeInterpolation) inputs.getValue(STANDARD_VOLATILITY_SURFACE_DATA);
      final Pair<Currency, Currency> currencyPair = Pairs.of(security.getBaseCurrency(), security.getCounterCurrency());
      return new CarrLeeFXData(currencyPair, volatilitySurface, data);
    }

  }
}
