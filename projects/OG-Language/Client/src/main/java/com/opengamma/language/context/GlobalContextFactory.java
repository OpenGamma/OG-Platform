/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

/**
 * A factory for providing the global contexts.
 */
public interface GlobalContextFactory {

  /**
   * Returns an initialized {@link GlobalContext} object, creating one if it hasn't already been created.
   * 
   * @return the initialized context
   */
  GlobalContext getOrCreateGlobalContext();

}
