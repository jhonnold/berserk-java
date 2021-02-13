package me.honnold.berserk.tune;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.honnold.berserk.board.Position;
import me.honnold.berserk.eval.Piece;
import me.honnold.berserk.eval.PositionEvaluations;
import me.honnold.berserk.util.EPD;

public class TuneUp {
    public static final int THREAD_COUNT = 16;
    private final ExecutorService service = Executors.newFixedThreadPool(THREAD_COUNT);
    private final int[] sideScalar = {1, -1};
    public double K = -1.121;
    private List<EPD.EPDTexel> scores;
    private PositionEvaluations evals = PositionEvaluations.getInstance();

    public TuneUp() throws IOException {
        scores =
                EPD.loadTexel(
                        "C:\\Users\\jayho\\iCloudDrive\\Documents\\Programming\\chess\\epds\\texel\\quiet.epd");
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        TuneUp tuneUp = new TuneUp();
        //        tuneUp.chooseK();

        for (int i = 0; i < 100; i++) {
            System.out.println("Iteration: " + i);
            System.out.println("Starting Piece Values");
            tuneUp.tuneUpPieceValues();
            System.out.println("Starting PSQT");
            tuneUp.tuneUpPSQT();
            System.out.println("Starting Mobility");
            tuneUp.tuneUpMobility();
        }
    }

    private void tuneUpPSQT() throws InterruptedException, IOException {
        int[][] bestValues = new int[2][64];

        for (int piece = 0; piece < 6; piece++) {

            boolean isPawn = piece == 0;
            System.arraycopy(Piece.positionValue[0][piece], 0, bestValues[0], 0, 64);
            System.arraycopy(Piece.positionValue[1][piece], 0, bestValues[1], 0, 64);
            double bestError = calculateError();

            int step = 4;

            while (step > 0) {
                boolean improved = true;
                while (improved) {
                    //                    pp(bestValues);
                    System.out.println("Error: " + bestError);

                    improved = false;

                    int min = isPawn ? 8 : 0;
                    int max = isPawn ? 56 : 64;

                    for (int stage = 0; stage < 2; stage++) {
                        int[] changing = Piece.positionValue[stage][piece];
                        for (int sq = min; sq < max; sq++) {

                            int col = sq % 8;
                            if (col >= 4) continue;

                            System.arraycopy(bestValues[stage], 0, changing, 0, 64);

                            changing[sq] += step;
                            changing[sq + (7 - 2 * col)] += step;

                            double newError = calculateError();
                            if (newError < bestError - 0.00000001) {
                                bestError = newError;

                                System.arraycopy(changing, 0, bestValues[stage], 0, 64);
                                improved = true;
                            } else {
                                changing[sq] -= 2 * step;
                                changing[sq + (7 - 2 * col)] -= 2 * step;
                                newError = calculateError();
                                if (newError < bestError - 0.00000001) {
                                    bestError = newError;

                                    System.arraycopy(changing, 0, bestValues[stage], 0, 64);

                                    improved = true;
                                }
                            }
                        }
                    }

                    step /= 2;
                }

                FileWriter fw = new FileWriter("piece-" + piece + ".txt");
                fw.write(Arrays.toString(bestValues[0]));
                fw.write(Arrays.toString(bestValues[1]));
                fw.close();
            }
        }
    }

    private void pp(int[][] arr) {
        for (int stg = 0; stg < 2; stg++) {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    System.out.printf(" %3d,", arr[stg][i * 8 + j]);
                }
                System.out.println();
            }
            System.out.println();
        }
    }

    private void tuneUpMobility() throws InterruptedException, IOException {
        int[] movements = {9, 14, 15, 28};
        int[][][] best = {
            { // knights
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
            },
            { // bishops
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            },
            { // rooks
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            },
            { // queens
                {
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0
                },
                {
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0
                },
            }
        };

        for (int piece = 1; piece < 5; piece++) {
            int pieceIdx = piece - 1;
            System.arraycopy(
                    Piece.mobilities[pieceIdx][0], 0, best[pieceIdx][0], 0, movements[pieceIdx]);
            System.arraycopy(
                    Piece.mobilities[pieceIdx][1], 0, best[pieceIdx][1], 0, movements[pieceIdx]);
            double bestError = calculateError();

            int step = 4;

            while (step > 0) {
                boolean improved = true;
                while (improved) {
                    //                    System.out.println(Arrays.toString(best[pieceIdx][0]));
                    //                    System.out.println(Arrays.toString(best[pieceIdx][1]));
                    System.out.println("Error: " + bestError);

                    improved = false;

                    for (int stage = 0; stage < 2; stage++) {
                        int[] changing = Piece.mobilities[pieceIdx][stage];
                        for (int sq = 0; sq < movements[pieceIdx]; sq++) {

                            int col = sq % 8;
                            if (col >= 4) continue;

                            System.arraycopy(
                                    best[pieceIdx][stage], 0, changing, 0, movements[pieceIdx]);

                            changing[sq] += step;

                            double newError = calculateError();
                            if (newError < bestError - 0.00000001) {
                                bestError = newError;

                                System.arraycopy(
                                        changing, 0, best[pieceIdx][stage], 0, movements[pieceIdx]);
                                improved = true;
                            } else {
                                changing[sq] -= 2 * step;
                                newError = calculateError();
                                if (newError < bestError - 0.00000001) {
                                    bestError = newError;

                                    System.arraycopy(
                                            changing,
                                            0,
                                            best[pieceIdx][stage],
                                            0,
                                            movements[pieceIdx]);

                                    improved = true;
                                }
                            }
                        }
                    }

                    step /= 2;
                }

                FileWriter fw = new FileWriter("mobilities-" + piece + ".txt");
                fw.write(Arrays.toString(best[pieceIdx][0]));
                fw.write(Arrays.toString(best[pieceIdx][1]));
                fw.close();
            }
        }
    }

    public void chooseK() throws InterruptedException {
        double k = -1.01;
        double minError = calculateError();
        double bestK = k;

        for (k = -1.02; k > -2; k -= 0.01) {
            K = k;
            double error = calculateError();
            if (error < minError) {
                bestK = k;
                minError = error;
            }
        }

        System.out.println("Choosing K of " + K);
        K = bestK;
    }

    private double calculateError() throws InterruptedException {
        List<Callable<Double>> chunks =
                chunked(scores.stream(), (int) Math.ceil(scores.size() >> 4))
                        .map(
                                epds ->
                                        (Callable<Double>)
                                                () -> {
                                                    double totalScore = 0;

                                                    for (EPD.EPDTexel texel : epds) {
                                                        Position p = new Position(texel.fen);
                                                        int score = evals.positionEvaluation(p);
                                                        double scoreSigmoid =
                                                                calculateSigmoid(
                                                                        sideScalar[p.sideToMove]
                                                                                * score);

                                                        totalScore +=
                                                                Math.pow(
                                                                        texel.result - scoreSigmoid,
                                                                        2);
                                                    }

                                                    return totalScore;
                                                })
                        .collect(Collectors.toList());

        double result =
                service.invokeAll(chunks).stream()
                        .mapToDouble(
                                f -> {
                                    try {
                                        return f.get();
                                    } catch (InterruptedException | ExecutionException e) {
                                        e.printStackTrace();
                                        return 0;
                                    }
                                })
                        .sum();

        return result / scores.size();
    }

    public static <T> Stream<List<T>> chunked(Stream<T> stream, int chunkSize) {
        AtomicInteger index = new AtomicInteger(0);

        return stream
                .collect(Collectors.groupingBy(x -> index.getAndIncrement() / chunkSize))
                .values()
                .stream();
    }

    public double calculateSigmoid(double score) {
        return 1 / (1 + Math.pow(10, K * score / 400));
    }

    private void tuneUpPieceValues() throws InterruptedException, IOException {
        int[] changing = Piece.pieceValues;
        int size = changing.length;
        int[] bestValues = new int[size];

        System.arraycopy(changing, 0, bestValues, 0, size);
        double bestError = calculateError();

        int step = 8;

        while (step > 0) {
            boolean improved = true;
            while (improved) {
                //                System.out.println(Arrays.toString(bestValues));
                System.out.println("Error: " + bestError);

                improved = false;

                for (int piece = 0; piece < size; piece++) {
                    if (piece == 5 || piece == 11 || piece == 0) continue;

                    System.arraycopy(bestValues, 0, changing, 0, size);

                    changing[piece] += step;
                    double newError = calculateError();
                    if (newError < bestError - 0.00000001) {
                        bestError = newError;

                        System.arraycopy(changing, 0, bestValues, 0, size);
                        improved = true;
                    } else {
                        changing[piece] -= 2 * step;
                        newError = calculateError();
                        if (newError < bestError - 0.00000001) {
                            bestError = newError;

                            System.arraycopy(changing, 0, bestValues, 0, size);

                            improved = true;
                        }
                    }
                }
            }

            step /= 2;
        }

        FileWriter fw = new FileWriter("piece-values.txt");
        fw.write(Arrays.toString(bestValues));
        fw.close();
    }
}
