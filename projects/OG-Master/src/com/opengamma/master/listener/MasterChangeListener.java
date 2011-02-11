/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.listener;

import com.opengamma.util.PublicSPI;

/**
 * Listener interface used to receive events when a master changes.
 * <p>
 * Events will be sent when a master is added, updated, removed or corrected.
 */
@PublicSPI
public interface MasterChangeListener {

  /**
   * Event called when the master is changed.
   * <p>
   * Events will be sent when a master is added, updated, removed or corrected.
   * 
   * @param event  the event description, not null
   */
  void masterChanged(MasterChanged event);

}
