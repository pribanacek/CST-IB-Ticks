package uk.ac.cam.cl.jp775.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise1;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Exercise1 implements IExercise1 {

    /**
     * Read the lexicon and determine whether the sentiment of each review in
     * the test set is positive or negative based on whether there are more
     * positive or negative words.
     *
     * @param testSet
     *            {@link Set}<{@link Path}> Paths to reviews to classify
     * @param lexiconFile
     *            {@link Path} Path to the lexicon file
     * @return {@link Map}<{@link Path}, {@link Sentiment}> Map of calculated
     *         sentiment for each review
     * @throws IOException
     */
    public Map<Path, Sentiment> simpleClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
        Map<Path, Sentiment> sMap = new HashMap<Path, Sentiment>();
        Map<String, Sentiment> wordMap = new HashMap<String, Sentiment>();

        BufferedReader lexiconBuffer = Files.newBufferedReader(lexiconFile, StandardCharsets.UTF_8);
        String line = lexiconBuffer.readLine();
        while (line != null) {
            String[] tokens = line.split(" ");
            String word = tokens[0].substring(5);
            String s = tokens[2].substring(9);
            Sentiment sentiment = s.equals("negative") ? Sentiment.NEGATIVE : Sentiment.POSITIVE;
            wordMap.put(word, sentiment);
            line = lexiconBuffer.readLine();
        }

        for (Path p : testSet) {
            List<String> tokens = Tokenizer.tokenize(p);
            int posCount = 0;
            int negCount = 0;
            for (String token : tokens) {
                if (wordMap.containsKey(token)) {
                    if (wordMap.get(token) == Sentiment.NEGATIVE) {
                        negCount++;
                    } else {
                        posCount++;
                    }
                }
            }
            sMap.put(p, posCount < negCount ? Sentiment.NEGATIVE : Sentiment.POSITIVE);
        }
        return sMap;
    }

    /**
     * Calculate the proportion of predicted sentiments that were correct.
     *
     * @param trueSentiments
     *            {@link Map}<{@link Path}, {@link Sentiment}> Map of correct
     *            sentiment for each review
     * @param predictedSentiments
     *            {@link Map}<{@link Path}, {@link Sentiment}> Map of calculated
     *            sentiment for each review
     * @return <code>double</code> The overall accuracy of the predictions
     */
    public double calculateAccuracy(Map<Path, Sentiment> trueSentiments, Map<Path, Sentiment> predictedSentiments) {
        Set<Path> paths = trueSentiments.keySet();
        int total = paths.size();
        int correct = 0;
        for (Path p : paths) {
            if (predictedSentiments.containsKey(p)) {
                if (trueSentiments.get(p) == predictedSentiments.get(p)) {
                    correct++;
                }
            } else {
                System.err.println("Path not included");
            }
        }
        return (double) correct / total;
    }

    private static enum Intensity {
        WEAK, STRONG;
    }

    /**
     * Use the training data to improve your classifier, perhaps by choosing an
     * offset for the classifier cutoff which works better than 0.
     *
     * @param testSet
     *            {@link Set}<{@link Path}> Paths to reviews to classify
     * @param lexiconFile
     *            {@link Path} Path to the lexicon file
     * @return {@link Map}<{@link Path}, {@link Sentiment}> Map of calculated
     *         sentiment for each review
     * @throws IOException
     */
    public Map<Path, Sentiment> improvedClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
        Map<Path, Sentiment> sMap = new HashMap<Path, Sentiment>();

        Map<String, Sentiment> lexiconSentiments = new HashMap<String, Sentiment>();
        Map<String, Intensity> lexiconIntensities = new HashMap<String, Intensity>();

        BufferedReader lexiconBuffer = Files.newBufferedReader(lexiconFile, StandardCharsets.UTF_8);
        String line = lexiconBuffer.readLine();
        while (line != null) {
            String[] tokens = line.split(" ");
            String word = tokens[0].substring(5);
            String i = tokens[1].substring(10);
            String s = tokens[2].substring(9);
            Sentiment sentiment = s.equals("negative") ? Sentiment.NEGATIVE : Sentiment.POSITIVE;
            Intensity intensity = i.equals("strong") ? Intensity.STRONG : Intensity.WEAK;
            lexiconSentiments.put(word, sentiment);
            lexiconIntensities.put(word, intensity);
            line = lexiconBuffer.readLine();
        }

        final double INTENSITY_FACTOR = 9.0;

        for (Path p : testSet) {
            List<String> tokens = Tokenizer.tokenize(p);
            double posCount = 0;
            double negCount = 0;
            for (String token : tokens) {
                if (lexiconSentiments.containsKey(token)) {
                    if (lexiconSentiments.get(token) == Sentiment.NEGATIVE) {
                        negCount += 1.0 + INTENSITY_FACTOR * lexiconIntensities.get(token).ordinal();
                    } else {
                        posCount += 1.0 + INTENSITY_FACTOR * lexiconIntensities.get(token).ordinal();
                    }
                }
            }
            sMap.put(p, posCount < negCount ? Sentiment.NEGATIVE : Sentiment.POSITIVE);
        }
        return sMap;
    }

}
