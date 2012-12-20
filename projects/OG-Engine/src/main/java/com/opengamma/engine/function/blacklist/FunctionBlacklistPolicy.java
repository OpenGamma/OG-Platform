/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.Set;

import com.opengamma.id.UniqueIdentifiable;

/**
 * Defines a blacklisting policy. A policy defines the type of rules that should be constructed and their recommended time-to-live. A policy is typically returned from a
 * {@link FunctionBlacklistPolicySource} and used to update a blacklist using a {@link FunctionBlacklistMaintainer}. A policy entry defines the type of rule that should be constructed and the
 * activation period of that rule (time to live). A typical policy will have the most specific entries having a long activation period and the least specific having the shortest. For example, after a
 * failure of function X applied to Y then all invocations of X might be blacklisted for the next 10 minutes but X applied specifically to Y will remain blacklisted for the next hour.
 */
public interface FunctionBlacklistPolicy extends UniqueIdentifiable {

  // TODO: no support currently here for partial inputs or outputs as nothing else in the package supports them

  /**
   * An entry within a policy describing how to construct and use a blacklisting rule after a failure has been detected.
   */
  public static final class Entry {

    /**
     * An entry that produces rules which will match everything. This can be used, for example, to suppress all behaviors for a brief period.
     */
    public static final Entry WILDCARD = new Entry();

    /**
     * An entry that produces rules which will match only the function identifier. This can be used, for example, to suppress any execution of a generally flawed function implementation.
     */
    public static final Entry FUNCTION = WILDCARD.matchFunctionIdentifier();

    /**
     * An entry that produces rules which will match a function identifier when used in a given form. This can be used, for example, to suppress any execution of a function that is flawed under
     * certain calculation configurations that include erroneous parameters.
     */
    public static final Entry PARAMETERIZED_FUNCTION = FUNCTION.matchFunctionParameters();

    /**
     * An entry that produces rules which match a node in a dependency graph based on the computation target and parameterized function. This can be used, for example, to suppress any execution of a
     * function that is only flawed when operating on certain targets.
     */
    public static final Entry PARTIAL_NODE = PARAMETERIZED_FUNCTION.matchTarget();

    /**
     * An entry that produces while which match a node in a dependency graph based on the computation target, parameterized function and inputs. This can be used, for example, in preference of
     * {@link #EXACT_NODE} to produce rules which will match during graph construction (as well as execution).
     */
    public static final Entry BUILD_NODE = PARTIAL_NODE.matchInputs();

    /**
     * An entry that produces rules which match a node in a dependency graph based on the exact shape of the graph. This can be used, for example, to suppress any execution of a function that is only
     * flawed when operating on certain targets and asked to produce certain outputs from certain inputs. This is best used for execution blacklists - graph construction may not have the full set of
     * output specifications at the point at which the blacklist is checked.
     */
    public static final Entry EXECUTION_NODE = BUILD_NODE.matchInputs().matchOutputs();

    private static final int FUNCTION_IDENTIFIER = 0x01;
    private static final int FUNCTION_PARAMETERS = 0x02;
    private static final int TARGET = 0x04;
    private static final int INPUTS = 0x08;
    private static final int OUTPUTS = 0x10;

    private final int _what;
    private final Integer _ttl;

    private Entry() {
      _what = 0;
      _ttl = null;
    }

    private Entry(final int what, final Integer ttl) {
      _what = what;
      _ttl = ttl;
    }

    private Entry match(final int mask) {
      return new Entry(_what | mask, _ttl);
    }

    private Entry ignore(final int mask) {
      return new Entry(_what & ~mask, _ttl);
    }

    private boolean is(final int mask) {
      return (_what & mask) != 0;
    }

    /**
     * Returns a policy entry that will produce rules which include the function identifier.
     * 
     * @return the new entry
     */
    public Entry matchFunctionIdentifier() {
      return match(FUNCTION_IDENTIFIER);
    }

    /**
     * Returns a policy entry that will produce rules which do not include the function identifier.
     * 
     * @return the new entry
     */
    public Entry ignoreFunctionIdentifier() {
      return ignore(FUNCTION_IDENTIFIER);
    }

    /**
     * Tests if this entry should produce a rule which will match on function identifiers.
     * 
     * @return true if function identifiers should be matched
     */
    public boolean isMatchFunctionIdentifier() {
      return is(FUNCTION_IDENTIFIER);
    }

    /**
     * Returns an entry that will produce rules which include the function parameters.
     * 
     * @return the new entry
     */
    public Entry matchFunctionParameters() {
      return match(FUNCTION_PARAMETERS);
    }

    /**
     * Returns an entry that will produce rules which do not include the function parameters.
     * 
     * @return the new entry
     */
    public Entry ignoreFunctionParameters() {
      return ignore(FUNCTION_PARAMETERS);
    }

    /**
     * Tests if this entry should produce a rule which will match on function parameters.
     * 
     * @return true if function parameters should be matched
     */
    public boolean isMatchFunctionParameters() {
      return is(FUNCTION_PARAMETERS);
    }

    /**
     * Returns an entry that will produce rules which include the computation target.
     * 
     * @return the new entry
     */
    public Entry matchTarget() {
      return match(TARGET);
    }

    /**
     * Returns an entry that will produce rules which do not include the computation target.
     * 
     * @return the new entry
     */
    public Entry ignoreTarget() {
      return ignore(TARGET);
    }

    /**
     * Tests if this entry should produce a rule which will match on function parameters.
     * 
     * @return true if function parameters should be matched
     */
    public boolean isMatchTarget() {
      return is(TARGET);
    }

    /**
     * Returns an entry that will produce rules which include the function inputs.
     * 
     * @return the new entry
     */
    public Entry matchInputs() {
      return match(INPUTS);
    }

    /**
     * Returns an entry that will produce rules which do not include the function inputs.
     * 
     * @return the new entry
     */
    public Entry ignoreInputs() {
      return ignore(INPUTS);
    }

    /**
     * Tests if this entry should produce a rule which will match on the function inputs.
     * 
     * @return true if function inputs should be matched
     */
    public boolean isMatchInputs() {
      return is(INPUTS);
    }

    /**
     * Returns an entry that will produce rules which include the function outputs.
     * 
     * @return the new entry
     */
    public Entry matchOutputs() {
      return match(OUTPUTS);
    }

    /**
     * Returns an entry that will produce rules which do not include the function outputs.
     * 
     * @return the new entry
     */
    public Entry ignoreOutputs() {
      return ignore(OUTPUTS);
    }

    /**
     * Tests if this entry should produce a rule which will match on the function outputs.
     * 
     * @return true if function outputs should be matched
     */
    public boolean isMatchOutputs() {
      return is(OUTPUTS);
    }

    /**
     * Returns an entry that will produce rules which have a specific activation period.
     * 
     * @param timeToLive the activation period, null to use the policy default
     * @return the new entry
     */
    public Entry activationPeriod(final Integer timeToLive) {
      return new Entry(_what, timeToLive);
    }

    /**
     * Tests if this is the policy default.
     * 
     * @return true if this is the policy default activation period
     */
    public boolean isDefaultActivationPeriod() {
      return _ttl == null;
    }

    /**
     * Returns the activation period (time to live) for rules generated by this entry.
     * 
     * @return the time to live, in seconds, or null to use the policy default
     */
    public Integer getActivationPeriod() {
      return _ttl;
    }

    /**
     * Returns the activation period.
     * 
     * @param policy the policy to use for the default activation period
     * @return the activation period, in seconds, when this entry is used as part of the given policy.
     */
    public int getActivationPeriod(final FunctionBlacklistPolicy policy) {
      if (isDefaultActivationPeriod()) {
        return policy.getDefaultEntryActivationPeriod();
      } else {
        return getActivationPeriod();
      }
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof Entry)) {
        return false;
      }
      final Entry e = (Entry) o;
      if (e._what != _what) {
        return false;
      }
      if (_ttl != null) {
        return _ttl.equals(e._ttl);
      } else {
        return e._ttl == null;
      }
    }

    @Override
    public int hashCode() {
      int hc = _what * 31 * 31 * 31;
      if (_ttl != null) {
        hc += _ttl;
      }
      return hc;
    }

  }

  /**
   * Returns the symbolic name of the policy.
   * 
   * @return the policy name, not null
   */
  String getName();

  /**
   * Returns the default activation period for entries which do not specify one.
   * 
   * @return the default activation period, in seconds
   */
  int getDefaultEntryActivationPeriod();

  /**
   * Returns the entries in the policy.
   * 
   * @return the entries defined in the policy
   */
  Set<Entry> getEntries();

  /**
   * Tests if the policy contains no entries.
   * 
   * @return true if the policy has no entries, false otherwise
   */
  boolean isEmpty();

  /**
   * Tests equality of two policies. Two policies are equal if they contain the same entries, have the same name, and same activation period. An implementation is available in
   * AbstractFunctionBlacklistPolicy.
   * 
   * @param o other object to test
   * @return true if the policies are equal, false otherwise
   */
  boolean equals(Object o);

  /**
   * Produces a hash code of the policy. The hashcode must be based on the name, activation period and entries. An implementation is available in AbstractFunctionBlacklistPolicy.
   * 
   * @return the hash code
   */
  int hashCode();

}
