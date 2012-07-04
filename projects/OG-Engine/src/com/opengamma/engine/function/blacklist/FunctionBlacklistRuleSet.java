/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * Manages a set of {@link FunctionBlacklistRule}s with expiry times.
 */
public class FunctionBlacklistRuleSet implements Set<FunctionBlacklistRule> {

  private static final Logger s_logger = LoggerFactory.getLogger(FunctionBlacklistRuleSet.class);
  private static final int MAX_TTL = 86400;

  private final Map<FunctionBlacklistRule, Long> _rules = new ConcurrentHashMap<FunctionBlacklistRule, Long>();
  private volatile int _minimumTTL;
  private final Cleaner _cleaner;

  private static class Cleaner implements Runnable {

    private final ScheduledExecutorService _executor;
    private final WeakReference<FunctionBlacklistRuleSet> _ref;
    private Future<?> _future;

    public Cleaner(final FunctionBlacklistRuleSet ref, final ScheduledExecutorService executor, final int minimumTTL) {
      s_logger.debug("Creating cleaner for {} at {}s", ref, minimumTTL);
      _executor = executor;
      _ref = new WeakReference<FunctionBlacklistRuleSet>(ref);
      _future = executor.scheduleWithFixedDelay(this, minimumTTL, minimumTTL, TimeUnit.SECONDS);
    }

    public void reschedule(final int time) {
      // Reference must be valid because this gets called from the referent
      s_logger.debug("Rescheduling cleanup of {} for {}s", _ref.get(), time);
      _future.cancel(false);
      _future = _executor.scheduleWithFixedDelay(this, time, time, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
      final FunctionBlacklistRuleSet ref = _ref.get();
      if (ref == null) {
        _future.cancel(false);
      } else {
        ref.clean();
      }
    }

  }

  public FunctionBlacklistRuleSet(final ScheduledExecutorService executor) {
    this(executor, MAX_TTL);
  }

  public FunctionBlacklistRuleSet(final ScheduledExecutorService executor, final int minimumTTL) {
    _minimumTTL = minimumTTL;
    _cleaner = new Cleaner(this, executor, minimumTTL);
  }

  @Override
  public int size() {
    return _rules.size();
  }

  @Override
  public boolean isEmpty() {
    return _rules.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return _rules.keySet().contains(o);
  }

  @Override
  public Iterator<FunctionBlacklistRule> iterator() {
    final Iterator<FunctionBlacklistRule> itr = _rules.keySet().iterator();
    return new Iterator<FunctionBlacklistRule>() {

      private FunctionBlacklistRule _last;

      @Override
      public boolean hasNext() {
        return itr.hasNext();
      }

      @Override
      public FunctionBlacklistRule next() {
        _last = itr.next();
        return _last;
      }

      @Override
      public void remove() {
        itr.remove();
        onRemove(_last);
      }

    };
  }

  @Override
  public Object[] toArray() {
    return _rules.keySet().toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return _rules.keySet().toArray(a);
  }

  @Override
  public boolean add(final FunctionBlacklistRule e) {
    add(e, _minimumTTL);
    return true;
  }

  @Override
  public boolean remove(Object o) {
    if (!_rules.keySet().remove(o)) {
      return false;
    }
    onRemove((FunctionBlacklistRule) o);
    return true;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return _rules.keySet().containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends FunctionBlacklistRule> c) {
    for (FunctionBlacklistRule rule : c) {
      add(rule, _minimumTTL);
    }
    return true;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    boolean changed = false;
    final Iterator<FunctionBlacklistRule> itr = iterator();
    while (itr.hasNext()) {
      if (!c.contains(itr.next())) {
        itr.remove();
        changed = true;
      }
    }
    return changed;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    boolean changed = false;
    for (Object o : c) {
      changed |= remove(o);
    }
    return changed;
  }

  @Override
  public void clear() {
    final Iterator<FunctionBlacklistRule> itr = iterator();
    while (itr.hasNext()) {
      itr.next();
      itr.remove();
    }
  }

  public void add(final FunctionBlacklistRule rule, final int timeToLive) {
    ArgumentChecker.notNegativeOrZero(timeToLive, "timeToLive");
    final long expiry = System.nanoTime() + (long) timeToLive * 1000000000L;
    if (_rules.put(rule, expiry) == null) {
      onAdd(rule);
    }
    if (timeToLive < _minimumTTL) {
      synchronized (this) {
        if (timeToLive < _minimumTTL) {
          _cleaner.reschedule(timeToLive);
          _minimumTTL = timeToLive;
        }
      }
    }
  }

  protected void clean() {
    s_logger.debug("Cleaning ruleset {}", this);
    final long time = System.nanoTime();
    _minimumTTL = MAX_TTL + 1;
    final Iterator<Map.Entry<FunctionBlacklistRule, Long>> itr = _rules.entrySet().iterator();
    long lowestTTL = (long) MAX_TTL * 1000000000L;
    while (itr.hasNext()) {
      final Map.Entry<FunctionBlacklistRule, Long> entry = itr.next();
      final long ttl = entry.getValue() - time;
      if (ttl <= 0) {
        itr.remove();
        onRemove(entry.getKey());
      } else {
        if (ttl < lowestTTL) {
          lowestTTL = ttl;
        }
      }
    }
    final int ttl = (int) ((lowestTTL + 999999999L) / 1000000000L);
    if (ttl < _minimumTTL) {
      synchronized (this) {
        if (ttl < _minimumTTL) {
          _cleaner.reschedule(ttl);
        }
      }
    }
  }

  /**
   * Called when a rule has been added to the set. This is provided so that a subclass may perform specific actions. This is called after the rule has been added.
   * 
   * @param rule the rule that was added to the set
   */
  protected void onAdd(final FunctionBlacklistRule rule) {
    s_logger.debug("Added rule {}", rule);
  }

  /**
   * Called when a rule added to the set is removed, perhaps because it's reached its expiry. This is provided so that a subclass may perform specific actions. This is called after the rule has been
   * removed.
   * 
   * @param rule the rule that was removed from the set
   */
  protected void onRemove(final FunctionBlacklistRule rule) {
    s_logger.debug("Removed rule {}", rule);
  }

}
