package uk.ac.cam.cl.jp775.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise4;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Exercise4 implements IExercise4 {

    private static enum Intensity {
        WEAK, STRONG;
    }

    /**
     * Modify the simple classifier from Exercise1 to include the information about the magnitude of a sentiment.
     * @param testSet
     *            {@link Set}<{@link Path}> Paths to reviews to classify
     * @param lexiconFile
     *            {@link Path} Path to the lexicon file
     * @return {@link Map}<{@link Path}, {@link Sentiment}> Map of calculated
     *         sentiment for each review
     * @throws IOException
     */
    public Map<Path, Sentiment> magnitudeClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
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

        final double INTENSITY_FACTOR = 1.0;

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

    /**
     * Implement the two-sided sign test algorithm to determine if one
     * classifier is significantly better or worse than another.
     * The sign for a result should be determined by which
     * classifier is more correct, or if they are equally correct should be 0.5
     * positive, 0.5 negative and the ceiling of the least common sign total
     * should be used to calculate the probability.
     *
     * @param actualSentiments
     *            {@link Map}<{@link Path}, {@link Sentiment}>
     * @param classificationA
     *            {@link Map}<{@link Path}, {@link Sentiment}>
     * @param classificationB
     *            {@link Map}<{@link Path}, {@link Sentiment}>
     * @return <code>double</code>
     */
    public double signTest(Map<Path, Sentiment> actualSentiments, Map<Path, Sentiment> classificationA,
                           Map<Path, Sentiment> classificationB) {
        int plus = 0;
        int minus = 0;
        int nul = 0;
        for (Path p : actualSentiments.keySet()) {
            Sentiment a = classificationA.get(p);
            Sentiment b = classificationB.get(p);
            Sentiment actual = actualSentiments.get(p);
            if (a == b) {
                nul++;
            } else if (a == actual) {
                plus++;
            } else {
                minus++;
            }
        }

        int n = (int) (2 * Math.ceil((double) nul / 2d) + plus + minus);
        int k = (int) Math.ceil((double) nul / 2d) + Math.min(plus, minus);

        double q = 0.5;

        double sum = 0;
        for (int i = 0; i <= k; i++) {
            BigInteger nFact = factorial(n);
            BigInteger iFact = factorial(i);
            BigInteger niFact = factorial(n - i);
            BigInteger nChoosei = nFact.divide(iFact.multiply(niFact));

            double q_i = Math.pow(q, i);
            double q_ni = Math.pow(q, n - i);
            sum += q_i * q_ni * nChoosei.doubleValue();
        }
        sum *= 2;
        return sum;
    }

    private static BigInteger factorial(int N) {
        BigInteger f = BigInteger.ONE;
        for (int i = 2; i <= N; i++) {
            f = f.multiply(BigInteger.valueOf(i));
        }
        return f;
    }
}
