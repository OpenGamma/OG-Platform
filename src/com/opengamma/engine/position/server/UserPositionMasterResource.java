/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position.server;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
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
  private final UserPositionMaster<UniqueIdentifier> _userPositionMaster;
  
  public UserPositionMasterResource(FudgeContext fudgeContext, UserPositionMaster<UniqueIdentifier> userPositionMaster) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(userPositionMaster, "userPositionMaster");    
    _fudgeContext = fudgeContext;
    _userPositionMaster = userPositionMaster;
  }
  
  @PUT
  @Path("addPortfolio")
  public void addPortfolio(@FormParam("uid") String ownerUid, FudgeMsgEnvelope portfolioEnvelope) {
    UniqueIdentifier owner = UniqueIdentifier.parse(ownerUid); 
    FudgeFieldContainer portfolioMsg = portfolioEnvelope.getMessage();
    FudgeDeserializationContext deserializationContext = new FudgeDeserializationContext(_fudgeContext);
    Portfolio portfolio = deserializationContext.fudgeMsgToObject(Portfolio.class, portfolioMsg);
    _userPositionMaster.addPortfolio(owner, portfolio);
  }
  
  @PUT
  @Path("heartbeat")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public boolean heartbeat(@FormParam("uid") String ownerUid) {
    UniqueIdentifier owner = UniqueIdentifier.parse(ownerUid);
    return _userPositionMaster.heartbeat(owner);
  }
  
}
