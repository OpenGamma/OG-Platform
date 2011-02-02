/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import com.google.common.base.Supplier;
import com.opengamma.id.ObjectIdentifier;
import com.opengamma.id.ObjectIdentifierSupplier;
import com.opengamma.master.config.ConfigTypeMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple, in-memory implementation of {@code ConfigMaster}.
 * <p>
 * This master does not support versioning of configuration documents.
 * <p>
 * This implementation does not copy stored elements, making it thread-hostile.
 * As such, this implementation is currently most useful for testing scenarios.
 */
public class InMemoryConfigMaster extends DefaultConfigMaster {

  /**
   * The default scheme used for each {@link ObejctIdentifier}.
   */
  public static final String DEFAULT_OID_SCHEME = "MemCfg";

  /**
   * The supplied of identifiers.
   */
  private final Supplier<ObjectIdentifier> _objectIdSupplier;

  /**
   * Creates an instance using the default scheme for any {@link ObjectIdentifier}s created.
   */
  public InMemoryConfigMaster() {
    this(new ObjectIdentifierSupplier(DEFAULT_OID_SCHEME));
  }

  /**
   * Creates an instance specifying the supplier of object identifiers.
   * 
   * @param objectIdSupplier  the supplier of object identifiers, not null
   */
  public InMemoryConfigMaster(final Supplier<ObjectIdentifier> objectIdSupplier) {
    ArgumentChecker.notNull(objectIdSupplier, "objectIdSupplier");
    _objectIdSupplier = objectIdSupplier;
  }

  //-------------------------------------------------------------------------
  @Override
  protected <T> ConfigTypeMaster<T> createTypedMaster(Class<T> clazz) {
    return new InMemoryConfigTypeMaster<T>(_objectIdSupplier);
  }

}
