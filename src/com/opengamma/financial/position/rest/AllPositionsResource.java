/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.rest;

import javax.ws.rs.POST;
import javax.ws.rs.Produces;

import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;

import com.opengamma.financial.position.master.PositionDocument;
import com.opengamma.financial.position.master.PositionMaster;
import com.opengamma.transport.jaxrs.FudgeRest;

/**
 * RESTful resource for all positions globally, which makes sense given that they have globally-unique identifiers.
 */
public class AllPositionsResource {

  private final PortfoliosResource _portfoliosResource;
  private final FudgeDeserializationContext _fudgeDeserializationContext;
  
  public AllPositionsResource(PortfoliosResource portfoliosResource, FudgeDeserializationContext fudgeDeserializationContext) {
    _portfoliosResource = portfoliosResource;
    _fudgeDeserializationContext = fudgeDeserializationContext;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Gets the portfolios resource.
   * @return the portfolios resource, not null
   */
  public PortfoliosResource getPortfoliosResource() {
    return _portfoliosResource;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Gets the position master.
   * @return the position master, not null
   */
  public PositionMaster getPositionMaster() {
    return getPortfoliosResource().getPositionMaster();
  }
  
  protected FudgeDeserializationContext getFudgeContext() {
    return _fudgeDeserializationContext;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Adds a new position to the position master.
   * 
   * @param addPortfolioRequestMsg  the request containing the position document, not null
   * @return  the updated document, not null
   */
  @POST
  @Produces(FudgeRest.MEDIA)
  public PositionDocument postFudge(FudgeMsgEnvelope addPortfolioRequestMsg) {
    PositionDocument request = _fudgeDeserializationContext.fudgeMsgToObject(PositionDocument.class, addPortfolioRequestMsg.getMessage());
    return getPositionMaster().addPosition(request);
  }
  
}
