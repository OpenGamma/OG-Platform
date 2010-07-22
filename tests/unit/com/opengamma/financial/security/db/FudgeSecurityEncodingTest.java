/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import static org.junit.Assert.assertEquals;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.security.Security;
import com.opengamma.financial.fudgemsg.FinancialFudgeContextConfiguration;
import com.opengamma.financial.security.FinancialSecurity;

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

  private void roundTrip(final FinancialSecurity security) {
    final FudgeSerializationContext context = new FudgeSerializationContext(s_fudgeContext);
    final MutableFudgeFieldContainer message = context.newMessage();
    security.toFudgeMsg(context, message);
    FudgeSerializationContext.addClassHeader(message, security.getClass());
    s_logger.debug("Security {} encoded to {}", security, message);
    final Security decoded = s_fudgeContext.fromFudgeMsg(Security.class, message);
    s_logger.debug("Message {} decoded to {}", message, decoded);
    testEquals(security, decoded);
  }

  @Test
  public void testAAPLEquitySecurity() {
    roundTrip(HibernateSecurityMasterTestUtils.makeExpectedAAPLEquitySecurity());
  }

  @Test
  public void testAPVLEquityOptionSecurity() {
    roundTrip(HibernateSecurityMasterTestUtils.makeAPVLEquityOptionSecurity());
  }

  @Test
  public void testSPXIndexOptionSecurity() {
    roundTrip(HibernateSecurityMasterTestUtils.makeSPXIndexOptionSecurity());
  }

  @Test
  public void testWheatFuture() {
    roundTrip(HibernateSecurityMasterTestUtils.makeWheatFuture());
  }

  @Test
  public void testIndexFuture() {
    roundTrip(HibernateSecurityMasterTestUtils.makeIndexFuture());
  }

  @Test
  public void testAUDUSDCurrencyFuture() {
    roundTrip(HibernateSecurityMasterTestUtils.makeAUDUSDCurrencyFuture());
  }

  @Test
  public void testEuroBondFuture() {
    roundTrip(HibernateSecurityMasterTestUtils.makeEuroBondFuture());
  }

  @Test
  public void testSilverFuture() {
    roundTrip(HibernateSecurityMasterTestUtils.makeSilverFuture());
  }

  @Test
  public void testEthanolFuture() {
    roundTrip(HibernateSecurityMasterTestUtils.makeEthanolFuture());
  }

  @Test
  public void testInterestRateFuture() {
    roundTrip(HibernateSecurityMasterTestUtils.makeInterestRateFuture());
  }

}
