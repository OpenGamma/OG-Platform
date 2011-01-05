/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

/**
 * Trait for a "master" that allows listeners to be registered for events.
 */
public interface NotifyingMaster {

  void addChangeListener(MasterChangeListener listener);

  void removeChangeListener(MasterChangeListener listener);

}
