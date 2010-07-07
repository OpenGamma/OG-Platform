/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.rest;

import java.util.Set;

import javax.time.InstantProvider;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.financial.position.AddPortfolioNodeRequest;
import com.opengamma.financial.position.AddPortfolioRequest;
import com.opengamma.financial.position.AddPositionRequest;
import com.opengamma.financial.position.ManageablePositionMaster;
import com.opengamma.financial.position.ManagedPortfolio;
import com.opengamma.financial.position.ManagedPortfolioNode;
import com.opengamma.financial.position.ManagedPosition;
import com.opengamma.financial.position.SearchPortfoliosRequest;
import com.opengamma.financial.position.SearchPortfoliosResult;
import com.opengamma.financial.position.SearchPositionsRequest;
import com.opengamma.financial.position.SearchPositionsResult;
import com.opengamma.financial.position.UpdatePortfolioNodeRequest;
import com.opengamma.financial.position.UpdatePortfolioRequest;
import com.opengamma.financial.position.UpdatePositionRequest;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;

/**
 * Provides access to a remote {@link ManageablePositionMaster}.
 */
public class RemoteManagablePositionMaster implements ManageablePositionMaster {

  private final FudgeContext _fudgeContext;
  private final RestTarget _baseTarget;
  private final RestClient _restClient;
  
  public RemoteManagablePositionMaster(FudgeContext fudgeContext, RestTarget baseTarget) {
    _fudgeContext = fudgeContext;
    _baseTarget = baseTarget;
    _restClient = RestClient.getInstance(fudgeContext, null);
  }

  @Override
  public UniqueIdentifier addPortfolio(AddPortfolioRequest request) {
    FudgeSerializationContext serializationContext = new FudgeSerializationContext(_fudgeContext);
    MutableFudgeFieldContainer msg = serializationContext.objectToFudgeMsg(request);
    FudgeMsgEnvelope response = _restClient.post(_baseTarget, new FudgeMsgEnvelope(msg));
    
    FudgeDeserializationContext deserializationContext = new FudgeDeserializationContext(_fudgeContext);
    UniqueIdentifier uid = deserializationContext.fudgeMsgToObject(UniqueIdentifier.class, response.getMessage());
    return uid;
  }

  @Override
  public UniqueIdentifier addPortfolioNode(AddPortfolioNodeRequest request) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public UniqueIdentifier addPosition(AddPositionRequest request) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ManagedPortfolio getManagedPortfolio(UniqueIdentifier portfolioUid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ManagedPortfolioNode getManagedPortfolioNode(UniqueIdentifier nodeUid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ManagedPosition getManagedPosition(UniqueIdentifier positionUid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Portfolio getPortfolio(UniqueIdentifier uid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Portfolio getPortfolio(UniqueIdentifier uid, InstantProvider instantProvider) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PortfolioNode getPortfolioNode(UniqueIdentifier uid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PortfolioNode getPortfolioNode(UniqueIdentifier uid, InstantProvider instantProvider) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Position getPosition(UniqueIdentifier uid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Position getPosition(UniqueIdentifier uid, InstantProvider instantProvider) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isManagerFor(UniqueIdentifier uid) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isModificationSupported() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public UniqueIdentifier reinstatePortfolio(UniqueIdentifier portfolioUid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public UniqueIdentifier reinstatePortfolioNode(UniqueIdentifier nodeUid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public UniqueIdentifier reinstatePosition(UniqueIdentifier positionUid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public UniqueIdentifier removePortfolio(UniqueIdentifier portfolioUid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public UniqueIdentifier removePortfolioNode(UniqueIdentifier nodeUid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public UniqueIdentifier removePosition(UniqueIdentifier positionUid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SearchPortfoliosResult searchPortfolios(SearchPortfoliosRequest request) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SearchPositionsResult searchPositions(SearchPositionsRequest request) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public UniqueIdentifier updatePortfolio(UpdatePortfolioRequest request) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public UniqueIdentifier updatePortfolioNode(UpdatePortfolioNodeRequest request) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public UniqueIdentifier updatePosition(UpdatePositionRequest request) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<UniqueIdentifier> getPortfolioIds() {
    // TODO Auto-generated method stub
    return null;
  }
  
}
