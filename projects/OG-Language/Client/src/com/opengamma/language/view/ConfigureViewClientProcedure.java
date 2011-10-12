/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import java.util.Arrays;
import java.util.List;

import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.procedure.AbstractProcedureInvoker;
import com.opengamma.language.procedure.MetaProcedure;
import com.opengamma.language.procedure.PublishedProcedure;

/**
 * Makes a configuration change to a view client.
 */
public final class ConfigureViewClientProcedure extends AbstractProcedureInvoker.NoResult implements PublishedProcedure {

  /**
   * Default instance.
   */
  public static final ConfigureViewClientProcedure INSTANCE = new ConfigureViewClientProcedure();

  private final MetaProcedure _meta;

  private static List<MetaParameter> parameters() {
    final MetaParameter viewClient = new MetaParameter("viewClient", JavaTypeInfo.builder(ViewClientHandle.class).get());
    final MetaParameter configuration = new MetaParameter("configuration", JavaTypeInfo.builder(Object.class).get()); // TODO
    return Arrays.asList(viewClient, configuration);
  }

  private ConfigureViewClientProcedure(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaProcedure("ConfigureViewClient", getParameters(), this));
  }

  private ConfigureViewClientProcedure() {
    this(new DefinitionAnnotater(ConfigureViewClientProcedure.class));
  }

  public static void applyConfiguration(final UserViewClient viewClient, final Object configuration) {
    // TODO:
  }

  // AbstractProcedureInvoker.NoResult

  @Override
  protected void invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    final ViewClientHandle viewClient = (ViewClientHandle) parameters[0];
    final Object configuration = (Object) parameters[1]; // TODO:
    applyConfiguration(viewClient.get(), configuration);
    viewClient.unlock();
  }

  // PublishedProcedure

  @Override
  public MetaProcedure getMetaProcedure() {
    return _meta;
  }

}
