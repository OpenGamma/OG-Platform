/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.security;

import com.opengamma.core.Attributable;
import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicAPI;

/**
 * A security that it may be possible to hold a position in.
 * <p>
 * A security generically defined as anything that a position can be held in.
 * This includes the security defined in "OTC" trades, permitting back-to-back
 * trades to be linked correctly.
 * <p>
 * This interface is read-only.
 * Implementations may be mutable.
 */
@PublicAPI
public interface Security extends UniqueIdentifiable, ExternalBundleIdentifiable, Attributable {

  /**
   * Gets the external identifier bundle that defines the security.
   * <p>
   * Each external system has one or more identifiers by which they refer to the security.
   * Some of these may be unique within that system, while others may be more descriptive.
   * This bundle stores the set of these external identifiers.
   * 
   * @return the bundle defining the security, not null
   */
  @Override  // override for Javadoc
  ExternalIdBundle getExternalIdBundle();

  /**
   * Gets the text-based type of this security.
   * <p>
   * For example, this can be used to distinguish equities, swaps and options.
   * 
   * @return the text-based type of this security, not null
   */
  String getSecurityType();

  /**
   * Gets the name of the security intended for display purposes.
   * 
   * @return the name, not null
   */
  String getName();

}
