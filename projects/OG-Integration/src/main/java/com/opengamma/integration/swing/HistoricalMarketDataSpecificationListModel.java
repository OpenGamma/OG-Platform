/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.swing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.SwingWorker;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRating;
import com.opengamma.provider.livedata.LiveDataMetaDataProvider;
import com.opengamma.provider.livedata.LiveDataMetaDataProviderRequest;
import com.opengamma.provider.livedata.LiveDataMetaDataProviderResult;

/**
 * List/ComboBox model for historical market data specifications
 */
public class HistoricalMarketDataSpecificationListModel extends AbstractListModel<String> implements ComboBoxModel<String> {
  private static final long serialVersionUID = 1L;
  private List<String> _names = Collections.emptyList();
  private Object _selected;
  
  public HistoricalMarketDataSpecificationListModel(final ConfigMaster configMaster) {
    SwingWorker<List<String>, Object> worker = new SwingWorker<List<String>, Object>() {

      @Override
      protected List<String> doInBackground() throws Exception {
        List<String> resolverNames = new ArrayList<>();
        ConfigSearchRequest<HistoricalTimeSeriesRating> configSearchRequest = new ConfigSearchRequest<HistoricalTimeSeriesRating>();
        configSearchRequest.setType(HistoricalTimeSeriesRating.class);
        ConfigSearchResult<HistoricalTimeSeriesRating> searchResults = configMaster.search(configSearchRequest);
        for (ConfigItem<HistoricalTimeSeriesRating> item : searchResults.getValues()) {
          resolverNames.add(item.getName());
        }
        return resolverNames;
      }
      
      @Override
      protected void done() {
        try {
          _names = get();
          fireIntervalAdded(HistoricalMarketDataSpecificationListModel.this, 0, _names.size() - 1);
        } catch (InterruptedException ex) {
          throw new OpenGammaRuntimeException("InterruptedException retreiving available market data specifications", ex);
        } catch (ExecutionException ex) {
          throw new OpenGammaRuntimeException("ExecutionException retreiving available market data specifications", ex);
        }
      }
    };
    worker.execute();
  }
  
   
  @Override
  public int getSize() {
    return _names.size();
  }

  @Override
  public String getElementAt(int index) {
    return _names.get(index);
  }

  @Override
  public void setSelectedItem(Object anItem) {
    _selected = anItem;
  }

  @Override
  public Object getSelectedItem() {
    return _selected;
  }
  
}
