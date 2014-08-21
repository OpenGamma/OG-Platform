/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.threeten.bp.Instant;

import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;

/**
 * Utilities for managing masters.
 * <p>
 * This is a thread-safe static utility class.
 */
public class MasterUtils {

  public static <D extends AbstractDocument> List<D> adjustVersionInstants(Instant now, Instant from, Instant to, List<D> documents) {
    for (D document : documents) {
      Instant fromInstant = document.getVersionFromInstant();
      if (fromInstant == null) {
        document.setVersionFromInstant(from);
      }
    }
    List<D> copy = newArrayList(documents);
    Collections.sort(copy, new Comparator<D>() {
      @Override
      public int compare(D a, D b) {
        Instant fromA = a.getVersionFromInstant();
        Instant fromB = b.getVersionFromInstant();
        return fromA.compareTo(fromB);
      }
    });
    final Instant latestDocumentVersionTo = copy.get(copy.size() - 1).getVersionToInstant();
    D prevDocument = null;
    for (D document : copy) {
      document.setVersionToInstant(latestDocumentVersionTo == null ? to : latestDocumentVersionTo);
      if (prevDocument != null) {
        prevDocument.setVersionToInstant(document.getVersionFromInstant());
      }
      prevDocument = document;
      document.setCorrectionFromInstant(now);
      document.setCorrectionToInstant(null);
    }
    return copy;
  }

  public static <D extends AbstractDocument> boolean checkUniqueVersionsFrom(List<D> documents) {
    Set<Instant> instants = new HashSet<Instant>();
    for (D document : documents) {
      instants.add(document.getVersionFromInstant());
    }
    return instants.size() == documents.size();
  }

  public static <D extends AbstractDocument> boolean checkVersionInstantsWithinRange(Instant missing, Instant from, Instant to, List<D> documents, boolean equalFrom) {
    if (!documents.isEmpty()) {
      SortedSet<Instant> instants = new TreeSet<Instant>();
      for (D document : documents) {
        Instant fromInstant = document.getVersionFromInstant();
        if (fromInstant == null) {
          instants.add(missing);
        } else {
          instants.add(document.getVersionFromInstant());
        }
      }
      Instant minFromVersion = instants.first();
      Instant maxFromVersion = instants.last();
      return
        ((equalFrom && minFromVersion.equals(from)) || (!equalFrom && !minFromVersion.isBefore(from)))
          &&
          (to == null || !maxFromVersion.isAfter(to));
    } else {
      return true;
    }
  }

  public static <D extends UniqueIdentifiable> List<UniqueId> mapToUniqueIDs(List<D> documents) {
    List<UniqueId> result = new ArrayList<UniqueId>();
    for (D doc : documents) {
      result.add(doc.getUniqueId());
    }
    return result;
  }

}
