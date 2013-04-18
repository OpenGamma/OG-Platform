/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.target.ComputationTargetSpecificationResolver;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.resolver.ObjectResolver;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A target resolver implementation that delegates to a backing resolver.
 * <p>
 * This can be used to implement additional behavior on top of an underlying resolver as per the decorator pattern.
 */
public abstract class DelegatingComputationTargetResolver implements ComputationTargetResolver {

  // [PLAT-444]: move to com.opengamma.engine.target

  /**
   * The underlying resolver.
   */
  private final ComputationTargetResolver _underlying;

  /**
   * Creates an instance specifying the underlying resolver.
   * 
   * @param underlying the underlying resolver, not null
   */
  public DelegatingComputationTargetResolver(final ComputationTargetResolver underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  /**
   * Gets the underlying resolver.
   * 
   * @return the underlying resolver, not null
   */
  protected ComputationTargetResolver getUnderlying() {
    return _underlying;
  }

  @Override
  public ComputationTarget resolve(final ComputationTargetSpecification specification, final VersionCorrection versionCorrection) {
    return getUnderlying().resolve(specification, versionCorrection);
  }

  @Override
  public ObjectResolver<?> getResolver(final ComputationTargetSpecification specification) {
    return getUnderlying().getResolver(specification);
  }

  @Override
  public ComputationTargetType simplifyType(final ComputationTargetType type) {
    return getUnderlying().simplifyType(type);
  }

  @Override
  public SecuritySource getSecuritySource() {
    return getUnderlying().getSecuritySource();
  }

  @Override
  public ComputationTargetSpecificationResolver getSpecificationResolver() {
    return getUnderlying().getSpecificationResolver();
  }

  @Override
  public ComputationTargetResolver.AtVersionCorrection atVersionCorrection(final VersionCorrection versionCorrection) {
    return getUnderlying().atVersionCorrection(versionCorrection);
  }

  @Override
  public ChangeManager changeManager() {
    return getUnderlying().changeManager();
  }

}
