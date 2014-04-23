/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.link;

import java.util.Collection;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.config.SnapshotSource;
import com.opengamma.core.snapshot.impl.SnapshotItem;
import com.opengamma.id.VersionCorrection;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.VersionCorrectionProvider;

/**
 * Link resolver to resolve snapshot links using a ServiceContext.
 *
 * @param <T> the type of config object to be resolved
 */
/* package */ final class ServiceContextSnapshotLinkResolver<T> extends SourceLinkResolver<String, T, SnapshotSource> {

  /**
   * Logger for the class.
   */
  private static final Logger s_logger = LoggerFactory.getLogger(ServiceContextSnapshotLinkResolver.class);

  /**
   * Creates the resolver using the default service context.
   */
  /* package */ ServiceContextSnapshotLinkResolver() {
    super();
  }

  /**
   * Creates the resolver using the supplied service context.
   *
   * @param serviceContext the service context to use when resolving the link
   */
  /* package */ ServiceContextSnapshotLinkResolver(ServiceContext serviceContext) {
    super(serviceContext);
  }

  @Override
  protected Class<SnapshotSource> getSourceClass() {
    return SnapshotSource.class;
  }

  @Override
  protected VersionCorrection getVersionCorrection(VersionCorrectionProvider vcProvider) {
    return vcProvider.getConfigVersionCorrection();
  }

  @Override
  protected T executeQuery(SnapshotSource snapshotSource, Class<T> type,  String name, VersionCorrection versionCorrection) {

    // The database stores config items with exact type, but we may want to search
    // with a more general type. We therefore may need to try the search twice.
    final T result = findWithMatchingType(snapshotSource, type, name, versionCorrection);
    return result != null ?
        result :
        findWithGeneralType(snapshotSource, type, name, versionCorrection);
  }

  private T findWithMatchingType(SnapshotSource snapshotSource, Class<T> type,
                                 String identifier, VersionCorrection versionCorrection) {
    return selectResult(type, identifier, snapshotSource.get(type, identifier, versionCorrection));
  }

  @SuppressWarnings("unchecked")
  private T findWithGeneralType(SnapshotSource snapshotSource, final Class<T> type,
                                String identifier, VersionCorrection versionCorrection) {

    // Filter the items so we only have ones with compatible types
    Collection<SnapshotItem<Object>> allMatches = snapshotSource.get(Object.class, identifier, versionCorrection);
    Iterable<SnapshotItem<Object>> results = filterForCorrectType(allMatches, type);

    final T result = (T) selectResult(type, identifier, results);
    if (result != null) {
      return result;
    } else {
      throw new DataNotFoundException("No config found with type: [" + type.getName() + "], id: [" +
                                      identifier + "] and versionCorrection: [" + versionCorrection + "]");
    }
  }

  private Iterable<SnapshotItem<Object>> filterForCorrectType(Collection<SnapshotItem<Object>> allMatches,
                                                            Class<T> type) {
    return Iterables.filter(allMatches, typeMatcher(type));
  }

  /**
   * Predicate which can be used to check that each item passed to
   * it is of the required type.
   *
   * @param type the type to check items are, subclasses of the type will also match
   * @return the predicate to perform the type matching
   */
  private Predicate<SnapshotItem<Object>> typeMatcher(final Class<T> type) {
    return new Predicate<SnapshotItem<Object>>() {
      @Override
      public boolean apply(SnapshotItem<Object> item) {
        return type.isAssignableFrom(item.getValue().getClass());
      }
    };
  }

  private <R> R selectResult(Class<T> type, String identifier, Iterable<SnapshotItem<R>> results) {
    final Iterator<SnapshotItem<R>> iterator = results.iterator();
    return iterator.hasNext() ? selectFirst(type, identifier, iterator) : null;
  }

  private <R> R selectFirst(Class<T> type, String identifier, Iterator<SnapshotItem<R>> iterator) {
    R result = iterator.next().getValue();
    if (iterator.hasNext()) {
      s_logger.warn("Found multiple matching config results for type: {} and name: {} - returning first found",
                    type.getName(), identifier);
    }
    return result;
  }
}
