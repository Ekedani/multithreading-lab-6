import mpi.MPI;
import mpi.Request;

import java.util.ArrayList;
import java.util.List;

public class NonBlockingMatrixMultiplicator {
    public static int NUMBER_OF_ROWS_IN_A = 10;
    public static int NUMBER_OF_COLS_IN_A = 10;
    public static int NUMBER_OF_COLS_IN_B = 10;
    public static int MASTER = 0;
    public static int FROM_MASTER_TAG = 1;
    public static int FROM_WORKER_TAG = 2;

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
            System.out.println("MPI_NBMM has started with " + tasksNumber + " tasks.");
            Helper.initializeMatrixWithNumber(a, 10);
            Helper.initializeMatrixWithNumber(b, 10);

            int averow = NUMBER_OF_ROWS_IN_A / workersNumber;
            int extra = NUMBER_OF_ROWS_IN_A % workersNumber;

            for (int destination = 1; destination <= workersNumber; destination++) {
                rows[0] = (destination <= extra) ? averow + 1 : averow;
                MPI.COMM_WORLD.Isend(offset, 0, 1, MPI.INT, destination, FROM_MASTER_TAG);
                MPI.COMM_WORLD.Isend(rows, 0, 1, MPI.INT, destination, FROM_MASTER_TAG);
                MPI.COMM_WORLD.Isend(a, offset[0], rows[0], MPI.OBJECT, destination, FROM_MASTER_TAG);
                MPI.COMM_WORLD.Isend(b, 0, NUMBER_OF_COLS_IN_A, MPI.OBJECT, destination, FROM_MASTER_TAG);
                offset[0] += rows[0];
            }
            ArrayList<Request> subTasksRequests = new ArrayList<>();
            for (int source = 1; source <= workersNumber; source++) {
                var offsetRequest = MPI.COMM_WORLD.Irecv(offset, 0, 1, MPI.INT, source, FROM_WORKER_TAG);
                var rowsRequest = MPI.COMM_WORLD.Irecv(rows, 0, 1, MPI.INT, source, FROM_WORKER_TAG);
                offsetRequest.Wait();
                rowsRequest.Wait();
                subTasksRequests.add(MPI.COMM_WORLD.Irecv(c, offset[0], rows[0], MPI.OBJECT, source, FROM_WORKER_TAG));
            }
            for (var request : subTasksRequests) {
                request.Wait();
            }
            System.out.println("===== RESULT MATRIX =====");
            Helper.outputMatrix(c);
        } else {
            var offsetRequest = MPI.COMM_WORLD.Irecv(offset, 0, 1, MPI.INT, MASTER, FROM_MASTER_TAG);
            var rowsRequest = MPI.COMM_WORLD.Irecv(rows, 0, 1, MPI.INT, MASTER, FROM_MASTER_TAG);
            var bRequest = MPI.COMM_WORLD.Irecv(b, 0, NUMBER_OF_COLS_IN_A, MPI.OBJECT, MASTER, FROM_MASTER_TAG);
            offsetRequest.Wait();
            rowsRequest.Wait();
            MPI.COMM_WORLD.Isend(offset, 0, 1, MPI.INT, MASTER, FROM_WORKER_TAG);
            MPI.COMM_WORLD.Isend(rows, 0, 1, MPI.INT, MASTER, FROM_WORKER_TAG);
            var aRequest = MPI.COMM_WORLD.Irecv(a, 0, rows[0], MPI.OBJECT, MASTER, FROM_MASTER_TAG);
            aRequest.Wait();
            bRequest.Wait();

            for (int k = 0; k < NUMBER_OF_COLS_IN_B; k++) {
                for (int i = 0; i < rows[0]; i++) {
                    for (int j = 0; j < NUMBER_OF_COLS_IN_A; j++) {
                        c[i][k] += a[i][j] * b[j][k];
                    }
                }
            }

            MPI.COMM_WORLD.Isend(c, 0, rows[0], MPI.OBJECT, MASTER, FROM_WORKER_TAG);
        }
        MPI.Finalize();
    }
}
