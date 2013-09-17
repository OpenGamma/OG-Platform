/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.target.ComputationTargetReferenceVisitor;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Implements a {@link MarketDataAvailabilityProvider} around a fixed set of available market data items.
 */
public class FixedMarketDataAvailabilityProvider extends AbstractMarketDataAvailabilityProvider {

  private static final AtomicInteger s_nextIdentifier = new AtomicInteger();

  private static class TargetData extends ConcurrentHashMap<String, Set<ValueSpecification>> {
    
    private static final long serialVersionUID = 1L;

    public TargetData(final ValueSpecification initialValue) {
      final Set<ValueSpecification> values = new CopyOnWriteArraySet<ValueSpecification>();
      values.add(initialValue);
      put(initialValue.getValueName(), values);
    }

    public TargetData() {
    }

    public ValueSpecification getAvailability(final ValueRequirement desiredValue) {
      final Set<ValueSpecification> specs = get(desiredValue.getValueName());
      if (specs != null) {
        final ValueProperties constraints = desiredValue.getConstraints().withoutAny(ValuePropertyNames.FUNCTION);
        for (final ValueSpecification spec : specs) {
          if (constraints.isSatisfiedBy(spec.getProperties())) {
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

  private final String _syntheticScheme = "InMemoryLKV" + s_nextIdentifier.getAndIncrement();
  private final AtomicInteger _nextSyntheticIdentifier = new AtomicInteger();
  private final ConcurrentMap<ExternalId, ComputationTargetSpecification> _weakIndex;
  private final ConcurrentMap<ComputationTargetSpecification, TargetData> _strictIndex;
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
        final TargetData data = new TargetData();
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

  public FixedMarketDataAvailabilityProvider() {
    this(new ConcurrentHashMap<ExternalId, ComputationTargetSpecification>(), new ConcurrentHashMap<ComputationTargetSpecification, TargetData>());
  }

  private FixedMarketDataAvailabilityProvider(final ConcurrentMap<ExternalId, ComputationTargetSpecification> weakIndex, final ConcurrentMap<ComputationTargetSpecification, TargetData> strictIndex) {
    _weakIndex = weakIndex;
    _strictIndex = strictIndex;
  }

  protected ValueSpecification getAvailabilityImpl(final ComputationTargetSpecification targetSpec, final ValueRequirement desiredValue) {
    if (targetSpec != null) {
      final TargetData values = _strictIndex.get(targetSpec);
      if (values != null) {
        return values.getAvailability(desiredValue);
      }
    }
    return null;
  }

  protected ValueSpecification getAvailabilityImpl(final ExternalId key, final ValueRequirement desiredValue) {
    final ComputationTargetSpecification target = _weakIndex.get(key);
    if (target != null) {
      return getAvailabilityImpl(target, desiredValue);
    } else {
      return null;
    }
  }

  @Override
  protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalId identifier, final ValueRequirement desiredValue) {
    ValueSpecification available = getAvailabilityImpl(targetSpec, desiredValue);
    if (available == null) {
      available = getAvailabilityImpl(identifier, desiredValue);
    }
    return available;
  }

  @Override
  protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalIdBundle identifiers, final ValueRequirement desiredValue) {
    ValueSpecification available = getAvailabilityImpl(targetSpec, desiredValue);
    if (available == null) {
      for (final ExternalId identifier : identifiers) {
        available = getAvailabilityImpl(identifier, desiredValue);
        if (available != null) {
          break;
        }
      }
    }
    return available;
  }

  @Override
  protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final UniqueId identifier, final ValueRequirement desiredValue) {
    return getAvailabilityImpl(targetSpec, desiredValue);
  }

  @Override
  protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ValueRequirement desiredValue) {
    return getAvailabilityImpl(targetSpec, desiredValue);
  }

  /**
   * Returns the {@link ValueSpecification} that is a possible resolution of the value requirement. This will be the specification used by {@link #addValue(ValueRequirement,Object)} or
   * {@link #removeValue(ValueRequirement)}.
   * 
   * @param requirement the requirement to resolve, not null
   * @return the resolved {@code ValueSpecification}, not null
   */
  public ValueSpecification resolveRequirement(final ValueRequirement requirement) {
    final ComputationTargetSpecification targetSpec = requirement.getTargetReference().accept(_getTargetSpecification);
    return new ValueSpecification(requirement.getValueName(), targetSpec, requirement.getConstraints().copy().withoutAny(ValuePropertyNames.FUNCTION)
        .with(ValuePropertyNames.FUNCTION, MarketDataSourcingFunction.UNIQUE_ID).get());
  }

  protected void addAvailableData(final ComputationTargetSpecification target, final ValueSpecification valueSpecification) {
    TargetData data = _strictIndex.get(target);
    if (data == null) {
      data = new TargetData(valueSpecification);
      final TargetData existing = _strictIndex.putIfAbsent(target, data);
      if (existing != null) {
        existing.addValue(valueSpecification);
      }
    } else {
      data.addValue(valueSpecification);
    }
  }

  protected void removeAvailableData(final ComputationTargetSpecification target, final ValueSpecification valueSpecification) {
    final TargetData data = _strictIndex.get(target);
    if (data != null) {
      data.removeValue(valueSpecification);
    }
  }

  public void addAvailableData(final ExternalId identifier, final ValueSpecification valueSpecification) {
    ArgumentChecker.notNull(identifier, "identifier");
    ArgumentChecker.notNull(valueSpecification, "valueSpecification");
    final ComputationTargetSpecification target = valueSpecification.getTargetSpecification();
    addAvailableData(target, valueSpecification);
    _weakIndex.put(identifier, target);
  }

  public void addAvailableData(final ExternalIdBundle identifiers, final ValueSpecification valueSpecification) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    ArgumentChecker.notNull(valueSpecification, "valueSpecification");
    final ComputationTargetSpecification target = valueSpecification.getTargetSpecification();
    addAvailableData(target, valueSpecification);
    for (final ExternalId identifier : identifiers) {
      _weakIndex.put(identifier, target);
    }
  }

  public void addAvailableData(final ValueSpecification valueSpecification) {
    ArgumentChecker.notNull(valueSpecification, "valueSpecification");
    addAvailableData(valueSpecification.getTargetSpecification(), valueSpecification);
  }

  public void removeAvailableData(final ValueSpecification valueSpecification) {
    ArgumentChecker.notNull(valueSpecification, "valueSpecification");
    removeAvailableData(valueSpecification.getTargetSpecification(), valueSpecification);
  }
  
  
  @Override
  public Serializable getAvailabilityHintKey() {
    final ArrayList<Serializable> key = new ArrayList<Serializable>(2);
    key.add(getClass().getName());
    key.add(_syntheticScheme);
    return key;
  }

}
