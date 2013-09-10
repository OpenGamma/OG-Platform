/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ConfigDocumentWatchSetProvider} class.
 */
@Test(groups = TestGroup.UNIT)
public class ConfigDocumentWatchSetProviderTest {

  public void testEmptyWatchSet() {
    final ConfigDocumentWatchSetProvider provider = new ConfigDocumentWatchSetProvider(Mockito.mock(ConfigSource.class));
    assertEquals(provider.getAdditionalWatchSet(Collections.<ObjectId>emptySet()), Collections.emptySet());
  }

  public void testMissingClass() {
    final ConfigDocumentWatchSetProvider provider = new ConfigDocumentWatchSetProvider(Mockito.mock(ConfigSource.class));
    assertEquals(provider.getAdditionalWatchSet(Collections.<ObjectId>singleton(ObjectId.of(ConfigDocumentWatchSetProvider.CONFIG_TYPE_SCHEME, "ClassDoesn'tExist"))), Collections.emptySet());
  }

  public void testInvalidWatches() {
    final ConfigDocumentWatchSetProvider provider = new ConfigDocumentWatchSetProvider(Mockito.mock(ConfigSource.class));
    assertEquals(provider.getAdditionalWatchSet(Collections.<ObjectId>singleton(ObjectId.of("Foo", "Bar"))), Collections.emptySet());
  }

  private ConfigItem<ConfigDocumentWatchSetProviderTest> configItem(final int id) {
    final ConfigItem<ConfigDocumentWatchSetProviderTest> item = ConfigItem.of(null);
    item.setType(ConfigDocumentWatchSetProviderTest.class);
    item.setUniqueId(UniqueId.of("Foo", Integer.toString(id), "V"));
    return item;
  }

  public void testWatches() {
    final ConfigSource configSource = Mockito.mock(ConfigSource.class);
    final ConfigDocumentWatchSetProvider provider = new ConfigDocumentWatchSetProvider(configSource);
    Mockito.when(configSource.getAll(ConfigDocumentWatchSetProviderTest.class, VersionCorrection.LATEST)).thenReturn(
        Arrays.<ConfigItem<ConfigDocumentWatchSetProviderTest>>asList(configItem(1), configItem(2)));
    assertEquals(provider.getAdditionalWatchSet(Collections.<ObjectId>singleton(ObjectId.of(ConfigDocumentWatchSetProvider.CONFIG_TYPE_SCHEME, ConfigDocumentWatchSetProviderTest.class.getName()))),
        ImmutableSet.of(ObjectId.of("Foo", "1"), ObjectId.of("Foo", "2")));
  }

}
