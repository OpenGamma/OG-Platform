/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;
import com.opengamma.util.time.Tenor;

/**
 * Test Fudge encoding.
 */
@Test
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
    Triple<Tenor, Tenor, Expiry> object = Triple.of(Tenor.DAY, Tenor.WORKING_DAYS_IN_MONTH, new Expiry(ZonedDateTime.now(), ExpiryAccuracy.DAY_MONTH_YEAR));
    assertEncodeDecodeCycle(Triple.class, object);
  }

}
