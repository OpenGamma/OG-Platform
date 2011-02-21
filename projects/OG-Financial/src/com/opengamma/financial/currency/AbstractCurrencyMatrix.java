/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.opengamma.core.common.CurrencyUnit;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * A simple base class for a {@link CurrencyMatrix}.
 */
public abstract class AbstractCurrencyMatrix implements CurrencyMatrix, MutableUniqueIdentifiable {

  private final ConcurrentHashMap<CurrencyUnit, ConcurrentHashMap<CurrencyUnit, CurrencyMatrixValue>> _values = new ConcurrentHashMap<CurrencyUnit, ConcurrentHashMap<CurrencyUnit, CurrencyMatrixValue>>();
  private final ConcurrentHashMap<CurrencyUnit, AtomicInteger> _targets = new ConcurrentHashMap<CurrencyUnit, AtomicInteger>();

  private UniqueIdentifier _uniqueId;

  // MutableUniqueIdentifiable

  @Override
  public void setUniqueId(final UniqueIdentifier uniqueId) {
    _uniqueId = uniqueId;
  }

  // UniqueIdentifiable

  @Override
  public UniqueIdentifier getUniqueId() {
    return _uniqueId;
  }

  // CurrencyMatrix

  @SuppressWarnings("unchecked")
  @Override
  public Set<CurrencyUnit> getSourceCurrencies() {
    return Collections.unmodifiableSet(_values.keySet());
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<CurrencyUnit> getTargetCurrencies() {
    return Collections.unmodifiableSet(_targets.keySet());
  }

  @Override
  public CurrencyMatrixValue getConversion(final CurrencyUnit source, final CurrencyUnit target) {
    if (source.equals(target)) {
      // This shouldn't happen in sensible code
      return CurrencyMatrixValue.of(1.0);
    }
    ConcurrentHashMap<CurrencyUnit, CurrencyMatrixValue> conversions = _values.get(source);
    if (conversions == null) {
      return null;
    } else {
      return conversions.get(target);
    }
  }

  // Helper methods for sub-classes

  protected void addConversion(final CurrencyUnit source, final CurrencyUnit target, final CurrencyMatrixValue rate) {
    ArgumentChecker.notNull(source, "source");
    ArgumentChecker.notNull(target, "target");
    ArgumentChecker.notNull(rate, "rate");
    ConcurrentHashMap<CurrencyUnit, CurrencyMatrixValue> conversions = _values.get(source);
    if (conversions == null) {
      conversions = new ConcurrentHashMap<CurrencyUnit, CurrencyMatrixValue>();
      final ConcurrentHashMap<CurrencyUnit, CurrencyMatrixValue> newConversions = _values.putIfAbsent(source, conversions);
      if (newConversions != null) {
        conversions = newConversions;
      }
    }
    if (conversions.put(target, rate) == null) {
      // Added something to the map, so increase the target's reference count
      AtomicInteger targetCount = _targets.get(target);
      if (targetCount == null) {
        targetCount = new AtomicInteger(1);
        targetCount = _targets.putIfAbsent(target, targetCount);
        if (targetCount != null) {
          // Another thread already inserted the reference count
          if (targetCount.incrementAndGet() == 1) {
            // Another thread may have removed the last reference, confirm and re-insert atomically against "remove"
            synchronized (targetCount) {
              if (targetCount.get() > 0) {
                _targets.putIfAbsent(target, targetCount);
              }
            }
          }
        }
      } else {
        if (targetCount.incrementAndGet() == 1) {
          // Another thread may have removed the last reference, confirm and re-insert atomically against "remove"
          synchronized (targetCount) {
            if (targetCount.get() > 0) {
              _targets.putIfAbsent(target, targetCount);
            }
          }
        }
      }
    }
  }

  protected CurrencyMatrixValue removeConversion(final CurrencyUnit source, final CurrencyUnit target) {
    ArgumentChecker.notNull(source, "source");
    ArgumentChecker.notNull(target, "target");
    ConcurrentHashMap<CurrencyUnit, CurrencyMatrixValue> conversions = _values.get(source);
    if (conversions == null) {
      // Nothing from that source
      return null;
    }
    final CurrencyMatrixValue value = conversions.remove(target);
    if (value == null) {
      // No conversion from source to target
      return null;
    }
    // Removed a value, so need to decrease the target's reference count
    AtomicInteger targetCount = _targets.get(target);
    if (targetCount != null) {
      // Target count should never be null at this point
      if (targetCount.decrementAndGet() == 0) {
        // This was the last reference to the target, confirm and remove atomically against the "add" operation
        synchronized (targetCount) {
          if (targetCount.get() == 0) {
            _targets.remove(target);
          }
        }
      }
    }
    return value;
  }

}
