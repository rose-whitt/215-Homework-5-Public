package main.rice.basegen;

import main.rice.node.APyNode;
import main.rice.obj.APyObj;
import main.rice.test.TestCase;

import java.util.*;

public class BaseSetGenerator {

    // fields
    /**
     * field for the list of nodes encapsulating the type and domain specifications
     * for each parameter to the function under test passed through constructor
     */
    private List<APyNode<?>> nodeTypeDomainList;

    /**
     * field for an integer representing the number of random tests cases to generate
     * passed through the constructor
     */
    private int randNum;

    /**
     * Constructor for a BaseSetGenerator.
     * @param nodes : list of nodes encapsulating the type and domain specifications
     *                      for each parameter to the function under test
     * @param numRand : an integer representing the number of random tests cases to generate
     */
    public BaseSetGenerator(List<APyNode<?>> nodes, int numRand) {
        this.nodeTypeDomainList = nodes;
        this.randNum = numRand;
//        System.out.println("nodes: " + this.nodeTypeDomainList);
//        System.out.println("random number: " + this.randNum);
//        nodeTypePrinter();
//        nodesHelper();
    }

    public void nodeTypePrinter() {
        int index = 0;
        for (APyNode<?> node : this.nodeTypeDomainList) {
            System.out.println("index " + index + ": " + node.getClass());
            index++;
        }
    }

    public void nodesHelper() {
        int index = 0;
        for (APyNode<?> node : this.nodeTypeDomainList) {
            System.out.println("index: " + index);
            System.out.println("Exhaustive Domain: " + node.getExDomain());
            System.out.println("Random Domain: " + node.getRanDomain());
            index++;
        }
    }

    public void setPrinter(Set<? extends APyObj> set) {
        for (Object elem : set) {
            System.out.println(elem);
        }
    }


    /**
     * Generates the base test set, which should be the union of all tests within
     *      the exhaustive domain and a selection of tests within the random domain.
     *      The random tests should be generated fresh on each call to genBaseSet().
     *
     * @return : a list rather than a set so that we can use its indices within the Tester
     *                  in order to improve space efficiency.
     */
    public List<TestCase> genBaseSet() {
        // get exhaustive set of tests
        Set<TestCase> exTests = genExTests();

        // get random set of tests
        Set<TestCase> randTests = genRandTests();

        // create list of test cases
        List<TestCase> baseSetTests = new ArrayList<>();

        // add all of exTests
        for (TestCase test : exTests) {
            baseSetTests.add(test);
        }

        // add random set of tests
        for (TestCase randTest : randTests) {
            baseSetTests.add(randTest);
        }

        return baseSetTests;
    }


    /**
     * Helper for genExTests that recursively generates possible TestCase
     *      argument lists based off of exhaustive domains
     *
     * @param nodeIndex : node index of the object you're currently adding to accumulation
     * @param nodeExDoms
     * @return : a set of lists where the type of the ith element of each list is the type of the ith
     *              element in the nodeTypeDomainList field
     */
    public Set<List<? extends APyObj>> genExTestsRecHelper(int nodeIndex, List<Set<? extends APyObj>> nodeExDoms) {
        //      BASE CASE

        // when length is zero
        if (nodeIndex == 0) {
            // create return set list
            Set<List<? extends APyObj>> minRetSet = new HashSet<>(Set.of(new ArrayList<>()));

            // for each APyObj in the exhaustive domain at index 0
            for (APyObj obj : nodeExDoms.get(0)) {
                // create a list containing just the single APyObj
                List<APyObj> temp = new ArrayList(List.of(obj));
                // add the list to the return set
                minRetSet.add(temp);
            }

            // return the set
            return minRetSet;
        }

        else {
            //      accumulation
            Set<List<? extends APyObj>> subsets = new HashSet<>(Set.of(new ArrayList<>()));
            //      permutations, recursive call
            Set<List<? extends APyObj>> shortPerms = genExTestsRecHelper(nodeIndex - 1, nodeExDoms);

            // for each permutation of length - 1
            for (List<? extends APyObj> shortPerm : shortPerms) {
                // for each value to be added
                for (APyObj exVal : nodeExDoms.get(nodeIndex)) {
                    // copy the previous permutation of lower length
                    List<APyObj> temp = new ArrayList<>(shortPerm);
                    // add your value to that permutation
                    temp.add(exVal);
                    // add the updated permutation to your subsets
                    subsets.add(temp);
                }
            }

            // return your subsets, each element of the given length
            return subsets;

        }
    }

    /**
     * Generates and returns the set of all test cases adhering to the type and
     *          exhaustive domain specifications in the list of nodes that was
     *          passed during construction.
     * @return : retSet : a set of TestCases adhering to specifications above
     */
    public Set<TestCase> genExTests() {
        //      PRE PROCESSING

        // accumulate exhaustive domains for each node in one place for convience
        List<Set<? extends APyObj>> nodeExDoms = new ArrayList<>();

        // for each node
        for (APyNode<?> node : this.nodeTypeDomainList) {
            Set<? extends APyObj> temp = node.genExVals();
            nodeExDoms.add(temp);
        }

        // --------

        //      RECURSION
        Set<List<? extends APyObj>> helper = genExTestsRecHelper(this.nodeTypeDomainList.size() - 1, nodeExDoms);

        // --------

        //      POST PROCESSING

        // 1) convert to TestCases
        // 2) eliminate lists not of length of node domain thingy

        // create return set
        Set<TestCase> retSet = new HashSet<>();

        // store length for ease/design
        int len = this.nodeTypeDomainList.size();

        // for each possible argument list generated in the recursive step
        for (List<? extends APyObj> possibleTest : helper) {
            // if correct length
            if (possibleTest.size() == len) {
                // make this list a TestCase object with list as arguments
                TestCase temp = new TestCase((List<APyObj>) possibleTest);
                // add this TestCase object to your return set
                retSet.add(temp);
            }

        }

        // return the set of TestCases generated from exhaustive domain
        return retSet;

    }


    /**
     * Generates and returns a random selection of test cases adhering to the type
     *      and random domain specifications in the input list of nodes.
     * @return
     */
    public Set<TestCase> genRandTests() {

        Set<TestCase> randTests = new HashSet<>();

        // while there are less than the desired number of random TestCases
        while (randTests.size() != this.randNum) {
            // create list to hold random arguments for potential TestCase
            List<APyObj> randArgs = new ArrayList<>();

            // for each node in the given domain list
            for (APyNode<?> node : this.nodeTypeDomainList) {
                // add a random value from the node's random domain to the random arguments
                randArgs.add(node.genRandVal());
            }
            // create TestCase from randomly generated arguments
            TestCase tempRandTest = new TestCase(randArgs);

            if (!genExTests().contains(tempRandTest)) {
                randTests.add(tempRandTest);
            }


        }

        // return the set of random tests
        return randTests;

    }
}
