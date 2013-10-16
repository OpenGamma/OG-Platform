OpenGamma Platform 2.2 milestones
---------------------------------

These release notes cover changes from v2.1 to v2.2.


Upgrading from 2.1.0
====================

To 2.2.0-M1
-----------

Configuration compatibility
- No changes required

Database compatibility
- No upgrade required

API compatibility
- No significant changes

Analytics compatibility
- No expected differences


Changes since 2.1.0
===================

To 2.2.0-M1
-----------
http://jira.opengamma.com/issues/?jql=fixVersion%20%3D%20%222.2.0-M1%22

API compatability
- The sub-classes of ValueProperties are no longer publicly visible. Details
  for correcting any affected code can be found in the Javadoc for the
  ValueProperties.isNearInfiniteProperties method.
- The representation of a dependency graph has changed. This affects the
  DependencyGraph and DependencyNode classes. Graphs (and nodes) are now
  immutable and it is now only possible to navigate a graph from root to leaf
  without maintaining additional data structures. Nodes no longer hold details
  of whether any outputs are terminal. This can reduce the memory foot-print of
  the application by allowing node instances to be shared between graphs.
  Additional methods have been added to DependencyGraphExplorer to provide
  similar functionality to the indices that were previously available.
- The CompiledFunctionResolver interface now has a getFunction method.
- ComputationTargetFilter is now an interface and is no longer extended from
  a dependency graph node filter.
