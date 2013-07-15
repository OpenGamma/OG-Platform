/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.FileReader;
import java.util.Set;
import java.util.TreeSet;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.bbg.BloombergConnector;
import com.opengamma.bbg.referencedata.impl.BloombergReferenceDataProvider;
import com.opengamma.bbg.test.BloombergTestUtils;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.bbg.util.BloombergDomainIdentifierResolver;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION)
public class BloombergReferenceDataProviderTest {

  private BloombergReferenceDataProvider _refDataProvider;

  @BeforeMethod
  public void setUp() throws Exception {
    BloombergConnector connector = BloombergTestUtils.getBloombergConnector();
    _refDataProvider = new BloombergReferenceDataProvider(connector);
    _refDataProvider.start();
  }

  @AfterMethod
  public void terminateReferenceDataProvider() throws Exception {
    if (_refDataProvider != null) {
      _refDataProvider.stop();
      _refDataProvider = null;
    }
  }

  //-------------------------------------------------------------------------
  @Test(timeOut=30000)
  public void singleSecuritySingleField() {
    final String secName = "AAPL US Equity";
    ReferenceDataProviderGetRequest request = ReferenceDataProviderGetRequest.createGet(secName, "SECURITY_TYP", false);
    ReferenceDataProviderGetResult result = _refDataProvider.getReferenceData(request);
    assertNotNull(result);
    assertEquals(1, result.getReferenceData().size());
    ReferenceData aaplResult = result.getReferenceData(secName);
    assertEquals(secName, aaplResult.getIdentifier());
    assertTrue(aaplResult.getErrors().isEmpty());
    FudgeMsg fieldData = aaplResult.getFieldValues();
    assertNotNull(fieldData);
    assertEquals(1, fieldData.getNumFields());
    assertEquals("Common Stock", fieldData.getString("SECURITY_TYP"));
  }

  @Test(timeOut=30000)
  public void optionExpiryDate() {
    final String secName = "AAPL 02/19/11 C320 Equity";
    ReferenceDataProviderGetRequest request = ReferenceDataProviderGetRequest.createGet(secName, "OPT_EXPIRE_DT", false);
    ReferenceDataProviderGetResult result = _refDataProvider.getReferenceData(request);
    assertNotNull(result);
    assertEquals(1, result.getReferenceData().size());
    ReferenceData aaplResult = result.getReferenceData(secName);
    assertEquals(secName, aaplResult.getIdentifier());
    assertTrue(aaplResult.getErrors().isEmpty());
    FudgeMsg fieldData = aaplResult.getFieldValues();
    assertNotNull(fieldData);
    assertEquals(1, fieldData.getNumFields());
    assertEquals("2011-02-19", fieldData.getString("OPT_EXPIRE_DT"));
  }

  @Test(timeOut=30000)
  public void optionStrikePrice() {
    final String secName = "AAPL 02/19/11 C320 Equity";
    ReferenceDataProviderGetRequest request = ReferenceDataProviderGetRequest.createGet(secName, "OPT_STRIKE_PX", false);
    ReferenceDataProviderGetResult result = _refDataProvider.getReferenceData(request);
    assertNotNull(result);
    assertEquals(1, result.getReferenceData().size());
    ReferenceData aaplResult = result.getReferenceData(secName);
    assertEquals(secName, aaplResult.getIdentifier());
    assertTrue(aaplResult.getErrors().isEmpty());
    FudgeMsg fieldData = aaplResult.getFieldValues();
    assertNotNull(fieldData);
    assertEquals(1, fieldData.getNumFields());
    assertEquals(new Double(320.0), fieldData.getDouble("OPT_STRIKE_PX"));
  }

  @Test(timeOut=30000)
  public void singleSecurityBulkDataField() {
    final String secName = "AAPL US Equity";
    ReferenceDataProviderGetRequest request = ReferenceDataProviderGetRequest.createGet(secName, "OPT_CHAIN", false);
    ReferenceDataProviderGetResult result = _refDataProvider.getReferenceData(request);
    assertNotNull(result);
    assertEquals(1, result.getReferenceData().size());
    ReferenceData aaplResult = result.getReferenceData(secName);
    assertEquals(secName, aaplResult.getIdentifier());
    assertTrue(aaplResult.getErrors().isEmpty());
    FudgeMsg fieldData = aaplResult.getFieldValues();
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
    ReferenceDataProviderGetRequest request = ReferenceDataProviderGetRequest.createGet(securities, ImmutableSet.of("SECURITY_TYP"), false);
    ReferenceDataProviderGetResult result = _refDataProvider.getReferenceData(request);
    assertNotNull(result);
    assertEquals(securities.size(), result.getReferenceData().size());
    for(String secName : securities) {
      ReferenceData perSecResult = result.getReferenceData(secName);
      assertEquals(secName, perSecResult.getIdentifier());
      assertTrue(perSecResult.getErrors().isEmpty());
      FudgeMsg fieldData = perSecResult.getFieldValues();
      assertNotNull(fieldData);
      assertEquals(1, fieldData.getNumFields());
      assertEquals("Common Stock", fieldData.getString("SECURITY_TYP"));
    }
  }

  //-------------------------------------------------------------------------
  private static final Set<String> VALID_EQUITY_TYPES = new TreeSet<String>();
  static {
    VALID_EQUITY_TYPES.add("Common Stock");
    VALID_EQUITY_TYPES.add("ADR");
    VALID_EQUITY_TYPES.add("Closed-End Fund");
    VALID_EQUITY_TYPES.add("ETP");
    VALID_EQUITY_TYPES.add("REIT");
    VALID_EQUITY_TYPES.add("Tracking Stk");
  }

  @Test(enabled = false, description = "a lot of data to request from bloomberg")
  //Bloomberg sends multiple messages per request when you have over 10 securities in the the request
  public void multiMessagePerRequest() throws Exception {
    String testWatchList = BloombergReferenceDataProviderTest.class.getResource("watchListTest.txt").getPath();
    Set<ExternalId> identifiers = BloombergDataUtils.identifierLoader(new FileReader(testWatchList));
    Set<String> bloombergKeys = toBloombergKeys(identifiers);
    ReferenceDataProviderGetRequest request = ReferenceDataProviderGetRequest.createGet(bloombergKeys, ImmutableSet.of("SECURITY_TYP"), false);
    ReferenceDataProviderGetResult result = _refDataProvider.getReferenceData(request);
    assertNotNull(result);
    assertEquals(bloombergKeys.size(), result.getReferenceData().size());
    for (String secName : bloombergKeys) {
      ReferenceData perSecResult = result.getReferenceData(secName);
      assertEquals(secName, perSecResult.getIdentifier());
      assertTrue(perSecResult.getErrors().isEmpty());
      FudgeMsg fieldData = perSecResult.getFieldValues();
      assertNotNull(fieldData);
      assertEquals(1, fieldData.getNumFields());
      assertTrue(VALID_EQUITY_TYPES.contains(fieldData.getString("SECURITY_TYP")));
    }
  }

  private Set<String> toBloombergKeys(Set<ExternalId> identifiers) {
    Set<String> bloombergKeys = Sets.newHashSet();
    for (ExternalId identifier : identifiers) {
      bloombergKeys.add(BloombergDomainIdentifierResolver.toBloombergKey(identifier));
    }
    return bloombergKeys;
  }

}
