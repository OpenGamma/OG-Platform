/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.JodaBeanSerialization;
import com.opengamma.util.test.TestGroup;

/**
 * Test the Joda Bean XML encoding of securities.
 */
@Test(groups = TestGroup.UNIT)
public class SecuritiesJodaBeanPrettyXmlTest extends SecurityTestCase {

  @Override
  protected <T extends ManageableSecurity> void assertSecurity(Class<T> securityClass, T security) {
    String xml = JodaBeanSerialization.serializer(true).xmlWriter().write(security);
//    System.out.println(xml);
    
    T readIn = securityClass.cast(JodaBeanSerialization.deserializer().xmlReader().read(xml));
    assertEquals(security, readIn);
    
    // fudge equivalent
//    StringWriter writer = new StringWriter();
//    FudgeXMLStreamWriter xmlStreamWriter = new FudgeXMLStreamWriter(OpenGammaFudgeContext.getInstance(), writer);
//    FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
//    MutableFudgeMsg msg = serializer.objectToFudgeMsg(security);
//    FudgeMsgWriter fudgeMsgWriter = new FudgeMsgWriter(xmlStreamWriter);
//    fudgeMsgWriter.writeMessage(msg);
//    fudgeMsgWriter.close();
//    String writerXml = writer.toString();
//    System.out.println(writerXml);
//    System.out.println(xml.length() + " vs " + writerXml.length());
//    System.out.println("");
  }

}
