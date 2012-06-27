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

/**
 * Manages a set of {@link FunctionBlacklistRule}s with expiry times.
 */
public class FunctionBlacklistRuleSet implements Set<FunctionBlacklistRule> {

  private static final Logger s_logger = LoggerFactory.getLogger(FunctionBlacklistRuleSet.class);

  private static final long MIN_TTL = 5 * 60;
  private final Map<FunctionBlacklistRule, Long> _rules = new ConcurrentHashMap<FunctionBlacklistRule, Long>();
  private final long _minimumTTL;

  private static class Cleaner implements Runnable {

    private final WeakReference<FunctionBlacklistRuleSet> _ref;
    private final Future<?> _future;

    public Cleaner(final FunctionBlacklistRuleSet ref, final ScheduledExecutorService executor, final long minimumTTL) {
      _ref = new WeakReference<FunctionBlacklistRuleSet>(ref);
      _future = executor.scheduleWithFixedDelay(this, minimumTTL, minimumTTL, TimeUnit.SECONDS);
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
    this(executor, MIN_TTL);
  }

  public FunctionBlacklistRuleSet(final ScheduledExecutorService executor, final long minimumTTL) {
    new Cleaner(this, executor, minimumTTL);
    _minimumTTL = minimumTTL;
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
    add(e, MIN_TTL);
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

  public void add(final FunctionBlacklistRule rule, final long timeToLive) {
    final long expiry = System.nanoTime() + timeToLive * 1000000000L;
    if (_rules.put(rule, expiry) == null) {
      onAdd(rule);
    }
  }

  protected void clean() {
    final long time = System.nanoTime();
    final Iterator<Map.Entry<FunctionBlacklistRule, Long>> itr = _rules.entrySet().iterator();
    while (itr.hasNext()) {
      final Map.Entry<FunctionBlacklistRule, Long> entry = itr.next();
      if (entry.getValue() - time <= 0) {
        itr.remove();
        onRemove(entry.getKey());
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
