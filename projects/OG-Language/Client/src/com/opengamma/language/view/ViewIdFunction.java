/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.DataNotFoundException;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.id.UniqueId;
import com.opengamma.language.async.AsynchronousExecution;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Returns the identifier of a view definition with a given name.
 */
public class ViewIdFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final ViewIdFunction INSTANCE = new ViewIdFunction();
  
  private final MetaFunction _meta;
  
  private static List<MetaParameter> parameters() {
    final MetaParameter name = new MetaParameter("name", JavaTypeInfo.builder(String.class).get());
    return ImmutableList.of(name);
  }
  
  protected ViewIdFunction() {
    this(new DefinitionAnnotater(ViewIdFunction.class));
  }
  
  private ViewIdFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.VIEW, "ViewId", getParameters(), this));
  }
  
  public UniqueId invoke(ViewDefinitionRepository repository, String viewDefinitionName) {
    ViewDefinition definition = repository.getDefinition(viewDefinitionName);
    if (definition == null) {
      throw new DataNotFoundException("No view definition found with name '" + viewDefinitionName + "'");
    }
    return definition.getUniqueId();
  }

  @Override
  protected Object invokeImpl(SessionContext sessionContext, Object[] parameters) throws AsynchronousExecution {
    final ViewDefinitionRepository repository = sessionContext.getGlobalContext().getViewProcessor().getViewDefinitionRepository();
    final String viewDefinitionName = (String) parameters[0];
    return invoke(repository, viewDefinitionName);
  }

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }  
  
}
