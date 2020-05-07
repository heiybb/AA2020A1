import java.io.PrintWriter;

/**
 * Run queue interface used to implement a number of data structures.
 *
 * Note, you should not need to modify this.
 *
 * @author Sajal Halder, Minyi Li, Jeffrey Chan
 */
public interface Runqueue {

    /**
     * Create and add a process.
     *
     * @param procLabel Process label to be added.
     * @param vt Vruntime of added process.
     */
    public abstract void enqueue(String procLabel, int vt);


    /**
     * Delete the process with the highest priority, i.e., the smallest vt value.
     * Processes having the same vt follows FIFO order.
     *
     * @return Label of deleted/dequeued process.
     */
    public abstract String dequeue();


    /**
     * Find the specified process.
     *
     * @param procLabel Process label to find.
     *
     * @return True if procLabel exists in the runqueue; otherwise false.
     */
    public abstract boolean findProcess(String procLabel);


    /**
     * Removes a process from the run queue.
     *
     * @param procLabel Process to remove.
     *
     * @return True if successfully deleted, otherwise false (the process does not exist).
     */
    public abstract boolean removeProcess(String procLabel);


    /**
     * Calculate the total run time of the preceding processes of specified process.
     *
     * @param procLabel Process to calculate total proceding time for.
     *
     * @return Total executed vruntime before the specified process.
     */
    public abstract int precedingProcessTime(String procLabel);


   /**
     * Calculate the total run time of the succeeding processes of specified process.
     *
     * @param procLabel Process to calculate total succeeding time for.
     *
     * @return Total executed vruntime after the specified process.
     */
    public abstract int succeedingProcessTime(String procLabel);


    /**
     * Prints the list of process to PrintWriter 'os'.
     *
     * @param os PrinterWriter to print to.
     */
    public abstract void printAllProcesses(PrintWriter os);

} // end of interface Runqueue
