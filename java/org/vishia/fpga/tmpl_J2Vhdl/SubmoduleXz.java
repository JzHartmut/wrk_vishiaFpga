package org.vishia.fpga.tmpl_J2Vhdl;

import org.vishia.fpga.Fpga;
import org.vishia.fpga.stdmodules.Reset_ifc;

public class SubmoduleXz implements org.vishia.fpga.FpgaModule_ifc {

  public static class In {                                                      // (2)
    boolean var;
    ;
  }
  public final In in = new In();
  
  public static class Out {                                                     // (3)
    boolean outX1;
    @Fpga.STDVECTOR(8) int outVal;
  }
  public final Out out = new Out();

  
  public void init ( Reset_ifc reset) {
    
  }
  
  
  @Override public void reset () {
    // TODO Auto-generated method stub
    
  }

  @Override public void step ( int time ) {
    // TODO Auto-generated method stub
    
  }

  @Override public void update () {
    // TODO Auto-generated method stub
    
  }

  public void output() {
    this.out.outX1 = false;
    this.out.outVal = 0xc3;
  }
  
}
