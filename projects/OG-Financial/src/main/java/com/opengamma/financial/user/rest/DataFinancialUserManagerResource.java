/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.financial.user.FinancialUser;
import com.opengamma.financial.user.FinancialUserManager;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for users.
 * <p>
 * This resource receives and processes RESTful calls.
 */
@Path("userManager")
public class DataFinancialUserManagerResource extends AbstractDataResource {

  /**
   * The user manager.
   */
  private final FinancialUserManager _manager;

  /**
   * Creates an instance.
   * 
   * @param manager  the manager, not null
   */
  public DataFinancialUserManagerResource(FinancialUserManager manager) {
    ArgumentChecker.notNull(manager, "manager");
    _manager = manager;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the manager.
   * 
   * @return the manager, not null
   */
  public FinancialUserManager getUserManager() {
    return _manager;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @Path("users/{userName}")
  public DataFinancialUserResource findUser(@PathParam("userName") String userName) {
    ArgumentChecker.notNull(userName, "userName");
    
    FinancialUser user = _manager.getOrCreateUser(userName);
    return new DataFinancialUserResource(user);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param userName  the user name, not null
   * @return the URI, not null
   */
  public static URI uriUser(URI baseUri, String userName) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("users/{userName}");
    return bld.build(userName);
  }

}
