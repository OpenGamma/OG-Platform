/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position;

import static org.testng.AssertJUnit.fail;

import java.math.BigDecimal;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link ManageableTrade} Fudge.
 */
@Test(groups = TestGroup.UNIT)
public class ManageableTradeFudgeEncodingTest {

  private static final Logger s_logger = LoggerFactory.getLogger(ManageableTradeFudgeEncodingTest.class);
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  public void test() {
    ManageableTrade obj = new ManageableTrade();
    obj.setUniqueId(UniqueId.of("U", "1"));
    obj.setQuantity(BigDecimal.ONE);
    obj.setSecurityLink(new ManageableSecurityLink(ExternalId.of("A", "B")));
    obj.getSecurityLink().setObjectId(ObjectId.of("O", "1"));
    obj.setTradeDate(LocalDate.of(2011, 6, 1));
    obj.setCounterpartyExternalId(ExternalId.of("C", "D"));
    testFudgeMessage(obj);
  }

  private void testFudgeMessage(final ManageableTrade obj) {
    final FudgeSerializer serializer = new FudgeSerializer(s_fudgeContext);
    FudgeMsg msg = serializer.objectToFudgeMsg(obj);
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
