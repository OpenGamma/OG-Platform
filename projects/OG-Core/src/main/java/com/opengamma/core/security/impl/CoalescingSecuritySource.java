/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.security.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Wrapper around an existing {@link SecuritySource} that coalesces concurrent calls into a single call to one of the bulk operation methods on the underlying. This can improve efficiency where the
 * underlying uses network resources and the round trip of multiple single calls is less desirable than a single bulk call.
 */
public class CoalescingSecuritySource implements SecuritySource {

  private final SecuritySource _underlying;

  private abstract static class Callback {

    private int _expected;

    public Callback(final int expected) {
      _expected = expected;
    }

    protected abstract void store(final UniqueId uid, final Security security);

    public synchronized void found(final UniqueId uid, final Security security) {
      store(uid, security);
      if (--_expected <= 0) {
        notify();
      }
    }

    public synchronized void missed() {
      if (--_expected <= 0) {
        notify();
      }
    }

    /**
     * Blocks until all expected values are out, or the write lock could be claimed.
     */
    public synchronized boolean waitForResult(final AtomicBoolean writing) {
      try {
        while ((_expected > 0) && !writing.compareAndSet(false, true)) {
          wait();
        }
        return _expected <= 0;
      } catch (InterruptedException e) {
        throw new OpenGammaRuntimeException("Interrupted", e);
      }
    }

    public synchronized void release() {
      notify();
    }

  }

  private static class SingleCallback extends Callback {

    private Security _security;

    public SingleCallback() {
      super(1);
    }

    @Override
    protected void store(final UniqueId uid, final Security security) {
      _security = security;
    }

    public synchronized Security getSecurity() {
      return _security;
    }

  }

  private static class MultipleCallback extends Callback {

    private final Map<UniqueId, Security> _result;

    public MultipleCallback(final int expected) {
      super(expected);
      _result = Maps.newHashMapWithExpectedSize(expected);
    }

    @Override
    protected void store(final UniqueId uid, final Security security) {
      _result.put(uid, security);
    }

    public synchronized Map<UniqueId, Security> getSecurities() {
      return _result;
    }

  }

  private final AtomicBoolean _fetching = new AtomicBoolean();
  private final Queue<Pair<UniqueId, ? extends Callback>> _pending = new ConcurrentLinkedQueue<Pair<UniqueId, ? extends Callback>>();

  public CoalescingSecuritySource(final SecuritySource underlying) {
    _underlying = underlying;
  }

  protected SecuritySource getUnderlying() {
    return _underlying;
  }

  @Override
  public ChangeManager changeManager() {
    return getUnderlying().changeManager();
  }

  private Collection<Pair<UniqueId, ? extends Callback>> drainPending() {
    final Collection<Pair<UniqueId, ? extends Callback>> pending = new LinkedList<Pair<UniqueId, ? extends Callback>>();
    Pair<UniqueId, ? extends Callback> entry = _pending.poll();
    while (entry != null) {
      pending.add(entry);
      entry = _pending.poll();
    }
    return pending;
  }

  private void addPendingToRequest(final Collection<Pair<UniqueId, ? extends Callback>> pending, final Set<UniqueId> request) {
    for (Pair<UniqueId, ? extends Callback> pendingEntry : pending) {
      request.add(pendingEntry.getFirst());
    }
  }

  private void notifyPending(final Collection<Pair<UniqueId, ? extends Callback>> pending, final Map<UniqueId, Security> result) {
    for (Pair<UniqueId, ? extends Callback> pendingEntry : pending) {
      final Security security = result.get(pendingEntry.getFirst());
      if (security != null) {
        pendingEntry.getSecond().found(pendingEntry.getFirst(), security);
      } else {
        pendingEntry.getSecond().missed();
      }
    }
  }

  private void errorPending(final Collection<Pair<UniqueId, ? extends Callback>> pending) {
    for (Pair<UniqueId, ? extends Callback> pendingEntry : pending) {
      pendingEntry.getSecond().missed();
    }
  }

  protected void releaseOtherWritingThreads() {
    final Pair<UniqueId, ? extends Callback> otherThread = _pending.peek();
    if (otherThread != null) {
      // Notify the thread that it might be able to claim the write lock
      otherThread.getSecond().release();
    }
  }

  @Override
  public Security get(final UniqueId uniqueId) {
    if (!_fetching.compareAndSet(false, true)) {
      final SingleCallback callback = new SingleCallback();
      _pending.add(Pairs.of(uniqueId, callback));
      if (callback.waitForResult(_fetching)) {
        return callback.getSecurity();
      }
      // Request the pending queue
      final Collection<Pair<UniqueId, ? extends Callback>> pending = drainPending();
      final Set<UniqueId> request = Sets.newHashSetWithExpectedSize(pending.size());
      addPendingToRequest(pending, request);
      final Map<UniqueId, Security> fullResult;
      try {
        fullResult = getUnderlying().get(request);
        notifyPending(pending, fullResult);
      } catch (RuntimeException t) {
        errorPending(pending);
        throw t;
      } finally {
        _fetching.set(false);
        releaseOtherWritingThreads();
      }
      // We've either notified our own callback or another thread has already done it
      return callback.getSecurity();
    } else {
      Pair<UniqueId, ? extends Callback> e = _pending.poll();
      if (e == null) {
        // Single request
        Security security = null;
        try {
          security = getUnderlying().get(uniqueId);
        } catch (DataNotFoundException ex) {
          // Ignore
        } finally {
          _fetching.set(false);
          releaseOtherWritingThreads();
        }
        return security;
      } else {
        // Single request, e and the content of the pending queue
        final Collection<Pair<UniqueId, ? extends Callback>> pending = drainPending();
        pending.add(e);
        final Set<UniqueId> request = Sets.newHashSetWithExpectedSize(pending.size() + 1);
        request.add(uniqueId);
        addPendingToRequest(pending, request);
        final Map<UniqueId, Security> fullResult;
        try {
          fullResult = getUnderlying().get(request);
          notifyPending(pending, fullResult);
        } catch (RuntimeException t) {
          errorPending(pending);
          throw t;
        } finally {
          _fetching.set(false);
          releaseOtherWritingThreads();
        }
        return fullResult.get(uniqueId);
      }
    }
  }

  @Override
  public Map<UniqueId, Security> get(final Collection<UniqueId> uniqueIds) {
    if (!_fetching.compareAndSet(false, true)) {
      final MultipleCallback callback = new MultipleCallback(uniqueIds.size());
      for (UniqueId uniqueId : uniqueIds) {
        _pending.add(Pairs.of(uniqueId, callback));
      }
      if (callback.waitForResult(_fetching)) {
        return callback.getSecurities();
      }
      // Request the pending queue
      final Collection<Pair<UniqueId, ? extends Callback>> pending = drainPending();
      final Set<UniqueId> request = Sets.newHashSetWithExpectedSize(pending.size());
      addPendingToRequest(pending, request);
      final Map<UniqueId, Security> fullResult;
      try {
        fullResult = getUnderlying().get(request);
        notifyPending(pending, fullResult);
      } catch (RuntimeException t) {
        errorPending(pending);
        throw t;
      } finally {
        _fetching.set(false);
        releaseOtherWritingThreads();
      }
      // We've either notified our own callback or another thread has already done it
      return callback.getSecurities();
    } else {
      Pair<UniqueId, ? extends Callback> e = _pending.poll();
      if (e == null) {
        // Direct request
        final Map<UniqueId, Security> result;
        try {
          result = getUnderlying().get(uniqueIds);
        } finally {
          _fetching.set(false);
          releaseOtherWritingThreads();
        }
        return result;
      } else {
        // Bulk request, e and the content of the pending queue
        final Collection<Pair<UniqueId, ? extends Callback>> pending = drainPending();
        pending.add(e);
        final Set<UniqueId> request = Sets.newHashSetWithExpectedSize(pending.size() + uniqueIds.size());
        request.addAll(uniqueIds);
        addPendingToRequest(pending, request);
        final Map<UniqueId, Security> fullResult;
        try {
          fullResult = getUnderlying().get(request);
          notifyPending(pending, fullResult);
        } catch (RuntimeException t) {
          errorPending(pending);
          throw t;
        } finally {
          _fetching.set(false);
          releaseOtherWritingThreads();
        }
        final Map<UniqueId, Security> result = Maps.newHashMapWithExpectedSize(uniqueIds.size());
        for (UniqueId uniqueId : uniqueIds) {
          final Security security = fullResult.get(uniqueId);
          if (security != null) {
            result.put(uniqueId, security);
          }
        }
        return result;
      }
    }
  }

  @Override
  public Security get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    return getUnderlying().get(objectId, versionCorrection);
  }

  @Override
  public Map<ObjectId, Security> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
    return getUnderlying().get(objectIds, versionCorrection);
  }

  @Override
  public Collection<Security> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    return getUnderlying().get(bundle, versionCorrection);
  }

  @Override
  public Map<ExternalIdBundle, Collection<Security>> getAll(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    return getUnderlying().getAll(bundles, versionCorrection);
  }

  @Override
  public Collection<Security> get(final ExternalIdBundle bundle) {
    return getUnderlying().get(bundle);
  }

  @Override
  public Security getSingle(final ExternalIdBundle bundle) {
    return getUnderlying().getSingle(bundle);
  }

  @Override
  public Security getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    return getUnderlying().getSingle(bundle, versionCorrection);
  }

  @Override
  public Map<ExternalIdBundle, Security> getSingle(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    return getUnderlying().getSingle(bundles, versionCorrection);
  }

}
