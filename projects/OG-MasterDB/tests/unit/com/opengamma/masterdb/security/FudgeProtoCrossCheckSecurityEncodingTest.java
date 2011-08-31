/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import static org.testng.AssertJUnit.assertEquals;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
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
    final FudgeMsg builderMsg = serializer.objectToFudgeMsg(security);
    final FudgeMsg protoMsg = security.toFudgeMsg(serializer);
    assertEquals(protoMsg, builderMsg);
    
    FudgeDeserializer deserializer = new FudgeDeserializer(s_fudgeContext);
    final ManageableSecurity builderObj = deserializer.fudgeMsgToObject(securityClass, builderMsg);
    assertEquals(security, builderObj);
  }

}
