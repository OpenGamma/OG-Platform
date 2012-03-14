/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.FileReader;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.fudgemsg.FudgeMsg;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.bbg.test.BloombergLiveDataServerUtils;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.bbg.util.BloombergDomainIdentifierResolver;
import com.opengamma.id.ExternalId;

/**
 * 
 */
public class BloombergReferenceDataProviderDirectTest extends BloombergReferenceDataProviderTestCase {

  private static final Set<String> VALID_EQUITY_TYPES = new TreeSet<String>();

  static {
    VALID_EQUITY_TYPES.add("Common Stock");
    VALID_EQUITY_TYPES.add("ADR");
    VALID_EQUITY_TYPES.add("Closed-End Fund");
    VALID_EQUITY_TYPES.add("ETP");
    VALID_EQUITY_TYPES.add("REIT");
    VALID_EQUITY_TYPES.add("Tracking Stk");
  }

  @Override
  protected ReferenceDataProvider createReferenceDataProvider(Class<?> c) throws Exception {
    return BloombergLiveDataServerUtils.getCachingReferenceDataProvider(c);
  }
  
  @Override
  protected void stopProvider() {
    ReferenceDataProvider referenceDataProvider = getReferenceDataProvider();
    BloombergLiveDataServerUtils.stopCachingReferenceDataProvider((CachingReferenceDataProvider) referenceDataProvider);
  }

  @Test(enabled = false, description = "a lot of data to request from bloomberg")
  //Bloomberg sends multiple messages per request when you have over 10 securities in the the request
  public void multiMessagePerRequest() throws Exception {
    String testWatchList = BloombergReferenceDataProviderDirectTest.class.getResource("watchListTest.txt").getPath();
    BloombergReferenceDataProvider dataProvider = (BloombergReferenceDataProvider) getReferenceDataProvider();
    Set<ExternalId> identifiers = BloombergDataUtils.identifierLoader(new FileReader(testWatchList));
    Set<String> bloombergKeys = toBloombergKeys(identifiers);
    ReferenceDataResult result = dataProvider.getFields(bloombergKeys, Collections.singleton("SECURITY_TYP"));
    assertNotNull(result);
    assertEquals(bloombergKeys.size(), result.getSecurities().size());
    for (String secName : bloombergKeys) {
      assertTrue(result.getSecurities().contains(secName));
      PerSecurityReferenceDataResult perSecResult = result.getResult(secName);
      assertEquals(secName, perSecResult.getSecurity());
      assertTrue(perSecResult.getExceptions().isEmpty());
      FudgeMsg fieldData = perSecResult.getFieldData();
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
