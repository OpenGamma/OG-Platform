/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.link;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.Source;
import com.opengamma.id.VersionCorrection;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.util.ArgumentChecker;

/**
 * Responsible for performing the resolve operation of a link using
 * an appropriate {@link Source} instance.
 *
 * @param <I> the type of the identifier used by the resolver
 * @param <T> the type of object provided by the resolver
 * @param <S> the source used to resolve the link
 */
/* package */ abstract class SourceLinkResolver<I, T, S extends Source<?>> implements LinkResolver<T> {

  /**
   * The identifier to use when resolving.
   */
  private final I _identifier;
  /**
   * The specific service context to be used to look up service providers.
   * May be null in which case a thread-local context will be used.
   */
  private final ServiceContext _serviceContext;

  /**
   * Create the link resolver.
   *
   * @param identifier the identifier to be used for resolution, not null.
   * @param serviceContext the context to use for service-providers, may be null.
   */
  protected SourceLinkResolver(I identifier, ServiceContext serviceContext) {
    _identifier = ArgumentChecker.notNull(identifier, "identifier");
    _serviceContext = serviceContext;
  }

  //-------------------------------------------------------------------------
  /**
   * Return the identifier to be used.
   *
   * @return the identifier
   */
  public I getIdentifier() {
    return _identifier;
  }

  @Override
  public T resolve() {
    VersionCorrectionProvider vcProvider = lookupService(VersionCorrectionProvider.class);
    S source = lookupService(getSourceClass());
    return executeQuery(source, getVersionCorrection(vcProvider));
  }

  private <R> R lookupService(Class<R> serviceClass) {
    return getServiceContext().get(serviceClass);
  }

  private ServiceContext getServiceContext() {
    ServiceContext serviceContext = _serviceContext;
    if (serviceContext == null) {
      serviceContext = ThreadLocalServiceContext.getInstance();
      if (serviceContext == null) {
        throw new IllegalStateException("No service context found for use by the current thread");
      }
    }
    return serviceContext;
  }

  //-------------------------------------------------------------------------
  /**
   * Get the {@link Source} class to be used to resolve the link target.
   *
   * @return the appropriate Source class, not null
   */
  protected abstract Class<S> getSourceClass();

  /**
   * Get the VersionCorrection to be used during the link resolution.
   * It is expected, but not required, that the supplied VersionCorrectionProvider is used.
   *
   * @param vcProvider  the version correction provider (retrieved from the service context), not null
   * @return the VersionCorrection to be used, not null
   */
  protected abstract VersionCorrection getVersionCorrection(VersionCorrectionProvider vcProvider);

  /**
   * Execute a query against the source to retrieve the target of the link.
   *
   * @param source the source to retrieve the object from, not null
   * @param versionCorrection  the version correction to be used during the query, not null
   * @return the target of the link
   * @throws DataNotFoundException if the link is not resolvable
   */
  protected abstract T executeQuery(S source, VersionCorrection versionCorrection);

}
