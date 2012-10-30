/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.util.ArgumentChecker;

/**
 * A computation target resolver backed solely by a {@code Map}.
 * <p>
 * This class is intended for use by a single thread.
 */
public class MapComputationTargetResolver implements ComputationTargetResolver {

  // TODO: move to com.opengamma.engine.target

  /**
   * The backing map.
   */
  private final Map<ComputationTargetSpecification, ComputationTarget> _backingMap = Maps.newHashMap();

  //-------------------------------------------------------------------------
  @Override
  public ComputationTarget resolve(ComputationTargetSpecification specification) {
    return _backingMap.get(specification); 
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
  public PositionSource getPositionSource() {
    return null;
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
