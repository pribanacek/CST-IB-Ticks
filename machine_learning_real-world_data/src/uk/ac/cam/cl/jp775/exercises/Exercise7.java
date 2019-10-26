package uk.ac.cam.cl.jp775.exercises;

import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Exercise7 implements IExercise7 {

    /**
     * Loads the sequences of visible and hidden states from the sequence files
     * (visible dice rolls on first line and hidden dice types on second) and uses
     * them to estimate the parameters of the Hidden Markov Model that generated
     * them.
     *
     * @param sequenceFiles
     *            {@link Collection}<{@link Path}> The files containing dice roll
     *            sequences
     * @return {@link HiddenMarkovModel}<{@link DiceRoll}, {@link DiceType}> The
     *         estimated model
     * @throws IOException
     */
    public HiddenMarkovModel<DiceRoll, DiceType> estimateHMM(Collection<Path> sequenceFiles) throws IOException {
        List<HMMDataStore<DiceRoll, DiceType>> diceFiles = HMMDataStore.loadDiceFiles(sequenceFiles);

        Map<DiceType, Map<DiceType, Integer>> transitionMatrixCounts = getEmptyTransitionMatrixCounts();
        Map<DiceType, Map<DiceRoll, Integer>> emissionMatrixCounts = getEmptyEmissionMatrixCounts();

        for (HMMDataStore<DiceRoll, DiceType> data : diceFiles) {

            List<DiceType> hidden = data.hiddenSequence;
            List<DiceRoll> observed = data.observedSequence;
            assert hidden.size() == observed.size();

            for (int i = 0; i < hidden.size() - 1; i++) {
                DiceType from = hidden.get(i);
                DiceType to = hidden.get(i + 1);
                incrementMap(transitionMatrixCounts, from, to);

                DiceRoll observation = observed.get(i);
                incrementMap(emissionMatrixCounts, from, observation);
            }
            incrementMap(emissionMatrixCounts, DiceType.END, DiceRoll.END);
        }


        Map<DiceType, Map<DiceType, Double>> transitionMatrix = getEmptyTransitionMatrix();
        Map<DiceType, Map<DiceRoll, Double>> emissionMatrix = getEmptyEmissionMatrix();

        for (DiceType roll : transitionMatrix.keySet()) {
            Map<DiceType, Integer> transitionCounts = transitionMatrixCounts.get(roll);
            int total = 0;
            for (DiceType r : transitionCounts.keySet()) {
                total += transitionCounts.get(r);
            }

            for (DiceType r : transitionCounts.keySet()) {
                double prob = (double) transitionCounts.get(r) / total;
                if (total > 0) {
                    transitionMatrix.get(roll).put(r, prob);
                } else {
                    transitionMatrix.get(roll).put(r, 0.0);
                }
            }

            Map<DiceRoll, Integer> emissionCounts = emissionMatrixCounts.get(roll);
            total = 0;
            for (DiceRoll r : emissionCounts.keySet()) {
                total += emissionCounts.get(r);
            }

            for (DiceRoll r : emissionCounts.keySet()) {
                double prob = (double) emissionCounts.get(r) / total;
                if (total > 0) {
                    emissionMatrix.get(roll).put(r, prob);
                } else {
                    emissionMatrix.get(roll).put(r, 0.0);
                }
            }
        }

        HiddenMarkovModel<DiceRoll, DiceType> model = new HiddenMarkovModel<DiceRoll, DiceType>(transitionMatrix, emissionMatrix);
        return model;
    }

    private static void incrementMap(Map map, Object key1, Object key2) {
        Map map2 = (Map) map.get(key1);
        int i = (Integer) map2.get(key2);
        map2.put(key2, i + 1);
    }

    private static Map<DiceType, Map<DiceType, Integer>> getEmptyTransitionMatrixCounts() {
        Map<DiceType, Map<DiceType, Integer>> transitionMatrix = new HashMap<>();
        for (DiceType fromRoll : DiceType.values()) {
            Map<DiceType, Integer> map = new HashMap<>();
            for (DiceType toRoll : DiceType.values()) {
                map.put(toRoll, 0);
            }
            transitionMatrix.put(fromRoll, map);
        }
        return transitionMatrix;
    }

    private static Map<DiceType, Map<DiceRoll, Integer>> getEmptyEmissionMatrixCounts() {
        Map<DiceType, Map<DiceRoll, Integer>> emissionMatrix = new HashMap<>();
        for (DiceType fromRoll : DiceType.values()) {
            Map<DiceRoll, Integer> map = new HashMap<>();
            for (DiceRoll toRoll : DiceRoll.values()) {
                map.put(toRoll, 0);
            }
            emissionMatrix.put(fromRoll, map);
        }
        return emissionMatrix;
    }


    private static Map<DiceType, Map<DiceType, Double>> getEmptyTransitionMatrix() {
        Map<DiceType, Map<DiceType, Double>> transitionMatrix = new HashMap<>();
        for (DiceType fromRoll : DiceType.values()) {
            Map<DiceType, Double> map = new HashMap<>();
            for (DiceType toRoll : DiceType.values()) {
                map.put(toRoll, 0.0);
            }
            transitionMatrix.put(fromRoll, map);
        }
        return transitionMatrix;
    }

    private static Map<DiceType, Map<DiceRoll, Double>> getEmptyEmissionMatrix() {
        Map<DiceType, Map<DiceRoll, Double>> emissionMatrix = new HashMap<>();
        for (DiceType fromRoll : DiceType.values()) {
            Map<DiceRoll, Double> map = new HashMap<>();
            for (DiceRoll toRoll : DiceRoll.values()) {
                map.put(toRoll, 0.0);
            }
            emissionMatrix.put(fromRoll, map);
        }
        return emissionMatrix;
    }


}
