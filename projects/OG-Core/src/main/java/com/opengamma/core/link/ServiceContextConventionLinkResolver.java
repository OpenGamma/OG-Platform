/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.link;

import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.VersionCorrection;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.VersionCorrectionProvider;

/**
 * Private link resolver to resolve links using a ServiceContext.
 *
 * @param <C> the type of convention object to be resolved
 */
/* package */ final class ServiceContextConventionLinkResolver<C extends Convention>
    extends SourceLinkResolver<C, ExternalIdBundle, ConventionSource> {

  // Private constructor as only for use by enclosing class
  ServiceContextConventionLinkResolver() {
    super(null);
  }

  // Private constructor as only for use by enclosing class
  ServiceContextConventionLinkResolver(ServiceContext serviceContext) {
    super(serviceContext);
  }

  @Override
  public LinkResolver<C, ExternalIdBundle> withTargetType(Class<C> targetType) {
    return this;
  }

  @Override
  protected Class<ConventionSource> getSourceClass() {
    return ConventionSource.class;
  }

  @Override
  protected VersionCorrection getVersionCorrection(VersionCorrectionProvider vcProvider) {
    return vcProvider.getPortfolioVersionCorrection();
  }

  @Override
  @SuppressWarnings("unchecked")
  protected C executeQuery(ConventionSource source, ExternalIdBundle identifier, VersionCorrection versionCorrection) {
    // ConfigSource already throws DataNotFoundException when there is no data
    return (C) source.getSingle(identifier, versionCorrection);
  }
}
