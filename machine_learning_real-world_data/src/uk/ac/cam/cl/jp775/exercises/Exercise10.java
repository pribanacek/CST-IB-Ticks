package uk.ac.cam.cl.jp775.exercises;

import uk.ac.cam.cl.mlrd.exercises.social_networks.IExercise10;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Exercise10 implements IExercise10 {
    /**
     * Load the graph file. Each line in the file corresponds to an edge; the
     * first column is the source node and the second column is the target. As
     * the graph is undirected, your program should add the source as a
     * neighbour of the target as well as the target a neighbour of the source.
     *
     * @param graphFile
     *            {@link Path} the path to the network specification
     * @return {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> For
     *         each node, all the nodes neighbouring that node
     */
    public Map<Integer, Set<Integer>> loadGraph(Path graphFile) throws IOException {
        Map<Integer, Set<Integer>> map = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(graphFile)) {
            String s = reader.readLine();
            while (s != null) {
                String[] tokens = s.split(" ");
                int from = Integer.parseInt(tokens[0]);
                int to = Integer.parseInt(tokens[1]);
                if (!map.containsKey(from)) {
                    map.put(from, new HashSet<>());
                }
                map.get(from).add(to);

                //undirected graph, so do the same the other way
                if (!map.containsKey(to)) {
                    map.put(to, new HashSet<>());
                }
                map.get(to).add(from);

                s = reader.readLine();
            }
        } catch (IOException e) {
            throw new IOException("Can't access file " + graphFile, e);
        }

        return map;
    }

    /**
     * Find the number of neighbours for each point in the graph.
     *
     * @param graph
     *            {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> The
     *            loaded graph
     * @return {@link Map}<{@link Integer}, {@link Integer}> For each node, the
     *         number of neighbours it has
     */
    public Map<Integer, Integer> getConnectivities(Map<Integer, Set<Integer>> graph) {
        Map<Integer, Integer> neighbours = new HashMap<>();
        for (Integer a : graph.keySet()) {
            Set<Integer> s = graph.get(a);
            neighbours.put(a, s.size());
        }
        return neighbours;
    }

    /**
     * Find the maximal shortest distance between any two nodes in the network
     * using a breadth-first search.
     *
     * @param graph
     *            {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> The
     *            loaded graph
     * @return <code>int</code> The diameter of the network
     */
    public int getDiameter(Map<Integer, Set<Integer>> graph) {
        int diameter = 0;
        for (Integer a : graph.keySet()) {
            int max = bfsDepth(graph, a);
            if (max > diameter) {
                diameter = max;
            }
        }
        return diameter;
    }

    private int bfsDepth(Map<Integer, Set<Integer>> graph, int start) {
        Map<Integer, Integer> distances = new HashMap<>();
        Queue<Integer> queue = new LinkedList<>();
        distances.put(start, 0);
        queue.add(start);
        int maxDepth = 0;
        while (!queue.isEmpty()) {
            Integer a = queue.poll();
            for (Integer b : graph.get(a)) {
                if (!distances.containsKey(b)) {
                    queue.add(b);
                    int depth = distances.get(a) + 1;
                    distances.put(b, depth);
                    if (depth > maxDepth) {
                        maxDepth = depth;
                    }
                }
            }
        }
        return maxDepth;
    }


}
