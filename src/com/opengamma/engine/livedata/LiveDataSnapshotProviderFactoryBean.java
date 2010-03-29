/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import org.springframework.beans.factory.FactoryBean;

/**
 * A FactoryBean abstract base for constructing LiveDataSnapshotProvider objects. Using this can make
 * the Spring config cleaner - create a factory in Java for complex initiations and inject that
 * as a parameter instead.
 * 
 * @author Andrew Griffin
 */
public abstract class LiveDataSnapshotProviderFactoryBean implements FactoryBean {

  @Override
  public abstract LiveDataSnapshotProvider getObject();

  @Override
  public Class<LiveDataSnapshotProvider> getObjectType() {
    return LiveDataSnapshotProvider.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
  
}
