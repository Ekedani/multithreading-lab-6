import mpi.MPI;

public class NonBlockingMatrixMultiplicator {
    public static int NRA = 62;
    public static int NCA = 15;
    public static int NCB = 7;
    public static int MASTER = 0;
    public static int FROM_MASTER = 1;
    public static int FROM_WORKER = 2;

    public static void main(String[] args) {
        int taskId, tasksNumber, workersNumber;
        int averow, extra, offset, rows;

        double[][] a = new double[NRA][NCA];
        double[][] b = new double[NCA][NCB];
        double[][] c = new double[NRA][NCB];

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

            averow = NRA / workersNumber;
            extra = NRA % workersNumber;
            offset = 0;

            for (int destination = 1; destination <= workersNumber; destination++) {
                rows = (destination <= extra) ? averow + 1 : averow;
                MPI.COMM_WORLD.Send(offset, 0, 1, MPI.INT, destination, FROM_MASTER);
                MPI.COMM_WORLD.Send(rows, 0, 1, MPI.INT, destination, FROM_MASTER);
                MPI.COMM_WORLD.Send(offset, offset, rows, MPI.DOUBLE, destination, FROM_MASTER);
                MPI.COMM_WORLD.Send(offset, offset, 1, MPI.DOUBLE, destination, FROM_MASTER);
                offset += rows;
            }
            for (int source = 1; source <= workersNumber; source++) {

            }
            System.out.println("===== RESULT MATRIX =====");
            Helper.outputMatrix(c);
        } else {
            /*for (int i = 0; i < NCB; i++) {
                for (int j = 0; j < buff[0]; j++) {
                    c[i][k] = 0;
                    for (int k = 0; k < NCA; k++) {
                        c[i][k] = c[i][k] + a[i][j] * b[j][k];
                    }
                }
            }*/
        }
        MPI.Finalize();
    }
}
