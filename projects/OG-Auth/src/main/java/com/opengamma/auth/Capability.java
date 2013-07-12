package com.opengamma.auth;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
public abstract class Capability<T> {
  final protected Collection<SignedMessage<T>> _messages = newArrayList();

  public Collection<SignedMessage<T>> getMessages() {
    return _messages;
  }
}
