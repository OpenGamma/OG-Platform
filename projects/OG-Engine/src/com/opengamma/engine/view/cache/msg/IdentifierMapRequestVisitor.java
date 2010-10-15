/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.engine.view.cache.msg;

/**
 * Visitor to the {@link IdentifierMapRequest} message sub-classes.
 */
public abstract class IdentifierMapRequestVisitor {

  protected abstract void visitUnexpectedMessage(IdentifierMapRequest message);

}
