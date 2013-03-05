/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import java.util.concurrent.ExecutorService;

import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.helper.AvailableOutputsProvider;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.financial.currency.CurrencyPairsSource;
import com.opengamma.financial.user.rest.RemoteClient;
import com.opengamma.language.function.AggregatingFunctionProvider;
import com.opengamma.language.function.FunctionDefinitionFilter;
import com.opengamma.language.invoke.AggregatingTypeConverterProvider;
import com.opengamma.language.invoke.ParameterConverter;
import com.opengamma.language.invoke.ResultConverter;
import com.opengamma.language.invoke.ValueConverter;
import com.opengamma.language.livedata.AggregatingLiveDataProvider;
import com.opengamma.language.livedata.LiveDataDefinitionFilter;
import com.opengamma.language.livedata.LiveDataDispatcher;
import com.opengamma.language.procedure.AggregatingProcedureProvider;
import com.opengamma.language.procedure.ProcedureDefinitionFilter;
import com.opengamma.util.ArgumentChecker;

/**
 * A mutable version of {@link GlobalContext}.
 */
public class MutableGlobalContext extends GlobalContext {

  /* package */MutableGlobalContext() {
  }

  // Definition providers

  @Override
  public AggregatingFunctionProvider getFunctionProvider() {
    return getFunctionProviderImpl();
  }

  @Override
  public AggregatingLiveDataProvider getLiveDataProvider() {
    return getLiveDataProviderImpl();
  }

  @Override
  public AggregatingProcedureProvider getProcedureProvider() {
    return getProcedureProviderImpl();
  }

  // Standard context members

  public void setAvailableOutputsProvider(final AvailableOutputsProvider availableOutputsProvider) {
    removeOrReplaceValue(AVAILABLE_OUTPUTS_PROVIDER, availableOutputsProvider);
  }

  public void setClient(final RemoteClient client) {
    removeOrReplaceValue(CLIENT, client);
  }
  
  public void setComputationTargetResolver(final DefaultComputationTargetResolver computationTargetResolver) {
    removeOrReplaceValue(COMPUTATION_TARGET_RESOLVER, computationTargetResolver);
  }

  public void setCurrencyPairsSource(final CurrencyPairsSource currencyPairsSource) {
    removeOrReplaceValue(CURRENCY_PAIRS_SOURCE, currencyPairsSource);
  }

  public void setExchangeSource(final ExchangeSource exchangeSource) {
    removeOrReplaceValue(EXCHANGE_SOURCE, exchangeSource);
  }

  public void setFunctionDefinitionFilter(final FunctionDefinitionFilter functionDefinitionFilter) {
    ArgumentChecker.notNull(functionDefinitionFilter, "functionDefinitionFilter");
    replaceValue(FUNCTION_DEFINITION_FILTER, functionDefinitionFilter);
  }

  public void setFunctionParameterConverter(final ParameterConverter parameterConverter) {
    removeOrReplaceValue(FUNCTION_PARAMETER_CONVERTER, parameterConverter);
  }

  public void setFunctionResultConverter(final ResultConverter resultConverter) {
    removeOrReplaceValue(FUNCTION_RESULT_CONVERTER, resultConverter);
  }

  public void setHistoricalTimeSeriesSource(final HistoricalTimeSeriesSource historicalTimeSeriesSource) {
    removeOrReplaceValue(HISTORICAL_TIME_SERIES_SOURCE, historicalTimeSeriesSource);
  }

  public void setHolidaySource(final HolidaySource holidaySource) {
    removeOrReplaceValue(HOLIDAY_SOURCE, holidaySource);
  }

  public void setLiveDataDefinitionFilter(final LiveDataDefinitionFilter liveDataDefinitionFilter) {
    ArgumentChecker.notNull(liveDataDefinitionFilter, "liveDataDefinitionFilter");
    replaceValue(LIVEDATA_DEFINITION_FILTER, liveDataDefinitionFilter);
  }

  public void setLiveDataDispatcher(final LiveDataDispatcher liveDataDispatcher) {
    ArgumentChecker.notNull(liveDataDispatcher, "liveDataDispatcher");
    replaceValue(LIVEDATA_DISPATCHER, liveDataDispatcher);
  }

  public void setLiveDataParameterConverter(final ParameterConverter parameterConverter) {
    removeOrReplaceValue(LIVEDATA_PARAMETER_CONVERTER, parameterConverter);
  }

  public void setLiveDataResultConverter(final ResultConverter resultConverter) {
    removeOrReplaceValue(LIVEDATA_RESULT_CONVERTER, resultConverter);
  }

  public void setMarketDataSnapshotSource(final MarketDataSnapshotSource marketDataSnapshotSource) {
    removeOrReplaceValue(MARKET_DATA_SNAPSHOT_SOURCE, marketDataSnapshotSource);
  }

  public void setParameterConverter(final ParameterConverter parameterConverter) {
    removeOrReplaceValue(PARAMETER_CONVERTER, parameterConverter);
  }

  public void setPositionSource(final PositionSource positionSource) {
    removeOrReplaceValue(POSITION_SOURCE, positionSource);
  }

  public void setProcedureDefinitionFilter(final ProcedureDefinitionFilter procedureDefinitionFilter) {
    ArgumentChecker.notNull(procedureDefinitionFilter, "procedureDefinitionFilter");
    replaceValue(PROCEDURE_DEFINITION_FILTER, procedureDefinitionFilter);
  }

  public void setProcedureParameterConverter(final ParameterConverter parameterConverter) {
    removeOrReplaceValue(PROCEDURE_PARAMETER_CONVERTER, parameterConverter);
  }

  public void setProcedureResultConverter(final ResultConverter resultConverter) {
    removeOrReplaceValue(PROCEDURE_RESULT_CONVERTER, resultConverter);
  }

  public void setRegionSource(final RegionSource regionSource) {
    removeOrReplaceValue(REGION_SOURCE, regionSource);
  }

  public void setResultConverter(final ResultConverter resultConverter) {
    removeOrReplaceValue(RESULT_CONVERTER, resultConverter);
  }

  public void setSaturatingExecutor(final ExecutorService executorService) {
    setValue(SATURATING_EXECUTOR, executorService);
  }

  public void setSecuritySource(final SecuritySource securitySource) {
    removeOrReplaceValue(SECURITY_SOURCE, securitySource);
  }
  
  @Override
  public AggregatingTypeConverterProvider getTypeConverterProvider() {
    return getTypeConverterProviderImpl();
  }

  public void setValueConverter(final ValueConverter valueConverter) {
    ArgumentChecker.notNull(valueConverter, "valueConverter");
    replaceValue(VALUE_CONVERTER, valueConverter);
  }

  public void setViewProcessor(final ViewProcessor viewProcessor) {
    removeOrReplaceValue(VIEW_PROCESSOR, viewProcessor);
  }
  
  public void setVolatilityCubeDefinitionSource(final VolatilityCubeDefinitionSource volatilityCubeDefinitionSource) {
    removeOrReplaceValue(VOLATILITY_CUBE_DEFINITION_SOURCE, volatilityCubeDefinitionSource);
  }

  // Arbitrary values

  @Override
  public void setValue(final String key, final Object value) {
    super.setValue(key, value);
  }

}
