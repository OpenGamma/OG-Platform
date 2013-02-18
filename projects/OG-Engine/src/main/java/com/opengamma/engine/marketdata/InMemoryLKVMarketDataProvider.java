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

  private static class TargetData extends ConcurrentHashMap<String, Set<ValueSpecification>> {

    private static final long serialVersionUID = 1L;

    private final Set<ExternalId> _identifiers;

    public TargetData(final ValueSpecification initialValue) {
      final Set<ValueSpecification> values = new CopyOnWriteArraySet<ValueSpecification>();
      values.add(initialValue);
      put(initialValue.getValueName(), values);
      _identifiers = new CopyOnWriteArraySet<ExternalId>();
    }

    public TargetData(final ExternalIdBundle identifiers) {
      _identifiers = new CopyOnWriteArraySet<ExternalId>(identifiers.getExternalIds());
    }

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

    public Set<ExternalId> getIdentifiers() {
      return _identifiers;
    }

  }

  private final String _syntheticScheme = "InMemoryLKV" + s_nextIdentifier.getAndIncrement();
  private final AtomicInteger _nextSyntheticIdentifier = new AtomicInteger();
  private final Map<ValueSpecification, Object> _lastKnownValues = new ConcurrentHashMap<ValueSpecification, Object>();
  private final ConcurrentMap<ExternalId, ComputationTargetSpecification> _weakIndex = new ConcurrentHashMap<ExternalId, ComputationTargetSpecification>();
  private final ConcurrentMap<ComputationTargetSpecification, TargetData> _strictIndex = new ConcurrentHashMap<ComputationTargetSpecification, TargetData>();
  private final MarketDataPermissionProvider _permissionProvider;
  private final MarketDataAvailabilityProvider _availability = new AbstractMarketDataAvailabilityProvider() {

    @Override
    protected MarketDataAvailabilityProvider withDelegate(final Delegate delegate) {
      assert false;
      throw new UnsupportedOperationException();
    }

    @Override
    protected ValueSpecification getAvailability(final ComputationTargetSpecification key, final ValueRequirement desiredValue) {
      final TargetData values = _strictIndex.get(key);
      if (values != null) {
        return values.getAvailability(desiredValue);
      } else {
        return null;
      }
    }

    private ValueSpecification getAvailability(final ExternalId key, final ValueRequirement desiredValue) {
      final ComputationTargetSpecification target = _weakIndex.get(key);
      if (target != null) {
        return getAvailability(target, desiredValue);
      } else {
        return null;
      }
    }

    @Override
    protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalId identifier, final ValueRequirement desiredValue) {
      ValueSpecification available = getAvailability(targetSpec, desiredValue);
      if (available == null) {
        available = getAvailability(identifier, desiredValue);
      }
      return available;
    }

    @Override
    protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalIdBundle identifiers, final ValueRequirement desiredValue) {
      ValueSpecification available = getAvailability(targetSpec, desiredValue);
      if (available == null) {
        for (final ExternalId identifier : identifiers) {
          available = getAvailability(identifier, desiredValue);
          if (available != null) {
            break;
          }
        }
      }
      return available;
    }

    @Override
    protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final UniqueId identifier, final ValueRequirement desiredValue) {
      return getAvailability(targetSpec, desiredValue);
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
        final ComputationTargetSpecification weakSpec = _weakIndex.get(identifier);
        if (weakSpec != null) {
          if (found != null) {
            if (!update) {
              update = !found.equals(weakSpec);
            }
          } else {
            found = weakSpec;
          }
        } else {
          update = true;
        }
      }
      if (found == null) {
        found = requirement.replaceIdentifier(UniqueId.of(_syntheticScheme, Integer.toString(_nextSyntheticIdentifier.getAndIncrement())));
        final TargetData data = new TargetData(requirement.getIdentifiers());
        _strictIndex.put(found, data);
      }
      if (update) {
        for (final ExternalId identifier : requirement.getIdentifiers()) {
          _weakIndex.put(identifier, found);
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
    TargetData data = _strictIndex.get(specification.getTargetSpecification());
    if (data == null) {
      data = new TargetData(specification);
      final TargetData existing = _strictIndex.putIfAbsent(specification.getTargetSpecification(), data);
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
    final TargetData data = _strictIndex.get(specification.getTargetSpecification());
    if (data != null) {
      data.removeValue(specification);
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
