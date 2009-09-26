/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.viewer;

import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;

import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.position.Position;

@SuppressWarnings("unchecked") 
public class PortfolioSelectionListenerAndTableModel extends AbstractTableModel implements ListSelectionListener, TableModelListener {
  private final String[] _columnNames = new String[] { "Definition", "Value" };
  private JXTable _parentTable;
  private Position _position = null;
  private Map.Entry<AnalyticValueDefinition<?>, AnalyticValue<?>>[] _rows = new Map.Entry[0];
  public PortfolioSelectionListenerAndTableModel(JXTable parentTable) {
    _parentTable = parentTable;
    _parentTable.getSelectionModel().addListSelectionListener(this);
    TableModel model = _parentTable.getModel();
    model.addTableModelListener(this);
  }
  
  private void readChanges(ListSelectionModel lsm) {
    boolean allDataChanged = false; // flag to refresh whole table or just all rows
    if (lsm.isSelectionEmpty()) {
      synchronized (this) {
        _rows = new Map.Entry[0];
        setPosition(null);
      }
    } else {
      int selectedRow = lsm.getMinSelectionIndex();
      RowSorter<? extends TableModel> rowSorter = _parentTable.getRowSorter();
      int modelRow;
      if (rowSorter != null) {
        modelRow = rowSorter.convertRowIndexToModel(selectedRow);
      }  else {
        modelRow = selectedRow;
      }
      PortfolioTableModel model = (PortfolioTableModel) _parentTable.getModel();
      Entry<Position, Map<AnalyticValueDefinition<?>, AnalyticValue<?>>> row = model.getRow(modelRow);
      synchronized (this) {
        int previousRows = _rows.length;
        _rows = row.getValue().entrySet().toArray(_rows);
        if (_rows.length != previousRows) {
          allDataChanged = true;
        }
        setPosition(row.getKey());
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
  @Override
  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) return;
    
    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
    readChanges(lsm);
  }
  
  public synchronized Map.Entry<AnalyticValueDefinition<?>, AnalyticValue<?>> getRow(final int row) {
    if (row > 0) {
      return _rows[row-1];
    } else {
      return null;
    }
  }
  
  
  @Override
  public int getColumnCount() {
    return 2;
  }
  @Override
  public synchronized int getRowCount() {
    return _rows.length+1;
  }
  @Override
  public synchronized Object getValueAt(int rowIndex, int columnIndex) {
    if (rowIndex == 0) {
      if (columnIndex == 0) {
        return "Position"; 
      } else { 
        return getPosition(); 
      }
    }
    if (columnIndex == 0) {
      return _rows[rowIndex-1].getKey();
    } else {
      return _rows[rowIndex-1].getValue();
    }
  }
  
  public String getColumnName(int column) {
    return _columnNames[column];
  }

  @Override
  public void tableChanged(TableModelEvent e) {
    ListSelectionModel lsm = _parentTable.getSelectionModel();
    RowSorter<? extends TableModel> rowSorter = _parentTable.getRowSorter();
    int row;
    if (rowSorter != null) {
      row = rowSorter.convertRowIndexToModel(lsm.getMinSelectionIndex());
    } else {
      row = lsm.getMinSelectionIndex();
    }
    // this logic isn't actually necessary at the moment, but might be if we get more efficient table updating.
    if (e.getFirstRow() <= row && e.getLastRow() >= row) {
      // the selected row has changed under us, regrab it.
      readChanges(lsm);
    }
  }

  /**
   * @param position the position to set
   */
  private void setPosition(Position position) {
    _position = position;
  }

  /**
   * @return the position
   */
  public Position getPosition() {
    return _position;
  }
}