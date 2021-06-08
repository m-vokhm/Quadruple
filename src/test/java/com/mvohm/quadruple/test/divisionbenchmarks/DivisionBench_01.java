package com.mvohm.quadruple.test.divisionbenchmarks;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Random;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.mvohm.quadruple.Quadruple;
import static com.mvohm.quadruple.test.AuxMethods.*;

@State(value = Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(java.util.concurrent.TimeUnit.NANOSECONDS)
@Fork(value = 1)
@Warmup(iterations = 3, time = 3)
@Measurement(iterations = 5, time = 6)
public class DivisionBench_01 {

  // To do BigDecimal arithmetic with the precision close to this of Quadruple
  private static final MathContext MC_38 = new MathContext(38, RoundingMode.HALF_EVEN);

  private static final int DATA_SIZE      // = 0x80_0000;  // 8 388 608;
                                          // =  0x1_0000;  // 65536
                                          // = 0x10_0000;  // 1 048 576
                                          = 0x20_0000;  // 2 097 152

  private static final int INDEX_MASK     = DATA_SIZE - 1;  // 0xFFFF
  private static final int RAND_SEED      = 12345;

  private static final double RAND_SCALE  = 1e39; // To provide a sensible range of operands,
                                                 // so that the actual calculations don't get bypassed

  private final BigDecimal[]
      bdOp1     = new BigDecimal[DATA_SIZE],
      bdOp2     = new BigDecimal[DATA_SIZE],
      bdResult  = new BigDecimal[DATA_SIZE];

  private final Quadruple[]
      qOp1_0    = new Quadruple[DATA_SIZE],
      qOp1      = new Quadruple[DATA_SIZE],
      qOp2      = new Quadruple[DATA_SIZE],
      qResult   = new Quadruple[DATA_SIZE];

  private int index = 0;

  private int invocationCount;
  private Blackhole bh;

  @Setup(Level.Trial)
  public void initPatterns(Blackhole bh) {
    this.bh = bh;
    final Random rand = new Random(RAND_SEED); // for reproducibility

    // 1/8 заполним случайными числами, а то сильно долго
    for (int i = 0; i < DATA_SIZE / 8; i++) {
      bdOp1[i] = randomBigDecimal(rand);
      bdOp2[i] = randomBigDecimal(rand);
      qOp1_0[i] = randomQuadruple(rand);
      qOp1[i] = qOp1_0[i];
      qOp2[i] = randomQuadruple(rand);
      if (i % ((DATA_SIZE / 8) / 10) == 0)
        say_(".");
    }

    // Остальные 7/8 заполним копиями первых 1/8
    for (int i = DATA_SIZE / 8; i < DATA_SIZE; i++) {
      bdOp1[i] = bdOp1[i % (DATA_SIZE / 8)];
      bdOp2[i] = bdOp2[i % (DATA_SIZE / 8)]; // Этим по барабану, они Immutable
      qOp1_0[i] = new Quadruple(qOp1_0[i % (DATA_SIZE / 8)]); // А эти будут изменяться в результате деления, все должны быть уникальные экземпляры
      qOp1[i] = qOp1_0[i];
      qOp2[i] = qOp2[i % (DATA_SIZE / 8)]; // Эти не меняются
    }
    say_("! ");

  }

//  @Setup(Level.Iteration)
//  public void initData() {
//    say_("-");
//    copyArray(qOp1_0, qOp1);
//    say_("- ");
//    invocationCount = 0;
//  }

  private void copyArray(Quadruple[] src, Quadruple[] dst) {
    for (int i = 0; i < src.length; i++)
      dst[i] = new Quadruple(src[i]);
  }

  private static Quadruple randomQuadruple(Random rand) {
    return Quadruple.nextRandom(rand).multiply(RAND_SCALE);
  }

  private static BigDecimal randomBigDecimal(Random rand) {
    return Quadruple.nextRandom(rand).multiply(RAND_SCALE).bigDecimalValue();
  }


  @Benchmark
  public void g_BigDecimal_Division() {
    bh.consume(bdResult[index] = bdOp1[index].divide(bdOp2[index], MC_38));
    index = ++index & INDEX_MASK;
//    if (index == 0)
//      say("%6d", invocationCount++);

//      if (index < 10) System.out.format("q[%2d] = %s\n", index, bdResult[index]);
  }

  @Benchmark
  public void h_Quadruple__Division() {

  // Instance operation:
  // qResult[index] = qOp1[index].divide(qOp2[index]);

  // Static class operation:
    bh.consume(qResult[index] = Quadruple.divide(qOp1[index], qOp2[index]));

    index = ++index & INDEX_MASK;
//    if (index == 0)
//      say("%6d", invocationCount++);

//      if (index < 10) System.out.format("q[%2d] = %s\n", index, qResult[index]);
  }

  /**
   * @param args
   * @throws IOException
   */
  private void run(String... args) throws IOException, RunnerException {
//    final Random rand = new Random(123);
//    for (int i = 0; i < 20; i++) {
//      final BigDecimal bd1 =  randomBigDecimal(rand);
//      final BigDecimal bd2 =  randomBigDecimal(rand);
//      final BigDecimal bd = bd1.divide(bd2, MC_38);
//      System.out.printf("%s / %s = %s\n", bd1, bd2, bd);
//    }
//    System.exit(0);
    final Options opt = new OptionsBuilder()
        .include(DivisionBench_01.class.getSimpleName())
        .forks(1)
        .build();
    new Runner(opt).run();
  }

  public static void main(String... args) throws IOException, RunnerException {
    new DivisionBench_01().run(args);
  }


}
