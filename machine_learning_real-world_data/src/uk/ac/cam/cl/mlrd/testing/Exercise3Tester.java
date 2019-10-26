package uk.ac.cam.cl.mlrd.testing;

import uk.ac.cam.cl.jp775.exercises.Exercise1;
import uk.ac.cam.cl.jp775.exercises.Exercise2;
import uk.ac.cam.cl.jp775.exercises.Exercise3;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.DataPreparation1;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise1;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise2;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.utils.BestFit;
import uk.ac.cam.cl.mlrd.utils.BestFit.Point;
import uk.ac.cam.cl.mlrd.utils.ChartPlotter;
import uk.ac.cam.cl.mlrd.utils.DataSplit;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

//TODO: Replace with your packages.

public class Exercise3Tester {

    static final Path dataDirectory = Paths.get("data");

    public static void main(String[] args) throws IOException {

        Path datasetDirectory = dataDirectory.resolve("large_dataset");
        Set<Path> dataSet = new HashSet<Path>();
        try (DirectoryStream<Path> files = Files.newDirectoryStream(datasetDirectory)) {
            for (Path item : files) {
                dataSet.add(item);
            }
        } catch (IOException e) {
            throw new IOException("Can't read the files.", e);
        }

        Exercise3 implementation = new Exercise3();

        System.out.println("Calculating frequencies");
        Map<String, Integer> frequencies = implementation.calculateFrequencies(dataSet);
        System.out.println("Calculating ranks");
        Map<String, Integer> ranks = implementation.calculateRanks(frequencies);
        System.out.println("Assembling points");
        List<Point> points = new ArrayList<Point>();
        for (String k : ranks.keySet()) {
            if (ranks.get(k) < 10000) {
                Point point = new Point(ranks.get(k), frequencies.get(k));
                points.add(point);
            }
        }

        System.out.println("Plotting rank-frequency graph.");
        ChartPlotter.plotLines(points);

        System.out.println("Task 1 Words:");
        List<String> task1words = implementation.getTask1Words();
        List<Point> task1points = new ArrayList<>();
        for (String s : task1words) {
            System.out.println(" - " + s);
            Point point = new Point(ranks.get(s), frequencies.get(s));
            task1points.add(point);
        }
        ChartPlotter.plotLines(task1points);


        System.out.println("Setting up log-log graph");
        List<Point> logPoints = new ArrayList<Point>();
        for (String k : ranks.keySet()) {
            if (ranks.get(k) < 10000) {
                Point point = new Point(Math.log(ranks.get(k)), Math.log(frequencies.get(k)));
                logPoints.add(point);
            }
        }
        ChartPlotter.plotLines(logPoints);


        System.out.println("Doing best-fit line");
        BestFit.Line bestFit = implementation.calculateBestFit(ranks, frequencies);

        List<Point> bestFitPoints = new ArrayList<Point>();
        bestFitPoints.add(new Point(0, (double) bestFit.yIntercept));
        bestFitPoints.add(new Point(-bestFit.yIntercept / bestFit.gradient, 0));

        ChartPlotter.plotLines(logPoints, bestFitPoints);
        System.out.println("y-intercept: " + bestFit.yIntercept);
        System.out.println("gradient: " + bestFit.gradient);

        System.out.println("\n Task1 words predicted vs real frequencies");
        for (String s : task1words) {
            int frequency = frequencies.get(s);
            int predicted = (int) implementation.getExpectedFrequency(ranks.get(s));
            int error = (int) ((double) Math.abs(predicted - frequency) / frequency * 100);
            System.out.println(" - " + s + ": " + frequency + ", " + predicted + "; error " + error + "%");
        }

        System.out.println("Zipfs Law Parameters");
        System.out.println("α = " + (-bestFit.gradient));
        System.out.println("k = " + Math.exp(bestFit.yIntercept));


        System.out.println("\nHeaps Law");
        List<Point> heapsLawPoints = implementation.getHeapsLawPoints(dataSet);
        ChartPlotter.plotLines(heapsLawPoints);
    }
}