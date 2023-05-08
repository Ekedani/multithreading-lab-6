import java.util.Random;

public class Helper {

    public static void initializeMatrixWithNumber(double[][] matrix, double number) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrix[i][j] = number;
            }
        }
    }

    public static void initializeMatrixWithRandom(double[][] matrix, int low, int high) {
        var random = new Random();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrix[i][j] = low + random.nextDouble() * high;
            }
        }
    }

    public static void outputMatrix(double[][] matrix) {
        for (var row : matrix) {
            for (var number : row) {
                System.out.print(number + " ");
            }
            System.out.println();
        }
    }
}
