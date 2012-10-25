/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.AbstractMaster;

/**
 * This class is needed to work around problem with method name clash with generics 
 * when {@code ConfigMaster} interface is directly implemented
 */
/*package*/ abstract class AbstractInMemoryMaster<D extends AbstractDocument> implements AbstractMaster<D> {

  @Override
  public D get(UniqueId uniqueId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public D get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    throw new UnsupportedOperationException();
  }

  @Override
  public D add(D document) {
    throw new UnsupportedOperationException();
  }

  @Override
  public D update(D document) {
    throw new UnsupportedOperationException();
  }

  @Override
  public D correct(D document) {
    throw new UnsupportedOperationException();
  }

}
