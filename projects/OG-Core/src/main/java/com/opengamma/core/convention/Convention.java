/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.convention;

import com.opengamma.core.Attributable;
import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicAPI;

/**
 * A convention that provides common or shared information about a financial product.
 * <p>
 * A convention is used to capture information that is common in a market.
 * For example, they are used in curve and security construction.
 * <p>
 * This interface is read-only.
 * Implementations may be mutable.
 */
@PublicAPI
public interface Convention extends UniqueIdentifiable, ExternalBundleIdentifiable, Attributable {

  /**
   * Gets the external identifier bundle that defines the convention.
   * <p>
   * Each external system has one or more identifiers by which they refer to the convention.
   * Some of these may be unique within that system, while others may be more descriptive.
   * This bundle stores the set of these external identifiers.
   * 
   * @return the bundle defining the convention, not null
   */
  @Override  // override for Javadoc
  ExternalIdBundle getExternalIdBundle();

  /**
   * Gets the convention type.
   * 
   * @return the convention type, not null
   */
  ConventionType getConventionType();

  /**
   * Gets the name of the security intended for display purposes.
   * 
   * @return the name, not null
   */
  String getName();

}
