/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import static org.testng.Assert.assertEquals;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.marketdata.MarketDataAddOperation;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.language.config.ConfigurationItem;
import com.opengamma.language.config.MarketDataOverride;
import com.opengamma.language.config.MarketDataOverride.Operation;
import com.opengamma.language.config.ValueProperty;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * Tests the core logic of the {@link ConfigureViewClientProcedure} class.
 */
@Test
public class ConfigureViewClientProcedureTest {

  private Set<ConfigurationItem> createLesserConfiguration() {
    final Set<ConfigurationItem> config = new HashSet<ConfigurationItem>();
    config.add(new MarketDataOverride(new ValueRequirement("Foo", new ComputationTargetSpecification(UniqueId.of("Test", "1"))), null, null, 1, null));
    config.add(new MarketDataOverride(null, "Foo", ExternalId.of("UID", "Test~2"), 2, Operation.ADD));
    config.add(new ValueProperty("Ignored", false));
    return config;
  }

  private Set<ConfigurationItem> createExtraConfiguration() {
    final Set<ConfigurationItem> config = createLesserConfiguration();
    config.add(new MarketDataOverride(new ValueRequirement("Bar", new ComputationTargetSpecification(UniqueId.of("Test", "1"))), null, null, 1, Operation.MULTIPLY));
    config.add(new MarketDataOverride(null, "Bar", ExternalId.of("UID", "Test~2"), 2, null));
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
    final Collection<Pair<ValueRequirement, Object>> i = injector.getAddByValueRequirement();
    assertEquals(1, i.size());
    assertEquals((Integer) 1, i.iterator().next().getSecond());
    final Collection<Triple<ExternalId, String, Object>> j = injector.getAddByValueName();
    assertEquals(1, j.size());
    assertEquals(new MarketDataAddOperation(2), j.iterator().next().getThird());
    assertEquals(0, injector.getRemoveByValueRequirement().size());
    assertEquals(0, injector.getRemoveByValueName().size());
    ConfigureViewClientProcedure.invoke(client, createExtraConfiguration());
    assertEquals(1, injector.getAddByValueRequirement().size());
    assertEquals(1, injector.getAddByValueName().size());
    assertEquals(0, injector.getRemoveByValueRequirement().size());
    assertEquals(0, injector.getRemoveByValueName().size());
  }

  public void testRemoveConfigurationItems() {
    final MockMarketDataInjector injector = new MockMarketDataInjector();
    final UserViewClient client = createViewClient(injector);
    ConfigureViewClientProcedure.invoke(client, createExtraConfiguration());
    assertEquals(2, injector.getAddByValueRequirement().size());
    assertEquals(2, injector.getAddByValueName().size());
    assertEquals(0, injector.getRemoveByValueRequirement().size());
    assertEquals(0, injector.getRemoveByValueName().size());
    ConfigureViewClientProcedure.invoke(client, createLesserConfiguration());
    assertEquals(0, injector.getAddByValueRequirement().size());
    assertEquals(0, injector.getAddByValueName().size());
    assertEquals(1, injector.getRemoveByValueRequirement().size());
    assertEquals(1, injector.getRemoveByValueName().size());
  }

}
