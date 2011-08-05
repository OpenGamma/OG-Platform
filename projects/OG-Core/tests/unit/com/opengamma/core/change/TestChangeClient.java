/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.change;

import static org.testng.AssertJUnit.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.tuple.Pair;

/**
 * ChangeListener for use in a test environment.
 */
/* package */ class TestChangeClient implements ChangeListener {

  private UniqueIdentifier _addedItem;
  private UniqueIdentifier _removedItem;
  private Pair<UniqueIdentifier, UniqueIdentifier> _updatedItem;
  private Pair<UniqueIdentifier, UniqueIdentifier> _correctedItem;
  private final CountDownLatch _removedItemLatch = new CountDownLatch(1);
  private final CountDownLatch _addedItemLatch = new CountDownLatch(1);
  private final CountDownLatch _updatedItemLatch = new CountDownLatch(1);
  private final CountDownLatch _correctedItemLatch = new CountDownLatch(1);

  @Override
  public void entityChanged(ChangeEvent event) {
    switch (event.getType()) {
      case ADDED:
        _addedItem = event.getAfterId();
        _addedItemLatch.countDown();
        break;
      case UPDATED:
        _updatedItem = Pair.of(event.getBeforeId(), event.getAfterId());
        _updatedItemLatch.countDown();
        break;
      case REMOVED:
        _removedItem = event.getBeforeId();
        _removedItemLatch.countDown();
        break;
      case CORRECTED:
        _correctedItem = Pair.of(event.getBeforeId(), event.getAfterId());
        _correctedItemLatch.countDown();
        break;
    }
  }

  //-------------------------------------------------------------------------
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

  //-------------------------------------------------------------------------
  public void waitForAddedItem(long timeoutMs) {
    waitForLatch(_addedItemLatch, timeoutMs);
  }

  public void waitForUpdatedItem(long timeoutMs) {
    waitForLatch(_updatedItemLatch, timeoutMs);
  }

  public void waitForRemovedItem(long timeoutMs) {
    waitForLatch(_removedItemLatch, timeoutMs);
  }

  public void waitForCorrectedItem(long timeoutMs) {
    waitForLatch(_correctedItemLatch, timeoutMs);
  }

  private void waitForLatch(CountDownLatch countDownLatch, long timeoutMs) {
    try {
      assertEquals(true, countDownLatch.await(timeoutMs, TimeUnit.MILLISECONDS));
    } catch (InterruptedException e) {
      Thread.interrupted();
      throw new RuntimeException("Interrupted");
    }
  }

}
