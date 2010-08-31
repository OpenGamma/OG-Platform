/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import static org.junit.Assert.fail;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.engine.security.Security;
import com.opengamma.util.fudge.OpenGammaFudgeContext;

public class FudgeSecurityEncodingTest extends SecurityTestCase {

  private static final Logger s_logger = LoggerFactory.getLogger(FudgeSecurityEncodingTest.class);

  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  @Override
  protected <T extends DefaultSecurity> void testSecurity(Class<T> securityClass, T security) {
    final FudgeSerializationContext context = new FudgeSerializationContext(s_fudgeContext);
    FudgeFieldContainer msg = context.objectToFudgeMsg(security);
    s_logger.debug("Security {}", security);
    s_logger.debug("Encoded to {}", security);
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
