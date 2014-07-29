/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.link;

import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.service.ServiceContext;

/**
 * Represents a link to a Security object using an ExternalId or ExternalIdBundle
 * that is resolved on demand. Use of links allows provision of Securities by remote
 * servers while maintaining the ability to capture updates to the linked resources on
 * each subsequent resolution.
 *
 * @param <T> type of the security
 */
public abstract class SecurityLink<T extends Security> implements Link<T> {

  /**
   * Creates a link that will use a service context accessed via a thread local
   * to access a pre-configured service context containing the SecuritySource
   * and VersionCorrectionProvider necessary to resolve the provided bundle into
   * the target object.
   *
   * @param <S> type of the security
   * @param bundle the external id bundle to be resolved into the target object, not null
   * @param type the type of object being linked to, not null
   * @return a security link, not null
   */
  public static <S extends Security> SecurityLink<S> resolvable(ExternalIdBundle bundle, Class<S> type) {
    return new ResolvableSecurityLink<>(bundle, type, new ServiceContextSecurityLinkResolver<S>());
  }

  /**
   * Creates a link that will use a service context accessed via a thread local to
   * access a pre-configured service context containing the SecuritySource and
   * VersionCorrectionProvider necessary to resolve the provided externalId into
   * the target object. Try to use the bundle version of this call bundles where
   * possible rather than a single externalId.
   *
   * @param <S> the type of the underlying Security the link refers to
   * @param externalId the external id to be resolved into the target object, not null
   * @param type the type of object being linked to, not null
   * @return a security link, not null
   */
  public static <S extends Security> SecurityLink<S> resolvable(ExternalId externalId, Class<S> type) {
    return resolvable(externalId.toBundle(), type);
  }

  /**
   * Creates a link that will use a service context accessed via a thread local
   * to access a pre-configured service context containing the SecuritySource
   * and VersionCorrectionProvider necessary to resolve the provided bundle into
   * the target object. This version of the method will return a link where the
   * type is fixed to be Security.
   *
   * @param <S> the type of the underlying Security the link refers to
   * @param bundle the external id bundle to be resolved into the target object, not null
   * @return a security link, not null
   */
  @SuppressWarnings("unchecked")
  public static <S extends Security> SecurityLink<S> resolvable(ExternalIdBundle bundle) {
    return (SecurityLink<S>) resolvable(bundle, Security.class);
  }

  /**
   * Creates a link that will use a service context accessed via a thread local to
   * access a pre-configured service context containing the SecuritySource and
   * VersionCorrectionProvider necessary to resolve the provided externalId into
   * the target object. Try to use the bundle version of this call bundles where
   * possible rather than a single externalId. This version of the method will return
   * a link where the type is fixed to be Security.
   *
   * @param <S> the type of the underlying Security the link refers to
   * @param externalId the external id to be resolved into the target object, not null
   * @return a security link, not null
   */
  @SuppressWarnings("unchecked")
  public static <S extends Security> SecurityLink<S> resolvable(ExternalId externalId) {
    return (SecurityLink<S>) resolvable(externalId.toBundle(), Security.class);
  }

  /**
   * Creates a link that will use the provided service context to resolve the
   * link rather than use one available via a thread local environment. Use
   * of this method should only be necessary when you need to use resolution
   * outside of the current VersionCorrection threadlocal environment.
   *
   * @param <S> the type of the underlying Security the link refers to
   * @param bundle the external id bundle to use as the link reference, not null
   * @param type the type of object being linked to, not null
   * @param serviceContext a service context containing the SecuritySource and
   * VersionCorrectionProvider necessary to resolve, not null
   * @return the security link, not null
   */
  public static <S extends Security> SecurityLink<S> resolvable(ExternalIdBundle bundle, Class<S> type,
                                                                ServiceContext serviceContext) {
    return new ResolvableSecurityLink<>(bundle, type, new ServiceContextSecurityLinkResolver<S>(serviceContext));
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
   * @param type the type of object being linked to, not null
   * @param serviceContext a service context containing the SecuritySource and
   * VersionCorrectionProvider necessary to resolve, not null
   * @return the security link, not null
   */
  public static <S extends Security> SecurityLink<S> resolvable(ExternalId externalId,
                                                                Class<S> type,
                                                                ServiceContext serviceContext) {
    return resolvable(externalId.toBundle(), type, serviceContext);
  }

  /**
   * Creates a link that embeds the provided object directly. This should only
   * be used for testing as it will not update if the underlying object is updated
   * via another data source or by a change in the VersionCorrection environment.
   *
   * @param <S> the type of the underlying Security the link refers to
   * @param security the security to embed in the link, not null
   * @return the security link, not null
   */
  public static <S extends Security> SecurityLink<S> resolved(S security) {
    return new ResolvedSecurityLink<>(security);
  }

  /**
   * This is a temporary addition to allow links to operate in code that
   * relies on serialization of the identifier rather than a link. It
   * returns the bundle identifying the security. Note that a link
   * may not have an identifier in which case this method will throw an
   * {@link UnsupportedOperationException}.
   *
   * @return the identifier for a link if available, not null
   */
  public abstract ExternalIdBundle getIdentifier();

}
