/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;

/**
 * Base for testing {@link AbstractMarketDataAvailabilityProvider} derived classes.
 */
public abstract class AbstractMarketDataAvailabilityProviderTest {

  protected static class Delegate extends AbstractMarketDataAvailabilityProvider.Delegate {

    public Delegate(final AbstractMarketDataAvailabilityProvider underlying) {
      super(underlying);
    }

    @Override
    protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalId identifier, final ValueRequirement desiredValue) {
      return new ValueSpecification(desiredValue.getValueName(), targetSpec, ValueProperties.with(ValuePropertyNames.FUNCTION, "ExternalId").get());
    }

    @Override
    protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalIdBundle identifiers, final ValueRequirement desiredValue) {
      return new ValueSpecification(desiredValue.getValueName(), targetSpec, ValueProperties.with(ValuePropertyNames.FUNCTION, "ExternalIdBundle").get());
    }

    @Override
    protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final UniqueId identifier, final ValueRequirement desiredValue) {
      if (identifier != null) {
        return new ValueSpecification(desiredValue.getValueName(), targetSpec, ValueProperties.with(ValuePropertyNames.FUNCTION, "UniqueId").get());
      } else {
        return null;
      }
    }

  }

  protected abstract AbstractMarketDataAvailabilityProvider createBase();

  protected MarketDataAvailabilityProvider create() {
    return create(createBase());
  }

  protected MarketDataAvailabilityProvider create(final AbstractMarketDataAvailabilityProvider availability) {
    return new Delegate(availability);
  }

}
