/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import java.util.Arrays;
import java.util.List;

import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.procedure.AbstractProcedureInvoker;
import com.opengamma.language.procedure.MetaProcedure;
import com.opengamma.language.procedure.PublishedProcedure;

/**
 * Requests that the next cycle be run on a view.
 */
public final class TriggerViewCycleProcedure extends AbstractProcedureInvoker.NoResult implements PublishedProcedure {

  /**
   * Default instance.
   */
  public static final TriggerViewCycleProcedure INSTANCE = new TriggerViewCycleProcedure();

  private final MetaProcedure _meta;
  
  private static List<MetaParameter> parameters() {
    final MetaParameter viewClient = new MetaParameter("viewClient", JavaTypeInfo.builder(ViewClientHandle.class).get());
    return Arrays.asList(viewClient);
  }

  private TriggerViewCycleProcedure(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaProcedure(Categories.VIEW, "TriggerViewCycle", getParameters(), this));
  }

  private TriggerViewCycleProcedure() {
    this(new DefinitionAnnotater(TriggerViewCycleProcedure.class));
  }

  public static void invoke(final ViewClient viewClient) {
    viewClient.triggerCycle();
  }

  // AbstractProcedureInvoker.NoResult

  @Override
  protected void invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    final ViewClientHandle viewClient = (ViewClientHandle) parameters[0];
    invoke(viewClient.get().getViewClient());
    viewClient.unlock();
  }

  // PublishedProcedure

  @Override
  public MetaProcedure getMetaProcedure() {
    return _meta;
  }

}
