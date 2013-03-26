/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.distribution;

import static org.testng.AssertJUnit.assertEquals;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.livedata.LiveDataValueUpdate;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.livedata.server.MapLastKnownValueStoreProvider;
import com.opengamma.livedata.server.Subscription;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MarketDataDistributorTest {
  public static MarketDataDistributor getTestDistributor(MarketDataSenderFactory factory) {
    MapLastKnownValueStoreProvider lkvStoreProvider = new MapLastKnownValueStoreProvider();
    return new MarketDataDistributor(new DistributionSpecification(
        ExternalId.of("RIC", "AAPL.O"),
        StandardRules.getNoNormalization(),
        "LiveData.Bloomberg.Equity.AAPL"),
        new Subscription("", factory, lkvStoreProvider),
        factory,
        false,
        lkvStoreProvider);
  }

  static MarketDataDistributor getTestDistributor() {
    return getTestDistributor(new EmptyMarketDataSenderFactory());
  }

  //-------------------------------------------------------------------------
  public void testSequenceNumber() {
    MarketDataDistributor mdd = getTestDistributor();
    assertEquals(LiveDataValueUpdate.SEQUENCE_START, mdd.getNumMessagesSent());
    mdd.updateFieldHistory(FudgeContext.EMPTY_MESSAGE);
    assertEquals(0, mdd.getNumMessagesSent());
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("foo", "bar");
    mdd.distributeLiveData(msg);
    assertEquals(1, mdd.getNumMessagesSent());
    
    mdd.distributeLiveData(FudgeContext.EMPTY_MESSAGE); // empty msg not sent
    assertEquals(1, mdd.getNumMessagesSent());
  }

}
