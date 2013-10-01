OpenGamma Platform 2.2 milestones
---------------------------------

These release notes cover changes from v2.1 to v2.2.


Upgrading from 2.1.0
====================

To 2.2.0-M1
-----------

Configuration compatibility
- The legacy analytics web UI has been retired. WebsiteAnalyticsComponentFactory, which used to construct its
  server-side components, has been removed. Any instances of this can be safely deleted from component configuration
  .ini files.

Database compatibility
- No upgrade required

API compatibility
- The legacy analytics web UI has been retired. The dependency on the CometD long-polling library, and the custom
  RESTful end-points that it used, have been removed.

- ExternalIdSearch is now immutable
Change constructor to of() factory

- FXForwardCurveDefinition is now immutable
Change constructor to of() factory
Change getTenors() to getTenorsArray()

Analytics compatibility
- No expected differences


Changes since 2.1.0
===================

To 2.2.0-M1
-----------
http://jira.opengamma.com/issues/?jql=fixVersion%20%3D%20%222.2.0-M1%22
