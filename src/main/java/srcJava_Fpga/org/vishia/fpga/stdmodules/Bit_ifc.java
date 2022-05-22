package org.vishia.fpga.stdmodules;

import org.vishia.fpga.Fpga;

/**This is a very common interface only for one bit for any usage.
 * For some reason an input is necessary and an output is offered,
 * but the connection from the input to the output is determined not by a module,
 * It is determined by the interconnection of modules.
 * In such cases a specialized interface should not be used.
 * The connection should be proper for any plug.
 * The responsibility of the correctness of the connection is associated to the interconnection design only.
 * <br>
 * The implementor should have the annotation <code>@Fpga.IfcAccess</code>, example:
 * <pre>
  public @Fpga.IfcAccess Bit_ifc txReqMaster = new Bit_ifc ( ) {
    @Override public boolean getBit() { 
      return SpiMaster.this.spiM.stateCmd;  
    }
  };
 * </pre>
 * @author Hartmut Schorrig
 *
 */
public interface Bit_ifc {

  boolean getBit ( );
}
