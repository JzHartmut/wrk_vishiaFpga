package org.vishia.fpga.stdmodules;

/**A plug for a word, maybe also BIT_VECTOR or STD_VECTOR
 * @author Hartmut Schorrig
 *
 */
public interface Word_ifc {

  
  int getWord(int time, int min);
  
}
