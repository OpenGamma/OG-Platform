/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class RawSecurityFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void test_basic() {
    RawSecurity object = new RawSecurity("Dummy", new byte[0]);
    assertEncodeDecodeCycle(RawSecurity.class, object);
  }

  public void test_full() {
    UniqueId uid = UniqueId.of("A", "123");
    ExternalIdBundle bundle = ExternalIdBundle.of("X", "Y");
    RawSecurity object = new RawSecurity(uid, "OpenGamma", "Dummy", bundle, new byte[] {1, 2, 4});
    assertEncodeDecodeCycle(RawSecurity.class, object);
  }

}
