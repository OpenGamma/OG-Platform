import com.opengamma.math.matrix.DoubleMatrix1D;

public class MatrixDemo {
    public static void main(String[] args) {
      double[] primitives = new double[] {1, 2, 3, 4, 5, 6};
      DoubleMatrix1D d = new DoubleMatrix1D(primitives);
      System.out.println(d.getNumberOfElements());
    }
}

