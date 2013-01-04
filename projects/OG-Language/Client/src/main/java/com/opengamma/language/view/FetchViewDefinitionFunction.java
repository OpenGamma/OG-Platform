/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Retrieves a view definition from the database.
 */
public class FetchViewDefinitionFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final FetchViewDefinitionFunction INSTANCE = new FetchViewDefinitionFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    final MetaParameter viewDefinitionId = new MetaParameter("id", JavaTypeInfo.builder(UniqueId.class).get());
    return ImmutableList.of(viewDefinitionId);
  }

  protected FetchViewDefinitionFunction() {
    this(new DefinitionAnnotater(FetchViewDefinitionFunction.class));
  }

  private FetchViewDefinitionFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.VIEW, "FetchViewDefinition", getParameters(), this));
  }

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

  public ViewDefinition invoke(final ConfigSource configSource, final UniqueId viewDefinitionId) {
    try {
      return configSource.getConfig(ViewDefinition.class, viewDefinitionId);
    } catch (final DataNotFoundException e) {
      throw new InvokeInvalidArgumentException(0, "View definition not found");
    }
  }

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) throws AsynchronousExecution {
    final ConfigSource configSource = sessionContext.getGlobalContext().getViewProcessor().getConfigSource();
    final UniqueId viewDefinitionId = (UniqueId) parameters[0];
    return invoke(configSource, viewDefinitionId);
  }

}
