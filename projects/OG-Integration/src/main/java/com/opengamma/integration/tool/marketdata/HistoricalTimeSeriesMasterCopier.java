/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.marketdata;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesMasterUtils;

/**
 * Class to copy all HTS from one master to another.
 */
public class HistoricalTimeSeriesMasterCopier {
  private static final Logger s_logger = LoggerFactory.getLogger(HistoricalTimeSeriesMasterCopier.class);
  
  private HistoricalTimeSeriesMaster _sourceMaster;
  private HistoricalTimeSeriesMaster _destinationMaster;

  public HistoricalTimeSeriesMasterCopier(HistoricalTimeSeriesMaster sourceMaster, HistoricalTimeSeriesMaster destinationMaster) {
    _sourceMaster = sourceMaster;
    _destinationMaster = destinationMaster;
  }
  
  public void copy(boolean fastCopy, boolean deleteDestinationSeriesNotInSource, boolean verbose, boolean noAdditions) {
    HistoricalTimeSeriesInfoSearchRequest infoSearchRequest = new HistoricalTimeSeriesInfoSearchRequest();
    HistoricalTimeSeriesInfoSearchResult sourceSearchResult = _sourceMaster.search(infoSearchRequest);
    List<ManageableHistoricalTimeSeriesInfo> sourceInfoList = sourceSearchResult.getInfoList();
    HistoricalTimeSeriesInfoSearchResult destSearchResult = _destinationMaster.search(infoSearchRequest);
    List<ManageableHistoricalTimeSeriesInfo> destInfoList = destSearchResult.getInfoList();
    Set<ManageableHistoricalTimeSeriesInfo> bothInfoSetSource = new TreeSet<ManageableHistoricalTimeSeriesInfo>(new ManageableHistoricalTimeSeriesInfoComparator());
    bothInfoSetSource.addAll(sourceInfoList);
    bothInfoSetSource.retainAll(destInfoList);
    Set<ManageableHistoricalTimeSeriesInfo> bothInfoSetDestination = new TreeSet<ManageableHistoricalTimeSeriesInfo>(new ManageableHistoricalTimeSeriesInfoComparator());
    bothInfoSetDestination.addAll(destInfoList);
    bothInfoSetDestination.retainAll(sourceInfoList);
    Map<ManageableHistoricalTimeSeriesInfo, UniqueId> infoToSourceUniqueIds = Maps.newHashMap();
    // we have have two sets containing the TS in both, with the ids from the source in one and the ids from the dest in the other
    // now build a map of info->source uid
    for (ManageableHistoricalTimeSeriesInfo info : bothInfoSetSource) {
      infoToSourceUniqueIds.put(info, info.getUniqueId());
    }
    // step through the destination results and look up the corresponding source id.
    for (ManageableHistoricalTimeSeriesInfo info : bothInfoSetDestination) {
      if (infoToSourceUniqueIds.containsKey(info)) {
        if (verbose) {
          System.out.println("Time series " + info + " is in source and destination");
        }
        UniqueId sourceId = infoToSourceUniqueIds.get(info);
        UniqueId destinationId = info.getUniqueId();
        diffAndCopy(sourceId, destinationId, fastCopy, verbose);
      } else {
        throw new OpenGammaRuntimeException("Couldn't find info in set, which is supposed to be impossible");
      }
    }
    if (!noAdditions) {
      Set<ManageableHistoricalTimeSeriesInfo> sourceNotDestinationInfo = new TreeSet<ManageableHistoricalTimeSeriesInfo>(new ManageableHistoricalTimeSeriesInfoComparator());
      sourceNotDestinationInfo.addAll(sourceInfoList);
      sourceNotDestinationInfo.removeAll(destInfoList);
      for (ManageableHistoricalTimeSeriesInfo info : sourceNotDestinationInfo) {
        if (verbose) {
          System.out.println("Time series " + info + " is in source but not destination");
        }
        add(info, verbose);
      }
    }
    if (deleteDestinationSeriesNotInSource) {
      Set<ManageableHistoricalTimeSeriesInfo> destinationNotSourceInfo = new TreeSet<ManageableHistoricalTimeSeriesInfo>(new ManageableHistoricalTimeSeriesInfoComparator());
      destinationNotSourceInfo.addAll(destInfoList);
      destinationNotSourceInfo.removeAll(sourceInfoList);
      for (ManageableHistoricalTimeSeriesInfo info : destinationNotSourceInfo) {
        delete(info);
        if (verbose) {
          System.out.println("Deleted time series " + info + " which is in destination but not source");
        }
      }
    }
  }
  
  private void delete(ManageableHistoricalTimeSeriesInfo info) {
    _destinationMaster.remove(info.getUniqueId());

  }

  private void add(ManageableHistoricalTimeSeriesInfo sourceInfo, boolean verbose) {
    HistoricalTimeSeriesMasterUtils destinationMasterUtils = new HistoricalTimeSeriesMasterUtils(_destinationMaster);
    HistoricalTimeSeries series  = _destinationMaster.getTimeSeries(sourceInfo.getUniqueId());
    destinationMasterUtils.writeTimeSeries(sourceInfo.getName(), 
                                           sourceInfo.getDataSource(),
                                           sourceInfo.getDataProvider(),
                                           sourceInfo.getDataField(), 
                                           sourceInfo.getObservationTime(), 
                                           sourceInfo.getExternalIdBundle().toBundle(),
                                           series.getTimeSeries());
    if (verbose) {
      System.out.println("Added new time series to destination with " + series.getTimeSeries().size() + " data points");
    }
  }
  
  private boolean diffAndCopy(UniqueId sourceId, UniqueId destinationId, boolean fastCopy, boolean verbose) {
    if (fastCopy) {
      ManageableHistoricalTimeSeries sourceTimeSeries = _sourceMaster.getTimeSeries(sourceId, HistoricalTimeSeriesGetFilter.ofLatestPoint());
      ManageableHistoricalTimeSeries destTimeSeries = _destinationMaster.getTimeSeries(destinationId, HistoricalTimeSeriesGetFilter.ofLatestPoint());
      if (!sourceTimeSeries.getTimeSeries().equals(destTimeSeries.getTimeSeries())) {
        HistoricalTimeSeriesGetFilter filter = new HistoricalTimeSeriesGetFilter();
        LocalDate lastSourceDate = sourceTimeSeries.getTimeSeries().getLatestTime();
        LocalDate lastDestinationDate = destTimeSeries.getTimeSeries().getLatestTime();
        if (lastSourceDate.isAfter(lastDestinationDate)) {
          filter.setEarliestDate(lastDestinationDate.plusDays(1));
          filter.setLatestDate(lastSourceDate);
          sourceTimeSeries = _sourceMaster.getTimeSeries(sourceId, filter);
          // get JUST the new days
          _destinationMaster.updateTimeSeriesDataPoints(destinationId, sourceTimeSeries.getTimeSeries());
          if (verbose) {
            System.out.println("Fast updating " + sourceTimeSeries.getTimeSeries().size() + " data points");
          }
          return true;
        } else {
          s_logger.warn("Destination for " + destinationId + " has more up to date data than source, skipping!");
          return false;
        }
      } else {
        if (verbose) {
          System.out.println("Fast compare of source and destination show they are the same, skipping");
        }
      }
      return false;
    } else {
      ManageableHistoricalTimeSeries sourceTimeSeries = _sourceMaster.getTimeSeries(sourceId);
      ManageableHistoricalTimeSeries destTimeSeries = _destinationMaster.getTimeSeries(destinationId);
      if (!sourceTimeSeries.getTimeSeries().equals(destTimeSeries.getTimeSeries())) {
        sourceTimeSeries = _sourceMaster.getTimeSeries(sourceId);
        HistoricalTimeSeriesMasterUtils masterUtils = new HistoricalTimeSeriesMasterUtils(_destinationMaster);
        masterUtils.writeTimeSeries(destinationId, sourceTimeSeries.getTimeSeries());
        if (verbose) {
          System.out.println("Full (slow) copy of source data to destination");
        } else {
          System.out.println("Full (slow) compare of source and destination show they are the same, skipping");
        }
      }
      return sourceTimeSeries.getTimeSeries().equals(destTimeSeries.getTimeSeries());
    }
  }
  

}

