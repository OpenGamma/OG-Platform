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
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.provider.livedata.LiveDataMetaDataProvider;
import com.opengamma.provider.livedata.LiveDataMetaDataProviderRequest;
import com.opengamma.provider.livedata.LiveDataMetaDataProviderResult;

/**
 * List/ComboBox model for live market data specifications
 */
public class LiveMarketDataSpecificationListModel extends AbstractListModel<String> implements ComboBoxModel<String> {
  private static final long serialVersionUID = 1L;
  private Map<String, MarketDataSpecification> _specsByName = Collections.emptyMap();
  private Map<MarketDataSpecification, String> _namesBySpec = Collections.emptyMap();
  private List<String> _names = Collections.emptyList();
  private Object _selected;
  
  public LiveMarketDataSpecificationListModel(final List<LiveDataMetaDataProvider> liveDataMetaDataProviders) {
    SwingWorker<Map<String, MarketDataSpecification>, Object> worker = new SwingWorker<Map<String, MarketDataSpecification>, Object>() {

      @Override
      protected Map<String, MarketDataSpecification> doInBackground() throws Exception {
        Map<String, MarketDataSpecification> specsByName = new LinkedHashMap<>();
        for (LiveDataMetaDataProvider liveDataMetaDataProvider : liveDataMetaDataProviders) {
          LiveDataMetaDataProviderRequest liveDataMetaDataProviderRequest = new LiveDataMetaDataProviderRequest();
          LiveDataMetaDataProviderResult metaData = liveDataMetaDataProvider.metaData(liveDataMetaDataProviderRequest);
          // REVIEW jim 30-Oct-2013 -- This is a hack because BloombergLiveDataClientComponentFactory doesn't respect the description.  
          // I don't want to risk changing it at the moment. Comment in code to refactor if fixed.
          if (metaData.getMetaData().getDescription().equals("Bloomberg")) {
            specsByName.put(metaData.getMetaData().getDescription(), LiveMarketDataSpecification.of("Live market data (" + metaData.getMetaData().getDescription() + ")"));
          } else {
            specsByName.put(metaData.getMetaData().getDescription(), LiveMarketDataSpecification.of(metaData.getMetaData().getDescription()));
          }
        }
        return specsByName;
      }
      
      @Override
      protected void done() {
        try {
          _specsByName = get();
          _names = new ArrayList<String>(_specsByName.keySet());
          _namesBySpec = new LinkedHashMap<MarketDataSpecification, String>();
          for (String name : _names) {
            _namesBySpec.put(_specsByName.get(name), name);
          }
          fireIntervalAdded(LiveMarketDataSpecificationListModel.this, 0, _specsByName.size() - 1);
        } catch (InterruptedException ex) {
          throw new OpenGammaRuntimeException("InterruptedException retreiving available market data specifications", ex);
        } catch (ExecutionException ex) {
          throw new OpenGammaRuntimeException("ExecutionException retreiving available market data specifications", ex);
        }
      }
    };
    worker.execute();
  }
  
  public MarketDataSpecification getMarketDataSpec(String selected) {
    return _specsByName.get(selected);
  }
  
  @Override
  public int getSize() {
    return _specsByName.size();
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
