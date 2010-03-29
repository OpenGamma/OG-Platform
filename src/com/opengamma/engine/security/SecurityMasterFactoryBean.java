/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import org.springframework.beans.factory.FactoryBean;

/**
 * A FactoryBean abstract base for constructing SecurityMaster objects. Using this can make
 * the Spring config cleaner - create a factory in Java for complex initiations and inject that
 * as a parameter instead.
 * 
 * @author Andrew Griffin
 */
public abstract class SecurityMasterFactoryBean implements FactoryBean {

  @Override
  public abstract SecurityMaster getObject();

  @Override
  public Class<SecurityMaster> getObjectType() {
    return SecurityMaster.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
  
}
