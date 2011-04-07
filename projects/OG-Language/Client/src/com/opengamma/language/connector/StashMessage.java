/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.connector;

import org.fudgemsg.FudgeMsg;

/**
 * Access to a "stash" message to preserve context state across a Java stack restart.
 */
public interface StashMessage {

  FudgeMsg get();

  void put(FudgeMsg message);

}
