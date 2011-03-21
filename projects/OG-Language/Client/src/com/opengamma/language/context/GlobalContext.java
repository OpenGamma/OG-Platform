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
public abstract class GlobalContext extends AbstractContext {

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

  private final Map<String, UserContext> _userContexts = new HashMap<String, UserContext>();

  /* package */GlobalContext() {
    setValue(FUNCTION_PROVIDER, AggregatingFunctionProvider.cachingInstance());
    setValue(LIVEDATA_PROVIDER, AggregatingLiveDataProvider.cachingInstance());
    setValue(PROCEDURE_PROVIDER, AggregatingProcedureProvider.cachingInstance());
    setValue(FUNCTION_DEFINITION_FILTER, new DefaultFunctionDefinitionFilter());
    setValue(LIVEDATA_DEFINITION_FILTER, new DefaultLiveDataDefinitionFilter());
    setValue(PROCEDURE_DEFINITION_FILTER, new DefaultProcedureDefinitionFilter());
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

}
