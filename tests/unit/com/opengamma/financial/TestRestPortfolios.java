/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import org.apache.http.impl.client.DefaultHttpClient;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.financial.position.SearchPortfoliosResult;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;

/**
 * HttpClient wrapper with some additional helper functions for Fudge based JAX-RS calls.
 */
public class TestRestPortfolios extends RestClient {

  public static void main(String[] args) {
    new TestRestPortfolios();
  }

  public TestRestPortfolios() {
    super(FudgeContext.GLOBAL_DEFAULT, new DefaultHttpClient());
    RestTarget target = new RestTarget("http://localhost:8080/jax/portfolios");
    
    FudgeFieldContainer result = getMsg(target);
    SearchPortfoliosResult res = getFudgeContext().fromFudgeMsg(SearchPortfoliosResult.class, result);
    System.out.println(res);
    
//    FudgeMsgEnvelope env = this.getMsgEnvelope(target);
//    System.out.println(env);
  }

}
