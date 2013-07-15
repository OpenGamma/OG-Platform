/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.Collection;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
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
 * Tests the {@link AbstractMarketDataAvailabilityFilter} class.
 */
@Test(groups = TestGroup.UNIT)
public class MarketDataAvailabilityFilterTest {

  public void testExternalId_present() {
    final ComputationTargetSpecification targetSpec = ComputationTargetSpecification.of(UniqueId.of("X", "0"));
    final ExternalId identifier = ExternalId.of("Foo", "1");
    final ValueRequirement desiredValue = new ValueRequirement("Foo", targetSpec);
    final MarketDataAvailabilityFilter filter = new AbstractMarketDataAvailabilityFilter() {

      @Override
      public boolean isAvailable(final ComputationTargetSpecification ts, final ExternalId i, final ValueRequirement dv) {
        assertEquals(ts, targetSpec);
        assertEquals(i, identifier);
        assertEquals(dv, desiredValue);
        return true;
      }

      @Override
      protected void populateAvailabilityHintKey(final Collection<Serializable> key) {
        // Don't care
      }

    };
    final Object target = new ExternalIdentifiablePrimitive(targetSpec.getUniqueId(), identifier);
    assertTrue(filter.isAvailable(targetSpec, target, desiredValue));
    final AbstractMarketDataAvailabilityProvider provider = Mockito.mock(AbstractMarketDataAvailabilityProvider.class);
    final ValueSpecification valueSpec = new ValueSpecification(desiredValue.getValueName(), targetSpec, ValueProperties.with(ValuePropertyNames.FUNCTION, "Mock").get());
    Mockito.when(provider.getAvailability(targetSpec, identifier, desiredValue)).thenReturn(valueSpec);
    assertEquals(filter.withProvider(provider).getAvailability(targetSpec, target, desiredValue), valueSpec);
  }

  public void testExternalId_missing() {
    final ComputationTargetSpecification targetSpec = ComputationTargetSpecification.of(UniqueId.of("X", "0"));
    final ExternalId identifier = ExternalId.of("Foo", "1");
    final ValueRequirement desiredValue = new ValueRequirement("Foo", targetSpec);
    final MarketDataAvailabilityFilter filter = new AbstractMarketDataAvailabilityFilter() {

      @Override
      public boolean isAvailable(final ComputationTargetSpecification ts, final ExternalId i, final ValueRequirement dv) {
        assertEquals(ts, targetSpec);
        assertEquals(i, identifier);
        assertEquals(dv, desiredValue);
        return false;
      }

      @Override
      protected void populateAvailabilityHintKey(final Collection<Serializable> key) {
        // Don't care
      }

    };
    final Object target = identifier;
    assertFalse(filter.isAvailable(targetSpec, target, desiredValue));
    final AbstractMarketDataAvailabilityProvider provider = Mockito.mock(AbstractMarketDataAvailabilityProvider.class);
    assertEquals(filter.withProvider(provider).getAvailability(targetSpec, target, desiredValue), null);
    Mockito.verifyZeroInteractions(provider);
  }

  public void testExternalIdBundle_allPresent() {
    final ComputationTargetSpecification targetSpec = ComputationTargetSpecification.of(UniqueId.of("X", "0"));
    final ExternalId identifier1 = ExternalId.of("Foo", "1");
    final ExternalId identifier2 = ExternalId.of("Foo", "2");
    final ExternalIdBundle identifiers = ExternalIdBundle.of(identifier1, identifier2);
    final ValueRequirement desiredValue = new ValueRequirement("Foo", targetSpec);
    final MarketDataAvailabilityFilter filter = new AbstractMarketDataAvailabilityFilter() {

      @Override
      public boolean isAvailable(final ComputationTargetSpecification ts, final ExternalId i, final ValueRequirement dv) {
        assertEquals(ts, targetSpec);
        assertEquals(dv, desiredValue);
        assertTrue(i.equals(identifier1) || i.equals(identifier2));
        return true;
      }

      @Override
      protected void populateAvailabilityHintKey(final Collection<Serializable> key) {
        // Don't care
      }

    };
    final Object target = new ExternalBundleIdentifiablePrimitive(targetSpec.getUniqueId(), identifiers);
    assertTrue(filter.isAvailable(targetSpec, target, desiredValue));
    final AbstractMarketDataAvailabilityProvider provider = Mockito.mock(AbstractMarketDataAvailabilityProvider.class);
    final ValueSpecification valueSpec = new ValueSpecification(desiredValue.getValueName(), targetSpec, ValueProperties.with(ValuePropertyNames.FUNCTION, "Mock").get());
    Mockito.when(provider.getAvailability(targetSpec, identifiers, desiredValue)).thenReturn(valueSpec);
    assertEquals(filter.withProvider(provider).getAvailability(targetSpec, target, desiredValue), valueSpec);
  }

  public void testExternalIdBundle_onePresent() {
    final ComputationTargetSpecification targetSpec = ComputationTargetSpecification.of(UniqueId.of("X", "0"));
    final ExternalId identifier1 = ExternalId.of("Foo", "1");
    final ExternalId identifier2 = ExternalId.of("Foo", "2");
    final ExternalIdBundle identifiers = ExternalIdBundle.of(identifier1, identifier2);
    final ValueRequirement desiredValue = new ValueRequirement("Foo", targetSpec);
    final MarketDataAvailabilityFilter filter = new AbstractMarketDataAvailabilityFilter() {

      @Override
      public boolean isAvailable(final ComputationTargetSpecification ts, final ExternalId i, final ValueRequirement dv) {
        assertEquals(ts, targetSpec);
        assertTrue(i.equals(identifier1) || i.equals(identifier2));
        assertEquals(dv, desiredValue);
        return i.equals(identifier1);
      }

      @Override
      protected void populateAvailabilityHintKey(final Collection<Serializable> key) {
        // Don't care
      }

    };
    final Object target = identifiers;
    assertTrue(filter.isAvailable(targetSpec, target, desiredValue));
    final AbstractMarketDataAvailabilityProvider provider = Mockito.mock(AbstractMarketDataAvailabilityProvider.class);
    final ValueSpecification valueSpec = new ValueSpecification(desiredValue.getValueName(), targetSpec, ValueProperties.with(ValuePropertyNames.FUNCTION, "Mock").get());
    Mockito.when(provider.getAvailability(targetSpec, identifier1, desiredValue)).thenReturn(valueSpec);
    assertEquals(filter.withProvider(provider).getAvailability(targetSpec, target, desiredValue), valueSpec);
  }

  public void testExternalIdBundle_allMissing() {
    final ComputationTargetSpecification targetSpec = ComputationTargetSpecification.of(UniqueId.of("X", "0"));
    final ExternalId identifier1 = ExternalId.of("Foo", "1");
    final ExternalId identifier2 = ExternalId.of("Foo", "2");
    final ExternalIdBundle identifiers = ExternalIdBundle.of(identifier1, identifier2);
    final ValueRequirement desiredValue = new ValueRequirement("Foo", targetSpec);
    final MarketDataAvailabilityFilter filter = new AbstractMarketDataAvailabilityFilter() {

      @Override
      public boolean isAvailable(final ComputationTargetSpecification ts, final ExternalId i, final ValueRequirement dv) {
        assertEquals(ts, targetSpec);
        assertTrue(i.equals(identifier1) || i.equals(identifier2));
        assertEquals(dv, desiredValue);
        return false;
      }

      @Override
      protected void populateAvailabilityHintKey(final Collection<Serializable> key) {
        // Don't care
      }

    };
    final Object target = identifiers;
    assertFalse(filter.isAvailable(targetSpec, target, desiredValue));
    final AbstractMarketDataAvailabilityProvider provider = Mockito.mock(AbstractMarketDataAvailabilityProvider.class);
    assertEquals(filter.withProvider(provider).getAvailability(targetSpec, target, desiredValue), null);
    Mockito.verifyZeroInteractions(provider);
  }

  public void testUniqueId_present() {
    final ComputationTargetSpecification targetSpec = ComputationTargetSpecification.of(UniqueId.of("X", "0"));
    final ValueRequirement desiredValue = new ValueRequirement("Foo", targetSpec);
    final MarketDataAvailabilityFilter filter = new AbstractMarketDataAvailabilityFilter() {

      @Override
      public boolean isAvailable(final ComputationTargetSpecification ts, final UniqueId i, final ValueRequirement dv) {
        assertEquals(ts, targetSpec);
        assertEquals(i, targetSpec.getUniqueId());
        assertEquals(dv, desiredValue);
        return true;
      }

      @Override
      protected void populateAvailabilityHintKey(final Collection<Serializable> key) {
        // Don't care
      }

    };
    final Object target = new Primitive(targetSpec.getUniqueId());
    assertTrue(filter.isAvailable(targetSpec, target, desiredValue));
    final AbstractMarketDataAvailabilityProvider provider = Mockito.mock(AbstractMarketDataAvailabilityProvider.class);
    final ValueSpecification valueSpec = new ValueSpecification(desiredValue.getValueName(), targetSpec, ValueProperties.with(ValuePropertyNames.FUNCTION, "Mock").get());
    Mockito.when(provider.getAvailability(targetSpec, targetSpec.getUniqueId(), desiredValue)).thenReturn(valueSpec);
    assertEquals(filter.withProvider(provider).getAvailability(targetSpec, target, desiredValue), valueSpec);
  }

  public void testUniqueId_missing() {
    final ComputationTargetSpecification targetSpec = ComputationTargetSpecification.of(UniqueId.of("X", "0"));
    final ValueRequirement desiredValue = new ValueRequirement("Foo", targetSpec);
    final MarketDataAvailabilityFilter filter = new AbstractMarketDataAvailabilityFilter() {

      @Override
      public boolean isAvailable(final ComputationTargetSpecification ts, final UniqueId i, final ValueRequirement dv) {
        assertEquals(ts, targetSpec);
        assertEquals(i, targetSpec.getUniqueId());
        assertEquals(dv, desiredValue);
        return false;
      }

      @Override
      protected void populateAvailabilityHintKey(final Collection<Serializable> key) {
        // Don't care
      }

    };
    final Object target = targetSpec.getUniqueId();
    assertFalse(filter.isAvailable(targetSpec, target, desiredValue));
    final AbstractMarketDataAvailabilityProvider provider = Mockito.mock(AbstractMarketDataAvailabilityProvider.class);
    assertEquals(filter.withProvider(provider).getAvailability(targetSpec, target, desiredValue), null);
    Mockito.verifyZeroInteractions(provider);
  }

  public void testNull_present() {
    final ValueRequirement desiredValue = new ValueRequirement("Foo", ComputationTargetSpecification.NULL);
    final MarketDataAvailabilityFilter filter = new AbstractMarketDataAvailabilityFilter() {

      @Override
      public boolean isAvailable(final ComputationTargetSpecification ts, final ValueRequirement dv) {
        assertEquals(ts, ComputationTargetSpecification.NULL);
        assertEquals(dv, desiredValue);
        return true;
      }

      @Override
      protected void populateAvailabilityHintKey(final Collection<Serializable> key) {
        // Don't care
      }

    };
    assertTrue(filter.isAvailable(ComputationTargetSpecification.NULL, null, desiredValue));
    final AbstractMarketDataAvailabilityProvider provider = Mockito.mock(AbstractMarketDataAvailabilityProvider.class);
    final ValueSpecification valueSpec = new ValueSpecification(desiredValue.getValueName(), ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, "Mock").get());
    Mockito.when(provider.getAvailability(ComputationTargetSpecification.NULL, desiredValue)).thenReturn(valueSpec);
    assertEquals(filter.withProvider(provider).getAvailability(ComputationTargetSpecification.NULL, null, desiredValue), valueSpec);
  }

  public void testNull_missing() {
    final ValueRequirement desiredValue = new ValueRequirement("Foo", ComputationTargetSpecification.NULL);
    final MarketDataAvailabilityFilter filter = new AbstractMarketDataAvailabilityFilter() {

      @Override
      public boolean isAvailable(final ComputationTargetSpecification ts, final ValueRequirement dv) {
        assertEquals(ts, ComputationTargetSpecification.NULL);
        assertEquals(dv, desiredValue);
        return false;
      }

      @Override
      protected void populateAvailabilityHintKey(final Collection<Serializable> key) {
        // Don't care
      }

    };
    assertFalse(filter.isAvailable(ComputationTargetSpecification.NULL, null, desiredValue));
    final AbstractMarketDataAvailabilityProvider provider = Mockito.mock(AbstractMarketDataAvailabilityProvider.class);
    assertEquals(filter.withProvider(provider).getAvailability(ComputationTargetSpecification.NULL, null, desiredValue), null);
    Mockito.verifyZeroInteractions(provider);
  }

}
