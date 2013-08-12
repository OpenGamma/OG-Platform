/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.swing;

import java.util.Comparator;

/**
 * Simple comparator for ViewEntry
 * @author jim
 */
public final class ViewEntryComparator implements Comparator<ViewEntry> {

  private static final ViewEntryComparator INSTANCE = new ViewEntryComparator();
  
  public static ViewEntryComparator getInstance() {
    return INSTANCE;
  }
  
  private ViewEntryComparator() {
  }

  @Override
  public int compare(ViewEntry o1, ViewEntry o2) {
    return o1.getName().compareTo(o2.getName());
  }

}
