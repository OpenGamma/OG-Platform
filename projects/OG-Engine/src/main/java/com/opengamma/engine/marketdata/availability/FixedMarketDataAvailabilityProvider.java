/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 * Implements a {@link MarketDataAvailabilityProvider} around a fixed set of available market data items.
 * <p>
 * This is intended for constructing test cases only as the population of available items must be coupled to the target resolution strategy.
 */
public class FixedMarketDataAvailabilityProvider implements MarketDataAvailabilityProvider {

  private static final class TargetMarketData {

    private Set<String> _missing;
    private Map<String, ValueSpecification> _available;

    public void addAvailableData(final ValueSpecification valueSpecification) {
      if (_available == null) {
        _available = new HashMap<String, ValueSpecification>();
      }
      _available.put(valueSpecification.getValueName(), valueSpecification);
    }

    public void addMissingData(final String valueName) {
      if (_missing == null) {
        _missing = new HashSet<String>();
      }
      _missing.add(valueName);
    }

    public ValueSpecification getAvailability(final ValueRequirement desiredValue) {
      if ((_missing != null) && _missing.contains(desiredValue.getValueName())) {
        throw new MarketDataNotSatisfiableException(desiredValue);
      }
      if (_available != null) {
        return _available.get(desiredValue.getValueName());
      }
      return null;
    }

  }

  private final Map<ExternalId, TargetMarketData> _dataByEid;
  private final Map<UniqueId, TargetMarketData> _dataByUid;

  public FixedMarketDataAvailabilityProvider() {
    this(new HashMap<ExternalId, TargetMarketData>(), new HashMap<UniqueId, TargetMarketData>());
  }

  private FixedMarketDataAvailabilityProvider(final Map<ExternalId, TargetMarketData> dataByEid, final Map<UniqueId, TargetMarketData> dataByUid) {
    _dataByEid = dataByEid;
    _dataByUid = dataByUid;
  }

  protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final UniqueId identifier, final ValueRequirement desiredValue, final TargetMarketData data) {
    return data.getAvailability(desiredValue);
  }

  protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalId identifier, final ValueRequirement desiredValue, final TargetMarketData data) {
    return data.getAvailability(desiredValue);
  }

  @Override
  public ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) {
    ValueSpecification result;
    TargetMarketData data;
    synchronized (_dataByUid) {
      if (target instanceof UniqueIdentifiable) {
        final UniqueId identifier = ((UniqueIdentifiable) target).getUniqueId();
        data = _dataByUid.get(identifier);
        if (data != null) {
          result = getAvailability(targetSpec, identifier, desiredValue, data);
          if (result != null) {
            return result;
          }
        }
      }
    }
    synchronized (_dataByEid) {
      if (target instanceof ExternalIdentifiable) {
        final ExternalId identifier = ((ExternalIdentifiable) target).getExternalId();
        data = _dataByEid.get(identifier);
        if (data != null) {
          result = getAvailability(targetSpec, identifier, desiredValue, data);
          if (result != null) {
            return result;
          }
        }
      }
      if (target instanceof ExternalBundleIdentifiable) {
        for (final ExternalId identifier : ((ExternalBundleIdentifiable) target).getExternalIdBundle()) {
          data = _dataByEid.get(identifier);
          if (data != null) {
            result = getAvailability(targetSpec, identifier, desiredValue, data);
            if (result != null) {
              return result;
            }
          }
        }
      }
    }
    return null;
  }

  @Override
  public MarketDataAvailabilityFilter getAvailabilityFilter() {
    return new MarketDataAvailabilityFilter() {

      @Override
      public boolean isAvailable(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) {
        return getAvailability(targetSpec, target, desiredValue) != null;
      }

      @Override
      public MarketDataAvailabilityProvider withProvider(final MarketDataAvailabilityProvider provider) {
        return FixedMarketDataAvailabilityProvider.this.withProvider(provider);
      }

    };
  }

  protected MarketDataAvailabilityProvider withProvider(final MarketDataAvailabilityProvider provider) {
    final AbstractMarketDataAvailabilityProvider underlying = AbstractMarketDataAvailabilityProvider.of(provider);
    return new FixedMarketDataAvailabilityProvider(_dataByEid, _dataByUid) {

      @Override
      protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final UniqueId identifier, final ValueRequirement desiredValue, final TargetMarketData data) {
        if (super.getAvailability(targetSpec, identifier, desiredValue, data) != null) {
          return underlying.getAvailability(targetSpec, identifier, desiredValue);
        } else {
          return null;
        }
      }

      @Override
      protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalId identifier, final ValueRequirement desiredValue, final TargetMarketData data) {
        if (super.getAvailability(targetSpec, identifier, desiredValue, data) != null) {
          return underlying.getAvailability(targetSpec, identifier, desiredValue);
        } else {
          return null;
        }
      }

      @Override
      public MarketDataAvailabilityFilter getAvailabilityFilter() {
        return FixedMarketDataAvailabilityProvider.this.getAvailabilityFilter();
      }

    };
  }

  private TargetMarketData getOrCreateTargetMarketData(final ExternalId identifier) {
    TargetMarketData targetData = _dataByEid.get(identifier);
    if (targetData == null) {
      targetData = new TargetMarketData();
      _dataByEid.put(identifier, targetData);
    }
    return targetData;
  }

  private TargetMarketData getOrCreateTargetMarketData(final UniqueId identifier) {
    TargetMarketData targetData = _dataByUid.get(identifier);
    if (targetData == null) {
      targetData = new TargetMarketData();
      _dataByUid.put(identifier, targetData);
    }
    return targetData;
  }

  public void addAvailableData(final ExternalId identifier, final ValueSpecification valueSpecification) {
    ArgumentChecker.notNull(identifier, "identifier");
    ArgumentChecker.notNull(valueSpecification, "valueSpecification");
    synchronized (_dataByEid) {
      getOrCreateTargetMarketData(identifier).addAvailableData(valueSpecification);
    }
  }

  public void addAvailableData(final ExternalIdBundle identifiers, final ValueSpecification valueSpecification) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    ArgumentChecker.notNull(valueSpecification, "valueSpecification");
    synchronized (_dataByEid) {
      for (final ExternalId identifier : identifiers) {
        getOrCreateTargetMarketData(identifier).addAvailableData(valueSpecification);
      }
    }
  }

  public void addAvailableData(final ValueSpecification valueSpecification) {
    ArgumentChecker.notNull(valueSpecification, "valueSpecification");
    synchronized (_dataByUid) {
      getOrCreateTargetMarketData(valueSpecification.getTargetSpecification().getUniqueId()).addAvailableData(valueSpecification);
    }
  }

  public void addMissingData(final ExternalId identifier, final String valueName) {
    ArgumentChecker.notNull(identifier, "identifier");
    ArgumentChecker.notNull(valueName, "valueName");
    synchronized (_dataByEid) {
      getOrCreateTargetMarketData(identifier).addMissingData(valueName);
    }
  }

  public void addMissingData(final ExternalIdBundle identifiers, final String valueName) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    ArgumentChecker.notNull(valueName, "valueName");
    synchronized (_dataByEid) {
      for (final ExternalId identifier : identifiers) {
        getOrCreateTargetMarketData(identifier).addMissingData(valueName);
      }
    }
  }

  public void addMissingData(final UniqueId identifier, final String valueName) {
    ArgumentChecker.notNull(identifier, "identifier");
    ArgumentChecker.notNull(valueName, "valueName");
    synchronized (_dataByUid) {
      getOrCreateTargetMarketData(identifier).addMissingData(valueName);
    }
  }

}
