/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.link;

import java.io.Serializable;

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
/* package */ abstract class SourceLinkResolver<I, T, S extends Source<?>> implements LinkResolver<I, T> , Serializable {

  /**
   * The specific service context to be used to look up service providers.
   * May be null in which case a thread-local context will be used.
   */
  private final ServiceContext _serviceContext;

  /**
   * Create the link resolver.
   *
   * @param serviceContext the context to use for service-providers, not null.
   */
  /* package */ SourceLinkResolver(ServiceContext serviceContext) {
    _serviceContext = ArgumentChecker.notNull(serviceContext, "serviceContext");
  }

  /* package */ SourceLinkResolver() {
    _serviceContext = null;
  }

  private <R> R lookupService(Class<R> serviceClass) {
    return getServiceContext().get(serviceClass);
  }

  @Override
  public T resolve(LinkIdentifier<I, T> identifier) {
    VersionCorrectionProvider vcProvider = lookupService(VersionCorrectionProvider.class);
    S source = lookupService(getSourceClass());
    return executeQuery(source, identifier.getType(), identifier.getIdentifier(), getVersionCorrection(vcProvider));
  }

  /**
   * Return a service context. If one has been supplied to the class then it will
   * be used, else the {@link ThreadLocalServiceContext} will be used. If no context
   * can be found an exception will be thrown.
   *
   * @return a service context, if available
   * @throws IllegalStateException if no context is available
   */
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
   * @throws DataNotFoundException if the link is not resolved
   */
  protected abstract T executeQuery(S source, Class<T> type, I identifier, VersionCorrection versionCorrection);
}
