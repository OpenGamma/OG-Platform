/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

/**
 * @param <T> Type of object formatted by the formatter
 */
public abstract class AbstractFormatter<T> implements Formatter<T> {

  @Override
  public FormatType getFormatForValue(T value) {
    return getFormatForType();
  }
}
