/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

// REVIEW kirk 2010-05-21 -- This is currently package protected as it's only used
// to control the View itself. However, it's not an inner enum as we might want to use
// it for other things that are expensive to do lifecycle on as a general
// enum, and it might want to be exposed to clients.
// I'm keeping it in limbo for now to avoid documenting it, and we'll
// see how it goes.

/**
 * Specifies the current state of a {@link View}.
 */
/*package*/ enum ViewCalculationState {
  NOT_INITIALIZED,
  INITIALIZING,
  NOT_STARTED,
  STARTING,
  RUNNING,
  TERMINATING,
  TERMINATED;
}
