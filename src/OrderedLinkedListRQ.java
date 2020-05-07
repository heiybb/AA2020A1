import java.io.PrintWriter;
import java.lang.String;

/**
 * Implementation of the run queue interface using an Ordered Link List.
 * <p>
 * Your task is to complete the implementation of this class.
 * You may add methods and attributes, but ensure your modified class compiles and runs.
 *
 * @author Sajal Halder, Minyi Li, Jeffrey Chan.
 */
public class OrderedLinkedListRQ implements Runqueue {
    int size;
    LinkedProc first;
    LinkedProc last;

    /**
     * Constructs empty linked list
     */
    public OrderedLinkedListRQ() {
    }  // end of OrderedLinkedList()

    @Override
    public void enqueue(String procLabel, int vt) {
        if (size == 0) {
            first = new LinkedProc(procLabel, vt);
            last = first;
            size++;
        } else {
            int index = getIndexByLabel(procLabel);
            if (index == -1) {
                int preInsert = findInsertPos(vt);
                add(preInsert, procLabel, vt);
            }
        }
    } // end of enqueue()

    @Override
    public String dequeue() {
        String deLabel = "";
        if (first != null) {
            deLabel = first.label;
            deleteLink(0);
        }
        return deLabel;
    } // end of dequeue()


    @Override
    public boolean findProcess(String procLabel) {
        return getIndexByLabel(procLabel) != -1;
    } // end of findProcess()

    @Override
    public boolean removeProcess(String procLabel) {
        int index = getIndexByLabel(procLabel);
        if (index != -1) {
            deleteLink(index);
            return true;
        }
        return false;
    } // End of removeProcess()


    @Override
    public int precedingProcessTime(String procLabel) {
        int index = getIndexByLabel(procLabel);
        if (index != -1) {
            LinkedProc process = getByIndex(index);
            int sum = 0;
            LinkedProc current = process.prev;
            while (current != null) {
                sum += current.vt;
                current = current.prev;
            }
            return sum;
        }
        return -1;
    } // end of precedingProcessTime()


    @Override
    public int succeedingProcessTime(String procLabel) {
        int index = getIndexByLabel(procLabel);
        if (index != -1) {
            LinkedProc process = getByIndex(index);
            int sum = 0;
            LinkedProc current = process.next;
            while (current != null) {
                sum += current.vt;
                current = current.next;
            }
            return sum;
        }
        return -1;
    } // end of precedingProcessTime()


    @Override
    public void printAllProcesses(PrintWriter os) {
        if (first != null) {
            LinkedProc current = first;
            while (current != null) {
                os.print(current.label + " ");
                current = current.next;
            }
            os.println();
        }
    } // end of printAllProcess()

    public void add(int index, String procLabel, int vt) {
        if (index > size || index < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (index == size) {
            addAtLast(procLabel, vt);
        } else {
            LinkedProc l = getByIndex(index);
            addBeforeNode(procLabel, vt, l);
        }
    }

    private int findInsertPos(int vt) {
        LinkedProc current = first;
        int count = 0;
        while (current != null) {
            if (vt >= current.vt) {
                count++;
            }
            current = current.next;
        }
        return count;
    }

    private void addAtLast(String procLabel, int vt) {
        LinkedProc last = this.last;
        LinkedProc node = new LinkedProc(procLabel, vt, null, last);
        this.last = node;
        if (last == null) {
            first = node;
        } else {
            last.next = node;
        }
        size++;
    }

    public int getIndexByLabel(String label) {
        LinkedProc cursor = first;
        int count = 0;
        while (cursor != null) {
            if (label.equals(cursor.label)) {
                return count;
            }
            count++;
            cursor = cursor.next;
        }
        return -1;
    }

    private LinkedProc getByIndex(int index) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (index < (size >> 1)) {
            LinkedProc cursor = first;
            for (int i = 0; i < index; i++) {
                cursor = cursor.next;
            }
            return cursor;
        } else {
            LinkedProc cursor = last;
            for (int i = size - 1; i > index; i--) {
                cursor = cursor.prev;
            }
            return cursor;
        }
    }

    private void addBeforeNode(String procLabel, int vt, LinkedProc preAdd) {
        LinkedProc preNode = preAdd.prev;
        LinkedProc newNode = new LinkedProc(procLabel, vt, preAdd, preNode);
        if (preNode == null) {
            first = newNode;
        } else {
            preNode.next = newNode;
        }
        preAdd.prev = newNode;
        size++;
    }

    private void deleteLink(int index) {
        LinkedProc l = getByIndex(index);
        LinkedProc prevNode = l.prev;
        LinkedProc nextNode = l.next;

        if (prevNode == null) {
            first = nextNode;
        } else {
            prevNode.next = nextNode;
            l.next = null;
        }

        if (nextNode == null) {
            last = prevNode;
        } else {
            nextNode.prev = prevNode;
            l.prev = null;
        }
        size--;
        l = null;
    }

    static class LinkedProc {
        String label;
        int vt;
        LinkedProc next;
        LinkedProc prev;

        public LinkedProc(String label, int vt) {
            this.label = label;
            this.vt = vt;
        }

        public LinkedProc(String label, int vt, LinkedProc next, LinkedProc prev) {
            this.label = label;
            this.vt = vt;
            this.next = next;
            this.prev = prev;
        }

        @Override
        public int hashCode() {
            return label.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            LinkedProc that = (LinkedProc) o;

            return label.equals(that.label);
        }
    }

} // end of class OrderedLinkedListRQ
