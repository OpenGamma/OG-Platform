/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.threeten.bp.Instant;

/**
 * A manager of clients for each user that provide access to underlying services.
 * <p>
 * This class is a placeholder until full user management is added.
 */
public class FinancialClientManager {

  /**
   * The user.
   */
  private final FinancialUser _user;
  /**
   * The map of clients.
   */
  private final ConcurrentHashMap<String, FinancialClient> _clientMap = new ConcurrentHashMap<String, FinancialClient>();

  /**
   * Creates an instance.
   * 
   * @param user  the user, not null
   */
  public FinancialClientManager(FinancialUser user) {
    _user = user;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the user.
   * 
   * @return the user, not null
   */
  public FinancialUser getUser() {
    return _user;
  }

  /**
   * Gets the tracker.
   * 
   * @return the tracker, not null
   */
  public FinancialClientTracker getClientTracker() {
    return _user.getUserManager().getClientTracker();
  }

  /**
   * Gets the tracker.
   * 
   * @return the tracker, not null
   */
  public FinancialUserDataTracker getUserDataTracker() {
    return _user.getUserManager().getUserDataTracker();
  }

  /**
   * Gets the user name.
   * 
   * @return the user name, not null
   */
  public String getUserName() {
    return _user.getUserName();
  }

  /**
   * Gets the services.
   * 
   * @return the services, not null
   */
  public FinancialUserServices getServices() {
    return _user.getUserManager().getServices();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a client.
   * 
   * @param clientName  the client name, not null
   * @return the client, null if not found
   */
  public FinancialClient getClient(String clientName) {
    return _clientMap.get(clientName);
  }

  /**
   * Gets a client, creating if it does not exist.
   * 
   * @param clientName  the client name, not null
   * @return the client, not null
   */
  public FinancialClient getOrCreateClient(String clientName) {
    FinancialClient client = _clientMap.get(clientName);
    if (client == null) {
      getClientTracker().clientCreated(getUserName(), clientName);
      FinancialClient freshClient = new FinancialClient(this, clientName);
      client = _clientMap.putIfAbsent(clientName, freshClient);
      if (client == null) {
        client = freshClient;
      }
    }
    return client;
  }

  //-------------------------------------------------------------------------
  /**
   * Discards any clients that haven't been accessed since the given timestamp. The local
   * last accessed time is set to the timestamp of the most recently accessed client.
   * 
   * @param timestamp  any client resources with a last accessed time before this will be removed, not null
   * @return the number of active clients
   */
  public int deleteClients(final Instant timestamp) {
    final Iterator<Map.Entry<String, FinancialClient>> clientIterator = _clientMap.entrySet().iterator();
    int activeClients = 0;
    while (clientIterator.hasNext()) {
      final Map.Entry<String, FinancialClient> clientEntry = clientIterator.next();
      final Instant clientTime = clientEntry.getValue().getLastAccessed();
      if (clientTime.isBefore(timestamp)) {
        clientIterator.remove();
        getClientTracker().clientDiscarded(getUserName(), clientEntry.getKey());
      } else {
        activeClients++;
      }
    }
    return activeClients;
  }

}
