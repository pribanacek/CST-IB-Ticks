package uk.ac.cam.cl.jp775.exercises;

import uk.ac.cam.cl.mlrd.exercises.social_networks.IExercise11;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Exercise11 implements IExercise11 {

    private Map<Integer, Set<Integer>> loadGraph(Path graphFile) throws IOException {
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

                //undirected graph
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
     * Load the graph file. Use Brandes' algorithm to calculate the betweenness
     * centrality for each node in the graph.
     *
     * @param graphFile
     *            {@link Path} the path to the network specification
     * @return {@link Map}<{@link Integer}, {@link Double}> For
     *         each node, its betweenness centrality
     */
    public Map<Integer, Double> getNodeBetweenness(Path graphFile) throws IOException {
        Map<Integer, Set<Integer>> graph = loadGraph(graphFile);
        Set<Integer> V = graph.keySet();

        Map<Integer, Double> betweenness = new HashMap<>();
        for (Integer i : V) {
            betweenness.put(i, 0.0);
        }

        Queue<Integer> queue = new LinkedList<>();
        Stack<Integer> stack = new Stack<>();
        Map<Integer, Integer> distance = new HashMap<>();
        Map<Integer, List<Integer>> pred = new HashMap<>();
        Map<Integer, Integer> numberOfPaths = new HashMap<>();
        Map<Integer, Double> dependencies = new HashMap<>();

        for (Integer s : V) {
            //single source shortest paths
                //initialization
            for (Integer w : V) {
                pred.put(w, new ArrayList<>());
                distance.put(w, -1);
                numberOfPaths.put(w, 0);
            }
            distance.put(s, 0);
            numberOfPaths.put(s, 1);
            queue.add(s);

            while (!queue.isEmpty()) {
                Integer v = queue.poll();
                stack.push(v);
                for (Integer w : graph.get(v)) {
                    //path discovery
                    if (distance.get(w) < 0) {
                        distance.put(w, distance.get(v) + 1);
                        queue.add(w);
                    }
                    //path counting
                    if (distance.get(w) == distance.get(v) + 1) {
                        numberOfPaths.put(w, numberOfPaths.get(w) + numberOfPaths.get(v));
                        pred.get(w).add(v);
                    }
                }
            }

            //accumulation
            for (Integer v : V) {
                dependencies.put(v, 0.0);
            }
            while (!stack.empty()) {
                Integer w = stack.pop();
                for (Integer v : pred.get(w)) {
                    double d_v = dependencies.get(v);
                    double d_w = dependencies.get(w);
                    int s_v = numberOfPaths.get(v);
                    int s_w = numberOfPaths.get(w);
                    double new_dv = d_v + (1 + d_w) * ((double) s_v / s_w);
                    dependencies.put(v, new_dv);
                }
                if (w != s) {
                    betweenness.put(w, betweenness.get(w) + dependencies.get(w));
                }
            }
        }

        for (Integer i : betweenness.keySet()) {
            betweenness.put(i, betweenness.get(i) / 2);
        }

        return betweenness;
    }
}
