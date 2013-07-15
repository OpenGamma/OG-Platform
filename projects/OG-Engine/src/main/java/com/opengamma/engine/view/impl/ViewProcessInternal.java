/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.impl;

import com.opengamma.engine.view.ViewProcess;

/**
 * Exposes engine-level access to a view process.
 */
public interface ViewProcessInternal extends ViewProcess {
  
  /**
   * Suspends all operations on the view process, blocking until everything is in a suspendable state. While suspended,
   * any operations which would alter the state of the view process will block until {@link #resume} is called.
   */
  void suspend();

  /**
   * Resumes operations on the view process, following a call to {@link #suspend}.
   */
  void resume();
  
}
