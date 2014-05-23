/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph.ambiguity;

import java.io.PrintStream;
import java.util.Collection;

import com.opengamma.util.ArgumentChecker;

/**
 * Formats a {@link FullRequirementResolution} object onto a {@link PrintStream}.
 */
public class FullRequirementResolutionPrinter {

  private final PrintStream _out;

  public FullRequirementResolutionPrinter(final PrintStream out) {
    ArgumentChecker.notNull(out, "out");
    _out = out;
  }

  protected PrintStream out() {
    return _out;
  }

  public void print(final FullRequirementResolution resolution) {
    print("", resolution);
  }

  protected void print(String indent, final FullRequirementResolution resolution) {
    String state;
    if (resolution.isResolved()) {
      if (resolution.isAmbiguous()) {
        state = "\tAMBIGUOUS";
      } else if (resolution.isDeeplyAmbiguous()) {
        state = "\tDEEP-AMBIGUOUS";
      } else {
        state = "";
      }
    } else {
      state = "\tUNRESOLVED";
    }
    out().println(indent + resolution.getRequirement() + state);
    indent = indent + "\t";
    for (Collection<RequirementResolution> resolutions : resolution.getResolutions()) {
      if (resolutions.size() != 1) {
        out().println(indent + resolutions.size() + " ambiguous resolutions");
        state = "* ";
      } else {
        state = "  ";
      }
      for (RequirementResolution nested : resolutions) {
        print(indent + state, nested);
      }
    }
  }

  protected void print(String indent, final RequirementResolution resolution) {
    out().println(indent + resolution.getFunction() + " producing " + resolution.getSpecification());
    indent = indent + "\t";
    for (FullRequirementResolution input : resolution.getInputs()) {
      print(indent, input);
    }
  }

}
