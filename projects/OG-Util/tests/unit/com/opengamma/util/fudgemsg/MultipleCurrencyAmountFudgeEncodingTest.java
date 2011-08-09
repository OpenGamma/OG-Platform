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
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Test MultipleCurrencyAmount Fudge.
 */
@Test
public class MultipleCurrencyAmountFudgeEncodingTest {

  private static final Logger s_logger = LoggerFactory.getLogger(MultipleCurrencyAmountFudgeEncodingTest.class);
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  public void test() {
    MultipleCurrencyAmount mca = MultipleCurrencyAmount.of(Currency.AUD, 101);
    mca = mca.plus(Currency.GBP, 300);
    mca = mca.plus(Currency.USD, 400);
    testFudgeMessage(mca);
  }

  private void testFudgeMessage(final MultipleCurrencyAmount mca) {
    final FudgeSerializer serializer = new FudgeSerializer(s_fudgeContext);
    FudgeMsg msg = serializer.objectToFudgeMsg(mca);
    s_logger.debug("MultipleCurrencyAmount {}", mca);
    s_logger.debug("Encoded to {}", msg);
    final byte[] bytes = s_fudgeContext.toByteArray(msg);
    msg = s_fudgeContext.deserialize(bytes).getMessage();
    s_logger.debug("Serialised to {}", msg);
    final MultipleCurrencyAmount decoded = s_fudgeContext.fromFudgeMsg(MultipleCurrencyAmount.class, msg);
    s_logger.debug("Decoded to {}", decoded);
    if (!mca.equals(decoded)) {
      s_logger.warn("Expected {}", mca);
      s_logger.warn("Received {}", decoded);
      fail();
    }
  }

}
