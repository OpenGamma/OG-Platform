/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;


/**
 * Lifetime events on a global context.
 */
public interface GlobalContextEventHandler {

  void initContext(MutableGlobalContext context);
  
}
