/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client.merging;

import com.google.common.base.Function;
import com.opengamma.engine.view.ViewProcessListener;

/**
 * Represents a call to {@link ViewProcessListener#shutdown()}.
 */
public class ShutdownCall implements Function<ViewProcessListener, Object> {

  @Override
  public Object apply(ViewProcessListener viewProcessListener) {
    viewProcessListener.shutdown();
    return null;
  }

}
