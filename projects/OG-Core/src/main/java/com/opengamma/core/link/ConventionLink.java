/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.link;

import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.VersionCorrection;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.VersionCorrectionProvider;

/**
 * Represents a link to a Convention object using an ExternalId or ExternalIdBundle
 * that is resolved on demand.  Use of links allows provision of Securities by remote
 * servers while maintaining the ability to capture updates to the linked resources
 * on each subsequent resolution.
 *
 * @param <T> type of the convention
 */
public final class ConventionLink<T extends Convention> extends AbstractLink<ExternalIdBundle, T> {

  @SuppressWarnings("unchecked")
  private ConventionLink(ExternalIdBundle bundle, LinkResolver<T> resolver) {
    super(bundle, (Class<T>) Convention.class, resolver);
  }
  
  /**
   * Creates a link that will use a service context accessed via a thread local to
   * access a pre-configured service context containing the ConventionSource and
   * VersionCorrectionProvider necessary to resolve the provided bundle into the target
   * object.
   *
   * @param <C> the type of the object being linked to
   * @param bundle the external id bundle to be resolved into the target object, not null
   * @return a convention link
   */
  public static <C extends Convention> ConventionLink<C> of(ExternalIdBundle bundle) {
    return new ConventionLink<>(bundle, new ServiceContextConventionLinkResolver<C>(bundle));
  }
  
  /**
   * Creates a link that will use a service context accessed via a thread local
   * to access a pre-configured service context containing the ConventionSource
   * and VersionCorrectionProvider necessary to resolve the provided externalId
   * into the target object.  Try to use the bundle version of this call bundles
   * where possible rather than a single externalId.
   *
   * @param <C> the type of the object being linked to
   * @param externalId the external id to be resolved into the target object, not null
   * @return a convention link  
   */
  public static <C extends Convention> ConventionLink<C> of(ExternalId externalId) {
    return of(externalId.toBundle());
  }
  
  /**
   * Creates a link that embeds the provided object directly.  This should only
   * be used for testing as it will not update if the underlying object is updated
   * via another data source or by a change in the VersionCorrection environment.
   *
   * @param <C> the type of the underlying Convention the link refers to
   * @param convention the convention to embed in the link, not null
   * @return the convention link
   */
  public static <C extends Convention> ConventionLink<C> of(C convention) {
    return new ConventionLink<>(convention.getExternalIdBundle(), new FixedLinkResolver<>(convention));
  }
  
  /**
   * Creates a link that will use the provided service context to resolve the link
   * rather than use one available via a thread local environment.  Use of this
   * method should only be necessary when you need to use resolution outside of
   * the current VersionCorrection threadlocal environment.
   *
   * @param <C> the type of the underlying Convention the link refers to
   * @param bundle the external id bundle to use as the link reference, not null
   * @param serviceContext a service context containing the ConventionSource and
   * VersionCorrectionProvider necessary to resolve, not null
   * @return the convention link
   */
  public static <C extends Convention> ConventionLink<C> of(ExternalIdBundle bundle, ServiceContext serviceContext) {
    return new ConventionLink<>(bundle, new ServiceContextConventionLinkResolver<C>(bundle, serviceContext));
  }
  
  /**
   * Creates a link that will use the provided service context to resolve the
   * link rather than use one available via a thread local environment.  Use of
   * this method should only be necessary when you need to use resolution outside
   * of the current VersionCorrection threadlocal environment.  Links should be
   * alternatively created from bundles where possible.
   *
   * @param <C> the type of the underlying Convention the link refers to
   * @param externalId a single ExternalId to use as the link reference, not null
   * @param serviceContext a service context containing the ConvenetionSource and
   * VersionCorrectionProvider necessary to resolve, not null
   * @return the convention link
   */
  public static <C extends Convention> ConventionLink<C> of(ExternalId externalId, ServiceContext serviceContext) {
    return of(externalId.toBundle(), serviceContext);
  }
  
  /**
   * Create a new ConventionLink, with the same ID bundle as this one that uses
   * a newly provided serviceContext.  This should only be necessary when you
   * need to use resolution outside of the current VersionCorrection threadlocal
   * environment.
   *
   * @param serviceContext a service context containing the ConventionSource and
   * VersionCorrectionProvider necessary to resolve, not null
   * @return a new convention link
   */
  public ConventionLink<T> with(ServiceContext serviceContext) {
    return of(getIdentifier(), serviceContext);
  }

  /**
   * Private link resolver to resolve links using a ServiceContext.
   *
   * @param <C> the type of convention object to be resolved
   */
  private static final class ServiceContextConventionLinkResolver<C extends Convention>
      extends SourceLinkResolver<ExternalIdBundle, C, ConventionSource> {

    // Private constructor as only for use by enclosing class
    private ServiceContextConventionLinkResolver(ExternalIdBundle bundle) {
      this(bundle, null);
    }

    // Private constructor as only for use by enclosing class
    private ServiceContextConventionLinkResolver(ExternalIdBundle bundle, ServiceContext serviceContext) {
      super(bundle, serviceContext);
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
    protected C executeQuery(ConventionSource source, VersionCorrection versionCorrection) {
      // ConfigSource already throws DataNotFoundException when there is no data
      return (C) source.getSingle(getIdentifier(), versionCorrection);
    }
  }
}
