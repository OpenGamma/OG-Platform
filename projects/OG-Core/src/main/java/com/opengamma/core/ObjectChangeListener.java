/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core;

import com.opengamma.id.ObjectId;

/**
 * Listener providing callbacks when the object changes.
 */
public interface ObjectChangeListener {

  /**
   * Callback that is invoked if the object identifier now refers to a object.
   *
   * @param oid the object identifier, not null
   */
  void objectChanged(ObjectId oid);

}
