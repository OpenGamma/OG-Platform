/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.util.ArgumentChecker;

/**
 * Private link resolver to resolve links using a ServiceContext.
 *
 * @param <T> the type of config object to be resolved
 */
/* package */ final class ServiceContextConfigLinkResolver<T> extends SourceLinkResolver<T, String, ConfigSource> {

  private final Class<T> _type;

  /**
   * Logger for the class.
   */
  private static final Logger s_logger = LoggerFactory.getLogger(ServiceContextConfigLinkResolver.class);

  public ServiceContextConfigLinkResolver() {
    super();
    _type = null;
  }

  public ServiceContextConfigLinkResolver(Class<T> type) {
    super();
    _type = ArgumentChecker.notNull(type, "type");
  }

  public ServiceContextConfigLinkResolver(ServiceContext serviceContext) {
    super(serviceContext);
    _type = null;
  }

  public ServiceContextConfigLinkResolver(ServiceContext serviceContext, Class<T> type) {
    super(serviceContext);
    _type = ArgumentChecker.notNull(type, "type");
  }

  @Override
  public LinkResolver<T, String> withTargetType(Class<T> targetType) {
    return new ServiceContextConfigLinkResolver<>(getServiceContext(), targetType);
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
  protected T executeQuery(ConfigSource configSource, String name, VersionCorrection versionCorrection) {

    if (_type == null) {
      throw new IllegalStateException("Unable to perform resolution without a target type");
    }

    // The database stores config items with exact type, but we may want to search
    // with a more general type. We therefore may need to try the search twice.
    final T result = findWithMatchingType(configSource, _type, name, versionCorrection);
    return result != null ?
        result :
        findWithGeneralType(configSource, _type, name, versionCorrection);
  }

  private T findWithMatchingType(ConfigSource configSource, Class<T> type,
                                 String identifier, VersionCorrection versionCorrection) {
    return selectResult(identifier, configSource.get(type, identifier, versionCorrection));
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
        }
    );

    final T result = (T) selectResult(identifier, results);
    if (result != null) {
      return result;
    } else {
      throw new DataNotFoundException("No config found with type: [" + type.getName() + "], id: [" +
                                      identifier + "] and versionCorrection: [" + versionCorrection + "]");
    }
  }

  private <R> R selectResult(String identifier, Iterable<ConfigItem<R>> results) {
    final Iterator<ConfigItem<R>> iterator = results.iterator();
    return iterator.hasNext() ? selectFirst(identifier, iterator) : null;
  }

  private <R> R selectFirst(String identifier, Iterator<ConfigItem<R>> iterator) {
    R result = iterator.next().getValue();
    if (iterator.hasNext()) {
      s_logger.warn("Found multiple matching config results for type: {} and name: {} - returning first found",
                    _type.getName(), identifier);
    }
    return result;
  }
}
