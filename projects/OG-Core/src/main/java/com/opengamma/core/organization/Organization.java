/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.organization;

import com.opengamma.core.obligor.definition.Obligor;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicAPI;

/**
 * An organization that may represent amongst other, an Obligor.
 * <p>
 * This interface is read-only.
 * Implementations may be mutable.
 */
@PublicAPI
public interface Organization extends UniqueIdentifiable {

  /**
   * Get the obligor that this Organization represents. Note that this should
   * be refactored such that the obligor fields are on the Organization itself.
   *
   * @return the obligor
   */
  Obligor getObligor();
}
