package uk.ac.cam.cl.jp775.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise2;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Exercise2 implements IExercise2 {

    /**
     * Calculate the probability of a document belonging to a given class based
     * on the training data.
     *
     * @param trainingSet
     *            {@link Map}<{@link Path}, {@link Sentiment}> Training review
     *            paths
     * @return {@link Map}<{@link Sentiment}, {@link Double}> Class
     *         probabilities.
     * @throws IOException
     */
    public Map<Sentiment, Double> calculateClassProbabilities(Map<Path, Sentiment> trainingSet) throws IOException {
        Map<Sentiment, Double> classProbabilities = new HashMap<Sentiment, Double>();
        int goodCount = 0;
        for (Sentiment s : trainingSet.values()) {
            if (s == Sentiment.POSITIVE) {
                goodCount++;
            }
        }
        int total = trainingSet.size();
        classProbabilities.put(Sentiment.POSITIVE, (double) goodCount / total);
        classProbabilities.put(Sentiment.NEGATIVE, 1.0 - (double) goodCount / total);
        return classProbabilities;
    }

    /**
     * For each word and sentiment present in the training set, estimate the
     * unsmoothed log probability of a word to occur in a review with a
     * particular sentiment.
     *
     * @param trainingSet
     *            {@link Map}<{@link Path}, {@link Sentiment}> Training review
     *            paths
     * @return {@link Map}<{@link String}, {@link Map}<{@link Sentiment},
     *         {@link Double}>> Estimated log probabilities
     * @throws IOException
     */
    public Map<String, Map<Sentiment, Double>> calculateUnsmoothedLogProbs(Map<Path, Sentiment> trainingSet) throws IOException {

        Map<String, Map<Sentiment, Double>> logProbs = new HashMap<String, Map<Sentiment, Double>>();

        //int[0] indicates number of occurrences in good reviews and int[1] in bad ones
        Map<String, int[]> wordCounts = new HashMap<String, int[]>();
        int posTokenCount = 0;
        int negTokenCount = 0;
        for (Path p : trainingSet.keySet()) {
            List<String> tokens = Tokenizer.tokenize(p);
            for (String token : tokens) {
                if (!wordCounts.containsKey(token)) {
                    wordCounts.put(token, new int[]{0, 0});
                }
                if (trainingSet.get(p) == Sentiment.POSITIVE) {
                    wordCounts.get(token)[0]++;
                    posTokenCount++;
                } else {
                    wordCounts.get(token)[1]++;
                    negTokenCount++;
                }
            }
        }

        for (String token : wordCounts.keySet()) {
            int[] value = wordCounts.get(token);
            double posProb = (double) value[0] / posTokenCount;
            double negProb = (double) value[1] / negTokenCount;
            Map<Sentiment, Double> logMap = new HashMap<Sentiment, Double>();
            logMap.put(Sentiment.POSITIVE, Math.log(posProb));
            logMap.put(Sentiment.NEGATIVE, Math.log(negProb));
            logProbs.put(token, logMap);
        }

        return logProbs;
    }

    /**
     * For each word and sentiment present in the training set, estimate the
     * smoothed log probability of a word to occur in a review with a particular
     * sentiment. Use the smoothing technique described in the instructions.
     *
     * @param trainingSet
     *            {@link Map}<{@link Path}, {@link Sentiment}> Training review
     *            paths
     * @return {@link Map}<{@link String}, {@link Map}<{@link Sentiment},
     *         {@link Double}>> Estimated log probabilities
     * @throws IOException
     */
    public Map<String, Map<Sentiment, Double>> calculateSmoothedLogProbs(Map<Path, Sentiment> trainingSet) throws IOException {
        Map<String, Map<Sentiment, Double>> logProbs = new HashMap<String, Map<Sentiment, Double>>();

        //int[0] indicates number of occurrences in good reviews and int[1] in bad ones
        Map<String, int[]> wordCounts = new HashMap<String, int[]>();
        int posTokenCount = 0;
        int negTokenCount = 0;
        for (Path p : trainingSet.keySet()) {
            List<String> tokens = Tokenizer.tokenize(p);
            for (String token : tokens) {
                if (!wordCounts.containsKey(token)) {
                    //Laplace smoothing here
                    wordCounts.put(token, new int[]{1, 1});
                    posTokenCount++;
                    negTokenCount++;
                }
                if (trainingSet.get(p) == Sentiment.POSITIVE) {
                    wordCounts.get(token)[0]++;
                    posTokenCount++;
                } else {
                    wordCounts.get(token)[1]++;
                    negTokenCount++;
                }
            }
        }

        for (String token : wordCounts.keySet()) {
            int[] value = wordCounts.get(token);
            double posProb = (double) value[0] / posTokenCount;
            double negProb = (double) value[1] / negTokenCount;
            Map<Sentiment, Double> logMap = new HashMap<Sentiment, Double>();
            logMap.put(Sentiment.POSITIVE, Math.log(posProb));
            logMap.put(Sentiment.NEGATIVE, Math.log(negProb));
            logProbs.put(token, logMap);
        }

        return logProbs;
    }

    /**
     * Use the estimated log probabilities to predict the sentiment of each
     * review in the test set.
     *
     * @param testSet
     *            {@link Set}<{@link Path}> Test review paths
     * @param tokenLogProbs
     *            {@link Map}<{@link String}, {@link Map}<{@link Sentiment},
     *            {@link Double}>> Log probabilities
     * @return {@link Map}<{@link Path}, {@link Sentiment}> Predicted sentiments
     * @throws IOException
     */
    public Map<Path, Sentiment> naiveBayes(Set<Path> testSet, Map<String, Map<Sentiment, Double>> tokenLogProbs,
                                           Map<Sentiment, Double> classProbabilities) throws IOException {
        Map<Path, Sentiment> pathMap = new HashMap<Path, Sentiment>();

        for (Path p : testSet) {
            List<String> tokens = Tokenizer.tokenize(p);
            double logPPos = Math.log(classProbabilities.get(Sentiment.POSITIVE));
            double logPNeg = Math.log(classProbabilities.get(Sentiment.NEGATIVE));
            for (String token : tokens) {
                Map<Sentiment, Double> tokenProbs = tokenLogProbs.get(token);
                if (tokenProbs != null) {
                    logPPos += tokenProbs.get(Sentiment.POSITIVE);
                    logPNeg += tokenProbs.get(Sentiment.NEGATIVE);
                } else {
                    //if we've never seen the word before, ignore it
                }
            }

            pathMap.put(p, logPPos >= logPNeg ? Sentiment.POSITIVE : Sentiment.NEGATIVE);
        }

        return pathMap;
    }


}
