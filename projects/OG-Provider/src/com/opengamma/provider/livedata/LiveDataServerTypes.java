/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.livedata;

import com.opengamma.util.PublicSPI;

/**
 * Standard types of the live data provider.
 * <p>
 * See {@link LiveDataServerType}.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicSPI
public enum LiveDataServerTypes implements LiveDataServerType {

  /**
   * The server is a standard OpenGamma live data server.
   */
  STANDARD,
  /**
   * The server is designed as a COGDA (Common OpenGamma Data Architecture) live data server.
   */
  COGDA;

}
