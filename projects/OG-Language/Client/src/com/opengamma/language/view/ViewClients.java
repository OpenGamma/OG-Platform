/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewResultMode;
import com.opengamma.id.UniqueId;
import com.opengamma.language.context.UserContext;
import com.opengamma.util.ArgumentChecker;

/**
 * Manages a set of {@link ViewClient} within a user context. Each is uniquely identified by a {@link ViewClientKey} (or
 * my its {@link UniqueId}). 
 */
public class ViewClients {

  private final ConcurrentMap<ViewClientKey, UserViewClient> _clientsByKey = new ConcurrentHashMap<ViewClientKey, UserViewClient>();
  private final ConcurrentMap<UniqueId, UserViewClient> _clientsByUID = new ConcurrentHashMap<UniqueId, UserViewClient>();
  private final UserContext _userContext;

  public ViewClients(final UserContext userContext) {
    ArgumentChecker.notNull(userContext, "userContext");
    _userContext = userContext;
  }

  private ConcurrentMap<ViewClientKey, UserViewClient> getClientsByKey() {
    return _clientsByKey;
  }

  private ConcurrentMap<UniqueId, UserViewClient> getClientsByUID() {
    return _clientsByUID;
  }

  private UserContext getUserContext() {
    return _userContext;
  }

  protected ViewClient createViewClient() {
    final ViewClient viewClient = getUserContext().getGlobalContext().getViewProcessor().createViewClient(getUserContext().getLiveDataUser());
    viewClient.setResultMode(ViewResultMode.DELTA_ONLY);
    return viewClient;
  }

  /**
   * Gets the view client described by the given key. If such a client already exists, it is returned. If none exists, a new
   * one is created. Do not call after (or while) calling {@link #destroyAll}. The client is "locked" and each call must be balanced
   * by a call to {@link #unlockViewClient}.
   * <p>
   * For example, if the view client is passed back to the language binding, it may remain locked. When the language representation
   * is discarded the unlock must occur. If listeners are registered with the client to push data to the language binding, the
   * client must remain locked. If the "push" feed to the binding breaks (or is formally discarded), the unlock must occur.
   * <p>
   * A locked client will not be destroyed unless the whole user context is destroyed.
   * 
   * @param viewClientKey key describing the client, not null
   * @return the view client
   */
  public UserViewClient lockViewClient(final ViewClientKey viewClientKey) {
    UserViewClient client;
    do {
      client = getClientsByKey().get(viewClientKey);
      if (client == null) {
        final ViewClient viewClient = createViewClient();
        client = new UserViewClient(getUserContext(), viewClient, viewClientKey);
        final UserViewClient existing = getClientsByKey().putIfAbsent(viewClientKey, client);
        if (existing == null) {
          getClientsByUID().put(client.getUniqueId(), client);
          return client;
        }
        client = existing;
      }
    } while (!client.incrementRefCount());
    return client;
  }

  /**
   * Unlocks a view client locked by {@link #lockViewClient}. Each call to lock a client must be balanced by a call to unlock
   * it. An unlocked client will be destroyed. Do not call after (or while) calling {@link #destroyAll}.
   * 
   * @param viewClient the client to unlock, not null
   */
  public void unlockViewClient(final UserViewClient viewClient) {
    if (!viewClient.decrementRefCount()) {
      getClientsByKey().remove(viewClient.getViewClientKey());
      getClientsByUID().remove(viewClient.getUniqueId());
      viewClient.destroy();
    }
  }

  /**
   * Gets the view client identified and increments the lock count. The client must already have been locked by a call to
   * {@link #lockViewClient}. Each call to lock a client must be balanced by a call to unlock it. Do not call after (or while)
   * calling {@link #destroyAll}.
   * 
   * @param uniqueId identifier of the view client
   * @return the client or null if the client is not already locked
   */
  public UserViewClient lockViewClient(final UniqueId uniqueId) {
    UserViewClient viewClient;
    do {
      viewClient = getClientsByUID().get(uniqueId);
      if (viewClient == null) {
        return null;
      }
    } while (!viewClient.incrementRefCount());
    return viewClient;
  }

  /**
   * Destroys any remaining view clients. Do not call {@link #lockViewClient} or {@link #unlockViewClient} again. 
   */
  protected void destroyAll() {
    for (UserViewClient client : getClientsByKey().values()) {
      client.destroy();
    }
    getClientsByKey().clear();
  }

}
