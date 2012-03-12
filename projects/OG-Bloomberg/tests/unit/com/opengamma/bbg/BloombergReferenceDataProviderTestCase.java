/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 */
public abstract class BloombergReferenceDataProviderTestCase {

  private ReferenceDataProvider _refDataProvider;

  @BeforeMethod
  public void setUpReferenceDataProvider(Method m) throws Exception {
    _refDataProvider = createReferenceDataProvider(m.getDeclaringClass());
  }

  protected abstract ReferenceDataProvider createReferenceDataProvider(Class<?> c) throws Exception;

  @AfterMethod
  public void terminateReferenceDataProvider() throws Exception {
    stopProvider();
    if (_refDataProvider != null) {
      _refDataProvider = null;
    }
  }

  protected abstract void stopProvider() throws Exception ;

  public ReferenceDataProvider getReferenceDataProvider() {
    return _refDataProvider;
  }

  //-------------------------------------------------------------------------
  @Test(timeOut=30000)
  public void singleSecuritySingleField() {
    final String secName = "AAPL US Equity";
    ReferenceDataResult result = _refDataProvider.getFields(Collections.singleton(secName), Collections.singleton("SECURITY_TYP"));
    assertNotNull(result);
    assertEquals(1, result.getSecurities().size());
    assertTrue(result.getSecurities().contains(secName));
    PerSecurityReferenceDataResult aaplResult = result.getResult(secName);
    assertEquals(secName, aaplResult.getSecurity());
    assertTrue(aaplResult.getExceptions().isEmpty());
    FudgeMsg fieldData = aaplResult.getFieldData();
    assertNotNull(fieldData);
    assertEquals(1, fieldData.getNumFields());
    assertEquals("Common Stock", fieldData.getString("SECURITY_TYP"));
  }

  @Test(timeOut=30000)
  public void optionExpiryDate() {
    final String secName = "AAPL 02/19/11 C320 Equity";
    ReferenceDataResult result = _refDataProvider.getFields(Collections.singleton(secName), Collections.singleton("OPT_EXPIRE_DT"));
    assertNotNull(result);
    assertEquals(1, result.getSecurities().size());
    assertTrue(result.getSecurities().contains(secName));
    PerSecurityReferenceDataResult aaplResult = result.getResult(secName);
    assertEquals(secName, aaplResult.getSecurity());
    assertTrue(aaplResult.getExceptions().isEmpty());
    FudgeMsg fieldData = aaplResult.getFieldData();
    assertNotNull(fieldData);
    assertEquals(1, fieldData.getNumFields());
    assertEquals("2011-02-19", fieldData.getString("OPT_EXPIRE_DT"));
  }

  @Test(timeOut=30000)
  public void optionStrikePrice() {
    final String secName = "AAPL 02/19/11 C320 Equity";
    ReferenceDataResult result = _refDataProvider.getFields(Collections.singleton(secName), Collections.singleton("OPT_STRIKE_PX"));
    assertNotNull(result);
    assertEquals(1, result.getSecurities().size());
    assertTrue(result.getSecurities().contains(secName));
    PerSecurityReferenceDataResult aaplResult = result.getResult(secName);
    assertEquals(secName, aaplResult.getSecurity());
    assertTrue(aaplResult.getExceptions().isEmpty());
    FudgeMsg fieldData = aaplResult.getFieldData();
    assertNotNull(fieldData);
    assertEquals(1, fieldData.getNumFields());
    assertEquals(new Double(320.0), fieldData.getDouble("OPT_STRIKE_PX"));
  }

  @Test(timeOut=30000)
  public void singleSecurityBulkDataField() {
    final String secName = "AAPL US Equity";
    ReferenceDataResult result = _refDataProvider.getFields(Collections.singleton(secName), Collections.singleton("OPT_CHAIN"));
    assertNotNull(result);
    assertEquals(1, result.getSecurities().size());
    assertTrue(result.getSecurities().contains(secName));
    PerSecurityReferenceDataResult aaplResult = result.getResult(secName);
    assertEquals(secName, aaplResult.getSecurity());
    assertTrue(aaplResult.getExceptions().isEmpty());
    FudgeMsg fieldData = aaplResult.getFieldData();
    assertNotNull(fieldData);
    assertTrue("Bloomberg only returning these for AAPL Option Chain: " + fieldData, fieldData.getNumFields() > 10);
    boolean foundOptionChain = false;
    for(FudgeField field : fieldData.getAllByName("OPT_CHAIN")) {
      foundOptionChain = true;
      assertEquals("OPT_CHAIN", field.getName());
      assertTrue(field.getValue() instanceof FudgeMsg);
      FudgeMsg chainContainer = (FudgeMsg)field.getValue();
      assertEquals(1, chainContainer.getNumFields());
      assertNotNull(chainContainer.getString("Security Description"));
    }
    assertTrue(foundOptionChain);
  }

  @Test(timeOut=3000000)
  public void multipleSecuritySingleField() {
    Set<String> securities = new TreeSet<String>();
    securities.add("AAPL US Equity");
    securities.add("T US Equity");
    securities.add("GS US Equity");
    securities.add("CSCO US Equity");
    ReferenceDataResult result = _refDataProvider.getFields(securities, Collections.singleton("SECURITY_TYP"));
    assertNotNull(result);
    assertEquals(securities.size(), result.getSecurities().size());
    for(String secName : securities) {
      assertTrue(result.getSecurities().contains(secName));
      PerSecurityReferenceDataResult perSecResult = result.getResult(secName);
      assertEquals(secName, perSecResult.getSecurity());
      assertTrue(perSecResult.getExceptions().isEmpty());
      FudgeMsg fieldData = perSecResult.getFieldData();
      assertNotNull(fieldData);
      assertEquals(1, fieldData.getNumFields());
      assertEquals("Common Stock", fieldData.getString("SECURITY_TYP"));
    }
  }

}
