/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import com.opengamma.util.PublicAPI;

/**
 * Provides uniform access to {@code ExternalId} and a {@code ExternalIdBundle}.
 * <p>
 * This differs from {@link ExternalBundleIdentifiable} because this is only implemented
 * by the two identifier classes themselves.
 * <p>
 * This interface makes no guarantees about the thread-safety of implementations.
 * However, wherever possible calls to this method should be thread-safe.
 */
@PublicAPI
public interface ExternalIdOrBundle {

  /**
   * Gets the external identifier bundle that defines the object.
   * <p>
   * Each external system has one or more identifiers by which they refer to the object.
   * Some of these may be unique within that system, while others may be more descriptive.
   * This bundle stores the set of these external identifiers.
   * 
   * @return the bundle defining the object, not null
   */
  ExternalIdBundle toBundle();

}
