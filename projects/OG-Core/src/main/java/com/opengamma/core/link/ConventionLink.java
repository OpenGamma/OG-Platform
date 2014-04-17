/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.link;


import com.opengamma.core.convention.Convention;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.service.ServiceContext;

/**
 * Represents a link to a Convention object using an ExternalId or ExternalIdBundle
 * that is resolved on demand.  Use of links allows provision of Securities by remote
 * servers while maintaining the ability to capture updates to the linked resources
 * on each subsequent resolution.
 *
 * @param <T> type of the convention
 */
public abstract class ConventionLink<T extends Convention> implements Link<T> {

  /**
   * Package protected no arg constructor so only subclasses in the package can use.
   */
  /* package */ ConventionLink() {
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
  public static <C extends Convention> ConventionLink<C> of(Class<C> type, ExternalIdBundle bundle) {
    return new ResolvableConventionLink<>(type, bundle, new ServiceContextConventionLinkResolver<C>());
  }
  
  /**
   * Creates a link that will use a service context accessed via a thread local
   * to access a pre-configured service context containing the ConventionSource
   * and VersionCorrectionProvider necessary to resolve the provided externalId
   * into the target object.  Try to use the bundle version of this call bundles
   * where possible rather than a single externalId.
   *
   * @param type the type of object being linked to
   * @param externalId the external id to be resolved into the target object, not null
   * @return a convention link
   */
  public static <C extends Convention> ConventionLink<C> of(Class<C> type, ExternalId externalId) {
    return of(type, externalId.toBundle());
  }
  
  /**
   * Creates a link that embeds the provided object directly. This should be used
   * with cautionas it will not update if the underlying object is updated
   * via another data source or by a change in the VersionCorrection environment.
   *
   * @param <C> the type of the underlying Convention the link refers to
   * @param convention the convention to embed in the link, not null
   * @return the convention link
   */
  public static <C extends Convention> ConventionLink<C> of(C convention) {
    return new FixedConventionLink<>(convention);
  }
  
  /**
   * Creates a link that will use the provided service context to resolve the link
   * rather than use one available via a thread local environment.  Use of this
   * method should only be necessary when you need to use resolution outside of
   * the current VersionCorrection threadlocal environment.
   *
   * @param <C> the type of the underlying Convention the link refers to
   * @param type the type of object being linked to
   * @param bundle the external id bundle to use as the link reference, not null
   * @param serviceContext a service context containing the ConventionSource and
   * VersionCorrectionProvider necessary to resolve, not null
   * @return the convention link
   */
  public static <C extends Convention> ConventionLink<C> of(Class<C> type, ExternalIdBundle bundle,
                                                            ServiceContext serviceContext) {
    return new ResolvableConventionLink<>(type, bundle, new ServiceContextConventionLinkResolver<C>(serviceContext));
  }
  
  /**
   * Creates a link that will use the provided service context to resolve the
   * link rather than use one available via a thread local environment.  Use of
   * this method should only be necessary when you need to use resolution outside
   * of the current VersionCorrection threadlocal environment.  Links should be
   * alternatively created from bundles where possible.
   *
   * @param <C> the type of the underlying Convention the link refers to
   * @param type the type of object being linked to
   * @param externalId a single ExternalId to use as the link reference, not null
   * @param serviceContext a service context containing the ConvenetionSource and
   * VersionCorrectionProvider necessary to resolve, not null
   * @return the convention link
   */
  public static <C extends Convention> ConventionLink<C> of(Class<C> type, ExternalId externalId,
                                                            ServiceContext serviceContext) {
    return of(type, externalId.toBundle(), serviceContext);
  }
}
