/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;

import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.language.context.AbstractContext;

/**
 * Base class for managing a set of {@link ViewClient} instances within a context.
 */
/* package */abstract class ViewClients<Key, Ctx extends AbstractContext<?>> {

  private final ConcurrentMap<Key, UserViewClient> _clients = new ConcurrentHashMap<Key, UserViewClient>();
  private final Ctx _context;

  protected ViewClients(final Ctx context) {
    _context = context;
  }

  protected ConcurrentMap<Key, UserViewClient> getClients() {
    return _clients;
  }

  protected Ctx getContext() {
    return _context;
  }

  public abstract ViewClientHandle lockViewClient(Key key);

  protected abstract void releaseViewClient(UserViewClient client);

  protected abstract Logger getLogger();

  /**
   * Releases any remaining view clients. Do not call {@link #lockViewClient} or {@link #unlockViewClient} again. 
   */
  protected void destroyAll() {
    getLogger().info("Destroy all called");
    for (UserViewClient client : getClients().values()) {
      releaseViewClient(client);
    }
    getClients().clear();
  }

}
