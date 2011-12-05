/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.position;

import java.util.Arrays;
import java.util.List;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Creates a portfolio from a root position node
 */
public class PortfolioFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final PortfolioFunction INSTANCE = new PortfolioFunction();

  private final MetaFunction _meta;

  private static final int NAME = 0;
  private static final int ROOT_NODE = 1;

  private static List<MetaParameter> parameters() {
    final MetaParameter name = new MetaParameter("name", JavaTypeInfo.builder(String.class).get());
    final MetaParameter node = new MetaParameter("rootNode", JavaTypeInfo.builder(PortfolioNode.class).get());
    return Arrays.asList(name, node);
  }

  private PortfolioFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.POSITION, "Portfolio", getParameters(), this));
  }

  protected PortfolioFunction() {
    this(new DefinitionAnnotater(PortfolioFunction.class));
  }

  public static Portfolio invoke(final String name, final PortfolioNode rootNode) {
    return new SimplePortfolio(name, new SimplePortfolioNode(rootNode));
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((String) parameters[NAME], (PortfolioNode) parameters[ROOT_NODE]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
