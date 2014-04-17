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
public abstract class ConfigLink<T> implements TargetableLink<T> {

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
   * @param name the name of the config object
   * @param type the class of the type of object being linked to
   * @return a config link
   */
  public static <C> ConfigLink<C> of(String name, Class<C> type) {
    return new ResolvableConfigLink<>(name, new ServiceContextConfigLinkResolver<>(type));
  }

  /**
   * Creates a link that will use a service context accessed via a thread local to access a
   * pre-configured service context containing the ConfigSource and VersionCorrectionProvider
   * necessary to resolve the provided bundle into the target object.
   *
   * @param <C> the type of the object being linked to
   * @param name the name of the config object
   * @return a config link
   */
  public static <C> ConfigLink<C> of(String name) {
    return new ResolvableConfigLink<>(name);
  }

  /**
   * Creates a link that will use a service context accessed via a thread local to access a
   * pre-configured service context containing the ConfigSource and VersionCorrectionProvider
   * necessary to resolve the provided bundle into the target object.
   *
   * @param <C> the type of the object being linked to
   * @param name the name of the config object
   * @return a config link
   */
  public static <C> ConfigLink<C> of(String name, ServiceContext serviceContext) {
    return new ResolvableConfigLink<>(name, new ServiceContextConfigLinkResolver<C>(serviceContext));
  }
  
  /**
   * Creates a link that embeds the provided object directly. This should only be used for
   * testing as it will not update if the underlying object is updated via another data
   * source or by a change in the VersionCorrection environment.
   *
   * @param <C> the type of the object being linked to
   * @param config the config to embed in the link, not null
   * @return the config link
   */
  public static <C> ConfigLink<C> of(C config) {
    return new FixedConfigLink<>(config);
  }

  /**
   * Creates a link that will use the provided service context to resolve the link rather
   * than use one available via a thread local environment. Use of this method should only
   * be necessary when you need to use resolution outside of the current VersionCorrection
   * threadlocal environment.
   *
   * @param <C> the type of the object being linked to
   * @param name the name of the config object
   * @param type the class of the type of Config the link refers to
   * @param serviceContext a service context containing the ConfigSource and
   * VersionCorrectionProvider necessary to resolve
   * @return the config link
   */

  // todo - do we want a method to generate a resolved version of a config object i.e. new FixedConfigLink(resolver.resolve()

}
