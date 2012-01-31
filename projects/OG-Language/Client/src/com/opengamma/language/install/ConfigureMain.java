/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.install;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;

import com.opengamma.util.ArgumentChecker;

/**
 * Configuration service for the Main connector
 */
public class ConfigureMain implements Runnable {

  /**
   * Callback for setting and querying properties.
   */
  public interface Callback {

    void setProperty(String property, String value);

    String getProperty(String property);

  }

  private final Callback _callback;

  public ConfigureMain(final Callback callback) {
    ArgumentChecker.notNull(callback, "callback");
    _callback = callback;
  }
  
  protected void setProperty(final String property, final String value) {
    _callback.setProperty(property, value);
  }

  protected String getProperty(final String property) {
    return _callback.getProperty(property);
  }

  @Override
  public void run() {
    final ConfigurationUrlDialog dialog = new ConfigurationUrlDialog();
    final String urlOriginal = getProperty("opengamma.configuration.url");
    if (urlOriginal != null) {
      dialog.setUrl(urlOriginal);
    }
    dialog.setVisible(true);
    final String urlNew = dialog.getUrl();
    if (urlNew != null) {
      if (!StringUtils.equals(urlOriginal, urlNew)) {
        setProperty("opengamma.configuration.url", urlNew);
        // TODO: temporary measure - the code in the service wrapper should manage the restart
        JOptionPane.showMessageDialog(null, "The OG-Language service needs to be restarted for the change to take effect.", "OpenGamma Language Integration Service", JOptionPane.INFORMATION_MESSAGE);
      }
    }
  }

}
