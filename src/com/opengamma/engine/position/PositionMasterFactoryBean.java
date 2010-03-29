/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import org.springframework.beans.factory.FactoryBean;

/**
 * A FactoryBean abstract base for constructing PositionMaster objects. Using this can make
 * the Spring config cleaner - create a factory in Java for complex initiations and inject that
 * as a parameter instead.
 * 
 * @author Andrew Griffin
 */
public abstract class PositionMasterFactoryBean implements FactoryBean {

  @Override
  public abstract PositionMaster getObject();

  @Override
  public Class<PositionMaster> getObjectType() {
    return PositionMaster.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
  
}
