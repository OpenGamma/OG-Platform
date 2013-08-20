/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract {@link TypeFormatter} that implements {@link #getDataTypeForValue} by delegating to {@link #getDataType()}.
 * @param <T> Type of object formatted by the formatter
 */
/* package */ abstract class AbstractFormatter<T> implements TypeFormatter<T> {

  private final Class<T> _type;
  private final Map<Format, Formatter<T>> _formatters = Maps.newHashMap();
  
  protected AbstractFormatter(Class<T> type) {
    ArgumentChecker.notNull(type, "type");
    _type = type;
  }

  protected void addFormatter(Formatter<T> formatter) {
    _formatters.put(formatter.getFormat(), formatter);
  }

  @Override
  public Object format(T value, ValueSpecification valueSpec, Format format, Object inlineKey) {
    if (format == Format.CELL) {
      return formatCell(value, valueSpec, inlineKey);
    }
    Formatter<T> formatter = _formatters.get(format);
    if (formatter != null) {
      return formatter.format(value, valueSpec, inlineKey);
    } 
    return new MissingValueFormatter(format + " format not supported for " + value.getClass().getSimpleName());    
  }

  @Override
  public Class<T> getType() {
    return _type;
  }

  /**
   * Returns the same format type as {@link #getDataType()}.
   * 
   * @param value The value
   * @return The format type returned by {@link #getDataType()}
   */
  @Override
  public DataType getDataTypeForValue(T value) {
    return getDataType();
  }

  //-------------------------------------------------------------------------
  /**
   * A formatter element.
   * @param <T>  the formatter type
   */
  abstract static class Formatter<T> {
    private final Format _format;

    Formatter(Format format) {
      _format = format;
    }

    Format getFormat() {
      return _format;
    }

    abstract Object format(T value, ValueSpecification valueSpec, Object inlineKey);
  }

}
