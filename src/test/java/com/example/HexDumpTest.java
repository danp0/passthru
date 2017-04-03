package com.example;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for HexDump
 */
public class HexDumpTest extends TestCase {
  public HexDumpTest(String testName) {
    super(testName);
  }

  public static Test suite() {
    return new TestSuite(HexDumpTest.class);
  }

  public void testHexDump() {
    HexDump hd = new HexDump();
    hd.dump(new byte[] {0, 0, 0, 0, 0, 0, 0, 0});
    assertEquals(1, hd.getHex().size());
    assertEquals(1, hd.getChr().size());
    assertEquals("00 00 00 00 00 00 00 00", hd.getHex().get(0));
    assertEquals("........", hd.getChr().get(0));

    hd.dump(new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
    assertEquals(2, hd.getHex().size());
    assertEquals(2, hd.getChr().size());
    assertEquals("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00", hd.getHex().get(0));
    assertEquals("00", hd.getHex().get(1));
    assertEquals("................", hd.getChr().get(0));
    assertEquals(".", hd.getChr().get(1));

    hd.dump(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'});
    assertEquals(2, hd.getHex().size());
    assertEquals(2, hd.getChr().size());
    assertEquals("00 01 02 03 04 05 06 07 08 39 61 62 63 64 65 66", hd.getHex().get(0));
    assertEquals("67 68", hd.getHex().get(1));
    assertEquals(".........9abcdef", hd.getChr().get(0));
    assertEquals("gh", hd.getChr().get(1));
  }
}
