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
   * The object identifier.
   */
  private final ObjectId _objectId;
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
    this(ObjectId.of(scheme, value));
  }

  /**
   * Creates a new instance.
   * 
   * @param objectId the base object identifier, not null
   */
  public VersionedUniqueIdSupplier(final ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    _objectId = objectId;
  }

  //-------------------------------------------------------------------------
  /**
   * Generates the next unique identifier.
   * 
   * @return the next unique identifier, not null
   */
  @Override
  public UniqueId get() {
    return _objectId.atVersion(Long.toString(_version.incrementAndGet()));
  }

}
