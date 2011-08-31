/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import com.opengamma.language.context.GlobalContext;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.context.UserContext;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.util.ArgumentChecker;

/**
 * Additional data for converting a value.
 */
public final class ValueConversionContext {

  private final SessionContext _sessionContext;
  private final com.opengamma.language.invoke.ValueConverter _converter;
  private boolean _hasResult;
  private boolean _hasFailed;
  private Object _result;
  private int _reentrance;

  public ValueConversionContext(final SessionContext sessionContext, final com.opengamma.language.invoke.ValueConverter converter) {
    ArgumentChecker.notNull(sessionContext, "sessionContext");
    ArgumentChecker.notNull(converter, "converter");
    _sessionContext = sessionContext;
    _converter = converter;
  }

  public SessionContext getSessionContext() {
    return _sessionContext;
  }

  public UserContext getUserContext() {
    return getSessionContext().getUserContext();
  }

  public GlobalContext getGlobalContext() {
    return getSessionContext().getGlobalContext();
  }

  public int getReentranceCount() {
    return _reentrance;
  }

  public void convertValue(final Object value, final JavaTypeInfo<?> type) {
    _reentrance++;
    _converter.convertValue(this, value, type);
    _reentrance--;
  }

  public boolean setFail() {
    if (_hasResult) {
      throw new IllegalStateException("Result (" + _result + ") already set");
    }
    if (_hasFailed) {
      throw new IllegalStateException("Already failed");
    }
    _hasFailed = true;
    return true;
  }

  public boolean isFailed() {
    if (_hasFailed) {
      _hasFailed = false;
      return true;
    } else {
      return false;
    }
  }

  public boolean setResult(final Object result) {
    if (_hasResult) {
      throw new IllegalStateException("Result (" + _result + ") already set");
    }
    if (_hasFailed) {
      throw new IllegalStateException("Already failed");
    }
    _hasResult = true;
    _result = result;
    return true;
  }

  @SuppressWarnings("unchecked")
  public <T> T getResult() {
    if (_hasResult) {
      _hasResult = false;
      return (T) _result;
    } else {
      if (_hasFailed) {
        throw new IllegalStateException("Conversion failed");
      } else {
        throw new IllegalStateException("Neither result nor failure set");
      }
    }
  }

}
