/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.HashMap;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ServiceContextTest {

  private static final ConfigSource MOCK_CONFIG_SOURCE = Mockito.mock(ConfigSource.class);
  private static final ConfigSource MOCK_CONFIG_SOURCE2 = Mockito.mock(ConfigSource.class);
  private static final SecuritySource MOCK_SECURITY_SOURCE = Mockito.mock(SecuritySource.class);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCreateServiceHandlesNullClass() {
    ServiceContext.of(null, new Object());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCreateServiceHandlesNullObject() {
    ServiceContext.of(ConfigSource.class, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCreateServiceWithMapHandlesNullClass() {
    ServiceContext.of(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCreateServiceWithMapHandlesNullKeys() {
    Map<Class<?>, Object> services = new HashMap<>();
    services.put(ConfigSource.class, null);
    ServiceContext.of(services);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCreateServiceWithMapHandlesNullValues() {
    Map<Class<?>, Object> services = new HashMap<>();
    services.put(null, new Object());
    ServiceContext.of(services);
  }

  @Test(expectedExceptions = ClassCastException.class)
  public void testCreateServiceWithIncorrectTypeIsDetected() {

    // Generics prevent ServiceContext.of(ConfigSource.class, MOCK_SECURITY_SOURCE)
    // from compiling. This test ensures we get equivalent safety when we configure
    // via a Map (where the generics can't help).
    Map<Class<?>, Object> services = new HashMap<>();
    services.put(ConfigSource.class, MOCK_SECURITY_SOURCE);
    ServiceContext.of(services);
  }

  public void testCreateServiceWorks() {
    ServiceContext context = ServiceContext.of(ConfigSource.class, MOCK_CONFIG_SOURCE);

    assertThat(context.get(ConfigSource.class), is(MOCK_CONFIG_SOURCE));
  }

  public void testCreateServiceWithMapWorks() {
    final Map<Class<?>, Object> services = ImmutableMap.<Class<?>, Object>of(
        ConfigSource.class, MOCK_CONFIG_SOURCE,
        SecuritySource.class, MOCK_SECURITY_SOURCE);
    ServiceContext context = ServiceContext.of(services);

    assertThat(context.get(ConfigSource.class), is(MOCK_CONFIG_SOURCE));
    assertThat(context.get(SecuritySource.class), is(MOCK_SECURITY_SOURCE));
  }

  public void testAddingServiceWorks() {
    ServiceContext context = ServiceContext.of(ConfigSource.class, MOCK_CONFIG_SOURCE);
    ServiceContext context2 = context.with(SecuritySource.class, MOCK_SECURITY_SOURCE);

    assertThat(context2.get(ConfigSource.class), is(MOCK_CONFIG_SOURCE));
    assertThat(context2.get(SecuritySource.class), is(MOCK_SECURITY_SOURCE));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddingServiceWithNullClassIsHandled() {
    ServiceContext context = ServiceContext.of(ConfigSource.class, MOCK_CONFIG_SOURCE);
    context.with(null, new Object());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddingServiceWithNullObjectIsHandled() {
    ServiceContext context = ServiceContext.of(ConfigSource.class, MOCK_CONFIG_SOURCE);
    context.with(ConfigSource.class, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddServiceWithMapHandlesNullKeys() {
    Map<Class<?>, Object> services = new HashMap<>();
    services.put(ConfigSource.class, null);
    ServiceContext context = ServiceContext.of(ConfigSource.class, MOCK_CONFIG_SOURCE);
    context.with(services);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddServiceWithMapHandlesNullValues() {
    Map<Class<?>, Object> services = new HashMap<>();
    services.put(null, new Object());
    ServiceContext context = ServiceContext.of(ConfigSource.class, MOCK_CONFIG_SOURCE);
    context.with(services);
  }

  public void testUpdatingServiceWorks() {
    ServiceContext context = ServiceContext.of(ConfigSource.class, MOCK_CONFIG_SOURCE);
    ServiceContext context2 = context.with(ConfigSource.class, MOCK_CONFIG_SOURCE2);

    assertThat(context2.get(ConfigSource.class), is(MOCK_CONFIG_SOURCE2));

  }

  public void testUpdatingServiceWithMapWorks() {
    ServiceContext context = ServiceContext.of(ConfigSource.class, MOCK_CONFIG_SOURCE);
    final Map<Class<?>, Object> services = ImmutableMap.<Class<?>, Object>of(
        ConfigSource.class, MOCK_CONFIG_SOURCE2,
        SecuritySource.class, MOCK_SECURITY_SOURCE);
    ServiceContext context2 = context.with(services);

    assertThat(context2.get(ConfigSource.class), is(MOCK_CONFIG_SOURCE2));
    assertThat(context2.get(SecuritySource.class), is(MOCK_SECURITY_SOURCE));
  }

}
