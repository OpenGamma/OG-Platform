/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.opengamma.financial.user.ClientTracker;
import com.opengamma.financial.user.UserDataTracker;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource representing a collection of a user's clients. If any client is requested then it will
 * be created and persist forever.
 */
public class ClientsResource {

  private final String _userName;
  private final ClientTracker _clientTracker;
  private final UserDataTracker _userDataTracker;
  private final ConcurrentHashMap<String, ClientResource> _clientMap = new ConcurrentHashMap<String, ClientResource>();
  private final UsersResourceContext _context;
  
  private long _lastAccessed;

  public ClientsResource(final String userName, final ClientTracker clientTracker, final UserDataTracker userDataTracker, final UsersResourceContext context) {
    ArgumentChecker.notNull(clientTracker, "clientTracker");
    _userName = userName;
    _clientTracker = clientTracker;
    _userDataTracker = userDataTracker;
    _context = context;
  }
  
//  public UserResource getUserResource() {
//    return _userResource;
//  }
//  
//  /**
//   * Gets the URI info.
//   * @return the uri info, not null
//   */
//  public UriInfo getUriInfo() {
//    return getUserResource().getUriInfo();
//  }
  
  @Path("{clientUid}")
  public ClientResource getClient(@PathParam("clientUid") String clientName) {
    ClientResource client = _clientMap.get(clientName);
    if (client == null) {
      _clientTracker.clientCreated(_userName, clientName);
      ClientResource freshClient = new ClientResource(_userName, clientName, _userDataTracker, _context);
      client = _clientMap.putIfAbsent(clientName, freshClient);
      if (client == null) {
        client = freshClient;
      }
    }
    return client;
  }

  /**
   * Discards any clients that haven't been accessed since the given timestamp. The local
   * last accessed time is set to the timestamp of the most recently accessed client.
   * 
   * @param timestamp any client resources with a last accessed time before this will be removed
   */
  public void deleteClients(final long timestamp) {
    final Iterator<Map.Entry<String, ClientResource>> clientIterator = _clientMap.entrySet().iterator();
    long lastAccessed = 0;
    while (clientIterator.hasNext()) {
      final Map.Entry<String, ClientResource> clientEntry = clientIterator.next();
      final long clientTime = clientEntry.getValue().getLastAccessed();
      if (clientTime < timestamp) {
        clientIterator.remove();
        _clientTracker.clientDiscarded(_userName, clientEntry.getKey());
      } else if (clientTime > lastAccessed) {
        lastAccessed = clientTime;
      }
    }
    _lastAccessed = lastAccessed;
  }

  public long getLastAccessed() {
    return _lastAccessed;
  }

}
