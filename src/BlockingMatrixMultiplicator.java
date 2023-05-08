import mpi.MPI;

public class BlockingMatrixMultiplicator {
    public static int NRA = 10;
    public static int NCA = 10;
    public static int NCB = 10;
    public static int MASTER = 0;
    public static int FROM_MASTER = 1;
    public static int FROM_WORKER = 2;

    public static void main(String[] args) {
        int averow, extra;
        int[] rows = {0}, offset = {0};
        int taskId, tasksNumber, workersNumber;

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
            System.out.println("MPI_BMM has started with " + tasksNumber + " tasks.");
            Helper.initializeMatrixWithNumber(a, 10);
            Helper.initializeMatrixWithNumber(b, 10);

            averow = NRA / workersNumber;
            extra = NRA % workersNumber;

            for (int destination = 1; destination <= workersNumber; destination++) {
                rows[0] = (destination <= extra) ? averow + 1 : averow;
                MPI.COMM_WORLD.Send(offset, 0, 1, MPI.INT, destination, FROM_MASTER);
                MPI.COMM_WORLD.Send(rows, 0, 1, MPI.INT, destination, FROM_MASTER);
                MPI.COMM_WORLD.Send(a, offset[0], rows[0], MPI.OBJECT, destination, FROM_MASTER);
                MPI.COMM_WORLD.Send(b, 0, NCA, MPI.OBJECT, destination, FROM_MASTER);
                offset[0] += rows[0];
            }
            for (int source = 1; source <= workersNumber; source++) {
                MPI.COMM_WORLD.Recv(offset, 0, 1, MPI.INT, source, FROM_WORKER);
                MPI.COMM_WORLD.Recv(rows, 0, 1, MPI.INT, source, FROM_WORKER);
                MPI.COMM_WORLD.Recv(c, offset[0], rows[0], MPI.OBJECT, source, FROM_WORKER);

            }
            System.out.println("===== RESULT MATRIX =====");
            Helper.outputMatrix(c);
        } else {
            MPI.COMM_WORLD.Recv(offset, 0, 1, MPI.INT, MASTER, FROM_MASTER);
            MPI.COMM_WORLD.Recv(rows, 0, 1, MPI.INT, MASTER, FROM_MASTER);
            MPI.COMM_WORLD.Recv(a, 0, rows[0], MPI.OBJECT, MASTER, FROM_MASTER);
            MPI.COMM_WORLD.Recv(b, 0, NCA, MPI.OBJECT, MASTER, FROM_MASTER);

            for (int k = 0; k < NCB; k++) {
                for (int i = 0; i < rows[0]; i++) {
                    for (int j = 0; j < NCA; j++) {
                        c[i][k] += a[i][j] * b[j][k];
                    }
                }
            }

            MPI.COMM_WORLD.Send(offset, 0, 1, MPI.INT, MASTER, FROM_WORKER);
            MPI.COMM_WORLD.Send(rows, 0, 1, MPI.INT, MASTER, FROM_WORKER);
            MPI.COMM_WORLD.Send(c, 0, rows[0], MPI.OBJECT, MASTER, FROM_WORKER);
        }
        MPI.Finalize();
    }
}
