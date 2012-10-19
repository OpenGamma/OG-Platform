/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.position;

import java.util.Arrays;
import java.util.List;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
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
 * Function to retrieve a portfolio from a position source
 */
public class FetchPortfolioFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance
   */
  public static final FetchPortfolioFunction INSTANCE = new FetchPortfolioFunction();
  
  private final MetaFunction _meta;
  
  private static final int IDENTIFIER = 0;

  private static List<MetaParameter> parameters() {
    final MetaParameter identifier = new MetaParameter("identifier", JavaTypeInfo.builder(UniqueId.class).get());
    return Arrays.asList(identifier);
  }

  private FetchPortfolioFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.POSITION, "FetchPortfolio", getParameters(), this));
  }

  protected FetchPortfolioFunction() {
    this(new DefinitionAnnotater(FetchPortfolioFunction.class));
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    final UniqueId identifier = (UniqueId) parameters[IDENTIFIER];
    try {
      return sessionContext.getGlobalContext().getPositionSource().getPortfolio(identifier, VersionCorrection.LATEST);
    } catch (DataNotFoundException e) {
      throw new InvokeInvalidArgumentException(IDENTIFIER, "Identifier not found");
    }
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
