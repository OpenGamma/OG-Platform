/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import static org.testng.AssertJUnit.assertEquals;

import org.joda.beans.ser.bin.MsgPackVisualizer;
import org.testng.annotations.Test;

import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.JodaBeanSerialization;
import com.opengamma.util.test.TestGroup;

/**
 * Test the Joda Bean binary encoding of securities.
 */
@Test(groups = TestGroup.UNIT)
public class SecuritiesJodaBeanBinTest extends SecurityTestCase {

  @Override
  protected <T extends ManageableSecurity> void assertSecurity(Class<T> securityClass, T security) {
    byte[] bytes = JodaBeanSerialization.serializer(false).binWriter().write(security, false);
//    System.out.println(bytes);
    
    try {
      T readIn = securityClass.cast(JodaBeanSerialization.deserializer().binReader().read(bytes, securityClass));
      assertEquals(security, readIn);
    } catch (Throwable ex) {
      new MsgPackVisualizer(bytes).visualize();
      throw ex;
    }
    
    // fudge equivalent
//    FudgeContext fc = OpenGammaFudgeContext.getInstance();
//    FudgeSerializer serializer = new FudgeSerializer(fc);
//    FudgeMsg msg = serializer.objectToFudgeMsg(security);
//    ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
//    @SuppressWarnings("resource")
//    final FudgeMsgWriter fmwr = new FudgeMsgWriter(new FudgeDataOutputStreamWriter(fc, baos));
//    fmwr.writeMessage(msg);
//    fmwr.flush();
//    try {
//      baos.close();
//    } catch (IOException ex) {
//      ex.printStackTrace();
//    }
//    byte[] fudgeBytes = baos.toByteArray();
//    double a = bytes.length;
//    double b = fudgeBytes.length;
//    int percent = (int) ((a / b) * 100);
//    System.out.println(percent + "% Joda " +  bytes.length + " vs Fudge " + fudgeBytes.length);
//    if (bytes.length > fudgeBytes.length) {
//      System.out.println(securityClass.getSimpleName());
//      dump(bytes);
//      dump(fudgeBytes);
//    }
  }

  void dump(byte[] bytes) {
    for (byte b : bytes) {
      System.out.print(String.format("%02X ", b));
    }
    System.out.println();
    for (byte b : bytes) {
      int unsigned = ((int) b) & 0xFF;
      if (unsigned >= 32 && unsigned <= 127) {
        System.out.print(" " + ((char) unsigned) + " ");
      } else {
        System.out.print(String.format("%02X ", b));
      }
    }
    System.out.println();
  }

}
