/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.AbstractBuilderTestCase;

/**
 * Test Fudge encoding.
 */
@Test
public class ExternalIdBundleFudgeEncodingTest extends AbstractBuilderTestCase {

  public void test() {
    ExternalIdBundle object = ExternalIdBundle.of(
        ExternalId.of("id1", "value1"),
        ExternalId.of("id2", "value2"));
    assertEncodeDecodeCycle(ExternalIdBundle.class, object);
  }

}
