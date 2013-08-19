/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.snapshot;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;

/**
 * Snapshot copier visitor that stays quiet
 */
public class QuietSnapshotCopierVisitor implements SnapshotCopierVisitor {

  @Override
  public void info(String message, ManageablePosition position, ManageableSecurity[] securities) {
  }

  @Override
  public void info(String message) {
  }

  @Override
  public void error(String message) {
  }

}
