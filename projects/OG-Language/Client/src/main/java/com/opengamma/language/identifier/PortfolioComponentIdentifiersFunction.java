/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.identifier;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.AbstractPortfolioNodeTraversalCallback;
import com.opengamma.core.position.impl.PortfolioNodeTraverser;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.language.position.PortfolioUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Retrieves the identifiers of components that make up a portfolio.
 */
public class PortfolioComponentIdentifiersFunction extends AbstractFunctionInvoker implements PublishedFunction {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioComponentIdentifiersFunction.class);

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
    _meta = info.annotate(new MetaFunction(Categories.IDENTIFIER, "PortfolioComponentIdentifiers", getParameters(), this));
  }

  protected PortfolioComponentIdentifiersFunction() {
    this(new DefinitionAnnotater(PortfolioComponentIdentifiersFunction.class));
  }

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

  private static void storeIdentifier(final List<Pair<String, String>> result, final UniqueId identifier) {
    result.add(Pair.<String, String>of(identifier.toString(), null));
  }

  private static void storeIdentifier(final List<Pair<String, String>> result, final ObjectId identifier) {
    result.add(Pair.<String, String>of(identifier.toString(), null));
  }

  private static void storeIdentifiers(final List<Pair<String, String>> result, final UniqueId uniqueId, final ExternalId externalId) {
    result.add(Pair.of(uniqueId.toString(), externalId.toString()));
  }

  private static void storeIdentifiers(final List<Pair<String, String>> result, final ObjectId objectId, final ExternalId externalId) {
    result.add(Pair.of(objectId.toString(), externalId.toString()));
  }

  private static void storeIdentifiers(final List<Pair<String, String>> result, final UniqueId identifier, final ExternalIdBundle identifiers, final ExternalSchemeRank rank) {
    final ExternalId externalId = rank.getPreferredIdentifier(identifiers);
    if (externalId == null) {
      storeIdentifier(result, identifier);
    } else {
      storeIdentifiers(result, identifier, externalId);
    }
  }

  private static void storeIdentifiers(final List<Pair<String, String>> result, final ObjectId identifier, final ExternalIdBundle identifiers, final ExternalSchemeRank rank) {
    final ExternalId externalId = rank.getPreferredIdentifier(identifiers);
    if (externalId == null) {
      storeIdentifier(result, identifier);
    } else {
      storeIdentifiers(result, identifier, externalId);
    }
  }

  @Override
  protected Data invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    final UniqueId portfolioIdentifier = (UniqueId) parameters[0];
    final ExternalSchemeRank externalSchemeRank = (ExternalSchemeRank) parameters[1];
    final boolean includeSecurity = (Boolean) parameters[2];
    final boolean includePosition = (Boolean) parameters[3];
    final boolean includeTrade = (Boolean) parameters[4];
    final boolean includePortfolioNode = (Boolean) parameters[5];
    s_logger.info("invoke {}, {}", portfolioIdentifier, externalSchemeRank);
    final Portfolio portfolio = PortfolioUtils.getPortfolio(sessionContext.getGlobalContext(), portfolioIdentifier, includeSecurity);
    if (portfolio == null) {
      throw new InvokeInvalidArgumentException(0, "Portfolio " + portfolioIdentifier + " not found");
    }
    final List<Pair<String, String>> componentIds = new LinkedList<Pair<String, String>>();
    PortfolioNodeTraverser.depthFirst(new AbstractPortfolioNodeTraversalCallback() {

      @Override
      public void preOrderOperation(final PortfolioNode node) {
        if (includePortfolioNode) {
          storeIdentifier(componentIds, node.getUniqueId());
        }
      }

      @Override
      public void preOrderOperation(final PortfolioNode parentNode, final Position position) {
        if (includePosition) {
          storeIdentifier(componentIds, position.getUniqueId());
        }
        if (includeTrade) {
          for (Trade trade : position.getTrades()) {
            storeIdentifier(componentIds, trade.getUniqueId());
          }
        }
        if (includeSecurity) {
          if (position.getSecurity() != null) {
            storeIdentifiers(componentIds, position.getSecurity().getUniqueId(), position.getSecurity().getExternalIdBundle(), externalSchemeRank);
          } else {
            storeIdentifiers(componentIds, position.getSecurityLink().getObjectId(), position.getSecurityLink().getExternalId(), externalSchemeRank);
          }
        }
      }

    }).traverse(portfolio.getRootNode());

    int width = includeSecurity ? 2 : 1;
    Value[][] values = new Value[componentIds.size()][width];
    int i = 0;
    for (Pair<String, String> portfolioComponent : componentIds) {
      values[i][0] = portfolioComponent.getFirst() != null ? ValueUtils.of(portfolioComponent.getFirst()) : new Value();
      if (includeSecurity) {
        values[i][1] = portfolioComponent.getSecond() != null ? ValueUtils.of(portfolioComponent.getSecond()) : new Value();
      }
      i++;
    }
    return DataUtils.of(values);
  }

}
