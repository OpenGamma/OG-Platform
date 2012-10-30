/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.target.ComputationTargetSpecificationResolver;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.DefaultComputationTargetSpecificationResolver;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A computation target resolver backed solely by a {@code Map}.
 * <p>
 * This class is intended for use by a single thread.
 */
public class MapComputationTargetResolver implements ComputationTargetResolver {

  // [PLAT-444]: move to com.opengamma.engine.target

  /**
   * The backing map.
   */
  private final Map<ComputationTargetSpecification, ComputationTarget> _backingMap = Maps.newHashMap();

  /**
   * The backing specification resolver.
   */
  private final DefaultComputationTargetSpecificationResolver _specificationResolver = new DefaultComputationTargetSpecificationResolver();

  @Override
  public ComputationTarget resolve(ComputationTargetSpecification specification, VersionCorrection versionCorrection) {
    return _backingMap.get(specification);
  }

  @Override
  public ComputationTargetType simplifyType(ComputationTargetType type) {
    return type;
  }

  /**
   * Adds a target to the resolver.
   * 
   * @param target  the target to add, not null
   */
  public void addTarget(final ComputationTarget target) {
    ArgumentChecker.notNull(target, "target");
    _backingMap.put(target.toSpecification(), target);
  }

  @Override
  public SecuritySource getSecuritySource() {
    return null;
  }

  @Override
  public ComputationTargetSpecificationResolver getSpecificationResolver() {
    return _specificationResolver;
  }

  @Override
  public ComputationTargetResolver.AtVersionCorrection atVersionCorrection(final VersionCorrection versionCorrection) {
    final ComputationTargetSpecificationResolver.AtVersionCorrection specificationResolver = MapComputationTargetResolver.this.getSpecificationResolver().atVersionCorrection(versionCorrection);
    return new AtVersionCorrection() {

      @Override
      public ComputationTarget resolve(ComputationTargetSpecification specification) {
        return MapComputationTargetResolver.this.resolve(specification, versionCorrection);
      }

      @Override
      public ComputationTargetSpecificationResolver.AtVersionCorrection getSpecificationResolver() {
        return specificationResolver;
      }

      @Override
      public ComputationTargetType simplifyType(ComputationTargetType type) {
        return MapComputationTargetResolver.this.simplifyType(type);
      }
    };
  }

  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

  /**
   * Returns a string suitable for debugging.
   * 
   * @return the string, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[map=" + _backingMap + "]";
  }

}
