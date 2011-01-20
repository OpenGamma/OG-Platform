/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.listener;

import com.opengamma.id.UniqueIdentifier;

/**
 * Listener interface used to receive events when a master changes.
 * <p>
 * Events will be sent when a document in a master is added, updated, removed or corrected.
 */
public interface MasterChangeListener {

  /**
   * Event called when a document is added to the master.
   * 
   * @param addedItem  the unique identifier of the added item, not null
   */
  void added(UniqueIdentifier addedItem);

  /**
   * Event called when a document is removed from the master.
   * 
   * @param removedItem  the unique identifier of the removed item, not null
   */
  void removed(UniqueIdentifier removedItem);

  /**
   * Event called when a document is updated in the master.
   * 
   * @param oldItem  the unique identifier of the item before the update, not null
   * @param newItem  the unique identifier of the item after the update, not null
   */
  void updated(UniqueIdentifier oldItem, UniqueIdentifier newItem);

  /**
   * Event called when a document is corrected in the master.
   * 
   * @param oldItem  the unique identifier of the item before the correction, not null
   * @param newItem  the unique identifier of the item after the correction, not null
   */
  void corrected(UniqueIdentifier oldItem, UniqueIdentifier newItem);

}
