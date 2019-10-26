package uk.ac.cam.cl.jp775.exercises;

import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Exercise8 implements IExercise8 {

    /**
     * Uses the Viterbi algorithm to calculate the most likely single sequence
     * of hidden states given the observed sequence and a model.
     *
     * @param model
     *            {@link HiddenMarkovModel}<{@link DiceRoll}, {@link DiceType}>
     *            A sequence model.
     * @param observedSequence
     *            {@link List}<{@link DiceRoll}> A sequence of observed die
     *            rolls
     * @return {@link List}<{@link DiceType}> The most likely single sequence of
     *         hidden states
     */
    public List<DiceType> viterbi(HiddenMarkovModel<DiceRoll, DiceType> model, List<DiceRoll> observedSequence) {
        List<Map<DiceType, DiceType>> phis = new ArrayList<>();
        List<Map<DiceType, Double>> deltas = new ArrayList<>();

        phis.add(new HashMap<>());
        phis.get(0).put(DiceType.START, null);
        deltas.add(new HashMap<>());
        deltas.get(0).put(DiceType.START, 0.0);

        Map<DiceType, Map<DiceType, Double>> transition = model.getTransitionMatrix();
        Map<DiceType, Map<DiceRoll, Double>> emission = model.getEmissionMatrix();

        for (int t = 1; t < observedSequence.size(); t++) { // for each time step
            HashMap<DiceType, Double> newProbabilities = new HashMap<>();
            HashMap<DiceType, DiceType> newMostLikelies = new HashMap<>();
            for (DiceType currentState : DiceType.values()) { //for each type could go to
                double maxProb = Double.NEGATIVE_INFINITY;
                DiceType from = null;
                for (DiceType prevState : deltas.get(t-1).keySet()) {
                    double prev_delta = deltas.get(t-1).get(prevState);
                    double b = Math.log(emission.get(currentState).get(observedSequence.get(t)));
                    double a_ij = Math.log(transition.get(prevState).get(currentState));
                    double prob = prev_delta + b + a_ij;
                    if (prob > maxProb) {
                        maxProb = prob;
                        from = prevState;
                    }
                }
                newProbabilities.put(currentState, maxProb);
                newMostLikelies.put(currentState, from);
            }
            deltas.add(newProbabilities);
            phis.add(newMostLikelies);
        }

        // Backtracking
        List<DiceType> out = new ArrayList<>();
        DiceType currentType = DiceType.END;
        int t = observedSequence.size() - 1;
        while (currentType != null) {
            out.add(0, currentType);
            currentType = phis.get(t).get(currentType);
            t--;
        }

        return out;
    }

    /**
     * Uses the Viterbi algorithm to predict hidden sequences of all observed
     * sequences in testFiles.
     *
     * @param model
     *            The HMM model.
     * @param testFiles
     *            A list of files with observed and true hidden sequences.
     * @return {@link Map}<{@link List}<{@link DiceType}>,
     *         {@link List}<{@link DiceType}>> A map from a real hidden sequence
     *         to the equivalent estimated hidden sequence.
     * @throws IOException
     */
    public Map<List<DiceType>, List<DiceType>> predictAll(HiddenMarkovModel<DiceRoll, DiceType> model, List<Path> testFiles) throws IOException {
        Map<List<DiceType>, List<DiceType>> map = new HashMap<>();
        for (Path p : testFiles) {
            HMMDataStore<DiceRoll, DiceType> dataStore = HMMDataStore.loadDiceFile(p);
            List<DiceType> predictedSequence = viterbi(model, dataStore.observedSequence);
            map.put(dataStore.hiddenSequence, predictedSequence);
        }
        return map;
    }

    /**
     * Calculates the precision of the estimated sequence with respect to the
     * weighted state, i.e. the proportion of predicted weighted states that
     * were actually weighted.
     *
     * @param true2PredictedMap
     *            {@link Map}<{@link List}<{@link DiceType}>,
     *            {@link List}<{@link DiceType}>> A map from a real hidden
     *            sequence to the equivalent estimated hidden sequence.
     * @return <code>double</code> The precision of the estimated sequence with
     *         respect to the weighted state averaged over all the test
     *         sequences.
     */
    public double precision(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
        int correct = 0;
        int predicted = 0;

        for (List<DiceType> trueTypes : true2PredictedMap.keySet()) {
            for (int i = 0; i < trueTypes.size(); i++) {
                DiceType prediction = true2PredictedMap.get(trueTypes).get(i);
                DiceType trueType = trueTypes.get(i);
                if (prediction == DiceType.WEIGHTED) {
                    if (trueType == DiceType.WEIGHTED) {
                        correct++;
                    }
                    predicted++;
                }
            }
        }
        double precision = (double) correct/predicted;
        return precision;
    }

    /**
     * Calculates the recall of the estimated sequence with respect to the
     * weighted state, i.e. the proportion of actual weighted states that were
     * predicted weighted.
     *
     * @param true2PredictedMap
     *            {@link Map}<{@link List}<{@link DiceType}>,
     *            {@link List}<{@link DiceType}>> A map from a real hidden
     *            sequence to the equivalent estimated hidden sequence.
     * @return <code>double</code> The recall of the estimated sequence with
     *         respect to the weighted state averaged over all the test
     *         sequences.
     */
    public double recall(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
        int correct = 0;
        int trueGuess = 0;

        for (List<DiceType> trueTypes : true2PredictedMap.keySet()) {
            for (int i=0; i<trueTypes.size(); i++) {
                DiceType prediction = true2PredictedMap.get(trueTypes).get(i);
                DiceType trueType = trueTypes.get(i);
                if (trueType == DiceType.WEIGHTED) {
                    if (prediction == DiceType.WEIGHTED) {
                        correct++;
                    }
                    trueGuess++;
                }
            }
        }
        double recall = (double) correct / trueGuess;
        return recall;
    }

    /**
     * Calculates the F1 measure of the estimated sequence with respect to the
     * weighted state, i.e. the harmonic mean of precision and recall.
     *
     * @param true2PredictedMap
     *            {@link Map}<{@link List}<{@link DiceType}>,
     *            {@link List}<{@link DiceType}>> A map from a real hidden
     *            sequence to the equivalent estimated hidden sequence.
     * @return <code>double</code> The F1 measure of the estimated sequence with
     *         respect to the weighted state averaged over all the test
     *         sequences.
     */
    public double fOneMeasure(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
        double precision = precision(true2PredictedMap);
        double recall = recall(true2PredictedMap);
        double f1 = 2.0 * precision * recall / (precision + recall);
        return f1;
    }

}
