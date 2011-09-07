import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap as FixedCouponSwap
from com.opengamma.financial.interestrate.annuity.definition import *  # contains AnnuityCouponFixed and AnnuityCouponIbor
import com.opengamma.util.money.Currency as Currency 
import com.opengamma.financial.interestrate as IR 

# Putting together the pieces of the MultipleYieldCurveFinderDataBundle

# Specification of Yield Points
ccy = Currency.EUR
crvName = 'onlyThis'
nMats = 10
mats = range(1,nMats+1)
rates = [0.025, 0.02, 0.0225, 0.025, 0.03, 0.035, 0.04, 0.045,0.04, 0.045] # Some uniformity to the rates # !DEXY  Would be great to include a plot
guess = rates # rates are swap rates, the final yield curve are Continuously-Compounding discount rates. But this is a reasonable starting point


# Interpolation for Calculators
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory as InterpFactory
import com.opengamma.math.interpolation.sensitivity.CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory as SensCalc
interp = InterpFactory.getInterpolator("NaturalCubicSpline","LinearExtrapolator","FlatExtrapolator")
interpSens = SensCalc.getSensitivityCalculator("NaturalCubicSpline","LinearExtrapolator","FlatExtrapolator", False);

# YieldCurveBundle
from com.opengamma.financial.model.interestrate.curve import *
from com.opengamma.math.curve import InterpolatedDoublesCurve

yldCrv = YieldCurve(InterpolatedDoublesCurve.from(mats,rates,interp))
crvBundle = IR.YieldCurveBundle([crvName],[yldCrv])


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
  mktValues.append(pvCalc.visit(iswap,crvBundle))
  print 'mktValues['+str(i)+'] = '+str(mktValues[i])

# Market Data Bundle likes maps
from java.util import LinkedHashMap
mapCrvMat = LinkedHashMap()
mapCrvInterp = LinkedHashMap()
mapSensInterp = LinkedHashMap()
mapCrvMat.put(crvName,[n+0.0 for n in mats])
print mapCrvMat.values()
print [n+0.0 for n in mats]
mapCrvInterp.put(crvName,interp)
mapSensInterp.put(crvName,interpSens)

crvFinderDataBundle = IR.MultipleYieldCurveFinderDataBundle(mktInstruments, mktValues,None, mapCrvMat, mapCrvInterp, mapSensInterp)
# The function itself
#func = MultipleYieldCurveFinderFunction(crvFinderDataBundle, pvCalc);
