/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

import javax.time.InstantProvider;

/**
 * Trait for a "master" that allows listeners to be registered for events.
 */
public interface NotifyingMaster {

  // TODO this is a dirty hack until this is done more neatly

  /**
   * Callback interface implemented by a listener.
   */
  interface MasterChangeListener {

    void onMasterChanged(InstantProvider changeInstant);

  }

  void addOnChangeListener(MasterChangeListener listener);

  void removeOnChangeListener(MasterChangeListener listener);

}
