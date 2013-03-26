/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.io.Serializable;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.Primitive;
import com.opengamma.engine.target.Primitive.ExternalBundleIdentifiablePrimitive;
import com.opengamma.engine.target.Primitive.ExternalIdentifiablePrimitive;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link AbstractMarketDataAvailabilityProvider} class.
 */
@Test(groups = TestGroup.UNIT)
public class MarketDataAvailabilityProviderTest {

  private static class Impl extends AbstractMarketDataAvailabilityProvider {

    @Override
    public MarketDataAvailabilityFilter getAvailabilityFilter() {
      throw new UnsupportedOperationException();
    }

    @Override
    protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalId identifier, final ValueRequirement desiredValue) {
      return new ValueSpecification(desiredValue.getValueName(), targetSpec, ValueProperties.with(ValuePropertyNames.FUNCTION, "externalId").get());
    }

    @Override
    protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalIdBundle identifiers, final ValueRequirement desiredValue) {
      return new ValueSpecification(desiredValue.getValueName(), targetSpec, ValueProperties.with(ValuePropertyNames.FUNCTION, "externalIdBundle").get());
    }

    @Override
    protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final UniqueId identifier, final ValueRequirement desiredValue) {
      return new ValueSpecification(desiredValue.getValueName(), targetSpec, ValueProperties.with(ValuePropertyNames.FUNCTION, "uniqueId").get());
    }

    @Override
    protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ValueRequirement desiredValue) {
      return new ValueSpecification(desiredValue.getValueName(), targetSpec, ValueProperties.with(ValuePropertyNames.FUNCTION, "null").get());
    }

    @Override
    public Serializable getAvailabilityHintKey() {
      return getClass();
    }

  }

  protected MarketDataAvailabilityProvider create() {
    return new Impl();
  }

  public void testGetAvailability_uniqueId() {
    final MarketDataAvailabilityProvider availability = create();
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Security", "Foo"));
    final Object target = new Primitive(UniqueId.of("Security", "Foo"));
    final ValueRequirement desiredValue = new ValueRequirement("Bar", targetSpec);
    assertEquals(availability.getAvailability(targetSpec, target, desiredValue).getProperty(ValuePropertyNames.FUNCTION), "uniqueId");
  }

  public void testGetAvailability_externalId() {
    final MarketDataAvailabilityProvider availability = create();
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Security", "Foo"));
    final Object target = new ExternalIdentifiablePrimitive(UniqueId.of("Security", "Foo"), ExternalId.of("Ticker", "Foo"));
    final ValueRequirement desiredValue = new ValueRequirement("Bar", targetSpec);
    assertEquals(availability.getAvailability(targetSpec, target, desiredValue).getProperty(ValuePropertyNames.FUNCTION), "externalId");
  }

  public void testGetAvailability_externalIdBundle() {
    final MarketDataAvailabilityProvider availability = create();
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Security", "Foo"));
    final Object target = new ExternalBundleIdentifiablePrimitive(UniqueId.of("Security", "Foo"), ExternalIdBundle.of("Ticker", "Foo"));
    final ValueRequirement desiredValue = new ValueRequirement("Bar", targetSpec);
    assertEquals(availability.getAvailability(targetSpec, target, desiredValue).getProperty(ValuePropertyNames.FUNCTION), "externalIdBundle");
  }

  public void testGetAvailability_null() {
    final MarketDataAvailabilityProvider availability = create();
    final ValueRequirement desiredValue = new ValueRequirement("Bar", ComputationTargetSpecification.NULL);
    assertEquals(availability.getAvailability(ComputationTargetSpecification.NULL, null, desiredValue).getProperty(ValuePropertyNames.FUNCTION), "null");
  }

  public void testOf_same() {
    final MarketDataAvailabilityProvider availability = create();
    assertSame(AbstractMarketDataAvailabilityProvider.of(availability), availability);
  }

  public void testOf_wrapped() {
    final MarketDataAvailabilityFilter filter = Mockito.mock(MarketDataAvailabilityFilter.class);
    final MarketDataAvailabilityProvider provider = new MarketDataAvailabilityProvider() {

      @Override
      public ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) throws MarketDataNotSatisfiableException {
        return new ValueSpecification(desiredValue.getValueName(), targetSpec, ValueProperties.with(ValuePropertyNames.FUNCTION, "Mock").get());
      }

      @Override
      public MarketDataAvailabilityFilter getAvailabilityFilter() {
        return filter;
      }

      @Override
      public Serializable getAvailabilityHintKey() {
        return getClass();
      }

    };
    final AbstractMarketDataAvailabilityProvider availability = AbstractMarketDataAvailabilityProvider.of(provider);
    assertSame(availability.getAvailabilityFilter(), filter);
    final ValueRequirement desiredValue = new ValueRequirement("Foo", ComputationTargetSpecification.NULL);
    assertEquals(availability.getAvailability(ComputationTargetSpecification.NULL, (ExternalId) null, desiredValue).getProperty(ValuePropertyNames.FUNCTION), "Mock");
    assertEquals(availability.getAvailability(ComputationTargetSpecification.NULL, (ExternalIdBundle) null, desiredValue).getProperty(ValuePropertyNames.FUNCTION), "Mock");
    assertEquals(availability.getAvailability(ComputationTargetSpecification.NULL, (UniqueId) null, desiredValue).getProperty(ValuePropertyNames.FUNCTION), "Mock");
    assertEquals(availability.getAvailability(ComputationTargetSpecification.NULL, desiredValue).getProperty(ValuePropertyNames.FUNCTION), "Mock");
  }

}
