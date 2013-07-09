/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.cache;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.bbg.referencedata.MockReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.test.MongoCachedReferenceData;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups= {TestGroup.UNIT_DB, "mongodb"})
public class MongoDBValueCachingReferenceDataProviderTest extends AbstractValueCachingReferenceDataProviderTestCase {

  private MockReferenceDataProvider _underlyingProvider;
  private UnitTestingReferenceDataProvider _unitProvider;
  private ReferenceDataProvider _provider;

  @BeforeMethod
  public void setUp() {
    _underlyingProvider = new MockReferenceDataProvider();
    _unitProvider = new UnitTestingReferenceDataProvider(_underlyingProvider);
    boolean clearData = true; // This is why we make real queries
    _provider = MongoCachedReferenceData.makeMongoProvider(
        _unitProvider, MongoDBValueCachingReferenceDataProviderTest.class, clearData);
  }

  //-------------------------------------------------------------------------
  @Override
  protected MockReferenceDataProvider getUnderlyingProvider() {
    return _underlyingProvider;
  }

  @Override
  protected UnitTestingReferenceDataProvider getUnitProvider() {
    return _unitProvider;
  }

  @Override
  protected ReferenceDataProvider getProvider() {
    return _provider;
  }

  //-------------------------------------------------------------------------
  @Test(groups= {TestGroup.UNIT_DB, "mongodb"})
  public void numberOfReturnedFields() {
    super.numberOfReturnedFields();
  }

  @Test(groups= {TestGroup.UNIT_DB, "mongodb"})
  public void singleSecurityEscalatingFields() {
    super.numberOfReturnedFields();
  }

  @Test(groups= {TestGroup.UNIT_DB, "mongodb"})
  public void fieldNotAvailable() {
    super.numberOfReturnedFields();
  }

  @Test(groups= {TestGroup.UNIT_DB, "mongodb"})
  public void securityNotAvailable() {
    super.numberOfReturnedFields();
  }

  @Test(groups= {TestGroup.UNIT_DB, "mongodb"})
  public void multipleSecuritiesSameEscalatingFields() {
    super.numberOfReturnedFields();
  }

}
