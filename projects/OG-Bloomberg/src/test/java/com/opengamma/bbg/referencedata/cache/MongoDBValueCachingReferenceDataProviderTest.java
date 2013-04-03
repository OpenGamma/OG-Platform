/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.cache;

import org.testng.annotations.Test;

import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.test.MongoCachedReferenceData;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups= {TestGroup.UNIT_DB, "mongodb"})
public class MongoDBValueCachingReferenceDataProviderTest extends AbstractValueCachingReferenceDataProviderTestCase {

  @Override
  protected ReferenceDataProvider createCachingProvider() {
    boolean clearData = true; // This is why we make real queries
    return MongoCachedReferenceData.makeMongoProvider(getUnderlyingProvider(), MongoDBValueCachingReferenceDataProviderTest.class, clearData);
  }

}
