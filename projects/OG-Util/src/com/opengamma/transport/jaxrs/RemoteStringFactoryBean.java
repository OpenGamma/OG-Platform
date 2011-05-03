/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;

import com.opengamma.util.SingletonFactoryBean;

/**
 * Spring factory bean for a string published as a Fudge message value
 */
public class RemoteStringFactoryBean extends SingletonFactoryBean<String> {

  // Note: this was inserted as a workaround for [DVI-135], and is unlikely to be necessary after that is resolved

  private String _uri;

  public String getUri() {
    return _uri;
  }

  public void setUri(String uri) {
    _uri = uri;
  }

  @Override
  protected String createObject() {
    final RestClient client = RestClient.getInstance(FudgeContext.GLOBAL_DEFAULT, null);
    final FudgeMsg msg = client.getMsg(new RestTarget(_uri));
    final String value = msg.getString("value");
    return value;
  }

}
