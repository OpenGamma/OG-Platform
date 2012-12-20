/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;


/**
 * Lifetime events on a user context
 */
public interface UserContextEventHandler {

  void initContext(MutableUserContext context);

  void doneContext(MutableUserContext context);

}
