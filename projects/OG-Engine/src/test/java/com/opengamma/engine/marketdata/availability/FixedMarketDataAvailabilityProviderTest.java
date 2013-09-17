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
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link FixedMarketDataAvailabilityProvider} class.
 */
@Test(groups = TestGroup.UNIT)
public class FixedMarketDataAvailabilityProviderTest {

  public void testEmpty() {
    final FixedMarketDataAvailabilityProvider available = new FixedMarketDataAvailabilityProvider();
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("X", "1"));
    assertNull(available.getAvailability(targetSpec, ExternalId.of("A", "1"), new ValueRequirement("Foo", targetSpec)));
    assertNull(available.getAvailability(targetSpec, ExternalIdBundle.of(ExternalId.of("A", "1"), ExternalId.of("C", "1")), new ValueRequirement("Foo", targetSpec)));
    assertNull(available.getAvailability(targetSpec, UniqueId.of("X", "1"), new ValueRequirement("Foo", targetSpec)));
  }

  public void testAddAvailable_byExternalId() {
    final FixedMarketDataAvailabilityProvider available = new FixedMarketDataAvailabilityProvider();
    final ValueProperties properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "Mock").get();
    final ValueSpecification fooSpec = new ValueSpecification("Foo", ComputationTargetSpecification.NULL, properties);
    available.addAvailableData(ExternalId.of("A", "1"), fooSpec);
    available.addAvailableData(ExternalId.of("A", "1"), new ValueSpecification("Bar", ComputationTargetSpecification.NULL, properties));
    assertEquals(available.getAvailability(ComputationTargetSpecification.NULL, ExternalId.of("A", "1"), new ValueRequirement("Foo", ComputationTargetSpecification.NULL)), fooSpec);
  }

  public void testAddAvailable_byExternalIdBundle() {
    final FixedMarketDataAvailabilityProvider available = new FixedMarketDataAvailabilityProvider();
    final ValueProperties properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "Mock").get();
    final ValueSpecification fooSpec = new ValueSpecification("Foo", ComputationTargetSpecification.NULL, properties);
    available.addAvailableData(ExternalIdBundle.of(ExternalId.of("A", "1"), ExternalId.of("B", "1")), fooSpec);
    available.addAvailableData(ExternalIdBundle.of(ExternalId.of("A", "1"), ExternalId.of("B", "1")), new ValueSpecification("Bar", ComputationTargetSpecification.NULL, properties));
    assertEquals(available.getAvailability(ComputationTargetSpecification.NULL, ExternalIdBundle.of(ExternalId.of("A", "1"), ExternalId.of("C", "1")), new ValueRequirement("Foo",
        ComputationTargetSpecification.NULL)), fooSpec);
  }

  public void testAddAvailable_byUniqueId() {
    final FixedMarketDataAvailabilityProvider available = new FixedMarketDataAvailabilityProvider();
    final ValueProperties properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "Mock").get();
    final ValueSpecification fooSpec = new ValueSpecification("Foo", ComputationTargetSpecification.of(UniqueId.of("X", "1")), properties);
    final ValueSpecification barSpec = new ValueSpecification("Bar", ComputationTargetSpecification.of(UniqueId.of("X", "1")), properties);
    available.addAvailableData(fooSpec);
    available.addAvailableData(barSpec);
    assertEquals(available.getAvailability(ComputationTargetSpecification.of(UniqueId.of("X", "1")), UniqueId.of("X", "1"),
        new ValueRequirement("Foo", ComputationTargetSpecification.of(UniqueId.of("X", "1")))), fooSpec);
  }

}
