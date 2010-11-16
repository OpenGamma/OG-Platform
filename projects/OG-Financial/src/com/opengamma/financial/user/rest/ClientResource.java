/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriInfo;

import org.fudgemsg.FudgeContext;

import com.opengamma.financial.position.master.rest.DataPortfolioTreesResource;
import com.opengamma.financial.position.master.rest.DataPositionsResource;
import com.opengamma.financial.security.rest.SecurityMasterResource;
import com.opengamma.financial.user.UserResourceDetails;
import com.opengamma.financial.user.UserUniqueIdentifierUtils;
import com.opengamma.financial.view.ManageableViewDefinitionRepository;
import com.opengamma.financial.view.memory.InMemoryViewDefinitionRepository;
import com.opengamma.financial.view.rest.ViewDefinitionsResource;
import com.opengamma.id.UniqueIdentifierTemplate;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.InMemorySecurityMaster;

/**
 * Temporary RESTful resource representing a user's client session.
 */
@Path("/data/users/{username}/clients/{clientUid}")
public class ClientResource {
  
  /**
   * The path used to retrieve user portfolios
   */
  public static final String PORTFOLIOS_PATH = "portfoliotrees";
  /**
   * The path used to retrieve user positions
   */
  public static final String POSITIONS_PATH = "positions";
  /**
   * The path used to retrieve user securities
   */
  public static final String SECURITIES_PATH = "securities";
  /**
   * The path used to retrieve user view definitions
   */
  public static final String VIEW_DEFINITIONS_PATH = "viewDefinitions";
  
  private final ClientsResource _clientsResource;
  private final SecurityMaster _securityMaster;
  private final ManageableViewDefinitionRepository _viewDefinitionRepository;
  private final UsersResourceContext _usersResourceContext;
  
  public ClientResource(ClientsResource clientsResource, String clientName, UsersResourceContext context) {
    _clientsResource = clientsResource;
    String username = clientsResource.getUserResource().getUserName();
    _securityMaster = new InMemorySecurityMaster(getTemplate(username, clientName, SECURITIES_PATH).createSupplier());
    _usersResourceContext = context;
    // [FIN-124] The user SecuritySource is done wrongly throughout
    _viewDefinitionRepository = new InMemoryViewDefinitionRepository();
  }

  private UniqueIdentifierTemplate getTemplate(final String username, final String clientName, final String resourceType) {
    UserResourceDetails resourceDetails = new UserResourceDetails(username, clientName, resourceType);
    return UserUniqueIdentifierUtils.getTemplate(resourceDetails);
  }
  
  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  public FudgeContext getFudgeContext() {
    return _usersResourceContext.getFudgeContext();
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
  public DataPortfolioTreesResource getPortfolios() {
    return new DataPortfolioTreesResource(_usersResourceContext.getPositionMaster());
  }
  
  @Path(POSITIONS_PATH)
  public DataPositionsResource getPositions() {
    return new DataPositionsResource(_usersResourceContext.getPositionMaster());
  }
  
  @Path(SECURITIES_PATH)
  public SecurityMasterResource getSecurities() {
    return new SecurityMasterResource(getSecurityMaster(), getFudgeContext());
  }
  
  @Path(VIEW_DEFINITIONS_PATH)
  public ViewDefinitionsResource getViewDefinitions() {
    return new ViewDefinitionsResource(_viewDefinitionRepository);
  }
  
}
