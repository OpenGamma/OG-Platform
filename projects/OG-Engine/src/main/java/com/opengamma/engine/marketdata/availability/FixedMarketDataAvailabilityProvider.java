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
 * Implements {@link MarketDataAvailabilityProvider} around a fixed set of available market data items.
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

  private final Map<ExternalId, TargetMarketData> _dataByEid = new HashMap<ExternalId, TargetMarketData>();
  private final Map<UniqueId, TargetMarketData> _dataByUid = new HashMap<UniqueId, TargetMarketData>();

  @Override
  public synchronized ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) {
    ValueSpecification result;
    TargetMarketData data;
    if (target instanceof UniqueIdentifiable) {
      data = _dataByUid.get(((UniqueIdentifiable) target).getUniqueId());
      if (data != null) {
        result = data.getAvailability(desiredValue);
        if (result != null) {
          return result;
        }
      }
    }
    if (target instanceof ExternalIdentifiable) {
      data = _dataByEid.get(((ExternalIdentifiable) target).getExternalId());
      if (data != null) {
        result = data.getAvailability(desiredValue);
        if (result != null) {
          return result;
        }
      }
    }
    if (target instanceof ExternalBundleIdentifiable) {
      for (final ExternalId identifier : ((ExternalBundleIdentifiable) target).getExternalIdBundle()) {
        data = _dataByEid.get(identifier);
        if (data != null) {
          result = data.getAvailability(desiredValue);
          if (result != null) {
            return result;
          }
        }
      }
    }
    return null;
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

  public synchronized void addAvailableData(final ExternalId identifier, final ValueSpecification valueSpecification) {
    ArgumentChecker.notNull(identifier, "identifier");
    ArgumentChecker.notNull(valueSpecification, "valueSpecification");
    getOrCreateTargetMarketData(identifier).addAvailableData(valueSpecification);
  }

  public synchronized void addAvailableData(final ExternalIdBundle identifiers, final ValueSpecification valueSpecification) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    ArgumentChecker.notNull(valueSpecification, "valueSpecification");
    for (final ExternalId identifier : identifiers) {
      getOrCreateTargetMarketData(identifier).addAvailableData(valueSpecification);
    }
  }

  public synchronized void addAvailableData(final ValueSpecification valueSpecification) {
    ArgumentChecker.notNull(valueSpecification, "valueSpecification");
    getOrCreateTargetMarketData(valueSpecification.getTargetSpecification().getUniqueId()).addAvailableData(valueSpecification);
  }

  public synchronized void addMissingData(final ExternalId identifier, final String valueName) {
    ArgumentChecker.notNull(identifier, "identifier");
    ArgumentChecker.notNull(valueName, "valueName");
    getOrCreateTargetMarketData(identifier).addMissingData(valueName);
  }

  public synchronized void addMissingData(final ExternalIdBundle identifiers, final String valueName) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    ArgumentChecker.notNull(valueName, "valueName");
    for (final ExternalId identifier : identifiers) {
      getOrCreateTargetMarketData(identifier).addMissingData(valueName);
    }
  }

  public synchronized void addMissingData(final UniqueId identifier, final String valueName) {
    ArgumentChecker.notNull(identifier, "identifier");
    ArgumentChecker.notNull(valueName, "valueName");
    getOrCreateTargetMarketData(identifier).addMissingData(valueName);
  }

}
