/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Renderer for ViewEntry 
 */
public class ViewListCellRenderer implements ListCellRenderer<ViewEntry> {
  @Override
  public Component getListCellRendererComponent(JList<? extends ViewEntry> list, ViewEntry value, int index, boolean isSelected, boolean cellHasFocus) {
    return value == null ? new JLabel("<no view selected>") : new JLabel(value.getName());
  }

}
