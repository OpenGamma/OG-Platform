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
import com.opengamma.engine.target.resolver.DeepResolver;
import com.opengamma.engine.target.resolver.ObjectResolver;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A computation target resolver backed solely by a {@code Map}.
 * <p>
 * This class is intended for use by a single thread.
 */
public class MapComputationTargetResolver extends DefaultComputationTargetResolver {

  // [PLAT-444]: move to com.opengamma.engine.target

  /**
   * The backing map.
   */
  private final Map<ComputationTargetSpecification, ComputationTarget> _backingMap = Maps.newHashMap();

  @Override
  public ComputationTarget resolve(final ComputationTargetSpecification specification, final VersionCorrection versionCorrection) {
    ComputationTarget resolved = _backingMap.get(specification);
    if (resolved == null) {
      resolved = super.resolve(specification, versionCorrection);
    }
    return resolved;
  }

  @Override
  public ObjectResolver<?> getResolver(final ComputationTargetSpecification specification) {
    final ComputationTarget resolved = _backingMap.get(specification);
    if (resolved != null) {
      return new ObjectResolver<UniqueIdentifiable>() {

        @Override
        public ChangeManager changeManager() {
          return DummyChangeManager.INSTANCE;
        }

        @Override
        public UniqueIdentifiable resolveObject(UniqueId uniqueId, VersionCorrection versionCorrection) {
          return resolved.getUniqueId();
        }

        @Override
        public DeepResolver deepResolver() {
          return null;
        }

      };
    } else {
      return super.getResolver(specification);
    }
  }

  /**
   * Adds a target to the resolver.
   * 
   * @param target the target to add, not null
   */
  public void addTarget(final ComputationTarget target) {
    ArgumentChecker.notNull(target, "target");
    _backingMap.put(target.toSpecification(), target);
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
