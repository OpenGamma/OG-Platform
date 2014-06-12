/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.tuple;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Triple;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class TripleFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void test_objectAndReducedNumber() {
    Triple<String, ExternalIdBundle, Long> object = Triple.of("Hello", ExternalIdBundle.of(ExternalId.of("A", "B")), 6L);
    assertEncodeDecodeCycle(Triple.class, object);
  }

  public void test_objectAndSecondaryType() {
    Triple<String, UniqueId, LocalDate> object = Triple.of(null, UniqueId.of("A", "B"), LocalDate.of(2011, 6, 30));
    assertEncodeDecodeCycle(Triple.class, object);
  }

  public void test_TypeWithSecondaryTypeAndBuilderEncoding() {
    Triple<Tenor, Tenor, Expiry> object = Triple.of(Tenor.DAY, Tenor.TEN_MONTHS, new Expiry(ZonedDateTime.now(), ExpiryAccuracy.DAY_MONTH_YEAR));
    assertEncodeDecodeCycle(Triple.class, object);
  }

  public void test_nullFirst() {
    Triple<String, String, String> object = Triple.of(null, "B", "C");
    assertEncodeDecodeCycle(Triple.class, object);
  }

  public void test_nullSecond() {
    Triple<String, String, String> object = Triple.of("A", null, "C");
    assertEncodeDecodeCycle(Triple.class, object);
  }

  public void test_nullThird() {
    Triple<String, String, String> object = Triple.of("A", "B", null);
    assertEncodeDecodeCycle(Triple.class, object);
  }

}
