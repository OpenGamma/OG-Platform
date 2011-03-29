/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import static org.testng.AssertJUnit.fail;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.util.db.Paging;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Test Paging Fudge.
 */
@Test
public class PagingFudgeEncodingTest {

  private static final Logger s_logger = LoggerFactory.getLogger(PagingFudgeEncodingTest.class);
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  public void test() {
    Paging paging = new Paging(1, 20, 210);
    testFudgeMessage(paging);
  }

  private void testFudgeMessage(final Paging paging) {
    final FudgeSerializationContext context = new FudgeSerializationContext(s_fudgeContext);
    FudgeFieldContainer msg = context.objectToFudgeMsg(paging);
    s_logger.debug("Paging {}", paging);
    s_logger.debug("Encoded to {}", msg);
    final byte[] bytes = s_fudgeContext.toByteArray(msg);
    msg = s_fudgeContext.deserialize(bytes).getMessage();
    s_logger.debug("Serialised to {}", msg);
    final Paging decoded = s_fudgeContext.fromFudgeMsg(Paging.class, msg);
    s_logger.debug("Decoded to {}", decoded);
    if (!paging.equals(decoded)) {
      s_logger.warn("Expected {}", paging);
      s_logger.warn("Received {}", decoded);
      fail();
    }
  }

}
