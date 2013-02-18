/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.async.BlockingOperation;

/**
 * Tests the {@link UnionMarketDataAvailabilityProvider} class.
 */
@Test
public class UnionMarketDataAvailabilityProviderTest {

  protected MarketDataAvailabilityProvider create() {
    final FixedMarketDataAvailabilityProvider a = new FixedMarketDataAvailabilityProvider();
    final FixedMarketDataAvailabilityProvider b = new FixedMarketDataAvailabilityProvider();
    final MarketDataAvailabilityProvider c = new MarketDataAvailabilityProvider() {

      @Override
      public ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) throws MarketDataNotSatisfiableException {
        if (target instanceof ExternalBundleIdentifiable) {
          if (((ExternalBundleIdentifiable) target).getExternalIdBundle().contains(ExternalId.of("C", "Blocking"))) {
            throw BlockingOperation.block();
          }
        }
        return null;
      }

    };
    final ValueProperties properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "Mock").get();
    a.addAvailableData(ExternalId.of("A", "Present"), new ValueSpecification("Foo", ComputationTargetSpecification.NULL, properties));
    a.addMissingData(ExternalId.of("A", "Missing"), "Foo");
    b.addAvailableData(ExternalId.of("B", "Present"), new ValueSpecification("Foo", ComputationTargetSpecification.NULL, properties));
    b.addMissingData(ExternalId.of("B", "Missing"), "Foo");
    return new UnionMarketDataAvailabilityProvider(Arrays.asList(c, a, b));
  }

  public void testGetAvailability_available() {
    try {
      BlockingOperation.off();
      final MarketDataAvailabilityProvider availability = create();
      final ValueRequirement desiredValue = new ValueRequirement("Foo", ComputationTargetSpecification.NULL);
      assertNotNull(availability.getAvailability(ComputationTargetSpecification.NULL, ExternalId.of("A", "Present"), desiredValue));
      assertNotNull(availability.getAvailability(ComputationTargetSpecification.NULL, ExternalId.of("B", "Present"), desiredValue));
      assertNotNull(availability.getAvailability(ComputationTargetSpecification.NULL, ExternalIdBundle.of(ExternalId.of("A", "Present"), ExternalId.of("B", "Present")), desiredValue));
      assertNotNull(availability.getAvailability(ComputationTargetSpecification.NULL, ExternalIdBundle.of(ExternalId.of("A", "Present"), ExternalId.of("B", "Present"), ExternalId.of("C", "Absent")),
          desiredValue));
      assertNotNull(availability.getAvailability(ComputationTargetSpecification.NULL,
          ExternalIdBundle.of(ExternalId.of("A", "Present"), ExternalId.of("B", "Present"), ExternalId.of("C", "Blocking")), desiredValue));
      assertNotNull(availability.getAvailability(ComputationTargetSpecification.NULL,
          ExternalIdBundle.of(ExternalId.of("A", "Present"), ExternalId.of("B", "Missing"), ExternalId.of("C", "Blocking")), desiredValue));
      assertNotNull(availability.getAvailability(ComputationTargetSpecification.NULL, ExternalIdBundle.of(ExternalId.of("A", "Missing"), ExternalId.of("B", "Present")), desiredValue));
    } finally {
      BlockingOperation.on();
    }
  }

  public void testGetAvailability_absent() {
    final MarketDataAvailabilityProvider availability = create();
    final ValueRequirement desiredValue = new ValueRequirement("Foo", ComputationTargetSpecification.NULL);
    assertNull(availability.getAvailability(ComputationTargetSpecification.NULL, ExternalId.of("A", "Absent"), desiredValue));
    assertNull(availability.getAvailability(ComputationTargetSpecification.NULL, ExternalId.of("B", "Absent"), desiredValue));
    assertNull(availability.getAvailability(ComputationTargetSpecification.NULL, ExternalId.of("C", "Absent"), desiredValue));
  }

  @Test(expectedExceptions = {MarketDataNotSatisfiableException.class })
  public void testGetAvailability_missing_a() {
    final MarketDataAvailabilityProvider availability = create();
    final ValueRequirement desiredValue = new ValueRequirement("Foo", ComputationTargetSpecification.NULL);
    assertNull(availability.getAvailability(ComputationTargetSpecification.NULL, ExternalId.of("A", "Missing"), desiredValue));
  }

  @Test(expectedExceptions = {MarketDataNotSatisfiableException.class })
  public void testGetAvailability_missing_b() {
    final MarketDataAvailabilityProvider availability = create();
    final ValueRequirement desiredValue = new ValueRequirement("Foo", ComputationTargetSpecification.NULL);
    assertNull(availability.getAvailability(ComputationTargetSpecification.NULL, ExternalIdBundle.of(ExternalId.of("A", "Missing"), ExternalId.of("B", "Missing")), desiredValue));
  }

  @Test(expectedExceptions = {MarketDataNotSatisfiableException.class })
  public void testGetAvailability_missing_c() {
    final MarketDataAvailabilityProvider availability = create();
    final ValueRequirement desiredValue = new ValueRequirement("Foo", ComputationTargetSpecification.NULL);
    assertNull(availability.getAvailability(ComputationTargetSpecification.NULL, ExternalIdBundle.of(ExternalId.of("A", "Absent"), ExternalId.of("B", "Missing")), desiredValue));
  }

  @Test(expectedExceptions = {BlockingOperation.class })
  public void testGetAvailability_noneAvailableBlockingOperation_a() {
    try {
      BlockingOperation.off();
      final MarketDataAvailabilityProvider availability = create();
      final ValueRequirement desiredValue = new ValueRequirement("Foo", ComputationTargetSpecification.NULL);
      assertNull(availability.getAvailability(ComputationTargetSpecification.NULL, ExternalId.of("C", "Blocking").toBundle(), desiredValue));
    } finally {
      BlockingOperation.on();
    }
  }

  @Test(expectedExceptions = {BlockingOperation.class })
  public void testGetAvailability_noneAvailableBlockingOperation_b() {
    try {
      BlockingOperation.off();
      final MarketDataAvailabilityProvider availability = create();
      final ValueRequirement desiredValue = new ValueRequirement("Foo", ComputationTargetSpecification.NULL);
      assertNull(availability.getAvailability(ComputationTargetSpecification.NULL, ExternalIdBundle.of(ExternalId.of("A", "Absent"), ExternalId.of("C", "Blocking")), desiredValue));
    } finally {
      BlockingOperation.on();
    }
  }

}
