/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.link;

import java.io.Serializable;

import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.VersionCorrection;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.VersionCorrectionProvider;

/**
 * Link resolver to resolve links using a ServiceContext.
 *
 * @param <C> the type of convention object to be resolved
 */
/* package */ final class ServiceContextConventionLinkResolver<C extends Convention>
    extends SourceLinkResolver<ExternalIdBundle, C, ConventionSource> implements Serializable {

  /**
   * Creates the resolver using the default service context.
   */
   /* package */ ServiceContextConventionLinkResolver() {
    super();
  }

  /**
   * Creates the resolver using the supplied service context.
   *
   * @param serviceContext the service context to use when resolving the link
   */
  /* package */ ServiceContextConventionLinkResolver(ServiceContext serviceContext) {
    super(serviceContext);
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
  protected C executeQuery(ConventionSource source, Class<C> type, ExternalIdBundle identifier,
                           VersionCorrection versionCorrection) {
    // ConventionSource already throws DataNotFoundException when there is no data
    return (C) source.getSingle(identifier, versionCorrection);
  }
}
