/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriInfo;

import com.opengamma.financial.livedata.rest.LiveDataResource;
import com.opengamma.financial.livedata.user.InMemoryUserSnapshotProvider;
import com.opengamma.financial.position.ManagablePositionMaster;
import com.opengamma.financial.position.memory.InMemoryPositionMaster;
import com.opengamma.financial.position.rest.PortfoliosResource;
import com.opengamma.financial.user.UserResourceDetails;
import com.opengamma.financial.user.UserUniqueIdentifierUtils;

/**
 * Temporary RESTful resource representing a user's client session.
 */
@Path("/users/{username}/clients/{clientUid}")
public class ClientResource {
  
  /**
   * The path used to retrieve user portfolios
   */
  public static final String PORTFOLIOS_PATH = "portfolios";
  
  /**
   * The path used to retrieve user Live Data
   */
  public static final String LIVEDATA_PATH = "livedata";
  
  private final ClientsResource _clientsResource;
  private final ManagablePositionMaster _positionMaster;
  private final InMemoryUserSnapshotProvider _liveData;
  
  public ClientResource(ClientsResource clientsResource, String clientName) {
    _clientsResource = clientsResource;
    final String username = clientsResource.getUserResource().getUserName();
    _positionMaster = new InMemoryPositionMaster(UserUniqueIdentifierUtils.getTemplate(new UserResourceDetails(
        username, clientName, PORTFOLIOS_PATH)));
    _liveData = new InMemoryUserSnapshotProvider(UserUniqueIdentifierUtils.getTemplate(new UserResourceDetails(
        username, clientName, LIVEDATA_PATH)));
  }
  
  /**
   * Gets the URI info.
   * @return the uri info, not null
   */
  public UriInfo getUriInfo() {
    return getClientsResource().getUriInfo();
  }
  
  public ClientsResource getClientsResource() {
    return _clientsResource;
  }
  
  @Path(PORTFOLIOS_PATH)
  public PortfoliosResource getPortfolios() {
    return new PortfoliosResource(getUriInfo(), _positionMaster);
  }
  
  @Path(LIVEDATA_PATH)
  public LiveDataResource getLiveDataResource() {
    return new LiveDataResource(_liveData);
  }
  
}
