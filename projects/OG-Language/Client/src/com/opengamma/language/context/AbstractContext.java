/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.opengamma.language.function.AggregatingFunctionProvider;
import com.opengamma.language.function.FunctionProvider;
import com.opengamma.language.livedata.AggregatingLiveDataProvider;
import com.opengamma.language.livedata.LiveDataProvider;
import com.opengamma.language.procedure.AggregatingProcedureProvider;
import com.opengamma.language.procedure.ProcedureProvider;
import com.opengamma.util.ArgumentChecker;

/**
 * Base functionality for the global, session and user contexts.
 */
public abstract class AbstractContext {

  /**
   * 
   */
  protected static final String FUNCTION_PROVIDER = "functionProvider";
  /**
   * 
   */
  protected static final String LIVEDATA_PROVIDER = "liveDataProvider";
  /**
   * 
   */
  protected static final String PROCEDURE_PROVIDER = "procedureProvider";

  // TODO: The AbstractFunctionContext in OG-Engine is virtually identical, but it would be misleading to use
  // that because of its association with functions. Could the common behaviors be abstracted out to something
  // in OG-Util, and this class just have the bits common to global, user and session contexts.

  private final ConcurrentMap<String, Object> _values = new ConcurrentHashMap<String, Object>();

  /* package */AbstractContext() {
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue(final String key) {
    ArgumentChecker.notNull(key, "key");
    return (T) _values.get(key);
  }

  protected void setValue(final String key, final Object value) {
    ArgumentChecker.notNull(key, "key");
    ArgumentChecker.notNull(value, "value");
    if (_values.putIfAbsent(key, value) != null) {
      throw new IllegalStateException("Value '" + key + "' already in the context");
    }
  }

  protected void removeValue(final String key) {
    ArgumentChecker.notNull(key, "key");
    if (_values.remove(key) == null) {
      throw new IllegalStateException("Value '" + key + "' not in the context");
    }
  }

  protected void replaceValue(final String key, final Object value) {
    removeValue(key);
    setValue(key, value);
  }

  protected AggregatingFunctionProvider getFunctionProviderImpl() {
    return getValue(FUNCTION_PROVIDER);
  }

  public FunctionProvider getFunctionProvider() {
    return getFunctionProviderImpl();
  }

  protected AggregatingLiveDataProvider getLiveDataProviderImpl() {
    return getValue(LIVEDATA_PROVIDER);
  }

  public LiveDataProvider getLiveDataProvider() {
    return getLiveDataProviderImpl();
  }

  protected AggregatingProcedureProvider getProcedureProviderImpl() {
    return getValue(PROCEDURE_PROVIDER);
  }

  public ProcedureProvider getProcedureProvider() {
    return getProcedureProviderImpl();
  }

}
