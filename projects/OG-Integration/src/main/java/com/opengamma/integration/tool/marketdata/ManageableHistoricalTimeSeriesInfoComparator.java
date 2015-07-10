/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.marketdata;

import java.util.Comparator;

import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;

/**
 * Comparator for ManagableHistoricalTimeSeriesInfo that excludes UniqueIds and TS ObjectIds.
 */
public class ManageableHistoricalTimeSeriesInfoComparator implements Comparator<ManageableHistoricalTimeSeriesInfo> {

  @Override
  public int compare(ManageableHistoricalTimeSeriesInfo first, ManageableHistoricalTimeSeriesInfo second) {
    int name = first.getName().compareTo(second.getName());
    if (name != 0) {
      return name;
    }
    int dataField = first.getDataField().compareTo(second.getDataField());
    if (dataField != 0) {
      return dataField;
    }
    int dataSource = first.getDataSource().compareTo(second.getDataSource());
    if (dataSource != 0) {
      return dataSource;
    }
    int dataProvider = first.getDataProvider().compareTo(second.getDataProvider());
    if (dataProvider != 0) {
      return dataProvider;
    }
    int observationTime = first.getObservationTime().compareTo(second.getObservationTime());
    if (observationTime != 0) {
      return observationTime;
    }
    int externalIdBundle = first.getExternalIdBundle().compareTo(second.getExternalIdBundle());
    return externalIdBundle;
  }

}
