/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.rest;

import java.net.URI;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.opengamma.engine.security.Security;
import com.opengamma.financial.security.ManagableSecurityMaster;
import com.opengamma.financial.security.UpdateSecurityRequest;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource for a security.
 */
@Path("/securities/{securityUid}")
public class SecurityResource {

  /**
   * The securities resource.
   */
  private final SecuritiesResource _securitiesResource;
  /**
   * The security unique identifier.
   */
  private final UniqueIdentifier _securityUid;

  /**
   * Creates the resource.
   * @param securitiesResource  the parent resource, not null
   * @param securityUid  the security unique identifier, not null
   */
  public SecurityResource(final SecuritiesResource securitiesResource, final UniqueIdentifier securityUid) {
    ArgumentChecker.notNull(securitiesResource, "security master");
    ArgumentChecker.notNull(securityUid, "security");
    _securitiesResource = securitiesResource;
    _securityUid = securityUid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the securities resource.
   * @return the securities resource, not null
   */
  public SecuritiesResource getSecuritiesResource() {
    return _securitiesResource;
  }

  /**
   * Gets the security unique identifier.
   * @return the unique identifier, not null
   */
  public UniqueIdentifier getSecurityUid() {
    return _securityUid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the security master.
   * @return the security master, not null
   */
  public ManagableSecurityMaster getSecurityMaster() {
    return getSecuritiesResource().getSecurityMaster();
  }

  /**
   * Gets the URI info.
   * @return the URI info, not null
   */
  public UriInfo getUriInfo() {
    return getSecuritiesResource().getUriInfo();
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(FudgeRest.MEDIA)
  public Security getAsFudge() {
    return getSecurityMaster().getSecurity(_securityUid);
  }

  @PUT
  public Response putFudge(Security security) {
    UpdateSecurityRequest request = new UpdateSecurityRequest(security);
    request.setUniqueIdentifier(getSecurityUid());
    request.checkValid();
    UniqueIdentifier uid = getSecurityMaster().updateSecurity(request);
    URI uri = SecurityResource.uri(getUriInfo(), uid);
    return Response.ok().location(uri).build();
  }

  @DELETE
  public Response deleteFudge() {
    getSecurityMaster().removeSecurity(getSecurityUid());
    return Response.ok().build();
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for a security.
   * @param uriInfo  the URI information, not null
   * @param securityUid  the security unique identifier, not null
   * @return the URI, not null
   */
  public static URI uri(UriInfo uriInfo, UniqueIdentifier securityUid) {
    return uriInfo.getBaseUriBuilder().path(SecurityResource.class).build(securityUid);
  }

}
