/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Supplier;
import com.opengamma.util.ArgumentChecker;

/**
 * A supplier of unique identifiers with different version numbers.
 */
public class VersionedUniqueIdSupplier implements Supplier<UniqueId> {

  /**
   * The scheme.
   */
  private final String _scheme;
  /**
   * The identifier value.
   */
  private final String _value;
  /**
   * The last version number issued.
   */
  private final AtomicLong _version = new AtomicLong();

  /**
   * Creates a new instance.
   * 
   * @param scheme the scheme to use, not null or empty
   * @param value the value to use, not null or empty
   */
  public VersionedUniqueIdSupplier(final String scheme, final String value) {
    ArgumentChecker.notEmpty(scheme, "scheme");
    ArgumentChecker.notEmpty(scheme, "value");
    _scheme = scheme;
    _value = value;
  }

  /**
   * Creates a new instance.
   * 
   * @param objectId the base object identifier, not null
   */
  public VersionedUniqueIdSupplier(final ObjectId objectId) {
    this(objectId.getScheme(), objectId.getValue());
  }

  // Supplier

  /**
   * Generates the next unique identifier.
   * 
   * @return the next unique identifier, not null
   */
  @Override
  public UniqueId get() {
    return UniqueId.of(_scheme, _value, Long.toString(_version.incrementAndGet()));
  }

}
