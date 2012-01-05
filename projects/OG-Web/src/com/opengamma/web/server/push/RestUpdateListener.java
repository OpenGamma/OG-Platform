/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import java.util.Collection;

/**
 * Listener that receives notifications of changes to data that was requested over the REST interface.
 */
/* package */ interface RestUpdateListener {

  /**
   * Invoked when something that was requested via REST has been updated.
   * @param url The RESTful URL of the updated data
   */
  void itemUpdated(String url);

  /**
   * Invoked when objects requested via REST have been updated.
   * @param url The RESTful URLs of the updated data
   */
  void itemsUpdated(Collection<String> url);
}
