/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.link;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries2.HistoricalDataRequest;
import com.opengamma.core.historicaltimeseries2.HistoricalTimeSeriesSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.VersionCorrection;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.VersionCorrectionProvider;

/**
 * Represents a link to a Security object using an ExternalId or ExternalIdBundle
 * that is resolved on demand. Use of links allows provision of Securities by remote
 * servers while maintaining the ability to capture updates to the linked resources on
 * each subsequent resolution.
 *
 * @param <T> type of the security
 */
public abstract class SecurityLink<T extends Security> extends AbstractLink<ExternalIdBundle, T> {

  @SuppressWarnings("unchecked")
  private SecurityLink(ExternalIdBundle bundle, LinkResolver<T> resolver) {
    super(bundle, (Class<T>) Security.class, resolver);
  }
  
  /**
   * Creates a link that will use a service context accessed via a thread local
   * to access a pre-configured service context containing the SecuritySource
   * and VersionCorrectionProvider necessary to resolve the provided bundle into
   * the target object.
   *
   * @param <S> the type of the object being linked to
   * @param bundle the external id bundle to be resolved into the target object, not null
   * @return a security link
   */
  public static <S extends Security> SecurityLink<S> of(ExternalIdBundle bundle) {
    return of(bundle, null);
  }
  
  /**
   * Creates a link that will use a service context accessed via a thread local to
   * access a pre-configured service context containing the SecuritySource and
   * VersionCorrectionProvider necessary to resolve the provided externalId into
   * the target object. Try to use the bundle version of this call bundles where
   * possible rather than a single externalId.
   *
   * @param <S> the type of the object being linked to
   * @param externalId the external id to be resolved into the target object, not null
   * @return a security link  
   */
  public static <S extends Security> SecurityLink<S> of(ExternalId externalId) {
    return of(externalId.toBundle());
  }
  
  /**
   * Creates a link that embeds the provided object directly. This should only
   * be used for testing as it will not update if the underlying object is updated
   * via another data source or by a change in the VersionCorrection environment.
   *
   * @param <S> the type of the underlying Security the link refers to
   * @param security the security to embed in the link, not null
   * @param timeSeries the timeseries to be returned
   * @param marketDataResult the market data to be returned
   * @return the security link
   */
  public static <S extends Security> SecurityLink<S> of(S security, final HistoricalTimeSeries timeSeries,
                                                        final MarketDataResult marketDataResult) {

    return new SecurityLink<S>(security.getExternalIdBundle(), new FixedLinkResolver<>(security)) {
      @Override
      public HistoricalTimeSeries getHistoricalData(HistoricalDataRequest request) {
        return timeSeries;
      }

      @Override
      public MarketDataResult getCurrentData(String field) {
        return marketDataResult;
      }
    };
  }
  
  /**
   * Creates a link that will use the provided service context to resolve the
   * link rather than use one available via a thread local environment. Use
   * of this method should only be necessary when you need to use resolution
   * outside of the current VersionCorrection threadlocal environment.
   *
   * @param <S> the type of the underlying Security the link refers to
   * @param bundle the external id bundle to use as the link reference, not null
   * @param serviceContext a service context containing the SecuritySource and
   * VersionCorrectionProvider necessary to resolve, not null
   * @return the security link
   */
  public static <S extends Security> SecurityLink<S> of(ExternalIdBundle bundle, final ServiceContext serviceContext) {
    return new SecurityLink<S>(bundle, new ServiceContextSecurityLinkResolver<S>(bundle, serviceContext)) {
      @Override
      public HistoricalTimeSeries getHistoricalData(HistoricalDataRequest request) {
        //VersionCorrectionProvider vcProvider = serviceContext.getService(VersionCorrectionProvider.class);
        HistoricalTimeSeriesSource htsSource = serviceContext.get(HistoricalTimeSeriesSource.class);
        return htsSource.getHistoricalTimeSeries(request);
      }

      @Override
      public MarketDataResult getCurrentData(String field) {
        return null;
      }
    };
  }
  
  /**
   * Creates a link that will use the provided service context to resolve
   * the link rather than use one available via a thread local environment.
   * Use of this method should only be necessary when you need to use resolution
   * outside of the current VersionCorrection threadlocal environment. Links
   * should be alternatively created from bundles where possible.
   *
   * @param <S> the type of the underlying Security the link refers to
   * @param externalId a single ExternalId to use as the link reference, not null
   * @param serviceContext a service context containing the SecuritySource and VersionCorrectionProvider necessary to resolve, not null
   * @return the security link
   */
  public static <S extends Security> SecurityLink<S> of(ExternalId externalId, ServiceContext serviceContext) {
    return of(externalId.toBundle(), serviceContext);
  }
  
  /**
   * Create a new SecurityLink, with the same ID bundle as this one that
   * uses a newly provided serviceContext. This should only be necessary
   * when you need to use resolution outside of the current VersionCorrection
   * threadlocal environment.
   *
   * @param <S> the type of the underlying Security the link refers to
   * @param serviceContext a service context containing the SecuritySource and VersionCorrectionProvider necessary to resolve, not null
   * @return a new security link
   */
  public <S extends Security> SecurityLink<S> with(ServiceContext serviceContext) {
    return of(getIdentifier(), serviceContext);
  }

  /**
   * Resolve the link and get the linked timeseries
   *
   * @param request the request object
   * @return the time series
   */
  public abstract HistoricalTimeSeries getHistoricalData(HistoricalDataRequest request);

  /**
   * Resolve the link and get the linked market data
   *
   * @param field the field to retrieve
   * @return the market data
   */
  public abstract MarketDataResult getCurrentData(String field);

  /**
   * Private link resolver to resolve links using a ServiceContext.
   *
   * @param <S> the type of security object to be resolved
   */
  private static final class ServiceContextSecurityLinkResolver<S extends Security>
      extends SourceLinkResolver<ExternalIdBundle, S, SecuritySource> {

     // Private constructor as only for use by enclosing class
    private ServiceContextSecurityLinkResolver(ExternalIdBundle bundle, ServiceContext serviceContext) {
      super(bundle, serviceContext);
    }

    @Override
    protected Class<SecuritySource> getSourceClass() {
      return SecuritySource.class;
    }

    @Override
    protected VersionCorrection getVersionCorrection(VersionCorrectionProvider vcProvider) {
      return vcProvider.getPortfolioVersionCorrection();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected S executeQuery(SecuritySource source, VersionCorrection versionCorrection) {
      final S result = (S) source.getSingle(getIdentifier(), versionCorrection);
      if (result != null) {
        return result;
      } else {
        throw new DataNotFoundException("No security found with id bundle: [" + getIdentifier() +
                                        "] and versionCorrection: [" + versionCorrection + "]");
      }
    }
  }
}
