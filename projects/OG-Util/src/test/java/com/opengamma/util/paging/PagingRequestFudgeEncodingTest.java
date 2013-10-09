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
public class PagingRequestFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void test() {
    PagingRequest object = PagingRequest.ofIndex(0, 20);
    assertEncodeDecodeCycle(PagingRequest.class, object);
  }

  public void test_toFudgeMsg() {
    PagingRequest sample = PagingRequest.ONE;
    assertNull(PagingRequestFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), null));
    assertNotNull(PagingRequestFudgeBuilder.toFudgeMsg(new FudgeSerializer(OpenGammaFudgeContext.getInstance()), sample));
  }

  public void test_fromFudgeMsg() {
    assertNull(PagingRequestFudgeBuilder.fromFudgeMsg(new FudgeDeserializer(OpenGammaFudgeContext.getInstance()), null));
  }

}
