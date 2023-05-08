import java.util.Random;

public class Helper {
    public static double[][] generateMatrix(int rows, int cols, int low, int high) {
        var random = new Random();
        var result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = low + random.nextDouble() * high;
            }
        }
        return result;
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
