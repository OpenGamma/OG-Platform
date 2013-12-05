/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.threeten.bp.Duration;

import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.test.TestGroup;

/**
 * Test the Joda Bean XML encoding performance.
 */
@Test(groups = TestGroup.UNIT, enabled = false)
public class SecuritiesJodaBeanXmlPerformanceTest extends SecurityTestCase {

  Map<String, Duration> times =  new HashMap<>();
  long total = 0;
  long number = 0;
  int count = 0;

  @AfterClass
  public void tearDown() {
    if (total > 0 && number > 0) {
      System.out.println("Average: " + (((double) total) / number));
    }
  }

  @Override
  protected <T extends ManageableSecurity> void assertSecurity(Class<T> securityClass, T security) {
//    count = 0;
//    
//    // Joda write
//    String jodaXml = null;
////    byte[] packedJoda2 = null;
//    for (int i = 0; i < 1000; i++) {
//      jodaXml = JodaBeanSer.PRETTY.xmlWriter().write(security);
////      packedJoda2 = ZipUtils.deflateString(jodaXml);
//    }
//    long start = System.nanoTime();
//    for (int i = 0; i < 1000; i++) {
//      jodaXml = JodaBeanSer.COMPACT.xmlWriter().write(security);
////      packedJoda2 = ZipUtils.deflateString(jodaXml);
//      if (jodaXml.length() > 0) {
//        count++;
//      }
//    }
//    long end = System.nanoTime();
//    long diffJodaWrite = (end - start) / 1_000_000;
//    
//    // Joda read
//    Object obj = null;
//    start = System.nanoTime();
//    for (int i = 0; i < 1000; i++) {
////      String unpacked = ZipUtils.inflateString(packedJoda2);
//      String unpacked = jodaXml;
//      obj = JodaBeanSer.COMPACT.xmlReader().read(unpacked);
//      if (obj instanceof ManageableSecurity) {
//        count++;
//      }
//    }
//    end = System.nanoTime();
//    long diffJodaRead = (end - start) / 1_000_000;
//    total += diffJodaRead;
//    number++;
//    
//    // Fudge write
//    byte[] fudgeBytes = null;
//    start = System.nanoTime();
//    for (int i = 0; i < 1000; i++) {
//      FudgeContext fc = OpenGammaFudgeContext.getInstance();
//      FudgeSerializer serializer = new FudgeSerializer(fc);
//      FudgeMsg msg = serializer.objectToFudgeMsg(security);
//      ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
//      @SuppressWarnings("resource")
//      final FudgeMsgWriter fmwr = new FudgeMsgWriter(new FudgeDataOutputStreamWriter(fc, baos));
//      fmwr.writeMessage(msg);
//      fmwr.flush();
//      try {
//        baos.close();
//      } catch (IOException ex) {
//        ex.printStackTrace();
//      }
//      fudgeBytes = baos.toByteArray();
//      if (fudgeBytes.length > 0) {
//        count++;
//      }
//    }
//    end = System.nanoTime();
//    long diffFudgeWrite = (end - start) / 1_000_000;
//    
//    // Fudge read
//    obj = null;
//    start = System.nanoTime();
//    for (int i = 0; i < 1000; i++) {
//      FudgeContext fc = OpenGammaFudgeContext.getInstance();
//      FudgeMsg msg = null;
//      try (ByteArrayInputStream bais = new ByteArrayInputStream(fudgeBytes)) {
//        final FudgeMsgReader fmr = new FudgeMsgReader(new FudgeDataInputStreamReader(fc, bais));
//        msg = fmr.nextMessage();
//        fmr.close();
//      } catch (IOException ex) {
//        throw new RuntimeException(ex);
//      }
//      FudgeDeserializer deserializer = new FudgeDeserializer(fc);
//      obj = deserializer.fudgeMsgToObject(msg);
//      if (obj instanceof ManageableSecurity) {
//        count++;
//      }
//    }
//    end = System.nanoTime();
//    long diffFudgeRead = (end - start) / 1_000_000;
//    
////    // Fudge XML write
////    FudgeContext fc = OpenGammaFudgeContext.getInstance();
////    FudgeSerializer serializer = new FudgeSerializer(fc);
////    FudgeMsg msg = serializer.objectToFudgeMsg(security);
////    StringWriter strWr = new StringWriter();
////    FudgeXMLStreamWriter xmlStreamWriter = new FudgeXMLStreamWriter(OpenGammaFudgeContext.getInstance(), strWr);
////    FudgeMsgWriter fudgeMsgWriter = new FudgeMsgWriter(xmlStreamWriter);
////    fudgeMsgWriter.writeMessage(msg);
////    fudgeMsgWriter.close();
////    String fudgeXml = strWr.toString();
//    
//    // output
//    System.out.println(securityClass.getSimpleName() + " JodaWrite:" + diffJodaWrite + " JodaRead:" + diffJodaRead +
//        " FudgeWrite:" + diffFudgeWrite + " FudgeRead:" + diffFudgeRead);
////    System.out.println(securityClass.getSimpleName() + " JodaXML:" + jodaXml.length() + " JodaXMLZlib:" +
////        packedJoda2.length + " Fudge:" + fudgeBytes.length + " FudgeXML:" + fudgeXml.length() + (count > 0 ? "" : " no count"));
  }

}
