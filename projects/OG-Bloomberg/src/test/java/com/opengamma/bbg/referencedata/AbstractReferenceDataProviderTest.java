/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.joda.beans.test.BeanAssert;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.bbg.referencedata.impl.AbstractReferenceDataProvider;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class AbstractReferenceDataProviderTest {

  private static final FudgeContext FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();
  private static final String ID1 = "ID1";
  private static final String ID2 = "ID2";
  private static final String FIELD1 = "FIELD1";
  private static final String FIELD2 = "FIELD2";
  private static final String VALUE1 = "VALUE1";
  private static final String VALUE2 = "VALUE2";

  @Test
  public void singleIdSingleField_dataReturned() {
    ReferenceDataProviderGetRequest request = ReferenceDataProviderGetRequest.createGet(ID1, FIELD1, true);
    MutableFudgeMsg values = FUDGE_CONTEXT.newMessage();
    values.add(FIELD1, VALUE1);
    ReferenceDataProviderGetResult result = new ReferenceDataProviderGetResult();
    result.addReferenceData(new ReferenceData(ID1, values));
    
    ReferenceDataProvider mock = new Mock(request, result);
//    ReferenceDataProvider mock = mock(AbstractReferenceDataProvider.class);
//    when(mock.getReferenceData(request)).thenReturn(result);
    
    String test = mock.getReferenceDataValue(ID1, FIELD1);
    assertEquals(VALUE1, test);
  }

  @Test
  public void singleIdSingleField_noValue() {
    ReferenceDataProviderGetRequest request = ReferenceDataProviderGetRequest.createGet(ID1, FIELD1, true);
    MutableFudgeMsg values = FUDGE_CONTEXT.newMessage();
    values.add(FIELD1, null);
    ReferenceDataProviderGetResult result = new ReferenceDataProviderGetResult();
    result.addReferenceData(new ReferenceData(ID1, values));
    
    ReferenceDataProvider mock = new Mock(request, result);
    String test = mock.getReferenceDataValue(ID1, FIELD1);
    assertEquals(null, test);
  }

  @Test
  public void singleIdSingleField_noField() {
    ReferenceDataProviderGetRequest request = ReferenceDataProviderGetRequest.createGet(ID1, FIELD1, true);
    MutableFudgeMsg values = FUDGE_CONTEXT.newMessage();
    ReferenceDataProviderGetResult result = new ReferenceDataProviderGetResult();
    result.addReferenceData(new ReferenceData(ID1, values));
    
    ReferenceDataProvider mock = new Mock(request, result);
    String test = mock.getReferenceDataValue(ID1, FIELD1);
    assertEquals(null, test);
  }

  @Test
  public void singleIdSingleField_noId() {
    ReferenceDataProviderGetRequest request = ReferenceDataProviderGetRequest.createGet(ID1, FIELD1, true);
    ReferenceDataProviderGetResult result = new ReferenceDataProviderGetResult();
    
    ReferenceDataProvider mock = new Mock(request, result);
    String test = mock.getReferenceDataValue(ID1, FIELD1);
    assertEquals(null, test);
  }

  @Test
  public void singleIdSingleField_fieldError() {
    ReferenceDataProviderGetRequest request = ReferenceDataProviderGetRequest.createGet(ID1, FIELD1, true);
    MutableFudgeMsg values = FUDGE_CONTEXT.newMessage();
    values.add(FIELD1, VALUE1);
    ReferenceDataProviderGetResult result = new ReferenceDataProviderGetResult();
    ReferenceData rd = new ReferenceData(ID1, values);
    rd.addError(new ReferenceDataError(FIELD1, -1, "A", "B", "C"));  // error overrides value
    result.addReferenceData(rd);
    
    ReferenceDataProvider mock = new Mock(request, result);
    String test = mock.getReferenceDataValue(ID1, FIELD1);
    assertEquals(null, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void singleIdMultipleFields_dataReturned() {;
    ReferenceDataProviderGetRequest request = ReferenceDataProviderGetRequest.createGet(ID1, ImmutableSet.of(FIELD1, FIELD2), true);
    MutableFudgeMsg values = FUDGE_CONTEXT.newMessage();
    values.add(FIELD1, VALUE1);
    values.add(FIELD2, VALUE2);
    ReferenceDataProviderGetResult result = new ReferenceDataProviderGetResult();
    result.addReferenceData(new ReferenceData(ID1, values));
    
    ReferenceDataProvider mock = new Mock(request, result);
    Map<String, String> test = mock.getReferenceDataValues(ID1, ImmutableSet.of(FIELD1, FIELD2));
    assertEquals(2, test.size());
    assertEquals(VALUE1, test.get(FIELD1));
    assertEquals(VALUE2, test.get(FIELD2));
  }

  @Test
  public void singleIdMultipleFields_idError() {;
    ReferenceDataProviderGetRequest request = ReferenceDataProviderGetRequest.createGet(ID1, ImmutableSet.of(FIELD1, FIELD2), true);
    MutableFudgeMsg values = FUDGE_CONTEXT.newMessage();
    values.add(FIELD1, VALUE1);
    ReferenceDataProviderGetResult result = new ReferenceDataProviderGetResult();
    ReferenceData rd = new ReferenceData(ID1, values);
    rd.addError(new ReferenceDataError(null, -1, "A", "B", "C"));  // overrides all fields
    result.addReferenceData(rd);
    
    ReferenceDataProvider mock = new Mock(request, result);
    Map<String, String> test = mock.getReferenceDataValues(ID1, ImmutableSet.of(FIELD1, FIELD2));
    assertEquals(0, test.size());
  }

  @Test
  public void singleIdMultipleFields_oneFieldError() {;
    ReferenceDataProviderGetRequest request = ReferenceDataProviderGetRequest.createGet(ID1, ImmutableSet.of(FIELD1, FIELD2), true);
    MutableFudgeMsg values = FUDGE_CONTEXT.newMessage();
    values.add(FIELD2, VALUE2);
    ReferenceDataProviderGetResult result = new ReferenceDataProviderGetResult();
    ReferenceData rd = new ReferenceData(ID1, values);
    rd.addError(new ReferenceDataError(ID1, -1, "A", "B", "C"));
    result.addReferenceData(rd);
    
    ReferenceDataProvider mock = new Mock(request, result);
    Map<String, String> test = mock.getReferenceDataValues(ID1, ImmutableSet.of(FIELD1, FIELD2));
    assertEquals(1, test.size());
    assertEquals(VALUE2, test.get(FIELD2));
  }

  //-------------------------------------------------------------------------
  @Test
  public void multipleIdsSingleField_dataReturned() {;
    ReferenceDataProviderGetRequest request = ReferenceDataProviderGetRequest.createGet(ImmutableSet.of(ID1, ID2), ImmutableSet.of(FIELD1), true);
    MutableFudgeMsg values1 = FUDGE_CONTEXT.newMessage();
    values1.add(FIELD1, VALUE1);
    MutableFudgeMsg values2 = FUDGE_CONTEXT.newMessage();
    values2.add(FIELD1, VALUE2);
    ReferenceDataProviderGetResult result = new ReferenceDataProviderGetResult();
    result.addReferenceData(new ReferenceData(ID1, values1));
    result.addReferenceData(new ReferenceData(ID2, values2));
    
    ReferenceDataProvider mock = new Mock(request, result);
    Map<String, String> test = mock.getReferenceDataValues(ImmutableSet.of(ID1, ID2), FIELD1);
    assertEquals(2, test.size());
    assertEquals(VALUE1, test.get(ID1));
    assertEquals(VALUE2, test.get(ID2));
  }

  @Test
  public void multipleIdsSingleField_idError() {;
  ReferenceDataProviderGetRequest request = ReferenceDataProviderGetRequest.createGet(ImmutableSet.of(ID1, ID2), ImmutableSet.of(FIELD1), true);
    MutableFudgeMsg values = FUDGE_CONTEXT.newMessage();
    values.add(FIELD1, VALUE1);
    ReferenceDataProviderGetResult result = new ReferenceDataProviderGetResult();
    result.addReferenceData(new ReferenceData(ID1, values));
    ReferenceData rd = new ReferenceData(ID2);
    rd.addError(new ReferenceDataError(null, -1, "A", "B", "C"));  // overrides all fields
    result.addReferenceData(rd);
    
    ReferenceDataProvider mock = new Mock(request, result);
    Map<String, String> test = mock.getReferenceDataValues(ImmutableSet.of(ID1, ID2), FIELD1);
    assertEquals(1, test.size());
    assertEquals(VALUE1, test.get(ID1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void multipleIdsMultipleFields_dataReturned() {
    ReferenceDataProviderGetRequest request = ReferenceDataProviderGetRequest.createGet(ImmutableSet.of(ID1, ID2), ImmutableSet.of(FIELD1, FIELD2), true);
    MutableFudgeMsg values1 = FUDGE_CONTEXT.newMessage();
    values1.add(FIELD1, VALUE1);
    MutableFudgeMsg values2 = FUDGE_CONTEXT.newMessage();
    values2.add(FIELD1, VALUE2);
    ReferenceDataProviderGetResult result = new ReferenceDataProviderGetResult();
    result.addReferenceData(new ReferenceData(ID1, values1));
    result.addReferenceData(new ReferenceData(ID2, values2));
    
    ReferenceDataProvider mock = new Mock(request, result);
    Map<String, FudgeMsg> test = mock.getReferenceData(ImmutableSet.of(ID1, ID2), ImmutableSet.of(FIELD1, FIELD2));
    assertEquals(2, test.size());
    assertEquals(values1, test.get(ID1));
    assertEquals(values2, test.get(ID2));
  }

  @Test
  public void multipleIdsMultipleFields_idError() {;
  ReferenceDataProviderGetRequest request = ReferenceDataProviderGetRequest.createGet(ImmutableSet.of(ID1, ID2), ImmutableSet.of(FIELD1, FIELD2), true);
    MutableFudgeMsg values = FUDGE_CONTEXT.newMessage();
    values.add(FIELD1, VALUE1);
    ReferenceDataProviderGetResult result = new ReferenceDataProviderGetResult();
    result.addReferenceData(new ReferenceData(ID1, values));
    ReferenceData rd = new ReferenceData(ID2);
    rd.addError(new ReferenceDataError(null, -1, "A", "B", "C"));  // overrides all fields
    result.addReferenceData(rd);
    
    ReferenceDataProvider mock = new Mock(request, result);
    Map<String, FudgeMsg> test = mock.getReferenceData(ImmutableSet.of(ID1, ID2), ImmutableSet.of(FIELD1, FIELD2));
    assertEquals(1, test.size());
    assertEquals(values, test.get(ID1));
  }

  //-------------------------------------------------------------------------
  class Mock extends AbstractReferenceDataProvider {
    ReferenceDataProviderGetRequest _request;
    ReferenceDataProviderGetResult _result;

    public Mock(ReferenceDataProviderGetRequest request, ReferenceDataProviderGetResult result) {
      _request = request;
      _result = result;
    }

    @Override
    protected ReferenceDataProviderGetResult doBulkGet(ReferenceDataProviderGetRequest request) {
      BeanAssert.assertBeanEquals(_request, request);
      return _result;
    }
  }

}
