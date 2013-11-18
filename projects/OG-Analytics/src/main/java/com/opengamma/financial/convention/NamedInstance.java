/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

/**
 * Interface for factories that can create instances from names.
 */
public interface NamedInstance {

  /**
   * Gets the name of the instance.
   * 
   * @return the name of this instance, not null
   */
  String getName();

}
