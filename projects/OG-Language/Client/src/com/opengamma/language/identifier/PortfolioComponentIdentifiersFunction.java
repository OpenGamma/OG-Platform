/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.identifier;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.AbstractPortfolioNodeTraversalCallback;
import com.opengamma.core.position.impl.PortfolioNodeTraverser;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.language.position.PortfolioUtils;

/**
 * Retrieves the identifiers of components that make up a portfolio. 
 */
public class PortfolioComponentIdentifiersFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final PortfolioComponentIdentifiersFunction INSTANCE = new PortfolioComponentIdentifiersFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    return Arrays.asList(
        new MetaParameter("portfolioIdentifier", JavaTypeInfo.builder(UniqueId.class).get()),
        new MetaParameter("preferredScheme", JavaTypeInfo.builder(ExternalSchemeRank.class).defaultValue(ExternalSchemeRank.DEFAULT).get()),
        new MetaParameter("includeSecurity", JavaTypeInfo.builder(Boolean.class).defaultValue(true).get()),
        new MetaParameter("includePosition", JavaTypeInfo.builder(Boolean.class).defaultValue(false).get()),
        new MetaParameter("includeTrade", JavaTypeInfo.builder(Boolean.class).defaultValue(false).get()),
        new MetaParameter("includePortfolioNode", JavaTypeInfo.builder(Boolean.class).defaultValue(false).get()));
  }

  private PortfolioComponentIdentifiersFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction("PortfolioComponentIdentifiers", getParameters(), this));
  }

  protected PortfolioComponentIdentifiersFunction() {
    this(new DefinitionAnnotater(PortfolioComponentIdentifiersFunction.class));
  }

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

  private static void storeIdentifier(final List<String> result, final UniqueId identifier) {
    result.add(identifier.toString());
  }

  private static void storeIdentifier(final List<String> result, final UniqueId identifier, final ExternalIdBundle identifiers, final ExternalSchemeRank rank) {
    final ExternalId externalId = rank.getPreferredIdentifier(identifiers);
    if (externalId == null) {
      result.add(identifier.toString());
    } else {
      result.add(externalId.toString());
    }
  }

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    final UniqueId portfolioIdentifier = (UniqueId) parameters[0];
    final ExternalSchemeRank externalSchemeRank = (ExternalSchemeRank) parameters[1];
    final boolean includeSecurity = (Boolean) parameters[2];
    final boolean includePosition = (Boolean) parameters[3];
    final boolean includeTrade = (Boolean) parameters[4];
    final boolean includePortfolioNode = (Boolean) parameters[5];
    final Portfolio portfolio = PortfolioUtils.getPortfolio(sessionContext.getGlobalContext(), portfolioIdentifier, includeSecurity);
    final List<String> result = new LinkedList<String>();
    PortfolioNodeTraverser.depthFirst(new AbstractPortfolioNodeTraversalCallback() {

      @Override
      public void preOrderOperation(final PortfolioNode node) {
        if (includePortfolioNode) {
          storeIdentifier(result, node.getUniqueId());
        }
      }

      @Override
      public void preOrderOperation(final Position position) {
        if (includePosition) {
          storeIdentifier(result, position.getUniqueId());
        }
        if (includeTrade) {
          for (Trade trade : position.getTrades()) {
            storeIdentifier(result, trade.getUniqueId());
          }
        }
        if (includeSecurity) {
          storeIdentifier(result, position.getSecurity().getUniqueId(), position.getSecurity().getExternalIdBundle(), externalSchemeRank);
        }
      }

    }).traverse(portfolio.getRootNode());
    return result;
  }

}
