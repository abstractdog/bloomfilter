/**
 * Copyright 2014 Prasanth Jayachandran
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.prasanthj.bloomfilter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Random;

/**
 *
 */
public class TestBloomFilter {
  private static final int COUNT = 100;
  Random rand = new Random(123);
  // From "Less Hashing, Same Performance: Building a Better Bloom Filter"
  // https://www.eecs.harvard.edu/~michaelm/postscripts/rsa2008.pdf
  // "if we choose a large number of distinct elements not in S,
  // the fraction of them that yield false positives is approximately f"
  // So we always get approximately configured false positive probability (not guaranteed to be always <fpp).
  // This will cause test failures sometime, expected=0.05 but actual will be ~0.057
  // To fix this introduce delta (alternatively square, cube approach in above paper can be used with additional cycles)
  private double deltaError = 0.010;

  @Test(expected = AssertionError.class)
  public void testBloomIllegalArg1() {
    BloomFilter bf = new BloomFilter(0, 0);
  }

  @Test(expected = AssertionError.class)
  public void testBloomIllegalArg2() {
    BloomFilter bf = new BloomFilter(0, 0.1);
  }

  @Test(expected = AssertionError.class)
  public void testBloomIllegalArg3() {
    BloomFilter bf = new BloomFilter(1, 0.0);
  }

  @Test(expected = AssertionError.class)
  public void testBloomIllegalArg4() {
    BloomFilter bf = new BloomFilter(1, 1.0);
  }

  @Test(expected = AssertionError.class)
  public void testBloomIllegalArg5() {
    BloomFilter bf = new BloomFilter(-1, -1);
  }


  @Test
  public void testBloomNumBits() {
    assertEquals(0, BloomFilter.optimalNumOfBits(0, 0));
    assertEquals(1549, BloomFilter.optimalNumOfBits(1, 0));
    assertEquals(0, BloomFilter.optimalNumOfBits(0, 1));
    assertEquals(0, BloomFilter.optimalNumOfBits(1, 1));
    assertEquals(7, BloomFilter.optimalNumOfBits(1, 0.03));
    assertEquals(72, BloomFilter.optimalNumOfBits(10, 0.03));
    assertEquals(729, BloomFilter.optimalNumOfBits(100, 0.03));
    assertEquals(7298, BloomFilter.optimalNumOfBits(1000, 0.03));
    assertEquals(72984, BloomFilter.optimalNumOfBits(10000, 0.03));
    assertEquals(729844, BloomFilter.optimalNumOfBits(100000, 0.03));
    assertEquals(7298440, BloomFilter.optimalNumOfBits(1000000, 0.03));
    assertEquals(6235224, BloomFilter.optimalNumOfBits(1000000, 0.05));
  }

  @Test
  public void testBloomNumHashFunctions() {
    assertEquals(1, BloomFilter.optimalNumOfHashFunctions(-1, -1));
    assertEquals(1, BloomFilter.optimalNumOfHashFunctions(0, 0));
    assertEquals(1, BloomFilter.optimalNumOfHashFunctions(10, 0));
    assertEquals(1, BloomFilter.optimalNumOfHashFunctions(10, 10));
    assertEquals(7, BloomFilter.optimalNumOfHashFunctions(10, 100));
    assertEquals(1, BloomFilter.optimalNumOfHashFunctions(100, 100));
    assertEquals(1, BloomFilter.optimalNumOfHashFunctions(1000, 100));
    assertEquals(1, BloomFilter.optimalNumOfHashFunctions(10000, 100));
    assertEquals(1, BloomFilter.optimalNumOfHashFunctions(100000, 100));
    assertEquals(1, BloomFilter.optimalNumOfHashFunctions(1000000, 100));
  }

  @Test
  public void testBloomFilterBytes() {
    BloomFilter bf = new BloomFilter(10000);
    byte[] val = new byte[]{1, 2, 3};
    byte[] val1 = new byte[]{1, 2, 3, 4};
    byte[] val2 = new byte[]{1, 2, 3, 4, 5};
    byte[] val3 = new byte[]{1, 2, 3, 4, 5, 6};

    assertEquals(false, bf.test(val));
    assertEquals(false, bf.test(val1));
    assertEquals(false, bf.test(val2));
    assertEquals(false, bf.test(val3));
    bf.add(val);
    assertEquals(true, bf.test(val));
    assertEquals(false, bf.test(val1));
    assertEquals(false, bf.test(val2));
    assertEquals(false, bf.test(val3));
    bf.add(val1);
    assertEquals(true, bf.test(val));
    assertEquals(true, bf.test(val1));
    assertEquals(false, bf.test(val2));
    assertEquals(false, bf.test(val3));
    bf.add(val2);
    assertEquals(true, bf.test(val));
    assertEquals(true, bf.test(val1));
    assertEquals(true, bf.test(val2));
    assertEquals(false, bf.test(val3));
    bf.add(val3);
    assertEquals(true, bf.test(val));
    assertEquals(true, bf.test(val1));
    assertEquals(true, bf.test(val2));
    assertEquals(true, bf.test(val3));

    byte[] randVal = new byte[COUNT];
    for (int i = 0; i < COUNT; i++) {
      rand.nextBytes(randVal);
      bf.addBytes(randVal);
    }
    // last value should be present
    assertEquals(true, bf.testBytes(randVal));
    // most likely this value should not exist
    randVal[0] = 0;
    randVal[1] = 0;
    randVal[2] = 0;
    randVal[3] = 0;
    randVal[4] = 0;
    assertEquals(false, bf.testBytes(randVal));

    assertEquals(7800, bf.sizeInBytes());
  }

  @Test
  public void testBloomFilterByte() {
    BloomFilter bf = new BloomFilter(10000);
    byte val = Byte.MIN_VALUE;
    byte val1 = 1;
    byte val2 = 2;
    byte val3 = Byte.MAX_VALUE;

    assertEquals(false, bf.testByte(val));
    assertEquals(false, bf.testByte(val1));
    assertEquals(false, bf.testByte(val2));
    assertEquals(false, bf.testByte(val3));
    bf.addByte(val);
    assertEquals(true, bf.testByte(val));
    assertEquals(false, bf.testByte(val1));
    assertEquals(false, bf.testByte(val2));
    assertEquals(false, bf.testByte(val3));
    bf.addByte(val1);
    assertEquals(true, bf.testByte(val));
    assertEquals(true, bf.testByte(val1));
    assertEquals(false, bf.testByte(val2));
    assertEquals(false, bf.testByte(val3));
    bf.addByte(val2);
    assertEquals(true, bf.testByte(val));
    assertEquals(true, bf.testByte(val1));
    assertEquals(true, bf.testByte(val2));
    assertEquals(false, bf.testByte(val3));
    bf.addByte(val3);
    assertEquals(true, bf.testByte(val));
    assertEquals(true, bf.testByte(val1));
    assertEquals(true, bf.testByte(val2));
    assertEquals(true, bf.testByte(val3));

    byte randVal = 0;
    for (int i = 0; i < COUNT; i++) {
      randVal = (byte) rand.nextInt(Byte.MAX_VALUE);
      bf.addByte(randVal);
    }
    // last value should be present
    assertEquals(true, bf.testByte(randVal));
    // most likely this value should not exist
    assertEquals(false, bf.testByte((byte) -120));

    assertEquals(7800, bf.sizeInBytes());
  }

  @Test
  public void testBloomFilterInt() {
    BloomFilter bf = new BloomFilter(10000);
    int val = Integer.MIN_VALUE;
    int val1 = 1;
    int val2 = 2;
    int val3 = Integer.MAX_VALUE;

    assertEquals(false, bf.testInt(val));
    assertEquals(false, bf.testInt(val1));
    assertEquals(false, bf.testInt(val2));
    assertEquals(false, bf.testInt(val3));
    bf.addInt(val);
    assertEquals(true, bf.testInt(val));
    assertEquals(false, bf.testInt(val1));
    assertEquals(false, bf.testInt(val2));
    assertEquals(false, bf.testInt(val3));
    bf.addInt(val1);
    assertEquals(true, bf.testInt(val));
    assertEquals(true, bf.testInt(val1));
    assertEquals(false, bf.testInt(val2));
    assertEquals(false, bf.testInt(val3));
    bf.addInt(val2);
    assertEquals(true, bf.testInt(val));
    assertEquals(true, bf.testInt(val1));
    assertEquals(true, bf.testInt(val2));
    assertEquals(false, bf.testInt(val3));
    bf.addInt(val3);
    assertEquals(true, bf.testInt(val));
    assertEquals(true, bf.testInt(val1));
    assertEquals(true, bf.testInt(val2));
    assertEquals(true, bf.testInt(val3));

    int randVal = 0;
    for (int i = 0; i < COUNT; i++) {
      randVal = rand.nextInt();
      bf.addInt(randVal);
    }
    // last value should be present
    assertEquals(true, bf.testInt(randVal));
    // most likely this value should not exist
    assertEquals(false, bf.testInt(-120));

    assertEquals(7800, bf.sizeInBytes());
  }

  @Test
  public void testBloomFilterLong() {
    BloomFilter bf = new BloomFilter(10000);
    long val = Long.MIN_VALUE;
    long val1 = 1;
    long val2 = 2;
    long val3 = Long.MAX_VALUE;

    assertEquals(false, bf.testLong(val));
    assertEquals(false, bf.testLong(val1));
    assertEquals(false, bf.testLong(val2));
    assertEquals(false, bf.testLong(val3));
    bf.addLong(val);
    assertEquals(true, bf.testLong(val));
    assertEquals(false, bf.testLong(val1));
    assertEquals(false, bf.testLong(val2));
    assertEquals(false, bf.testLong(val3));
    bf.addLong(val1);
    assertEquals(true, bf.testLong(val));
    assertEquals(true, bf.testLong(val1));
    assertEquals(false, bf.testLong(val2));
    assertEquals(false, bf.testLong(val3));
    bf.addLong(val2);
    assertEquals(true, bf.testLong(val));
    assertEquals(true, bf.testLong(val1));
    assertEquals(true, bf.testLong(val2));
    assertEquals(false, bf.testLong(val3));
    bf.addLong(val3);
    assertEquals(true, bf.testLong(val));
    assertEquals(true, bf.testLong(val1));
    assertEquals(true, bf.testLong(val2));
    assertEquals(true, bf.testLong(val3));

    long randVal = 0;
    for (int i = 0; i < COUNT; i++) {
      randVal = rand.nextLong();
      bf.addLong(randVal);
    }
    // last value should be present
    assertEquals(true, bf.testLong(randVal));
    // most likely this value should not exist
    assertEquals(false, bf.testLong(-120));

    assertEquals(7800, bf.sizeInBytes());
  }

  @Test
  public void testBloomFilterFloat() {
    BloomFilter bf = new BloomFilter(10000);
    float val = Float.MIN_VALUE;
    float val1 = 1.1f;
    float val2 = 2.2f;
    float val3 = Float.MAX_VALUE;

    assertEquals(false, bf.testFloat(val));
    assertEquals(false, bf.testFloat(val1));
    assertEquals(false, bf.testFloat(val2));
    assertEquals(false, bf.testFloat(val3));
    bf.addFloat(val);
    assertEquals(true, bf.testFloat(val));
    assertEquals(false, bf.testFloat(val1));
    assertEquals(false, bf.testFloat(val2));
    assertEquals(false, bf.testFloat(val3));
    bf.addFloat(val1);
    assertEquals(true, bf.testFloat(val));
    assertEquals(true, bf.testFloat(val1));
    assertEquals(false, bf.testFloat(val2));
    assertEquals(false, bf.testFloat(val3));
    bf.addFloat(val2);
    assertEquals(true, bf.testFloat(val));
    assertEquals(true, bf.testFloat(val1));
    assertEquals(true, bf.testFloat(val2));
    assertEquals(false, bf.testFloat(val3));
    bf.addFloat(val3);
    assertEquals(true, bf.testFloat(val));
    assertEquals(true, bf.testFloat(val1));
    assertEquals(true, bf.testFloat(val2));
    assertEquals(true, bf.testFloat(val3));

    float randVal = 0;
    for (int i = 0; i < COUNT; i++) {
      randVal = rand.nextFloat();
      bf.addFloat(randVal);
    }
    // last value should be present
    assertEquals(true, bf.testFloat(randVal));
    // most likely this value should not exist
    assertEquals(false, bf.testFloat(-120.2f));

    assertEquals(7800, bf.sizeInBytes());
  }

  @Test
  public void testBloomFilterDouble() {
    BloomFilter bf = new BloomFilter(10000);
    double val = Double.MIN_VALUE;
    double val1 = 1.1d;
    double val2 = 2.2d;
    double val3 = Double.MAX_VALUE;

    assertEquals(false, bf.testDouble(val));
    assertEquals(false, bf.testDouble(val1));
    assertEquals(false, bf.testDouble(val2));
    assertEquals(false, bf.testDouble(val3));
    bf.addDouble(val);
    assertEquals(true, bf.testDouble(val));
    assertEquals(false, bf.testDouble(val1));
    assertEquals(false, bf.testDouble(val2));
    assertEquals(false, bf.testDouble(val3));
    bf.addDouble(val1);
    assertEquals(true, bf.testDouble(val));
    assertEquals(true, bf.testDouble(val1));
    assertEquals(false, bf.testDouble(val2));
    assertEquals(false, bf.testDouble(val3));
    bf.addDouble(val2);
    assertEquals(true, bf.testDouble(val));
    assertEquals(true, bf.testDouble(val1));
    assertEquals(true, bf.testDouble(val2));
    assertEquals(false, bf.testDouble(val3));
    bf.addDouble(val3);
    assertEquals(true, bf.testDouble(val));
    assertEquals(true, bf.testDouble(val1));
    assertEquals(true, bf.testDouble(val2));
    assertEquals(true, bf.testDouble(val3));

    double randVal = 0;
    for (int i = 0; i < COUNT; i++) {
      randVal = rand.nextDouble();
      bf.addDouble(randVal);
    }
    // last value should be present
    assertEquals(true, bf.testDouble(randVal));
    // most likely this value should not exist
    assertEquals(false, bf.testDouble(-120.2d));

    assertEquals(7800, bf.sizeInBytes());
  }

  @Test
  public void testBloomFilterString() {
    BloomFilter bf = new BloomFilter(10000);
    String val = "bloo";
    String val1 = "bloom fil";
    String val2 = "bloom filter";
    String val3 = "cuckoo filter";

    assertEquals(false, bf.testString(val));
    assertEquals(false, bf.testString(val1));
    assertEquals(false, bf.testString(val2));
    assertEquals(false, bf.testString(val3));
    bf.addString(val);
    assertEquals(true, bf.testString(val));
    assertEquals(false, bf.testString(val1));
    assertEquals(false, bf.testString(val2));
    assertEquals(false, bf.testString(val3));
    bf.addString(val1);
    assertEquals(true, bf.testString(val));
    assertEquals(true, bf.testString(val1));
    assertEquals(false, bf.testString(val2));
    assertEquals(false, bf.testString(val3));
    bf.addString(val2);
    assertEquals(true, bf.testString(val));
    assertEquals(true, bf.testString(val1));
    assertEquals(true, bf.testString(val2));
    assertEquals(false, bf.testString(val3));
    bf.addString(val3);
    assertEquals(true, bf.testString(val));
    assertEquals(true, bf.testString(val1));
    assertEquals(true, bf.testString(val2));
    assertEquals(true, bf.testString(val3));

    long randVal = 0;
    for (int i = 0; i < COUNT; i++) {
      randVal = rand.nextLong();
      bf.addString(Long.toString(randVal));
    }
    // last value should be present
    assertEquals(true, bf.testString(Long.toString(randVal)));
    // most likely this value should not exist
    assertEquals(false, bf.testString(Long.toString(-120)));

    assertEquals(7800, bf.sizeInBytes());
  }

  @Test
  public void testMerge() {
    BloomFilter bf = new BloomFilter(10000);
    String val = "bloo";
    String val1 = "bloom fil";
    String val2 = "bloom filter";
    String val3 = "cuckoo filter";
    bf.addString(val);
    bf.addString(val1);
    bf.addString(val2);
    bf.addString(val3);

    BloomFilter bf2 = new BloomFilter(10000);
    String v = "2_bloo";
    String v1 = "2_bloom fil";
    String v2 = "2_bloom filter";
    String v3 = "2_cuckoo filter";
    bf2.addString(v);
    bf2.addString(v1);
    bf2.addString(v2);
    bf2.addString(v3);

    assertEquals(true, bf.testString(val));
    assertEquals(true, bf.testString(val1));
    assertEquals(true, bf.testString(val2));
    assertEquals(true, bf.testString(val3));
    assertEquals(false, bf.testString(v));
    assertEquals(false, bf.testString(v1));
    assertEquals(false, bf.testString(v2));
    assertEquals(false, bf.testString(v3));

    bf.merge(bf2);

    assertEquals(true, bf.testString(val));
    assertEquals(true, bf.testString(val1));
    assertEquals(true, bf.testString(val2));
    assertEquals(true, bf.testString(val3));
    assertEquals(true, bf.testString(v));
    assertEquals(true, bf.testString(v1));
    assertEquals(true, bf.testString(v2));
    assertEquals(true, bf.testString(v3));
  }

  @Test
  public void testFpp1K() {
    int size = 1000;
    BloomFilter bf = new BloomFilter(size);
    int fp = 0;
    for (int i = 0; i < size; i++) {
      bf.addLong(i);
    }

    Random random = new Random();
    for (int i = 0; i < size; i++) {
      int probe = random.nextInt();
      // out of range probes
      if ((probe > size) || (probe < 0)) {
        if (bf.testLong(probe)) {
          fp++;
        }
      }
    }

    for (int i = 0; i < size; i++) {
      assertTrue(bf.testLong(i));
    }

    double actualFpp = (double) fp / (double) size;
    double expectedFpp = bf.getFalsePositivePercent();
    if (actualFpp < expectedFpp) {
      assertTrue(actualFpp != 0.0);
    } else {
      assertEquals(expectedFpp, actualFpp, deltaError);
    }
  }

  @Test
  public void testFpp10K() {
    int size = 10_000;
    BloomFilter bf = new BloomFilter(size);
    int fp = 0;
    for (int i = 0; i < size; i++) {
      bf.addLong(i);
    }

    Random random = new Random();
    for (int i = 0; i < size; i++) {
      int probe = random.nextInt();
      // out of range probes
      if ((probe > size) || (probe < 0)) {
        if (bf.testLong(probe)) {
          fp++;
        }
      }
    }

    for (int i = 0; i < size; i++) {
      assertTrue(bf.testLong(i));
    }

    double actualFpp = (double) fp / (double) size;
    double expectedFpp = bf.getFalsePositivePercent();
    if (actualFpp < expectedFpp) {
      assertTrue(actualFpp != 0.0);
    } else {
      assertEquals(expectedFpp, actualFpp, deltaError);
    }
  }

  @Test
  public void testFpp1M() {
    int size = 1_000_000;
    BloomFilter bf = new BloomFilter(size);
    int fp = 0;
    for (int i = 0; i < size; i++) {
      bf.addLong(i);
    }

    for (int i = 0; i < size; i++) {
      assertTrue(bf.testLong(i));
    }

    Random random = new Random();
    for (int i = 0; i < size; i++) {
      int probe = random.nextInt();
      // out of range probes
      if ((probe > size) || (probe < 0)) {
        if (bf.testLong(probe)) {
          fp++;
        }
      }
    }

    double actualFpp = (double) fp / (double) size;
    double expectedFpp = bf.getFalsePositivePercent();
    if (actualFpp < expectedFpp) {
      assertTrue(actualFpp != 0.0);
    } else {
      assertEquals(expectedFpp, actualFpp, deltaError);
    }
  }

  @Test
  public void testFpp10M() {
    int size = 10_000_000;
    BloomFilter bf = new BloomFilter(size);
    int fp = 0;
    for (int i = 0; i < size; i++) {
      bf.addLong(i);
    }

    for (int i = 0; i < size; i++) {
      assertTrue(bf.testLong(i));
    }

    Random random = new Random();
    for (int i = 0; i < size; i++) {
      int probe = random.nextInt();
      // out of range probes
      if ((probe > size) || (probe < 0)) {
        if (bf.testLong(probe)) {
          fp++;
        }
      }
    }

    double actualFpp = (double) fp / (double) size;
    double expectedFpp = bf.getFalsePositivePercent();
    if (actualFpp < expectedFpp) {
      assertTrue(actualFpp != 0.0);
    } else {
      assertEquals(expectedFpp, actualFpp, deltaError);
    }
  }
}
