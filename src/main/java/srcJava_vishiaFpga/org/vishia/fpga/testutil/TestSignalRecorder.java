package org.vishia.fpga.testutil;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.vishia.util.StringFunctions;


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
  protected void registerLine(StringBuilder ob, String signaleName) {
    ob.setLength(0);
    this.sbs.add(ob);
    ob.append(this.moduleName).append('.').append(signaleName);
    while(ob.length() <18) { ob.append('_'); }
    ob.append(':');
    this.pos = ob.length();
    
  }
  
  /**This operation should be overridden in a kind that all buffers should be created and reseted
   * using the {@link #registerLine(StringBuilder, String)} operation. 
   * Example/Template: <pre>
   *  @Override public void clean() {
      super.clean();
      super.startLine(this.sbCtLow, "ctLow");
      super.startLine(this.sbCt, "ct");
      super.startLine(this.sbtime, "time");
    }
   * </pre>
   * Note: This operation <code>super.clean();</code> should be called at first, it creates newly the container 
   * used in {@link #registerLine(StringBuilder, String)}.
   */
  public void clean() {
    this.sbs = new LinkedList<StringBuilder>();
  }
  
  
  
  
  /**This operation should be implemented to add the necessary signals to all existing lines
   * which are created or cleaned in the overridden {@link #clean()} and registered with {@link #registerLine(StringBuilder, String)}.
   *   Usage example/pattern:<pre>
    (at)Override public int addSignals ( int time, boolean bAdd ) throws IOException {
      BlinkingLedCt mdl = BlinkingLedCt.this;
      int zCurr = this.sbCt.length(); // current length for this time
      int zAdd = 0;                   // >0 then position of new length for this time
      if(mdl.ref.clkDiv.q.ce) {       // because the own states switches only with this ce, the signals should also recorded only then.
        if(mdl.q.ctLow == 1) {        // on this condition
          this.wrCt = 5;              // switch on, write 5 steps info
        }
        if(--this.wrCt >0) {          // if one of the 5 infos shouls be written:
          StringFunctions_C.appendHex(this.sbCtLow, mdl.q.ctLow,4).append(' ');    //append info
          StringFunctions_C.appendHex(this.sbCt, mdl.q.ct,2);                      //append info
          if(checkLen(this.sbtime, zCurr)) {      // add the time information if here is space.
            StringFunctions_C.appendIntPict(this.sbtime, time, "33'331.111.11");   // append time info
          }
          zAdd = this.sbCtLow.length();  //length of buffers for new time determined by the sbCtLow, the longest entry.
        } 
        else if(this.wrCt ==0) {      // end of the 5 steps, append .... as separation
          this.sbCtLow.append("..... ");
          zAdd = this.sbCtLow.length();  //length of buffers for new time determined by the sbCtLow, the longest entry.
        }
      }// if ce
      return zAdd;       // will be used in TestSignalRecorderSet.addSignals(zAdd) to set all lines to this length
    }//addSignals
   * </pre>
   * Here signals are only added if the ce of the other module is set. Elsewhere the operation returns 0.
   * This is sensible because signals either changes only with this ce, or they are interesting only in ce steps.
   * <br>
   * Furthermore not in any step signals are added, only after a trigger value. And then 5 times.
   * <br>
   * Last not least a time information is added as second or millisecond presentation value.
   *    
   * @param time the system time may be used for output
   * @param bAdd true then other TestSignalsRecorders have added an information in this step time before.
   *   This information can be used to decide whether to add.
   *   But it depends on the order of registering in {@link TestSignalRecorderSet#registerRecorder(TestSignalRecorder)}.
   *   If you want to use this function, this test generator is subordinate, it accepts the behavior of the recorders before
   *   delivered in the return value of this operation. 
   *   If the return value of all called before addSignals(...) operation is 0, then this value is false
   *   on usage of {@link TestSignalRecorderSet#addSignals(int)}.
   * @return 0 if no signal is added. Elsewhere the length of the longest yet written internal buffer.  
   * @throws IOException 
   */
  public abstract int addSignals ( int time, boolean bAdd) throws IOException;
  
  /**Simple for with bAdd = true, see {@link #addSignals(int, boolean)}
   */
  public final int addSignals ( int time) throws IOException { return addSignals(time, true);}
  
  
  
  
  /**This operation fills all registered StringBuilder to the same length. 
   * It can be called manually, but it is automatically called for all TestSignalRecorder
   * using the {@link TestSignalRecorderSet#addSignals(int)}.
   * @throws IOException */
  protected void endSignals ( int pos) throws IOException {
    this.pos = pos;
    for(StringBuilder sb : this.sbs) {
      while(sb.length() < pos) { sb.append(' '); }
    }
  }
  
  
  
  
  /**This operation can be used to decide whether the StringBuilder has place for a new longer information,
   * for example a bus value (hexa). See Example in {@link #addSignals(int, boolean)}  
   * @param sb the StringBuilder should be used for append
   * @param zTime The length of relevant other StringBuilder lines for this time step.
   *   It is the length before writing an information to the other StringBuilder in this step time, 
   *   not the position after writing as used for {@link #endSignals(int)}.
   *   It means it should be gathered first before fill any other buffer, see example on {@link #addSignals(int, boolean)}.
   *   If the <code>sb.length()</code> of this StringBuilder is lesser, 
   *   then firstly the content is padded with spaces to ensure the same position as given for zTime. 
   *   But this is also done if {@link #endSignals(int)} was called also for this buffer.
   * @return true if there is space to add an information. The sb is not longer as zTime, and the last character is a space. 
   *   false if sb is longer as the zTime position, or it does not contain at least one space for separation on the last position. 
   *   It means do not write into.
   */
  protected boolean checkLen ( StringBuilder sb, int zTime) {
    int zsb = sb.length();                   //length of sb
    if(zsb >= zTime && sb.charAt(zsb-1) !=' ') {    // >= because should at least one space
      return false;             // do not write to this buffer, no space for the zTime position.
    }
    else {
      while(++zsb <= zTime) {   // padding with spaces till zTime
        sb.append(' ');         // append spaces till the current position of the time
      }
      return true;              // can append
    }
  }
  
  
  
  /**This operation outputs all lines.
   * Special function: If no lines are registered, an empty line is outputted.
   * This can be used to structure the output. 
   * @param out
   * @throws IOException
   */
  public void output ( Appendable out) throws IOException {
    if(this.sbs.size()==0) {
      out.append('\n');
    } else {
      for(StringBuilder sb: this.sbs) {
        out.append(sb).append('\n');
      }
    }
  }
  
  
  
  public StringBuilder getLine(String id) {
    if(!id.startsWith(this.moduleName)) return null;
    int zid = id.length();
    for(StringBuilder sb: this.sbs) {
      if(   StringFunctions.startsWith(sb, id)
         && (sb.charAt(zid) == '_' || sb.charAt(zid) == ':') 
        )
        return sb;
    }
    return null;   //not found
  }

  
  
  /**With this class a member of {@link TestSignalRecorderSet} can be built
   * which produces a separation line. 
   *
   */
  public static class Empty extends TestSignalRecorder {

    private final String lineSep;
    
    /**ctor
     * @param lineSep null or "", then an empty line will be output.
     *   contains one character, then this character is repeated till the current length of all other lines.
     *   contains a longer String: Writes this string as separation line.
     */
    public Empty(String lineSep) {
      super(null);
      this.lineSep = lineSep;
    }

    /**This operation is empty, does nothing, returns 0, no contribution for presentation.
     *
     */
    @Override public int addSignals(int time, boolean bAdd) throws IOException {
      return 0;
    }
    
    
    
    @Override public void output ( Appendable out) throws IOException {
      if(this.lineSep==null || this.lineSep.length()==0) {
      } else if(this.lineSep.length()==1) {
        super.endSignals(super.pos);
      } else {
        out.append(this.lineSep);
      }
      out.append("\n");
      
    }
   }
  
}
