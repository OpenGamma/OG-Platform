/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.config;

import java.util.Arrays;
import java.util.List;

import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Creates a {@link ValueProperty} configuration item.
 */
public class ValuePropertyFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final ValuePropertyFunction INSTANCE = new ValuePropertyFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    return Arrays.asList(
        new MetaParameter("name", JavaTypeInfo.builder(String.class).get()),
        new MetaParameter("value", JavaTypeInfo.builder(String.class).get().arrayOfWithAllowNull(true)),
        new MetaParameter("configuration", JavaTypeInfo.builder(String.class).allowNull().get()),
        new MetaParameter("optional", JavaTypeInfo.builder(Boolean.class).defaultValue(false).get()));
  }

  private ValuePropertyFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.VALUE, "ValueProperty", getParameters(), this));
  }

  protected ValuePropertyFunction() {
    this(new DefinitionAnnotater(ValuePropertyFunction.class));
  }

  public static ValueProperty invoke(final String name, final String[] value, final String configuration, final boolean optional) {
    final ValueProperty result = new ValueProperty(name, optional);
    if (value != null) {
      result.setValue(Arrays.asList(value));
    }
    if (configuration != null) {
      result.setConfiguration(configuration);
    }
    return result;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((String) parameters[0], (String[]) parameters[1], (String) parameters[2], (Boolean) parameters[3]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
