/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

/**
 * Abstract {@link Formatter} that implements {@link #getFormatForValue} by delegating to {@link #getFormatForType()}.
 * @param <T> Type of object formatted by the formatter
 */
public abstract class AbstractFormatter<T> implements Formatter<T> {

  /**
   * Returns the same format type as {@link #getFormatForType()}.
   * @param value The value
   * @return The format type returned by {@link #getFormatForType()}
   */
  @Override
  public FormatType getFormatForValue(T value) {
    return getFormatForType();
  }
}
