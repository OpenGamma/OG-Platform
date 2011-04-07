/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
import com.opengamma.language.livedata.DefaultLiveDataDefinitionFilter;
import com.opengamma.language.livedata.LiveDataDefinitionFilter;
import com.opengamma.language.procedure.AggregatingProcedureProvider;
import com.opengamma.language.procedure.DefaultProcedureDefinitionFilter;
import com.opengamma.language.procedure.ProcedureDefinitionFilter;

/**
 * A global information context shared by all client instances. This corresponds to the
 * OpenGamma installation the language integration framework is connecting to.
 */
public abstract class GlobalContext extends AbstractContext<AbstractContext<?>> {

  /**
   * Name under which the system settings (OpenGamma.properties) are bound. 
   */
  protected static final String SYSTEM_SETTINGS = "systemSettings";

  /**
   * Name under which the function definition filter is bound.
   */
  protected static final String FUNCTION_DEFINITION_FILTER = "functionDefinitionFilter";
  /**
   * Name under which the live data definition filter is bound.
   */
  protected static final String LIVEDATA_DEFINITION_FILTER = "liveDataDefinitionFilter";
  /**
   * Name under which the procedure definition filter is bound.
   */
  protected static final String PROCEDURE_DEFINITION_FILTER = "procedureDefinitionFilter";
  /**
   * Name under which the generic parameter converter is bound.
   */
  protected static final String PARAMETER_CONVERTER = "parameterConverter";
  /**
   * Name under which the generic result converter is bound.
   */
  protected static final String RESULT_CONVERTER = "resultConverter";
  /**
   * Name under which the generic value converter is bound. 
   */
  protected static final String VALUE_CONVERTER = "valueConverter";
  /**
   * Name under which a source of type converters is bound.
   */
  protected static final String TYPE_CONVERTER_PROVIDER = "typeConverterProvider";
  /**
   * Name under which a function specific parameter converter is bound. If none is bound, the generic one will be used.
   */
  protected static final String FUNCTION_PARAMETER_CONVERTER = "functionParameterConverter";
  /**
   * Name under which a function specific result converter is bound. If none is bound, the generic one will be used.
   */
  protected static final String FUNCTION_RESULT_CONVERTER = "functionResultConverter";
  /**
   * Name under which a live data specific parameter converter is bound. If none is bound, the generic one will be used.
   */
  protected static final String LIVEDATA_PARAMETER_CONVERTER = "liveDataParameterConverter";
  /**
   * Name under which a live data specific result converter is bound. If none is bound, the generic one will be used.
   */
  protected static final String LIVEDATA_RESULT_CONVERTER = "liveDataResultConverter";
  /**
   * Name under which a procedure specific parameter converter is bound. If none is bound, the generic one will be used.
   */
  protected static final String PROCEDURE_PARAMETER_CONVERTER = "procedureParameterConverter";
  /**
   * Name under which a procedure specific result converter is bound. If none is bound, the generic one will be used.
   */
  protected static final String PROCEDURE_RESULT_CONVERTER = "procedureResultConverter";

  private final Map<String, UserContext> _userContexts = new HashMap<String, UserContext>();

  /* package */GlobalContext() {
    super(null);
    setValue(FUNCTION_PROVIDER, AggregatingFunctionProvider.cachingInstance());
    setValue(LIVEDATA_PROVIDER, AggregatingLiveDataProvider.cachingInstance());
    setValue(PROCEDURE_PROVIDER, AggregatingProcedureProvider.cachingInstance());
    setValue(FUNCTION_DEFINITION_FILTER, new DefaultFunctionDefinitionFilter());
    setValue(LIVEDATA_DEFINITION_FILTER, new DefaultLiveDataDefinitionFilter());
    setValue(PROCEDURE_DEFINITION_FILTER, new DefaultProcedureDefinitionFilter());
    setValue(TYPE_CONVERTER_PROVIDER, new AggregatingTypeConverterProvider());
    setValue(VALUE_CONVERTER, new DefaultValueConverter());
  }

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
   * @return an existing context, or {@code null} if none is available
   */
  protected synchronized UserContext getUserContext(final String userName) {
    return _userContexts.get(userName);
  }

  /**
   * Returns {@code true} iff the service is running from a debug build. This is dependent
   * only on the service runner and should probably control infrastructure behavior,
   * logging or diagnostics. The session context will indicate whether the code used by
   * the bound language is a debug build which could control the operation available or
   * additional debugging/diagnostic metadata apply to the results.
   * 
   * @return {@code true} if the service runner is a debug build, {@code false} otherwise
   */
  public static boolean isDebug() {
    return System.getProperty("system.debug") != null;
  }

  public Properties getSystemSettings() {
    return getValue(SYSTEM_SETTINGS);
  }

  public FunctionDefinitionFilter getFunctionDefinitionFilter() {
    return getValue(FUNCTION_DEFINITION_FILTER);
  }

  public LiveDataDefinitionFilter getLiveDataDefinitionFilter() {
    return getValue(LIVEDATA_DEFINITION_FILTER);
  }

  public ProcedureDefinitionFilter getProcedureDefinitionFilter() {
    return getValue(PROCEDURE_DEFINITION_FILTER);
  }

  public ParameterConverter getParameterConverter() {
    ParameterConverter v = getValue(PARAMETER_CONVERTER);
    if (v == null) {
      v = new DefaultParameterConverter();
      replaceValue(PARAMETER_CONVERTER, v);
    }
    return v;
  }

  protected ParameterConverter getParameterConverter(final String key) {
    ParameterConverter v = getValue(key);
    if (v == null) {
      v = getParameterConverter();
      replaceValue(key, v);
    }
    return v;
  }

  public ParameterConverter getFunctionParameterConverter() {
    return getParameterConverter(FUNCTION_PARAMETER_CONVERTER);
  }

  public ParameterConverter getLiveDataParameterConverter() {
    return getParameterConverter(LIVEDATA_PARAMETER_CONVERTER);
  }

  public ParameterConverter getProcedureParameterConverter() {
    return getParameterConverter(PROCEDURE_PARAMETER_CONVERTER);
  }

  public ResultConverter getResultConverter() {
    ResultConverter v = getValue(RESULT_CONVERTER);
    if (v == null) {
      v = new DefaultResultConverter();
      replaceValue(RESULT_CONVERTER, v);
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

  public ResultConverter getFunctionResultConverter() {
    return getResultConverter(FUNCTION_RESULT_CONVERTER);
  }

  public ResultConverter getLiveDataResultConverter() {
    return getResultConverter(LIVEDATA_RESULT_CONVERTER);
  }

  public ResultConverter getProcedureResultConverter() {
    return getResultConverter(PROCEDURE_RESULT_CONVERTER);
  }

  public ValueConverter getValueConverter() {
    return getValue(VALUE_CONVERTER);
  }

  protected AggregatingTypeConverterProvider getTypeConverterProviderImpl() {
    return getValue(TYPE_CONVERTER_PROVIDER);
  }

  public TypeConverterProvider getTypeConverterProvider() {
    return getTypeConverterProviderImpl();
  }

}
