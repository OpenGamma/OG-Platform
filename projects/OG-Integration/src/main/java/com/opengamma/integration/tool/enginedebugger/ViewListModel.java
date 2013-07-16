/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;

/**
 * List model holding list of views - updates when underlying config master updates.
 */
public class ViewListModel extends AbstractListModel<ViewEntry> implements ComboBoxModel<ViewEntry> {
  /**
   * Version UID
   */
  private static final long serialVersionUID = 1L;
  private Object _viewsLock = new Object();
  private volatile List<ViewEntry> _views = new ArrayList<>();
  private ConfigMaster _configMaster;
  private ViewEntry _selectedItem;

  public ViewListModel(ConfigMaster configMaster) {
    _configMaster = configMaster;
    registerListener();
    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
      @Override
      protected Void doInBackground() throws Exception {
        pullItems();
        return null;
      }
    };
    worker.execute();
  }
  
  private void pullItems() {
    ConfigSearchRequest<ViewDefinition> searchReq = new ConfigSearchRequest<ViewDefinition>();
    searchReq.setType(ViewDefinition.class);
    ConfigSearchResult<ViewDefinition> search = _configMaster.search(searchReq);
    synchronized (_viewsLock) {
      _views.clear();
      for (ConfigDocument document : search.getDocuments()) {
        _views.add(ViewEntry.of(document.getUniqueId(), document.getName()));
      }
    }
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        fireContentsChanged(this, 0, getSize());
      }
    });
  }
  
  private void registerListener() {
    _configMaster.changeManager().addChangeListener(new ChangeListener() {
      @Override
      public void entityChanged(ChangeEvent event) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
          @Override
          protected Void doInBackground() throws Exception {
            pullItems();
            return null;
          }
        };
        worker.execute();
      }
    });
  }
  
  @Override
  public int getSize() {
    return _views.size();
  }

  @Override
  public ViewEntry getElementAt(int index) {
    return _views.get(index);
  }

  @Override
  public void setSelectedItem(Object anItem) {
    _selectedItem = (ViewEntry) anItem;
  }

  @Override
  public Object getSelectedItem() {
    return _selectedItem;
  }

}
