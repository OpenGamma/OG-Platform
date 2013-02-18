/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.Primitive;
import com.opengamma.engine.target.Primitive.ExternalBundleIdentifiablePrimitive;
import com.opengamma.engine.target.Primitive.ExternalIdentifiablePrimitive;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;

/**
 * Tests the {@link AbstractMarketDataAvailabilityProvider} class.
 */
@Test
public class MarketDataAvailabilityProviderTest extends AbstractMarketDataAvailabilityProviderTest {

  private static class Base extends AbstractMarketDataAvailabilityProvider {

    private Base() {
    }

    private Base(final Delegate delegate) {
      super(delegate);
    }

    @Override
    protected MarketDataAvailabilityProvider withDelegate(final Delegate delegate) {
      return new Base(delegate);
    }

  }

  @Override
  protected AbstractMarketDataAvailabilityProvider createBase() {
    return new Base();
  }

  public void testGetAvailability_uniqueId() {
    final AbstractMarketDataAvailabilityProvider base = createBase();
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Security", "Foo"));
    final Object target = new Primitive(UniqueId.of("Security", "Foo"));
    final ValueRequirement desiredValue = new ValueRequirement("Bar", targetSpec);
    assertNull(base.getAvailability(targetSpec, target, desiredValue));
    final MarketDataAvailabilityProvider delegate = create(base);
    assertEquals(delegate.getAvailability(targetSpec, target, desiredValue).getProperty(ValuePropertyNames.FUNCTION), "UniqueId");
  }

  public void testGetAvailability_externalId() {
    final AbstractMarketDataAvailabilityProvider base = createBase();
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Security", "Foo"));
    final Object target = new ExternalIdentifiablePrimitive(UniqueId.of("Security", "Foo"), ExternalId.of("Ticker", "Foo"));
    final ValueRequirement desiredValue = new ValueRequirement("Bar", targetSpec);
    assertNull(base.getAvailability(targetSpec, target, desiredValue));
    final MarketDataAvailabilityProvider delegate = create(base);
    assertEquals(delegate.getAvailability(targetSpec, target, desiredValue).getProperty(ValuePropertyNames.FUNCTION), "ExternalId");
  }

  public void testGetAvailability_externalIdBundle() {
    final AbstractMarketDataAvailabilityProvider base = createBase();
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Security", "Foo"));
    final Object target = new ExternalBundleIdentifiablePrimitive(UniqueId.of("Security", "Foo"), ExternalIdBundle.of("Ticker", "Foo"));
    final ValueRequirement desiredValue = new ValueRequirement("Bar", targetSpec);
    assertNull(base.getAvailability(targetSpec, target, desiredValue));
    final MarketDataAvailabilityProvider delegate = create(base);
    assertEquals(delegate.getAvailability(targetSpec, target, desiredValue).getProperty(ValuePropertyNames.FUNCTION), "ExternalIdBundle");
  }

  public void testGetAvailability_null() {
    final AbstractMarketDataAvailabilityProvider base = createBase();
    final ValueRequirement desiredValue = new ValueRequirement("Bar", ComputationTargetSpecification.NULL);
    assertNull(base.getAvailability(ComputationTargetSpecification.NULL, (Object) null, desiredValue));
    final MarketDataAvailabilityProvider delegate = create(base);
    assertNull(delegate.getAvailability(ComputationTargetSpecification.NULL, null, desiredValue));
  }

}
