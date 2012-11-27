/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.debug;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.livedata.AbstractLiveDataConnector;
import com.opengamma.language.livedata.MetaLiveData;
import com.opengamma.language.livedata.PublishedLiveData;

/**
 * Trivial live data for debugging. Returns random numbers.
 */
public class DebugLiveDataRandom implements PublishedLiveData {

  @Override
  public MetaLiveData getMetaLiveData() {
    final List<MetaParameter> args = Collections.emptyList();
    return new MetaLiveData(Categories.DEBUG, "DebugLiveDataRandom", args, new AbstractLiveDataConnector(args) {

      @Override
      protected void connectImpl(final SessionContext context, final Object[] parameters, final AbstractConnection connection) {
        final Random random = new Random();
        final Timer timer = new Timer();
        connection.setCancelHandler(new Runnable() {
          @Override
          public void run() {
            timer.cancel();
          }
        });
        timer.scheduleAtFixedRate(new TimerTask() {
          @Override
          public void run() {
            connection.setValue(random.nextInt());
          }
        }, 1000, 1000);
      }

    });
  }

}
