/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

/**
 * Generic TestCase for a SecurityMaster implementation.
 */
public interface SecurityMasterTestCaseMethods {

  void aaplEquityByBbgTicker() throws Exception;

  void aaplEquityByUniqueIdentifier() throws Exception;

  void apvEquityOptionByBbgTicker() throws Exception;

  void spxIndexOptionByBbgTicker() throws Exception;

  void spxIndexOptionByBbgUnique() throws Exception;

  void agricultureFuture() throws Exception;

  void indexFuture() throws Exception;

  void governmentBondSecurityBean();

  void testGovernmentBondSecurityBean();

  void currencyFuture() throws Exception;

  void euroBondFuture() throws Exception;

  void metalFuture() throws Exception;

  void energyFuture() throws Exception;

  void interestRateFuture() throws Exception;

  void update() throws Exception;

}
