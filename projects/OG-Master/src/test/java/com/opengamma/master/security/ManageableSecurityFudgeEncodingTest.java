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
public class ManageableSecurityFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

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

}
