/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.util.test.AbstractBuilderTestCase;

/**
 * Test Fudge encoding.
 */
@Test
public class ExternalIdWithDatesFudgeEncodingTest extends AbstractBuilderTestCase {

  private static final LocalDate VALID_FROM = LocalDate.of(2010, MonthOfYear.JANUARY, 1);
  private static final LocalDate VALID_TO = LocalDate.of(2010, MonthOfYear.DECEMBER, 1);

  public void test_noDates() {
    ExternalIdWithDates object = ExternalIdWithDates.of(ExternalId.of("A", "B"), null, null);
    assertEncodeDecodeCycle(ExternalIdWithDates.class, object);
  }

  public void test_withDates() {
    ExternalIdWithDates object = ExternalIdWithDates.of(ExternalId.of("A", "B"), VALID_FROM, VALID_TO);
    assertEncodeDecodeCycle(ExternalIdWithDates.class, object);
  }

  //-------------------------------------------------------------------------
  public void test_fudgeEncoding_with_valid_dates() {
    ExternalId identifier = ExternalId.of("id1", "value1");
    ExternalIdWithDates object = ExternalIdWithDates.of(identifier, VALID_FROM, VALID_TO);
    FudgeContext context = new FudgeContext();
    FudgeSerializer serializer = new FudgeSerializer(context);
    MutableFudgeMsg msg = serializer.newMessage();
    object.toFudgeMsg(serializer, msg);
    assertNotNull(msg);
    assertEquals(4, msg.getNumFields());
    ExternalIdWithDates decoded = ExternalIdWithDates.fromFudgeMsg(new FudgeDeserializer(context), msg);
    assertEquals(object, decoded);
  }

  public void test_fudgeEncoding_with_validFrom() {
    ExternalId identifier = ExternalId.of("id1", "value1");
    ExternalIdWithDates object = ExternalIdWithDates.of(identifier, VALID_FROM, null);
    FudgeContext context = new FudgeContext();
    FudgeSerializer serializer = new FudgeSerializer(context);
    MutableFudgeMsg msg = serializer.newMessage();
    object.toFudgeMsg(serializer, msg);
    assertNotNull(msg);
    assertEquals(3, msg.getNumFields());
    ExternalIdWithDates decoded = ExternalIdWithDates.fromFudgeMsg(new FudgeDeserializer(context), msg);
    assertEquals(object, decoded);
  }

  public void test_fudgeEncoding_with_validTo() {
    ExternalId identifier = ExternalId.of("id1", "value1");
    ExternalIdWithDates object = ExternalIdWithDates.of(identifier, null, VALID_TO);
    FudgeContext context = new FudgeContext();
    FudgeSerializer serializer = new FudgeSerializer(context);
    MutableFudgeMsg msg = serializer.newMessage();
    object.toFudgeMsg(serializer, msg);
    assertNotNull(msg);
    assertEquals(3, msg.getNumFields());
    ExternalIdWithDates decoded = ExternalIdWithDates.fromFudgeMsg(new FudgeDeserializer(context), msg);
    assertEquals(object, decoded);
  }

}
