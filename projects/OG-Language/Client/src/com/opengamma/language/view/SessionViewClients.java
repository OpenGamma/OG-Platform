/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.id.UniqueId;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.context.UserContext;

/**
 * Manages a set of {@link ViewClient} instances within a {@link SessionContext}. Each is uniquely identified by a {@link UniqueId}
 * and is also locked in the {@link UserContext} by its {@link ViewClientKey}. Typically a client that is referred to by a language
 * construct is "detached" from the user context into the session context. When the language construct releases the object, the
 * session reference is destroyed and the original user level lock released, possibly allowing the client to be destroyed.
 */
public class SessionViewClients extends ViewClients<UniqueId, SessionContext> {

  private static final Logger s_logger = LoggerFactory.getLogger(SessionViewClients.class);

  private final Map<UniqueId, AtomicInteger> _detachCount = new HashMap<UniqueId, AtomicInteger>();

  public SessionViewClients(final SessionContext context) {
    super(context);
  }

  /**
   * Gets the view client identified and increments the lock count. The client must already have been locked by a call to
   * {@link UserViewClients#lockViewClient}. Each call to lock a client must be balanced by a call to unlock it. Do not call after
   * (or while) calling {@link UserViewClients#destroyAll} or {@link SessionViewClients#destroyAll}.
   * 
   * @param uniqueId identifier of the view client
   * @return the client or null if the client is not already locked
   */
  @Override
  public DetachedViewClientHandle lockViewClient(final UniqueId uniqueId) {
    if (uniqueId == null) {
      return null;
    }
    UserViewClient client;
    do {
      client = getClients().get(uniqueId);
      if (client == null) {
        return null;
      }
    } while (!client.incrementRefCount());
    return new DetachedViewClientHandle(this, client);
  }

  /**
   * Releases the detached reference, unlocking it in the user context. The client can no longer be referenced from the session context.
   * 
   * @param viewClient the client to unlock, not null
   */
  @Override
  protected void releaseViewClient(final UserViewClient viewClient) {
    s_logger.debug("Releasing view client {}", viewClient);
    synchronized (this) {
      if ((_detachCount.remove(viewClient.getUniqueId()) == null) || (getClients().remove(viewClient.getUniqueId()) == null)) {
        // This shouldn't happen
        throw new IllegalStateException();
      }
    }
    releaseViewClientImpl(viewClient);
  }

  private void releaseViewClientImpl(final UserViewClient viewClient) {
    if (!viewClient.decrementRefCount()) {
      s_logger.debug("Last reference on {} released", viewClient);
      getContext().getUserContext().getViewClients().releaseViewClient(viewClient);
    } else {
      s_logger.debug("Outstanding references on {}", viewClient);
    }
  }

  protected synchronized void beginDetach(final UserViewClient viewClient) {
    AtomicInteger detachCount = _detachCount.get(viewClient.getUniqueId());
    if (detachCount == null) {
      s_logger.debug("First detach {}", viewClient);
      detachCount = new AtomicInteger(1);
      _detachCount.put(viewClient.getUniqueId(), detachCount);
      if (!viewClient.incrementRefCount()) {
        // This shouldn't happen
        throw new IllegalStateException();
      }
      getClients().put(viewClient.getUniqueId(), viewClient);
    } else {
      s_logger.debug("Duplicate detach {} ({} previous times)", viewClient, detachCount);
      detachCount.incrementAndGet();
      assert getClients().get(viewClient.getUniqueId()) == viewClient;
    }
  }

  protected void endDetach(final UserViewClient viewClient) {
    s_logger.debug("End detach {}", viewClient);
    synchronized (this) {
      AtomicInteger detachCount = _detachCount.get(viewClient.getUniqueId());
      if (detachCount == null) {
        // This happens if the context was destroyed
        return;
      }
      if (detachCount.decrementAndGet() > 0) {
        // Still detached
        s_logger.debug("{} outstanding detaches on {}", detachCount, viewClient);
        return;
      }
      // Last detach has happened; release it
      _detachCount.remove(viewClient.getUniqueId());
      getClients().remove(viewClient.getUniqueId());
    }
    releaseViewClientImpl(viewClient);
  }

  /**
   * Returns the currently detached clients. Note that the clients are not locked by this operation, so may not be valid if they
   * have been unlocked by other threads.
   * 
   * @return the detached clients
   */
  public Collection<UserViewClient> getDetachedClients() {
    return Collections.unmodifiableCollection(getClients().values());
  }

  @Override
  protected Logger getLogger() {
    return s_logger;
  }

}
