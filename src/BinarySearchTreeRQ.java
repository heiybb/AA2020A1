import java.io.PrintWriter;
import java.lang.String;

/**
 * Implementation of the Runqueue interface using a Binary Search Tree.
 * <p>
 * Your task is to complete the implementation of this class.
 * You may add methods and attributes, but ensure your modified class compiles and runs.
 *
 * @author Sajal Halder, Minyi Li, Jeffrey Chan
 */
public class BinarySearchTreeRQ implements Runqueue {
    private ProcNode root;

    /**
     * Constructs empty queue
     */
    public BinarySearchTreeRQ() {
        root = null;
    }  // end of BinarySearchTreeRQ()


    @Override
    public void enqueue(String procLabel, int vt) {
        ProcNode procNode = new ProcNode(procLabel, vt);
        if (root == null) {
            root = procNode;
            root.pos = 0;
        }
        if (findProcess(procLabel)) {
            return;
        }
        ProcNode current = root;
        ProcNode parent;
        while (true) {
            parent = current;
            if (vt < current.vt) {
                current = current.leftChild;
                if (current == null) {
                    procNode.pos = -1;
                    parent.leftChild = procNode;
                    return;
                }
            } else {
                current = current.rightChild;
                if (current == null) {
                    procNode.pos = 1;
                    parent.rightChild = procNode;
                    return;
                }
            }
        }
    } // end of enqueue()


    @Override
    public String dequeue() {
        String deLabel;
        if (root == null) {
            return "";
        } else {
            deLabel = findMin(root).procLabel;
            removeProcess(deLabel);
            return deLabel;
        }
    } // end of dequeue()


    @Override
    public boolean findProcess(String procLabel) {
        return inorderFind(root, procLabel) != null;
    } // end of findProcess()


    @Override
    public boolean removeProcess(String procLabel) {
        ProcNode node = inorderFind(root, procLabel);
        if (node != null) {
            ProcNode parent = parentHelper(root, node);
            //Case 1: The node has no children
            if (node.leftChild == null && node.rightChild == null) {
                if (node.pos == 0) {
                    root = null;
                } else if (node.pos == -1) {

                    parent.leftChild = null;
                } else if (node.pos == 1) {
                    parent.rightChild = null;
                }
            }
            //Case 2: The node has no left child but right child exist
            else if (node.leftChild == null) {
                if (node.pos == 0) {
                    node.rightChild.pos = 0;
                    root = node.rightChild;
                } else if (node.pos == -1) {
                    node.rightChild.pos = -1;
                    parent.leftChild = node.rightChild;
                } else if (node.pos == 1) {
                    parent.rightChild = node.rightChild;
                }
            }
            //Case 3: The node has no right child but left child exist
            else if (node.rightChild == null) {
                if (node.pos == 0) {
                    node.leftChild.pos = 0;
                    root = node.leftChild;
                } else if (node.pos == -1) {
                    parent.leftChild = node.leftChild;
                } else if (node.pos == 1) {
                    node.leftChild.pos = -1;
                    parent.rightChild = node.leftChild;
                }
            }//Case 4: The node has two children
            else {
                ProcNode nRoot = findMin(node.rightChild);
                if (node.pos == 0) {
                    //Set the new root Node's pos as 0 which represents the root Node
                    nRoot.pos = 0;
                } else if (node.pos == -1) {
                    nRoot.pos = -1;
                } else if (node.pos == 1) {
                    nRoot.pos = 1;
                }
                //Update the LR child as the nRoot originally has no children
                nRoot.leftChild = node.leftChild;
                nRoot.rightChild = node.rightChild;
                //Remove the nRoot from its parent
                ProcNode nRootParent = findMinParent(node.rightChild);
                nRootParent.leftChild = null;
                //Update the root Node ref
                node = nRoot;
            }
            //GC Recycle
            node = null;
            parent = null;
            return true;
        }
        return false;
    } // end of removeProcess()


    @Override
    public int precedingProcessTime(String procLabel) {
        ProcNode node = inorderFind(root, procLabel);
        if (node == null) {
            return -1;
        }
        if (node.pos == -1 || node.pos == 0) {
            return time(node.leftChild);
        } else {
            return time(root) - time(node) + time(node.leftChild);
        }
    } // end of precedingProcessTime()


    @Override
    public int succeedingProcessTime(String procLabel) {
        ProcNode node = inorderFind(root, procLabel);
        ProcNode nodeParent = parentHelper(root, node);
        if (node == null) {
            return -1;
        }
        if (node.pos == 0 || node.pos == 1) {
            return time(node.rightChild);
        } else {
            return time(node.rightChild) + time(nodeParent) - time(node);
        }
    } // end of precedingProcessTime()


    @Override
    public void printAllProcesses(PrintWriter os) {
        inorderPrint(os, root);
        os.println();
    } // end of printAllProcess()

    private ProcNode inorderFind(ProcNode node, String procLabel) {
        ProcNode preR = null;
        if (node != null) {
            if (node.leftChild != null) {
                preR = inorderFind(node.leftChild, procLabel);
                if (preR != null) {
                    return preR;
                }
            }
            if (node.procLabel.equals(procLabel)) {
                return node;
            }
            if (node.rightChild != null) {
                preR = inorderFind(node.rightChild, procLabel);
                if (preR != null) {
                    return preR;
                }
            }
        }
        return preR;
    }

    private ProcNode parentHelper(ProcNode currentRoot, ProcNode targetNode) {
        if (targetNode == root || currentRoot == null || targetNode == null) {
            return null;
        } else {
            if (currentRoot.leftChild == targetNode || currentRoot.rightChild == targetNode) {
                return currentRoot;
            } else {
                if (currentRoot.vt <= targetNode.vt) {
                    return parentHelper(currentRoot.rightChild, targetNode);
                } else {
                    return parentHelper(currentRoot.leftChild, targetNode);
                }
            }
        }
    }

    private int time(ProcNode node) {
        if (node == null) {
            return 0;
        }
        int sum = 0;
        if (node.leftChild != null) {
            sum += time(node.leftChild);
        }
        sum += node.vt;
        if (node.rightChild != null) {
            sum += time(node.rightChild);
        }
        return sum;
    }

    private ProcNode findMin(ProcNode start) {
        if (start == null) {
            return null;
        }
        if (start.leftChild == null) {
            return start;
        }
        ProcNode min = start;
        ProcNode current = start;
        while (current != null) {
            min = current;
            current = current.leftChild;
        }
        return min;
    }

    private ProcNode findMinParent(ProcNode start) {
        if (start == null) {
            return null;
        }
        ProcNode min = start;
        ProcNode minParent = start;
        ProcNode current = start;
        while (current != null) {
            minParent = min;
            min = current;
            current = current.leftChild;
        }
        return minParent;
    }


    private void inorderPrint(PrintWriter os, ProcNode node) {
        if (node != null) {
            inorderPrint(os, node.leftChild);
            os.print(node.procLabel + " ");
            os.flush();
            inorderPrint(os, node.rightChild);
        }
    }

    static class ProcNode {
        ProcNode leftChild;
        ProcNode rightChild;
        String procLabel;
        int vt;
        /*
         *   0 root, -1 left, 1 right
         */
        int pos;


        ProcNode(String procLabel, int vt, int pos) {
            this.procLabel = procLabel;
            this.vt = vt;
            this.pos = pos;
        }

        public ProcNode(String procLabel, int vt) {
            this.procLabel = procLabel;
            this.vt = vt;
        }
    }

} // end of class BinarySearchTreeRQ
