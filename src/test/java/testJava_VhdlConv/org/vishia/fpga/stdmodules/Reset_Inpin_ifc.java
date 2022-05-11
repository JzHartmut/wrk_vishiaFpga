package org.vishia.fpga.stdmodules;

public interface Reset_Inpin_ifc {

  
  
  /**Access to an FPGA input pin for reset.
   * It is usual low active clarified in the using Reset module. 
   * @return state of the reset pin.
   */
  public boolean reset_Pin();

}
