package uk.ac.cam.cl.jp775.exercises;

import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Exercise9 implements IExercise9 {

    /**
     * Loads the sequences of visible and hidden states from the sequence files
     * (visible amino acids on first line and hidden features on second) and
     * uses them to estimate the parameters of the Hidden Markov Model that
     * generated them.
     *
     * @param bioDataFiles
     *            {@link Collection}<{@link Path}> The files containing amino
     *            acid sequences
     * @return {@link HiddenMarkovModel}<{@link AminoAcid}, {@link Feature}> The
     *         estimated model
     * @throws IOException
     */
    public HiddenMarkovModel<AminoAcid, Feature> estimateHMM(List<HMMDataStore<AminoAcid, Feature>> sequencePairs) throws IOException {

        Map<Feature, Map<Feature, Integer>> transitionMatrixCounts = getEmptyTransitionMatrixCounts();
        Map<Feature, Map<AminoAcid, Integer>> emissionMatrixCounts = getEmptyEmissionMatrixCounts();

        for (HMMDataStore<AminoAcid, Feature> data : sequencePairs) {

            List<Feature> hidden = data.hiddenSequence;
            List<AminoAcid> observed = data.observedSequence;
            assert hidden.size() == observed.size();

            for (int i = 0; i < hidden.size() - 1; i++) {
                Feature from = hidden.get(i);
                Feature to = hidden.get(i + 1);
                incrementMap(transitionMatrixCounts, from, to);

                AminoAcid observation = observed.get(i);
                incrementMap(emissionMatrixCounts, from, observation);
            }
            incrementMap(emissionMatrixCounts, Feature.END, AminoAcid.END);
        }


        Map<Feature, Map<Feature, Double>> transitionMatrix = getEmptyTransitionMatrix();
        Map<Feature, Map<AminoAcid, Double>> emissionMatrix = getEmptyEmissionMatrix();

        for (Feature roll : transitionMatrix.keySet()) {
            Map<Feature, Integer> transitionCounts = transitionMatrixCounts.get(roll);
            int total = 0;
            for (Feature r : transitionCounts.keySet()) {
                total += transitionCounts.get(r);
            }

            for (Feature r : transitionCounts.keySet()) {
                double prob = (double) transitionCounts.get(r) / total;
                if (total > 0) {
                    transitionMatrix.get(roll).put(r, prob);
                } else {
                    transitionMatrix.get(roll).put(r, 0.0);
                }
            }

            Map<AminoAcid, Integer> emissionCounts = emissionMatrixCounts.get(roll);
            total = 0;
            for (AminoAcid r : emissionCounts.keySet()) {
                total += emissionCounts.get(r);
            }

            for (AminoAcid r : emissionCounts.keySet()) {
                double prob = (double) emissionCounts.get(r) / total;
                if (total > 0) {
                    emissionMatrix.get(roll).put(r, prob);
                } else {
                    emissionMatrix.get(roll).put(r, 0.0);
                }
            }
        }

        HiddenMarkovModel<AminoAcid, Feature> model = new HiddenMarkovModel<AminoAcid, Feature>(transitionMatrix, emissionMatrix);
        return model;
    }

    /**
     * Uses the Viterbi algorithm to calculate the most likely single sequence
     * of hidden states given the observed sequence.
     *
     * @param model
     *            A pre-trained HMM.
     * @param observedSequence
     *            {@link List}<{@link AminoAcid}> A sequence of observed amino
     *            acids
     * @return {@link List}<{@link Feature}> The most likely single sequence of
     *         hidden states
     */
    public List<Feature> viterbi(HiddenMarkovModel<AminoAcid, Feature> model, List<AminoAcid> observedSequence) {
        List<Map<Feature, Feature>> phis = new ArrayList<>();
        List<Map<Feature, Double>> deltas = new ArrayList<>();

        phis.add(new HashMap<>());
        phis.get(0).put(Feature.START, null);
        deltas.add(new HashMap<>());
        deltas.get(0).put(Feature.START, 0.0);

        Map<Feature, Map<Feature, Double>> transition = model.getTransitionMatrix();
        Map<Feature, Map<AminoAcid, Double>> emission = model.getEmissionMatrix();

        for (int t = 1; t < observedSequence.size(); t++) {
            HashMap<Feature, Double> newProbabilities = new HashMap<>();
            HashMap<Feature, Feature> newMostLikelies = new HashMap<>();
            for (Feature currentState : Feature.values()) {
                double maxProb = Double.NEGATIVE_INFINITY;
                Feature from = null;
                for (Feature prevState : deltas.get(t-1).keySet()) {
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
        List<Feature> viterbiPath = new ArrayList<>();
        Feature currentType = Feature.END;
        int t = observedSequence.size() - 1;
        while (currentType != null) {
            viterbiPath.add(0, currentType);
            currentType = phis.get(t).get(currentType);
            t--;
        }

        return viterbiPath;
    }

    /**
     * Uses the Viterbi algorithm to predict hidden sequences of all observed
     * sequences in testSequencePairs.
     *
     * @param model
     *            The HMM model.
     * @param testSequencePair
     *            A list of {@link HMMDataStore}s with observed and true hidden
     *            sequences.
     * @return {@link Map}<{@link List}<{@link Feature}>,
     *         {@link Feature}<{@link Feature}>> A map from a real hidden
     *         sequence to the equivalent estimated hidden sequence.
     * @throws IOException
     */
    public Map<List<Feature>, List<Feature>> predictAll(HiddenMarkovModel<AminoAcid, Feature> model,
                                                 List<HMMDataStore<AminoAcid, Feature>> testSequencePairs) throws IOException {
        Map<List<Feature>, List<Feature>> map = new HashMap<>();
        for (HMMDataStore<AminoAcid, Feature> dataStore : testSequencePairs) {
            List<Feature> predictedSequence = viterbi(model, dataStore.observedSequence);
            map.put(dataStore.hiddenSequence, predictedSequence);
        }
        return map;
    }

    /**
     * Calculates the precision of the estimated sequence with respect to the
     * membrane state, i.e. the proportion of predicted membrane states that
     * were actually in the membrane.
     *
     * @param true2PredictedMap
     *            {@link Map}<{@link List}<{@link Feature}>,
     *            {@link List}<{@link Feature}>> A map from a real hidden
     *            sequence to the equivalent estimated hidden sequence.
     * @return <code>double</code> The precision of the estimated sequence with
     *         respect to the membrane state averaged over all the test
     *         sequences.
     */
    public double precision(Map<List<Feature>, List<Feature>> true2PredictedMap) {
        int correct = 0;
        int predicted = 0;

        for (List<Feature> trueTypes : true2PredictedMap.keySet()) {
            for (int i = 0; i < trueTypes.size(); i++) {
                Feature prediction = true2PredictedMap.get(trueTypes).get(i);
                Feature trueType = trueTypes.get(i);
                if (prediction == Feature.MEMBRANE) {
                    if (trueType == Feature.MEMBRANE) {
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
     * Calculate the recall for the membrane state.
     *
     * @param true2PredictedMap
     *            {@link Map}<{@link List}<{@link Feature}>,
     *            {@link List}<{@link Feature}>> A map from a real hidden
     *            sequence to the equivalent estimated hidden sequence.
     * @return The recall for the membrane state.
     */
    public double recall(Map<List<Feature>, List<Feature>> true2PredictedMap) {
        int correct = 0;
        int trueGuess = 0;

        for (List<Feature> trueTypes : true2PredictedMap.keySet()) {
            for (int i=0; i<trueTypes.size(); i++) {
                Feature prediction = true2PredictedMap.get(trueTypes).get(i);
                Feature trueType = trueTypes.get(i);
                if (trueType == Feature.MEMBRANE) {
                    if (prediction == Feature.MEMBRANE) {
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
     * Calculate the F1 score for the membrane state.
     *
     * @param true2PredictedMap
     */
    public double fOneMeasure(Map<List<Feature>, List<Feature>> true2PredictedMap) {
        double precision = precision(true2PredictedMap);
        double recall = recall(true2PredictedMap);
        double f1 = 2.0 * precision * recall / (precision + recall);
        return f1;
    }

    private static void incrementMap(Map map, Object key1, Object key2) {
        Map map2 = (Map) map.get(key1);
        int i = (Integer) map2.get(key2);
        map2.put(key2, i + 1);
    }

    private static Map<Feature, Map<Feature, Integer>> getEmptyTransitionMatrixCounts() {
        Map<Feature, Map<Feature, Integer>> transitionMatrix = new HashMap<>();
        for (Feature fromRoll : Feature.values()) {
            Map<Feature, Integer> map = new HashMap<>();
            for (Feature toRoll : Feature.values()) {
                map.put(toRoll, 0);
            }
            transitionMatrix.put(fromRoll, map);
        }
        return transitionMatrix;
    }

    private static Map<Feature, Map<AminoAcid, Integer>> getEmptyEmissionMatrixCounts() {
        Map<Feature, Map<AminoAcid, Integer>> emissionMatrix = new HashMap<>();
        for (Feature fromRoll : Feature.values()) {
            Map<AminoAcid, Integer> map = new HashMap<>();
            for (AminoAcid toRoll : AminoAcid.values()) {
                map.put(toRoll, 0);
            }
            emissionMatrix.put(fromRoll, map);
        }
        return emissionMatrix;
    }


    private static Map<Feature, Map<Feature, Double>> getEmptyTransitionMatrix() {
        Map<Feature, Map<Feature, Double>> transitionMatrix = new HashMap<>();
        for (Feature fromRoll : Feature.values()) {
            Map<Feature, Double> map = new HashMap<>();
            for (Feature toRoll : Feature.values()) {
                map.put(toRoll, 0.0);
            }
            transitionMatrix.put(fromRoll, map);
        }
        return transitionMatrix;
    }

    private static Map<Feature, Map<AminoAcid, Double>> getEmptyEmissionMatrix() {
        Map<Feature, Map<AminoAcid, Double>> emissionMatrix = new HashMap<>();
        for (Feature fromRoll : Feature.values()) {
            Map<AminoAcid, Double> map = new HashMap<>();
            for (AminoAcid toRoll : AminoAcid.values()) {
                map.put(toRoll, 0.0);
            }
            emissionMatrix.put(fromRoll, map);
        }
        return emissionMatrix;
    }


}
