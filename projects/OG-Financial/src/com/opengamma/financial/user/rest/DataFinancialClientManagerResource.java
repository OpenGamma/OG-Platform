/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import java.net.URI;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.UriBuilder;

import com.opengamma.financial.user.FinancialClient;
import com.opengamma.financial.user.FinancialClientManager;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for the clients of a single user.
 * <p>
 * This resource receives and processes RESTful calls.
 */
public class DataFinancialClientManagerResource extends AbstractDataResource {

  /**
   * The client manager.
   */
  private final FinancialClientManager _clientManager;

  /**
   * Creates an instance.
   * 
   * @param clientManager  the client manager, not null
   */
  public DataFinancialClientManagerResource(FinancialClientManager clientManager) {
    _clientManager = clientManager;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the manager.
   * 
   * @return the manager, not null
   */
  public FinancialClientManager getClientManager() {
    return _clientManager;
  }

  //-------------------------------------------------------------------------
  @Path("{clientName}")
  public DataFinancialClientResource findClient(@PathParam("clientName") String clientName) {
    ArgumentChecker.notNull(clientName, "clientName");
    
    FinancialClient client = getClientManager().getOrCreateClient(clientName);
    return new DataFinancialClientResource(client);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param userName  the user name, not null
   * @param clientName  the client name, not null
   * @return the URI, not null
   */
  public static URI uriClient(URI baseUri, String userName, String clientName) {
    UriBuilder bld = UriBuilder.fromUri(DataFinancialUserManagerResource.uriUser(baseUri, userName)).path("clients/{clientName}");
    return bld.build(clientName);
  }

}
