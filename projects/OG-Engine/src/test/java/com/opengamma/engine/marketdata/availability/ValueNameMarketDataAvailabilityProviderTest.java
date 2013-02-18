/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.Primitive;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.UniqueId;

/**
 * Tests the {@link ValueNameMarketDataAvailabilityProvider} class.
 */
@Test
public class ValueNameMarketDataAvailabilityProviderTest extends AbstractMarketDataAvailabilityProviderTest {

  @Override
  protected AbstractMarketDataAvailabilityProvider createBase() {
    return new ValueNameMarketDataAvailabilityProvider(ImmutableSet.of("Foo", "Bar"));
  }

  public void testMissing() {
    final MarketDataAvailabilityProvider availability = create();
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Security", "Foo"));
    final Object target = new Primitive(UniqueId.of("Security", "Foo"));
    final ValueRequirement desiredValue = new ValueRequirement("Cow", targetSpec);
    assertNull(availability.getAvailability(targetSpec, target, desiredValue));
  }

  public void testPresent() {
    final MarketDataAvailabilityProvider availability = create();
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Security", "Foo"));
    final Object target = new Primitive(UniqueId.of("Security", "Foo"));
    final ValueRequirement desiredValue = new ValueRequirement("Bar", targetSpec);
    assertEquals(availability.getAvailability(targetSpec, target, desiredValue).getValueName(), desiredValue.getValueName());
  }

}
