/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.fudgemsg.FudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.test.AbstractBuilderTestCase;

/**
 * Test Fudge encoding.
 */
@Test
public class ManageableSecurityFudgeEncodingTest extends AbstractBuilderTestCase {

  public void test_basic() {
    ManageableSecurity object = new ManageableSecurity("Dummy");
    assertEncodeDecodeCycle(ManageableSecurity.class, object);
  }

  public void test_full() {
    UniqueId uid = UniqueId.of("A", "123");
    ExternalIdBundle bundle = ExternalIdBundle.of("X", "Y");
    ManageableSecurity object = new ManageableSecurity(uid, "OpenGamma", "Dummy", bundle);
    assertEncodeDecodeCycle(ManageableSecurity.class, object);
  }

  protected <T extends ManageableSecurity> void assertEncodeDecodeCycle(final Class<ManageableSecurity> clazz, final T object) {
    super.assertEncodeDecodeCycle(clazz, object);
    final FudgeMsg message1 = getFudgeSerializer().objectToFudgeMsg(object);
    final FudgeMsg message2 = object.toFudgeMsg(getFudgeSerializer());
    assertEquals(message2, message1);
  }

}
