/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.install.feedback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* package */class Feedback {

  private static final Logger s_logger = LoggerFactory.getLogger(Feedback.class);

  /* package */static final int STATE_UNKNOWN = 0;
  /* package */static final int STATE_LINKED = 1;
  /* package */static final int STATE_BROKEN = 2;

  private int _state = STATE_UNKNOWN;

  /* package */boolean tryStatus(final String str) {
    s_logger.debug("Testing native method call");
    try {
      status(str);
      s_logger.debug("Native method call ok");
      return true;
    } catch (final Throwable e) {
      s_logger.warn("Native method call not available - might be running silent/headless");
      s_logger.debug("Caught exception", e);
      return false;
    }
  }

  /* package */void setState(final int state) {
    _state = state;
  }

  /* package */int getState() {
    return _state;
  }

  private static native void status(String str);

  public void report(final String str) {
    s_logger.info("Feedback: {}", str);
    switch (getState()) {
      case STATE_UNKNOWN:
        setState(tryStatus(str) ? STATE_LINKED : STATE_BROKEN);
        break;
      case STATE_LINKED:
        status(str);
        break;
    }
  }

}
