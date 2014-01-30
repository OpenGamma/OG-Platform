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

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class ServiceContextTest {

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

  public void testCreateServiceWorks() {

    final Object o1 = new Object();
    ServiceContext context = ServiceContext.of(ConfigSource.class, o1);

    assertThat(context.getService(ConfigSource.class), is(o1));
  }

  public void testCreateServiceWithMapWorks() {

    final Object o1 = new Object();
    final Object o2 = new Object();
    final Map<Class<?>, Object> services = ImmutableMap.<Class<?>, Object>of(
        ConfigSource.class, o1,
        SecuritySource.class, o2);
    ServiceContext context = ServiceContext.of(services);

    assertThat(context.getService(ConfigSource.class), is(o1));
    assertThat(context.getService(SecuritySource.class), is(o2));
  }

  public void testAddingServiceWorks() {

    final Object o1 = new Object();
    ServiceContext context = ServiceContext.of(ConfigSource.class, o1);

    final Object o2 = new Object();
    ServiceContext context2 = context.with(SecuritySource.class, o2);

    assertThat(context2.getService(ConfigSource.class), is(o1));
    assertThat(context2.getService(SecuritySource.class), is(o2));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddingServiceWithNullClassIsHandled() {
    ServiceContext context = ServiceContext.of(ConfigSource.class, new Object());
    context.with(null, new Object());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddingServiceWithNullObjectIsHandled() {
    ServiceContext context = ServiceContext.of(ConfigSource.class, new Object());
    context.with(ConfigSource.class, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddServiceWithMapHandlesNullKeys() {
    Map<Class<?>, Object> services = new HashMap<>();
    services.put(ConfigSource.class, null);
    ServiceContext context = ServiceContext.of(ConfigSource.class, new Object());
    context.with(services);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddServiceWithMapHandlesNullValues() {
    Map<Class<?>, Object> services = new HashMap<>();
    services.put(null, new Object());
    ServiceContext context = ServiceContext.of(ConfigSource.class, new Object());
    context.with(services);
  }

  public void testUpdatingServiceWorks() {
    final Object o1 = new Object();
    ServiceContext context = ServiceContext.of(ConfigSource.class, o1);

    final Object o2 = new Object();
    ServiceContext context2 = context.with(ConfigSource.class, o2);

    assertThat(context2.getService(ConfigSource.class), is(o2));

  }

  public void testUpdatingServiceWithMapWorks() {

    final Object o1 = new Object();
    ServiceContext context = ServiceContext.of(ConfigSource.class, o1);

    final Object o2 = new Object();
    final Object o3 = new Object();
    final Map<Class<?>, Object> services = ImmutableMap.<Class<?>, Object>of(
        ConfigSource.class, o3,
        SecuritySource.class, o2);

    ServiceContext context2 = context.with(services);

    assertThat(context2.getService(ConfigSource.class), is(o3));
    assertThat(context2.getService(SecuritySource.class), is(o2));
  }
}
