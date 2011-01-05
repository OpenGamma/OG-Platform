package com.opengamma.master.jms;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.MasterChangeListener;
import com.opengamma.util.tuple.Pair;

/**
 * MasterChangeListener for use in a test environment.
 *
 */
/* package */ class TestMasterChangeClient implements MasterChangeListener {
    
    private UniqueIdentifier _addedItem;
    private UniqueIdentifier _removedItem;
    private Pair<UniqueIdentifier, UniqueIdentifier> _updatedItem;
    private Pair<UniqueIdentifier, UniqueIdentifier> _correctedItem;
    final private CountDownLatch _removedItemLatch = new CountDownLatch(1);
    final private CountDownLatch _addedItemLatch = new CountDownLatch(1);
    final private CountDownLatch _updatedItemLatch = new CountDownLatch(1);
    final private CountDownLatch _correctedItemLatch = new CountDownLatch(1);
    
    
    @Override
    public void added(UniqueIdentifier addedItem) {
      _addedItem = addedItem;
      _addedItemLatch.countDown();
    }
 
    /**
     * Gets the addedItem field.
     * @return the addedItem
     */
    public UniqueIdentifier getAddedItem() {
      return _addedItem;
    }

    /**
     * Gets the removedItem field.
     * @return the removedItem
     */
    public UniqueIdentifier getRemovedItem() {
      return _removedItem;
    }

    /**
     * Gets the updatedItem field.
     * @return the updatedItem
     */
    public Pair<UniqueIdentifier, UniqueIdentifier> getUpdatedItem() {
      return _updatedItem;
    }

    /**
     * Gets the correctedItem field.
     * @return the correctedItem
     */
    public Pair<UniqueIdentifier, UniqueIdentifier> getCorrectedItem() {
      return _correctedItem;
    }

    @Override
    public void removed(UniqueIdentifier removedItem) {
      _removedItem = removedItem;
      _removedItemLatch.countDown();
    }

    @Override
    public void updated(UniqueIdentifier oldItem, UniqueIdentifier newItem) {
      _updatedItem = Pair.of(oldItem, newItem);
      _updatedItemLatch.countDown();
    }

    @Override
    public void corrected(UniqueIdentifier oldItem, UniqueIdentifier newItem) {
      _correctedItem = Pair.of(oldItem, newItem);
      _correctedItemLatch.countDown();
    }

    public void waitForRemovedItem(long timeoutMs) {
      waitForLatch(_removedItemLatch, timeoutMs);
    }

    public void waitForAddedItem(long timeoutMs) {
      waitForLatch(_addedItemLatch, timeoutMs);
    }

    public void waitForUpdatedItem(long timeoutMs) {
      waitForLatch(_updatedItemLatch, timeoutMs);
    }
    
    public void waitForCorrectedItem(long timeoutMs) {
      waitForLatch(_correctedItemLatch, timeoutMs);
    }

    private void waitForLatch(CountDownLatch countDownLatch, long timeoutMs) {
      try {
        countDownLatch.await(timeoutMs, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        Thread.interrupted();
        throw new RuntimeException("Interrupted");
      }
    }
    
  }