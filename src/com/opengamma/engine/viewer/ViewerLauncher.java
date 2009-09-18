/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.viewer;

import org.jdesktop.application.SingleFrameApplication;

/**
 * 
 *
 * @author jim
 */
public class ViewerLauncher extends SingleFrameApplication {
  @Override
  protected void startup() {
     //JTreeTable treeTable = new JTreeTable();
  }  
  public static void main(String[] args) {
    launch(ViewerLauncher.class, args);
  }

}
