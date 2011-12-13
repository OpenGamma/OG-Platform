/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.position;

import java.util.Arrays;
import java.util.List;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
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
 * Creates a portfolio node from one or more positions or other nodes
 */
public class PortfolioNodeFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final PortfolioNodeFunction INSTANCE = new PortfolioNodeFunction();

  private final MetaFunction _meta;

  private static final int NAME = 0;
  private static final int NODES = 1;
  private static final int POSITIONS = 2;

  private static List<MetaParameter> parameters() {
    final MetaParameter name = new MetaParameter("name", JavaTypeInfo.builder(String.class).allowNull().get());
    final MetaParameter nodes = new MetaParameter("nodes", JavaTypeInfo.builder(List.class).allowNull().parameter(JavaTypeInfo.builder(PortfolioNode.class).get()).get());
    final MetaParameter positions = new MetaParameter("positions", JavaTypeInfo.builder(List.class).allowNull().parameter(JavaTypeInfo.builder(Position.class).get()).get());
    return Arrays.asList(name, nodes, positions);
  }

  private PortfolioNodeFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.POSITION, "PortfolioNode", getParameters(), this));
  }

  protected PortfolioNodeFunction() {
    this(new DefinitionAnnotater(PortfolioNodeFunction.class));
  }

  public static PortfolioNode invoke(final String name, final List<PortfolioNode> nodes, final List<Position> positions) {
    final SimplePortfolioNode node = new SimplePortfolioNode(name);
    if (nodes != null) {
      for (PortfolioNode child : nodes) {
        node.addChildNode(child);
      }
    }
    if (positions != null) {
      for (Position position : positions) {
        node.addPosition(position);
      }
    }
    return node;
  }

  // AbstractFunctionInvoker

  @SuppressWarnings("unchecked")
  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((String) parameters[NAME], (List<PortfolioNode>) parameters[NODES], (List<Position>) parameters[POSITIONS]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
