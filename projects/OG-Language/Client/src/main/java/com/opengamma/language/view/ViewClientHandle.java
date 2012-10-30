/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.UniqueId;
import com.opengamma.language.context.SessionContext;

/**
 * A locked handle encapsulating a view client instance. The handle should be unlocked when it is finished with, although failure
 * to do so will unlock the handle when it is garbage collected.
 * <p>
 * This is not suitable for concurrent use by multiple threads.
 */
public abstract class ViewClientHandle {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewClientHandle.class);

  private final UserViewClient _viewClient;
  private ViewClients<?, ?> _viewClients;

  /* package */ViewClientHandle(final ViewClients<?, ?> viewClients, final UserViewClient viewClient) {
    _viewClients = viewClients;
    _viewClient = viewClient;
  }

  protected ViewClients<?, ?> getViewClients() {
    return _viewClients;
  }

  protected ViewClients<?, ?> unlockAction() {
    final ViewClients<?, ?> viewClients = _viewClients;
    _viewClients = null;
    return viewClients;
  }

  protected UserViewClient getViewClient() {
    return _viewClient;
  }

  protected void finalize() throws Throwable {
    try {
      if (unlockImpl()) {
        s_logger.debug("{} unlocked by finalizer", _viewClient);
      }
    } finally {
      super.finalize();
    }
  }

  /**
   * Returns the view client represented by this handle.
   * 
   * @return the view client
   */
  public UserViewClient get() {
    if (_viewClients == null) {
      throw new IllegalStateException();
    }
    return getViewClient();
  }

  private boolean unlockImpl() {
    final ViewClients<?, ?> viewClients = unlockAction();
    if (viewClients != null) {
      final UserViewClient viewClient = getViewClient();
      s_logger.debug("Unlocking {}", viewClient);
      if (!viewClient.decrementRefCount()) {
        viewClients.releaseViewClient(viewClient);
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * Unlocks the handle. After unlocking, the value is no longer valid.
   */
  public void unlock() {
    if (unlockImpl()) {
      s_logger.debug("{} unlocked", getViewClient());
    } else {
      s_logger.error("Handle on {} already unlocked", getViewClient());
      throw new IllegalStateException();
    }
  }

  /**
   * Detaches the object from the handle into the {@link SessionContext}. The object remains "reachable" from the session context
   * by its {@link UniqueId}.
   * 
   * @param target the session context to detach the handle into
   * @return the unique id to reference the detached object by
   */
  protected UniqueId detachAndUnlock(final SessionContext target) {
    if (unlockAction() != null) {
      detach(target);
      if (!getViewClient().decrementRefCount()) {
        // This shouldn't happen (the detach operation should have incremented the count)
        throw new IllegalStateException();
      }
      return getViewClient().getUniqueId();
    } else {
      s_logger.error("Handle on {} already unlocked", getViewClient());
      throw new IllegalStateException();
    }
  }

  protected void detach(final SessionContext target) {
    final UserViewClient viewClient = getViewClient();
    s_logger.debug("Detaching {}", viewClient);
    target.getViewClients().beginDetach(viewClient);
  }

}
