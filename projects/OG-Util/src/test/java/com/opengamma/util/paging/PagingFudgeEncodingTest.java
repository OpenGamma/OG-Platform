/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.paging;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class PagingFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void test() {
    Paging object = Paging.of(PagingRequest.ofIndex(0, 20), 210);
    assertEncodeDecodeCycle(Paging.class, object);
  }

  public void test_toFudgeMsg() {
    Paging sample = Paging.of(PagingRequest.ONE, 1);
    assertNull(PagingFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), null));
    assertNotNull(PagingFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), sample));
  }

  public void test_fromFudgeMsg() {
    assertNull(PagingFudgeBuilder.fromFudgeMsg(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), null));
  }

}
