/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.rest;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.financial.position.master.FullPortfolioGetRequest;
import com.opengamma.financial.position.master.FullPortfolioNodeGetRequest;
import com.opengamma.financial.position.master.FullPositionGetRequest;
import com.opengamma.financial.position.master.PortfolioTreeDocument;
import com.opengamma.financial.position.master.PortfolioTreeSearchHistoricRequest;
import com.opengamma.financial.position.master.PortfolioTreeSearchHistoricResult;
import com.opengamma.financial.position.master.PortfolioTreeSearchRequest;
import com.opengamma.financial.position.master.PortfolioTreeSearchResult;
import com.opengamma.financial.position.master.PositionDocument;
import com.opengamma.financial.position.master.PositionMaster;
import com.opengamma.financial.position.master.PositionSearchHistoricRequest;
import com.opengamma.financial.position.master.PositionSearchHistoricResult;
import com.opengamma.financial.position.master.PositionSearchRequest;
import com.opengamma.financial.position.master.PositionSearchResult;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides access to a remote {@link PositionMaster}.
 */
public class RemotePositionMaster implements PositionMaster {

  private final FudgeContext _fudgeContext;
  private final RestTarget _baseTarget;
  private final RestTarget _positionsTarget;
  private final RestTarget _portfoliosTarget;
  private final RestClient _restClient;
  
  public RemotePositionMaster(FudgeContext fudgeContext, RestTarget baseTarget) {
    _fudgeContext = fudgeContext;
    _baseTarget = baseTarget;
    _positionsTarget = baseTarget.resolveBase(PositionMasterResourceNames.POSITION_MASTER_POSITIONS);
    _portfoliosTarget = baseTarget.resolveBase(PositionMasterResourceNames.POSITION_MASTER_PORTFOLIOS);
    _restClient = RestClient.getInstance(fudgeContext, null);
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioTreeSearchResult searchPortfolioTrees(final PortfolioTreeSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioTreeDocument getPortfolioTree(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioTreeDocument addPortfolioTree(final PortfolioTreeDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    ArgumentChecker.notNull(document.getPortfolio().getRootNode(), "document.portfolio.rootNode");
    
    FudgeSerializationContext serializationContext = new FudgeSerializationContext(_fudgeContext);
    MutableFudgeFieldContainer msg = serializationContext.objectToFudgeMsg(document);
    FudgeMsgEnvelope response = _restClient.post(_portfoliosTarget, new FudgeMsgEnvelope(msg));
    FudgeDeserializationContext deserializationContext = new FudgeDeserializationContext(_fudgeContext);
    return deserializationContext.fudgeMsgToObject(PortfolioTreeDocument.class, response.getMessage());
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioTreeDocument updatePortfolioTree(final PortfolioTreeDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    ArgumentChecker.notNull(document.getPortfolioId(), "document.portfolioId");
    
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  @Override
  public void removePortfolioTree(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioTreeSearchHistoricResult searchPortfolioTreeHistoric(final PortfolioTreeSearchHistoricRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPortfolioId(), "document.portfolioId");
    
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioTreeDocument correctPortfolioTree(final PortfolioTreeDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    ArgumentChecker.notNull(document.getPortfolioId(), "document.portfolioId");
    
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionSearchResult searchPositions(final PositionSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument getPosition(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument addPosition(final PositionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPosition(), "document.position");
    ArgumentChecker.notNull(document.getParentNodeId(), "document.parentNodeId");
    
    FudgeSerializationContext serializationContext = new FudgeSerializationContext(_fudgeContext);
    MutableFudgeFieldContainer msg = serializationContext.objectToFudgeMsg(document);
    FudgeMsgEnvelope response = _restClient.post(_positionsTarget, new FudgeMsgEnvelope(msg));
    FudgeDeserializationContext deserializationContext = new FudgeDeserializationContext(_fudgeContext);
    return deserializationContext.fudgeMsgToObject(PositionDocument.class, response.getMessage());
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument updatePosition(final PositionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPosition(), "document.position");
    ArgumentChecker.notNull(document.getPositionId(), "document.positionId");
    
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  @Override
  public void removePosition(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionSearchHistoricResult searchPositionHistoric(final PositionSearchHistoricRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPositionId(), "request.positionId");
    
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument correctPosition(final PositionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPosition(), "document.position");
    ArgumentChecker.notNull(document.getPositionId(), "document.positionId");
    
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  @Override
  public Portfolio getFullPortfolio(final FullPortfolioGetRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPortfolioId(), "document.portfolioId");
    
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioNode getFullPortfolioNode(final FullPortfolioNodeGetRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPortfolioNodeId(), "document.portfolioNodeId");
    
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  @Override
  public Position getFullPosition(final FullPositionGetRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPositionId(), "document.positionId");
    
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string summary of this position master.
   * @return the string summary, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + _baseTarget + "]";
  }

}
