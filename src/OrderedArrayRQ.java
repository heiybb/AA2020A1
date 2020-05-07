import java.io.PrintWriter;


/**
 * Implementation of the Runqueue interface using an Ordered Array.
 * <p>
 * Your task is to complete the implementation of this class.
 * You may add methods and attributes, but ensure your modified class compiles and runs.
 *
 * @author Sajal Halder, Minyi Li, Jeffrey Chan
 */
public class OrderedArrayRQ implements Runqueue {
    private Proc[] procs;

    /**
     * Constructs empty queue
     */
    public OrderedArrayRQ() {
        procs = new Proc[10];
    }  // end of OrderedArrayRQ()


    @Override
    public void enqueue(String procLabel, int vt) {
        //Do nothing if the process already exist
        if (findProcess(procLabel)) {
            return;
        }
        int insertIndex = -1;
        for (int i = 0; i < procs.length; i++) {
            if (procs[i] == null) {
                insertIndex = i;
                break;
            }
        }
        if (insertIndex != -1) {
            //Add at the last pos
            procs[insertIndex] = new Proc(procLabel, vt);
            if (insertIndex > 0) {
                sort(procs, 0, insertIndex);
            }
            //The expand operation ensures that the lastIndex+1 will never cause out of index exception :)
            if (insertIndex >= (int) (procs.length * 0.8)) {
                expandArray();
            }
        } else {
            System.out.println("Something went wrong");
        }
    } // end of enqueue()


    @Override
    public String dequeue() {
        if (isEmpty()) {
            return "";
        }
        String deLabel = procs[0].label;
        Proc[] swap = new Proc[procs.length];
        System.arraycopy(procs, 1, swap, 0, procs.length - 1);
        procs = swap;
        return deLabel;
    } // end of dequeue()


    @Override
    public boolean findProcess(String procLabel) {
        return findByLabel(procLabel) != -1;
    } // end of findProcess()


    @Override
    public boolean removeProcess(String procLabel) {
        int procIndex = findByLabel(procLabel);
        if (procIndex != -1) {
            //Move forward
            procs[procIndex] = null;
            for (int i = procIndex; i < procs.length - 1; i++) {
                //Ignore the null part which located in the end of the array
                if (procs[i] == null && procs[i + 1] == null) {
                    break;
                }
                procs[i] = procs[i + 1];
            }
            return true;
        }
        return false;
    } // end of removeProcess()


    @Override
    public int precedingProcessTime(String procLabel) {
        int procIndex = findByLabel(procLabel);
        if (procIndex != -1) {
            int sum = 0;
            for (int i = 0; i < procIndex; i++) {
                //predecessor always not null
                if (procs[i] != null) {
                    sum += procs[i].vt;
                }
            }
            return sum;
        }
        return -1;
    }// end of precedingProcessTime()


    @Override
    public int succeedingProcessTime(String procLabel) {
        int procIndex = findByLabel(procLabel);
        if (procIndex != -1) {
            int sum = 0;
            for (int i = procIndex + 1; i < procs.length; i++) {
                if (procs[i] != null) {
                    sum += procs[i].vt;
                }
            }
            return sum;
        }
        return -1;
    } // end of precedingProcessTime()


    @Override
    public void printAllProcesses(PrintWriter os) {
        for (Proc proc : procs) {
            if (proc != null) {
                os.print(proc.label + " ");
                //manually flush the writer to avoid nothing happen
                os.flush();
            }
        }
        os.println();
    } // end of printAllProcesses()

    private boolean isEmpty() {
        for (Proc proc : procs) {
            if (proc != null) {
                return false;
            }
        }
        return true;
    }

    private void expandArray() {
        Proc[] swap = new Proc[(int) (procs.length * 1.5)];
        System.arraycopy(procs, 0, swap, 0, procs.length);
        procs = swap;
    }

    private int findByLabel(String procLabel) {
        int procIndex = -1;
        for (int i = 0; i < procs.length; i++) {
            if (procs[i] != null && procs[i].label.equals(procLabel)) {
                procIndex = i;
            }
        }
        //If procIndex != -1 means that the process is found
        return procIndex;
    }

    static void merge(Proc[] arr, int l, int m, int r) {
        // Find sizes of two sub-arrays to be merged
        int n1 = m - l + 1;
        int n2 = r - m;

        /* Create temp arrays */
        Proc[] left = new Proc[n1];
        Proc[] right = new Proc[n2];

        /*Copy data to temp arrays*/
        System.arraycopy(arr, l, left, 0, n1);
        for (int j = 0; j < n2; ++j) {
            right[j] = arr[m + 1 + j];
        }

        /* Merge the temp arrays */

        // Initial indexes of first and second sub-arrays
        int i = 0, j = 0;
        // Initial index of merged sub-array array
        int k = l;
        while (i < n1 && j < n2) {
            if (left[i].vt <= right[j].vt) {
                arr[k] = left[i];
                i++;
            } else {
                arr[k] = right[j];
                j++;
            }
            k++;
        }

        /* Copy remaining elements of L[] if any */
        while (i < n1) {
            arr[k] = left[i];
            i++;
            k++;
        }

        /* Copy remaining elements of R[] if any */
        while (j < n2) {
            arr[k] = right[j];
            j++;
            k++;
        }
    }

    static void sort(Proc[] arr, int l, int r) {
        if (l < r) {
            // Find the middle point
            int m = (l + r) / 2;

            // Sort first and second halves
            sort(arr, l, m);
            sort(arr, m + 1, r);

            // Merge the sorted halves
            merge(arr, l, m, r);
        }
    }

    static class Proc {
        String label;
        int vt;

        public Proc(String label, int vt) {
            this.label = label;
            this.vt = vt;
        }
    }
} // end of class OrderedArrayRQ
