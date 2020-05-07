import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Bobin Yuan
 * @email bobin.yuan@rmit.edu.au
 *
 * This evaluation program requires the JUnit 5.4 and Apache POI 4.1.2
 * https://poi.apache.org/download.html
 *
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(ExecutionMode.CONCURRENT)
public class DataStructureEval {
    /**
     * This formatter is used to generate the Excel file using timestamp
     */
    private static final DateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH-mm");

    /**
     * Number of the test cases of each type of queue
     * Also indicates the last row number in the Excel file
     */
    private static final int VOLUME_PRE_TYPE = 100;

    /**
     * Store all the Runqueue Implementations
     * e.g BinarySearchTreeRQ/OrderedArrayRQ/OrderedLinkedListRQ
     */
    private static List<String> typeNameList;

    /**
     * Use volume and increase interval to generate size list
     */
    private static List<Integer> TEST_SIZE_LIST;

    /**
     * Store all the generated processes before test
     */
    private static HashMap<Integer, List<ProcTest>> processPool;

    /**
     * Store all the Runqueue when Test 1 is finished, in order to boost the test performance for Test 2, 3, 4
     * Which also raise up a problem that Test 2, 3, 4 depends on Test 1, so that Test 1 need to be executed first
     */
    private static HashMap<String, HashMap<Integer, Runqueue>> queuePool;

    /**
     * Key = Test Scenario Number
     * Value = HashMap<Implementation Name, TimeCost List>
     * Implementation Name includes (e.g. bst/array/linked-list)
     */
    private static HashMap<Integer, HashMap<String, List<Long>>> timeTableMap;

    @BeforeAll
    static void beforeAll() {
        typeNameList = new ArrayList<>();
        typeNameList.add(BinarySearchTreeRQ.class.getSimpleName());
        typeNameList.add(OrderedArrayRQ.class.getSimpleName());
        typeNameList.add(OrderedLinkedListRQ.class.getSimpleName());

        // Increase rate -> 50% and start from 10 with limited "VOLUME_PRE_TYPE" amount of iterations
        TEST_SIZE_LIST = IntStream.iterate(50, i -> i + 50)
                .limit(VOLUME_PRE_TYPE).boxed().collect(Collectors.toList());

        //Generate random processes for test
        processPool = new HashMap<>(VOLUME_PRE_TYPE);
        TEST_SIZE_LIST.forEach(n -> {
            //The list initial size is exactly the same with n
            List<ProcTest> tmp = new ArrayList<>(n);
            Random tmpRandom = new Random();
            for (int i = 0; i < n; i++) {
                // Process label vary from P0 to P9999
                String label = "P" + tmpRandom.nextInt(10000);
                // Process run-time vary from 1 to 100
                int vt = 1 + tmpRandom.nextInt(100);
                tmp.add(new ProcTest(label, vt));
            }
            // Make sure that every same size test case has the same test processes list
            processPool.put(n, tmp);
        });

        //Generate the queue pool to prepare to store the processed enqueue in the Test 1
        queuePool = new HashMap<>(typeNameList.size());
        typeNameList.forEach(impl -> {
            queuePool.put(impl, new HashMap<>(VOLUME_PRE_TYPE));
        });

        //Init the timeTableMap
        timeTableMap = new HashMap<>(4);
        timeTableMap.put(1, new HashMap<>(typeNameList.size()));
        timeTableMap.put(2, new HashMap<>(typeNameList.size()));
        timeTableMap.put(3, new HashMap<>(typeNameList.size()));
        timeTableMap.put(4, new HashMap<>(typeNameList.size()));
        timeTableMap.forEach((k, v) -> {
            typeNameList.forEach(impl -> {
                v.put(impl, new ArrayList<>());
            });
        });
    }

    @AfterAll
    static void afterAll() throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        // Map to the Test 1
        XSSFSheet test1 = wb.createSheet("Scenario 1");
        // Map to the Test 2
        XSSFSheet test2 = wb.createSheet("Scenario 3");
        // Map to the Test 3
        XSSFSheet test3 = wb.createSheet("Scenario 2.1");
        // Map to the Test 4
        XSSFSheet test4 = wb.createSheet("Scenario 2.2");

        List<XSSFSheet> sheetList = new ArrayList<>();
        sheetList.add(test1);
        sheetList.add(test2);
        sheetList.add(test3);
        sheetList.add(test4);

        for (int i = 0; i < sheetList.size(); i++) {
            XSSFSheet sheet = sheetList.get(i);
            XSSFRow firstRow = sheet.createRow(0);
            for (int j = 0; j < typeNameList.size(); j++) {
                firstRow.createCell(j + 1).setCellValue(typeNameList.get(j));
            }
            for (int j = 0; j < TEST_SIZE_LIST.size(); j++) {
                XSSFRow newRow = sheet.createRow(j + 1);
                newRow.createCell(0).setCellValue(TEST_SIZE_LIST.get(j));
                for (int k = 0; k < typeNameList.size(); k++) {
                    long timeCost = timeTableMap.get(i + 1).get(typeNameList.get(k)).get(j);
                    newRow.createCell(k + 1).setCellValue(timeCost);
                }
            }
            //Auto size the column to make the data frame look prettier
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);
            sheet.autoSizeColumn(3);

            //Generate the graph
            XSSFDrawing drawing = sheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 5, 1, 20, 21);

            XSSFChart chart = drawing.createChart(anchor);
            chart.setTitleText(sheet.getSheetName());

            XDDFChartLegend legend = chart.getOrAddLegend();
            legend.setPosition(LegendPosition.RIGHT);

            XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
            bottomAxis.setTitle("Runqueue Process Amount");

            XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
            leftAxis.setTitle("Time Cost(nano seconds)");

            int lastRowNum = TEST_SIZE_LIST.size();
            XDDFNumericalDataSource<Double> size = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                    new CellRangeAddress(1, lastRowNum, 0, 0));

            XDDFNumericalDataSource<Double> tree = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                    new CellRangeAddress(1, lastRowNum, 1, 1));

            XDDFNumericalDataSource<Double> array = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                    new CellRangeAddress(1, lastRowNum, 2, 2));

            XDDFNumericalDataSource<Double> linkedlist = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                    new CellRangeAddress(1, lastRowNum, 3, 3));


            XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);

            XDDFLineChartData.Series series1 = (XDDFLineChartData.Series) data.addSeries(size, tree);
            series1.setTitle("BST", null);
            series1.setSmooth(true);
            series1.setMarkerStyle(MarkerStyle.NONE);

            XDDFLineChartData.Series series2 = (XDDFLineChartData.Series) data.addSeries(size, array);
            series2.setTitle("Array", null);
            series2.setSmooth(true);
            series2.setMarkerStyle(MarkerStyle.NONE);

            XDDFLineChartData.Series series3 = (XDDFLineChartData.Series) data.addSeries(size, linkedlist);
            series3.setTitle("LinkedList", null);
            series3.setSmooth(true);
            series3.setMarkerStyle(MarkerStyle.NONE);

            chart.plot(data);
        }

        String timeStamp = SDF.format(new Date()) + ".xlsx";
        try (OutputStream fileOut = new FileOutputStream(timeStamp)) {
            wb.write(fileOut);
        }
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Order(1)
    @DisplayName("Scenario 1 Growing runqueue (Enqueue)")
    @ParameterizedTest(name = "{index} => Queue Type = {0}")
    @MethodSource("sizeProvider")
    void enqueue(String name, int size) {
        Runqueue test = null;
        switch (name) {
            case "BinarySearchTreeRQ":
                test = new BinarySearchTreeRQ();
                break;
            case "OrderedArrayRQ":
                test = new OrderedArrayRQ();
                break;
            case "OrderedLinkedListRQ":
                test = new OrderedLinkedListRQ();
                break;
            default:
                break;
        }
        System.out.printf("Queue Type: %s, Enqueue Size: %s\n", name, size);
        List<ProcTest> preEnqueue = processPool.get(size);

        assert test != null;
        long totalTime = 0L;
        for (ProcTest proc : preEnqueue) {
            String label = proc.label;
            int vt = proc.vt;
            long startTime = System.nanoTime();
            test.enqueue(label, vt);
            long endTime = System.nanoTime();
            totalTime += endTime - startTime;
            //Should return true as the process with this label is already enqueue
            assertTrue(test.findProcess(label));
        }
        //Store the queue for further use
        queuePool.get(name).put(size, test);
        System.out.println("Time Cost: " + totalTime);

        //Store the time into the timeTableMap
        timeTableMap.get(1).get(name).add(totalTime);
    }

    @Order(3)
    @DisplayName(" Scenario 2.1 Shrinking Runqueue (Dequeue Once)")
    @ParameterizedTest(name = "{index} => Queue Type = {0}")
    @MethodSource("sizeProvider")
    void dequeue1(String name, int size) {
        Runqueue test;
        test = queuePool.get(name).get(size);

        long startTime = System.nanoTime();
        String deLabel = test.dequeue();
        long totalTime = System.nanoTime() - startTime;
        //Should return false as the process with this label is already dequeue
        assertFalse(test.findProcess(deLabel));
        System.out.printf("Dequeue 1 Node Time Cost: %s\n", totalTime);
        //Store the time into the timeTableMap
        timeTableMap.get(3).get(name).add(totalTime);
    }

    @Order(4)
    @DisplayName(" Scenario 2.2 Shrinking runqueue (Dequeue All)")
    @ParameterizedTest(name = "{index} => Queue Type = {0}")
    @MethodSource("sizeProvider")
    void dequeue2(String name, int size) {
        Runqueue test;
        test = queuePool.get(name).get(size);

        int deAmount = size >> 1;
        System.out.printf("Queue Type: %s, Dequeue Size: %s\n", name, deAmount);

        long totalTime = 0L;
        for (int i = 0; i < deAmount; i++) {
            long startTime = System.nanoTime();
            String deLabel = test.dequeue();
            long endTime = System.nanoTime();
            totalTime += endTime - startTime;
            //Should return false as the process with this label is already dequeue
            assertFalse(test.findProcess(deLabel));
        }
        System.out.printf("Dequeue %s Nodes Time Cost: %s\n", deAmount, totalTime);
        //Store the time into the timeTableMap
        timeTableMap.get(4).get(name).add(totalTime);
    }

    @Order(2)
    @DisplayName("Scenario 3 Calculating total v-runtime of proceeding processes")
    @ParameterizedTest(name = "{index} => Queue Type = {0}")
    @MethodSource("sizeProvider")
    void proceeding(String name, int size) {
        Runqueue test;
        test = queuePool.get(name).get(size);
        //Create a magic process for dequeue once test
        String magicLabel = "P99999";
        test.enqueue(magicLabel, 150);

        long startTime = System.nanoTime();
        test.precedingProcessTime(magicLabel);
        long totalTime = System.nanoTime() - startTime;
        System.out.println("Time Cost: " + totalTime);

        //Remove the magic Process to avoid any other unpredictable impact
        test.removeProcess(magicLabel);
        //Should return false as the magic Process has already been removed.
        assertFalse(test.findProcess(magicLabel));
        //Store the time into the timeTableMap
        timeTableMap.get(2).get(name).add(totalTime);
    }

    private static Stream<Arguments> sizeProvider() {
        List<Arguments> arguments = new ArrayList<>();
        for (String typeName : typeNameList) {
            for (Integer size : TEST_SIZE_LIST) {
                arguments.add(Arguments.of(typeName, size));
            }
        }
        return arguments.stream();
    }

    /**
     * This class i used to store the generated processes' label and run-time
     * So it doesn't need to relay on any other class
     */
    private static class ProcTest {
        String label;
        int vt;

        public ProcTest(String label, int vt) {
            this.label = label;
            this.vt = vt;
        }
    }
}

