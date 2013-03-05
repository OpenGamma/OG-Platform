/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * An implementation of {@link MarketDataAvailabilityProvider} that produces arbitrary value specifications for all values.
 * <p>
 * This is provided for use in test cases only, and would normally be used as the provider passed to an existing {@link MarketDataAvailabilityFilter}. A market data provider would normally require
 * more control over the specifications issued in order to manage later subscriptions to the values.
 */
public class DefaultMarketDataAvailabilityProvider implements MarketDataAvailabilityProvider {

  @Override
  public ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) throws MarketDataNotSatisfiableException {
    return new ValueSpecification(desiredValue.getValueName(), targetSpec, ValueProperties.with(ValuePropertyNames.FUNCTION, MarketDataSourcingFunction.UNIQUE_ID).get());
  }

  @Override
  public MarketDataAvailabilityFilter getAvailabilityFilter() {
    return new ProviderMarketDataAvailabilityFilter(this);
  }

}
