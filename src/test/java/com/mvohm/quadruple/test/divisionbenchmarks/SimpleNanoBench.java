package com.mvohm.quadruple.test.divisionbenchmarks;

import static com.mvohm.quadruple.test.AuxMethods.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Random;

import com.mvohm.quadruple.Quadruple;

/**
 * A simple Q&D hand-made benchmark for divisions,
 * with good old nanoseconds,
 * to estimate the reliability of a more serious benchmark
 *
 * Started 21.06.07 11:27:16
 *
 * @author M.Vokhmentev
 *
 */
public class SimpleNanoBench {

  // Time to run every benchmark in seconds
  private final static double WARMUP_TIME = 3.0;
  private final static double BENCH_TIME  = 5.0;

  private final static int DATA_SIZE      = 250_000;
                                          // = 500_000;
                                          // = 1_000_000;
                                          // = 0x20_0000;  // 2 097 152

  private static final int RAND_SEED      = 12345;
  private static final double RAND_SCALE  = 1e39; // To provide a sensible range of operands,
                                                  // so that the actual calculations don't get bypassed
  private final static BigDecimal[]
      bdOp1     = new BigDecimal[DATA_SIZE],
      bdOp2     = new BigDecimal[DATA_SIZE],
      bdResult  = new BigDecimal[DATA_SIZE];

  private final static Quadruple[]
      qdStorage = new Quadruple[DATA_SIZE],
      qdOp1     = new Quadruple[DATA_SIZE],
      qdOp2     = new Quadruple[DATA_SIZE],
      qdResult  = new Quadruple[DATA_SIZE];

  private static final MathContext MC_38 = new MathContext(38, RoundingMode.HALF_EVEN);

  public static void main(String[] args) {
    new SimpleNanoBench().run();
  }

  private final Benchmarker[] benchmarkers = {
    new bigDecimal____Addition_Meter(),
    new quadStatic____Addition_Meter(),
    new quadInstance__Addition_Meter(),

    new bigDecimal____Subtraction_Meter(),
    new quadStatic____Subtraction_Meter(),
    new quadInstance__Subtraction_Meter(),

    new bigDecimal____Multiplication_Meter(),
    new quadStatic____Multiplication_Meter(),
    new quadInstance__Multiplication_Meter(),

    new bigDecimal____Division_Meter(),
    new quadStatic____Division_Meter(),
    new quadInstance__Division_Meter(),
  };

  private void run() {
    initData();

    for (final Benchmarker b: benchmarkers)
      b.execute();

    say();
    for (final Benchmarker b: benchmarkers)
      b.report();

    say();
  }

  static abstract class Benchmarker {
    protected String benchmarkName;
    private double time;
    boolean needToUpdateData;

    String getName() { return benchmarkName; }

    private void execute() {
      say(benchmarkName);
      say_("Warming up:  ");
      runBenchmark(WARMUP_TIME);
      say_("Benchmarking:");
      time = runBenchmark(BENCH_TIME);
      say("Result: %8.3f ns/op\n", time);
    }

    private double runBenchmark(double during) {
      final long startTime = System.nanoTime();
      int iterationCount = 0;
      double runTime = 0;
      long t = 0;
      do {
        if (needToUpdateData) {
          prepareForNextIteration();
        }
        t -= System.nanoTime();
        doBenchmark();
        t += System.nanoTime();
        iterationCount++;

        if (iterationCount < 60) say_(".");
        else if (iterationCount == 60) say_(" etc... ");

        runTime = (System.nanoTime() - startTime) / 1e9;
      } while (runTime < during);
      say(" %s done.", iterationCount);
      final double workTime = t / ((double) DATA_SIZE * iterationCount);
      return workTime;
    }

    void report() {  say("%s: %8.3f ns/op", benchmarkName, time); }

    abstract void doBenchmark();



  } // static abstract class Benchmarker {


  class bigDecimal____Addition_Meter extends Benchmarker {
    { benchmarkName = "              BigDecimal Addition"; }

    @Override void doBenchmark() {
      for (int i = 0; i < DATA_SIZE; i++)
        bdResult[i] = bdOp1[i].add(bdOp2[i], MC_38);
    }
  };

  class quadStatic____Addition_Meter  extends Benchmarker {
    { benchmarkName = "        Quadruple static Addition"; }

    @Override void doBenchmark() {
      for (int i = 0; i < DATA_SIZE; i++)
        qdResult[i] = Quadruple.add(qdOp1[i], qdOp2[i]);
    }
  };

  class quadInstance__Addition_Meter  extends Benchmarker {
    {
      benchmarkName = "      Quadruple instance Addition";
      needToUpdateData = true;
    }

    @Override void doBenchmark() {
      for (int i = 0; i < DATA_SIZE; i++)
        qdOp1[i].add(qdOp2[i]);
    }
  };

  class bigDecimal____Subtraction_Meter  extends Benchmarker {
    { benchmarkName = "           BigDecimal Subtraction"; }

    @Override void doBenchmark() {
      for (int i = 0; i < DATA_SIZE; i++)
        bdResult[i] = bdOp1[i].subtract(bdOp2[i], MC_38);
    }
  };

  class quadStatic____Subtraction_Meter  extends Benchmarker {
    { benchmarkName = "     Quadruple static Subtraction"; }

    @Override void doBenchmark() {
      for (int i = 0; i < DATA_SIZE; i++)
        qdResult[i] = Quadruple.subtract(qdOp1[i], qdOp2[i]);
    }
  };

  class quadInstance__Subtraction_Meter  extends Benchmarker {
    {
      benchmarkName = "   Quadruple instance Subtraction";
      needToUpdateData = true;
    }

    @Override void doBenchmark() {
      for (int i = 0; i < DATA_SIZE; i++)
        qdOp1[i].subtract(qdOp2[i]);
    }
  };

  class bigDecimal____Multiplication_Meter  extends Benchmarker {
    { benchmarkName = "        BigDecimal Multiplication"; }

    @Override void doBenchmark() {
      for (int i = 0; i < DATA_SIZE; i++)
        bdResult[i] = bdOp1[i].multiply(bdOp2[i], MC_38);
    }
  };

  class quadStatic____Multiplication_Meter  extends Benchmarker {
    { benchmarkName = "  Quadruple static Multiplication"; }

    @Override void doBenchmark() {
      for (int i = 0; i < DATA_SIZE; i++)
        qdResult[i] = Quadruple.multiply(qdOp1[i], qdOp2[i]);
    }
  };

  class quadInstance__Multiplication_Meter  extends Benchmarker {
    {
      benchmarkName = "Quadruple instance Multiplication";
      needToUpdateData = true;
    }

    @Override void doBenchmark() {
      for (int i = 0; i < DATA_SIZE; i++)
        qdOp1[i].multiply(qdOp2[i]);
    }
  };


  class bigDecimal____Division_Meter  extends Benchmarker {
    { benchmarkName = "              BigDecimal division"; }

    @Override void doBenchmark() {
      for (int i = 0; i < DATA_SIZE; i++)
        bdResult[i] = bdOp1[i].divide(bdOp2[i], MC_38);
    }
  };

  class quadStatic____Division_Meter  extends Benchmarker {
    { benchmarkName = "      Quadruple static division  "; }

    @Override void doBenchmark() {
      for (int i = 0; i < DATA_SIZE; i++)
        qdResult[i] = Quadruple.divide(qdOp1[i], qdOp2[i]);
    }
  };

  class quadInstance__Division_Meter  extends Benchmarker {
    {
      benchmarkName = "    Quadruple instance division  ";
      needToUpdateData = true;
    }

    @Override void doBenchmark() {
      for (int i = 0; i < DATA_SIZE; i++)
        qdOp1[i].divide(qdOp2[i]);
    }
  };


/* */

  public static void initData() {
    final Random rand = new Random(RAND_SEED); // for reproducibility
    say("Rand seed: %,d ", RAND_SEED);
    say_("Data size: %,d ", DATA_SIZE);
    // 1/8 заполним случайными числами, а то сильно долго
    for (int i = 0; i < DATA_SIZE / 8; i++) {
      bdOp1[i] = randomBigDecimal(rand);
      bdOp2[i] = randomBigDecimal(rand);
      qdStorage[i] = randomQuadruple(rand);
      qdOp1[i] = qdStorage[i];
      qdOp2[i] = randomQuadruple(rand);
      if (i % ((DATA_SIZE / 8) / 10) == 0)
        say_(".");
    }

    // Остальные 7/8 заполним копиями первых 1/8
    for (int i = DATA_SIZE / 8; i < DATA_SIZE; i++) {
      bdOp1[i] = bdOp1[i % (DATA_SIZE / 8)];
      bdOp2[i] = bdOp2[i % (DATA_SIZE / 8)]; // Этим по барабану, они Immutable
      qdStorage[i] = new Quadruple(qdStorage[i % (DATA_SIZE / 8)]); // А эти будут изменяться в результате деления, все должны быть уникальные экземпляры
      qdOp1[i] = qdStorage[i];
      qdOp2[i] = qdOp2[i % (DATA_SIZE / 8)]; // Эти не меняются
    }
    say();
  }

  public static void prepareForNextIteration() {
    copyArray(qdStorage, qdOp1);
  }

  private static void copyArray(Quadruple[] src, Quadruple[] dst) {
    for (int i = 0; i < src.length; i++)
      dst[i] = new Quadruple(src[i]);
  }

  private static Quadruple randomQuadruple(Random rand) {
    return Quadruple.nextRandom(rand).multiply(RAND_SCALE);
  }

  private static BigDecimal randomBigDecimal(Random rand) {
    return Quadruple.nextRandom(rand).multiply(RAND_SCALE).bigDecimalValue();
  }


}
