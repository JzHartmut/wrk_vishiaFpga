package org.vishia.fpga.exmpl.fpgatop;

import org.vishia.fpga.exmpl.modules.BlinkingLedCfg_ifc;
import org.vishia.fpga.exmpl.modules.BlinkingLedCt;
import org.vishia.fpga.exmpl.modules.ClockDivider;
import org.vishia.fpga.stdmodules.Reset;
import org.vishia.fpga.stdmodules.Reset_Inpin_ifc;
import org.vishia.fpga.Fpga;
import org.vishia.fpga.FpgaModule_ifc;


/**Extra configuration class associated to the top level FPGA definition, 
 * hence in the same file
 * Other possibility: interface is implemented in the public main class of the top level FPGA.
 * See usage, comment one of the lines to evaluate it.
 */
class BlinkingLedCfg implements BlinkingLedCfg_ifc {
  @Override 
  @Fpga.BITVECTOR(8) public int time_BlinkingLed() {
    return 0x64;
  }

  @Override
  public int onDuration_BlinkingLed() {
    return 10;
  }

}




//org.vishia.fpga.exmpl.fpgatop.BlinkingLed_Fpga

public class BlinkingLed_Fpga implements FpgaModule_ifc, BlinkingLedCfg_ifc {
  
  
  
  public class Input implements Reset_Inpin_ifc {

    public boolean reset_Pin;
    
    @Override
    public boolean reset_Pin() { return this.reset_Pin; }

  }

  
  
  public class Output {
    
    public boolean led1, led2;
    
    
  }
  
  
  
  public final Input input = new Input(); 
  
  public final Output output = new Output(); 
  
//tag::Modules[]
  /**The modules which are part of this Fpga for test. */
  public class Modules {
    
    public BlinkingLedCfg blinkingLedCfg = new BlinkingLedCfg();
    
    /**Build a reset signal high active for reset. Initial or also with the reset_Pin. 
     * This module is immediately connected to one of the inputFpga pins
     * via specific interface, see constructor argument type.
     */
    public final Reset res = new Reset(BlinkingLed_Fpga.this.input);
    
    public final ClockDivider ce = new ClockDivider();
    
    //Note: both lines are possible, comment only one: Using different implementations. 
    //public final BlinkingLedCt ct = new BlinkingLedCt(this.res, this.blinkingLedCfg, this.ce);    //cfg implemented in extra class in this file.
    public final BlinkingLedCt ct = new BlinkingLedCt(this.res, BlinkingLed_Fpga.this, this.ce);  //cfg implemented in main class
    
    
    Modules ( ) {
    }
  }

  public final Modules ref = new Modules();
  //end::Modules[]

  //public final Modules ref = this.m;
  
  @Override
  public void step(int time) {
    this.ref.res.step(time);
    this.ref.ce.step(time);
    this.ref.ct.step(time);
    
  }


  @Override
  public void update() {
    this.ref.res.update();
    this.ref.ce.update();
    this.ref.ct.update();
    this.output.led1 = Fpga.getBits(this.ref.ct.ct(), 2,0) == 0b000;
    this.output.led2 = this.ref.ct.ledBlinking();
  }
  
  //tag::time_BlinkingLed_topImpl[]
  @Override 
  public int time_BlinkingLed() {
    return 0xc8;  //200 TODO should be use the type designation of the interface operation definition. 
  }
  //end::time_BlinkingLed_topImpl[]
  

  @Override
  public int onDuration_BlinkingLed() {
    return 100;
  }

  
}


