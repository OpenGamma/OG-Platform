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
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.language.Data;
import com.opengamma.language.Value;
import com.opengamma.language.config.MarketDataOverride.Operation;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
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

  private static final JavaTypeInfo<ExternalId> EXTERNAL_ID = JavaTypeInfo.builder(ExternalId.class).get();
  private static final JavaTypeInfo<UniqueId> UNIQUE_ID = JavaTypeInfo.builder(UniqueId.class).get();

  private static final int VALUE = 0;
  private static final int VALUE_REQUIREMENT = 1;
  private static final int VALUE_NAME = 2;
  private static final int IDENTIFIER = 3;
  private static final int TYPE = 4;
  private static final int OPERATION = 5;

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    return Arrays.asList(
        new MetaParameter("value", JavaTypeInfo.builder(Data.class).allowNull().get()),
        new MetaParameter("valueRequirement", JavaTypeInfo.builder(ValueRequirement.class).allowNull().get()),
        new MetaParameter("valueName", JavaTypeInfo.builder(String.class).allowNull().get()),
        new MetaParameter("identifier", JavaTypeInfo.builder(Data.class).allowNull().get()),
        new MetaParameter("type", JavaTypeInfo.builder(ComputationTargetType.class).allowNull().get()),
        new MetaParameter("operation", JavaTypeInfo.builder(Operation.class).allowNull().get()));
  }

  private MarketDataOverrideFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.VIEW, "MarketDataOverride", getParameters(), this));
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
    for (final Value v : value) {
      result.add(convertSingle(v));
    }
    return result;
  }

  private static Collection<Collection<Object>> convertMatrix(final Value[][] value) {
    final Collection<Collection<Object>> result = new ArrayList<Collection<Object>>(value.length);
    for (final Value[] v : value) {
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

  public static MarketDataOverride invoke(final Object value, final ValueRequirement valueRequirement, final Operation operation) {
    return new MarketDataOverride(valueRequirement, value, operation);
  }

  public static MarketDataOverride invoke(final Object value, final String valueName, final UniqueId identifier, final ComputationTargetType type, final Operation operation) {
    return invoke(value, new ValueRequirement(valueName, new ComputationTargetSpecification(type, identifier)), operation);
  }

  public static MarketDataOverride invoke(final Object value, final String valueName, final ExternalId identifier, final Operation operation) {
    return invoke(value, new ValueRequirement(valueName, new ComputationTargetRequirement(ComputationTargetType.PRIMITIVE, identifier)), operation);
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    final Data value = (Data) parameters[VALUE];
    if (parameters[VALUE_REQUIREMENT] == null) {
      if (parameters[VALUE_NAME] == null) {
        throw new InvokeInvalidArgumentException(VALUE_NAME, "argument must be supplied when value requirement is omitted");
      }
      if (parameters[IDENTIFIER] == null) {
        throw new InvokeInvalidArgumentException(IDENTIFIER, "argument must be supplied when value requirement is omitted");
      }
      if (parameters[TYPE] == null) {
        final ExternalId externalId = sessionContext.getGlobalContext().getValueConverter().convertValue(sessionContext, parameters[IDENTIFIER], EXTERNAL_ID);
        return invoke(convertData(value), (String) parameters[VALUE_NAME], externalId, (Operation) parameters[OPERATION]);
      } else {
        final UniqueId uniqueId = sessionContext.getGlobalContext().getValueConverter().convertValue(sessionContext, parameters[IDENTIFIER], UNIQUE_ID);
        return invoke(convertData(value), (String) parameters[VALUE_NAME], uniqueId, (ComputationTargetType) parameters[TYPE], (Operation) parameters[OPERATION]);
      }
    } else {
      if (parameters[VALUE_NAME] != null) {
        throw new InvokeInvalidArgumentException(VALUE_NAME, "argument must be omitted when value requirement is supplied");
      }
      if (parameters[IDENTIFIER] != null) {
        throw new InvokeInvalidArgumentException(IDENTIFIER, "argument must be omitted when value requirement is supplied");
      }
      if (parameters[TYPE] != null) {
        throw new InvokeInvalidArgumentException(TYPE, "argument must be omitted when value requirement is supplied");
      }
      return invoke(convertData(value), (ValueRequirement) parameters[VALUE_REQUIREMENT], (Operation) parameters[OPERATION]);
    }
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
