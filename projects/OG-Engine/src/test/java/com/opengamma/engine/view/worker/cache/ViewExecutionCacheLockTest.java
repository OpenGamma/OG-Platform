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
import org.threeten.bp.Instant;

import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;

/**
 * Tests the {@link ViewExecutionCacheLock} class.
 */
@Test(groups = TestGroup.UNIT)
public class ViewExecutionCacheLockTest {

  private ViewDefinition viewDefinition() {
    return new ViewDefinition(UniqueId.of("View", "123"), "Test 1", "User");
  }

  private MarketDataAvailabilityProvider marketDataProvider(final String key) {
    final MarketDataAvailabilityProvider mock = Mockito.mock(MarketDataAvailabilityProvider.class);
    Mockito.when(mock.getAvailabilityHintKey()).thenReturn(key);
    return mock;
  }

  public void testBroadLock() {
    final ViewExecutionCacheLock locks1 = new ViewExecutionCacheLock();
    final ViewExecutionCacheLock locks2 = new ViewExecutionCacheLock();
    final ViewExecutionCacheKey keyA = ViewExecutionCacheKey.of(viewDefinition(), marketDataProvider("A"), null);
    final ViewExecutionCacheKey keyB = ViewExecutionCacheKey.of(viewDefinition(), marketDataProvider("B"), null);
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

  public void testFinerLock() {
    final ViewExecutionCacheLock locks = new ViewExecutionCacheLock();
    final ViewExecutionCacheKey keyA = ViewExecutionCacheKey.of(viewDefinition(), marketDataProvider("A"), null);
    final ViewExecutionCacheKey keyB = ViewExecutionCacheKey.of(viewDefinition(), marketDataProvider("B"), null);
    final Instant valuationTimeA = Instant.now();
    final Instant valuationTimeB = valuationTimeA.plusSeconds(100);
    final VersionCorrection resolverVersionCorrectionA = VersionCorrection.of(valuationTimeA.minusSeconds(1), valuationTimeA.minusSeconds(2));
    final VersionCorrection resolverVersionCorrectionB = VersionCorrection.of(valuationTimeA.minusSeconds(3), valuationTimeA.minusSeconds(3));
    @SuppressWarnings("unchecked")
    final Pair<Lock, Lock>[] ls = new Pair[] {locks.get(keyA, valuationTimeA, resolverVersionCorrectionA), locks.get(keyA, valuationTimeA, resolverVersionCorrectionB),
        locks.get(keyA, valuationTimeB, resolverVersionCorrectionA), locks.get(keyA, valuationTimeB, resolverVersionCorrectionB),
        locks.get(keyB, valuationTimeA, resolverVersionCorrectionA), locks.get(keyB, valuationTimeA, resolverVersionCorrectionB),
        locks.get(keyB, valuationTimeB, resolverVersionCorrectionA), locks.get(keyB, valuationTimeB, resolverVersionCorrectionB) };
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        if ((i < 4) == (j < 4)) {
          assertSame(ls[j].getFirst(), ls[i].getFirst());
        } else {
          assertNotSame(ls[j].getFirst(), ls[i].getFirst());
        }
        if (i != j) {
          assertNotSame(ls[j].getSecond(), ls[i].getSecond());
        }
      }
    }
    assertSame(locks.get(keyA, valuationTimeA, resolverVersionCorrectionB).getSecond(), ls[1].getSecond());
  }

}
