package com.mvohm.quadruple.test.divisionbenchmarks;

import static com.mvohm.quadruple.test.AuxMethods.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Random;

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.research.Dividers;

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
public class SimpleBench {

  // Time to run every benchmark in seconds
  private final static double WARMUP_TIME = 5.0;
  private final static double BENCH_TIME  = 10.0;

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
    new SimpleBench().run();
  }

  private void run() {
    initData();
    say();
//    bdAdditionBenchmarker.execute();
//    quadStaticAdditionBenchmarker.execute();
//    quadInstanceAdditionBenchmarker.execute();
//    bdDivisionMeter.execute();
//    quadStaticDivision_0_Meter.execute();
    quadStaticDivision_1_Meter.execute();
    quadStaticDivision_2_Meter.execute();
    quadStaticDivision_3_Meter.execute();
//    say("Add back counter: %s\n", Dividers.getMbiAddBackCounter());
//    quadInstanceDivisionBenchmarker.execute();
//    quadInstanceAltDivisionBenchmarker.execute();
//    benchmarkQStatic();
//    benchmarkQInstance();
  }


  static abstract class Benchmarker {
    String benchmarkName;
    double time;
    boolean needToUpdateData;

    private void execute() {
      say(benchmarkName);
      say_("Warming up:  ");
      runBenchmark(WARMUP_TIME);
      say_("Benchmarking:");
      time = runBenchmark(BENCH_TIME);
      say("%s: %8.3f ns/op\n", benchmarkName, time);
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
        say_(".");
        runTime = (System.nanoTime() - startTime) / 1e9;
      } while (runTime < during);
      say(" %s done.", iterationCount);
      final double workTime = t / ((double) DATA_SIZE * iterationCount);
      return workTime;
    }

    abstract void doBenchmark();

  } // static abstract class Benchmarker {

  Benchmarker bdDivisionMeter = new Benchmarker() {
    { benchmarkName = "          BigDecimal division"; }

    @Override void doBenchmark() {
      for (int i = 0; i < DATA_SIZE; i++)
        bdResult[i] = bdOp1[i].divide(bdOp2[i], MC_38);
    }
  };

  Benchmarker quadStaticDivision_0_Meter = new Benchmarker() {
    { benchmarkName = "  Quadruple static division 0"; }

    @Override void doBenchmark() {
      for (int i = 0; i < DATA_SIZE; i++)
        qdResult[i] = Quadruple.divide_0(qdOp1[i], qdOp2[i]);
    }
  };

  Benchmarker quadStaticDivision_1_Meter = new Benchmarker() {
    { benchmarkName = "  Quadruple static division 1"; }

    @Override void doBenchmark() {
      for (int i = 0; i < DATA_SIZE; i++)
        qdResult[i] = Quadruple.divide_1(qdOp1[i], qdOp2[i]);
    }
  };

  Benchmarker quadStaticDivision_2_Meter = new Benchmarker() {
    { benchmarkName = "  Quadruple static division 2"; }

    @Override void doBenchmark() {
      for (int i = 0; i < DATA_SIZE; i++)
        qdResult[i] = Quadruple.divide_2(qdOp1[i], qdOp2[i]);
    }
  };

  Benchmarker quadStaticDivision_3_Meter = new Benchmarker() {
    { benchmarkName = "  Quadruple static division 3"; }

    @Override void doBenchmark() {
      for (int i = 0; i < DATA_SIZE; i++)
        qdResult[i] = Quadruple.divide_3(qdOp1[i], qdOp2[i]);
    }
  };

  Benchmarker quadInstanDivision_0_Meter = new Benchmarker() {
    {
      benchmarkName = "Quadruple instance division 0";
      needToUpdateData = true;
    }

    @Override void doBenchmark() {
      for (int i = 0; i < DATA_SIZE; i++)
        qdOp1[i].divide_0(qdOp2[i]);
    }
  };

  Benchmarker quadInstanDivision_1_Meter = new Benchmarker() {
    {
      benchmarkName = "Quadruple instance division 1";
      needToUpdateData = true;
    }

    @Override void doBenchmark() {
      for (int i = 0; i < DATA_SIZE; i++)
        qdOp1[i].divide_1(qdOp2[i]);
    }
  };

  Benchmarker quadInstanDivision_2_Meter = new Benchmarker() {
    {
      benchmarkName = "Quadruple instance division 2";
      needToUpdateData = true;
    }

    @Override void doBenchmark() {
      for (int i = 0; i < DATA_SIZE; i++)
        qdOp1[i].divide_2(qdOp2[i]);
    }
  };

  Benchmarker quadInstanDivision_3_Meter = new Benchmarker() {
    {
      benchmarkName = "Quadruple instance division 3";
      needToUpdateData = true;
    }

    @Override void doBenchmark() {
      for (int i = 0; i < DATA_SIZE; i++)
        qdOp1[i].divide_3(qdOp2[i]);
    }
  };



  Benchmarker bdAdditionBenchmarker = new Benchmarker() {
    { benchmarkName = "        BigDecimal Addition"; }

    @Override void doBenchmark() {
      for (int i = 0; i < DATA_SIZE; i++)
        bdResult[i] = bdOp1[i].add(bdOp2[i], MC_38);
    }
  };

  Benchmarker quadStaticAdditionBenchmarker = new Benchmarker() {
    { benchmarkName = "  Quadruple static Addition"; }

    @Override void doBenchmark() {
      for (int i = 0; i < DATA_SIZE; i++)
        qdResult[i] = Quadruple.add(qdOp1[i], qdOp2[i]);
    }
  };

  Benchmarker quadInstanceAdditionBenchmarker = new Benchmarker() {
    {
      benchmarkName = "Quadruple instance Addition";
      needToUpdateData = true;
    }

    @Override void doBenchmark() {
      for (int i = 0; i < DATA_SIZE; i++)
        qdOp1[i].add(qdOp2[i]);
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
    say_("-");
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
