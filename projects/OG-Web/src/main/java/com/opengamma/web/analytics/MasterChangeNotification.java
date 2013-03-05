/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.AbstractMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Wraps a change event from a master and the master that produced it. This allows a listener to receive an event
 * and request a new copy of the changed object without having to know directly about all the masters that
 * can produce changes.
 * @param <D> The document type produced by the master.
 */
/* package */ class MasterChangeNotification<D extends AbstractDocument> {

  /** The change event that triggered this notification. */
  private final ChangeEvent _event;
  /** The master that produced the event. */
  private final AbstractMaster<D> _master;

  /* package */ MasterChangeNotification(ChangeEvent event, AbstractMaster<D> master) {
    ArgumentChecker.notNull(event, "event");
    ArgumentChecker.notNull(master, "master");
    _event = event;
    _master = master;
  }

  /**
   * @return The event that triggered this notification.
   */
  /* package */ ChangeEvent getEvent() {
    return _event;
  }

  /**
   * @return The changed version of the object, looked up from the master that produced the event.
   */
  /* package */ UniqueIdentifiable getEntity() {
    VersionCorrection versionCorrection = VersionCorrection.of(_event.getVersionFrom(), _event.getVersionInstant());
    return _master.get(_event.getObjectId(), versionCorrection).getValue();
  }
}
