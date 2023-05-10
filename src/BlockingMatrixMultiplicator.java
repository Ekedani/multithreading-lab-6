import mpi.MPI;

public class BlockingMatrixMultiplicator {
    public static int NUMBER_OF_ROWS_IN_A = 1000;
    public static int NUMBER_OF_COLS_IN_A = 1000;
    public static int NUMBER_OF_COLS_IN_B = 1000;
    public static int MASTER = 0;
    public static int FROM_MASTER_TAG = 1;
    public static int FROM_WORKER_TAG = 2;

    public static boolean RESULT_IS_PRINTED = false;
    public static boolean RANDOMIZE_MATRICES = false;
    public static boolean VALIDATE_RESULT = false;


    public static void main(String[] args) {
        int taskId, tasksNumber, workersNumber;

        int[] rows = {0}, offset = {0};
        double[][] a = new double[NUMBER_OF_ROWS_IN_A][NUMBER_OF_COLS_IN_A];
        double[][] b = new double[NUMBER_OF_COLS_IN_A][NUMBER_OF_COLS_IN_B];
        double[][] c = new double[NUMBER_OF_ROWS_IN_A][NUMBER_OF_COLS_IN_B];

        MPI.Init(args);
        taskId = MPI.COMM_WORLD.Rank();
        tasksNumber = MPI.COMM_WORLD.Size();
        workersNumber = tasksNumber - 1;

        if (tasksNumber < 2) {
            System.err.println("Need at least two MPI tasks. Quitting...");
            MPI.COMM_WORLD.Abort(1);
            return;
        }

        if (taskId == MASTER) {
            System.out.println("MPI_BMM has started with " + tasksNumber + " tasks.");
            if (RANDOMIZE_MATRICES) {
                Helper.initializeMatrixWithRandom(a, 50, 500);
                Helper.initializeMatrixWithRandom(b, 50, 500);
            } else {
                Helper.initializeMatrixWithNumber(a, 100);
                Helper.initializeMatrixWithNumber(b, 100);
            }

            long startTime = System.nanoTime();
            int rowsPerTask = NUMBER_OF_ROWS_IN_A / workersNumber;
            int extraRows = NUMBER_OF_ROWS_IN_A % workersNumber;

            for (int destination = 1; destination <= workersNumber; destination++) {
                rows[0] = (destination <= extraRows) ? rowsPerTask + 1 : rowsPerTask;
                MPI.COMM_WORLD.Send(offset, 0, 1, MPI.INT, destination, FROM_MASTER_TAG);
                MPI.COMM_WORLD.Send(rows, 0, 1, MPI.INT, destination, FROM_MASTER_TAG);
                MPI.COMM_WORLD.Send(a, offset[0], rows[0], MPI.OBJECT, destination, FROM_MASTER_TAG);
                MPI.COMM_WORLD.Send(b, 0, NUMBER_OF_COLS_IN_A, MPI.OBJECT, destination, FROM_MASTER_TAG);
                offset[0] += rows[0];
            }
            for (int source = 1; source <= workersNumber; source++) {
                MPI.COMM_WORLD.Recv(offset, 0, 1, MPI.INT, source, FROM_WORKER_TAG);
                MPI.COMM_WORLD.Recv(rows, 0, 1, MPI.INT, source, FROM_WORKER_TAG);
                MPI.COMM_WORLD.Recv(c, offset[0], rows[0], MPI.OBJECT, source, FROM_WORKER_TAG);

            }
            long endTime = System.nanoTime();
            System.out.println("===== RESULTS =====");
            if (RESULT_IS_PRINTED) {
                Helper.outputMatrix(c);
            }
            System.out.println("Execution time: " + (endTime - startTime) + " ns");
            if (VALIDATE_RESULT) {
                System.out.println("Result is valid: " + NaiveMatrixMultiplicator.validateMultiplicationResult(a, b, c));
            }
        } else {
            MPI.COMM_WORLD.Recv(offset, 0, 1, MPI.INT, MASTER, FROM_MASTER_TAG);
            MPI.COMM_WORLD.Recv(rows, 0, 1, MPI.INT, MASTER, FROM_MASTER_TAG);
            MPI.COMM_WORLD.Recv(a, 0, rows[0], MPI.OBJECT, MASTER, FROM_MASTER_TAG);
            MPI.COMM_WORLD.Recv(b, 0, NUMBER_OF_COLS_IN_A, MPI.OBJECT, MASTER, FROM_MASTER_TAG);

            for (int k = 0; k < NUMBER_OF_COLS_IN_B; k++) {
                for (int i = 0; i < rows[0]; i++) {
                    for (int j = 0; j < NUMBER_OF_COLS_IN_A; j++) {
                        c[i][k] += a[i][j] * b[j][k];
                    }
                }
            }

            MPI.COMM_WORLD.Send(offset, 0, 1, MPI.INT, MASTER, FROM_WORKER_TAG);
            MPI.COMM_WORLD.Send(rows, 0, 1, MPI.INT, MASTER, FROM_WORKER_TAG);
            MPI.COMM_WORLD.Send(c, 0, rows[0], MPI.OBJECT, MASTER, FROM_WORKER_TAG);
        }
        MPI.Finalize();
    }
}
