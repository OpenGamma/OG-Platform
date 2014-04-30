/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.testng.annotations.Test;

import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link FudgeXMLFormat} class.
 */
@Test(groups = TestGroup.UNIT)
public class FudgeXMLFormatTest {

  public void testBasicOperations() {
    final FudgeXMLFormat format = new FudgeXMLFormat();
    final Object context = format.openRead(null);
    assertEquals(format.getLogicalFileExtension(context), ".xml");
    format.closeRead(context);
  }

  public void testReadWrite() throws IOException {
    final FudgeXMLFormat format = new FudgeXMLFormat();
    final Object context = format.openWrite(format.openRead(null));
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final ManageableSecurity objectOut = new ManageableSecurity("Foo");
    format.write(context, objectOut, baos);
    final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    final Object object = format.read(context, bais);
    assertNotNull(object);
    assertEquals(object, objectOut);
    format.closeRead(format.closeWrite(context));
  }

}
