/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

/**
 * Test Fudge encoding.
 */
@Test
public class PairFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void test_OO_Bundle() {
    Pair<String, ExternalIdBundle> object = Pair.of("Hello", ExternalIdBundle.of(ExternalId.of("A", "B")));
    assertEncodeDecodeCycle(Pair.class, object);
  }

//  public void test_OO_UniqueId() {
//    Pair<String, UniqueId> object = Pair.of("Hello", UniqueId.of("A", "B"));
//    assertEncodeDecodeCycle(Pair.class, object);
//  }

  public void test_OO_null() {
    Pair<String, UniqueId> object = Pair.of("Hello", null);
    assertEncodeDecodeCycle(Pair.class, object);
  }

//  public void test_LO() {
//    Pair<Long, UniqueId> object = LongObjectPair.of(23L, UniqueId.of("A", "B"));
//    assertEncodeDecodeCycle(Pair.class, object);
//  }

  public void test_LD() {
    Pair<Long, Double> object = LongDoublePair.of(23L, 4.5d);
    assertEncodeDecodeCycle(Pair.class, object);
  }

//  public void test_IO() {
//    Pair<Integer, UniqueId> object = IntObjectPair.of(23, UniqueId.of("A", "B"));
//    assertEncodeDecodeCycle(Pair.class, object);
//  }

  public void test_ID() {
    Pair<Integer, Double> object = IntDoublePair.of(23, 4.5d);
    assertEncodeDecodeCycle(Pair.class, object);
  }

  public void test_DD() {
    Pair<Double, Double> object = DoublesPair.of(23.2, 4.5d);
    assertEncodeDecodeCycle(Pair.class, object);
  }

  // secondary type doesn't send class headers
//  public void test_TypeWithSecondaryTypeAndBuilderEncoding() {
//    Pair<Tenor, Tenor> object = Pair.of(Tenor.DAY, Tenor.WORKING_DAYS_IN_MONTH);
//    assertEncodeDecodeCycle(Pair.class, object);
//  }

}
