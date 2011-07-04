/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

/**
 * Deferred source of a {@link ResolvedValue}.
 */
/* package */interface ResolvedValueProducer {

  /**
   * Register a callback for notification when the value is produced. If the value has already
   * been produced it may be called immediately.
   * 
   * @param callback callback object to receive the notifications, not {@code null}
   */
  void addCallback(ResolvedValueCallback callback);

}
