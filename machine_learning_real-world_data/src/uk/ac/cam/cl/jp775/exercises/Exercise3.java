package uk.ac.cam.cl.jp775.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise2;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;
import uk.ac.cam.cl.mlrd.utils.BestFit;
import uk.ac.cam.cl.mlrd.utils.BestFit.Point;
import uk.ac.cam.cl.mlrd.utils.BestFit.Line;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Exercise3 {

    public Map<String, Integer> calculateFrequencies(Set<Path> dataSet) throws IOException {
        Map<String, Integer> frequencies = new HashMap<String, Integer>();
        for (Path p : dataSet) {
            List<String> tokens = Tokenizer.tokenize(p);
            for (String token : tokens) {
                if (!frequencies.containsKey(token)) {
                    frequencies.put(token, 1);
                } else {
                    frequencies.put(token, frequencies.get(token) + 1);
                }
            }
        }
        return frequencies;
    }

    public Map<String, Integer> calculateRanks(Map<String, Integer> frequencies) {
        Map<String, Integer> ranks = new HashMap<String, Integer>();

        List<Map.Entry<String, Integer>> list = new ArrayList<>(frequencies.entrySet());
        list.sort(Map.Entry.comparingByValue());

        for (int i = 0; i < list.size(); i++) {
            ranks.put(list.get(i).getKey(), list.size() - i);
        }
        return ranks;
    }

    public List<String> getTask1Words() {
        List<String> task1words = new ArrayList<>();
        task1words.add("interesting");
        task1words.add("unique");
        task1words.add("classic");
        task1words.add("nice");
        task1words.add("recommend");
        task1words.add("incongruous");
        task1words.add("annoying");
        task1words.add("unfortunately");
        task1words.add("bland");
        task1words.add("lacking");
        return task1words;
    }

    private Line bestFit = null;

    public Line calculateBestFit(Map<String, Integer> ranks, Map<String, Integer> frequencies) {
        Map<BestFit.Point, Double> pointsWeighted = new HashMap<>();
        for (String k : ranks.keySet()) {
            if (ranks.get(k) < 10000) {
                BestFit.Point point = new BestFit.Point(Math.log(ranks.get(k)), Math.log(frequencies.get(k)));
                pointsWeighted.put(point, (double) frequencies.get(k));
            }
        }
        this.bestFit = BestFit.leastSquares(pointsWeighted);
        return this.bestFit;
    }

    public double getExpectedFrequency(int rank) {
        if (bestFit == null) {
            System.err.println("Calculate best fit first");
            return 0;
        }
        double logRank = Math.log(rank);
        double logFreq = bestFit.yIntercept + bestFit.gradient * logRank;
        double freq = Math.exp(logFreq);
        return freq;
    }

    public List<Point> getHeapsLawPoints(Set<Path> dataSet) throws IOException {
        List<Point> points = new ArrayList<Point>();
        Map<String, Integer> frequencies = new HashMap<String, Integer>();
        int tokenCount = 0;
        for (Path p : dataSet) {
            List<String> tokens = Tokenizer.tokenize(p);
            for (String token : tokens) {
                if (!frequencies.containsKey(token)) {
                    frequencies.put(token, 1);
                } else {
                    frequencies.put(token, frequencies.get(token) + 1);
                }
                tokenCount++;
                if (tokenCount >= 1 << points.size()) {
                    int x = tokenCount;
                    int y = frequencies.keySet().size();
                    points.add(new Point(Math.log(x), Math.log(y)));
                }
            }
        }
        return points;
    }



}
