/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.firehose;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalScheme;

/**
 * Test.
 */
@Test(groups = "unit")
public class FireHoseLiveDataServerMBeanTest {

  protected static FireHoseLiveDataServer createTestServer() {
    final FireHoseLiveData fireHose = new AbstractFireHoseLiveData() {

      @Override
      public void start() {
        MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
        msg.add("A", null, 42d);
        msg.add("B1", null, "Foo");
        storeValue("Foo", msg);
        msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
        msg.add("A", null, 42d);
        msg.add("B2", null, "Foo");
        storeValue("Bar", msg);
      }

      @Override
      public void stop() {
      }

      @Override
      public boolean isStarted() {
        return false;
      }
    };
    final FireHoseLiveDataServer server = new FireHoseLiveDataServer(ExternalScheme.of("Test"), fireHose);
    server.start();
    return server;
  }

  public void testCSVEscape() {
    final FireHoseLiveDataServerMBean instance = new FireHoseLiveDataServerMBean(createTestServer());
    assertEquals(instance.csvEscape("Foo"), "Foo");
    assertEquals(instance.csvEscape("Foo,"), "\"Foo,\"");
    assertEquals(instance.csvEscape("F\"\""), "\"F\"\"\"\"\"");
    assertEquals(instance.csvEscape("XYZ\n"), "\"XYZ\\012\"");
  }

  public void testExportStateOfTheWorld() throws IOException {
    final FireHoseLiveDataServerMBean instance = new FireHoseLiveDataServerMBean(createTestServer());
    final File tmp = File.createTempFile("stateOfTheWorld", "csv");
    try {
      instance.exportStateOfTheWorld(tmp.getPath());
      // TODO: should really check the content of the file
      assertTrue(tmp.exists());
    } finally {
      tmp.delete();
    }
  }

}
