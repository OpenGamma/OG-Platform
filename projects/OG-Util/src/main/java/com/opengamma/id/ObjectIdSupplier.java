/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Supplier;
import com.opengamma.util.ArgumentChecker;

/**
 * A supplier of object identifiers.
 * <p>
 * An object identifier consists of a scheme and value.
 * This class creates object identifiers for a fixed scheme name, where each
 * value is an incrementing number. The values are created in a thread-safe way.
 * <p>
 * This class is thread-safe and not externally mutable.
 */
public class ObjectIdSupplier implements Supplier<ObjectId>, Serializable {

  /**
   * The scheme.
   */
  private final String _scheme;
  /**
   * The generator of identifiers.
   */
  private final AtomicLong _idCount = new AtomicLong();

  /**
   * Creates an instance specifying the scheme.
   * 
   * @param scheme  the scheme, not empty
   */
  public ObjectIdSupplier(final String scheme) {
    ArgumentChecker.notEmpty(scheme, "scheme");
    _scheme = scheme;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the scheme in use.
   * 
   * @return the scheme, not null
   */
  public String getScheme() {
    return _scheme;
  }

  //-------------------------------------------------------------------------
  /**
   * Generates the next object identifier.
   * 
   * @return the next unique identifier, not null
   */
  public ObjectId get() {
    final long id = _idCount.incrementAndGet();
    return ObjectId.of(_scheme, Long.toString(id));
  }

  /**
   * Generates the next object identifier prefixing the value.
   * 
   * @param valuePrefix  the prefix for the value, not null
   * @return the next unique identifier, not null
   */
  public ObjectId getWithValuePrefix(final String valuePrefix) {
    final long id = _idCount.incrementAndGet();
    return ObjectId.of(_scheme, valuePrefix + Long.toString(id));
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "ObjectIdSupplier[" + _scheme + "]";
  }

}
