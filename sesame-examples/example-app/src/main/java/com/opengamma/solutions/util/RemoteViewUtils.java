/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions.util;

import java.util.Iterator;
import java.util.Map;

import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCurveSensitivities;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

/**
 * Utility class for remote views
 */
public final class RemoteViewUtils {

  private RemoteViewUtils() { /* private constructor */ }

  public static void outputMultipleCurrencyAmount(String label, Result result) {

    if (result.isSuccess()) {
      MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
      System.out.println(label + ": PV "  + mca.toString());
    } else {
      System.out.println(label + ": Error - " + result.getFailureMessage());
    }
  }

  public static void outputBucketedCurveSensitivities(String label, Result result) {

    if (result.isSuccess()) {
      System.out.println(label  + ": Bucketed PV01");

      BucketedCurveSensitivities bcs = (BucketedCurveSensitivities) result.getValue();
      Map sensitivities = bcs.getSensitivities();
      Iterator entryIterator = sensitivities.entrySet().iterator();
      while (entryIterator.hasNext()) {
        Map.Entry entry = (Map.Entry) entryIterator.next();
        Pair pair = (Pair) entry.getKey();
        DoubleLabelledMatrix1D matrix = (DoubleLabelledMatrix1D) sensitivities.get(pair);
        System.out.println("  " + pair.getFirst().toString() + ": " + pair.getSecond().toString());
        for (int i=0; i < matrix.getLabels().length; i++) {
          System.out.println("    " + matrix.getLabels()[i].toString() + ": " + matrix.getValues()[i]);
        }
      }
    } else {
      System.out.println(label + ": Error - " + result.getFailureMessage());
    }

  }

}