package org.vishia.fpga.stdmodules;

public interface CrcGenSerial_Inpifc {

  
  boolean getBit ( int time, int min);
  
  
  boolean stateBeforeCrc ( int time, int min);
  
  boolean stateCrc ( int time, int min);
}
