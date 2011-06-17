/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import static org.testng.AssertJUnit.fail;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierSearch;

/**
 * Test IdentifierSearch Fudge.
 */
@Test
public class IdentifierSearchFudgeEncodingTest {

  private static final Logger s_logger = LoggerFactory.getLogger(IdentifierSearchFudgeEncodingTest.class);
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  public void test_empty() {
    IdentifierSearch obj = new IdentifierSearch();
    testFudgeMessage(obj);
  }

  public void test_full() {
    IdentifierSearch obj = new IdentifierSearch(Identifier.of("A", "B"));
    testFudgeMessage(obj);
  }

  private void testFudgeMessage(final IdentifierSearch obj) {
    final FudgeSerializationContext context = new FudgeSerializationContext(s_fudgeContext);
    FudgeMsg msg = context.objectToFudgeMsg(obj);
    s_logger.debug("IdentifierSearch {}", obj);
    s_logger.debug("Encoded to {}", msg);
    final byte[] bytes = s_fudgeContext.toByteArray(msg);
    msg = s_fudgeContext.deserialize(bytes).getMessage();
    s_logger.debug("Serialised to {}", msg);
    final IdentifierSearch decoded = s_fudgeContext.fromFudgeMsg(IdentifierSearch.class, msg);
    s_logger.debug("Decoded to {}", decoded);
    if (!obj.equals(decoded)) {
      s_logger.warn("Expected {}", obj);
      s_logger.warn("Received {}", decoded);
      fail();
    }
  }

}
