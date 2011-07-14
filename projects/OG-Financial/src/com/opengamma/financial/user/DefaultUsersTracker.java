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
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * Tracks userData and clients
 */
public class DefaultUsersTracker implements UserDataTracker, ClientTracker {

  private static final Logger s_logger = LoggerFactory.getLogger(DefaultUsersTracker.class);

  private final ConcurrentMap<String, Set<String>> _username2clients = new ConcurrentHashMap<String, Set<String>>();
  private final ConcurrentMap<Identifier, Set<String>> _viewDefinitionNames = new ConcurrentHashMap<Identifier, Set<String>>();
  private final UsersResourceContext _context;
 
  public DefaultUsersTracker(UsersResourceContext context) {
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
    Set<String> clients = _username2clients.get(userName);
    if (clients != null) {
      if (clients.contains(clientName)) {
        s_logger.debug("{} created by {}", identifier, userName);
        if (type == UserDataType.VIEW_DEFINITION) {
          trackCreatedViewDefinition(userName, clientName, identifier);
        }
        return;
      }
    } 
    s_logger.debug("Late creation of {} by {}", identifier, userName);
  }

  private void trackCreatedViewDefinition(String userName, String clientName, UniqueIdentifier identifier) {
    ConcurrentSkipListSet<String> freshDefinitions = new ConcurrentSkipListSet<String>();
    Set<String> viewDefinitions = _viewDefinitionNames.putIfAbsent(Identifier.of(userName, clientName), freshDefinitions);
    if (viewDefinitions == null) {
      viewDefinitions = freshDefinitions;
    }
    viewDefinitions.add(identifier.getValue());
    s_logger.debug("{} created by {}", identifier, userName);
  }

  @Override
  public void deleted(String userName, String clientName, UserDataType type, UniqueIdentifier identifier) {
    Set<String> clients = _username2clients.get(userName);
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
    Set<String> clients = _username2clients.get(userName);
    if (clients == null) {
      s_logger.debug("Late client construction for discarded user {}", userName);
      return;
    }
    clients.add(clientName);
    s_logger.debug("Client {} created for user {}", clientName, userName);
  }

  @Override
  public void clientDiscarded(String userName, String clientName) {
    Set<String> clients = _username2clients.get(userName);
    if (clients == null) {
      s_logger.debug("Late client discard for discarded user {}", userName);
      return;
    }
    clients.remove(clientName);
    s_logger.debug("Client {} discarded for user {}", clientName, userName);
    removeUserViewDefinitions(userName, clientName);
  }

  @Override
  public void userCreated(String userName) {
    Set<String> clients = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    _username2clients.putIfAbsent(userName, clients);
    s_logger.debug("User {} created", userName);
  }

  @Override
  public void userDiscarded(String userName) {
    Set<String> removedClients = _username2clients.remove(userName);
    s_logger.debug("User {} discarded", userName);
    if (removedClients != null) {
      for (String clientName : removedClients) {
        removeUserViewDefinitions(userName, clientName);
      }
    }
  }

  private void removeUserViewDefinitions(final String userName, final String clientName) {
    Set<String> viewDefinitions = _viewDefinitionNames.get(Identifier.of(userName, clientName));
    if (getContext() != null) {
      ManageableViewDefinitionRepository viewDefinitionRepository = getContext().getViewDefinitionRepository();
      if (viewDefinitionRepository != null) {
        for (String viewDefinitionName : viewDefinitions) {
          viewDefinitionRepository.removeViewDefinition(viewDefinitionName);
          s_logger.debug("View definition {} discarded for {}/{}", new Object[] {viewDefinitionName, userName, clientName});
        }
      }
    }
  }

}
