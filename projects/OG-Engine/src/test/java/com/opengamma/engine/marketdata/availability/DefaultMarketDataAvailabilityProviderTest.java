/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.Primitive;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link DefaultMarketDataAvailabilityProvider} class.
 */
@Test(groups = TestGroup.UNIT)
public class DefaultMarketDataAvailabilityProviderTest {

  protected MarketDataAvailabilityProvider create() {
    return new OptimisticMarketDataAvailabilityFilter().withProvider(new DefaultMarketDataAvailabilityProvider());
  }

  public void testGetAvailability() {
    final MarketDataAvailabilityProvider availability = create();
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Security", "Foo"));
    final Object target = new Primitive(UniqueId.of("Security", "Foo"));
    assertNull(availability.getAvailability(targetSpec, target, new ValueRequirement("Value", targetSpec)));
    assertNotNull(availability.getAvailability(targetSpec, new Primitive(UniqueId.of("Security", "Foo")), new ValueRequirement("Market_Value", targetSpec)));
  }

  public void testIsAvailable() {
    final MarketDataAvailabilityFilter availability = create().getAvailabilityFilter();
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Security", "Foo"));
    final Object target = new Primitive(UniqueId.of("Security", "Foo"));
    assertFalse(availability.isAvailable(targetSpec, target, new ValueRequirement("Value", targetSpec)));
    assertTrue(availability.isAvailable(targetSpec, new Primitive(UniqueId.of("Security", "Foo")), new ValueRequirement("Market_Value", targetSpec)));
  }

}
