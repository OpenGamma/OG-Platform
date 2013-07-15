/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import static org.mockito.Mockito.mock;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjuster;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Triple;

/**
 * Tests {@link FieldMappingHistoricalTimeSeriesResolver}.
 */
@Test(groups = TestGroup.UNIT)
public class FieldMappingHistoricalTimeSeriesResolverTest {

  private static final int TS_DATASET_SIZE = 1;
  private static final String LCLOSE_OBSERVATION_TIME = "LCLOSE";
  
  private static final String[] DATA_FIELDS = new String[] { "PX_LAST", "VOLUME" };
  private static final String[] DATA_PROVIDERS = new String[] { "UNKNOWN", "CMPL", "CMPT" };
  private static final String[] DATA_SOURCES = new String[] { "BLOOMBERG", "REUTERS", "JPM" };
  
  private List<ExternalIdBundleWithDates> _identifiers;
  private TestHistoricalTimeSeriesSelector _selector = new TestHistoricalTimeSeriesSelector();
  private HistoricalTimeSeriesMaster _htsMaster = new InMemoryHistoricalTimeSeriesMaster();
  
  @BeforeClass
  public void setUp() {
    _identifiers = HistoricalTimeSeriesMasterPopulator.populateAndTestMaster(_htsMaster, TS_DATASET_SIZE, DATA_SOURCES, DATA_PROVIDERS, DATA_FIELDS, LCLOSE_OBSERVATION_TIME);
  }
  
  @Test
  public void testNoMaps() {
    FieldMappingHistoricalTimeSeriesResolver resolver = new FieldMappingHistoricalTimeSeriesResolver(ImmutableList.<HistoricalTimeSeriesFieldAdjustmentMap>of(), _selector, _htsMaster);
    for (ExternalIdBundleWithDates identifierBundleWithDates : _identifiers) {
      resolver.resolve(identifierBundleWithDates.toBundle(), null, null, null, "PX_LAST", null);
      assertEquals(9, _selector.getLastCandidates().size());
      
      resolver.resolve(identifierBundleWithDates.toBundle(), null, null, null, "Something else", null);
      assertEquals(0, _selector.getLastCandidates().size());
    }
  }
  
  @Test
  public void testMappings() {
    String syntheticFieldName = "Synthetic";
    
    // Synthetic -> (CMPL, BLOOMBERG.PX_LAST)
    HistoricalTimeSeriesFieldAdjustmentMap map1 = new HistoricalTimeSeriesFieldAdjustmentMap("BLOOMBERG");
    map1.addFieldAdjustment(syntheticFieldName, "CMPL", "PX_LAST", null);
    
    // Synthetic -> (*, REUTERS.VOLUME, adjuster)
    HistoricalTimeSeriesFieldAdjustmentMap map2 = new HistoricalTimeSeriesFieldAdjustmentMap("REUTERS");
    HistoricalTimeSeriesAdjuster reutersAdjuster = mock(HistoricalTimeSeriesAdjuster.class);
    map2.addFieldAdjustment(syntheticFieldName, null, "VOLUME", reutersAdjuster);
    
    FieldMappingHistoricalTimeSeriesResolver resolver = new FieldMappingHistoricalTimeSeriesResolver(ImmutableList.<HistoricalTimeSeriesFieldAdjustmentMap>of(map1, map2), _selector, _htsMaster);
    for (ExternalIdBundleWithDates identifierBundleWithDates : _identifiers) {
      resolver.resolve(identifierBundleWithDates.toBundle(), null, null, null, "PX_LAST", null);
      assertEquals(9, _selector.getLastCandidates().size());
      
      resolver.resolve(identifierBundleWithDates.toBundle(), null, null, null, "Something else", null);
      assertEquals(0, _selector.getLastCandidates().size());
      
      resolver.resolve(identifierBundleWithDates.toBundle(), null, null, null, syntheticFieldName, null);
      Set<Triple<String, String, String>> candidateInfo = getCandidateInfo(_selector.getLastCandidates());
      assertEquals(candidateInfo.toString(), 4, candidateInfo.size());
      assertTrue(candidateInfo.contains(Triple.of("BLOOMBERG", "CMPL", "PX_LAST")));
      assertTrue(candidateInfo.contains(Triple.of("REUTERS", "UNKNOWN", "VOLUME")));
      assertTrue(candidateInfo.contains(Triple.of("REUTERS", "CMPL", "VOLUME")));
      assertTrue(candidateInfo.contains(Triple.of("REUTERS", "CMPT", "VOLUME")));
      
      HistoricalTimeSeriesResolutionResult resolutionResult = resolver.resolve(identifierBundleWithDates.toBundle(), null, "REUTERS", "Ignored for mapped fields", syntheticFieldName, null);
      assertEquals(3, _selector.getLastCandidates().size());
      assertEquals(reutersAdjuster, resolutionResult.getAdjuster());
    }
  }
  
  private Set<Triple<String, String, String>> getCandidateInfo(Collection<ManageableHistoricalTimeSeriesInfo> candidates) {
    Set<Triple<String, String, String>> results = new HashSet<Triple<String,String,String>>();
    for (ManageableHistoricalTimeSeriesInfo candidate : candidates) {
      results.add(Triple.of(candidate.getDataSource(), candidate.getDataProvider(), candidate.getDataField()));
    }
    return results;
  }
  
}
