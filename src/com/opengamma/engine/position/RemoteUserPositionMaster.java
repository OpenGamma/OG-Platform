/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import static com.opengamma.engine.position.server.UserPositionMasterServiceNames.USER_POSITION_MASTER_FIELD_OWNER;
import static com.opengamma.engine.position.server.UserPositionMasterServiceNames.USER_POSITION_MASTER_FIELD_RESULT;
import static com.opengamma.engine.position.server.UserPositionMasterServiceNames.USER_POSITION_MASTER_FIELD_PORTFOLIO;
import static com.opengamma.engine.position.server.UserPositionMasterServiceNames.USER_POSITION_MASTER_ADD_PORTFOLIO;
import static com.opengamma.engine.position.server.UserPositionMasterServiceNames.USER_POSITION_MASTER_HEARTBEAT;

import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides access to a remote {@link UserPositionMaster}.
 */
public class RemoteUserPositionMaster implements UserPositionMaster {

  private final RestClient _restClient;
  
  private final FudgeContext _fudgeContext;
  private final RestTarget _targetHeartbeat;
  private final RestTarget _targetAddPortfolio;
  
  public RemoteUserPositionMaster(FudgeContext fudgeContext, RestTarget baseTarget) {
    _restClient = RestClient.getInstance(fudgeContext, null);
    _targetHeartbeat = baseTarget.resolve(USER_POSITION_MASTER_HEARTBEAT);
    _targetAddPortfolio = baseTarget.resolve(USER_POSITION_MASTER_ADD_PORTFOLIO);
    _fudgeContext = fudgeContext;
  }
  
  @Override
  public void addPortfolio(UniqueIdentifier owner, Portfolio portfolio) {
    ArgumentChecker.notNull(owner, "owner");
    ArgumentChecker.notNull(portfolio, "portfolio");
    MutableFudgeFieldContainer msg = _fudgeContext.newMessage();
    FudgeSerializationContext context = new FudgeSerializationContext(_fudgeContext);
    context.objectToFudgeMsg(msg, USER_POSITION_MASTER_FIELD_OWNER, null, owner);
    context.objectToFudgeMsg(msg, USER_POSITION_MASTER_FIELD_PORTFOLIO, null, portfolio);
    _restClient.post(_targetAddPortfolio, msg);
  }

  @Override
  public boolean heartbeat(UniqueIdentifier owner) {
    ArgumentChecker.notNull(owner, "owner");
    MutableFudgeFieldContainer msg = _fudgeContext.newMessage();
    FudgeSerializationContext context = new FudgeSerializationContext(_fudgeContext);
    context.objectToFudgeMsg(msg, USER_POSITION_MASTER_FIELD_OWNER, null, owner);
    FudgeMsgEnvelope response = _restClient.post(_targetHeartbeat, msg);
    FudgeFieldContainer responseMsg = response.getMessage();
    boolean result = responseMsg.getBoolean(USER_POSITION_MASTER_FIELD_RESULT);
    return result;
  }

  @Override
  public Portfolio getPortfolio(UniqueIdentifier uid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<UniqueIdentifier> getPortfolioIds() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PortfolioNode getPortfolioNode(UniqueIdentifier uid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Position getPosition(UniqueIdentifier uid) {
    // TODO Auto-generated method stub
    return null;
  }
  
}
