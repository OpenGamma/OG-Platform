/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.yield;

import org.joda.convert.FromStringFactory;
import org.joda.convert.ToString;

import com.opengamma.financial.convention.NamedInstance;

/**
 * Convention for yields.
 */
@FromStringFactory(factory = YieldConventionFactory.class)
public interface YieldConvention extends NamedInstance {
  // TODO: supply whatever is needed to do a proper calculation

  /**
   * Gets the name of the convention.
   * 
   * @return the name, not null
   * @deprecated use getName()
   */
  @Deprecated
  String getConventionName();

  /**
   * Gets the name of the convention.
   * 
   * @return the name, not null
   */
  @ToString
  String getName();

}
