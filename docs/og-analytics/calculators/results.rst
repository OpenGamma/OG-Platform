Results or Risk Measures
================

Present value
-------

Par rate
--------

Curve sensitivity
-----------------

Three types or curve sensitivties are computed in the library: *point sensitivity*, *curve parameters sensitivity* and *market quotes sensitivity*.

The library uses a Algorithmic Differentiation approach. The different sensitivities above are computed one from the other, starting from the point sensitivity. 

The description of the objects in which those results are stored in the library is described in: multi-curve-implementation.rst.

**Point sensitivity**

The first step consists in computing the *point sensitivity*, i.e. the sensitivity with respect to each discount factor and each forward rate. This first output looks like

    USD=
    {USD-DSCON-OIS=[[0.6383561643835617, -14798.719687495473]]}
    {USD-LIBOR3M-FRAIRS=[ForwardSensitivity[start=0.6383561643835617, end=0.8876712328767123, af=0.25277777777777777, value=-2529910.310523003]]}

The instrument has sensitivity to one discounting points at time 0.63... for a value of -14,749. There is also the sensitivity to one forward rate which starts at time 0.63, finishes at time 0.88 with an accrual factor of 0.2527. The value of the sensitivity is 2,529,910 to a movement of 1.0 (100%) in the rate.

**Curve parameters sensitivity**

The second step consists in projecting the above sensitivity to the internal parameters of the curve. In this case both curves are represented by yield curve stored as interpolated curve on zero-coupon rates. The sensitivity obtained from this second step is the sensitivity to the zero-coupon rates (rescaled to one basis point):

    {[USD-DSCON-OIS, USD]= (0.0, 0.0, 0.0, 0.0, 0.0, -0.74, -0.73, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    [USD-LIBOR3M-FRAIRS, USD]= (0.0, 291.91, -78.14, -463.50, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) }

**Market quotes sensitivity**

The third step consists in multiplying the sensitivity to the parameters by the Jacobian matrix to obtain the market quote sensitivity.

The (generalised) Jacobian matrices are stored in the **CurveBuildingBlockBundle**. It contains, fore each curve, the curve on which it depends and the transistion matrix between market quotes and curves parameters. In the above example the object look like:

    USD-DSCON-OIS=[
    {USD-DSCON-OIS=[0, 17]},
    1.0139, 0.0000, 0.0000, 0.0000, ...
    0.5069, 0.5069, 0.0000, 0.0000, ...
    0.0000, 0.0000, 1.0138, 0.0000, ...
    0.0179, 0.0179, -0.078, 1.0559, ...
    ...]
    USD-LIBOR3M-FRAIRS=[
    {USD-DSCON-OIS=[0, 17], USD-LIBOR3M-FRAIRS=[17, 15]}, 
    0.0000, 0.0000, 0.0000, ..., 0.0000, 1.0139, 0.0000, 0.0000, ...
    0.0000, 0.0000, 0.0000, ..., 0.0000, 0.5094, 0.5038, 0.0000, ...
    0.0000, 0.0000, 0.0000, ..., 0.0000, 0.3390, 0.3353, 0.3389, ...
    ...
    0.0000, 0.0000, 0.0000, ..., 0.0627, 0.0004, 0.0000, 0.0000, ...
    ...]
The first matrix is of dimension 17x17 (it has been cut to fit in the table). It contains the sensitivity of the USD-DSCON-OIS curve parameters to the input of the USD-DSCON-OIS curve. The sensitivitires appear mainly on the diagonal but not only there. There are small sensitivities off-diagonal. The second matrix is of dimension 15x(17+15). It contains the sensitivity of the USD-LIBOR3M-FRAIRS curve parameters to the USD-DSCON-OIS and USD-LIBOR3M-FRAIRS market data. Sensitivity are mainly on the diagonal of the second 15x15 block, but there are sensitivities everywhere: parameters of the USD-LIBOR3M-FRAIRS are not only dependent on the market quote of hte same curve but also on the market quote of the previous curve USD-DSCON-OIS.





