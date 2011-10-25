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
 * 
 * @param <ParentContext> type of the parent context
 */
public abstract class AbstractContext<ParentContext extends AbstractContext<?>> {

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

  private final ConcurrentMap<String, Object> _values = new ConcurrentHashMap<String, Object>();
  private final ParentContext _parentContext;

  /* package */AbstractContext(final ParentContext parentContext) {
    _parentContext = parentContext;
  }

  protected ParentContext getParentContext() {
    return _parentContext;
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue(final String key) {
    // Don't need to check key for null; the map will do that for us
    return (T) _values.get(key);
  }

  /**
   * Returns a key that a class can use for local data. The key is composed from the class' simple name and the requested key.
   * For example, {@code getClassPrivateKey(Foo.class, "bar")} returns {@code Foo.bar}.
   * 
   * @param clazz class to scope the local data within, not null
   * @param key key name, not null
   * @return the constructed key name, not null
   */
  public static String getClassPrivateKey(final Class<?> clazz, final String key) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(key, "key");
    return clazz.getSimpleName() + "." + key;
  }

  /**
   * Set a value within the context. The value must not already have been set.
   * 
   * @param key key
   * @param value value
   * @throws IllegalStateException if the value is already set
   */
  protected void setValue(final String key, final Object value) {
    ArgumentChecker.notNull(key, "key");
    ArgumentChecker.notNull(value, "value");
    if (_values.putIfAbsent(key, value) != null) {
      throw new IllegalStateException("Value '" + key + "' already in the context");
    }
  }

  /**
   * Remove a value from the context. The value must be set.
   * 
   * @param key key
   * @throws IllegalStateException if the value has not been set
   */
  protected void removeValue(final String key) {
    ArgumentChecker.notNull(key, "key");
    if (_values.remove(key) == null) {
      throw new IllegalStateException("Value '" + key + "' not in the context");
    }
  }

  /**
   * Replaces a value within the context. Unlike {@link #setValue} and {@link #removeValue} this will
   * always succeed.
   * 
   * @param key key
   * @param value value
   */
  protected void replaceValue(final String key, final Object value) {
    ArgumentChecker.notNull(key, "key");
    ArgumentChecker.notNull(value, "value");
    _values.put(key, value);
  }

  protected void removeOrReplaceValue(final String key, final Object value) {
    ArgumentChecker.notNull(key, "key");
    if (value != null) {
      _values.put(key, value);
    } else {
      _values.remove(key);
    }
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

  protected <T> T getCascadedValueImpl(final String key) {
    T value = this.<T>getValue(key);
    if (value == null) {
      if (getParentContext() != null) {
        value = getParentContext().<T>getCascadedValueImpl(key);
      }
    }
    return value;
  }

  protected <T> T getCascadedValue(final String key) {
    T value = this.<T>getValue(key);
    if (value == null) {
      value = this.<T>getCascadedValueImpl(key);
      replaceValue(key, value);
    }
    return value;
  }

}
