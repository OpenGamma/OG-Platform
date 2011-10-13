/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.view;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.id.UniqueId;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Returns a list of matching views, searched by name.
 */
public class ViewsFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final ViewsFunction INSTANCE = new ViewsFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    final MetaParameter name = new MetaParameter("name", JavaTypeInfo.builder(String.class).allowNull().get());
    return Arrays.asList(name);
  }

  private ViewsFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction("Views", getParameters(), this));
  }

  protected ViewsFunction() {
    this(new DefinitionAnnotater(ViewsFunction.class));
  }

  public static Map<UniqueId, String> invoke(final ViewDefinitionRepository repository, final String viewName) {
    final Map<UniqueId, String> entries;
    if (viewName == null) {
      entries = repository.getDefinitionEntries();
    } else {
      final ViewDefinition viewDefinition = repository.getDefinition(viewName);
      if (viewDefinition != null) {
        entries = Collections.singletonMap(viewDefinition.getUniqueId(), viewDefinition.getName());
      } else {
        entries = Collections.emptyMap();
      }
    }
    return entries;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    final ViewDefinitionRepository repository = sessionContext.getGlobalContext().getViewProcessor().getViewDefinitionRepository();
    final String viewName = (String) parameters[0];
    return invoke(repository, viewName);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
