/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import static org.testng.AssertJUnit.fail;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.IntDoublePair;
import com.opengamma.util.tuple.LongDoublePair;
import com.opengamma.util.tuple.Pair;

/**
 * Test Pair Fudge.
 */
@Test
public class PairFudgeEncodingTest {

  private static final Logger s_logger = LoggerFactory.getLogger(PairFudgeEncodingTest.class);
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  public void test_OO_Bundle() {
    Pair<String, ExternalIdBundle> pair = Pair.of("Hello", ExternalIdBundle.of(ExternalId.of("A", "B")));
    testFudgeMessage(pair);
  }

//  public void test_OO_UniqueId() {
//    Pair<String, UniqueId> pair = Pair.of("Hello", UniqueId.of("A", "B"));
//    testFudgeMessage(pair);
//  }

  public void test_OO_null() {
    Pair<String, UniqueId> pair = Pair.of("Hello", null);
    testFudgeMessage(pair);
  }

//  public void test_LO() {
//    Pair<Long, UniqueId> pair = LongObjectPair.of(23L, UniqueId.of("A", "B"));
//    testFudgeMessage(pair);
//  }

  public void test_LD() {
    Pair<Long, Double> pair = LongDoublePair.of(23L, 4.5d);
    testFudgeMessage(pair);
  }

//  public void test_IO() {
//    Pair<Integer, UniqueId> pair = IntObjectPair.of(23, UniqueId.of("A", "B"));
//    testFudgeMessage(pair);
//  }

  public void test_ID() {
    Pair<Integer, Double> pair = IntDoublePair.of(23, 4.5d);
    testFudgeMessage(pair);
  }

  public void test_DD() {
    Pair<Double, Double> pair = DoublesPair.of(23.2, 4.5d);
    testFudgeMessage(pair);
  }

  public void test_TypeWithSecondaryTypeAndBuilderEncoding() {
    Pair<Tenor, Tenor> pair = Pair.of(Tenor.DAY, Tenor.WORKING_DAYS_IN_MONTH);
    testFudgeMessage(pair);
  }

  private void testFudgeMessage(final Pair<?, ?> pair) {
    final FudgeSerializationContext context = new FudgeSerializationContext(s_fudgeContext);
    FudgeMsg msg = context.objectToFudgeMsg(pair);
    s_logger.debug("Paging {}", pair);
    s_logger.debug("Encoded to {}", msg);
    final byte[] bytes = s_fudgeContext.toByteArray(msg);
    msg = s_fudgeContext.deserialize(bytes).getMessage();
    s_logger.debug("Serialised to {}", msg);
    final Pair<?, ?> decoded = s_fudgeContext.fromFudgeMsg(Pair.class, msg);
    s_logger.debug("Decoded to {}", decoded);
    if (!pair.equals(decoded)) {
      s_logger.warn("Expected {}", pair);
      s_logger.warn("Received {}", decoded);
      fail();
    }
  }

}
