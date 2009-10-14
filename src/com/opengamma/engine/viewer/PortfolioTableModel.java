/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.viewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.depgraph.DependencyGraphModel;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.engine.view.ComputationResultListener;
import com.opengamma.engine.view.MapViewComputationCache;
import com.opengamma.engine.view.ViewComputationCache;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.util.KeyValuePair;

class PortfolioTableModel extends AbstractTableModel implements ComputationResultListener {
  private List<Position> _positions = new ArrayList<Position>();
  private Map<Position, Map<String, AnalyticValue<?>>> _resultsMap = new HashMap<Position, Map<String, AnalyticValue<?>>>();
  private Map<Position, Object[]> _table = new HashMap<Position, Object[]>();
  
  private String[] _columnHeadings = new String[] {};
  private RequiredColumnsRenderVisitor _requiredColumnsRenderVisitor = new RequiredColumnsRenderVisitor();
  // REVIEW: jim 12-Oct-2009 -- we need to review all these, but esp secMaster.
  private ViewComputationCache _viewComputationCache;
  private DependencyGraphModel _dependencyGraphModel;
  private SecurityMaster _secMaster;
  @Override
  public synchronized String getColumnName(int column) {
    if (column == 0) {
      return "Trade";
    } else {
      String defn = _columnHeadings[column-1];
      return defn;
    }
  }
  @Override
  public synchronized int getColumnCount() {
    return _columnHeadings.length + 1;
  }

  @Override
  public synchronized int getRowCount() {
    return _positions.size();
  }

  @Override
  public synchronized Object getValueAt(int rowIndex, int columnIndex) {
    Position position = _positions.get(rowIndex);
    if (columnIndex == 0) {
      return position.getSecurityKey().getIdentifiers().iterator().next().getValue() + " @ "+position.getQuantity();
    } else {
      String columnNameXpath = _columnHeadings[columnIndex-1];
      String[] xpath = columnNameXpath.split("/");
      String type = xpath[0];
      String fieldName = xpath[1];
      AnalyticValue<?> value = _resultsMap.get(position).get(type);
      if (value != null) {
        if (value instanceof Renderable) {
          Renderable renderable = (Renderable)value;
          Object renderedObject = renderable.accept(new FieldXPathRenderVisitor(fieldName));
          return renderedObject;
        }
      }
      return "N/A";
    }
    
  }
  
  public synchronized Map.Entry<Position, Map<String, AnalyticValue<?>>> getRow(int rowIndex) {
    Position position = _positions.get(rowIndex);
    Map<String, AnalyticValue<?>> map = _resultsMap.get(position);
    return new KeyValuePair<Position, Map<String, AnalyticValue<?>>>(position, map);
  }

  @Override
  public void computationResultAvailable(
      ViewComputationResultModel resultModel) {
    boolean allDataChanged = false;
    synchronized (this) {
      _positions.clear();
      _positions.addAll(resultModel.getPositions());
      _resultsMap.clear();
      _viewComputationCache = resultModel.getComputationCache();
      MapViewComputationCache cache = (MapViewComputationCache)_viewComputationCache;
//      if (cache != null) {
//        cache.dump();
//      }
      _dependencyGraphModel = resultModel.getDependencyGraphModel();
      _secMaster = resultModel.getSecurityMaster();
      Map<String, List<String>> valueColumnsListMap = new HashMap<String, List<String>>();  
      for (Position position : _positions) {
        Map<AnalyticValueDefinition<?>, AnalyticValue<?>> values = resultModel.getValues(position);
        Map<String, AnalyticValue<?>> simpleValues = new HashMap<String, AnalyticValue<?>>();
        for (Entry<AnalyticValueDefinition<?>, AnalyticValue<?>> entry : values.entrySet()) {
          AnalyticValueDefinition<?> defn = entry.getKey();
          AnalyticValue<?> value = entry.getValue();
          String type = (String) defn.getValue("TYPE");
          if (value instanceof Renderable) {
            Renderable renderable = (Renderable) value;
            List<String> columnNames = renderable.accept(_requiredColumnsRenderVisitor);
            if (columnNames != null) {
              if (!valueColumnsListMap.containsKey(type)) {
                valueColumnsListMap.put(type, columnNames);
              } else {
                if (columnNames.size() > valueColumnsListMap.get(type).size()) {
                  valueColumnsListMap.put(type, columnNames);
                }
              }
            }
          }
          simpleValues.put(type, entry.getValue());
        }
        _resultsMap.put(position, simpleValues);
      }
      int lengthB4 = _columnHeadings.length;
      List<String> xpathColumns = new ArrayList<String>();
      for (String type : valueColumnsListMap.keySet()) {
        for (String field : valueColumnsListMap.get(type)) {
          xpathColumns.add(type +"/"+field);
        }
      }
      _columnHeadings = xpathColumns.toArray(new String[] {});
      if (_columnHeadings.length != lengthB4) {
        allDataChanged = true;
        
      }
    }
    if (allDataChanged) {
      deferredFireTableStructureChanged();
    } else {
      deferredFireTableRowsUpdated();
    }
  }
  
  @SuppressWarnings("unused")
  private void deferredFireTableDataChanged() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        fireTableDataChanged();
      }
    });    
  }

  private void deferredFireTableRowsUpdated() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        fireTableRowsUpdated(0, getRowCount());
      }
    });    
  }

  private void deferredFireTableStructureChanged() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        fireTableStructureChanged();
      }
    });    
  }
  
  public synchronized ViewComputationCache getViewComputationCache() {
    return _viewComputationCache;
  }
  
  public synchronized DependencyGraphModel getDependencyGraphModel() {
    return _dependencyGraphModel;
  }
  
  public synchronized SecurityMaster getSecurityMaster() {
    return _secMaster;
  }
}