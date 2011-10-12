/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalId;
import com.opengamma.language.Data;
import com.opengamma.language.Value;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Creates a {@link MarketDataOverride} configuration item.
 */
public class MarketDataOverrideFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final MarketDataOverrideFunction INSTANCE = new MarketDataOverrideFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    return Arrays.asList(
        new MetaParameter("value", JavaTypeInfo.builder(Data.class).allowNull().get()),
        new MetaParameter("valueRequirement", JavaTypeInfo.builder(ValueRequirement.class).allowNull().get()),
        new MetaParameter("valueName", JavaTypeInfo.builder(String.class).allowNull().get()),
        new MetaParameter("identifier", JavaTypeInfo.builder(ExternalId.class).allowNull().get()));
  }

  private MarketDataOverrideFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction("MarketDataOverride", getParameters(), this));
  }

  protected MarketDataOverrideFunction() {
    this(new DefinitionAnnotater(MarketDataOverrideFunction.class));
  }

  // TODO: this functionality should not be here; a "best guess" Java object should be a service from the global context
  // that can be extended with language binding rules if necessary.

  private static Object convertSingle(final Value value) {
    if (value.getErrorValue() != null) {
      return new OpenGammaRuntimeException("Error " + value.getErrorValue());
    } else if (value.getBoolValue() != null) {
      return value.getBoolValue();
    } else if (value.getDoubleValue() != null) {
      return value.getDoubleValue();
    } else if (value.getIntValue() != null) {
      return value.getIntValue();
    } else if (value.getStringValue() != null) {
      return value.getStringValue();
    } else if (value.getMessageValue() != null) {
      // TODO: a best guess Fudge deserialization should be plugged in here
      return value.getMessageValue();
    } else {
      return null;
    }
  }

  private static Collection<Object> convertLinear(final Value[] value) {
    final Collection<Object> result = new ArrayList<Object>(value.length);
    for (Value v : value) {
      result.add(convertSingle(v));
    }
    return result;
  }

  private static Collection<Collection<Object>> convertMatrix(final Value[][] value) {
    final Collection<Collection<Object>> result = new ArrayList<Collection<Object>>(value.length);
    for (Value[] v : value) {
      result.add(convertLinear(v));
    }
    return result;
  }

  private static Object convertData(final Data data) {
    if (data.getSingle() != null) {
      return convertSingle(data.getSingle());
    } else if (data.getLinear() != null) {
      return convertLinear(data.getLinear());
    } else if (data.getMatrix() != null) {
      return convertMatrix(data.getMatrix());
    } else {
      return null;
    }
  }

  // end of code which shouldn't be here

  public static MarketDataOverride invoke(final Object value, final ValueRequirement valueRequirement) {
    return new MarketDataOverride(valueRequirement, null, null, value);
  }

  public static MarketDataOverride invoke(final Object value, final String valueName, final ExternalId identifier) {
    return new MarketDataOverride(null, valueName, identifier, value);
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    final Data value = (Data) parameters[0];
    if (parameters[1] == null) {
      if (parameters[2] == null) {
        throw new InvokeInvalidArgumentException(2, "valueName cannot be omitted when valueRequirement is not specified");
      }
      if (parameters[3] == null) {
        throw new InvokeInvalidArgumentException(3, "identifier cannot be omitted when valueRequirement is not specified");
      }
      final String valueName = (String) parameters[2];
      final ExternalId identifier = (ExternalId) parameters[3];
      return invoke(convertData(value), valueName, identifier);
    } else {
      if (parameters[2] != null) {
        throw new InvokeInvalidArgumentException(2, "valueName must be omitted when valueRequirement is specified");
      }
      if (parameters[3] != null) {
        throw new InvokeInvalidArgumentException(3, "identifier must be omitted when valueRequirement is specified");
      }
      final ValueRequirement valueRequirement = (ValueRequirement) parameters[1];
      return invoke(convertData(value), valueRequirement);
    }
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
