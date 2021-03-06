import bfs.BFS;
import bfs.ParallelBFS;
import bfs.SequenceBFS;
import cube.Cube;
import simulator.graph.CubeSimulator;
import simulator.graph.GraphSimulator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Main {
    private static final int P_FOR_BLOCK_SIZE = 2000;
    private static final int P_SCAN_BLOCK_SIZE = 5000;
    private static final int CUBE_SIZE = 350;
    private static final int ATTEMPTS_COUNT = 5;

    private static String timesToString(List<Long> times) {
        String timesString = times.stream().map(String::valueOf).collect(Collectors.joining(" ms, "));
        return timesString + " ms";
    }

    private static long getAverageTime(List<Long> times) {
        if (times.isEmpty()) {
            return 0;
        }
        long allTime = times.stream().reduce(0L, Long::sum);
        return allTime / times.size();
    }

    public static void main(String[] args) {
        System.out.println("Parallel for block size: " + P_FOR_BLOCK_SIZE);
        System.out.println("Scan block size: " + P_SCAN_BLOCK_SIZE);
        System.out.println("Cube size: " + CUBE_SIZE);
        System.out.println("Attempts count: " + ATTEMPTS_COUNT + "\n\n");


        List<Long> seqTimes = new ArrayList<>();
        List<Long> parTimes = new ArrayList<>();


        BFS sequenceBFS = new SequenceBFS();
        ParallelBFS parallelBFS = new ParallelBFS();
        parallelBFS.setP_FOR_BLOCK_SIZE(P_FOR_BLOCK_SIZE);
        parallelBFS.setP_SCAN_BLOCK_SIZE(P_SCAN_BLOCK_SIZE);

        int[][] cubeGraph = new Cube().generateCubeGraph(CUBE_SIZE);

        for (int i = 0; i < ATTEMPTS_COUNT; i++) {
            System.out.println("Attempt number " + (i + 1));
            long seqTime = launchBFS(sequenceBFS, cubeGraph, "Sequence");
            seqTimes.add(seqTime);

            long parTime = launchBFS(parallelBFS, cubeGraph, "Parallel");
            parTimes.add(parTime);
        }


        long aveParTime = getAverageTime(parTimes);
        long aveSeqTime = getAverageTime(seqTimes);

        System.out.println("Parallel BFS times: " + timesToString(parTimes) + ".");
        System.out.println("Sequence BFS times: " + timesToString(seqTimes) + ".");

        System.out.println("\nAverage parallel BFS time: " + aveParTime + " ms.");
        System.out.println("Average sequence BFS time: " + aveSeqTime + " ms.");
        System.out.println("\nParallel BFS " + ((double) aveSeqTime / (double) aveParTime) + " times faster than Sequence BFS.");
    }

    private static long launchBFS(BFS bfs, int[][] cubeGraph, String typeBFS) {
        long startTime;
        long endTime;
        startTime = System.nanoTime();
        int[] distances = bfs.compute(cubeGraph, 0);
        endTime = System.nanoTime();
        long time = TimeUnit.MILLISECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS);

        if (!new Cube().checkBFSResult(distances, CUBE_SIZE)) {
            System.err.println(typeBFS + " BFS is not correct.");
        }

        System.out.println(typeBFS + " time: " + time + " ms.");

        return time;
    }
}
