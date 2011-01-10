/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;

/**
 * Returns a description of the server end point that a client may connect to. The content
 * of the message should include a clue about the transport, e.g. JMS or Socket, and then
 * any relevant parameters for a client to initiate a connection. Clients may implement
 * this interface and return connection details for the server they are connected to.
 */
public interface EndPointDescriptionProvider {

  FudgeFieldContainer getEndPointDescription(FudgeContext fudgeContext);

}
