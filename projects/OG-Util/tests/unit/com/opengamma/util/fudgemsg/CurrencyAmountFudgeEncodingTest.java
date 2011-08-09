/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import static org.testng.AssertJUnit.fail;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Test CurrencyAmount Fudge.
 */
@Test
public class CurrencyAmountFudgeEncodingTest {

  private static final Logger s_logger = LoggerFactory.getLogger(CurrencyAmountFudgeEncodingTest.class);
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  public void test() {
    CurrencyAmount ca = CurrencyAmount.of(Currency.AUD, 101);
    testFudgeMessage(ca);
  }

  private void testFudgeMessage(final CurrencyAmount ca) {
    final FudgeSerializer serializer = new FudgeSerializer(s_fudgeContext);
    FudgeMsg msg = serializer.objectToFudgeMsg(ca);
    s_logger.debug("CurrencyAmount {}", ca);
    s_logger.debug("Encoded to {}", msg);
    final byte[] bytes = s_fudgeContext.toByteArray(msg);
    msg = s_fudgeContext.deserialize(bytes).getMessage();
    s_logger.debug("Serialised to {}", msg);
    final CurrencyAmount decoded = s_fudgeContext.fromFudgeMsg(CurrencyAmount.class, msg);
    s_logger.debug("Decoded to {}", decoded);
    if (!ca.equals(decoded)) {
      s_logger.warn("Expected {}", ca);
      s_logger.warn("Received {}", decoded);
      fail();
    }
  }

}
