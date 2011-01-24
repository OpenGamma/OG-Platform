/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.master.msg;

/**
 * Visitor to {@link RemoteCalcNodeMessage} subclasses.
 */
public abstract class MasterChangeMessageVisitor {

  protected abstract void visitUnexpectedMessage(MasterChangeMessage message);

  protected void visitAddedMessage(Added message) {
    visitUnexpectedMessage(message);
  }

  protected void visitUpdatedMessage(Updated message) {
    visitUnexpectedMessage(message);
  }

  protected void visitRemovedMessage(Removed message) {
    visitUnexpectedMessage(message);
  }

  protected void visitCorrectedMessage(Corrected message) {
    visitUnexpectedMessage(message);
  }

}
