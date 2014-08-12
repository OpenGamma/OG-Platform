====================================
ISDA Curve Snapshot Calibration Tool
====================================

The curve calibration tool can be used to validate your ISDA curve data. It 
simply loads the snapshots referenced on the command line and iterates over
the stored curves calibrating them one by one. Results are dumped to the
console in a tabular CSV format, first for yield curves and then for credit
curves.

Tool overview
=============

To run the tool, you will need to have loaded a ``YieldCurveDataSnapshot`` 
and a ``CreditCurveDataSnapshot``. To view available snapshots, navigate to
the green screens page for NamedSnapshots (normally on 
http://server:8080/jax/snapshots).

An example invocation is as follows::

  IsdaCurveSnapshotCalibrationTool -c http://localhost:8080/jax \
                                   -cs CompositesByConvention_20140806 \
                                   -ys YieldCurves_20140806 \
                                   -l com/opengamma/util/info-logback.xml

