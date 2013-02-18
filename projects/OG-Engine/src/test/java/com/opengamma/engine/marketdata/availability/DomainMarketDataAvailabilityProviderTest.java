/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.Primitive;
import com.opengamma.engine.target.Primitive.ExternalBundleIdentifiablePrimitive;
import com.opengamma.engine.target.Primitive.ExternalIdentifiablePrimitive;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;

/**
 * Tests the {@link DomainMarketDataAvailabilityProvider} class.
 */
@Test
public class DomainMarketDataAvailabilityProviderTest extends AbstractMarketDataAvailabilityProviderTest {

  @Override
  protected AbstractMarketDataAvailabilityProvider createBase() {
    return new DomainMarketDataAvailabilityProvider(ImmutableSet.of(ExternalScheme.of("Ticker")), ImmutableSet.of("Foo", "Bar"));
  }

  public void testGetAvailability_uniqueId() {
    final MarketDataAvailabilityProvider availability = create();
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Security", "Foo"));
    final Object target = new Primitive(UniqueId.of("Security", "Foo"));
    ValueRequirement desiredValue = new ValueRequirement("Cow", targetSpec);
    assertNull(availability.getAvailability(targetSpec, target, desiredValue));
    desiredValue = new ValueRequirement("Foo", targetSpec);
    assertNull(availability.getAvailability(targetSpec, target, desiredValue));
  }

  public void testGetAvailability_externalId_nomatch() {
    final MarketDataAvailabilityProvider availability = create();
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Security", "Foo"));
    final Object target = new ExternalIdentifiablePrimitive(UniqueId.of("Security", "Foo"), ExternalId.of("BAD", "Foo"));
    ValueRequirement desiredValue = new ValueRequirement("Cow", targetSpec);
    assertNull(availability.getAvailability(targetSpec, target, desiredValue));
    desiredValue = new ValueRequirement("Foo", targetSpec);
    assertNull(availability.getAvailability(targetSpec, target, desiredValue));
  }

  public void testGetAvailability_externalId_match() {
    final MarketDataAvailabilityProvider availability = create();
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Security", "Foo"));
    final Object target = new ExternalIdentifiablePrimitive(UniqueId.of("Security", "Foo"), ExternalId.of("Ticker", "Foo"));
    ValueRequirement desiredValue = new ValueRequirement("Cow", targetSpec);
    assertNull(availability.getAvailability(targetSpec, target, desiredValue));
    desiredValue = new ValueRequirement("Foo", targetSpec);
    assertNotNull(availability.getAvailability(targetSpec, target, desiredValue));
  }

  public void testGetAvailability_externalIdBundle_nomatch() {
    final MarketDataAvailabilityProvider availability = create();
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Security", "Foo"));
    final Object target = new ExternalBundleIdentifiablePrimitive(UniqueId.of("Security", "Foo"), ExternalIdBundle.of(ExternalId.of("BAD1", "Foo"), ExternalId.of("BAD2", "Foo")));
    ValueRequirement desiredValue = new ValueRequirement("Cow", targetSpec);
    assertNull(availability.getAvailability(targetSpec, target, desiredValue));
    desiredValue = new ValueRequirement("Foo", targetSpec);
    assertNull(availability.getAvailability(targetSpec, target, desiredValue));
  }

  public void testGetAvailability_externalIdBundle_match() {
    final MarketDataAvailabilityProvider availability = create();
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Security", "Foo"));
    final Object target = new ExternalBundleIdentifiablePrimitive(UniqueId.of("Security", "Foo"), ExternalIdBundle.of(ExternalId.of("Ticker", "X"), ExternalId.of("BAD2", "Foo"),
        ExternalId.of("Ticker", "Y")));
    ValueRequirement desiredValue = new ValueRequirement("Cow", targetSpec);
    assertNull(availability.getAvailability(targetSpec, target, desiredValue));
    desiredValue = new ValueRequirement("Foo", targetSpec);
    assertNotNull(availability.getAvailability(targetSpec, target, desiredValue));
  }

}
