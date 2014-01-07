/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.snapshot.copier;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;

/**
 * Abstract visitor for snapshot copier
 */
public interface SnapshotCopierVisitor {

  void info(String message, ManageablePosition position, ManageableSecurity[] securities);
  
  void info(String message);
  
  void error(String message);
}
