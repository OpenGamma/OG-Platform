def identity(n):
	''' return a 2d nxn array representing the identity matrix '''
	mtx = [[0]*n for col in range(n)]
	for row in range(n): mtx[row][row]=1
	return mtx

print 'Calling identity(4)'
print identity(4)

import com.opengamma.math.matrix as ogmtx

mtx = ogmtx.DoubleMatrix2D(identity(4))
print 'mtx has ' + str(mtx.getNumberOfElements()) + ' elements'
print 'Here is its second column'
print mtx.getColumnVector(1)


vec = ogmtx.DoubleMatrix1D(range(1,5))
print 'this is a DoubleMatrix1D built from range(1,5)'
print vec

print 'Now  attempt to multiply the mtx and the vec using ColtMatrixAlgebra'
prod = ogmtx.ColtMatrixAlgebra().multiply(mtx,vec)
print prod
print 'good. a = Ia'
