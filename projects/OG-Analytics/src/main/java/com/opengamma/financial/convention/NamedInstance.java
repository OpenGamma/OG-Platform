/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

/**
 * A named instance is a type where each instance is uniquely identified by a name.
 * 
 * @see NamedInstanceFactory
 */
public interface NamedInstance {

  /**
   * Gets the name of the instance.
   * 
   * @return the name of this instance, not null
   */
  String getName();

}
