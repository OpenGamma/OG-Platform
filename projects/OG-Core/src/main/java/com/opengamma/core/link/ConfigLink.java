/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.link;

import com.opengamma.service.ServiceContext;

/**
 * Represents a link to a Config object using the object's name which is resolved on demand.
 * Use of links allows provision of Config objects by remote servers while maintaining the
 * ability to capture updates to the linked resources on each subsequent resolution.
 *
 * @param <T> type of the config
 */
public abstract class ConfigLink<T> implements Link<T> {

  /**
   * No arg constructor for the use of subclasses in the package.
   */
  /* package */ ConfigLink() {
  }

  /**
   * Creates a link that will use a service context accessed via a thread local to access a
   * pre-configured service context containing the ConfigSource and VersionCorrectionProvider
   * necessary to resolve the provided bundle into the target object.
   *
   * @param <C> the type of the object being linked to
   * @param name the name of the config object, not null
   * @param type the type of object being linked to, not null
   * @return a config link, not null
   */
  public static <C> ConfigLink<C> resolvable(String name, Class<C> type) {
    return new ResolvableConfigLink<>(name, type, new ServiceContextConfigLinkResolver<C>());
  }

  /**
   * Creates a link that will use a service context accessed via a thread local to access a
   * pre-configured service context containing the ConfigSource and VersionCorrectionProvider
   * necessary to resolve the provided bundle into the target object.
   *
   * @param <C> the type of the object being linked to
   * @param name the name of the config object, not null
   * @param type the type of object being linked to, not null
   * @param serviceContext a service context containing the ConfigSource and
   * VersionCorrectionProvider necessary to resolve, not null
   * @return a config link, not null
   */
  public static <C> ConfigLink<C> resolvable(String name, Class<C> type, ServiceContext serviceContext) {
    return new ResolvableConfigLink<>(name, type, new ServiceContextConfigLinkResolver<C>(serviceContext));
  }

  /**
   * Creates a link that embeds the provided object directly. Note that if the
   * embedded object has come from a source, there is no way of listening for
   * changes to the object.
   *
   * @param <C> the type of the object being linked to, not null
   * @param config the config to embed in the link, not null
   * @return the config link, not null
   */
  public static <C> ConfigLink<C> resolved(C config) {
    return new ResolvedConfigLink<>(config);
  }
}
