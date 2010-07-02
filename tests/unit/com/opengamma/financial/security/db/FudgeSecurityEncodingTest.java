/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import static org.junit.Assert.assertEquals;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.security.Security;
import com.opengamma.financial.fudgemsg.FinancialFudgeContextConfiguration;

public class FudgeSecurityEncodingTest {

  private static final Logger s_logger = LoggerFactory.getLogger(FudgeSecurityEncodingTest.class);

  private static final FudgeContext s_fudgeContext = new FudgeContext();

  static {
    s_fudgeContext.setConfiguration(FinancialFudgeContextConfiguration.INSTANCE);
  }

  private void testEquals(final Object expected, final Object actual) {
    // The securities don't all have "equals" methods, so the bit below is a bit of a kludge
    final FudgeFieldContainer expectedMsg = s_fudgeContext.toFudgeMsg(expected).getMessage();
    final FudgeFieldContainer actualMsg = s_fudgeContext.toFudgeMsg(actual).getMessage();
    s_logger.debug("expected = {}", expectedMsg);
    s_logger.debug("actual   = {}", actualMsg);
    assertEquals(expectedMsg, actualMsg);
  }

  private void roundTrip(final Security security) {
    final FudgeMsgEnvelope fme = s_fudgeContext.toFudgeMsg(security);
    final FudgeFieldContainer message = fme.getMessage();
    s_logger.debug("Security {} encoded to {}", security, message);
    final Security decoded = s_fudgeContext.fromFudgeMsg(Security.class, message);
    s_logger.debug("Message {} decoded to {}", message, decoded);
    testEquals(security, decoded);
  }

  @Test
  @Ignore("still working on this")
  public void testAAPLEquitySecurity() {
    roundTrip(HibernateSecurityMasterTestUtils.makeExpectedAAPLEquitySecurity());
  }

  @Test
  public void testAPVLEquityOptionSecurity() {
    roundTrip(HibernateSecurityMasterTestUtils.makeAPVLEquityOptionSecurity());
  }

  @Test
  @Ignore("still working on this")
  public void testSPXIndexOptionSecurity() {
    roundTrip(HibernateSecurityMasterTestUtils.makeSPXIndexOptionSecurity());
  }

  @Test
  @Ignore("still working on this")
  public void testWheatFuture() {
    roundTrip(HibernateSecurityMasterTestUtils.makeWheatFuture());
  }

  @Test
  @Ignore("still working on this")
  public void testIndexFuture() {
    roundTrip(HibernateSecurityMasterTestUtils.makeIndexFuture());
  }

  @Test
  @Ignore("still working on this")
  public void testAUDUSDCurrencyFuture() {
    roundTrip(HibernateSecurityMasterTestUtils.makeAUDUSDCurrencyFuture());
  }

  @Test
  @Ignore("still working on this")
  public void testEuroBondFuture() {
    roundTrip(HibernateSecurityMasterTestUtils.makeEuroBondFuture());
  }

  @Test
  @Ignore("still working on this")
  public void testSilverFuture() {
    roundTrip(HibernateSecurityMasterTestUtils.makeSilverFuture());
  }

  @Test
  @Ignore("still working on this")
  public void testEthanolFuture() {
    roundTrip(HibernateSecurityMasterTestUtils.makeEthanolFuture());
  }

  @Test
  @Ignore("still working on this")
  public void testInterestRateFuture() {
    roundTrip(HibernateSecurityMasterTestUtils.makeInterestRateFuture());
  }

}
