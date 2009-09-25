/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.viewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.view.ComputationResultListener;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.util.KeyValuePair;

class PortfolioTableModel extends AbstractTableModel implements ComputationResultListener {
  private List<Position> _positions = new ArrayList<Position>();
  private Map<Position, Map<AnalyticValueDefinition<?>, AnalyticValue<?>>> _resultsMap = new HashMap<Position, Map<AnalyticValueDefinition<?>, AnalyticValue<?>>>();
  private Set<AnalyticValueDefinition<?>> _valueDefinitionsSet = new HashSet<AnalyticValueDefinition<?>>();
  private AnalyticValueDefinition<?>[] _valueDefinitionsArray = new AnalyticValueDefinition<?>[] {};
  
  @Override
  public synchronized String getColumnName(int column) {
    if (column == 0) {
      return "Trade";
    } else {
      AnalyticValueDefinition<?> defn = _valueDefinitionsArray[column-1];
      return defn.getValue("TYPE").toString();
    }
  }
  @Override
  public synchronized int getColumnCount() {
    return _valueDefinitionsArray.length + 1;
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
      AnalyticValueDefinition<?> defn = _valueDefinitionsArray[columnIndex-1];
      
      if (_resultsMap.containsKey(position) && _resultsMap.get(position) != null && _resultsMap.get(position).containsKey(defn)) {
        Object o = _resultsMap.get(position).get(defn).getValue();
        if (o instanceof DiscountCurve) {
          DiscountCurve curve = (DiscountCurve)o;
          return curve.getData().toString();
        } else {
          return o.toString();
        }
      } else {
        return "N/A";
      }
    }
  }
  
  public synchronized Map.Entry<Position, Map<AnalyticValueDefinition<?>, AnalyticValue<?>>> getRow(int rowIndex) {
    Position position = _positions.get(rowIndex);
    Map<AnalyticValueDefinition<?>, AnalyticValue<?>> map = _resultsMap.get(position);
    return new KeyValuePair<Position, Map<AnalyticValueDefinition<?>, AnalyticValue<?>>>(position, map);
  }

  @Override
  public void computationResultAvailable(
      ViewComputationResultModel resultModel) {
    System.err.println("Tick!");
    boolean allDataChanged = false;
    synchronized (this) {
      _positions.clear();
      _positions.addAll(resultModel.getPositions());
      _resultsMap.clear();
      
      for (Position position : _positions) {
        Map<AnalyticValueDefinition<?>, AnalyticValue<?>> values = resultModel.getValues(position);
        _valueDefinitionsSet.addAll(values.keySet());
        _resultsMap.put(position, values);
      }
      int lengthB4 = _valueDefinitionsArray.length;
      _valueDefinitionsArray = _valueDefinitionsSet.toArray(new AnalyticValueDefinition<?>[] {});
      if (_valueDefinitionsArray.length != lengthB4) {
        allDataChanged = true;
      }
    }
    if (allDataChanged) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          fireTableDataChanged();
        }
      });
    } else {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          fireTableRowsUpdated(0, getRowCount());
        }
      });
    }
  }
}