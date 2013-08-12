/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.swing;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * Renderer for ViewEntry 
 */
public class ViewListCellRenderer extends DefaultListCellRenderer {
  private static final long serialVersionUID = 1L;

  @Override
  public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    if (value == null) {
      return super.getListCellRendererComponent(list, "< no views >", index, isSelected, cellHasFocus);
    } else {
      return super.getListCellRendererComponent(list, ((ViewEntry) value).getName(), index, isSelected, cellHasFocus);
    }
  }

}
