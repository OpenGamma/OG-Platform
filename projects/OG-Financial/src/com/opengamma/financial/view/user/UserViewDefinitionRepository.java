/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.user;

import java.util.HashSet;
import java.util.Set;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.financial.user.rest.ClientResource;
import com.opengamma.financial.user.rest.UserResource;
import com.opengamma.financial.user.rest.UsersResource;
import com.opengamma.util.ArgumentChecker;

/**
 * A view definition repository which delegates to individual user and client repositories from an underlying
 * {@link UsersResource}.
 * <p>
 * This is a total hack as it assumes that a view definition name has a particular structure indicating the user and
 * client. This information should all be encoded in an identifier allocated by the server and returned to the client.
 */
public class UserViewDefinitionRepository implements ViewDefinitionRepository {
  
  private final UsersResource _underlying;
  
  public UserViewDefinitionRepository(UsersResource underlying) {
    _underlying = underlying;
  }
  
  private ViewDefinitionRepository findViewDefinitionRepository(String definitionName) {
    String[] parts = definitionName.split("/", 3);
    if (parts.length < 3) {
      return null;
    }
    
    String username = parts[0];
    String clientName = parts[1];
    
    UserResource userResource = _underlying.getUser(username);
    if (userResource == null) {
      return null;
    }
    ClientResource clientResource = userResource.getClients().getClient(clientName);
    if (clientResource == null) {
      return null;
    }
    return clientResource.getViewDefinitions().getViewDefinitionRepository();
  }

  @Override
  public ViewDefinition getDefinition(String definitionName) {
    ArgumentChecker.notNull(definitionName, "definitionName");
    ViewDefinitionRepository repository = findViewDefinitionRepository(definitionName);
    if (repository == null) {
      return null;
    }
    return repository.getDefinition(definitionName);
  }

  @Override
  public Set<String> getDefinitionNames() {
    Set<String> result = new HashSet<String>();
    for (UserResource userResource : _underlying.getAllUsers()) {
      for (ClientResource clientResource : userResource.getClients().getAllClients()) {
        result.addAll(clientResource.getViewDefinitions().getViewDefinitionRepository().getDefinitionNames());
      }
    }
    return result;
  }
  
  
}
