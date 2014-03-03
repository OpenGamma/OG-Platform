/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.link;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.VersionCorrection;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.VersionCorrectionProvider;

/**
 * Represents a link to a Config object using the object's name which is resolved on demand.
 * Use of links allows provision of Config objects by remote servers while maintaining the
 * ability to capture updates to the linked resources on each subsequent resolution.
 *
 * @param <T> type of the config
 */
public final class ConfigLink<T> extends AbstractLink<String, T> {

  private ConfigLink(String name, Class<T> type, LinkResolver<T> resolver) {
    super(name, type, resolver);
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
    return new ConfigLink<>(name, type, new ServiceContextConfigLinkResolver<>(name, type));
  }
  
  /**
   * Creates a link that embeds the provided object directly. This should only be used for
   * testing as it will not update if the underlying object is updated via another data
   * source or by a change in the VersionCorrection environment.
   *
   * @param <C> the type of the object being linked to
   * @param name the name of the config object
   * @param config the config to embed in the link, not null
   * @return the config link
   */
  @SuppressWarnings("unchecked")
  public static <C> ConfigLink<C> of(String name, C config) {
    return new ConfigLink<>(name, (Class<C>)  config.getClass(), new FixedLinkResolver<>(config));
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
  public static <C> ConfigLink<C> of(String name, Class<C> type, ServiceContext serviceContext) {
    return new ConfigLink<>(name, type, new ServiceContextConfigLinkResolver<>(name, type, serviceContext));
  }
  
  /**
   * Create a new ConfigLink, with the same name and type as this one that uses a newly
   * provided serviceContext. This should only be necessary when you need to use resolution
   * outside of the current VersionCorrection threadlocal environment.
   *
   * @param serviceContext a service context containing the ConfigSource and
   * VersionCorrectionProvider necessary to resolve
   * @return a new config link
   */
  public ConfigLink<T> with(ServiceContext serviceContext) {
    return of(getIdentifier(), getType(), serviceContext);
  }

  /**
   * Private link resolver to resolve links using a ServiceContext.
   *
   * @param <T> the type of config object to be resolved
   */
  private static final class ServiceContextConfigLinkResolver<T>
      extends SourceLinkResolver<String, T, ConfigSource> {

    /**
     * Logger for the class.
     */
    private static final Logger s_logger = LoggerFactory.getLogger(ServiceContextConfigLinkResolver.class);

    /**
     * The type of the config object to be returned.
     */
    private final Class<T> _type;

    // Private constructor as only for use by enclosing class
    private ServiceContextConfigLinkResolver(String name, Class<T> type) {
      this(name, type, null);
    }

    // Private constructor as only for use by enclosing class
    private ServiceContextConfigLinkResolver(String name, Class<T> type, ServiceContext serviceContext) {
      super(name, serviceContext);
      _type = type;
    }

    @Override
    protected Class<ConfigSource> getSourceClass() {
      return ConfigSource.class;
    }

    @Override
    protected VersionCorrection getVersionCorrection(VersionCorrectionProvider vcProvider) {
      return vcProvider.getConfigVersionCorrection();
    }

    @Override
    protected T executeQuery(ConfigSource configSource, VersionCorrection versionCorrection) {
      // The database stores config items with exact type, but we may want to search
      // with a more general type. We therefore may need to try the search twice.
      final T result = findWithMatchingType(configSource, _type, getIdentifier(), versionCorrection);
      return result != null ?
          result :
          findWithGeneralType(configSource, _type, getIdentifier(), versionCorrection);
    }

    private T findWithMatchingType(ConfigSource configSource, Class<T> type,
                                   String identifier, VersionCorrection versionCorrection) {
      return selectResult(configSource.get(type, identifier, versionCorrection));
    }

    @SuppressWarnings("unchecked")
    private T findWithGeneralType(ConfigSource configSource, final Class<T> type,
                                  String identifier, VersionCorrection versionCorrection) {

      Iterable<ConfigItem<Object>> results = Iterables.filter(
          configSource.get(Object.class, identifier, versionCorrection),
          new Predicate<ConfigItem<Object>>() {
            @Override
            public boolean apply(ConfigItem<Object> item) {
              return type.isAssignableFrom(item.getValue().getClass());
            }
          });

      final T result = (T) selectResult(results);
      if (result != null) {
        return result;
      } else {
        throw new DataNotFoundException("No config found with type: [" + type.getName() + "], id: [" +
                                        identifier + "] and versionCorrection: [" + versionCorrection + "]");
      }
    }

    private <R> R selectResult(Iterable<ConfigItem<R>> results) {
      final Iterator<ConfigItem<R>> iterator = results.iterator();
      return iterator.hasNext() ? selectFirst(iterator) : null;
    }

    private <R> R selectFirst(Iterator<ConfigItem<R>> iterator) {
      R result = iterator.next().getValue();
      if (iterator.hasNext()) {
        s_logger.warn("Found multiple matching config results for type: {} and name: {} - returning first found",
                      _type.getName(), getIdentifier());
      }
      return result;
    }
  }
}
