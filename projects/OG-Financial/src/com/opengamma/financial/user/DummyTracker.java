/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.UniqueIdentifier;

/**
 * Ignores the user data events.
 */
public class DummyTracker implements UserDataTracker, ClientTracker {

  private static final Logger s_logger = LoggerFactory.getLogger(DummyTracker.class);

  private final ConcurrentMap<String, Set<String>> _valid = new ConcurrentHashMap<String, Set<String>>();

  @Override
  public void created(String userName, String clientName, UserDataType type, UniqueIdentifier identifier) {
    Set<String> clients = _valid.get(userName);
    if (clients != null) {
      if (clients.contains(clientName)) {
        s_logger.debug("{} created by {}", identifier, userName);
        return;
      }
    }
    s_logger.debug("Late creation of {} by {}", identifier, userName);
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
  }

}
