/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

/**
 * An implementation of {@link FunctionParameters} that is a trivial map of strings to values.
 * The values must be Fudge serializable. The Fudge encoding of simple parameters is a message
 * with one field per parameter, with the field name equal to the parameter name and the value
 * equal to the value. There is thus a limit of 255 characters on a parameter name.
 */
public class SimpleFunctionParameters implements FunctionParameters {

  private final ConcurrentMap<String, Object> _parameters;

  public SimpleFunctionParameters(final Map<String, ?> parameters) {
    _parameters = new ConcurrentHashMap<String, Object>(parameters);
  }

  public SimpleFunctionParameters() {
    _parameters = new ConcurrentHashMap<String, Object>();
  }

  public <T> void setValue(final String parameter, final T value) {
    if (parameter.length() > 255) {
      // 255 char limit from the Fudge encoding
      throw new IllegalArgumentException("parameter too long");
    }
    _parameters.put(parameter, value);
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue(final String parameter) {
    return (T) _parameters.get(parameter);
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof SimpleFunctionParameters)) {
      return false;
    }
    return _parameters.equals(((SimpleFunctionParameters) o)._parameters);
  }

  @Override
  public int hashCode() {
    return _parameters.hashCode();
  }

  public void toFudgeMsg(final FudgeSerializer serializer, final MutableFudgeMsg message) {
    for (Map.Entry<String, Object> parameter : _parameters.entrySet()) {
      serializer.addToMessageWithClassHeaders(message, parameter.getKey(), null, parameter.getValue());
    }
  }

  public static SimpleFunctionParameters fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final SimpleFunctionParameters parameters = new SimpleFunctionParameters();
    for (FudgeField field : message) {
      if (field.getName() != null) {
        parameters.setValue(field.getName(), deserializer.fieldValueToObject(field));
      }
    }
    return parameters;
  }

}
