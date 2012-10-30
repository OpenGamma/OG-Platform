/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

/**
 * Lifetime events on a session context
 */
public interface SessionContextDestructionEventHandler {

  void doneContext(MutableSessionContext context);

}
