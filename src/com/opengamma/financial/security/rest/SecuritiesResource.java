/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.rest;

import java.net.URI;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.server.EngineFudgeContextConfiguration;
import com.opengamma.financial.fudgemsg.FinancialFudgeContextConfiguration;
import com.opengamma.financial.security.AddSecurityRequest;
import com.opengamma.financial.security.ManagableSecurityMaster;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource for all securities.
 * <p>
 * The securities resource represents the whole of a security master.
 */
@Path("/securities")
public class SecuritiesResource {

  /**
   * The injected security master.
   */
  private final ManagableSecurityMaster _secMaster;
  /**
   * The fudge context to use when deserializing requests 
   */
  private final FudgeDeserializationContext _fudgeDeserializationContext;
  /**
   * The fudge context to use when serializing responses 
   */
  private final FudgeSerializationContext _fudgeSerializationContext;
  /**
   * Information about the URI injected by JSR-311.
   */
  @Context
  private UriInfo _uriInfo;

  /**
   * Creates the resource.
   * @param secMaster  the security master, not null
   */
  public SecuritiesResource(final ManagableSecurityMaster secMaster) {
    ArgumentChecker.notNull(secMaster, "SecurityMaster");
    _secMaster = secMaster;
    
    FudgeContext fudgeContext = new FudgeContext();
    EngineFudgeContextConfiguration.INSTANCE.configureFudgeContext(fudgeContext);
    FinancialFudgeContextConfiguration.INSTANCE.configureFudgeContext(fudgeContext);
    _fudgeDeserializationContext = new FudgeDeserializationContext(fudgeContext);
    _fudgeSerializationContext = new FudgeSerializationContext(fudgeContext);
  }

  /**
   * Creates the resource.
   * @param uriInfo  the URI information, not null
   * @param secMaster  the security master, not null
   */
  public SecuritiesResource(UriInfo uriInfo, final ManagableSecurityMaster secMaster) {
    this(secMaster);
    ArgumentChecker.notNull(uriInfo, "uriInfo");
    _uriInfo = uriInfo;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the security master.
   * @return the security master, not null
   */
  public ManagableSecurityMaster getSecurityMaster() {
    return _secMaster;
  }

  /**
   * Gets the URI info.
   * @return the uri info, not null
   */
  public UriInfo getUriInfo() {
    return _uriInfo;
  }

  //-------------------------------------------------------------------------
//  @GET
//  @Produces(FudgeRest.MEDIA)
//  public SearchPortfoliosResult getAsFudge(
//      @QueryParam("page") int page,
//      @QueryParam("pageSize") int pageSize,
//      @QueryParam("name") String name,
//      @QueryParam("deleted") boolean deleted) {
//    PagingRequest paging = PagingRequest.of(page, pageSize);
//    SearchSecuritiesRequest request = new SearchSecuritiesRequest(paging);
//    request.setName(StringUtils.trimToNull(name));
//    request.setIncludeDeleted(deleted);
//    return getSecurityMaster().searchSecurities(request);
//  }

  @POST
  public FudgeMsgEnvelope postFudge(FudgeMsgEnvelope env) {
    AddSecurityRequest request = _fudgeDeserializationContext.fudgeMsgToObject(AddSecurityRequest.class, env.getMessage());
    request.checkValid();
    
    UniqueIdentifier uid = getSecurityMaster().addSecurity(request);
    MutableFudgeFieldContainer responseMsg = _fudgeSerializationContext.objectToFudgeMsg(uid);
    return new FudgeMsgEnvelope(responseMsg);
    // TODO: Use code below
//    UniqueIdentifier uid = getSecurityMaster().addSecurity(request);
//    URI uri = SecurityResource.uri(getUriInfo(), uid);
//    return Response.created(uri).build();
  }

  //-------------------------------------------------------------------------
  @Path("{securityUid}")
  public SecurityResource findSecurity(@PathParam("securityUid") String uidStr) {
    UniqueIdentifier uid = UniqueIdentifier.parse(uidStr);
    return new SecurityResource(this, uid);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for securities.
   * @param uriInfo  the URI information, not null
   * @return the URI, not null
   */
  public static URI uri(UriInfo uriInfo) {
    return uriInfo.getBaseUriBuilder().path(SecuritiesResource.class).build();
  }

}
