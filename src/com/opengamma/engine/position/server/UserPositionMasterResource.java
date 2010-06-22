/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position.server;

import static com.opengamma.engine.position.server.UserPositionMasterServiceNames.USER_POSITION_MASTER_ADD_PORTFOLIO;
import static com.opengamma.engine.position.server.UserPositionMasterServiceNames.USER_POSITION_MASTER_FIELD_OWNER;
import static com.opengamma.engine.position.server.UserPositionMasterServiceNames.USER_POSITION_MASTER_FIELD_PORTFOLIO;
import static com.opengamma.engine.position.server.UserPositionMasterServiceNames.USER_POSITION_MASTER_FIELD_RESULT;
import static com.opengamma.engine.position.server.UserPositionMasterServiceNames.USER_POSITION_MASTER_HEARTBEAT;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;

import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.UserPositionMaster;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful wrapper for {@link UserPositionMaster}.
 */
public class UserPositionMasterResource {

  private final FudgeContext _fudgeContext;
  private final UserPositionMaster _userPositionMaster;
  
  public UserPositionMasterResource(FudgeContext fudgeContext, UserPositionMaster userPositionMaster) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(userPositionMaster, "userPositionMaster");    
    _fudgeContext = fudgeContext;
    _userPositionMaster = userPositionMaster;
  }
  
  @POST
  @Path(USER_POSITION_MASTER_ADD_PORTFOLIO)
  public void addPortfolio(FudgeMsgEnvelope envelope) {        
    FudgeFieldContainer msg = envelope.getMessage();
    FudgeDeserializationContext context = new FudgeDeserializationContext(_fudgeContext);
    UniqueIdentifier owner = context.fieldValueToObject(UniqueIdentifier.class, msg.getByName(USER_POSITION_MASTER_FIELD_OWNER));
    Portfolio portfolio = context.fieldValueToObject(Portfolio.class, msg.getByName(USER_POSITION_MASTER_FIELD_PORTFOLIO));
    _userPositionMaster.addPortfolio(owner, portfolio);
  }
  
  @POST
  @Path(USER_POSITION_MASTER_HEARTBEAT)
  public FudgeMsgEnvelope heartbeat(FudgeMsgEnvelope envelope) {
    FudgeFieldContainer msg = envelope.getMessage();
    FudgeDeserializationContext context = new FudgeDeserializationContext(_fudgeContext);
    UniqueIdentifier owner = context.fieldValueToObject(UniqueIdentifier.class, msg.getByName(USER_POSITION_MASTER_FIELD_OWNER));
    boolean result = _userPositionMaster.heartbeat(owner);
    
    MutableFudgeFieldContainer response = _fudgeContext.newMessage();
    response.add(USER_POSITION_MASTER_FIELD_RESULT, result);
    return new FudgeMsgEnvelope(response);
  }
  
}
