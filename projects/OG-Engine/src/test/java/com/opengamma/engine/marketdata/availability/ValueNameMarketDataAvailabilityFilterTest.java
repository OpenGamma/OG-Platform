/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.Primitive;
import com.opengamma.engine.target.Primitive.ExternalIdentifiablePrimitive;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ValueNameMarketDataAvailabilityFilter} class.
 */
@Test(groups = TestGroup.UNIT)
public class ValueNameMarketDataAvailabilityFilterTest {

  protected MarketDataAvailabilityFilter create() {
    return new ValueNameMarketDataAvailabilityFilter(ImmutableSet.of("Foo", "Bar"));
  }

  public void testMissing() {
    final MarketDataAvailabilityFilter availability = create();
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Security", "Foo"));
    final Object target = new Primitive(UniqueId.of("Security", "Foo"));
    final ValueRequirement desiredValue = new ValueRequirement("Cow", targetSpec);
    assertFalse(availability.isAvailable(targetSpec, target, desiredValue));
  }

  public void testPresent_uniqueId() {
    final MarketDataAvailabilityFilter availability = create();
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Security", "Foo"));
    final Object target = new Primitive(UniqueId.of("Security", "Foo"));
    final ValueRequirement desiredValue = new ValueRequirement("Bar", targetSpec);
    assertTrue(availability.isAvailable(targetSpec, target, desiredValue));
  }

  public void testPresent_externalId() {
    final MarketDataAvailabilityFilter availability = create();
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Security", "Foo"));
    final Object target = new ExternalIdentifiablePrimitive(UniqueId.of("Security", "Foo"), ExternalId.of("Ticker", "Foo"));
    final ValueRequirement desiredValue = new ValueRequirement("Bar", targetSpec);
    assertTrue(availability.isAvailable(targetSpec, target, desiredValue));
  }

}
