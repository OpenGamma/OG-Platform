/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.change;

import com.opengamma.util.PublicSPI;

/**
 * Listener interface used to receive entity change events.
 * <p>
 * Events are sent when an entity is added, updated, removed or corrected.
 */
@PublicSPI
public interface ChangeListener {

  /**
   * Called when an entity is changed.
   * <p>
   * Events are sent when an entity is added, updated, removed or corrected.
   * 
   * @param event  the event description, not null
   */
  void entityChanged(ChangeEvent event);

}
