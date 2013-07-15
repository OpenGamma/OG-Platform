/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.security.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.security.impl.test.MockSecuritySource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.Timeout;

/**
 * Tests the {@link CoalescingSecuritySource} class.
 */
@Test(groups = TestGroup.INTEGRATION)
public class CoalescingSecuritySourceTest {

  private static void join(final CyclicBarrier barrier) {
    try {
      barrier.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("interrupted", e);
    }
  }

  private static void sleep() {
    try {
      Thread.sleep(Timeout.standardTimeoutMillis() / 4);
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("interrupted", e);
    }
  }

  public void testGetSecurity_byUniqueId_a() throws Exception {
    final UniqueId uidA = UniqueId.of("Test", "A");
    final Security secA = Mockito.mock(Security.class);
    final UniqueId uidB = UniqueId.of("Test", "B");
    final Security secB = Mockito.mock(Security.class);
    final UniqueId uidC = UniqueId.of("Test", "C");
    final Security secC = Mockito.mock(Security.class);
    final CyclicBarrier barrier = new CyclicBarrier(4);
    final MockSecuritySource underlying = new MockSecuritySource() {

      int _state;

      @Override
      public Security get(final UniqueId uid) {
        assertEquals(_state++, 0);
        join(barrier); //1
        assertEquals(uid, uidA);
        // Pause for a bit to make sure that the other threads get blocked in their getSecurity methods
        sleep();
        return secA;
      }

      @Override
      public Map<UniqueId, Security> get(final Collection<UniqueId> uids) {
        assertEquals(_state++, 1);
        assertEquals(uids.size(), 2);
        assertTrue(uids.contains(uidB));
        assertTrue(uids.contains(uidC));
        final Map<UniqueId, Security> result = Maps.newHashMapWithExpectedSize(2);
        result.put(uidB, secB);
        result.put(uidC, secC);
        return result;
      }

    };
    final CoalescingSecuritySource coalescing = new CoalescingSecuritySource(underlying);
    // Start three threads. One will do the first write, the other two will be blocked. Then one of the other two will do a second
    // write that includes that required by the third. The third will do no I/O itself.
    final ExecutorService exec = Executors.newCachedThreadPool();
    try {
      final Future<?> a = exec.submit(new Runnable() {
        @Override
        public void run() {
          final Security s = coalescing.get(uidA);
          assertSame(s, secA);
        }
      });
      final Future<?> b = exec.submit(new Runnable() {
        @Override
        public void run() {
          join(barrier);
          final Security s = coalescing.get(uidB);
          assertSame(s, secB);
        }
      });
      final Future<?> c = exec.submit(new Runnable() {
        @Override
        public void run() {
          join(barrier);
          final Security s = coalescing.get(uidC);
          assertSame(s, secC);
        }
      });
      join(barrier);
      a.get(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      b.get(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      c.get(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
    } finally {
      exec.shutdownNow();
    }
  }

  public void testGetSecurity_byUniqueId_b() throws Exception {
    final UniqueId uidA = UniqueId.of("Test", "A");
    final Security secA = Mockito.mock(Security.class);
    final UniqueId uidB = UniqueId.of("Test", "B");
    final Security secB = Mockito.mock(Security.class);
    final UniqueId uidC = UniqueId.of("Test", "C");
    final Security secC = Mockito.mock(Security.class);
    final CyclicBarrier barrier1 = new CyclicBarrier(3);
    final CyclicBarrier barrier2 = new CyclicBarrier(2);
    final MockSecuritySource underlying = new MockSecuritySource() {

      int _state;

      @Override
      public Security get(final UniqueId uid) {
        assertEquals(_state++, 0);
        join(barrier1);
        assertEquals(uid, uidA);
        // Pause for a bit to make sure that the other thread gets blocked in their getSecurity methods
        sleep();
        return secA;
      }

      @Override
      public Map<UniqueId, Security> get(final Collection<UniqueId> uids) {
        assertEquals(_state++, 1);
        assertEquals(uids.size(), 2);
        assertTrue(uids.contains(uidB));
        assertTrue(uids.contains(uidC));
        final Map<UniqueId, Security> result = Maps.newHashMapWithExpectedSize(2);
        result.put(uidB, secB);
        result.put(uidC, secC);
        return result;
      }

    };
    final CoalescingSecuritySource coalescing = new CoalescingSecuritySource(underlying) {
      @Override
      protected void releaseOtherWritingThreads() {
        join(barrier2); // 1 + 2 // release the third thread
      }
    };
    // Start two threads. One will do the first write, the other will be blocked. Suppressing releaseOtherThreads means a third
    // call will try to write its own value plus those from the other threads. The second thread will do no I/O itself.
    final ExecutorService exec = Executors.newCachedThreadPool();
    try {
      final Future<?> a = exec.submit(new Runnable() {
        @Override
        public void run() {
          final Security s = coalescing.get(uidA);
          assertSame(s, secA);
        }
      });
      final Future<?> b = exec.submit(new Runnable() {
        @Override
        public void run() {
          join(barrier1);
          final Security s = coalescing.get(uidB);
          assertSame(s, secB);
        }
      });
      final Future<?> c = exec.submit(new Runnable() {
        @Override
        public void run() {
          join(barrier2); // 1
          final Security s = coalescing.get(uidC);
          assertSame(s, secC);
        }
      });
      join(barrier1);
      a.get(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      b.get(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      join(barrier2); // 2
      c.get(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
    } finally {
      exec.shutdownNow();
    }
  }

  public void testGetSecurities_byUniqueId_a() throws Exception {
    final UniqueId uidA = UniqueId.of("Test", "A");
    final Security secA = Mockito.mock(Security.class);
    final UniqueId uidB = UniqueId.of("Test", "B");
    final Security secB = Mockito.mock(Security.class);
    final UniqueId uidC = UniqueId.of("Test", "C");
    final Security secC = Mockito.mock(Security.class);
    final CyclicBarrier barrier = new CyclicBarrier(4);
    final MockSecuritySource underlying = new MockSecuritySource() {

      int _state;

      @Override
      public Map<UniqueId, Security> get(final Collection<UniqueId> uids) {
        final Map<UniqueId, Security> result = Maps.newHashMapWithExpectedSize(uids.size());
        if (++_state == 1) {
          assertEquals(uids.size(), 2);
          join(barrier);
          assertTrue(uids.contains(uidA));
          assertTrue(uids.contains(uidB));
          result.put(uidA, secA);
          result.put(uidB, secB);
          // Pause for a bit to make sure that the other threads get blocked in their getSecurity methods
          sleep();
        } else if (_state == 2) {
          assertEquals(uids.size(), 3);
          assertTrue(uids.contains(uidA));
          assertTrue(uids.contains(uidB));
          assertTrue(uids.contains(uidC));
          result.put(uidA, secA);
          result.put(uidB, secB);
          result.put(uidC, secC);
        } else {
          fail();
        }
        return result;
      }

    };
    final CoalescingSecuritySource coalescing = new CoalescingSecuritySource(underlying);
    // Start three threads. One will do the first write, the other two will be blocked. Then one of the other two will do a second
    // write that includes that required by the third. The third will do no I/O itself.
    final ExecutorService exec = Executors.newCachedThreadPool();
    try {
      final Future<?> a = exec.submit(new Runnable() {
        @Override
        public void run() {
          final Map<UniqueId, Security> result = coalescing.get(Arrays.asList(uidA, uidB));
          assertEquals(result.size(), 2);
          assertSame(result.get(uidA), secA);
          assertSame(result.get(uidB), secB);
        }
      });
      final Future<?> b = exec.submit(new Runnable() {
        @Override
        public void run() {
          join(barrier);
          final Map<UniqueId, Security> result = coalescing.get(Arrays.asList(uidA, uidC));
          assertEquals(result.size(), 2);
          assertSame(result.get(uidA), secA);
          assertSame(result.get(uidC), secC);
        }
      });
      final Future<?> c = exec.submit(new Runnable() {
        @Override
        public void run() {
          join(barrier);
          final Map<UniqueId, Security> result = coalescing.get(Arrays.asList(uidB, uidC));
          assertEquals(result.size(), 2);
          assertSame(result.get(uidB), secB);
          assertSame(result.get(uidC), secC);
        }
      });
      join(barrier);
      a.get(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      b.get(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      c.get(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
    } finally {
      exec.shutdownNow();
    }
  }

  public void testGetSecurities_byUniqueId_b() throws Exception {
    final UniqueId uidA = UniqueId.of("Test", "A");
    final Security secA = Mockito.mock(Security.class);
    final UniqueId uidB = UniqueId.of("Test", "B");
    final Security secB = Mockito.mock(Security.class);
    final UniqueId uidC = UniqueId.of("Test", "C");
    final Security secC = Mockito.mock(Security.class);
    final CyclicBarrier barrier1 = new CyclicBarrier(3);
    final CyclicBarrier barrier2 = new CyclicBarrier(2);
    final MockSecuritySource underlying = new MockSecuritySource() {

      int _state;

      @Override
      public Map<UniqueId, Security> get(final Collection<UniqueId> uids) {
        final Map<UniqueId, Security> result = Maps.newHashMapWithExpectedSize(uids.size());
        if (++_state == 1) {
          assertEquals(uids.size(), 2);
          join(barrier1);
          assertTrue(uids.contains(uidA));
          assertTrue(uids.contains(uidB));
          result.put(uidA, secA);
          result.put(uidB, secB);
          // Pause for a bit to make sure that the other threads get blocked in their getSecurity methods
          sleep();
        } else if (_state == 2) {
          assertEquals(uids.size(), 3);
          assertTrue(uids.contains(uidA));
          assertTrue(uids.contains(uidB));
          assertTrue(uids.contains(uidC));
          result.put(uidA, secA);
          result.put(uidB, secB);
          result.put(uidC, secC);
        } else {
          fail();
        }
        return result;
      }

    };
    final CoalescingSecuritySource coalescing = new CoalescingSecuritySource(underlying) {
      @Override
      protected void releaseOtherWritingThreads() {
        join(barrier2); // 1 + 2 // release the third thread
      }
    };
    // Start two threads. One will do the first write, the other will be blocked. Suppressing releaseOtherThreads means a third
    // call will try to write its own value plus those from the other threads. The second thread will do no I/O itself.
    final ExecutorService exec = Executors.newCachedThreadPool();
    try {
      final Future<?> a = exec.submit(new Runnable() {
        @Override
        public void run() {
          final Map<UniqueId, Security> result = coalescing.get(Arrays.asList(uidA, uidB));
          assertEquals(result.size(), 2);
          assertSame(result.get(uidA), secA);
          assertSame(result.get(uidB), secB);
        }
      });
      final Future<?> b = exec.submit(new Runnable() {
        @Override
        public void run() {
          join(barrier1);
          final Map<UniqueId, Security> result = coalescing.get(Arrays.asList(uidA, uidC));
          assertEquals(result.size(), 2);
          assertSame(result.get(uidA), secA);
          assertSame(result.get(uidC), secC);
        }
      });
      final Future<?> c = exec.submit(new Runnable() {
        @Override
        public void run() {
          join(barrier2); //1
          final Map<UniqueId, Security> result = coalescing.get(Arrays.asList(uidB, uidC));
          assertEquals(result.size(), 2);
          assertSame(result.get(uidB), secB);
          assertSame(result.get(uidC), secC);
        }
      });
      join(barrier1);
      a.get(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      b.get(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      join(barrier2); // 1 + 2
      c.get(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
    } finally {
      exec.shutdownNow();
    }
  }

  public void testGetSecurity_byObjectId() {
    final SecuritySource underlying = Mockito.mock(SecuritySource.class);
    final SecuritySource coalescing = new CoalescingSecuritySource(underlying);
    coalescing.get(ObjectId.of("Test", "Test"), VersionCorrection.LATEST);
    Mockito.verify(underlying).get(ObjectId.of("Test", "Test"), VersionCorrection.LATEST);
  }

  public void testGetSecurities_byExternalIdBundleVersionCorrection() {
    final SecuritySource underlying = Mockito.mock(SecuritySource.class);
    final SecuritySource coalescing = new CoalescingSecuritySource(underlying);
    coalescing.get(ExternalIdBundle.EMPTY, VersionCorrection.LATEST);
    Mockito.verify(underlying).get(ExternalIdBundle.EMPTY, VersionCorrection.LATEST);
  }

  public void testGetSecurities_byExternalIdBundle() {
    final SecuritySource underlying = Mockito.mock(SecuritySource.class);
    final SecuritySource coalescing = new CoalescingSecuritySource(underlying);
    coalescing.get(ExternalIdBundle.EMPTY);
    Mockito.verify(underlying).get(ExternalIdBundle.EMPTY);
  }

  public void testGetSecurity_byExternalIdBundle() {
    final SecuritySource underlying = Mockito.mock(SecuritySource.class);
    final SecuritySource coalescing = new CoalescingSecuritySource(underlying);
    coalescing.get(ExternalIdBundle.EMPTY);
    Mockito.verify(underlying).get(ExternalIdBundle.EMPTY);
  }

  public void testGetSecurity_byExternalIdBundleVersionCorrection() {
    final SecuritySource underlying = Mockito.mock(SecuritySource.class);
    final SecuritySource coalescing = new CoalescingSecuritySource(underlying);
    coalescing.get(ExternalIdBundle.EMPTY, VersionCorrection.LATEST);
    Mockito.verify(underlying).get(ExternalIdBundle.EMPTY, VersionCorrection.LATEST);
  }

}
