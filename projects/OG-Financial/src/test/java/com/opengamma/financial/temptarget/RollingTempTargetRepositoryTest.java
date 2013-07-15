/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.temptarget;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.testng.annotations.Test;

import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class RollingTempTargetRepositoryTest {

  private static class Mock extends RollingTempTargetRepository {

    @Override
    protected TempTarget getOldGeneration(final long uid) {
      throw new UnsupportedOperationException();
    }

    @Override
    protected TempTarget getNewGeneration(final long uid) {
      throw new UnsupportedOperationException();
    }

    @Override
    protected Long findOldGeneration(final TempTarget target) {
      throw new UnsupportedOperationException();
    }

    @Override
    protected long findOrAddNewGeneration(final TempTarget target) {
      throw new UnsupportedOperationException();
    }

    @Override
    protected boolean copyOldToNewGeneration(final long deadTime, final List<Long> deletes) {
      throw new UnsupportedOperationException();
    }

    @Override
    protected void nextGeneration() {
      throw new UnsupportedOperationException();
    }

  }

  public void testInvalidScheme() {
    final RollingTempTargetRepository mock = new Mock();
    assertNull(mock.get(UniqueId.of("Foo", "42")));
  }

  public void testGetDefault() {
    final TempTarget target = new MockTempTarget("Foo");
    final RollingTempTargetRepository mock = new Mock() {
      @Override
      public TempTarget getNewGeneration(final long v) {
        assertEquals(v, 0L);
        return target;
      }
    };
    assertEquals(mock.get(UniqueId.of(RollingTempTargetRepository.SCHEME, "0")), target);
  }

  public void testLocateOld() {
    final TempTarget target = new MockTempTarget("Foo");
    final RollingTempTargetRepository mock = new Mock() {
      @Override
      public Long findOldGeneration(final TempTarget t) {
        assertEquals(t, target);
        return 42L;
      }
    };
    assertEquals(mock.locateOrStore(target), UniqueId.of(RollingTempTargetRepository.SCHEME, "42"));
  }

  public void testLocateNew() {
    final TempTarget target = new MockTempTarget("Foo");
    final RollingTempTargetRepository mock = new Mock() {

      @Override
      public Long findOldGeneration(final TempTarget t) {
        assertEquals(t, target);
        return null;
      }

      @Override
      public long findOrAddNewGeneration(final TempTarget t) {
        assertEquals(t, target);
        return 69;
      }

    };
    assertEquals(mock.locateOrStore(target), UniqueId.of(RollingTempTargetRepository.SCHEME, "69"));
  }

  public void testHousekeepNoCopy() {
    final RollingTempTargetRepository mock = new Mock() {
      @Override
      public boolean copyOldToNewGeneration(final long deadTime, final List<Long> deletes) {
        return false;
      }
    };
    mock.housekeep();
  }

  public void testHousekeep() {
    final AtomicLong oldGenerationGet = new AtomicLong();
    final AtomicLong newGenerationGet = new AtomicLong();
    final AtomicInteger nextGenerationCount = new AtomicInteger();
    final RollingTempTargetRepository mock = new Mock() {

      @Override
      public TempTarget getOldGeneration(final long uid) {
        oldGenerationGet.set(uid);
        return null;
      }

      @Override
      public TempTarget getNewGeneration(final long uid) {
        newGenerationGet.set(uid);
        return null;
      }

      @Override
      public boolean copyOldToNewGeneration(final long deadTime, final List<Long> deletes) {
        return true;
      }

      @Override
      public void nextGeneration() {
        nextGenerationCount.incrementAndGet();
      }

    };
    final long id1 = mock.allocIdentifier();
    final long id2 = mock.allocIdentifier();
    final long id3 = mock.allocIdentifier();
    assertEquals(nextGenerationCount.get(), 0);
    mock.housekeep();
    assertEquals(nextGenerationCount.get(), 1);
    final long id4 = mock.allocIdentifier();
    final long id5 = mock.allocIdentifier();
    mock.get(UniqueId.of(RollingTempTargetRepository.SCHEME, Long.toString(id1)));
    assertEquals(oldGenerationGet.get(), id1);
    mock.get(UniqueId.of(RollingTempTargetRepository.SCHEME, Long.toString(id2)));
    assertEquals(oldGenerationGet.get(), id2);
    mock.get(UniqueId.of(RollingTempTargetRepository.SCHEME, Long.toString(id3)));
    assertEquals(oldGenerationGet.get(), id3);
    mock.get(UniqueId.of(RollingTempTargetRepository.SCHEME, Long.toString(id4)));
    assertEquals(newGenerationGet.get(), id4);
    mock.get(UniqueId.of(RollingTempTargetRepository.SCHEME, Long.toString(id5)));
    assertEquals(newGenerationGet.get(), id5);
  }

}
