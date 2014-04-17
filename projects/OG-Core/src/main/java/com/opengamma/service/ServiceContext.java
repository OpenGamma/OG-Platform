/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.service;

import java.util.Map;

import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.opengamma.util.ArgumentChecker;

/**
 * Registry containing service-providing objects keyed by class.
 * <p>
 * This is used via {@code ThreadLocalServiceContext} to provide access to services
 * across the whole system.
 */
public final class ServiceContext {

  /**
   * The services held by this context.
   */
  private ImmutableClassToInstanceMap<Object> _services;

  //-------------------------------------------------------------------------
  /**
   * Creates a new service context using the provided map to populate the service registry.
   * 
   * @param services  a map of type to service-providing objects, not null
   * @return a populated service context, not null
   */
  public static ServiceContext of(Map<Class<?>, Object> services) {
    ArgumentChecker.noNulls(services, "services");
    return new ServiceContext(ImmutableClassToInstanceMap.copyOf(services));
  }

  /**
   * Creates a new service context using the single provided service and type.
   * Typically this context would then be augmented using the {@code with} method.
   * 
   * @param <T> the type being added
   * @param clazz  the class of the initial service being registered, not null
   * @param service  a service-providing object, not null
   * @return a populated service context, not null
   */
  public static <T> ServiceContext of(Class<T> clazz, T service) {
    ArgumentChecker.notNull(clazz, "class");
    ArgumentChecker.notNull(service, "service");
    return new ServiceContext(ImmutableClassToInstanceMap.builder().put(clazz, service).build());
  }

  //-------------------------------------------------------------------------
  /**
   * Restricted constructor.
   * 
   * @param services  the services to start with, not null
   */
  private ServiceContext(ImmutableClassToInstanceMap<Object> services) {
    // don't need to argument check as private
    _services = services;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if the service-providing object is available for the specified type.
   * 
   * @param serviceClass  the class of the service, not null
   * @return true if the service is available
   */
  public boolean contains(Class<?> serviceClass) {
    ArgumentChecker.notNull(serviceClass, "serviceClass");
    return _services.containsKey(serviceClass);
  }

  /**
   * Gets the service-providing object of the specified type.
   * 
   * @param <T> expected type
   * @param serviceClass  the class of the service, not null
   * @return the service-providing object, not null
   * @throws IllegalArgumentException if the service is not found
   */
  public <T> T get(Class<T> serviceClass) {
    ArgumentChecker.notNull(serviceClass, "serviceClass");
    final T service = _services.getInstance(serviceClass);
    if (service == null) {
      throw new IllegalArgumentException("No service found: " + serviceClass);
    }
    return service;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a copy of this context with the map of services added.
   * <p>
   * If any services are provided that are already registered, the service registry
   * will be updated with the provided services.
   * 
   * @param services  a map of services objects keyed by their class, not null
   * @return an updated service context
   */
  public ServiceContext with(Map<Class<?>, Object> services) {
    // We have to calculate which of the original objects need to be
    // retained as ImmutableMap.Builder won't allow a key to be put
    // more than once
    ArgumentChecker.noNulls(services, "services");
    Map<Class<?>, Object> unchanged = Maps.difference(_services, services).entriesOnlyOnLeft();
    ImmutableClassToInstanceMap<Object> combined = ImmutableClassToInstanceMap.builder()
            .putAll(services)
            .putAll(unchanged)
            .build();
    return new ServiceContext(combined);
  }

  /**
   * Returns a copy of this context with the specified service added.
   * <p>
   * If the service provided is already registered, the service registry
   * will be updated with the provided service.
   * 
   * @param clazz  the class of the service to be added, not null
   * @param service  the service-providing object to be registered, not null
   * @return an updated service context
   */
  public ServiceContext with(Class<?> clazz, Object service) {
    ArgumentChecker.notNull(clazz, "class");
    ArgumentChecker.notNull(service, "service");
    return with(ImmutableMap.<Class<?>, Object>of(clazz, service));
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "ServiceContext[size=" + _services.size() + "]";
  }

}
