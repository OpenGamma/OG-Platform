/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.helper.AvailableOutputsProvider;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.financial.currency.CurrencyPairsSource;
import com.opengamma.financial.user.rest.RemoteClient;
import com.opengamma.language.function.AggregatingFunctionProvider;
import com.opengamma.language.function.DefaultFunctionDefinitionFilter;
import com.opengamma.language.function.FunctionDefinitionFilter;
import com.opengamma.language.invoke.AggregatingTypeConverterProvider;
import com.opengamma.language.invoke.DefaultParameterConverter;
import com.opengamma.language.invoke.DefaultResultConverter;
import com.opengamma.language.invoke.DefaultValueConverter;
import com.opengamma.language.invoke.ParameterConverter;
import com.opengamma.language.invoke.ResultConverter;
import com.opengamma.language.invoke.TypeConverterProvider;
import com.opengamma.language.invoke.ValueConverter;
import com.opengamma.language.livedata.AggregatingLiveDataProvider;
import com.opengamma.language.livedata.BlockingLiveDataDispatcher;
import com.opengamma.language.livedata.DefaultLiveDataDefinitionFilter;
import com.opengamma.language.livedata.LiveDataDefinitionFilter;
import com.opengamma.language.livedata.LiveDataDispatcher;
import com.opengamma.language.procedure.AggregatingProcedureProvider;
import com.opengamma.language.procedure.DefaultProcedureDefinitionFilter;
import com.opengamma.language.procedure.ProcedureDefinitionFilter;

/**
 * A global information context shared by all client instances. This corresponds to the
 * OpenGamma installation the language integration framework is connecting to.
 */
public abstract class GlobalContext extends AbstractContext<AbstractContext<?>> {

  /**
   * Name under which the available outputs provider is bound.
   */
  protected static final String AVAILABLE_OUTPUTS_PROVIDER = "availableOutputsProvider";

  /**
   * Name under which the shared engine client is bound.
   */
  protected static final String CLIENT = "client";

  /**
   * Name under which the default computation target resolver is bound.
   */
  protected static final String COMPUTATION_TARGET_RESOLVER = "computationTargetResolver";

  /**
   * Name under which the currency pairs source is bound.
   */
  protected static final String CURRENCY_PAIRS_SOURCE = "currencyPairsSource";

  /**
   * Name under which the exchange source is bound.
   */
  protected static final String EXCHANGE_SOURCE = "exchangeSource";

  /**
   * Name under which the function definition filter is bound.
   */
  protected static final String FUNCTION_DEFINITION_FILTER = "functionDefinitionFilter";

  /**
   * Name under which a function specific parameter converter is bound. If none is bound, the generic one will be used.
   */
  protected static final String FUNCTION_PARAMETER_CONVERTER = "functionParameterConverter";

  /**
   * Name under which a function specific result converter is bound. If none is bound, the generic one will be used.
   */
  protected static final String FUNCTION_RESULT_CONVERTER = "functionResultConverter";

  /**
   * Name under which a historical time series source is bound.
   */
  protected static final String HISTORICAL_TIME_SERIES_SOURCE = "historicalTimeSeriesSource";

  /**
   * Name under which a holiday source is bound.
   */
  protected static final String HOLIDAY_SOURCE = "holidaySource";

  /**
   * Name under which the live data definition filter is bound.
   */
  protected static final String LIVEDATA_DEFINITION_FILTER = "liveDataDefinitionFilter";

  /**
   * Name under which a live data specific parameter converter is bound. If none is bound, the generic one will be used.
   */
  protected static final String LIVEDATA_PARAMETER_CONVERTER = "liveDataParameterConverter";

  /**
   * Name under which a live data specific result converter is bound. If none is bound, the generic one will be used.
   */
  protected static final String LIVEDATA_RESULT_CONVERTER = "liveDataResultConverter";

  /**
   * Name under which a live data dispatcher is bound.
   */
  protected static final String LIVEDATA_DISPATCHER = "liveDataDispatcher";

  /**
   * Name under which a market data snapshot source is bound.
   */
  protected static final String MARKET_DATA_SNAPSHOT_SOURCE = "marketDataSnapshotSource";

  /**
   * Name under which the procedure definition filter is bound.
   */
  protected static final String PROCEDURE_DEFINITION_FILTER = "procedureDefinitionFilter";

  /**
   * Name under which a procedure specific parameter converter is bound. If none is bound, the generic one will be used.
   */
  protected static final String PROCEDURE_PARAMETER_CONVERTER = "procedureParameterConverter";

  /**
   * Name under which a procedure specific result converter is bound. If none is bound, the generic one will be used.
   */
  protected static final String PROCEDURE_RESULT_CONVERTER = "procedureResultConverter";

  /**
   * Name under which the generic parameter converter is bound.
   */
  protected static final String PARAMETER_CONVERTER = "parameterConverter";

  /**
   * Name under which the position source is bound.
   */
  protected static final String POSITION_SOURCE = "positionSource";
  
  /**
   * Name under which the region source is bound.
   */
  protected static final String REGION_SOURCE = "regionSource";

  /**
   * Name under which the generic result converter is bound.
   */
  protected static final String RESULT_CONVERTER = "resultConverter";

  /**
   * Name under which the {@link ExecutorService} for saturating the processor(s) is bound.
   */
  protected static final String SATURATING_EXECUTOR = "saturatingExecutor";

  /**
   * Name under which the security source is bound.
   */
  protected static final String SECURITY_SOURCE = "securitySource";

  /**
   * Name under which a source of type converters is bound.
   */
  protected static final String TYPE_CONVERTER_PROVIDER = "typeConverterProvider";

  /**
   * Name under which the generic value converter is bound. 
   */
  protected static final String VALUE_CONVERTER = "valueConverter";

  /**
   * Name under which the view processor is bound.
   */
  protected static final String VIEW_PROCESSOR = "viewProcessor";
  
  /**
   * Name under which the volatility cube definition source is bound.
   */
  protected static final String VOLATILITY_CUBE_DEFINITION_SOURCE = "volatilityCubeDefinitionSource";


  private final Map<String, UserContext> _userContexts = new HashMap<String, UserContext>();

  /* package */GlobalContext() {
    super(null);
    setValue(FUNCTION_DEFINITION_FILTER, new DefaultFunctionDefinitionFilter());
    setValue(FUNCTION_PROVIDER, AggregatingFunctionProvider.cachingInstance());
    setValue(LIVEDATA_DEFINITION_FILTER, new DefaultLiveDataDefinitionFilter());
    setValue(LIVEDATA_DISPATCHER, new BlockingLiveDataDispatcher());
    setValue(LIVEDATA_PROVIDER, AggregatingLiveDataProvider.cachingInstance());
    setValue(PROCEDURE_DEFINITION_FILTER, new DefaultProcedureDefinitionFilter());
    setValue(PROCEDURE_PROVIDER, AggregatingProcedureProvider.cachingInstance());
    setValue(TYPE_CONVERTER_PROVIDER, new AggregatingTypeConverterProvider());
    setValue(VALUE_CONVERTER, new DefaultValueConverter());
  }

  // System calls

  /**
   * Adds a user context. To combine the user context operations into an atomic operation,
   * synchronize on the global context object (e.g. for get followed by add).
   * 
   * @param userContext user context to add
   * @throws IllegalStateException if an active context already exists for the user
   */
  protected synchronized void addUserContext(final UserContext userContext) {
    if (_userContexts.get(userContext.getUserName()) != null) {
      throw new IllegalStateException("User context for '" + userContext.getUserName() + "' already exists");
    }
    _userContexts.put(userContext.getUserName(), userContext);
  }

  /**
   * Removes a user context. To combine the user context operations into an atomic operation,
   * synchronize on the global context object (e.g. for get followed by add).
   * 
   * @param userContext user context to remove
   * @throws IllegalStateException if an active context does not exist for the user
   */
  protected synchronized void removeUserContext(final UserContext userContext) {
    if (_userContexts.remove(userContext.getUserName()) == null) {
      throw new IllegalStateException("User context for '" + userContext.getUserName() + "' doesn't exist");
    }
  }

  /**
   * Returns an existing user context. To combine the user context operations into an atomic
   * operation, synchronize on the global context object (e.g. for get followed by add).
   * 
   * @param userName name of the user to search for
   * @return an existing context, null if none is available
   */
  protected synchronized UserContext getUserContext(final String userName) {
    return _userContexts.get(userName);
  }

  // Core members

  /**
   * Returns true iff the service is running from a debug build.
   * This is dependent only on the service runner and should probably control infrastructure
   * behavior, logging or diagnostics. The session context will indicate whether the code used
   * by the bound language is a debug build which could control the operation available or
   * additional debugging/diagnostic metadata apply to the results.
   * 
   * @return true if the service runner is a debug build
   */
  public static boolean isDebug() {
    return System.getProperty("service.debug") != null;
  }

  // Helper members

  protected ParameterConverter getParameterConverter(final String key) {
    ParameterConverter v = getValue(key);
    if (v == null) {
      v = getParameterConverter();
      replaceValue(key, v);
    }
    return v;
  }

  protected ResultConverter getResultConverter(final String key) {
    ResultConverter v = getValue(key);
    if (v == null) {
      v = getResultConverter();
      replaceValue(key, v);
    }
    return v;
  }

  protected AggregatingTypeConverterProvider getTypeConverterProviderImpl() {
    return getValue(TYPE_CONVERTER_PROVIDER);
  }

  // Standard context members

  public AvailableOutputsProvider getAvailableOutputsProvider() {
    return getValue(AVAILABLE_OUTPUTS_PROVIDER);
  }

  public RemoteClient getClient() {
    return getValue(CLIENT);
  }
  
  public ComputationTargetResolver getComputationTargetResolver() {
    return getValue(COMPUTATION_TARGET_RESOLVER);
  }

  public CurrencyPairsSource getCurrencyPairsSource() {
    return getValue(CURRENCY_PAIRS_SOURCE);
  }

  public ExchangeSource getExchangeSource() {
    return getValue(EXCHANGE_SOURCE);
  }

  public FunctionDefinitionFilter getFunctionDefinitionFilter() {
    return getValue(FUNCTION_DEFINITION_FILTER);
  }

  public ParameterConverter getFunctionParameterConverter() {
    return getParameterConverter(FUNCTION_PARAMETER_CONVERTER);
  }

  public ResultConverter getFunctionResultConverter() {
    return getResultConverter(FUNCTION_RESULT_CONVERTER);
  }

  public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource() {
    return getValue(HISTORICAL_TIME_SERIES_SOURCE);
  }

  public HolidaySource getHolidaySource() {
    return getValue(HOLIDAY_SOURCE);
  }

  public LiveDataDefinitionFilter getLiveDataDefinitionFilter() {
    return getValue(LIVEDATA_DEFINITION_FILTER);
  }

  public LiveDataDispatcher getLiveDataDispatcher() {
    return getValue(LIVEDATA_DISPATCHER);
  }

  public ParameterConverter getLiveDataParameterConverter() {
    return getParameterConverter(LIVEDATA_PARAMETER_CONVERTER);
  }

  public ResultConverter getLiveDataResultConverter() {
    return getResultConverter(LIVEDATA_RESULT_CONVERTER);
  }

  public MarketDataSnapshotSource getMarketDataSnapshotSource() {
    return getValue(MARKET_DATA_SNAPSHOT_SOURCE);
  }

  public ParameterConverter getParameterConverter() {
    ParameterConverter v = getValue(PARAMETER_CONVERTER);
    if (v == null) {
      v = new DefaultParameterConverter();
      replaceValue(PARAMETER_CONVERTER, v);
    }
    return v;
  }

  public PositionSource getPositionSource() {
    return getValue(POSITION_SOURCE);
  }
  
  public ProcedureDefinitionFilter getProcedureDefinitionFilter() {
    return getValue(PROCEDURE_DEFINITION_FILTER);
  }

  public ParameterConverter getProcedureParameterConverter() {
    return getParameterConverter(PROCEDURE_PARAMETER_CONVERTER);
  }

  public ResultConverter getProcedureResultConverter() {
    return getResultConverter(PROCEDURE_RESULT_CONVERTER);
  }

  public RegionSource getRegionSource() {
    return getValue(REGION_SOURCE);
  }

  public ResultConverter getResultConverter() {
    ResultConverter v = getValue(RESULT_CONVERTER);
    if (v == null) {
      v = new DefaultResultConverter();
      replaceValue(RESULT_CONVERTER, v);
    }
    return v;
  }

  public ExecutorService getSaturatingExecutor() {
    return getValue(SATURATING_EXECUTOR);
  }

  public SecuritySource getSecuritySource() {
    return getValue(SECURITY_SOURCE);
  }

  public TypeConverterProvider getTypeConverterProvider() {
    return getTypeConverterProviderImpl();
  }

  public ValueConverter getValueConverter() {
    return getValue(VALUE_CONVERTER);
  }

  public ViewProcessor getViewProcessor() {
    return getValue(VIEW_PROCESSOR);
  }
  
  public VolatilityCubeDefinitionSource getVolatilityCubeDefinitionSource() {
    return getValue(VOLATILITY_CUBE_DEFINITION_SOURCE);
  }

}
