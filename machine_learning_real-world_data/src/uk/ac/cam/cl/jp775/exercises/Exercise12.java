package uk.ac.cam.cl.jp775.exercises;

import uk.ac.cam.cl.mlrd.exercises.social_networks.IExercise12;

import java.util.*;

public class Exercise12 implements IExercise12 {

    /**
     * Compute graph clustering using the Girvan-Newman method. Stop algorithm when the
     * minimum number of components has been reached (your answer may be higher than
     * the minimum).
     *
     * @param graph
     *        {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> The
     *        loaded graph
     * @param minimumComponents {@link int} The minimum number of components to reach.
     * @return {@link List}<{@link Set}<{@link Integer}>>
     *        List of components for the graph.
     */
    public List<Set<Integer>> GirvanNewman(Map<Integer, Set<Integer>> graph, int minimumComponents) {
        List<Set<Integer>> subgraphs = getComponents(graph);
        while (subgraphs.size() < minimumComponents) {
            Map<Integer, Map<Integer, Double>> betweenness = getEdgeBetweenness(graph);
            //get biggest betweenness
            double max = Double.NEGATIVE_INFINITY;
            List<int[]> maxes = new ArrayList<>();
            for (Integer i : betweenness.keySet()) {
                for (Integer j : betweenness.get(i).keySet()) {
                    double b = betweenness.get(i).get(j);
                    if (b > max) {
                        max = b;
                        maxes.clear();
                        maxes.add(new int[]{i, j});
                    }
                    if (b == max) {
                        maxes.add(new int[]{i, j});
                    }
                }
            }

            for (int[] pair : maxes) {
                int i = pair[0], j = pair[1];
                graph.get(i).remove(j);
                graph.get(j).remove(i);
            }

            subgraphs = getComponents(graph);
        }
        return subgraphs;
    }

    /**
     * Find the number of edges in the graph.
     *
     * @param graph
     *        {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> The
     *        loaded graph
     * @return {@link Integer}> Number of edges.
     */
    public int getNumberOfEdges(Map<Integer, Set<Integer>> graph) {
        int edges = 0;
        for (Integer i : graph.keySet()) {
            edges += graph.get(i).size();
        }
        edges /= 2;
        return edges;
    }

    private void dfsRecurse(Map<Integer, Set<Integer>> graph, int vertex, Map<Integer, Boolean> visited, Set<Integer> set) {
        visited.put(vertex, true);
        set.add(vertex);
        for (Integer i : graph.get(vertex)) {
            if (!visited.containsKey(i) || !visited.get(i)) {
                dfsRecurse(graph, i, visited, set);
            }
        }
    }

    /**
     * Find the number of components in the graph using a DFS.
     *
     * @param graph
     *        {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> The
     *        loaded graph
     * @return {@link List}<{@link Set}<{@link Integer}>>
     *        List of components for the graph.
     */
    public List<Set<Integer>> getComponents(Map<Integer, Set<Integer>> graph) {
        Map<Integer, Boolean> visited = new HashMap<>();
        List<Set<Integer>> components = new ArrayList<>();
        for (Integer i : graph.keySet()) {
            if (!visited.containsKey(i)) {
                Set<Integer> set = new HashSet<>();
                dfsRecurse(graph, i, visited, set);
                components.add(set);
            }
        }
        return components;
    }

    /**
     * Calculate the edge betweenness.
     *
     * @param graph
     *         {@link Map}<{@link Integer}, {@link Set}<{@link Integer}>> The
     *         loaded graph
     * @return {@link Map}<{@link Integer},
     *         {@link Map}<{@link Integer},{@link Double}>> Edge betweenness for
     *         each pair of vertices in the graph
     */
    public Map<Integer, Map<Integer, Double>> getEdgeBetweenness(Map<Integer, Set<Integer>> graph) {
        Set<Integer> V = graph.keySet();

        Map<Integer, Map<Integer, Double>> betweenness = new HashMap<>();
        for (Integer i : V) {
            Map<Integer, Double> map = new HashMap<>();
            for (Integer n : graph.get(i)) {
                map.put(n, 0.0);
            }
            betweenness.put(i, map);
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
                    double c = (1 + d_w) * ((double) s_v / s_w);
                    double c_b = betweenness.get(v).get(w);
                    betweenness.get(v).put(w, c_b + c);
                    dependencies.put(v, d_v + c);
                }
            }
        }

        return betweenness;
    }

}
