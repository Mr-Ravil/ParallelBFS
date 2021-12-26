package bfs;

import simulator.graph.GraphSimulator;
import parallel.util.ParallelUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ParallelBFS implements BFS {
    private int P_FOR_BLOCK_SIZE = 1000;
    private int P_SCAN_BLOCK_SIZE = 10000;

    public int getP_FOR_BLOCK_SIZE() {
        return P_FOR_BLOCK_SIZE;
    }

    public void setP_FOR_BLOCK_SIZE(int P_FOR_BLOCK_SIZE) {
        this.P_FOR_BLOCK_SIZE = P_FOR_BLOCK_SIZE;
    }

    public int getP_SCAN_BLOCK_SIZE() {
        return this.P_SCAN_BLOCK_SIZE;
    }

    public void setP_SCAN_BLOCK_SIZE(int P_SCAN_BLOCK_SIZE) {
        this.P_SCAN_BLOCK_SIZE = P_SCAN_BLOCK_SIZE;
    }

//    public List<Long> createDegList = new ArrayList<>(); //TODO
//    public List<Long> createStartBlockList = new ArrayList<>(); //TODO
//    public List<Long> createNextFrontierList = new ArrayList<>(); //TODO
//    public List<Long> doP_FORList = new ArrayList<>(); //TODO
//    public List<Long> setFrontierList = new ArrayList<>(); //TODO

    @Override
    public List<Distance> compute(GraphSimulator graph, int start) {
        ParallelUtil parallelUtils = new ParallelUtil();
        parallelUtils.setP_FOR_BLOCK_SIZE(P_FOR_BLOCK_SIZE);
        parallelUtils.setP_SCAN_BLOCK_SIZE(P_SCAN_BLOCK_SIZE);

//        List<Distance> distances = new ArrayList<>(Collections.nCopies(graph.getSize(), null));
        int[] dist = new int[graph.getSize()];
        AtomicBoolean[] flag = new AtomicBoolean[graph.getSize()];
        parallelUtils.parallelFor(graph.getSize(), i -> flag[i] = new AtomicBoolean());

        AtomicReference<int[]> frontier = new AtomicReference<>(new int[1]);

        dist[start] = 0;
//        distances.set(start, new Distance(0, -1));
        frontier.get()[0] = start;
        flag[start].set(true);

        int distance = 1;


//        long startTime; //TODO
//        long endTime; //TODO
//        long createDeg = 0; //TODO
//        long createStartBlock = 0; //TODO
//        long createNextFrontier = 0; //TODO
//        long doP_FOR = 0; //TODO
//        long setFrontier = 0; //TODO

        while (frontier.get().length != 0) {
//            startTime = System.nanoTime(); //TODO

            // put into big p_for
            int[] deg = new int[frontier.get().length];
            parallelUtils.parallelFor(frontier.get().length,
                    i -> deg[i] = frontier.get()[i] == -1 ? 0 : graph.getNeighbours(frontier.get()[i]).length);

//            endTime = System.nanoTime(); //TODO
//            createDeg += TimeUnit.MILLISECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS); //TODO
//            startTime = System.nanoTime(); //TODO

            int[] startBlock = parallelUtils.parallelScan(deg);

//            endTime = System.nanoTime(); //TODO
//            createStartBlock += TimeUnit.MILLISECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS); //TODO
//            startTime = System.nanoTime(); //TODO

            int nextFrontierSize = startBlock[startBlock.length - 1];
            int[] nextFrontier = new int[nextFrontierSize];
//            parallelUtils.parallelFor(nextFrontierSize, i -> nextFrontier[i] = -1);

//            endTime = System.nanoTime(); //TODO
//            createNextFrontier += TimeUnit.MILLISECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS); //TODO

            int finalDistance = distance;

//            startTime = System.nanoTime(); //TODO

            parallelUtils.parallelFor(frontier.get().length, currentIndex -> {
                int current = frontier.get()[currentIndex];
                if (current != -1) {
                    int[] neighbours = graph.getNeighbours(current);
                    for (int i = 0; i < neighbours.length; i++) {
                        int next = neighbours[i];
                        if (flag[next].compareAndSet(false, true)) {
//                            distances.set(next, new Distance(finalDistance, current));
                            dist[next] = finalDistance;
                            nextFrontier[startBlock[currentIndex] + i] = next;
                        } else {
                            nextFrontier[startBlock[currentIndex] + i] = -1;
                        }
                    }
                } else {
                    for (int i = startBlock[currentIndex]; i < startBlock[currentIndex + 1]; i++) {
                        nextFrontier[i] = -1;
                    }
                }
            });

//            endTime = System.nanoTime(); //TODO
//            doP_FOR += TimeUnit.MILLISECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS); //TODO
//            startTime = System.nanoTime(); //TODO

            frontier.set(nextFrontier);

//            endTime = System.nanoTime(); //TODO
//            setFrontier += TimeUnit.MILLISECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS); //TODO

            distance++;
        }

//        createDegList.add(createDeg); //TODO
//        createStartBlockList.add(createStartBlock); //TODO
//        createNextFrontierList.add(createNextFrontier); //TODO
//        doP_FORList.add(doP_FOR); //TODO
//        setFrontierList.add(setFrontier); //TODO

        return null;
    }
}
