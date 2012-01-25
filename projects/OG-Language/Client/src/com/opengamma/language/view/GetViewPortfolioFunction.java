/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.id.UniqueId;
import com.opengamma.language.async.AsynchronousExecution;
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
 * Returns the identifier of the portfolio associated with the view definition
 */
public class GetViewPortfolioFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final GetViewPortfolioFunction INSTANCE = new GetViewPortfolioFunction();
  
  private final MetaFunction _meta;
  
  private static List<MetaParameter> parameters() {
    final MetaParameter viewDefinitionId = new MetaParameter("id", JavaTypeInfo.builder(UniqueId.class).get());
    return ImmutableList.of(viewDefinitionId);
  }
  
  protected GetViewPortfolioFunction() {
    this (new DefinitionAnnotater(GetViewPortfolioFunction.class));
  }
  
  private GetViewPortfolioFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.VIEW, "GetViewPortfolio", getParameters(), this));
  }
  
  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }
  
  public UniqueId invoke(ViewDefinitionRepository repository, UniqueId viewDefinitionId) {
    
    ViewDefinition viewDefinition = repository.getDefinition(viewDefinitionId);
    if (viewDefinition == null) {
      throw  new InvokeInvalidArgumentException("View definition not found");
    }
    return viewDefinition.getPortfolioId();
  }

  @Override
  protected Object invokeImpl(SessionContext sessionContext, Object[] parameters) throws AsynchronousExecution {
    final ViewDefinitionRepository repository = sessionContext.getGlobalContext().getViewProcessor().getViewDefinitionRepository();  
    final UniqueId viewDefinitionId  = (UniqueId) parameters[0]; 
    return invoke(repository, viewDefinitionId);
  }

}
