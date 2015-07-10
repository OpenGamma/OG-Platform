/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.link;

import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.NamedSnapshot;
import com.opengamma.id.VersionCorrection;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.VersionCorrectionProvider;

/**
 * Link resolver to resolve snapshot links using a ServiceContext.
 *
 * @param <T> the type of config object to be resolved
 */
final class ServiceContextSnapshotLinkResolver<T extends NamedSnapshot>
    extends SourceLinkResolver<String, T, MarketDataSnapshotSource> {

  /**
   * Creates the resolver using the default service context.
   */
  ServiceContextSnapshotLinkResolver() {
    super();
  }

  /**
   * Creates the resolver using the supplied service context.
   *
   * @param serviceContext the service context to use when resolving the link
   */
  ServiceContextSnapshotLinkResolver(ServiceContext serviceContext) {
    super(serviceContext);
  }

  @Override
  protected Class<MarketDataSnapshotSource> getSourceClass() {
    return MarketDataSnapshotSource.class;
  }

  @Override
  protected VersionCorrection getVersionCorrection(VersionCorrectionProvider vcProvider) {
    return vcProvider.getConfigVersionCorrection();
  }

  @Override
  protected T executeQuery(MarketDataSnapshotSource snapshotSource, Class<T> type,  String name, VersionCorrection versionCorrection) {
    return snapshotSource.getSingle(type, name, versionCorrection);
  }
}
