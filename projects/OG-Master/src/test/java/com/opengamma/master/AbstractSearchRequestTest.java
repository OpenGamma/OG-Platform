/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link AbstractSearchRequest}.
 */
@Test(groups = TestGroup.UNIT)
public class AbstractSearchRequestTest {

  @DataProvider(name = "timeBounds")
  Object[][] data_timeBounds() {
    Instant now = Instant.now();
    return new Object[][] {
        {null, null, null, true},
        {null, now.minusSeconds(20), null, true},
        {null, now.plusSeconds(20), null, false},
        {null, null, now.minusSeconds(20), false},
        {null, null, now.plusSeconds(20), true},
        {now, null, null, true},
        {now, now.minusSeconds(20), null, true},
        {now, now.plusSeconds(20), null, false},
        {now, null, now.minusSeconds(20), false},
        {now, null, now.plusSeconds(20), true},
        {now, now.minusSeconds(20), now.plusSeconds(20), true},
        {now, now.minusSeconds(40), now.minusSeconds(20), false},
        {now, now.minusSeconds(20), now.minusSeconds(40), false},
        {now, now.plusSeconds(20), now.plusSeconds(40), false},
        {now, now.plusSeconds(40), now.plusSeconds(20), false},
    };
  }

  @Test(dataProvider = "timeBounds")
  public void test_matches_versions(Instant instant, Instant start, Instant end, boolean expected) {
    Mock mock = new Mock();
    MockDoc mockDoc = new MockDoc();
    mockDoc.setVersionFromInstant(start);
    mockDoc.setVersionToInstant(end);
    mockDoc.setCorrectionFromInstant(Instant.MIN);
    mockDoc.setCorrectionToInstant(Instant.MAX);
    assertEquals(expected, mock.matches(mockDoc));
  }

  @Test(dataProvider = "timeBounds")
  public void test_matches_corrections(Instant instant, Instant start, Instant end, boolean expected) {
    Mock mock = new Mock();
    MockDoc mockDoc = new MockDoc();
    mockDoc.setVersionFromInstant(Instant.MIN);
    mockDoc.setVersionToInstant(Instant.MAX);
    mockDoc.setCorrectionFromInstant(start);
    mockDoc.setCorrectionToInstant(end);
    assertEquals(expected, mock.matches(mockDoc));
  }

  //-------------------------------------------------------------------------
  static class Mock extends AbstractSearchRequest {
  }

  //-------------------------------------------------------------------------
  static class MockDoc extends AbstractDocument {
    @Override
    public UniqueIdentifiable getValue() {
      return null;
    }
    @Override
    public UniqueId getUniqueId() {
      return null;
    }
    @Override
    public void setUniqueId(UniqueId uniqueId) {
    }
  }

}
