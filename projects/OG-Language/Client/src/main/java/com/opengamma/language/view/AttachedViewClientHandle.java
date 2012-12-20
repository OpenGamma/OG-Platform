/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import com.opengamma.id.UniqueId;
import com.opengamma.language.context.SessionContext;

/**
 * A locked handle encapsulating a view client instance that is attached to the current context. Unlocking it may cause destruction
 * of the client.
 * <p>
 * This is not suitable for concurrent use by multiple threads.
 */
public final class AttachedViewClientHandle extends ViewClientHandle {

  protected AttachedViewClientHandle(final UserViewClients viewClients, final UserViewClient viewClient) {
    super(viewClients, viewClient);
  }

  @Override
  public UniqueId detachAndUnlock(final SessionContext target) {
    return super.detachAndUnlock(target);
  }

}
