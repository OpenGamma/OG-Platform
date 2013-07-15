/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.engine.calcnode.msg;

/**
 * Visitor to {@link RemoteCalcNodeMessage} subclasses.
 */
public abstract class RemoteCalcNodeMessageVisitor {

  protected abstract void visitUnexpectedMessage(RemoteCalcNodeMessage message);

  protected void visitCancelMessage(Cancel message) {
    visitUnexpectedMessage(message);
  }

  protected void visitExecuteMessage(Execute message) {
    visitUnexpectedMessage(message);
  }

  protected void visitFailureMessage(Failure message) {
    visitUnexpectedMessage(message);
  }

  protected void visitInitMessage(Init message) {
    visitUnexpectedMessage(message);
  }

  protected void visitInvocationsMessage(Invocations message) {
    visitUnexpectedMessage(message);
  }

  protected void visitIsAliveMessage(IsAlive message) {
    visitUnexpectedMessage(message);
  }

  protected void visitReadyMessage(Ready message) {
    visitUnexpectedMessage(message);
  }

  protected void visitResultMessage(Result message) {
    visitUnexpectedMessage(message);
  }

  protected void visitScalingMessage(Scaling message) {
    visitUnexpectedMessage(message);
  }

}
