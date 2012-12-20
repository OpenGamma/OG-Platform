/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.forward;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.currency.ConfigDBCurrencyPairsSource;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public class BloombergFXForwardSpotRateFunction extends AbstractFunction.NonCompiledInvoker {
  /** Property indicating the data type required */
  public static final String PROPERTY_DATA_TYPE = "DataType";
  /** Live FX spot rates for a security */
  public static final String LIVE = "Live";
  /** Last close FX spot rates for a security */
  public static final String LAST_CLOSE = "LastClose";

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final String dataType = desiredValue.getConstraint(PROPERTY_DATA_TYPE);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(security.accept(ForexVisitors.getPayCurrencyVisitor()), security.accept(ForexVisitors.getReceiveCurrencyVisitor()));
    if (dataType.equals(LIVE)) {
      final Object spotObject = inputs.getValue(ValueRequirementNames.SPOT_RATE);
      if (spotObject == null) {
        throw new OpenGammaRuntimeException("Could not get live market data for " + currencyPair);
      }
      final double spot = (Double) spotObject;
      return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.SPOT_RATE_FOR_SECURITY, target.toSpecification(),
          createValueProperties().with(PROPERTY_DATA_TYPE, LIVE).get()), spot));
    } else if (dataType.equals(LAST_CLOSE)) {
      final Object spotObject = inputs.getValue(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST);
      if (spotObject == null) {
        throw new OpenGammaRuntimeException("Could not get last close market data for " + currencyPair);
      }
      final double spot = (Double) spotObject;
      return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.SPOT_RATE_FOR_SECURITY, target.toSpecification(),
          createValueProperties().with(PROPERTY_DATA_TYPE, LAST_CLOSE).get()), spot));
    }
    throw new OpenGammaRuntimeException("Did not recognise property type " + dataType);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof FXForwardSecurity || target.getSecurity() instanceof NonDeliverableFXForwardSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.SPOT_RATE_FOR_SECURITY, target.toSpecification(),
        createValueProperties().withAny(PROPERTY_DATA_TYPE).get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> dataTypes = desiredValue.getConstraints().getValues(PROPERTY_DATA_TYPE);
    if (dataTypes == null || dataTypes.size() != 1) {
      return null;
    }
    final String dataType = Iterables.getOnlyElement(dataTypes);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(security.accept(ForexVisitors.getPayCurrencyVisitor()), security.accept(ForexVisitors.getReceiveCurrencyVisitor()));
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBCurrencyPairsSource currencyPairsSource = new ConfigDBCurrencyPairsSource(configSource);
    final CurrencyPairs baseQuotePairs = currencyPairsSource.getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    final CurrencyPair baseQuotePair = baseQuotePairs.getCurrencyPair(currencyPair.getFirstCurrency(), currencyPair.getSecondCurrency());
    if (baseQuotePair == null) {
      throw new OpenGammaRuntimeException("Could not get base/quote pair for currency pair (" + currencyPair.getFirstCurrency() + ", " + currencyPair.getSecondCurrency() + ")");
    }
    if (dataType.equals(LIVE)) {
      return Collections.singleton(new ValueRequirement(ValueRequirementNames.SPOT_RATE, ComputationTargetType.PRIMITIVE, currencyPair.getUniqueId()));
    } else if (dataType.equals(LAST_CLOSE)) {
      final HistoricalTimeSeriesResolver htsResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
      final ExternalId externalId = getBBGId(currencyPair, baseQuotePair);
      final HistoricalTimeSeriesResolutionResult resolutionResult = htsResolver.resolve(ExternalIdBundle.of(externalId), null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
      if (resolutionResult == null) {
        return null;
      }
      final UniqueId htsId = resolutionResult.getHistoricalTimeSeriesInfo().getUniqueId();
      return Collections.singleton(new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST, ComputationTargetType.PRIMITIVE, htsId));
    }
    return null;
  }

  private ExternalId getBBGId(final UnorderedCurrencyPair currencyPair, final CurrencyPair baseQuotePair) {
    if (baseQuotePair == null) {
      throw new OpenGammaRuntimeException("Could not get base/quote pair for currency pair (" + currencyPair.getFirstCurrency() + ", " + currencyPair.getSecondCurrency() + ")");
    }
    return ExternalSchemes.bloombergTickerSecurityId(baseQuotePair.getBase().getCode() + baseQuotePair.getCounter().getCode() + " Curncy");
  }
}
