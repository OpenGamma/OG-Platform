/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import static org.testng.AssertJUnit.assertEquals;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Test the Fudge encoding of securities.
 */
@Test
public class FudgeProtoCrossCheckSecurityEncodingTest extends SecurityTestCase {

  /**
   * The Fudge context.
   */
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  @Override
  protected <T extends ManageableSecurity> void assertSecurity(Class<T> securityClass, T security) {
    FudgeSerializer serializer = new FudgeSerializer(s_fudgeContext);
    final FudgeMsg message1 = serializer.objectToFudgeMsg(security);
    final FudgeMsg message2 = security.toFudgeMsg(serializer);
    assertEquals(message2, message1);
  }

}
