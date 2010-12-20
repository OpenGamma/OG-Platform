package com.opengamma.master;

import com.opengamma.id.UniqueIdentifier;

/**
 * Listener interface for changes in a master document
 */
public interface MasterChangeListener {

  /**
   * Call back method when an item is added to a master.
   * 
   * @param addedItem the UniqueIdentifier of added item
   */
  void added(UniqueIdentifier addedItem);
  
  /**
   * Call back method when an item is removed.
   * 
   * @param removedItem the UniqueIdentifier of removed item
   */
  void removed(UniqueIdentifier removedItem);
  
  /**
   * Call back method when an item is updated.
   * 
   * @param oldItem the UniqueIdentifier before update
   * @param newItem the UniqueIdentifier after update
   */
  void updated(UniqueIdentifier oldItem, UniqueIdentifier newItem);
  
  /**
   * Call back method when an item is corrected.
   * 
   * @param oldItem the UniqueIdentifier before update
   * @param newItem the UniqueIdentifier after update
   */
  void corrected(UniqueIdentifier oldItem, UniqueIdentifier newItem);
}
