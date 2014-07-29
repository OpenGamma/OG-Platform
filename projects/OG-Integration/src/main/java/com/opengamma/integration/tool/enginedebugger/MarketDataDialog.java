package com.opengamma.integration.tool.enginedebugger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.integration.tool.enginedebugger.MarketDataSpecificationRowComponent.Action;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.provider.livedata.LiveDataMetaDataProvider;

/**
 * 
 */
public class MarketDataDialog extends JDialog {
  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataDialog.class);
  private List<LiveDataMetaDataProvider> _liveDataMetaDataProvider;
  private ConfigMaster _configMaster;
  private MarketDataSnapshotMaster _snapshotMaster;
  private JButton _cancelButton = new JButton("Cancel");
  private JButton _okayButton = new JButton("OK");
  
  private Map<MarketDataSpecificationRowComponent, Boolean> _validState = new HashMap<>();
  private List<MarketDataSpecificationRowComponent> _components = new ArrayList<>();
  private volatile boolean _cancelled;

  public MarketDataDialog(List<LiveDataMetaDataProvider> liveDataMetaDataProvider, ConfigMaster configMaster, MarketDataSnapshotMaster snapshotMaster) {
    super();
    setModalityType(ModalityType.APPLICATION_MODAL);
    _liveDataMetaDataProvider = liveDataMetaDataProvider;
    _configMaster = configMaster;
    _snapshotMaster = snapshotMaster;
    buildForm();
  }
  
  public void buildForm() {
    MigLayout layout = new MigLayout();
    setLayout(layout);

    addRow(0);

    _okayButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        dispose();
      }
    });
    _cancelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        _cancelled = true;
        setVisible(false);
        dispose();
      }
    });
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    JPanel jPanel = new JPanel(new MigLayout("inset 15 0 -5 -5"));
    jPanel.add(_okayButton, "right");
    jPanel.add(_cancelButton, "right");
    add(jPanel, "span 2, right, wrap");
    pack();    
  }
  
  private boolean validState() {
    boolean activate = true;
    for (boolean good : _validState.values()) {
      activate = activate & good;
    }
    return activate && !_cancelled;
  }
  
  private void checkOkayCancel() {
    _okayButton.setEnabled(validState());
  }
  
  private void addRow(final int index) {
    // + 1 because we're about to add it.
    final MarketDataSpecificationRowComponent rowComponent = new MarketDataSpecificationRowComponent(_liveDataMetaDataProvider, _configMaster, _snapshotMaster);
    // listen for changes in component's validity and record them in map
    final ChangeListener changeListener = new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        MarketDataSpecificationComponent component = (MarketDataSpecificationComponent) e.getSource();
        _validState.put(rowComponent, component.getCurrentState() != null);
        checkOkayCancel();
      }
    };
    _validState.put(rowComponent, false); 
    rowComponent.addChangeListener(changeListener);
    rowComponent.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Action command = MarketDataSpecificationRowComponent.Action.valueOf(e.getActionCommand());
        MarketDataSpecificationRowComponent rowComponent = (MarketDataSpecificationRowComponent) e.getSource();
        int index = _components.indexOf(rowComponent);
        switch (command) {
          case MOVE_UP:
            remove(rowComponent);
            add(rowComponent, "span 2, wrap", index - 1);
            _components.remove(rowComponent);
            _components.add(index - 1, rowComponent);
            break;
          case MOVE_DOWN:
            remove(rowComponent);
            add(rowComponent, "span 2, wrap", index + 1);
            _components.remove(rowComponent);
            _components.add(index + 1, rowComponent);
            break;
          case ADD:
            addRow(index + 1);
            break;
          case REMOVE:
            remove(rowComponent);
            rowComponent.removeActionListener(this);
            rowComponent.removeChangeListener(changeListener);
            _validState.remove(rowComponent);
            _components.remove(rowComponent);
            break;
        }
        checkButtons();
        pack();
        for (int i = 0; i < _components.size(); i++) {
          MarketDataSpecificationRowComponent marketDataSpecificationRowComponent = _components.get(i);
          if (marketDataSpecificationRowComponent == null) {
            s_logger.error("{} was null", i);
          } else {
            s_logger.error("{} was {}", i, marketDataSpecificationRowComponent.getCurrentState());
          }
        }
      }
    });
    _components.add(index, rowComponent);
    checkButtons();
    checkOkayCancel();
    add(rowComponent, "span 2, wrap", index);
  }
  
  private void checkButtons() {
    int i = 0;
    for (MarketDataSpecificationRowComponent component : _components) {
      component.checkButtons(i++, _components.size());
    }
  }
  
  public List<MarketDataSpecification> showDialog() {
    setVisible(true);
    if (validState()) {
      List<MarketDataSpecification> specs = new ArrayList<>();
      for (MarketDataSpecificationRowComponent component : _components) {
        specs.add(component.getCurrentState());
      }
      return specs;
    } else {
      return null;
    }
  }

  
}
