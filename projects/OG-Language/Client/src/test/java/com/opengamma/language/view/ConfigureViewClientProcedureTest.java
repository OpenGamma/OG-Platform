/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.marketdata.MarketDataAddOperation;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.language.config.ConfigurationItem;
import com.opengamma.language.config.MarketDataOverride;
import com.opengamma.language.config.MarketDataOverride.Operation;
import com.opengamma.language.config.ValueProperty;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the core logic of the {@link ConfigureViewClientProcedure} class.
 */
@Test(groups = TestGroup.UNIT)
public class ConfigureViewClientProcedureTest {

  private Set<ConfigurationItem> createLesserConfiguration() {
    final Set<ConfigurationItem> config = new HashSet<ConfigurationItem>();
    config.add(new MarketDataOverride(new ValueRequirement("Foo", ComputationTargetSpecification.of(UniqueId.of("Test", "1"))), 1, null));
    config.add(new MarketDataOverride(new ValueRequirement("Foo", ComputationTargetRequirement.of(ExternalId.of("UID", "Test~2"))), 2, Operation.ADD));
    config.add(new ValueProperty("Ignored", false));
    return config;
  }

  private Set<ConfigurationItem> createExtraConfiguration() {
    final Set<ConfigurationItem> config = createLesserConfiguration();
    config.add(new MarketDataOverride(new ValueRequirement("Bar", ComputationTargetSpecification.of(UniqueId.of("Test", "1"))), 1, Operation.MULTIPLY));
    config.add(new MarketDataOverride(new ValueRequirement("Bar", ComputationTargetRequirement.of(ExternalId.of("UID", "Test~2"))), 2, null));
    return config;
  }

  private UserViewClient createViewClient(final MockMarketDataInjector mockMarketDataInjector) {
    final ViewClientKey key = new ViewClientKey(ViewClientDescriptor.tickingMarketData(UniqueId.of("View", "Test"), null), true);
    final MockViewClient client = new MockViewClient(UniqueId.of("Client", "Test"));
    client.setLiveDataOverrideInjector(mockMarketDataInjector);
    return new UserViewClient(null, client, key);
  }

  public void testAddConfigurationItems() {
    final MockMarketDataInjector injector = new MockMarketDataInjector();
    final UserViewClient client = createViewClient(injector);
    ConfigureViewClientProcedure.invoke(client, createLesserConfiguration());
    final Map<ValueSpecification, Object> i = injector.getAddByValueSpecification();
    assertTrue(i.isEmpty());
    final Map<ValueRequirement, Object> j = injector.getAddByValueRequirement();
    assertEquals(2, j.size());
    assertTrue(j.values().contains(1));
    assertTrue(j.values().contains(new MarketDataAddOperation(2)));
    assertEquals(0, injector.getRemoveByValueRequirement().size());
    assertEquals(0, injector.getRemoveByValueSpecification().size());
    ConfigureViewClientProcedure.invoke(client, createExtraConfiguration());
    assertEquals(2, injector.getAddByValueRequirement().size());
    assertEquals(0, injector.getAddByValueSpecification().size());
    assertEquals(0, injector.getRemoveByValueRequirement().size());
    assertEquals(0, injector.getRemoveByValueSpecification().size());
  }

  public void testRemoveConfigurationItems() {
    final MockMarketDataInjector injector = new MockMarketDataInjector();
    final UserViewClient client = createViewClient(injector);
    ConfigureViewClientProcedure.invoke(client, createExtraConfiguration());
    assertEquals(4, injector.getAddByValueRequirement().size());
    assertEquals(0, injector.getAddByValueSpecification().size());
    assertEquals(0, injector.getRemoveByValueRequirement().size());
    assertEquals(0, injector.getRemoveByValueSpecification().size());
    ConfigureViewClientProcedure.invoke(client, createLesserConfiguration());
    assertEquals(0, injector.getAddByValueRequirement().size());
    assertEquals(0, injector.getAddByValueSpecification().size());
    assertEquals(2, injector.getRemoveByValueRequirement().size());
    assertEquals(0, injector.getRemoveByValueSpecification().size());
  }

}
