import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap as FixedCouponSwap
from com.opengamma.financial.interestrate.annuity.definition import *  # contains AnnuityCouponFixed and AnnuityCouponIbor
import com.opengamma.util.money.Currency as Currency 
import com.opengamma.financial.interestrate as IR 
import array

''' NOTES: Here are all the guts to putting together a simple, but realistic, yield curve from a set of observed swap rates/
 It is a very rough draft, and lacks much description, print statements, and so on. It is a base. When we decide to make it DEXY,
 I will reorganize it accordingly
'''
# Putting together the pieces of the MultipleYieldCurveFinderDataBundle

# Specification of Yield Points
ccy = Currency.EUR
crvName = 'onlyThis'
nMats = 10
mats = range(1,nMats+1)
rates = [0.025, 0.02, 0.0225, 0.025, 0.03, 0.035, 0.04, 0.045,0.04, 0.045] # Some uniformity to the rates # !DEXY  Would be great to include a plot


# Interpolation for Calculators
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory as InterpFactory
import com.opengamma.math.interpolation.sensitivity.CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory as SensCalc
interp = InterpFactory.getInterpolator("NaturalCubicSpline","LinearExtrapolator","FlatExtrapolator")
interpSens = SensCalc.getSensitivityCalculator("NaturalCubicSpline","LinearExtrapolator","FlatExtrapolator", False);

# YieldCurveBundle
from com.opengamma.financial.model.interestrate.curve import *
from com.opengamma.math.curve import InterpolatedDoublesCurve



# Calculators
pvCalc = IR.PresentValueCalculator.getInstance()
sensCalc = IR.PresentValueSensitivityCalculator.getInstance()

# Market Instruments (just Swaps in our example)
def makeSwap(ccy,years,rate,crvName):
  floating = AnnuityCouponIbor(ccy,[ 0.5*i for i in range(1,2*years+1)],crvName,crvName,True) # floating paying semi-annually
  fixed = AnnuityCouponFixed(ccy,range(1,years+1),rate,crvName, False) # fixed side paying annually
  return FixedCouponSwap(fixed,floating)

mktInstruments = []
mktValues = []
for i in range(nMats):
  iswap = makeSwap(ccy,mats[i],rates[i],crvName)
  mktInstruments.append(iswap)
  mktValues.append(0.0) # !!! By definition, on-market swaps have zero value

# Market Data Bundle likes maps
from java.util import LinkedHashMap
mapCrvMat = LinkedHashMap()
mapCrvInterp = LinkedHashMap()
mapSensInterp = LinkedHashMap()
aMats = array.array('d',mats) # HashMaps like arrays
mapCrvMat.put(crvName,aMats)
print mapCrvMat.values()
mapCrvInterp.put(crvName,interp)
mapSensInterp.put(crvName,interpSens)

crvFinderDataBundle = IR.MultipleYieldCurveFinderDataBundle(mktInstruments, mktValues,None, mapCrvMat, mapCrvInterp, mapSensInterp)

# The function itself
func = IR.MultipleYieldCurveFinderFunction(crvFinderDataBundle, pvCalc);

# Compute Jacobian Analytically. We could also use FD to approximate the sensitivities from our Calculator
jacobian = IR.MultipleYieldCurveFinderJacobian(crvFinderDataBundle,  sensCalc)

# Choose a root finder
import com.opengamma.math.rootfinding.newton as rootfinders # TODO Ugly warning sent out here
rootFinder = rootfinders.BroydenVectorRootFinder()

# Naive initial guess
from com.opengamma.math.matrix import DoubleMatrix1D
guess = DoubleMatrix1D([0.01 for i in range(nMats)])

# Calibrate!
calibRates = rootFinder.getRoot(func,jacobian,guess)

yldCrv = YieldCurve(InterpolatedDoublesCurve.from(mats,calibRates.getData(),interp))
crvBundle = IR.YieldCurveBundle([crvName],[yldCrv])

# Confirm

for i in range(nMats):
  print 'pv swap('+str(i)+') = ' + str(pvCalc.visit(mktInstruments[i],crvBundle))
