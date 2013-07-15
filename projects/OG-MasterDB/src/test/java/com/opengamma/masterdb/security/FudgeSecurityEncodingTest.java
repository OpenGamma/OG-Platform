/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import static org.testng.AssertJUnit.fail;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.core.security.Security;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test the Fudge encoding of securities.
 */
@Test(groups = TestGroup.UNIT)
public class FudgeSecurityEncodingTest extends SecurityTestCase {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(FudgeSecurityEncodingTest.class);

  /**
   * The Fudge context.
   */
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  @Override
  protected <T extends ManageableSecurity> void assertSecurity(Class<T> securityClass, T security) {
    final FudgeSerializer serializer = new FudgeSerializer(s_fudgeContext);
    FudgeMsg msg = serializer.objectToFudgeMsg(security);
    s_logger.debug("Security {}", security);
    s_logger.debug("Encoded to {}", msg);
    final byte[] bytes = s_fudgeContext.toByteArray(msg);
    msg = s_fudgeContext.deserialize(bytes).getMessage();
    s_logger.debug("Serialised to to {}", msg);
    final Security decoded = s_fudgeContext.fromFudgeMsg(securityClass, msg);
    s_logger.debug("Decoded to {}", decoded);
    if (!security.equals(decoded)) {
      s_logger.warn("Expected {}", security);
      s_logger.warn("Received {}", decoded);
      fail();
    }
  }

}
