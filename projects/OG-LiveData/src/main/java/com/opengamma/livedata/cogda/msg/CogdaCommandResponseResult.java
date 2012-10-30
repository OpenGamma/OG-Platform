/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.cogda.msg;

import com.opengamma.livedata.msg.LiveDataSubscriptionResult;

/**
 * The generic nature of a response to a {@link CogdaCommandMessage}.
 */
public enum CogdaCommandResponseResult {
  /** The request was successful. */
  SUCCESSFUL,
  /** The client was not authorized for that request. */
  NOT_AUTHORIZED,
  /**
   * The client asked for something which the server can't process, but
   * was otherwise well-formed.
   */
  NOT_AVAILABLE,
  /** The server encountered an internal error in processing the request. */
  INTERNAL_ERROR;

  public LiveDataSubscriptionResult toLiveDataSubscriptionResult() {
    LiveDataSubscriptionResult ldsResult = null;
    switch (this) {
      case INTERNAL_ERROR:
        ldsResult = LiveDataSubscriptionResult.INTERNAL_ERROR;
        break;
      case NOT_AUTHORIZED:
        ldsResult = LiveDataSubscriptionResult.NOT_AUTHORIZED;
        break;
      case NOT_AVAILABLE:
        ldsResult = LiveDataSubscriptionResult.NOT_PRESENT;
        break;
      case SUCCESSFUL:
        ldsResult = LiveDataSubscriptionResult.SUCCESS;
        break;
    }

    return ldsResult;
  }
}
