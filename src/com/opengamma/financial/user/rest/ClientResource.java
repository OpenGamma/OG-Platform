/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriInfo;

import org.fudgemsg.FudgeContext;

import com.opengamma.financial.livedata.rest.LiveDataResource;
import com.opengamma.financial.livedata.user.InMemoryUserSnapshotProvider;
import com.opengamma.financial.position.ManageablePositionMaster;
import com.opengamma.financial.position.memory.InMemoryManageablePositionMaster;
import com.opengamma.financial.position.rest.PortfoliosResource;
import com.opengamma.financial.security.MasterSecuritySource;
import com.opengamma.financial.security.memory.InMemorySecurityMaster;
import com.opengamma.financial.security.rest.SecurityMasterResource;
import com.opengamma.financial.user.UserResourceDetails;
import com.opengamma.financial.user.UserUniqueIdentifierUtils;
import com.opengamma.financial.view.ManageableViewDefinitionRepository;
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
  
  /**
   * The path used to retrieve user Live Data
   */
  public static final String LIVEDATA_PATH = "livedata";
  
  private final ClientsResource _clientsResource;
  private final ManageablePositionMaster _positionMaster;
  private final MasterSecuritySource _securityMaster;
  private final ManageableViewDefinitionRepository _viewDefinitionRepository;
  private final InMemoryUserSnapshotProvider _liveData;
  private final FudgeContext _fudgeContext;
  
  public ClientResource(ClientsResource clientsResource, String clientName, FudgeContext fudgeContext) {
    _clientsResource = clientsResource;
    final String username = clientsResource.getUserResource().getUserName();
    _positionMaster = new InMemoryManageablePositionMaster(getTemplate(username, clientName, PORTFOLIOS_PATH));
    _securityMaster = new MasterSecuritySource(new InMemorySecurityMaster(getTemplate(username, clientName, SECURITIES_PATH).createSupplier()));
    _liveData = new InMemoryUserSnapshotProvider(getTemplate(username, clientName, LIVEDATA_PATH));
    _viewDefinitionRepository = new InMemoryViewDefinitionRepository();
    _fudgeContext = fudgeContext;
  }

  private UniqueIdentifierTemplate getTemplate(final String username, final String clientName, final String resourceType) {
    UserResourceDetails resourceDetails = new UserResourceDetails(username, clientName, resourceType);
    return UserUniqueIdentifierUtils.getTemplate(resourceDetails);
  }
  
  public MasterSecuritySource getSecurityMaster() {
    return _securityMaster;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
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
  public SecurityMasterResource getSecurities() {
    return new SecurityMasterResource(getSecurityMaster(), getFudgeContext());
  }
  
  @Path(VIEW_DEFINITIONS_PATH)
  public ViewDefinitionsResource getViewDefinitions() {
    return new ViewDefinitionsResource(_viewDefinitionRepository);
  }
  
  @Path(LIVEDATA_PATH)
  public LiveDataResource getLiveDataResource() {
    return new LiveDataResource(_liveData);
  }
  
}
