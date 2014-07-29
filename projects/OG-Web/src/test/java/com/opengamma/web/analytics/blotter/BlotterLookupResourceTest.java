/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import static org.testng.AssertJUnit.assertTrue;

import org.json.JSONArray;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.financial.conversion.JodaBeanConverters;
import com.opengamma.util.test.TestGroup;
import com.opengamma.web.analytics.JsonTestUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BlotterLookupResourceTest {

  private final BlotterLookupResource _resource = new BlotterLookupResource(BlotterUtils.getStringConvert());

  static {
    // ensure the converters are loaded and registered
    JodaBeanConverters.getInstance();
  }

  @Test
  public void getFrequencies() throws JSONException {
    JSONArray expected = new JSONArray(Lists.newArrayList(
        "Continuous",
        "Daily",
        "Weekly",
        "Bi-weekly",
        "Three week",
        "Twenty Eight Days",
        "Monthly",
        "Bi-monthly",
        "Quarterly",
        "Four Month",
        "Five Month",
        "Semi-annual",
        "Seven Month",
        "Eight Month",
        "Nine Month",
        "Ten Month",
        "Eleven Month",
        "Annual",
        "Never"));
    assertTrue(JsonTestUtils.equal(expected, new JSONArray(_resource.getFrequencies())));
  }

  @Test
  public void getExerciseTypes() throws JSONException {
    JSONArray expected = new JSONArray(Lists.newArrayList("European"));
    //JSONArray expected = new JSONArray(Lists.newArrayList("American", "Asian", "Bermudan", "European"));
    assertTrue(JsonTestUtils.equal(expected, new JSONArray(_resource.getExerciseTypes())));
  }

  @Test
  public void getBarrierTypes() throws JSONException {
    JSONArray expected = new JSONArray(Lists.newArrayList("Up", "Down", "Double"));
    assertTrue(JsonTestUtils.equal(expected, new JSONArray(_resource.getBarrierTypes())));
  }

  @Test
  public void getBarrierDirections() throws JSONException {
    JSONArray expected = new JSONArray(Lists.newArrayList("Knock In", "Knock Out"));
    assertTrue(JsonTestUtils.equal(expected, new JSONArray(_resource.getBarrierDirections())));
  }

  @Test
  public void getSamplingFrequencies() throws JSONException {
    JSONArray expected = new JSONArray(Lists.newArrayList("Daily Close", "Friday", "Weekly Close", "Continuous", "One Look"));
    assertTrue(JsonTestUtils.equal(expected, new JSONArray(_resource.getSamplingFrequencies())));
  }

  @Test
  public void getFloatingRateTypes() throws JSONException {
    JSONArray expected = new JSONArray(Lists.newArrayList("IBOR", "CMS", "OIS", "OVERNIGHT_ARITHMETIC_AVERAGE"));
    assertTrue(JsonTestUtils.equal(expected, new JSONArray(_resource.getFloatingRateTypes())));
  }

  @Test
  public void getMonitoringType() throws JSONException {
    JSONArray expected = new JSONArray(Lists.newArrayList("Continuous", "Discrete"));
    assertTrue(JsonTestUtils.equal(expected, new JSONArray(_resource.getMonitoringType())));
  }
}
