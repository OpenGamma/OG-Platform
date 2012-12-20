/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.push;

import java.util.Collection;

/**
 * Listener that receives notifications of changes to data.
 */
public interface UpdateListener {

  /**
   * Invoked when something has been updated.
   * @param callbackId The callback IDs of the updated data
   */
  void itemUpdated(Object callbackId);

  /**
   * Invoked when multiple items have been updated.
   * @param callbackIds The RESTful URLs of the updated data
   */
  void itemsUpdated(Collection<?> callbackIds);
}
