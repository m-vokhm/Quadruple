package com.mvohm.quadruple.test;

import static com.mvohm.quadruple.test.AuxMethods.say;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.test.TesterClasses.QuadTester;
import com.mvohm.quadruple.test.TesterClasses.Verbosity;

/**
  A concrete subclass of QuadTester designed to verify thread safety of the Quadruple class.
  It generates two arrays of random Quadruple values (args1 and args2)
  and an array of string representations of random Quadruple values.
  Basic operations — addition, subtraction, multiplication, division, square root, string conversion,
  and string parsing — are first performed in a single-threaded context to obtain the expected results.
  The same operations are then executed concurrently across multiple threads, with each thread
  storing its results in separate arrays.
  Finally, the results from the multithreaded execution are compared
  against the single-threaded baseline to ensure consistency and correctness.
  */

public class ThreadSafetyTester extends QuadTester{

  private static final int RAND_SEED = 12345;
  private static final int TIMEOUT = 15; // Allow to run for 15 s

  private Verbosity verbosity;
  private int dataSize = 0;
  private int nunumberOfThreads;

  private Object[] threadRefs;
  private ExecutorService executor;

  static Quadruple[] args1;
  static Quadruple[] args2;
  static String[] strings;

  class ResultSet {
    int size;
    Quadruple[] additionResults;
    Quadruple[] subtractionResults;
    Quadruple[] multiplicationResults;
    Quadruple[] divisionResults;
    Quadruple[] sqrtResults;
    Quadruple[] parseResults;
    String[] toStringResults;

    enum OPCODES {
      ADDITION, SUBTRACTION, MULTIPLICATIOIN, DIVISION, SQRT, PARSE, TOSTRING,
    };

    List<OPCODES> operations;
    String signature = "";

    ResultSet(int size) {
      this.size = size;
      additionResults       = new Quadruple[size];
      subtractionResults    = new Quadruple[size];
      multiplicationResults = new Quadruple[size];
      divisionResults       = new Quadruple[size];
      sqrtResults           = new Quadruple[size];
      parseResults          = new Quadruple[size];
      toStringResults       = new String[size];

      // To provide different order of performing operations
      operations = Arrays.asList(OPCODES.values());
      Collections.shuffle(operations);
    }

    void computeResults() {
      signature = Thread.currentThread().getName();
      if (signature.length() > 4) {
        signature = String.format("%-9s", signature.substring(signature.indexOf("thread")));
      }
      for(final OPCODES opcode: operations) {
        switch (opcode) {
          case ADDITION:
            performAdditions(); break;
          case SUBTRACTION:
            performSubtractions(); break;
          case MULTIPLICATIOIN:
            performMultiplications(); break;
          case DIVISION:
            performDivisions(); break;
          case SQRT:
            performSqrt(); break;
          case PARSE:
            performParse(); break;
          case TOSTRING:
            performToString(); break;
        }
      }
    }

    private void performAdditions() {
      for (int i = 0; i < size; i++) {
        additionResults[i] = Quadruple.add(args1[i], args2[i]);
      }
      sayIfVerbose(signature + " done addition");
    }

    private void performSubtractions() {
      for (int i = 0; i < size; i++) {
        subtractionResults[i] = Quadruple.subtract(args1[i], args2[i]);
      }
      sayIfVerbose(signature + " done subtraction");
    }

    private void performMultiplications() {
      for (int i = 0; i < size; i++) {
        multiplicationResults[i] = Quadruple.multiply(args1[i], args2[i]);
      }
      sayIfVerbose(signature + " done multiplication");
    }

    private void performDivisions() {
      for (int i = 0; i < size; i++) {
        divisionResults[i] =  Quadruple.divide(args1[i], args2[i]);
      }
      sayIfVerbose(signature + " done division");
    }

    private void performSqrt() {
      for (int i = 0; i < size; i++) {
        sqrtResults[i] = Quadruple.sqrt(args1[i]);
      }
      sayIfVerbose(signature + " done sqrt");
    }

    private void performParse() {
      for (int i = 0; i < size; i++) {
        parseResults[i] = new Quadruple(strings[i]);
      }
      sayIfVerbose(signature + " done parse");
    }

    private void performToString() {
      for (int i = 0; i < size; i++) {
        toStringResults[i] = args1[i].toString();
      }
      sayIfVerbose(signature + " done toString()");
    }

    public int verifyAdditionResults(Quadruple[] additionResults2) {
      final int errorCount = compareArtrays(additionResults, additionResults2);
      if (errorCount != 0) {
        sayIfVerbose("Addition errors:       %-6s", errorCount);
      }
      return errorCount;
    }

    public int verifySubtractionResults(Quadruple[] subtractionResults2) {
      final int errorCount = compareArtrays(subtractionResults, subtractionResults2);
      if (errorCount != 0) {
        sayIfVerbose("Subtraction errors:    %-6s", errorCount);
      }
      return errorCount;
    }

    public int verifyMultiplicationResults(Quadruple[] multiplicationResults2) {
      final int errorCount = compareArtrays(multiplicationResults, multiplicationResults2);
      if (errorCount != 0) {
        sayIfVerbose("Multiplication errors: %-6s", errorCount);
      }
      return errorCount;
    }

    public int verifyDivisionResults(Quadruple[] divisionResults2) {
      final int errorCount = compareArtrays(divisionResults, divisionResults2);
      if (errorCount != 0) {
        sayIfVerbose("Division errors:       %-6s", errorCount);
      }
      return errorCount;
    }

    public int verifySqrtResults(Quadruple[] sqrtResults2) {
      final int errorCount = compareArtrays(sqrtResults, sqrtResults2);
      if (errorCount != 0) {
        sayIfVerbose("sqrt() errors:         %-6s", errorCount);
      }
      return errorCount;
    }

    public int verifyParseResults(Quadruple[] parseResults2) {
      final int errorCount = compareArtrays(parseResults, parseResults2);
      if (errorCount != 0) {
        sayIfVerbose("parse errors:          %-6s", errorCount);
      }
      return errorCount;
    }

    public int verifytoStringResults(String[] toStringResults2) {
      final int errorCount = compareArtrays(toStringResults, toStringResults2);
      if (errorCount != 0) {
        sayIfVerbose("Tostring errors:       %-6s", errorCount);
      }
      return errorCount;
    }

    private int compareArtrays(Quadruple[] array1, Quadruple[] array2) {
      int errorCount = 0;
      for (int i = 0; i < array1.length; i++) {
        if (!array1[i].equals(array2[i])
            && !(array1[i].isNaN() && array2[i].isNaN())
            ) {
          errorCount++;
        }
      }
      return errorCount;
    }

    private int compareArtrays(String[] array1, String[] array2) {
      int errorCount = 0;
      for (int i = 0; i < array1.length; i++) {
        if (!array1[i].equals(array2[i])) {
          errorCount++;
        }
      }
      return errorCount;
    }

  }

  ResultSet model;

  public ThreadSafetyTester(int dataSize, int numberOfThreads) {
    this.dataSize = dataSize;
    this.nunumberOfThreads = numberOfThreads;
    generateSourceData(dataSize);

    model = new ResultSet(dataSize);
    model.computeResults();

    threadRefs = new Object[nunumberOfThreads];
    executor = Executors.newFixedThreadPool(numberOfThreads);
  }

  public void init(int dataSize, int numberOfThreads) {
    this.dataSize = dataSize;
    this.nunumberOfThreads = numberOfThreads;
    generateSourceData(dataSize);

    model = new ResultSet(dataSize);
    model.computeResults();

    threadRefs = new Object[nunumberOfThreads];
    executor = Executors.newFixedThreadPool(numberOfThreads);

  }

  @Override
  public TestResults test() {
    this.verbosity = TesterClasses.getVerbosity();
    say(getHeader());

    results = new TestResults(verbosity, dataSize);

    final ExecutorService executor = Executors.newFixedThreadPool(nunumberOfThreads);
    final List<Future<ResultSet>> futures = runConcurentComputations(executor);

    executor.shutdown();
    waitForCompletion(executor);

    final Map<String, Integer> errors = verifyResults(futures, model);
    final int totalErrorCount = countErrors(errors);
    results.setErrorCount(totalErrorCount);
    results.summarize();

    if (verbosity != Verbosity.SILENT) {
      say(results.getReport("testing " + getName()));
      say("====\n");
    }
    return results;
  }

  private int countErrors(Map<String, Integer> errors) {
    int errCount = 0;
    for (final int value: errors.values()) {
      errCount += value;
    }
    return errCount;
  }

  /** Builds and returns a header for the report including the specific test name,
   * provided by {@link #getName()}, and the error threshold value. */
  private String getHeader() {
    return String.format("Testing thread safety \non %s samples with %s threads\n", dataSize, nunumberOfThreads);
  }

  @Override
  protected String getName() {
    return String.format("thread safety on %s threads", nunumberOfThreads);
  }

  @Override
  protected List<String[]> getTestDataList() {
    // TODO Auto-generated method stub. Never used, actually.
    return null;
  }

  @Override
  protected void testOp(String[] dataSample) {
    // TODO Auto-generated method stub. Never used, actually.
  }

  //**************************************************************
  //*** Private methods

  private void generateSourceData(int dataSize) {
    args1 = new Quadruple[dataSize];
    args2 = new Quadruple[dataSize];
    strings = new String[dataSize];
    final Random random = new Random(RAND_SEED);
    for (int i = 0; i < dataSize; i++) {
      args1[i] = Quadruple.nextRandom(random).multiply(Math.pow(2.0, random.nextInt(-1024, 1024)));
      args2[i] = Quadruple.nextRandom(random).multiply(Math.pow(2.0, random.nextInt(-1024, 1024)));
      Quadruple qq;
      if (random.nextInt() % 111 == 0) {
        qq = Quadruple.nan();
      } else if (random.nextInt() % 107 == 0) {
        qq = (random.nextBoolean())? Quadruple.positiveInfinity() : Quadruple.negativeInfinity();
      } else {
        qq = Quadruple.nextRandom(random).multiply(Math.pow(2.0, random.nextInt(-1024, 1024)));
      }
      final String s = qq.toString();
      strings[i] = s;
    }
  }

  private List<Future<ResultSet>> runConcurentComputations(ExecutorService executor) {
    final List<Future<ResultSet>> futures = new ArrayList<>();
    for (int i = 0; i < nunumberOfThreads; i++) {
      final AtomicReference<Thread> threadRef = new AtomicReference<>();
      futures.add(executor.submit(() -> {
        threadRef.set(Thread.currentThread());
        final ResultSet result= new ResultSet(dataSize);
        result.computeResults();
        return result;
      }));
      threadRefs[i] = threadRef;
    }
    return futures;
  }

  private void waitForCompletion(final ExecutorService executor) {
    sayIfVerbose("Waiting for completion...");
    try {
      final boolean tasksFinished = executor.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
      if (tasksFinished) {
        sayIfVerbose("All tasks completed within the timeout.");
      } else {
        sayIfVerbose("Timeout occurred before all tasks finished.");
        executor.shutdownNow();
        for (final Object obj: threadRefs) {
          @SuppressWarnings("unchecked")
          final AtomicReference<Thread> threadRef = (AtomicReference<Thread>) obj;
          final Thread thread = threadRef.get();
          if (thread.getState() == Thread.State.RUNNABLE) {
            try {
              thread.interrupt();
            } catch (final Exception x) { x.printStackTrace(); }
          }
        }
      }
    } catch (final InterruptedException e) {
      System.out.println("Await termination interrupted.");
      Thread.currentThread().interrupt();
    }
  }

  private TreeMap<String, Integer>  verifyResults(List<Future<ResultSet>> futures, ResultSet model) {
    sayIfVerbose("---------------------------");
    final TreeMap<String, Integer> errorMap = new TreeMap<>();

    final int errors = 0;
    int unavailable = 0;
    for (final Future<ResultSet> future: futures) {
      ResultSet result = null;

      try {
        result = future.get();
      } catch (InterruptedException | ExecutionException x) { x.printStackTrace(); }

      if (result != null) {
        sayIfVerbose("Verifing " + result.signature + " results:");
        addErrors(verify(result, model), errorMap);
      } else {
        sayIfVerbose("Could not get the result...");
        unavailable += 1;
      }
      sayIfVerbose();
    }
    if (unavailable != 0) {
      errorMap.put("Unavailable results", unavailable);
      sayIfVerbose("%s results are unavailable", unavailable);
    }
    return errorMap;
  }

  private static void addErrors(TreeMap<String, Integer> summand, TreeMap<String, Integer> summary) {
    for (final String key: summand.keySet()) {
      summary.put(key, summary.getOrDefault(key, 0) + summand.get(key));
    }
  }

  private TreeMap<String, Integer> verify(ResultSet result, ResultSet model) {
    int errors = 0;
    final TreeMap<String, Integer> errorMap = new TreeMap<>();

    int err = model.verifyAdditionResults(result.additionResults);
    if (err != 0) {
      errors += err;
      errorMap.put("Addition", err);
    }

    err = model.verifySubtractionResults(result.subtractionResults);
    if (err != 0) {
      errors += err;
      errorMap.put("Subtraction", err);
    }

    err = model.verifyMultiplicationResults(result.multiplicationResults);
    if (err != 0) {
      errors += err;
      errorMap.put("Multiplication", err);
    }

    err = model.verifyDivisionResults(result.divisionResults);
    if (err != 0) {
      errors += err;
      errorMap.put("Division", err);
    }

    err = model.verifySqrtResults(result.sqrtResults);
    if (err != 0) {
      errors += err;
      errorMap.put("Sqrt", err);
    }

    err = model.verifyParseResults(result.parseResults);
    if (err != 0) {
      errors += err;
      errorMap.put("Parse", err);
    }

    err = model.verifytoStringResults(result.toStringResults);
    if (err != 0) {
      errors += err;
      errorMap.put("toString", err);
    }

    if (errors == 0) {
      sayIfVerbose("OK.");
    }
    return errorMap;
  }

  private void sayIfVerbose() {
    if (verbosity == Verbosity.TALKATIVE) {
      say();
    }
  }

  private void sayIfVerbose(String s) {
    if (verbosity == Verbosity.TALKATIVE) {
      say(s);
    }
  }

  private void sayIfVerbose(String format, Object... args) {
    if (verbosity == Verbosity.TALKATIVE) {
      say(format, args);
    }
  }

}
