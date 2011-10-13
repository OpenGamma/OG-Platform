/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import static org.testng.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.language.config.ConfigurationItem;
import com.opengamma.language.config.MarketDataOverride;
import com.opengamma.language.config.ValueProperty;

/**
 * Tests the core logic of the {@link ConfigureViewClientProcedure} class.
 */
@Test
public class ConfigureViewClientProcedureTest {

  private Set<ConfigurationItem> createLesserConfiguration() {
    final Set<ConfigurationItem> config = new HashSet<ConfigurationItem>();
    config.add(new MarketDataOverride(new ValueRequirement("Foo", new ComputationTargetSpecification(UniqueId.of("Test", "1"))), null, null, 1));
    config.add(new MarketDataOverride(null, "Foo", ExternalId.of("UID", "Test~2"), 2));
    config.add(new ValueProperty("Ignored", false));
    return config;
  }

  private Set<ConfigurationItem> createExtraConfiguration() {
    final Set<ConfigurationItem> config = createLesserConfiguration();
    config.add(new MarketDataOverride(new ValueRequirement("Bar", new ComputationTargetSpecification(UniqueId.of("Test", "1"))), null, null, 1));
    config.add(new MarketDataOverride(null, "Bar", ExternalId.of("UID", "Test~2"), 2));
    return config;
  }

  private UserViewClient createViewClient(final MockMarketDataInjector mockMarketDataInjector) {
    final ViewClientKey key = new ViewClientKey("View~Test", true);
    final MockViewClient client = new MockViewClient(UniqueId.of("Client", "Test"));
    client.setLiveDataOverrideInjector(mockMarketDataInjector);
    return new UserViewClient(null, client, key);
  }

  public void testAddConfigurationItems() {
    final MockMarketDataInjector injector = new MockMarketDataInjector();
    final UserViewClient client = createViewClient(injector);
    ConfigureViewClientProcedure.INSTANCE.invoke(client, createLesserConfiguration());
    assertEquals(1, injector.getAddByValueRequirement().size());
    assertEquals(1, injector.getAddByValueName().size());
    assertEquals(0, injector.getRemoveByValueRequirement().size());
    assertEquals(0, injector.getRemoveByValueName().size());
    ConfigureViewClientProcedure.INSTANCE.invoke(client, createExtraConfiguration());
    assertEquals(1, injector.getAddByValueRequirement().size());
    assertEquals(1, injector.getAddByValueName().size());
    assertEquals(0, injector.getRemoveByValueRequirement().size());
    assertEquals(0, injector.getRemoveByValueName().size());
  }

  public void testRemoveConfigurationItems() {
    final MockMarketDataInjector injector = new MockMarketDataInjector();
    final UserViewClient client = createViewClient(injector);
    ConfigureViewClientProcedure.INSTANCE.invoke(client, createExtraConfiguration());
    assertEquals(2, injector.getAddByValueRequirement().size());
    assertEquals(2, injector.getAddByValueName().size());
    assertEquals(0, injector.getRemoveByValueRequirement().size());
    assertEquals(0, injector.getRemoveByValueName().size());
    ConfigureViewClientProcedure.INSTANCE.invoke(client, createLesserConfiguration());
    assertEquals(0, injector.getAddByValueRequirement().size());
    assertEquals(0, injector.getAddByValueName().size());
    assertEquals(1, injector.getRemoveByValueRequirement().size());
    assertEquals(1, injector.getRemoveByValueName().size());
  }

}
