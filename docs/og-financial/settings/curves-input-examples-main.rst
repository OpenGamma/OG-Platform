Curves Input: Examples - Main
====================

The names in the examples are **XXX-INSIND[IND][-INSIND[IND]][-INSIND[IND]][-INT]**. 
The **XXX** is the currency. The **INS** is the type of instrument and **IND** is the relevant index in short form. Several instruments can be combined in the same curve. The name of the interpolator can be added at the end by **-INT** if the same curve is available with several interpolators. The standard names are **LIN** for linear, **NCS** for natural cubic spline and **DQ** for double quadratic.

The standard instruments are

* **OIS** Overnight index swap
* **FRA** Forward Rate Agreement
* **IRS** Interest Rate Swap
* **BS** Basis Swap
* **FUT** Short Term Interest Rate Futures
* **FFF** Fed Funds Futures
* **DSF** Deliverable Swap Futures

EUR
----

* EUR-OIS
* EUR-FRAE3M-IRSE3M
* EUR-FUTE3M-IRSE3M
* EUR-FRAE6M-IRSE6M
* EUR-FRA3M-BSE3ME6M
* EUR-FRA1M-IRS1M
* EUR-FRA12M-BSE6ME12M

GBP
-----

* GBP-OIS
* GBP-FRAL3M-IRSL3M
* GBP-FRAL3M-BSL3ML6M
* GBP-FRAL6M-IRSL6M

USD
-----

* USD-OIS
* USD-OIS-FFS
* USD-FFF-OIS
* USD-FRAL3M-IRSL3M
* USD-FUTL3M-IRSL3M
* USD-FUTL3M-DSFL3M
* USD-FRAL6M-BSL3ML6M
* USD-IRSL1M-BSL1ML3M

GBP/USD
-------

* GBPUSD-FX
* GBPUSD-FX-XCCYL3ML3M
* GBPUSD-GBPFRAL3M-XCCYL3ML3M
* GBPUSD-XCCYSOFF
