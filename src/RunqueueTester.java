import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.String;


/**
 * Framework to test the process scheduler implementations.
 * NO NEED TO MODIFY THIS FILE.
 *
 * @author Sajal Halder, Minyi Li, Jeffrey Chan
 */
public class RunqueueTester {

    /** Name of class, used in error messages. */
    protected static final String progName = "RunqueueTester";


    /**
     * Print help/usage message.
     */
    public static void usage(String progName) {
        System.err.println(progName + ": <implementation> [filename of input commands] [filename to print output]");
        System.err.println("<implementation> = <array | linkedlist | tree>");
        System.err.println("If optional filenames are specified, then the " +
            "non-interative mode will be used and output is written to the " +
            " specified output file.  Otherwise interative mode is assumed and " +
            " output is written to System.out.");

        System.exit(1);
   	 } // end of usage()


	/**
	 * Process the operation commands coming from inReader, and updates the
     * process queue according to the operations.
     *
	 * @param inReader Input reader where the operation commands are coming from.
	 * @param queue The queue structure which the operations are executed on.
	 * @param processOutWriter Where to send the results of running the commands.
     *
	 * @throws IOException Thrown if there is an I/O based exception.
	 */
	public static void processOperations(BufferedReader inReader, Runqueue queue, PrintWriter processOutWriter)
        throws IOException
    {
        String line;
        // current line number, which reflect how many commands have been entered.
        int lineNum = 1;
        boolean bQuit = false;

        // continue reading in commands until we either receive the quit signal
        // or there are no more input commands from input file
        while (!bQuit && (line = inReader.readLine()) != null) {
            String[] tokens = line.split(" ");

            // check if there is at least an operation command
            if (tokens.length < 1) {
                System.err.println(lineNum + ": not enough tokens.");
                lineNum++;
                continue;
            }

            String command = tokens[0];

            try {
                // determine which operation to execute
                switch (command.toUpperCase()) {
                    // add process to queue
                    case "EN":
                        if (tokens.length == 3) {
                            int vt = Integer.parseInt(tokens[2]);
                            if (vt < 0) {
                                System.err.println(lineNum + ": process run time must be non-negative.");
                            }
                            else {
                                queue.enqueue(tokens[1], vt);
                            }
                        }
                        else {
                            System.err.println(lineNum + ": incorrect number of tokens.");
                        }

                        break;
                    // remove highest priority process from the queue
                    case "DE":
                        if (tokens.length == 1) {
                            String procName = queue.dequeue();
				    		processOutWriter.println(procName);
                        }
                        else {
                            System.err.println(lineNum + ": incorrect number of tokens.");
                        }

                        break;
                    // find process
                    case "FP":
                        if (tokens.length == 2) {
                            boolean status = queue.findProcess(tokens[1]);
						    processOutWriter.println(status);
                        }
                        else {
                            System.err.println(lineNum + ": incorrect number of tokens.");
                        }

                        break;
                    // remove process from queue
                    case "RP":
                        if (tokens.length == 2) {
                            boolean status = queue.removeProcess(tokens[1]);
				    	    processOutWriter.println(status);
                        }
                        else {
                            System.err.println(lineNum + ": incorrect number of tokens.");
                        }

                        break;
                    // calculate preceding processes vt
                    case "PT":
                        if (tokens.length == 2) {
                            int value = queue.precedingProcessTime(tokens[1]);
				            processOutWriter.println(value);
                        }
                        else {
                            System.err.println(lineNum + ": incorrect number of tokens.");
                        }

                        break;
                    // calculate succeeding processes vt
                    case "ST":
                        if (tokens.length == 2) {
                            int value = queue.succeedingProcessTime(tokens[1]);
				            processOutWriter.println(value);
                        }
                        else {
                            System.err.println(lineNum + ": incorrect number of tokens.");
                        }

                        break;
                    // print all processes
                    case "PA":
                        queue.printAllProcesses(processOutWriter);
                        break;
                    // quit
                    case "Q":
                        bQuit = true;
                        break;
                    default:
                        System.err.println(lineNum + ": Unknown command.");
                } // end of switch
            }
            catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
            } // end of try-catch block

            lineNum++;
        } // end of while loop

	} // end of processOperations()


    /**
     * Main method.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {

        //
        // Parse command line arguments.
        //

        // if no arguments, we print out help message and exit
        if (args.length <= 0) {
            usage(progName);
        }

        // Implementation type
        String implementationType = args[0];

        // determine which implementation to test
        Runqueue queue = null;
        switch(implementationType) {
            case "array":
                queue = new OrderedArrayRQ();
                break;
            case "linkedlist":
                queue  = new OrderedLinkedListRQ();
                break;
            case "tree":
                queue = new BinarySearchTreeRQ();
                break;
            default:
                System.err.println("Unknown implmementation type.");
                usage(progName);
        }

        // Input file name.
        String commandInputFilename = null;
        // Output file name.
        String outputFilename = null;
        // Interactive mode flag (default is true or interactive mode)
        boolean bInteractive = true;

        // Check which mode we are running
        if (args.length >= 3) {
            // File input / output mode
            commandInputFilename = args[1];
            outputFilename = args[2];
            bInteractive = false;
        }
        else {
            // Interactive mode
            System.out.println("Running in Interative mode.");
        }


        // if not interactive mode
        if (!bInteractive) {
		    System.out.println("Loading commands from " + commandInputFilename + ".");

            try {
                assert(commandInputFilename != null && outputFilename != null);
                BufferedReader inReader = new BufferedReader(new FileReader(commandInputFilename));

                PrintWriter outWriter = new PrintWriter(new FileWriter(outputFilename), true);

                // process the operations
                processOperations(inReader, queue, outWriter);
            }
            catch (FileNotFoundException ex) {
                System.err.println("One of the specified files not found.");
            }
            catch(IOException ex) {
                System.err.println(ex.getMessage());
            } // end of try-catch block
        }
        else {
            // construct in and output streams/writers/readers, then process each operation.
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));

                PrintWriter outWriter = new PrintWriter(System.out, true);

                // process the operations
                processOperations(inReader, queue, outWriter);
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            } // end of try-catch block
        } // end of else

    } // end of main()

} // end of class RunqueueTester
