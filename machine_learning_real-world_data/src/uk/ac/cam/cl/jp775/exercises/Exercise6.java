package uk.ac.cam.cl.jp775.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise6;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.NuancedSentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Exercise6 implements IExercise6 {
    /**
     * Calculate the probability of a document belonging to a given class based
     * on the training data.
     *
     * @param trainingSet
     *            {@link Map}<{@link Path}, {@link NuancedSentiment}> Training review
     *            paths
     * @return {@link Map}<{@link NuancedSentiment}, {@link Double}> Class
     *         probabilities.
     * @throws IOException
     */
    public Map<NuancedSentiment, Double> calculateClassProbabilities(Map<Path, NuancedSentiment> trainingSet) throws IOException {
        Map<NuancedSentiment, Double> classProbabilities = new HashMap<NuancedSentiment, Double>();
        int pos = 0;
        int neutral = 0;
        int neg = 0;
        for (NuancedSentiment s : trainingSet.values()) {
            switch (s) {
                case POSITIVE:
                    pos++;
                    break;
                case NEGATIVE:
                    neg++;
                    break;
                default:
                    neutral++;
            }
        }
        int total = trainingSet.size();
        classProbabilities.put(NuancedSentiment.POSITIVE, (double) pos / total);
        classProbabilities.put(NuancedSentiment.NEGATIVE, (double) neg / total);
        classProbabilities.put(NuancedSentiment.NEUTRAL, (double) neutral / total);
        return classProbabilities;
    }

    /**
     * Modify your smoothed Naive Bayes to calculate log probabilities for three classes.
     *
     * @param trainingSet
     *            {@link Map}<{@link Path}, {@link NuancedSentiment}> Training review
     *            paths
     * @return {@link Map}<{@link String}, {@link Map}<{@link NuancedSentiment},
     *         {@link Double}>> Estimated log probabilities
     * @throws IOException
     */
    public Map<String, Map<NuancedSentiment, Double>> calculateNuancedLogProbs(Map<Path, NuancedSentiment> trainingSet)
            throws IOException {

        Map<String, Map<NuancedSentiment, Double>> logProbs = new HashMap<String, Map<NuancedSentiment, Double>>();

        //indices: positive, neutral, negative
        Map<String, int[]> wordCounts = new HashMap<String, int[]>();
        int[] totals = new int[]{0, 0, 0};
        for (Path p : trainingSet.keySet()) {
            List<String> tokens = Tokenizer.tokenize(p);
            for (String token : tokens) {
                if (!wordCounts.containsKey(token)) {
                    wordCounts.put(token, new int[]{1, 1, 1});
                    totals[0]++;
                    totals[1]++;
                    totals[2]++;
                }
                int index = trainingSet.get(p).ordinal();
                wordCounts.get(token)[index]++;
                totals[index]++;
            }
        }

        for (String token : wordCounts.keySet()) {
            int[] value = wordCounts.get(token);
            double posProb = (double) value[0] / totals[0];
            double neutralProb = (double) value[1] / totals[1];
            double negProb = (double) value[2] / totals[2];
            Map<NuancedSentiment, Double> logMap = new HashMap<NuancedSentiment, Double>();
            logMap.put(NuancedSentiment.POSITIVE, Math.log(posProb));
            logMap.put(NuancedSentiment.NEUTRAL, Math.log(neutralProb));
            logMap.put(NuancedSentiment.NEGATIVE, Math.log(negProb));
            logProbs.put(token, logMap);
        }

        return logProbs;
    }

    /**
     * Modify your Naive Bayes classifier so that it can classify reviews which
     * may also have neutral sentiment.
     *
     * @param testSet
     *            {@link Set}<{@link Path}> Test review paths
     * @param tokenLogProbs
     *            {@link Map}<{@link String}, {@link Map}<{@link NuancedSentiment}, {@link Double}> tokenLogProbs
     * @param classProbabilities
     * 			{@link Map}<{@link NuancedSentiment}, {@link Double}> classProbabilities
     * @return {@link Map}<{@link Path}, {@link NuancedSentiment}> Predicted sentiments
     * @throws IOException
     */
    public 	Map<Path, NuancedSentiment> nuancedClassifier(Set<Path> testSet,
                                                            Map<String, Map<NuancedSentiment, Double>> tokenLogProbs,
                                                            Map<NuancedSentiment, Double> classProbabilities)
            throws IOException {

        Map<Path, NuancedSentiment> pathMap = new HashMap<Path, NuancedSentiment>();

        for (Path p : testSet) {
            List<String> tokens = Tokenizer.tokenize(p);
            double logPPos = Math.log(classProbabilities.get(NuancedSentiment.POSITIVE));
            double logPNeu = Math.log(classProbabilities.get(NuancedSentiment.NEUTRAL));
            double logPNeg = Math.log(classProbabilities.get(NuancedSentiment.NEGATIVE));
            for (String token : tokens) {
                Map<NuancedSentiment, Double> tokenProbs = tokenLogProbs.get(token);
                if (tokenProbs != null) {
                    logPPos += tokenProbs.get(NuancedSentiment.POSITIVE);
                    logPNeu += tokenProbs.get(NuancedSentiment.NEUTRAL);
                    logPNeg += tokenProbs.get(NuancedSentiment.NEGATIVE);
                } else {
                    //if we've never seen the word before, ignore it
                }
            }

            if (logPPos >= logPNeu && logPPos >= logPNeg) {
                pathMap.put(p, NuancedSentiment.POSITIVE);
            } else if (logPNeu >= logPPos && logPNeu >= logPNeg) {
                pathMap.put(p, NuancedSentiment.NEUTRAL);
            } else {
                pathMap.put(p, NuancedSentiment.NEGATIVE);
            }

        }

        return pathMap;

    }
    /**
     * Calculate the proportion of predicted sentiments that were correct.
     *
     * @param trueSentiments
     *            {@link Map}<{@link Path}, {@link NuancedSentiment}> Map of
     *            correct sentiment for each review
     * @param predictedSentiments
     *            {@link Map}<{@link Path}, {@link NuancedSentiment}> Map of
     *            calculated sentiment for each review
     * @return <code>double</code> The overall accuracy of the predictions
     */
    public double nuancedAccuracy(Map<Path, NuancedSentiment> trueSentiments,
                                  Map<Path, NuancedSentiment> predictedSentiments) {
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

    /**
     * Given some predictions about the sentiment in reviews, generate an
     * agreement table which for each review contains the number of predictions
     * that predicted each sentiment.
     *
     * @param predictedSentiments
     *            {@link Collection}<{@link Map}<{@link Integer},
     *            {@link Sentiment}>> Different predictions for the
     *            sentiment in each of a set of reviews 1, 2, 3, 4.
     * @return {@link Map}<{@link Integer}, {@link Map}<{@link Sentiment},
     *         {@link Integer}>> For each review, the number of predictions that
     *         predicted each sentiment
     */
    public Map<Integer, Map<Sentiment, Integer>> agreementTable(Collection<Map<Integer, Sentiment>> predictedSentiments) {
        Map<Integer, Map<Sentiment, Integer>> agreementTable = new HashMap<>();
        for (Map<Integer, Sentiment> m : predictedSentiments) {
            for (Integer reviewKey : m.keySet()) {
                if (!agreementTable.containsKey(reviewKey)) {
                    Map<Sentiment, Integer> freshMap = new HashMap<>();
                    freshMap.put(Sentiment.POSITIVE, 0);
                    freshMap.put(Sentiment.NEGATIVE, 0);
                    agreementTable.put(reviewKey, freshMap);
                }
                Sentiment sentiment = m.get(reviewKey);
                Map<Sentiment, Integer> countMap = agreementTable.get(reviewKey);
                int count = countMap.get(sentiment);
                countMap.put(sentiment, count + 1);
            }
        }
        return agreementTable;
    }

    /**
     * Using your agreement table, calculate the kappa value for how much
     * agreement there was; 1 should mean total agreement and -1 should mean total disagreement.
     *
     * @param agreementTable
     *            {@link Map}<{@link Integer}, {@link Map}<{@link Sentiment},
     *            {@link Integer}>> For each review (1, 2, 3, 4) the number of predictions
     *            that predicted each sentiment
     * @return <code>double</code> The kappa value, between -1 and 1
     */
    public double kappa(Map<Integer, Map<Sentiment, Integer>> agreementTable) {
        Map<Integer, Integer> Nis = new HashMap<Integer, Integer>();
        Set<Sentiment> sentiments = new HashSet<Sentiment>();
        //calculating Pa
        double pa_sum_i = 0;
        int N = agreementTable.size();
        for (Integer id : agreementTable.keySet()) {
            Map<Sentiment, Integer> entry = agreementTable.get(id);
            int n_i = 0;
            int pa_sum_j = 0;
            for (Sentiment s : entry.keySet()) {
                int n_ij = entry.get(s);
                n_i += n_ij;
                pa_sum_j += n_ij * (n_ij - 1);
                sentiments.add(s);
            }
            pa_sum_i += (double) pa_sum_j / (n_i * (n_i - 1));
            Nis.put(id, n_i);
        }

        double Pa = pa_sum_i / N;

        double Pe = 0;
        double pe_sum_j = 0;
        for (Sentiment s : sentiments) {
            double pe_sum_i = 0;
            for (Integer id : agreementTable.keySet()) {
                if (agreementTable.get(id).containsKey(s)) {
                    int n_ij = agreementTable.get(id).get(s);
                    pe_sum_i += (double) n_ij / Nis.get(id);
                }
            }
            pe_sum_j += Math.pow(pe_sum_i / N, 2);
        }
        Pe = pe_sum_j;

        double kappa = (Pa - Pe) / (1 - Pe);
        return kappa;
    }

}
