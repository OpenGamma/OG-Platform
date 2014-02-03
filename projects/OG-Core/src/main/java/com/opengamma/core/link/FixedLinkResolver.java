/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.link;

import com.opengamma.util.ArgumentChecker;

/**
 * A resolver that designed to hold an actual instance of the underlying object that
 * the corresponding Link points to. This is useful for unit testing or scripting.
 * This should not be used in production engine code because it won't track
 * version/correction changes.
 *
 * @param <T> the type of the underlying object
 */
public class FixedLinkResolver<T> implements LinkResolver<T> {

  /**
   * The fixed instance that this resolver will resolve to.
   */
  private final T _instance;

  /**
   * Creates the resolver with a fixed instance.
   *
   * @param instance the instance the resolver should point to, not null
   */
  public FixedLinkResolver(T instance) {
    _instance = ArgumentChecker.notNull(instance, "instance");
  }

  @Override
  public T resolve() {
    return _instance;
  }
}
