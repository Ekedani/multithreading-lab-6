public class NaiveMatrixMultiplicator {
    public static int NUMBER_OF_ROWS_IN_A = 1000;
    public static int NUMBER_OF_COLS_IN_A = 1000;
    public static int NUMBER_OF_COLS_IN_B = 1000;

    public static boolean RESULT_IS_PRINTED = false;
    public static boolean RANDOMIZE_MATRICES = false;

    public static void main(String[] args) {
        double[][] a = new double[NUMBER_OF_ROWS_IN_A][NUMBER_OF_COLS_IN_A];
        double[][] b = new double[NUMBER_OF_COLS_IN_A][NUMBER_OF_COLS_IN_B];
        double[][] c = new double[NUMBER_OF_ROWS_IN_A][NUMBER_OF_COLS_IN_B];

        System.out.println("Naive has started");
        if (RANDOMIZE_MATRICES) {
            Helper.initializeMatrixWithRandom(a, 50, 500);
            Helper.initializeMatrixWithRandom(b, 50, 500);
        } else {
            Helper.initializeMatrixWithNumber(a, 100);
            Helper.initializeMatrixWithNumber(b, 100);
        }

        long startTime = System.nanoTime();
        for (int i = 0; i < NUMBER_OF_ROWS_IN_A; i++) {
            for (int j = 0; j < NUMBER_OF_COLS_IN_B; j++) {
                for (int k = 0; k < NUMBER_OF_COLS_IN_A; k++) {
                    c[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        long endTime = System.nanoTime();

        System.out.println("===== RESULT MATRIX =====");
        if (RESULT_IS_PRINTED) {
            Helper.outputMatrix(c);
        }
        System.out.println("Execution time: " + (endTime - startTime) + " ns");
    }

    public static boolean validateMultiplicationResult(double[][] a, double[][] b, double[][] c) {
        double[][] result = new double[a.length][b[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b[0].length; j++) {
                for (int k = 0; k < a[0].length; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        for (int i = 0; i < c.length; i++) {
            for (int j = 0; j < c[0].length; j++) {
                if (c[i][j] != result[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
