import mpi.MPI;

public class BlockingMatrixMultiplicator {
    public static int NRA = 62;
    public static int NCA = 15;
    public static int NCB = 7;
    public static int MASTER = 0;
    public static int FROM_MASTER = 1;
    public static int FROM_WORKER = 2;

    public static void main(String[] args) {
        MPI.Init(args);

        MPI.Finalize();
    }
}
