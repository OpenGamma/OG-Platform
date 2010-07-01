/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriInfo;

import com.opengamma.financial.position.ManagablePositionMaster;
import com.opengamma.financial.position.memory.InMemoryPositionMaster;
import com.opengamma.financial.position.rest.PortfoliosResource;
import com.opengamma.financial.security.ManagableSecurityMaster;
import com.opengamma.financial.security.memory.InMemorySecurityMaster;
import com.opengamma.financial.security.rest.SecuritiesResource;
import com.opengamma.financial.user.UserResourceDetails;
import com.opengamma.financial.user.UserUniqueIdentifierUtils;
import com.opengamma.financial.view.ManagableViewDefinitionRepository;
import com.opengamma.financial.view.memory.InMemoryViewDefinitionRepository;
import com.opengamma.financial.view.rest.ViewDefinitionsResource;
import com.opengamma.id.UniqueIdentifierTemplate;

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
   * The path used to retrieve user securities
   */
  public static final String SECURITIES_PATH = "securities";
  /**
   * The path used to retrieve user view definitions
   */
  public static final String VIEW_DEFINITIONS_PATH = "viewDefinitions";
  
  private final ClientsResource _clientsResource;
  private final ManagablePositionMaster _positionMaster;
  private final ManagableSecurityMaster _securityMaster;
  private final ManagableViewDefinitionRepository _viewDefinitionRepository;
  
  public ClientResource(ClientsResource clientsResource, String clientName) {
    _clientsResource = clientsResource;
    final String username = clientsResource.getUserResource().getUserName();
    _positionMaster = new InMemoryPositionMaster(getTemplate(username, clientName, PORTFOLIOS_PATH));
    _securityMaster = new InMemorySecurityMaster(getTemplate(username, clientName, SECURITIES_PATH));
    _viewDefinitionRepository = new InMemoryViewDefinitionRepository();
  }

  private UniqueIdentifierTemplate getTemplate(final String username, final String clientName, final String resourceType) {
    UserResourceDetails resourceDetails = new UserResourceDetails(username, clientName, PORTFOLIOS_PATH);
    return UserUniqueIdentifierUtils.getTemplate(resourceDetails);
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
  
  @Path(SECURITIES_PATH)
  public SecuritiesResource getSecurities() {
    return new SecuritiesResource(_securityMaster);
  }
  
  @Path(VIEW_DEFINITIONS_PATH)
  public ViewDefinitionsResource getViewDefinitions() {
    return new ViewDefinitionsResource(_viewDefinitionRepository);
  }
  
}
