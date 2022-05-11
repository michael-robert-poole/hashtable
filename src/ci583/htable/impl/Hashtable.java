package ci583.htable.impl;

/**
 * A HashTable with no deletions allowed. Duplicates overwrite the existing value. Values are of
 * type V and keys are strings -- one extension is to adapt this class to use other types as keys.
 * <p>
 * The underlying data is stored in the array `arr', and the actual values stored are pairs of
 * (key, value). This is so that we can detect collisions in the hash function and look for the next
 * location when necessary.
 */

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Hashtable<T, V> {

    private Object[] arr; // An array of Pair objects
    private int max; // Size of the Array
    private int itemCount; // Number of Items in the array
    private final double maxLoad = 0.6; // The maximum load factor - itemCount/max

    public static enum PROBE_TYPE {
        LINEAR_PROBE, QUADRATIC_PROBE, DOUBLE_HASH;
    }

    PROBE_TYPE probeType = PROBE_TYPE.LINEAR_PROBE; // the type of probe to use when dealing with collisions
    private BigInteger dblHashPrime; // A prime number less than the size of the Array

    /*

   /**
    * Create a new Hashtable. If the intialCapacity given isn't a prime number arrayLength will be set
    * to the closest prime number bigger than the initialCapacity. The dblHashPrime variable is set to a
    * prime number smaller than arrayLength. A new array is created. The size of this array
    * is the arrayLength calculated from the initialCapacity.
    *
    * @param initialCapacity - Minimum size of the HashTable/Array
    */
    public Hashtable(int initialCapacity) {
        max = nextPrime(initialCapacity);
        dblHashPrime = BigInteger.valueOf(dblHashPrime());
        arr = new Object[max];
    }

    /**
     * Create a new HashTable. The constructor uses the above constructor.
     * It also takes an additional argument to set the probe_type to be used in event of a collision.
     *
     * @param initialCapacity - Minimum size of the HashTable/Array
     * @param pt
     */
    public Hashtable(int initialCapacity, PROBE_TYPE pt) {
        this(initialCapacity); //Call above constructor
        probeType = pt;
    }

    /**
     * A method to put a Pair object into the Array. If the loadFactor is greater than 0.6 then
     * the resize method is invoked. A pair object has a key and a value. The key is hashed  and returns a
     * positive Integer. The findEmpty method returns the first empty index it finds or if key already exists it returns
     * this hash value to be overwritten.
     *
     * @param key   - A variable needed for Pair object constructor
     * @param value - A variable needed for Pair object constructor
     */
    public void put(T key, V value) {

        if (getLoadFactor() > maxLoad) {
            resize();
        }
        // Add new Instance of Pair to array (findEmpty returns an empty index in array)
        int hashValue = hash(key);
        int index = findEmpty(hashValue, key, 0);
        arr[index] = new Pair(key, value);
        itemCount++;

    }

    /**
     * Get the value associated with key, or return null if key does not exists.
     *
     * @param key
     * @return - Value associated with key - or null
     */
    public V get(T key) {
        return find(hash(key), key, 0);
    }

    /**
     * Return true if the Hashtable contains this key, false otherwise
     *
     * @param key
     * @return
     */
    public boolean hasKey(T key) {
        return (find(hash(key), key, 0) != null);
    }

    /**
     * Return all the keys in this Hashtable as an ArrayList
     *
     * @return
     */
    public Collection<T> getKeys() {
        List<T> keyArray = new ArrayList<T>();
        for (int i = 0; i < arr.length; i++) {
            //Iterate through the array. If Pair Object is found - add the String key to the collection
            if (arr[i] != null) {
                Pair pair = getPair(i);
                keyArray.add(pair.key);
            }
        }
        return keyArray;
    }

    /**
     * Return the load factor, which is the ratio of itemCount to arrayLength
     *
     * @return - Returns a number > 0 and <= 1
     */
    public double getLoadFactor() {
        return (double) itemCount / (double) max;
    }

    /**
     * return the maximum capacity of the Hashtable
     *
     * @return
     */
    public int getCapacity() {
        return max;
    }

    /**
     * Find the value stored for this key, starting the search at position startPos
     * in the array. If the item at position startPos is null, the Hashtable does
     * not contain the value, so return null. If the key stored in the pair at
     * position startPos matches the key we're looking for, return the associated
     * value. If the key stored in the pair at position startPos does not match the
     * key we're looking for, this is a hash collision so use the getNextLocation
     * method with an incremented value of stepNum to find the next location to
     * search (the way that this is calculated will differ depending on the probe
     * type being used). Then use the value of the next location in a recursive call
     * to find.
     *
     * @param startPos
     * @param key
     * @param stepNum
     * @return
     */
    private V find(int startPos, T key, int stepNum) {

        if (arr[startPos] == null) {
            //No Pair Object associated with this key has been found
            return null;
        }
        Pair pair = getPair(startPos);
        //compare the key of Pair object assigned to p with key being searched for. Return the value if a match is found
        if (pair.key.equals(key)) {
            return pair.value;
        }

        int nextStartPos = getNextLocation(startPos, key, stepNum++);
        return find(nextStartPos, key, stepNum);
    }


    /**
     * Find the first unoccupied location where a value associated with key can be
     * stored, starting the search at position startPos. If startPos is unoccupied,
     * return startPos. Otherwise use the getNextLocation method with an incremented
     * value of stepNum to find the appropriate next position to check (which will
     * differ depending on the probe type being used) and use this in a recursive
     * call to findEmpty.
     *
     * @param startPos
     * @param stepNum
     * @param key
     * @return
     */
    private int findEmpty(int startPos, T key, int stepNum) {

        if (arr[startPos] == null) {
            return startPos;
        }

        Pair pair = getPair(startPos);
        // If key already exits in Hashtable return the found index to be overwritten with new Pair Object
        if (pair.key.equals(key)) {
            return startPos;
        }
        int nextStartPos = getNextLocation(startPos, key, stepNum++);
        return findEmpty(nextStartPos, key, stepNum);
    }

    /**
     * Return the Pair object at given index
     *
     * @param index
     * @return
     */

    private Pair getPair(int index) {
        return (Pair) arr[index];
    }

    /**
     * Finds the next position in the Hashtable array starting at position startPos.
     * If the linear probe is being used, we just increment startPos. If the double
     * hash probe type is being used, add the double hashed value of the key to
     * startPos. If the quadratic probe is being used, add the square of the step
     * number to startPos.
     *
     * @param i
     * @param stepNum
     * @param key
     * @return
     */
    private int getNextLocation(int startPos, T key, int stepNum) {
        int step = startPos;

        switch (probeType) {
            case LINEAR_PROBE:
                step++;
                break;
            case DOUBLE_HASH:
                step += doubleHash(key);
                break;
            case QUADRATIC_PROBE:
                step += stepNum * stepNum;
                break;
            default:
                break;
        }
        return step % max;
    }

    /**
     * A secondary hash function which returns a small value (less than or equal to
     * dbl_hash_k) to probe the next location if the double hash probe type is being
     * used
     *
     * @param aKey
     * @return
     */
    private int doubleHash(T aKey) {
        String key = aKey.toString();

        BigInteger hashVal = BigInteger.valueOf(key.charAt(0) - 96);
        for (int i = 0; i < key.length(); i++) {
            BigInteger c = BigInteger.valueOf(key.charAt(i) - 96);
            hashVal = hashVal.multiply(BigInteger.valueOf(37)).add(c);
        }
        return 1 + (hashVal.mod(dblHashPrime)).intValue();
    }

    /**
     * Return an integer value calculated by hashing the key. The return value should be less than
     * the maximum capacity of the HashTable.
     *
     * @param aKey
     * @return
     */
    private int hash(T aKey) {
        String key = aKey.toString();

        long hash = 0;
        for (int i = 0; i < key.length(); i++) {
            hash = ((127 * i) * hash + key.charAt(i)) % max;
        }
        return (int) hash;

    }

    /**
     * Return true if n is prime
     *
     * @param n
     * @return
     */
    private boolean isPrime(int n) {

        if (n <= 2) {
            return true;
        }
        for (int i = 3; i * i < n + 1; i += 2) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the smallest prime number which is larger than n
     *
     * @param n
     * @return
     */
    private int nextPrime(int n) {
        // Make n an odd number
        if ((n % 2) == 0) {
            n++;
        }
        // check if n is prime
        while (!isPrime(n)) {
            n += 2;
        }
        return n;
    }

    /**
     * Resize the hashtable, to be used when the load factor exceeds maxLoad. The
     * new size of the underlying array should be the smallest prime number which is
     * at least twice the size of the old array.
     */
    private void resize() {

        Object[] oldArr = arr;
        max *= 2; //Double array Size
        itemCount = 0; //reset itemCount
        max = nextPrime(max);
        arr = new Object[max];
        dblHashPrime = BigInteger.valueOf(dblHashPrime());

        copyOldArray(oldArr);
    }

    /**
     * Find all pair objects in OldArr and put them in new array
     *
     * @param oldArr
     */

    private void copyOldArray(Object oldArr[]) {
        for (int i = 0; i < oldArr.length; i++) {
            if (oldArr[i] != null) {
                Pair p = (Pair) oldArr[i];
                put(p.key, p.value);
            }
        }
    }

    /**
     * Method returning a prime number smaller than the current array size.
     * The returned value is used in the doubleHash function
     *
     * @return
     */
    private int dblHashPrime() {
        int[] primeArray = {3, 11, 23, 47, 89, 167, 293, 563, 1091};
        int nElem;
        for (int i = 1; i < primeArray.length; i++) {
            nElem = primeArray[i - 1];
            if (primeArray[i] > max) {
                return nElem;
            }
        }
        return 1091;
    }


    /**
     * Instances of Pair are stored in the underlying array. We can't just store the
     * value because we need to check the original key in the case of collisions.
     *
     * @author jb259
     */
    public class Pair {
        private T key;
        private V value;

        public Pair(T key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return "Pair [key=" + key + ", value=" + value + "]";
        }

    }


}
