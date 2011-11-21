/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.wire.FudgeMsgReader;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class CurrencyPairFudgeSecondaryTypeTest {

  private static final String CURRENCY_PAIR = "currencyPair";

  /**
   * Writes a {@link CurrencyPair} to a Fudge message and reads it back.
   */
  @Test
  public void roundTrip() {
    FudgeContext context = OpenGammaFudgeContext.getInstance();
    CurrencyPair eurUsd = CurrencyPair.of("EUR/USD");
    MutableFudgeMsg msg = context.newMessage();
    msg.add(CURRENCY_PAIR, eurUsd);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    FudgeMsgWriter writer = context.createMessageWriter(baos);
    writer.writeMessage(msg);
    writer.close();

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    FudgeMsgReader reader = context.createMessageReader(bais);
    FudgeMsg rebuiltMsg = reader.nextMessage();
    AssertJUnit.assertNotNull(rebuiltMsg);
    FudgeField currencyPairField = rebuiltMsg.getByName(CURRENCY_PAIR);
    AssertJUnit.assertNotNull(currencyPairField);
    AssertJUnit.assertEquals(String.class, currencyPairField.getType().getJavaType());
    CurrencyPair currencyPair = rebuiltMsg.getFieldValue(CurrencyPair.class, currencyPairField);
    AssertJUnit.assertEquals(eurUsd, currencyPair);
  }
}
