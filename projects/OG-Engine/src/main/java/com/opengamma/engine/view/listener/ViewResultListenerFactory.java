/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.listener;

import com.opengamma.livedata.UserPrincipal;


/**
 * Factory producing listeners to be attached to the results of a view process for a specific purpose.
 */
public interface ViewResultListenerFactory {

  /**
   * Creates a new view result listener.
   * 
   * @param user  the listener user, not null
   * @return the view result listener, not null
   */
  ViewResultListener createViewResultListener(UserPrincipal user);
}
