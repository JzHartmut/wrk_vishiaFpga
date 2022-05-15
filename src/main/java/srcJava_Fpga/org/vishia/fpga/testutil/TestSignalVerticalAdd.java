package org.vishia.fpga.testutil;


public abstract class TestSignalVerticalAdd {

  
  /**Contribution to output
   * @param dst
   * @param ix
   * @return -1 if dst is set but no change, 0 if dst is set with changes, length if dst is not set because no change. 
   */
  public abstract int add(String[] dst, int ix);
}
