/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.install;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.opengamma.language.install.ConfigurationScanner.ConfigurationListener;

/**
 * Swing dialog for requesting a configuration URL from a user.
 */
public class ConfigurationUrlDialog extends JDialog {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private abstract static class DocumentAdapter implements DocumentListener {

    @Override
    public void insertUpdate(DocumentEvent e) {
      onChanged();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
      onChanged();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
      onChanged();
    }

    protected abstract void onChanged();

  }

  private final MultipleHostConfigurationScanner _scanner;
  // TODO: change the host name to a combo box containing all of the host names found
  private final JTextField _hostName = new JTextField();
  private final JTextField _portNumber = new JTextField();
  private final DefaultListModel _configurations = new DefaultListModel();
  private final JList _configurationList = new JList(_configurations);
  private final JTextField _url = new JTextField();
  private String _validUrl;
  private boolean _handlingEvent;

  public ConfigurationUrlDialog() {
    setTitle("Select OpenGamma installation");
    final ThreadPoolExecutor executor = new ThreadPoolExecutor(16, 16, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    executor.allowCoreThreadTimeOut(true);
    _scanner = new MultipleHostConfigurationScanner(executor);
    _scanner.addListener(new ConfigurationListener() {
      @Override
      public void foundConfigurations(final Set<Configuration> configurations, final boolean complete) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            updateAvailableConfigurations(configurations);
          }
        });
      }
    });
    // TODO: spawn a thread to scan for hosts and add them to the configuration scanner when they are found
    final Container inner = getContentPane();
    inner.setLayout(new BorderLayout());
    inner.add(createControls(), BorderLayout.CENTER);
    inner.add(createButtons(), BorderLayout.PAGE_END);
    setModalityType(ModalityType.DOCUMENT_MODAL);
    _configurations.addElement("");
    pack();
    final Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation((scr.width - getWidth()) >> 1, (scr.height - getHeight() >> 1));
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(final WindowEvent e) {
        onCancel();
      }
    });
  }

  private JPanel createLabelPanel(final String text) {
    final JLabel label = new JLabel(text);
    final JPanel panel = new JPanel();
    final FlowLayout layout = new FlowLayout();
    layout.setAlignment(FlowLayout.RIGHT);
    panel.setLayout(layout);
    panel.add(label);
    return panel;
  }

  private JPanel createControls() {
    final JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.weightx = 1;
    constraints.weighty = 1;
    constraints.gridwidth = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    panel.add(createLabelPanel("Host:"), constraints);
    constraints.gridwidth = 3;
    constraints.weightx = 3;
    _hostName.setColumns(16);
    _hostName.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void onChanged() {
        onHostNameChanged();
      }
    });
    panel.add(_hostName, constraints);
    constraints.gridwidth = 1;
    constraints.weightx = 1;
    panel.add(createLabelPanel("Port:"), constraints);
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.weightx = 2;
    _portNumber.setColumns(4);
    _portNumber.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void onChanged() {
        onPortNumberChanged();
      }
    });
    panel.add(_portNumber, constraints);
    constraints.weightx = 7;
    // TODO: event handler for _configurationList
    final JScrollPane list = new JScrollPane(_configurationList);
    list.setMinimumSize(new Dimension(_hostName.getMinimumSize().width, _hostName.getMinimumSize().height * 8));
    list.setPreferredSize(list.getMinimumSize());
    panel.add(list, constraints);
    constraints.gridwidth = 1;
    constraints.weightx = 1;
    panel.add(createLabelPanel("URL:"), constraints);
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    _url.setColumns(32);
    _url.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void onChanged() {
        onUrlChanged();
      }
    });
    panel.add(_url, constraints);
    return panel;
  }

  private JPanel createButtons() {
    final JButton ok = new JButton("Ok");
    ok.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        onOk();
      }
    });
    final JButton cancel = new JButton("Cancel");
    cancel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        onCancel();
      }
    });
    final JPanel panel = new JPanel();
    panel.add(ok);
    panel.add(cancel);
    return panel;
  }

  private boolean beginEvent() {
    if (_handlingEvent) {
      return false;
    }
    _handlingEvent = true;
    return true;
  }

  private void endEvent() {
    assert _handlingEvent == true;
    _handlingEvent = false;
  }

  private void updateAvailableConfigurations(final Collection<Configuration> configurations) {
    if (!beginEvent()) {
      return;
    }
    String host = _hostName.getText();
    if (host.length() == 0) {
      host = null;
    }
    final Integer port;
    if (_portNumber.getText().length() == 0) {
      port = null;
    } else {
      try {
        port = Integer.parseInt(_portNumber.getText());
      } catch (NumberFormatException e) {
        endEvent();
        return;
      }
    }
    _configurations.clear();
    int index = 0;
    for (Configuration configuration : configurations) {
      // TODO: add the host to the server name combo if not already there
      if (((host == null) || host.equals(configuration.getHost())) && ((port == null) || port.equals(configuration.getPort()))) {
        final StringBuilder sb = new StringBuilder(configuration.getDescription());
        if (host == null) {
          sb.append(" on ").append(configuration.getHost());
          if (port == null) {
            sb.append(':').append(configuration.getPort());
          }
        } else if (port == null) {
          sb.append(" on port ").append(configuration.getPort());
        }
        boolean select = false;
        if (_configurations.isEmpty()) {
          final String url = configuration.getURI().toString();
          if (_url.getText().length() == 0) {
            select = true;
            _validUrl = url;
            _url.setText(_validUrl);
          } else {
            if (_url.getText().equals(url)) {
              select = true;
              _validUrl = url;
            }
          }
        }
        _configurations.addElement(sb.toString());
        if (select) {
          _configurationList.setSelectedIndex(index);
        }
        index++;
      }
    }
    endEvent();
  }

  private void onHostNameChanged() {
    if (!beginEvent()) {
      return;
    }
    validateHostNameAndPortNumber();
    endEvent();
  }

  private void onPortNumberChanged() {
    if (!beginEvent()) {
      return;
    }
    validateHostNameAndPortNumber();
    endEvent();
  }

  private void validateHostNameAndPortNumber() {
    _configurations.clear();
    _url.setText("");
    _validUrl = null;
    String host = _hostName.getText();
    if (host.length() == 0) {
      host = null;
    }
    final Integer port;
    if (_portNumber.getText().length() == 0) {
      port = null;
    } else {
      try {
        port = Integer.parseInt(_portNumber.getText());
      } catch (NumberFormatException e) {
        return;
      }
    }
    final List<Configuration> configurations = new ArrayList<Configuration>(_scanner.getConfigurations());
    Collections.sort(configurations, Configuration.SORT_BY_DESCRIPTION);
    int index = 0;
    for (Configuration configuration : configurations) {
      if (((host == null) || host.equals(configuration.getHost())) && ((port == null) || port.equals(configuration.getPort()))) {
        final StringBuilder sb = new StringBuilder(configuration.getDescription());
        if (host == null) {
          sb.append(" on ").append(configuration.getHost());
          if (port == null) {
            sb.append(':').append(configuration.getPort());
          }
        } else if (port == null) {
          sb.append(" on port ").append(configuration.getPort());
        }
        boolean select = false;
        if (_configurations.isEmpty()) {
          _validUrl = configuration.getURI().toString();
          _url.setText(_validUrl);
        }
        _configurations.addElement(sb.toString());
        if (select) {
          _configurationList.setSelectedIndex(index);
        }
        index++;
      }
    }
    if (host != null) {
      if (_configurations.isEmpty()) {
        if (port != null) {
          _scanner.addHost(host, port);
        } else {
          _scanner.addHost(host);
        }
      }
    }
  }

  private void onUrlChanged() {
    if (!beginEvent()) {
      return;
    }
    _validUrl = null;
    _configurations.clear();
    validateUrl(_url.getText());
    endEvent();
  }

  private boolean validateUrl(final String url) {
    try {
      final URI uri = new URI(url);
      String host = uri.getHost();
      if (host != null) {
        if (host.length() > 0) {
          _hostName.setText(host);
        } else {
          host = null;
        }
      }
      final int port = uri.getPort();
      if (port > 0) {
        _portNumber.setText(Integer.toString(port));
      }
      if ((host != null) && (port > 0)) {
        String path = uri.getPath();
        if (path != null) {
          int slash = path.lastIndexOf('/', path.length() - 2);
          if (slash > 0) {
            path = path.substring(0, slash + 1);
          } else {
            path = null;
          }
        }
        final List<Configuration> configurations = new ArrayList<Configuration>(_scanner.getConfigurations());
        Collections.sort(configurations, Configuration.SORT_BY_DESCRIPTION);
        int index = 0;
        for (Configuration configuration : configurations) {
          if (host.equals(configuration.getHost()) && (port == configuration.getPort())) {
            final StringBuilder sb = new StringBuilder(configuration.getDescription());
            sb.append(" on ").append(configuration.getHost()).append(':').append(configuration.getPort());
            final String validUrl = configuration.getURI().toString();
            _configurations.addElement(sb.toString());
            if (url.equals(validUrl)) {
              _validUrl = url;
              _configurationList.setSelectedIndex(index);
            }
            index++;
          }
        }
        if (_validUrl != null) {
          if (path != null) {
            _scanner.addHost(host, port, path);
          } else {
            _scanner.addHost(host, port);
          }
        }
      }
      return true;
    } catch (URISyntaxException e) {
      return false;
    }
  }

  private void onOk() {
    if (_validUrl != null) {
      setVisible(false);
    } else {
      final String url = _url.getText().trim();
      final String prompt;
      if (url.length() == 0) {
        prompt = "A configuration URL should be entered; it will not be possible to start the system without one. Do you wish to continue?";
      } else {
        prompt = "The configuration URL cannot be verified; it may not be possible to start the system. Do you wish to continue?";
      }
      if (JOptionPane.showConfirmDialog(this, prompt, getTitle(), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
        _validUrl = url;
        setVisible(false);
      }
    }
  }

  private void onCancel() {
    _validUrl = null;
    setVisible(false);
  }

  public void setUrl(final String url) {
    _url.setText(url);
    _validUrl = validateUrl(url) ? url : null;
  }

  public String getUrl() {
    return _validUrl;
  }

  //CSOFF
  public static void main(final String[] args) {
    final ConfigurationUrlDialog dialog = new ConfigurationUrlDialog();
    // TODO: process the args to get the previous selection
    dialog.setVisible(true);
    final String url = dialog.getUrl();
    if (url == null) {
      System.exit(1);
    } else {
      System.out.println("URL = " + url);
      System.exit(0);
    }
  }
  //CSON

}
