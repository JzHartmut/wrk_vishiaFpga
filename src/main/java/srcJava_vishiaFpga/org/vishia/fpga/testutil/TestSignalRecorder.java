package org.vishia.fpga.testutil;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


/**A base for a test signal recorder for horizontal time deterministic presentation of states. 
 * A proper derivation of this class should be implement in any module. 
 * The derivation should define some StringBuilder for output lines.
 * The instantiation of this class should be done in the Test environment, not in the module itself (!).
 * 
 * */
public abstract class TestSignalRecorder {

  /**Registered module name to build the line title. */
  protected final String moduleName;
  
  protected int pos;
  
  private List<StringBuilder> sbs;
  
  /**Each module with several output lines need an instance of this class.
   * @param moduleName as start for the line.
   */
  public TestSignalRecorder(String moduleName) {
    this.moduleName = moduleName;
  }

  /**Helper operation to resets one line.
   * @param ob any of the output buffer
   * @param signaleName the signal name on start of line will be combinded with the module name of the constructor. 
   */
  protected void startLine(StringBuilder ob, String signaleName) {
    ob.setLength(0);
    this.sbs.add(ob);
    ob.append(this.moduleName).append('.').append(signaleName);
    while(ob.length() <18) { ob.append('_'); }
    ob.append(':');
    this.pos = ob.length();
    
  }
  
  /**This operation should be implemented in a kind that all buffers should be reseted
   * using the {@link #startLine(StringBuilder, String)} operation. 
   */
  public void clean() {
    this.sbs = new LinkedList<StringBuilder>();
  }
  
  /**This operation should be implemented to add all signals to all existing lines. 
   * Each line should have its own StringBuilder buffer.
   * @throws IOException */
  public abstract void addSignals() throws IOException;
  
  /**This operation should be implemented to add all signals to all existing lines. 
   * Each line should have its own StringBuilder buffer.
   * @throws IOException */
  public void addSignals(int pos) throws IOException {
    this.pos = pos;
    for(StringBuilder sb : this.sbs) {
      while(sb.length() < pos) { sb.append(' '); }
    }
  }
  
  protected boolean checkLen(StringBuilder sb, int currentLength) {
    int zsb = sb.length();
    if(zsb == currentLength && sb.charAt(zsb-1) !='X') {
      sb.append('X');
    }
    return zsb < currentLength;
  }
  
  
  
  /**This operation should implement all StringBuilder buffer to output all lines.
   * @param out
   * @throws IOException
   */
  public final void output(Appendable out) throws IOException {
    for(StringBuilder sb: this.sbs) {
      out.append(sb).append('\n');
    }
  }
}
