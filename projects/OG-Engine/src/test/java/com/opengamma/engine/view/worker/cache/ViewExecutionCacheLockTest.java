/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker.cache;

import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertSame;

import java.util.concurrent.locks.Lock;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.worker.cache.ViewExecutionCacheKey;
import com.opengamma.engine.view.worker.cache.ViewExecutionCacheLock;
import com.opengamma.id.UniqueId;

/**
 * Tests the {@link ViewExecutionCacheLock} class.
 */
@Test
public class ViewExecutionCacheLockTest {

  private ViewDefinition viewDefinition() {
    return new ViewDefinition(UniqueId.of("View", "123"), "Test 1", "User");
  }

  private MarketDataAvailabilityProvider marketDataProvider(final String key) {
    final MarketDataAvailabilityProvider mock = Mockito.mock(MarketDataAvailabilityProvider.class);
    Mockito.when(mock.getAvailabilityHintKey()).thenReturn(key);
    return mock;
  }

  public void testOperation() {
    final ViewExecutionCacheLock locks1 = new ViewExecutionCacheLock();
    final ViewExecutionCacheLock locks2 = new ViewExecutionCacheLock();
    final ViewExecutionCacheKey keyA = ViewExecutionCacheKey.of(viewDefinition(), marketDataProvider("A"));
    final ViewExecutionCacheKey keyB = ViewExecutionCacheKey.of(viewDefinition(), marketDataProvider("B"));
    final Lock lockA1 = locks1.get(keyA);
    final Lock lockB1 = locks1.get(keyB);
    final Lock lockA2 = locks2.get(keyA);
    final Lock lockB2 = locks2.get(keyB);
    assertNotSame(lockA1, lockB1);
    assertNotSame(lockA2, lockB2);
    assertNotSame(lockA1, lockA2);
    assertNotSame(lockB1, lockB2);
    assertSame(locks1.get(keyA), lockA1);
    assertSame(locks1.get(keyB), lockB1);
  }

}
