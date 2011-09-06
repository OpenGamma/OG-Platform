/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.paging;

import org.testng.annotations.Test;

import com.opengamma.util.Paging;
import com.opengamma.util.PagingRequest;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

/**
 * Test Fudge encoding.
 */
@Test
public class PagingFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void test() {
    Paging object = Paging.of(PagingRequest.ofIndex(0, 20), 210);
    assertEncodeDecodeCycle(Paging.class, object);
  }

}
