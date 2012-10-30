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
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.financial.user.rest.RemoteClient;
import com.opengamma.id.UniqueId;
import com.opengamma.language.client.ContextRemoteClient;
import com.opengamma.language.client.MasterID;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.procedure.AbstractProcedureInvoker;
import com.opengamma.language.procedure.MetaProcedure;
import com.opengamma.language.procedure.PublishedProcedure;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurityLink;

/**
 * Writes a portfolio to the {@link PositionMaster}.
 */
public class StorePortfolioProcedure extends AbstractProcedureInvoker.SingleResult implements PublishedProcedure {

  /**
   * Default instance.
   */
  public static final StorePortfolioProcedure INSTANCE = new StorePortfolioProcedure();

  private final MetaProcedure _meta;

  private static final int PORTFOLIO = 0;
  private static final int IDENTIFIER = 1;
  private static final int MASTER = 2;

  private static List<MetaParameter> parameters() {
    final MetaParameter portfolio = new MetaParameter("portfolio", JavaTypeInfo.builder(Portfolio.class).get());
    final MetaParameter identifier = new MetaParameter("identifier", JavaTypeInfo.builder(UniqueId.class).allowNull().get());
    final MetaParameter target = new MetaParameter("master", JavaTypeInfo.builder(MasterID.class).defaultValue(MasterID.SESSION).get());
    return Arrays.asList(portfolio, identifier, target);
  }

  private StorePortfolioProcedure(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaProcedure(Categories.POSITION, "StorePortfolio", getParameters(), this));
  }

  protected StorePortfolioProcedure() {
    this(new DefinitionAnnotater(StorePortfolioProcedure.class));
  }

  // TODO: Move logic from OG-Excel into OG-Language for cases where the securities are not in the same master

  private static ManageablePortfolioNode createPortfolioNode(final PositionMaster positionMaster, final PortfolioNode node) {
    final ManageablePortfolioNode newNode = new ManageablePortfolioNode(node.getName());
    for (PortfolioNode child : node.getChildNodes()) {
      newNode.addChildNode(createPortfolioNode(positionMaster, child));
    }
    for (Position position : node.getPositions()) {
      final ManageablePosition newPosition = new ManageablePosition();
      newPosition.setAttributes(position.getAttributes());
      newPosition.setQuantity(position.getQuantity());
      newPosition.setSecurityLink(new ManageableSecurityLink(position.getSecurityLink()));
      for (Trade trade : position.getTrades()) {
        newPosition.addTrade(new ManageableTrade(trade));
      }
      newNode.addPosition(positionMaster.add(new PositionDocument(newPosition)).getUniqueId());
    }
    return newNode;
  }

  protected static UniqueId invoke(final PortfolioMaster portfolioMaster, final PositionMaster positionMaster, final UniqueId identifier, final Portfolio portfolio) {
    final ManageablePortfolio newPortfolio = new ManageablePortfolio(portfolio.getName());
    newPortfolio.setAttributes(portfolio.getAttributes());
    newPortfolio.setRootNode(createPortfolioNode(positionMaster, portfolio.getRootNode()));
    PortfolioDocument document = new PortfolioDocument(newPortfolio);
    if (identifier != null) {
      document.setUniqueId(identifier);
      document = portfolioMaster.update(document);
    } else {
      document = portfolioMaster.add(document);
    }
    return document.getUniqueId();
  }

  public static UniqueId invoke(final SessionContext sessionContext, final Portfolio portfolio, final UniqueId identifier, final MasterID master) {
    final RemoteClient client = ContextRemoteClient.get(sessionContext, master);
    final PortfolioMaster prtMaster;
    final PositionMaster posMaster;
    try {
      prtMaster = client.getPortfolioMaster();
      posMaster = client.getPositionMaster();
    } catch (UnsupportedOperationException e) {
      throw new InvokeInvalidArgumentException(MASTER, e);
    }
    return invoke(prtMaster, posMaster, identifier, portfolio);
  }

  // AbstractProcedureInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke(sessionContext, (Portfolio) parameters[PORTFOLIO], (UniqueId) parameters[IDENTIFIER], (MasterID) parameters[MASTER]);
  }

  // PublishedProcedure

  @Override
  public MetaProcedure getMetaProcedure() {
    return _meta;
  }

}
