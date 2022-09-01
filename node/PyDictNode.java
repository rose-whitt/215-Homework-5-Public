package main.rice.node;

import main.rice.obj.*;

import java.security.Key;
import java.util.*;

public class PyDictNode<KeyType extends APyObj, ValueType extends APyObj> extends APyNode<PyDictObj<KeyType, ValueType>>{
    /**
     //     * The domain for exhaustive generation.
     //     */
    protected List<Number> exDomain;

    /**
     * The domain for random generation.
     */
    protected List<Number> ranDomain;

    /**
     * The RNG used for random generation.
     */
    protected Random rand = new Random();

    private APyNode<KeyType> innerNodeKey;
    private APyNode<ValueType> innerNodeValue;


    public PyDictNode(APyNode<KeyType> key, APyNode<ValueType> value) {
        innerNodeKey = key;
        innerNodeValue = value;
    }


    /**
     * Returns the left child of this PyDictNode, the keys.
     * @return : the keys of PyDictNode
     */
    public APyNode<KeyType> getLeftChild() {
        return this.innerNodeKey;
    }

    /**
     * Returns the right child of this PyDictNode, the values.
     * @return : the values of PyDictNode
     */
    public APyNode<ValueType> getRightChild() {
        return this.innerNodeValue;
    }

    /**
     * Generates all valid PyDictObjs of type <KeyType, ValueType> within the exhaustive domain.
     *
     * @return a set of PyDictObjs of type <KeyType, ValueType> comprising the exhaustive domain
     */
    public Set<PyDictObj<KeyType, ValueType>> genExVals() {
        // PRE PROCESS
        Set<PyDictObj<KeyType, ValueType>> retDict = new HashSet<>();

        // get maximum length
        List<Number> exDomainCopy = this.getExDomain();
        List<Integer> exDomainInts = new ArrayList<>();

        for (Number len : exDomainCopy) {
            int temp = (int) len;
            exDomainInts.add(temp);
        }
        Collections.sort(exDomainInts);

        int lenSize = this.getExDomain().size();
        int maxVal = exDomainInts.get(lenSize - 1);

        // RECURSION
        Set<KeyType> keyRecurse = this.getLeftChild().genExVals();
        Set<ValueType> valueRecurse = this.getRightChild().genExVals();

        Set<Map<KeyType, ValueType>> helper = genDictKeyValCombos(maxVal, keyRecurse, valueRecurse);


        // POST-PROCESSING
        for (Map<KeyType, ValueType> dict : helper) {
            if (this.getExDomain().contains(dict.size())) {
                PyDictObj dictObj = new PyDictObj(dict);
                retDict.add(dictObj);
            }
        }
        return retDict;

    }

    /**
     * Recursive helper for genExVals that creates all possible key value pairs of a dictionary of maxValue length
     * @param maxValue
     * @return
     */
    public Set<Map<KeyType, ValueType>> genDictKeyValCombos(int maxValue, Set<KeyType> keyRecurse, Set<ValueType> valueRecurse) {
        // length of zero
        if (maxValue == 0) {
            return new HashSet<>(Set.of(new HashMap<>()));
        } else {
            //      accumulation
            Set<Map<KeyType, ValueType>> subsets = new HashSet<>(Set.of(new HashMap<>()));
            //      combinations, recursive call
            Set<Map<KeyType, ValueType>> combos = genDictKeyValCombos(maxValue - 1, keyRecurse, valueRecurse);

            for (Map<KeyType, ValueType> combo : combos) {
                for (KeyType key : keyRecurse) {
                    for (ValueType value : valueRecurse) {
                        Map<KeyType, ValueType> temp = new HashMap<>(combo);
                        if (!temp.containsKey(key)) {
                            temp.put(key, value);
//                        System.out.println("temp: " + temp);
                            subsets.add(temp);
                        }

                    }
                }
//                System.out.println("current subsets: " + subsets);
            }
            System.out.println(" ");
            return subsets;
        }
    }

    /**
     * Generate a random PyDictObj containing random KeyType Value Type (generated by their
     *          own genRandVal()) and is of a randomly selected length from the random domain
     *
     * @return : a PyDictObj of a random length randomly chosen from the random domain
     *              with values of type KeyType Value Type that were randomly generated from
     *              KeyType Value Type's domain
     */
    public PyDictObj<KeyType, ValueType> genRandVal() {
        Map<KeyType, ValueType> randMap = new HashMap<>();

        int len = (int) this.ranDomainChoice();

        while (randMap.size() != len) {
            randMap.put(this.getLeftChild().genRandVal(), this.getRightChild().genRandVal());
        }

        // post processing
        // create PyListObj
        PyDictObj randDict = new PyDictObj(randMap);

        return randDict;
    }
}
