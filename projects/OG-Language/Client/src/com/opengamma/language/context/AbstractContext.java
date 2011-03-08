/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.opengamma.util.ArgumentChecker;

/**
 * Base functionality for the global, session and user contexts.
 */
public abstract class AbstractContext {

  // TODO: The AbstractFunctionContext in OG-Engine is virtually identical, but it would be misleading to use
  // that because of its association with functions. Could the common behaviour be abstracted out to something
  // in OG-Util.

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

}
