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

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdSearch;

/**
 * Test {@link ExternalIdSearch} Fudge.
 */
@Test
public class ExternalIdSearchFudgeEncodingTest {

  private static final Logger s_logger = LoggerFactory.getLogger(ExternalIdSearchFudgeEncodingTest.class);
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  public void test_empty() {
    ExternalIdSearch obj = new ExternalIdSearch();
    testFudgeMessage(obj);
  }

  public void test_full() {
    ExternalIdSearch obj = new ExternalIdSearch(ExternalId.of("A", "B"));
    testFudgeMessage(obj);
  }

  private void testFudgeMessage(final ExternalIdSearch obj) {
    final FudgeSerializer serializer = new FudgeSerializer(s_fudgeContext);
    FudgeMsg msg = serializer.objectToFudgeMsg(obj);
    s_logger.debug("ExternalIdSearch {}", obj);
    s_logger.debug("Encoded to {}", msg);
    final byte[] bytes = s_fudgeContext.toByteArray(msg);
    msg = s_fudgeContext.deserialize(bytes).getMessage();
    s_logger.debug("Serialised to {}", msg);
    final ExternalIdSearch decoded = s_fudgeContext.fromFudgeMsg(ExternalIdSearch.class, msg);
    s_logger.debug("Decoded to {}", decoded);
    if (!obj.equals(decoded)) {
      s_logger.warn("Expected {}", obj);
      s_logger.warn("Received {}", decoded);
      fail();
    }
  }

}
