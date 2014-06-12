/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import static com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames.DATA_PROVIDER_NAME;
import static com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames.DATA_SOURCE_NAME;
import static com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames.STAR_VALUE;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link DefaultHistoricalTimeSeriesSelector}.
 */
@Test(groups = TestGroup.UNIT)
public class DefaultHistoricalTimeSeriesResolverTest {

  private static final int TS_DATASET_SIZE = 1;
  private static final String LCLOSE_OBSERVATION_TIME = "LCLOSE";
  private static final String DEFAULT_DATA_SOURCE = "BLOOMBERG";
  private static final String DEFAULT_DATA_PROVIDER = "CMPL";

  private static final String[] DATA_FIELDS = new String[] { "PX_LAST", "VOLUME" };
  private static final String[] DATA_PROVIDERS = new String[] { "UNKNOWN", "CMPL", "CMPT" };
  private static final String[] DATA_SOURCES = new String[] { "BLOOMBERG", "REUTERS", "JPM" };
  private static final String CONFIG_DOC_NAME = "TEST";

  private DefaultHistoricalTimeSeriesResolver _infoResolver;
  private HistoricalTimeSeriesMaster _htsMaster = new InMemoryHistoricalTimeSeriesMaster();

  @BeforeMethod
  public void setUp() throws Exception {
    InMemoryConfigMaster configMaster = new InMemoryConfigMaster();
    populateConfigMaster(configMaster);
    _infoResolver = new DefaultHistoricalTimeSeriesResolver(new DefaultHistoricalTimeSeriesSelector(new MasterConfigSource(configMaster)), _htsMaster);
  }

  private void populateConfigMaster(InMemoryConfigMaster configMaster) {
    ConfigItem<HistoricalTimeSeriesRating> testDoc = ConfigItem.of(createRules());
    testDoc.setName(CONFIG_DOC_NAME);
    ConfigMasterUtils.storeByName(configMaster, testDoc);
  }

  private HistoricalTimeSeriesRating createRules() {
    List<HistoricalTimeSeriesRatingRule> rules = new ArrayList<HistoricalTimeSeriesRatingRule>();
    rules.add(HistoricalTimeSeriesRatingRule.of(DATA_SOURCE_NAME, "BLOOMBERG", 3));
    rules.add(HistoricalTimeSeriesRatingRule.of(DATA_SOURCE_NAME, "REUTERS", 2));
    rules.add(HistoricalTimeSeriesRatingRule.of(DATA_SOURCE_NAME, "JPM", 1));
    rules.add(HistoricalTimeSeriesRatingRule.of(DATA_SOURCE_NAME, "XXX", 0));
    rules.add(HistoricalTimeSeriesRatingRule.of(DATA_SOURCE_NAME, STAR_VALUE, 0));
    rules.add(HistoricalTimeSeriesRatingRule.of(DATA_PROVIDER_NAME, "CMPL", 3));
    rules.add(HistoricalTimeSeriesRatingRule.of(DATA_PROVIDER_NAME, "CMPT", 2));
    rules.add(HistoricalTimeSeriesRatingRule.of(DATA_PROVIDER_NAME, "CMPN", 1));
    rules.add(HistoricalTimeSeriesRatingRule.of(DATA_PROVIDER_NAME, "EXCH_LSE", 0));
    rules.add(HistoricalTimeSeriesRatingRule.of(DATA_PROVIDER_NAME, STAR_VALUE, 0));
    return HistoricalTimeSeriesRating.of(rules);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    _infoResolver = null;
    _htsMaster = null;
  }

  //-------------------------------------------------------------------------
  public void test() throws Exception {
    List<ExternalIdBundleWithDates> identifiers = HistoricalTimeSeriesMasterPopulator.populateAndTestMaster(_htsMaster, TS_DATASET_SIZE, DATA_SOURCES, DATA_PROVIDERS, DATA_FIELDS, LCLOSE_OBSERVATION_TIME);
    for (ExternalIdBundleWithDates identifierBundleWithDates : identifiers) {
      HistoricalTimeSeriesResolutionResult resolutionResult = _infoResolver.resolve(identifierBundleWithDates.toBundle(), null, null, null, "PX_LAST", CONFIG_DOC_NAME);
      assertNotNull(resolutionResult);
      HistoricalTimeSeriesInfoDocument doc = _htsMaster.get(resolutionResult.getHistoricalTimeSeriesInfo().getUniqueId());
      assertEquals(DEFAULT_DATA_SOURCE, doc.getInfo().getDataSource());
      assertEquals(DEFAULT_DATA_PROVIDER, doc.getInfo().getDataProvider());
      assertEquals("PX_LAST", doc.getInfo().getDataField());
    }
  }

}
