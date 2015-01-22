/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.swing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.*;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * List/ComboBox model for historical market data specifications
 */
public class SnapshotMarketDataSpecificationVersionListModel extends AbstractListModel<String> implements ComboBoxModel<String> {
  private static final long serialVersionUID = 1L;
  private List<String> _names = Collections.emptyList();
  private List<UniqueId> _uniqueIds = Collections.emptyList();
  private Object _selected;
  
  public SnapshotMarketDataSpecificationVersionListModel(final MarketDataSnapshotMaster snapshotMaster, final ObjectId objectId) {
    SwingWorker<List<Pair<String, UniqueId>>, Object> worker = new SwingWorker<List<Pair<String, UniqueId>>, Object>() {

      private final DateTimeFormatter _formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT);
      @Override
      protected List<Pair<String, UniqueId>> doInBackground() throws Exception {
        List<Pair<String, UniqueId>> resolverNames = new ArrayList<>();
        MarketDataSnapshotHistoryRequest historyRequest = new MarketDataSnapshotHistoryRequest();
        historyRequest.setIncludeData(false);
        historyRequest.setObjectId(objectId);
        MarketDataSnapshotHistoryResult searchResults = snapshotMaster.history(historyRequest);
        for (MarketDataSnapshotDocument item : searchResults.getDocuments()) {
          resolverNames.add(Pairs.of(versionDescription(item), item.getUniqueId()));
        }
        return resolverNames;
      }
      
      private String versionDescription(MarketDataSnapshotDocument doc) {
        StringBuilder sb = new StringBuilder();
        boolean vcEqual = ObjectUtils.equals(doc.getVersionFromInstant(), doc.getCorrectionFromInstant()) && ObjectUtils.equals(doc.getVersionToInstant(), doc.getCorrectionToInstant());
        if (vcEqual) {
          sb.append("Valid:");          
        } else {
          sb.append("Version Valid:");
        }
        if (doc.getVersionFromInstant() != null || doc.getVersionToInstant() != null) {
          if (doc.getVersionFromInstant() != null) {
            sb.append(_formatter.format(doc.getVersionFromInstant().atZone(ZoneOffset.UTC)));
          } else {
            sb.append("Start-of-time");
          }
          sb.append(" - ");
          if (doc.getVersionToInstant() != null) {
            sb.append(_formatter.format(doc.getVersionFromInstant().atZone(ZoneOffset.UTC)));
          } else {
            sb.append("End-of-time");
          }
        } else {
          sb.append("All-of-time");
        }
        if ((doc.getCorrectionFromInstant() != null || doc.getCorrectionToInstant() != null) && !vcEqual) {
          sb.append(", Correction Validity:");
          if (doc.getCorrectionFromInstant() != null) {
            sb.append(_formatter.format(doc.getVersionFromInstant().atZone(ZoneOffset.UTC)));
          } else {
            sb.append("Start-of-time");
          }
          sb.append(" - ");
          if (doc.getCorrectionToInstant() != null) {
            sb.append(_formatter.format(doc.getVersionFromInstant().atZone(ZoneOffset.UTC)));
          } else {
            sb.append("End-of-time");
          }
        } else if (!vcEqual) {
          sb.append(", No Corrections");
        }
        return sb.toString();
      }
      
      @Override
      protected void done() {
        try {
          List<Pair<String, UniqueId>> list = get();
          List<String> names = new ArrayList<>();
          List<UniqueId> uniqueIds = new ArrayList<>();
          // unpack - a bit icky, but I'd prefer to atomically swap out the list in case of multiple threads reading _names;
          for (Pair<String, UniqueId> pair : list) {
            names.add(pair.getFirst());
            uniqueIds.add(pair.getSecond());
          }
          synchronized (this) {
            _names = names;
            _uniqueIds = uniqueIds;
          }
          fireIntervalAdded(SnapshotMarketDataSpecificationVersionListModel.this, 0, _names.size() - 1);
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
  public synchronized int getSize() {
    return _names.size();
  }

  @Override
  public synchronized String getElementAt(int index) {
    return _names.get(index);
  }
  
  public synchronized UniqueId getUniqueIdAt(int index) {
    return _uniqueIds.get(index);
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
