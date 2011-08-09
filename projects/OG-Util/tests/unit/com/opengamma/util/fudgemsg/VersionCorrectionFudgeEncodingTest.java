/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import static org.testng.AssertJUnit.fail;

import javax.time.Instant;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.id.VersionCorrection;

/**
 * Test Fudge encoding.
 */
@Test
public class VersionCorrectionFudgeEncodingTest {

  private static final Logger s_logger = LoggerFactory.getLogger(VersionCorrectionFudgeEncodingTest.class);
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();
  private static final Instant INSTANT1 = Instant.ofEpochSeconds(1);
  private static final Instant INSTANT2 = Instant.ofEpochSeconds(2);

  public void test() {
    VersionCorrection object = VersionCorrection.of(INSTANT1, INSTANT2);
    testFudgeMessage(object);
  }

  public void testLatest() {
    VersionCorrection object = VersionCorrection.LATEST;
    testFudgeMessage(object);
  }

  private void testFudgeMessage(final VersionCorrection object) {
    final FudgeSerializer serializer = new FudgeSerializer(s_fudgeContext);
    FudgeMsg msg = serializer.objectToFudgeMsg(object);
    s_logger.debug("Paging {}", object);
    s_logger.debug("Encoded to {}", msg);
    final byte[] bytes = s_fudgeContext.toByteArray(msg);
    msg = s_fudgeContext.deserialize(bytes).getMessage();
    s_logger.debug("Serialised to {}", msg);
    final VersionCorrection decoded = s_fudgeContext.fromFudgeMsg(VersionCorrection.class, msg);
    s_logger.debug("Decoded to {}", decoded);
    if (!object.equals(decoded)) {
      s_logger.warn("Expected {}", object);
      s_logger.warn("Received {}", decoded);
      fail();
    }
  }

}
