/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.livedata;

import com.opengamma.util.PublicSPI;

/**
 * The type of the live data provider.
 * <p>
 * See {@link LiveDataServerTypes}.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicSPI
public interface LiveDataServerType {

  /**
   * Gets the name of the live data provider type.
   * 
   * @return the name, not null
   */
  String name();

}
