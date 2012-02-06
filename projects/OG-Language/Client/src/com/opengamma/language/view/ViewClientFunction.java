/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.view;

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
 * Creates (or returns an existing) view client for a given view definition.
 */
public class ViewClientFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final ViewClientFunction INSTANCE = new ViewClientFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    final MetaParameter viewDescriptorParameter = new MetaParameter("viewDescriptor", JavaTypeInfo.builder(ViewClientDescriptor.class).get());
    final MetaParameter useSharedProcessParameter = new MetaParameter("useSharedProcess", JavaTypeInfo.builder(Boolean.class).defaultValue(true).get());
    final MetaParameter clientNameParameter = new MetaParameter("clientName", JavaTypeInfo.builder(String.class).allowNull().get());
    return Arrays.asList(viewDescriptorParameter, useSharedProcessParameter, clientNameParameter);
  }

  private ViewClientFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.VIEW, "ViewClient", getParameters(), this));
  }

  protected ViewClientFunction() {
    this(new DefinitionAnnotater(ViewClientFunction.class));
  }

  public static ViewClientKey invoke(final ViewClientDescriptor viewDescriptor, final boolean useSharedProcess) {
    return new ViewClientKey(viewDescriptor, useSharedProcess);
  }

  public static ViewClientKey invoke(final ViewClientDescriptor viewDescriptor, final boolean useSharedProcess, final String clientName) {
    return new ViewClientKey(viewDescriptor, useSharedProcess, clientName);
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    final ViewClientKey key;
    if (parameters[2] == null) {
      key = invoke((ViewClientDescriptor) parameters[0], (Boolean) parameters[1]);
    } else {
      key = invoke((ViewClientDescriptor) parameters[0], (Boolean) parameters[1], (String) parameters[2]);
    }
    return sessionContext.getUserContext().getViewClients().lockViewClient(key);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
