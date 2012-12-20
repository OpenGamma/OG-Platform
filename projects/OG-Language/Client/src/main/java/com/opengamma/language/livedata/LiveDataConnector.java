/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.livedata;

import java.util.List;

import com.opengamma.language.Data;
import com.opengamma.language.context.SessionContext;

/**
 * Establishes a connection to a live data component for a client.
 */
public interface LiveDataConnector {

  /**
   * Establishes a connection, returning a connection handle that can be used
   * to receive values from the live data and terminate it on completion.
   * 
   * @param context the caller's session context
   * @param parameters the connection parameters
   * @return the connection handle
   */
  Connection connect(SessionContext context, List<Data> parameters);

}
