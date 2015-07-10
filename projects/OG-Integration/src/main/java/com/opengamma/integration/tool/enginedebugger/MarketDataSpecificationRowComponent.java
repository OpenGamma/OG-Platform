package com.opengamma.integration.tool.enginedebugger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.provider.livedata.LiveDataMetaDataProvider;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.miginfocom.swing.MigLayout;

public class MarketDataSpecificationRowComponent extends JPanel {

  private static final String HEAVY_MINUS = "-";
  private static final String HEAVY_PLUS = "+";
  private static final String DOWN_ARROW = "\u2193";
  private static final String UP_ARROW = "\u2191";
  private static final Dimension BUTTON_SIZE = new Dimension(28, 28);

  /** enum representing button action events */
  public enum Action {
    /** Move row up */
    MOVE_UP,
    /** Move row down */
    MOVE_DOWN,
    /** Add new row below */
    ADD,
    /** Remove this row */
    REMOVE
  }
  private Set<ActionListener> _listeners = new LinkedHashSet<ActionListener>();
  private MarketDataSpecificationComponent _marketDataSpecificationComponent;
  private List<LiveDataMetaDataProvider> _liveDataMetaDataProviders;
  private ConfigMaster _configMaster;
  private MarketDataSnapshotMaster _snapshotMaster;
  private JButton _upButton;
  private JButton _downButton;
  private JButton _addButton;
  private JButton _removeButton;

  public MarketDataSpecificationRowComponent(List<LiveDataMetaDataProvider> liveDataMetaDataProviders, ConfigMaster configMaster, MarketDataSnapshotMaster snapshotMaster) {
    super(new MigLayout("insets 0"));
    _liveDataMetaDataProviders = liveDataMetaDataProviders;
    _configMaster = configMaster;
    _snapshotMaster = snapshotMaster;
    addComponents();
  }
  
  private void addComponents() {
    _marketDataSpecificationComponent = new MarketDataSpecificationComponent(_liveDataMetaDataProviders, _configMaster, _snapshotMaster);
    add(_marketDataSpecificationComponent, "align left");
    final JLabel statusLabel = new JLabel();
    cross(statusLabel);
    _marketDataSpecificationComponent.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        MarketDataSpecificationComponent component = (MarketDataSpecificationComponent) e.getSource();
        if (component.getCurrentState() != null) {
          tick(statusLabel);
        } else {
          cross(statusLabel);
        }
      }
    });
    add(statusLabel, "align right");
    _upButton = new JButton(UP_ARROW); // up arrow
    _upButton.setMaximumSize(BUTTON_SIZE);
    _upButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fireMoveUp();
      }
    });
    _downButton = new JButton(DOWN_ARROW); // down arrow
    _downButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fireMoveDown();
      }
    });
    _downButton.setMaximumSize(BUTTON_SIZE);
    _addButton = new JButton(HEAVY_PLUS); // heavy plus sign
    _addButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fireAdd();
      }
    });
    _addButton.setMaximumSize(BUTTON_SIZE);
    _removeButton = new JButton(HEAVY_MINUS); // heavy minus sign
    _removeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fireRemove();
      }
    });
    _removeButton.setMaximumSize(BUTTON_SIZE);
    add(_upButton, "align right");
    add(_downButton, "align right");
    add(_addButton, "align right");
    add(_removeButton, "align right");
  }
  
  public void checkButtons(int index, int numRows) {
    // can't move bottom row down
    if (index == numRows - 1) {
      _downButton.setEnabled(false);
    } else {
      _downButton.setEnabled(true);
    }
    // can't move top row up
    if (index == 0) {
      _upButton.setEnabled(false);
    } else {
      _upButton.setEnabled(true);
    }
    // can't remove last row
    if (numRows == 1) {
      _removeButton.setEnabled(false);
    } else {
      _removeButton.setEnabled(true);
    }
  }
  
  private void tick(JLabel label) {
    label.setText("\u2713");
    label.setForeground(Color.GREEN);
  }
  
  private void cross(JLabel label) {
    label.setText("\u2717");
    label.setForeground(Color.RED);
  }
  
  private void fireMoveUp() {
    ActionEvent e = new ActionEvent(this, Action.MOVE_UP.ordinal(), Action.MOVE_UP.name());
    fireAction(e);
  }
  
  private void fireMoveDown() {
    ActionEvent e = new ActionEvent(this, Action.MOVE_DOWN.ordinal(), Action.MOVE_DOWN.name());
    fireAction(e);
  }
  
  private void fireAdd() {
    ActionEvent e = new ActionEvent(this, Action.ADD.ordinal(), Action.ADD.name());
    fireAction(e);
  }
  
  private void fireRemove() {
    ActionEvent e = new ActionEvent(this, Action.REMOVE.ordinal(), Action.REMOVE.name());
    fireAction(e);
  }
  
  private void fireAction(ActionEvent actionEvent) {
    for (ActionListener listener : _listeners) {
      listener.actionPerformed(actionEvent);  
    }    
  }
  
  public void addChangeListener(ChangeListener listener) {
    _marketDataSpecificationComponent.addChangeListener(listener);
  }
  
  public void removeChangeListener(ChangeListener listener) {
    _marketDataSpecificationComponent.removeChangeListener(listener);
  }
  
  public MarketDataSpecification getCurrentState() {
    return _marketDataSpecificationComponent.getCurrentState();
  }
  
  public void addActionListener(ActionListener listener) {
    _listeners.add(listener);
  }
  
  public void removeActionListener(ActionListener listener) {
    _listeners.remove(listener);
  }
}
