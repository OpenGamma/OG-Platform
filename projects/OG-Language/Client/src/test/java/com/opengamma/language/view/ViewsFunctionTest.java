/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ViewsFunction} class.
 */
@Test(groups = TestGroup.UNIT)
public class ViewsFunctionTest {

  private ConfigSource createConfigSource() {
    final InMemoryConfigMaster configMaster = new InMemoryConfigMaster();
    configMaster.add(new ConfigDocument(ConfigItem.of(new ViewDefinition("One", "Test"))));
    configMaster.add(new ConfigDocument(ConfigItem.of(new ViewDefinition("Two", "Test"))));
    configMaster.add(new ConfigDocument(ConfigItem.of(new ViewDefinition("Three", "Test"))));
    return new MasterConfigSource(configMaster);
  }

  public void testAllViews() {
    final ConfigSource source = createConfigSource();
    final Map<UniqueId, String> result = ViewsFunction.invoke(source, null);
    assertEquals(result.size(), 3);
  }

  public void testNamedViewPresent() {
    final ConfigSource source = createConfigSource();
    final Map<UniqueId, String> result = ViewsFunction.invoke(source, "Two");
    assertEquals(result.size(), 1);
    assertTrue(result.keySet().contains(UniqueId.of(InMemoryConfigMaster.DEFAULT_OID_SCHEME, "2")));
    assertTrue(result.values().contains("Two"));
  }

  public void testNamedViewMissing() {
    final ConfigSource source = createConfigSource();
    final Map<UniqueId, String> result = ViewsFunction.invoke(source, "Four");
    assertEquals(result.size(), 0);
  }

}
