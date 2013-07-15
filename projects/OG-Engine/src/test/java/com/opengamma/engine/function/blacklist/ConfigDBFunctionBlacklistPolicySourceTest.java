/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import static org.testng.Assert.assertEquals;

import java.util.Collections;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ConfigDBFunctionBlacklistPolicySource} class.
 */
@Test(groups = TestGroup.UNIT)
public class ConfigDBFunctionBlacklistPolicySourceTest {

  public void testByName() {
    final ConfigSource configSource = Mockito.mock(ConfigSource.class);
    Mockito.when(configSource.getLatestByName(FunctionBlacklistPolicy.class, "FOO_BLACKLIST")).thenReturn(
        new DefaultFunctionBlacklistPolicy(UniqueId.of("Config", "Foo"), "FOO", 60, Collections.<FunctionBlacklistPolicy.Entry>emptySet()));
    final ConfigDBFunctionBlacklistPolicySource source = new ConfigDBFunctionBlacklistPolicySource(configSource);
    FunctionBlacklistPolicy policy = source.getPolicy("FOO");
    assertEquals(policy.getName(), "FOO");
    assertEquals(policy.getUniqueId(), UniqueId.of("Config", "Foo"));
  }

  public void testByUniqueId() {
    final ConfigSource configSource = Mockito.mock(ConfigSource.class);
    Mockito.when(configSource.getConfig(FunctionBlacklistPolicy.class, UniqueId.of("Config", "Foo"))).thenReturn(
        new DefaultFunctionBlacklistPolicy(UniqueId.of("Config", "Foo"), "FOO", 60, Collections.<FunctionBlacklistPolicy.Entry>emptySet()));
    final ConfigDBFunctionBlacklistPolicySource source = new ConfigDBFunctionBlacklistPolicySource(configSource);
    FunctionBlacklistPolicy policy = source.getPolicy(UniqueId.of("Config", "Foo"));
    assertEquals(policy.getName(), "FOO");
    assertEquals(policy.getUniqueId(), UniqueId.of("Config", "Foo"));
  }

}