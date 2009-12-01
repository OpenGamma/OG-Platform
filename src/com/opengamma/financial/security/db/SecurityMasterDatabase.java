/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 *
 * @author jim
 */
public class SecurityMasterDatabase {
  private static ClassPathXmlApplicationContext _appContext;

  public static void main(String[] args) {
    _appContext = new ClassPathXmlApplicationContext("com/opengamma/financial/security/db/security-master-context.xml");
    _appContext.start();
  }
}
