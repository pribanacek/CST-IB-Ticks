package uk.ac.cam.cl.jp775.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise5;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Exercise5 implements IExercise5 {
    /**
     * Split the given data randomly into 10 folds.
     *
     * @param dataSet
     *            {@link Map}<{@link Path}, {@link Sentiment}> All review paths
     *
     * @param seed
     *            A seed for the random shuffling.
     * @return {@link List}<{@link Map}<{@link Path}, {@link Sentiment}>> A set
     *         of folds with even numbers of each sentiment
     */
    public List<Map<Path, Sentiment>> splitCVRandom(Map<Path, Sentiment> dataSet, int seed) {
        int foldCount = 10;
        int foldSize = (int) Math.ceil((double) dataSet.keySet().size() / foldCount);

        Random rand = new Random(seed);
        List<Path> keyList = new ArrayList<Path>(dataSet.keySet());
        Collections.shuffle(keyList, rand);

        List<Map<Path, Sentiment>> folds = new ArrayList<>(foldCount);
        for (int i = 0; i < foldCount; i++) {
            Map<Path, Sentiment> fold = new HashMap<>();
            for (int j = 0; j < foldSize; j++) {
                int index = i * foldSize + j;
                if (index >= keyList.size()) {
                    break;
                }
                Path p = keyList.get(i * foldSize + j);
                fold.put(p, dataSet.get(p));
            }
            folds.add(fold);
        }
        return folds;
    }

    /**
     * Split the given data randomly into 10 folds but so that class proportions
     * are preserved in each fold.
     *
     * @param dataSet
     *            {@link Map}<{@link Path}, {@link Sentiment}> All review paths
     * @param seed
     *            A seed for the random shuffling.
     * @return {@link List}<{@link Map}<{@link Path}, {@link Sentiment}>> A set
     *         of folds with even numbers of each sentiment
     */
    public List<Map<Path, Sentiment>> splitCVStratifiedRandom(Map<Path, Sentiment> dataSet, int seed) {
        int foldCount = 10;
        int foldSize = (int) Math.ceil((double) dataSet.keySet().size() / (foldCount * 2));
        Random rand = new Random(seed);
        List<Path> keyListPos = new ArrayList<Path>();
        List<Path> keyListNeg = new ArrayList<Path>();

        for (Path p : dataSet.keySet()) {
            if (dataSet.get(p) == Sentiment.POSITIVE) {
                keyListPos.add(p);
            } else {
                keyListNeg.add(p);
            }
        }

        Collections.shuffle(keyListPos, rand);
        Collections.shuffle(keyListNeg, rand);

        List<Map<Path, Sentiment>> folds = new ArrayList<>(foldCount);
        for (int i = 0; i < foldCount; i++) {
            Map<Path, Sentiment> fold = new HashMap<>();
            for (int j = 0; j < foldSize; j++) {
                int index = i * foldSize + j;

                if (index >= keyListPos.size()) {
                    break;
                }
                Path pPos = keyListPos.get(i * foldSize + j);
                fold.put(pPos, dataSet.get(pPos));

                if (index >= keyListNeg.size()) {
                    break;
                }
                Path pNeg = keyListNeg.get(i * foldSize + j);
                fold.put(pNeg, dataSet.get(pNeg));
            }
            folds.add(fold);
        }
        return folds;
    }

    private Map<Path, Sentiment> mergeFolds(List<Map<Path, Sentiment>> folds) {
        Map<Path,Sentiment> map = new HashMap<>();
        for(Map<Path,Sentiment> m : folds){
            map.putAll(m);
        }
        return map;
    }

    /**
     * Run cross-validation on the dataset according to the folds.
     *
     * @param folds
     *            {@link List}<{@link Map}<{@link Path}, {@link Sentiment}>> A
     *            set of folds.
     * @return Scores for individual cross-validation runs.
     * @throws IOException
     */
    public double[] crossValidate(List<Map<Path, Sentiment>> folds) throws IOException {
        List<Map<Path,Sentiment>> temp = new LinkedList<>(folds);
        Map<Path,Sentiment> testSet;
        double[] scoresonthedoors = new double[folds.size()];

        for(int i = 0; i < folds.size(); i++){
            testSet = temp.get(i);
            temp.remove(i);
            Map<Path,Sentiment> dataSet = mergeFolds(temp);

            Exercise2 implementation = new Exercise2();
            Map<String, Map<Sentiment, Double>> smoothedLogProbs = implementation.calculateSmoothedLogProbs(dataSet);
            Map<Sentiment, Double> classProbabilities = implementation.calculateClassProbabilities(dataSet);
            Map<Path, Sentiment> testPredictions = implementation.naiveBayes(testSet.keySet(), smoothedLogProbs, classProbabilities);

            // then calculate its accuracy
            Exercise1 implementation1 = new Exercise1();
            double score = implementation1.calculateAccuracy(testSet, testPredictions);
            scoresonthedoors[i] = score;

            temp = new LinkedList<>(folds);
        }

        return scoresonthedoors;
    }

    /**
     * Calculate the average of the scores.
     *
     * @param scores
     *            A double array with results of individual cross-validation
     *            runs.
     * @return The average cross-validation score.
     */
    public double cvAccuracy(double[] scores) {
        double sum = 0;
        for (double d : scores) {
            sum += d;
        }
        double avg = sum / scores.length;
        return avg;
    }

    /**
     * Calculate the variance of the scores.
     *
     * @param scores
     *            A double array with results of individual cross-validation
     *            runs.
     * @return The variance of cross-validation scores.
     */
    public double cvVariance(double[] scores) {
        double sum = 0;
        double sumSquares = 0;
        for (double d : scores) {
            sum += d;
            sumSquares += d * d;
        }

        double avg = sum / scores.length;
        int n = scores.length;

        double variance = (sumSquares - (n * avg * avg)) / n;

        return variance;
    }

}
