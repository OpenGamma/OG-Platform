/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.io.File;

import org.fudgemsg.FudgeContext;

/**
 * An implementation of {@link ViewComputationCacheSource} which will use an injected
 * {@link IdentifierMap} and construct {@link BerkeleyDBValueSpecificationBinaryDataStore}
 * instances on demand to satisfy cache requests.
 */
public class BerkeleyDBViewComputationCacheSource extends DefaultViewComputationCacheSource {

  public BerkeleyDBViewComputationCacheSource(IdentifierMap identifierMap, File dbDir, FudgeContext fudgeContext) {
    super(identifierMap, fudgeContext, new BerkeleyDBBinaryDataStoreFactory(dbDir));
  }

}
