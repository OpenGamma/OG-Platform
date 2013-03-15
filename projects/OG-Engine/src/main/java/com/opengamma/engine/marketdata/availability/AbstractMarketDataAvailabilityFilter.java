/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;

/**
 * A partial implementation of {@link MarketDataAvailabilityFilter}.
 */
public abstract class AbstractMarketDataAvailabilityFilter implements MarketDataAvailabilityFilter {

  /**
   * Tests the availability of an item that can be referenced by external identifier.
   * <p>
   * The default implementation here returns false.
   * 
   * @param targetSpec the target specification as passed to {@link MarketDataAvailabilityProvider#getAvailability}, possibly null
   * @param identifier the external identifier of the target object, not null
   * @param desiredValue the requested value to test and resolve, not null
   * @return true if the value is available, false otherwise
   */
  protected boolean isAvailable(final ComputationTargetSpecification targetSpec, final ExternalId identifier, final ValueRequirement desiredValue) {
    return false;
  }

  protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalId identifier, final ValueRequirement desiredValue,
      final AbstractMarketDataAvailabilityProvider underlying) {
    if (isAvailable(targetSpec, identifier, desiredValue)) {
      return underlying.getAvailability(targetSpec, identifier, desiredValue);
    } else {
      return null;
    }
  }

  /**
   * Tests the availability of an item that can be referenced by one or more external identifiers.
   * 
   * @param targetSpec the target specification as passed to {@link MarketDataAvailabilityProvider#getAvailability}, possibly null
   * @param identifiers the external identifiers of the target object, not null
   * @param desiredValue the requested value to test and resolve, not null
   * @return true if the value is available, false otherwise
   */
  protected final boolean isAvailable(final ComputationTargetSpecification targetSpec, final ExternalIdBundle identifiers, final ValueRequirement desiredValue) {
    for (final ExternalId identifier : identifiers) {
      if (isAvailable(targetSpec, identifier, desiredValue)) {
        return true;
      }
    }
    return false;
  }

  protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalIdBundle identifiers, final ValueRequirement desiredValue,
      final AbstractMarketDataAvailabilityProvider underlying) {
    List<ExternalId> acceptable = null;
    for (final ExternalId identifier : identifiers) {
      if (isAvailable(targetSpec, identifier, desiredValue)) {
        if (acceptable == null) {
          acceptable = new ArrayList<ExternalId>();
        }
        acceptable.add(identifier);
      }
    }
    if (acceptable != null) {
      if (acceptable.size() > 1) {
        return underlying.getAvailability(targetSpec, ExternalIdBundle.of(acceptable), desiredValue);
      } else {
        return underlying.getAvailability(targetSpec, acceptable.get(0), desiredValue);
      }
    } else {
      return null;
    }
  }

  /**
   * Tests the availability of an item that can only be referenced by unique identifier
   * <p>
   * The default implementation here returns false.
   * 
   * @param targetSpec the target specification as passed to {@link MarketDataAvailabilityProvider#getAvailability}, possibly null
   * @param identifier the unique identifier of the target object, null only if {@code targetSpec} is {@link ComputationTargetType#NULL}
   * @param desiredValue the requested value to test and resolve, not null
   * @return true if the value is available, false otherwise
   */
  protected boolean isAvailable(final ComputationTargetSpecification targetSpec, final UniqueId identifier, final ValueRequirement desiredValue) {
    return false;
  }

  protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final UniqueId identifier, final ValueRequirement desiredValue,
      final AbstractMarketDataAvailabilityProvider underlying) {
    if (isAvailable(targetSpec, identifier, desiredValue)) {
      return underlying.getAvailability(targetSpec, identifier, desiredValue);
    } else {
      return null;
    }
  }

  /**
   * Tests the availability of the null target.
   * <p>
   * The default implementation here calls the {@link #isAvailable(ComputationTargetSpecification,UniqueId,ValueRequirement)} form.
   * 
   * @param targetSpec the target specification, always {@link ComputationTargetSpecification#NULL}
   * @param desiredValue the requested value to test and resolve, not null
   * @return true if the value is available, false otherwise
   */
  protected boolean isAvailable(final ComputationTargetSpecification targetSpec, final ValueRequirement desiredValue) {
    return isAvailable(targetSpec, (UniqueId) null, desiredValue);
  }

  protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ValueRequirement desiredValue, final AbstractMarketDataAvailabilityProvider underlying) {
    if (isAvailable(targetSpec, desiredValue)) {
      return underlying.getAvailability(targetSpec, desiredValue);
    } else {
      return null;
    }
  }

  /**
   * Tests how the target can be referenced and defers to one of the other {@code isAvailable} methods.
   * 
   * @param targetSpec the target specification as passed to {@link MarketDataAvailabilityFilter#isAvailable}, possibly null
   * @param target the target to evaluate, not null
   * @param desiredValue the requested value to test and resolve, not null
   * @return true if the value is available, false otherwise
   */
  @Override
  public final boolean isAvailable(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) {
    if (target instanceof ExternalBundleIdentifiable) {
      if (isAvailable(targetSpec, ((ExternalBundleIdentifiable) target).getExternalIdBundle(), desiredValue)) {
        return true;
      }
    }
    if (target instanceof ExternalIdentifiable) {
      if (isAvailable(targetSpec, ((ExternalIdentifiable) target).getExternalId(), desiredValue)) {
        return true;
      }
    }
    if (target instanceof UniqueIdentifiable) {
      if (isAvailable(targetSpec, ((UniqueIdentifiable) target).getUniqueId(), desiredValue)) {
        return true;
      }
    }
    if (target == null) {
      return isAvailable(targetSpec, desiredValue);
    } else {
      return false;
    }
  }

  /**
   * Updates a collection containing keys that will be used to form the cache hint when this is used to construct an availability provider. A sub-class should put any construction parameters into the
   * key that distinguish its behavior from other filters of the same class.
   * 
   * @param key the key to update
   */
  protected abstract void populateAvailabilityHintKey(Collection<Serializable> key);

  @Override
  public MarketDataAvailabilityProvider withProvider(final MarketDataAvailabilityProvider provider) {
    final AbstractMarketDataAvailabilityProvider underlying = AbstractMarketDataAvailabilityProvider.of(provider);
    return new AbstractMarketDataAvailabilityProvider() {

      @Override
      public MarketDataAvailabilityFilter getAvailabilityFilter() {
        return AbstractMarketDataAvailabilityFilter.this;
      }

      @Override
      protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalId identifier, final ValueRequirement desiredValue) {
        return AbstractMarketDataAvailabilityFilter.this.getAvailability(targetSpec, identifier, desiredValue, underlying);
      }

      @Override
      protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalIdBundle identifiers, final ValueRequirement desiredValue) {
        return AbstractMarketDataAvailabilityFilter.this.getAvailability(targetSpec, identifiers, desiredValue, underlying);
      }

      @Override
      protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final UniqueId identifier, final ValueRequirement desiredValue) {
        return AbstractMarketDataAvailabilityFilter.this.getAvailability(targetSpec, identifier, desiredValue, underlying);
      }

      @Override
      protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ValueRequirement desiredValue) {
        return AbstractMarketDataAvailabilityFilter.this.getAvailability(targetSpec, desiredValue, underlying);
      }

      @Override
      public Serializable getAvailabilityHintKey() {
        final ArrayList<Serializable> key = new ArrayList<Serializable>(5);
        key.add(getClass().getName());
        key.add(AbstractMarketDataAvailabilityFilter.this.getClass().getName());
        key.add(provider.getAvailabilityHintKey());
        key.trimToSize();
        return key;
      }

    };
  }

}
