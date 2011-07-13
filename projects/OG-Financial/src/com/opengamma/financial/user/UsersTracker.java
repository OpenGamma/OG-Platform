/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.user.rest.UsersResourceContext;
import com.opengamma.financial.view.ManageableViewDefinitionRepository;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * Tracks userData and clients
 */
public class UsersTracker implements UserDataTracker, ClientTracker {

  private static final Logger s_logger = LoggerFactory.getLogger(UsersTracker.class);

  private final ConcurrentMap<String, Set<String>> _valid = new ConcurrentHashMap<String, Set<String>>();
  private final ConcurrentMap<String, Set<String>> _userName2viewDefinitionNames = new ConcurrentHashMap<String, Set<String>>();
  private final UsersResourceContext _context;
 
  public UsersTracker(UsersResourceContext context) {
    ArgumentChecker.notNull(context, "context");
    _context = context;
  }

  /**
   * Gets the context.
   * @return the context
   */
  public UsersResourceContext getContext() {
    return _context;
  }

  @Override
  public void created(String userName, String clientName, UserDataType type, UniqueIdentifier identifier) {
    s_logger.debug("{} created by {} with id {}", new Object[] {type, userName, identifier});
    Set<String> clients = _valid.get(userName);
    if (clients != null) {
      if (clients.contains(clientName)) {
        s_logger.debug("{} created by {}", identifier, userName);
      }
    } else {
      s_logger.debug("Late creation of {} by {}", identifier, userName);
    }
    if (type == UserDataType.VIEW_DEFINITION) {
      Set<String> viewDefinitions = new ConcurrentSkipListSet<String>();
      _userName2viewDefinitionNames.putIfAbsent(userName, viewDefinitions);
      viewDefinitions.add(identifier.getValue());
      s_logger.debug("User {} created {} view definition", userName, identifier.getValue());
    }
  }

  @Override
  public void deleted(String userName, String clientName, UserDataType type, UniqueIdentifier identifier) {
    Set<String> clients = _valid.get(userName);
    if (clients != null) {
      if (clients.contains(clientName)) {
        s_logger.debug("{} deleted by {}", identifier, userName);
        return;
      }
    }
    s_logger.debug("Late deletion of {} by {}", identifier, userName);
  }

  @Override
  public void clientCreated(String userName, String clientName) {
    Set<String> clients = _valid.get(userName);
    if (clients == null) {
      s_logger.debug("Late client construction for discarded user {}", userName);
      return;
    }
    clients.add(clientName);
    s_logger.debug("Client {} created for user {}", clientName, userName);
  }

  @Override
  public void clientDiscarded(String userName, String clientName) {
    Set<String> clients = _valid.get(userName);
    if (clients == null) {
      s_logger.debug("Late client discard for discarded user {}", userName);
      return;
    }
    clients.remove(clientName);
    s_logger.debug("Client {} discarded for user {}", clientName, userName);
  }

  @Override
  public void userCreated(String userName) {
    Set<String> clients = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    _valid.putIfAbsent(userName, clients);
    s_logger.debug("User {} created", userName);
  }

  @Override
  public void userDiscarded(String userName) {
    _valid.remove(userName);
    s_logger.debug("User {} discarded", userName);
    removeUserViewDefinitions(userName);
  }

  private void removeUserViewDefinitions(String userName) {
    Set<String> viewDefinitions = _userName2viewDefinitionNames.get(userName);
    if (getContext() != null) {
      ManageableViewDefinitionRepository viewDefinitionRepository = getContext().getViewDefinitionRepository();
      if (viewDefinitionRepository != null) {
        for (String viewDefinitionName : viewDefinitions) {
          viewDefinitionRepository.removeViewDefinition(viewDefinitionName);
          s_logger.debug("View definition {} removed", viewDefinitionName);
        }
      }
    }
  }

}
