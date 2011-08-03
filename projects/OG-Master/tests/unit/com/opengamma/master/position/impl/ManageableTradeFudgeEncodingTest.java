/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import static org.testng.AssertJUnit.fail;

import java.math.BigDecimal;

import javax.time.calendar.LocalDate;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Test ManageableTrade Fudge.
 */
@Test
public class ManageableTradeFudgeEncodingTest {

  private static final Logger s_logger = LoggerFactory.getLogger(ManageableTradeFudgeEncodingTest.class);
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  public void test() {
    ManageableTrade obj = new ManageableTrade();
    obj.setUniqueId(UniqueIdentifier.of("U", "1"));
    obj.setQuantity(BigDecimal.ONE);
    obj.setSecurityKey(IdentifierBundle.of(Identifier.of("A", "B")));
    obj.setTradeDate(LocalDate.of(2011, 6, 1));
    obj.setCounterpartyKey(Identifier.of("C", "D"));
    testFudgeMessage(obj);
  }

  private void testFudgeMessage(final ManageableTrade obj) {
    final FudgeSerializationContext context = new FudgeSerializationContext(s_fudgeContext);
    FudgeMsg msg = context.objectToFudgeMsg(obj);
    s_logger.debug("ManageableTrade {}", obj);
    s_logger.debug("Encoded to {}", msg);
    final byte[] bytes = s_fudgeContext.toByteArray(msg);
    msg = s_fudgeContext.deserialize(bytes).getMessage();
    s_logger.debug("Serialised to {}", msg);
    final ManageableTrade decoded = s_fudgeContext.fromFudgeMsg(ManageableTrade.class, msg);
    s_logger.debug("Decoded to {}", decoded);
    if (!obj.equals(decoded)) {
      s_logger.warn("Expected {}", obj);
      s_logger.warn("Received {}", decoded);
      fail();
    }
  }

}
