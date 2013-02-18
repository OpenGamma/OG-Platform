/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewResultMode;
import com.opengamma.id.UniqueId;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.context.UserContext;

/**
 * Manages a set of {@link ViewClient} within a {@link UserContext}. Each is uniquely identified by a {@link ViewClientKey}. When
 * clients are referred to externally within a session, the reference can be detached into the {@link SessionContext} and be
 * retrieved by its {@link UniqueId}.
 */
public class UserViewClients extends ViewClients<ViewClientKey, UserContext> {

  private static final Logger s_logger = LoggerFactory.getLogger(UserViewClients.class);

  public UserViewClients(final UserContext userContext) {
    super(userContext);
  }

  protected ViewClient createViewClient() {
    final ViewClient viewClient = getContext().getGlobalContext().getViewProcessor().createViewClient(getContext().getLiveDataUser());
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
  @Override
  public AttachedViewClientHandle lockViewClient(final ViewClientKey viewClientKey) {
    UserViewClient client;
    do {
      client = getClients().get(viewClientKey);
      if (client == null) {
        // TODO: check the graveyard for a matching view client instead of creating a new one
        final ViewClient viewClient = createViewClient();
        try {
          client = new UserViewClient(getContext(), viewClient, viewClientKey);
          final UserViewClient existing = getClients().putIfAbsent(viewClientKey, client);
          if (existing == null) {
            return new AttachedViewClientHandle(this, client);
          }
          client.destroy();
          client = existing;
        } catch (final Exception e) {
          viewClient.shutdown();
          throw new OpenGammaRuntimeException("Error initialising new view client", e);
        }
      }
    } while (!client.incrementRefCount());
    return new AttachedViewClientHandle(this, client);
  }

  /**
   * Releases a client that is no longer locked (i.e. has a zero reference count).
   * 
   * @param viewClient the client to unlock, not null
   */
  @Override
  protected void releaseViewClient(final UserViewClient viewClient) {
    assert !viewClient.isLocked();
    // TODO: post the old view client to a "graveyard" queue
    getClients().remove(viewClient.getViewClientKey());
    // TODO: only destroy immediately if the client is older than a given age
    // TODO: remove the client from the graveyard (only destroy if still in the graveyard)
    viewClient.destroy();
  }

  @Override
  protected void destroyAll() {
    super.destroyAll();
    // TODO: destroy anything in the graveyard
  }

  @Override
  protected Logger getLogger() {
    return s_logger;
  }

}
