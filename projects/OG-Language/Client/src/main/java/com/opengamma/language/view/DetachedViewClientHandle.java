/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.UniqueId;

/**
 * A locked handle encapsulating a view client instance that is detached from the current context. To be a candidate for
 * destruction, the client must be re-attached to the user context to release the original lock.
 * <p>
 * This is not suitable for concurrent use by multiple threads.
 */
public final class DetachedViewClientHandle extends ViewClientHandle {

  private static final Logger s_logger = LoggerFactory.getLogger(DetachedViewClientHandle.class);

  protected DetachedViewClientHandle(final SessionViewClients viewClients, final UserViewClient viewClient) {
    super(viewClients, viewClient);
  }

  /**
   * Unlocks this handle, and re-attaches the reference to the user context. If there are no other detached references or locks
   * on the client, it may then be destroyed.
   */
  public void attachAndUnlock() {
    final SessionViewClients viewClients = (SessionViewClients) unlockAction();
    if (viewClients != null) {
      final UserViewClient viewClient = getViewClient();
      s_logger.debug("Attaching and unlocking {}", viewClient);
      if (!viewClient.decrementRefCount()) {
        // Can only happen if the session context has been closed
        s_logger.debug("Last reference on {} released", viewClient);
        viewClient.getUserContext().getViewClients().releaseViewClient(viewClient);
      } else {
        viewClients.endDetach(viewClient);
      }
    } else {
      s_logger.error("Handle on {} already unlocked", getViewClient());
      throw new IllegalStateException();
    }
  }

  public UniqueId detachAndUnlock() {
    return super.detachAndUnlock(((SessionViewClients) getViewClients()).getContext());
  }

  public void detach() {
    super.detach(((SessionViewClients) getViewClients()).getContext());
  }

}
