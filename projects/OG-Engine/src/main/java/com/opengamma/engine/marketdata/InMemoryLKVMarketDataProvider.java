/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.marketdata.availability.AbstractMarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.target.ComputationTargetReferenceVisitor;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;

/**
 * An implementation of {@link MarketDataProvider} which maintains an LKV cache of externally-provided values.
 */
public class InMemoryLKVMarketDataProvider extends AbstractMarketDataProvider implements MarketDataInjector {

  private static final Logger s_logger = LoggerFactory.getLogger(InMemoryLKVMarketDataProvider.class);
  private static final AtomicInteger s_nextIdentifier = new AtomicInteger();

  private abstract static class TargetData extends ConcurrentHashMap<String, Set<ValueSpecification>> {

    private static final long serialVersionUID = 1L;

    public ValueSpecification getAvailability(final ValueRequirement desiredValue) {
      final Set<ValueSpecification> specs = get(desiredValue.getValueName());
      if (specs != null) {
        for (final ValueSpecification spec : specs) {
          if (desiredValue.getConstraints().isSatisfiedBy(spec.getProperties())) {
            return spec;
          }
        }
      }
      return null;
    }

    public void addValue(final ValueSpecification specification) {
      Set<ValueSpecification> values = get(specification.getValueName());
      if (values == null) {
        values = new CopyOnWriteArraySet<ValueSpecification>();
        values.add(specification);
        final Set<ValueSpecification> existing = putIfAbsent(specification.getValueName(), values);
        if (existing != null) {
          existing.add(specification);
        }
      } else {
        values.add(specification);
      }
    }

    public void removeValue(final ValueSpecification specification) {
      final Set<ValueSpecification> values = get(specification.getValueName());
      if (values != null) {
        values.remove(specification);
      }
    }

  }

  private static final class WeakTargetData extends TargetData {

    private static final long serialVersionUID = 1L;

    private volatile ComputationTargetSpecification _specification;

    public WeakTargetData(final ComputationTargetSpecification specification) {
      _specification = specification;
    }

    public ComputationTargetSpecification getSpecification() {
      return _specification;
    }

    public void setSpecification(final ComputationTargetSpecification specification) {
      _specification = specification;
    }

  }

  private static final class StrictTargetData extends TargetData {

    private static final long serialVersionUID = 1L;

    private final Set<ExternalId> _identifiers;

    public StrictTargetData(final ValueSpecification initialValue) {
      final Set<ValueSpecification> values = new CopyOnWriteArraySet<ValueSpecification>();
      values.add(initialValue);
      put(initialValue.getValueName(), values);
      _identifiers = new CopyOnWriteArraySet<ExternalId>();
    }

    public StrictTargetData(final ExternalIdBundle identifiers) {
      _identifiers = new CopyOnWriteArraySet<ExternalId>(identifiers.getExternalIds());
    }

    public Set<ExternalId> getIdentifiers() {
      return _identifiers;
    }

  }

  private final String _syntheticScheme = "InMemoryLKV" + s_nextIdentifier.getAndIncrement();
  private final AtomicInteger _nextSyntheticIdentifier = new AtomicInteger();
  private final Map<ValueSpecification, Object> _lastKnownValues = new ConcurrentHashMap<ValueSpecification, Object>();
  private final ConcurrentMap<ExternalId, WeakTargetData> _weakIndex = new ConcurrentHashMap<ExternalId, WeakTargetData>();
  private final ConcurrentMap<ComputationTargetSpecification, StrictTargetData> _strictIndex = new ConcurrentHashMap<ComputationTargetSpecification, StrictTargetData>();
  private final MarketDataPermissionProvider _permissionProvider;
  private final MarketDataAvailabilityProvider _availability = new AbstractMarketDataAvailabilityProvider() {

    @Override
    protected MarketDataAvailabilityProvider withDelegate(final Delegate delegate) {
      assert false;
      throw new UnsupportedOperationException();
    }

    private <T> ValueSpecification getAvailability(final Map<T, ? extends TargetData> data, final T key, final ValueRequirement desiredValue) {
      final TargetData values = data.get(key);
      if (values != null) {
        return values.getAvailability(desiredValue);
      } else {
        return null;
      }
    }

    @Override
    protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalId identifier, final ValueRequirement desiredValue) {
      ValueSpecification available = getAvailability(_strictIndex, targetSpec, desiredValue);
      if (available == null) {
        available = getAvailability(_weakIndex, identifier, desiredValue);
      }
      return available;
    }

    @Override
    protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalIdBundle identifiers, final ValueRequirement desiredValue) {
      ValueSpecification available = getAvailability(_strictIndex, targetSpec, desiredValue);
      if (available == null) {
        for (final ExternalId identifier : identifiers) {
          available = getAvailability(_weakIndex, identifier, desiredValue);
          if (available != null) {
            break;
          }
        }
      }
      return available;
    }

    @Override
    protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final UniqueId identifier, final ValueRequirement desiredValue) {
      return getAvailability(_strictIndex, targetSpec, desiredValue);
    }

  };
  private final ComputationTargetReferenceVisitor<ComputationTargetSpecification> _getTargetSpecification = new ComputationTargetReferenceVisitor<ComputationTargetSpecification>() {

    @Override
    public ComputationTargetSpecification visitComputationTargetRequirement(final ComputationTargetRequirement requirement) {
      if (requirement.getIdentifiers().isEmpty()) {
        return ComputationTargetSpecification.NULL;
      }
      ComputationTargetSpecification found = null;
      boolean update = false;
      for (final ExternalId identifier : requirement.getIdentifiers()) {
        final WeakTargetData data = _weakIndex.get(identifier);
        if (data != null) {
          if (found != null) {
            if (!update) {
              update = found != data.getSpecification();
            }
          } else {
            found = data.getSpecification();
          }
        } else {
          update = true;
        }
      }
      if (found == null) {
        found = requirement.replaceIdentifier(UniqueId.of(_syntheticScheme, Integer.toString(_nextSyntheticIdentifier.getAndIncrement())));
        final StrictTargetData data = new StrictTargetData(requirement.getIdentifiers());
        _strictIndex.put(found, data);
      }
      if (update) {
        for (final ExternalId identifier : requirement.getIdentifiers()) {
          final WeakTargetData data = _weakIndex.get(identifier);
          if (data == null) {
            _weakIndex.putIfAbsent(identifier, new WeakTargetData(found));
          } else {
            data.setSpecification(found);
          }
        }
      }
      return found;
    }

    @Override
    public ComputationTargetSpecification visitComputationTargetSpecification(final ComputationTargetSpecification specification) {
      return specification;
    }

  };

  /**
   * Constructs an instance.
   */
  public InMemoryLKVMarketDataProvider() {
    _permissionProvider = new PermissiveMarketDataPermissionProvider();
  }

  //-------------------------------------------------------------------------
  @Override
  public void subscribe(final ValueSpecification valueSpecification) {
    subscribe(Collections.singleton(valueSpecification));
  }

  @Override
  public void subscribe(final Set<ValueSpecification> valueSpecifications) {
    // No actual subscription to make, but we still need to acknowledge it.
    s_logger.debug("Added subscriptions to {}", valueSpecifications);
    subscriptionSucceeded(valueSpecifications);
  }

  @Override
  public void unsubscribe(final ValueSpecification valueSpecification) {
    unsubscribe(Collections.singleton(valueSpecification));
  }

  @Override
  public void unsubscribe(final Set<ValueSpecification> valueSpecifications) {
    // No actual unsubscription to make
    s_logger.debug("Unsubscribed from {}", valueSpecifications);
  }

  @Override
  public MarketDataAvailabilityProvider getAvailabilityProvider() {
    return _availability;
  }

  @Override
  public MarketDataPermissionProvider getPermissionProvider() {
    return _permissionProvider;
  }

  @Override
  public boolean isCompatible(final MarketDataSpecification marketDataSpec) {
    return true;
  }

  @Override
  public InMemoryLKVMarketDataSnapshot snapshot(final MarketDataSpecification marketDataSpec) {
    return new InMemoryLKVMarketDataSnapshot(this);
  }

  @Override
  public void addValue(final ValueSpecification specification, final Object value) {
    _lastKnownValues.put(specification, value);
    StrictTargetData data = _strictIndex.get(specification.getTargetSpecification());
    if (data == null) {
      data = new StrictTargetData(specification);
      final StrictTargetData existing = _strictIndex.putIfAbsent(specification.getTargetSpecification(), data);
      if (existing != null) {
        existing.addValue(specification);
      }
    } else {
      data.addValue(specification);
    }
    valueChanged(specification);
  }

  @Override
  public void addValue(final ValueRequirement requirement, final Object value) {
    addValue(resolveRequirement(requirement), value);
  }

  @Override
  public void removeValue(final ValueSpecification specification) {
    final StrictTargetData data = _strictIndex.get(specification.getTargetSpecification());
    if (data != null) {
      data.removeValue(specification);
      for (final ExternalId identifier : data.getIdentifiers()) {
        final ConcurrentMap<String, Set<ValueSpecification>> values = _weakIndex.get(identifier);
        if (values != null) {
          final Set<ValueSpecification> specs = values.get(specification.getValueName());
          if (specs != null) {
            specs.remove(specification);
          }
        }
      }
    }
    _lastKnownValues.remove(specification);
    valueChanged(specification);
  }

  @Override
  public void removeValue(final ValueRequirement valueRequirement) {
    removeValue(resolveRequirement(valueRequirement));
  }

  //-------------------------------------------------------------------------
  public Set<ValueSpecification> getAllValueKeys() {
    return Collections.unmodifiableSet(_lastKnownValues.keySet());
  }

  public Object getCurrentValue(final ValueSpecification specification) {
    return _lastKnownValues.get(specification);
  }

  //-------------------------------------------------------------------------

  /*package*/Map<ValueSpecification, Object> doSnapshot() {
    return new HashMap<ValueSpecification, Object>(_lastKnownValues);
  }

  protected ValueSpecification resolveRequirement(final ValueRequirement requirement) {
    final ComputationTargetSpecification targetSpec = requirement.getTargetReference().accept(_getTargetSpecification);
    return new ValueSpecification(requirement.getValueName(), targetSpec, requirement.getConstraints().copy().withoutAny(ValuePropertyNames.FUNCTION)
        .with(ValuePropertyNames.FUNCTION, MarketDataSourcingFunction.UNIQUE_ID).get());
  }

}
