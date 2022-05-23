package org.vishia.fpga.exmplBlinkingLed.fpgatop;

import org.vishia.fpga.stdmodules.Reset;
import org.vishia.fpga.stdmodules.Reset_Inpin_ifc;
import org.vishia.fpga.Fpga;
import org.vishia.fpga.FpgaModule_ifc;
import org.vishia.fpga.exmplBlinkingLed.modules.BlinkingLedCfg_ifc;
import org.vishia.fpga.exmplBlinkingLed.modules.BlinkingLedCt;
import org.vishia.fpga.exmplBlinkingLed.modules.ClockDivider;




//tag::theClassDef[]
public class BlinkingLed_Fpga implements FpgaModule_ifc {
  
//end::theClassDef[]

//  private BlinkingLed_FpgaInOutput inoutput;
//  
//  
//  public final BlinkingLed_FpgaInOutput.Input input = this.inoutput.input; 
//  
//  public final BlinkingLed_FpgaInOutput.Output output = this.inoutput.output; 
  

//tag::Modules[]
  /**The modules which are part of this Fpga for test. */
  public class Modules {
    
    /**The i/o pins of the top level FPGA should have exact this name ioPins. 
     * They are instantiated in this input output class*/
    BlinkingLed_FpgaInOutput ioPins = new BlinkingLed_FpgaInOutput();
    
    /**Build a reset signal high active for reset. Initial or also with the reset_Pin. 
     * This module is immediately connected to one of the inputFpga pins
     * via specific interface, see constructor argument type.
     */
    public final Reset res = new Reset(this.ioPins.reset_Inpin);
    
    public final ClockDivider ce = new ClockDivider();
    
    //Note: both lines are possible, comment only one: Using different implementations. 
    public final BlinkingLedCt ct = new BlinkingLedCt(this.res, BlinkingLed_Fpga.this.blinkingLedCfg, this.ce);    //cfg implemented in extra class in this file.
    
    
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
    this.ref.ioPins.output.led1 = Fpga.getBits(this.ref.ct.ct(), 2,0) == 0b000;
    this.ref.ioPins.output.led2 = this.ref.ct.ledBlinking();
  }
  
  
  /**Extra configuration class associated to the top level FPGA definition, 
   * hence in the same file
   * Other possibility: interface is implemented in the public main class of the top level FPGA.
   * See usage, comment one of the lines to evaluate it.
   */
  @Fpga.IfcAccess BlinkingLedCfg_ifc blinkingLedCfg = new BlinkingLedCfg_ifc ( ) {
    @Override 
    @Fpga.BITVECTOR(8) public int time_BlinkingLed() {
      return 0x64;
    }

    @Override
    public int onDuration_BlinkingLed() {
      return 10;
    }

  };



  
}


