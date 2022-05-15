package org.vishia.fpga.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import org.vishia.mainCmd.MainCmdLoggingStream;
import org.vishia.mainCmd.MainCmdLogging_ifc;
import org.vishia.parseJava.JavaSrc;
import org.vishia.parseJava.JavaSrc_Zbnf;
import org.vishia.util.CalculatorExpr;
import org.vishia.util.DataAccess;
import org.vishia.util.Debugutil;
import org.vishia.util.StringCmp;
import org.vishia.util.TestOrg;
import org.vishia.zbnf.Zbnf2Xml;
import org.vishia.zbnf.ZbnfJavaOutput;
import org.vishia.zbnf.ZbnfParseResultItem;
import org.vishia.zbnf.ZbnfParser;


public class TestParseJava {

  
  private Appendable log = null;
  
  public static void main(String[] args) {
    TestParseJava thiz = new TestParseJava();
    thiz.log = System.out;                                 // set it for debug
    try {
      String[] testArgs = {"---TESTverbose:8"};
      TestOrg test = new TestOrg("Test JavaParser, RPN expressions", 8, testArgs);
      thiz.testExpression(test);
      test.finish();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
  
  private final ZbnfParser parser;
  
  private final MainCmdLogging_ifc console;
  

  private final CalculatorExpr calculator = new CalculatorExpr();
  
  private Map<String, DataAccess.IntegerIx> nameVariables = new TreeMap<String, DataAccess.IntegerIx>();
  
  /**For test, some variable values for a...z = 1.0 ... 26.0 */
  private final float[] variable = new float[26];
  
  /**For test, some variable values for q1...q5. even is true, odd is false */
  private final boolean[] variableBool = new boolean[10];
  
  private final CalculatorExpr.Value[] results = new CalculatorExpr.Value[10];
  
  String[] backExpr = new String[10];


  private final JavaSrc.Expression[] expr = new JavaSrc.Expression[10];
  
  /**Should calculate in the example to have compare values. */
  private final ParseExample parseExample = new ParseExample();
  
  
  public TestParseJava() {
    this.console = new MainCmdLoggingStream(System.out);
    this.parser = new ZbnfParser(this.console, 10);
    this.parseExample.prc.exmplOperation();
    int ixVar = 0;
    for(char cc = 'a'; cc < 'z'; cc++) {                   // define some example variables a..z for execution
      this.nameVariables.put(""+cc, new DataAccess.IntegerIx(ixVar));
      this.variable[ixVar++] = 1.0f * (cc - 'a' + 1.0f);
    }
    for(char cc = '1'; cc <= '9'; cc++) {                   // define some example variables a..z for execution
      this.nameVariables.put("q"+cc, new DataAccess.IntegerIx(ixVar++));
      this.variableBool[cc-'1'] = ((byte)(cc) & 1)==0 ? false: true;
    }
    this.calculator.setLog(log);
    try {
      this.parser.setSyntaxFromJar(JavaSrc.class, "JavaSyntax.zbnf");
    } catch (IllegalCharsetNameException | UnsupportedCharsetException | IOException | ParseException e) {
      System.err.println("Error initializing JavaParser for syntax: " + e.getMessage());
    }
    this.parser.setReportIdents(MainCmdLogging_ifc.error, MainCmdLogging_ifc.fineInfo, MainCmdLogging_ifc.debug, MainCmdLogging_ifc.fineDebug);
   }

  
  
  
  
  static String[] cmpExprRpn = 
    { "@ x; @ a; @ b; * c; + @; = @; "
    , "@ y; @ a; + b; * c; = @; "
    , "@ z; @ a; + b; @ c; + d; * @; = @; "
    , "@ m; @ a; @ b; + c; + @; = @; "
    , "@ q6; @ q1; @ q2; && q4; || @; = @; "
    , "@ q7; @ c; < d; @ b; > a; @ c; > b; && @; || @; = @; "
    , "@ u; @ a; + b; * c; - d; = @; "
    , "@ v; @ a; @ d; + g; * - @; / e; @ a; + b; + @; = @; "
    , "@ w; @ a; @ b; @ c; + f; @ a; + b; / @; * - @; + @; - e; @ d; * g; / e; + @; = @; "
    };
  static String[] cmpBackExpr = 
    { " x =  a +  b * c   "
    , " y =  ( a + b ) * c  "
    , " z =  ( a + b ) *  ( c + d )   "
    , " m =  a +  ( b + c )   "
    , " q6 =  q1 ||  q2 && q4   "
    , " q7 =  c < d ||  b > a &&  c > b    "
    , " u =  ( a + b ) * c - d  "
    , " v =  a * - ( d + g )  / e +  ( a + b )   "
    , " w =  a +  b * - ( ( c + f ) /  ( a + b )  )   - e +  d * g / e   "  
    };
  
  
  
  //tag::testExpression[]
  /**Tests some expressions which are given in the Java src file.
   * @param testParent
   * @throws IllegalCharsetNameException
   * @throws UnsupportedCharsetException
   * @throws FileNotFoundException
   * @throws IOException
   */
  void testExpression(TestOrg testParent) throws IllegalCharsetNameException, UnsupportedCharsetException, FileNotFoundException, IOException {
    TestOrg test = new TestOrg("test Expressions in ParseExample", 7, testParent);
    try {
      String pathJavasrc = "D:/vishia/Java/cmpnJava_vishiaBase/src/test/java/org/vishia/parseJava/test/ParseExample.java";
      File fileIn = new File(pathJavasrc);
      JavaSrc res = parseJava(fileIn);
      int ixTest = 0;
      for(JavaSrc.ClassDefinition theClass: res.get_classDefinition()) {
        JavaSrc.ClassContent theClassC = theClass.get_classContent();
        for(JavaSrc.ClassDefinition iClass: theClassC.get_classDefinition()) {
          JavaSrc.ClassContent iClassC = iClass.get_classContent();
          for(JavaSrc.MethodDefinition rOper: iClassC.get_methodDefinition()) {
            for(JavaSrc.Statement rStmnt: rOper.get_methodbody().get_statement()) {
              JavaSrc.Expression rExpr = rStmnt.get_Expression();      // statement is an expression
              if(rExpr !=null) {
                rExpr.prep(log);
                JavaSrc.Expression exprRpn = rExpr;
                if(exprRpn !=null) {
                  rStmnt.set_AssignExpression(exprRpn);
                  if(ixTest ==3)
                    Debugutil.stop();
                  this.expr[ixTest] = exprRpn;
                  this.results[ixTest] = executeRpnExpr(exprRpn);
                  this.backExpr[ixTest] = convertToInfixExpr(exprRpn);
                  if(++ixTest >= this.results.length) { ixTest = this.results.length-1; } //limit it.
                }  //end::testExpression[]
                Debugutil.stop();
              }
            }
          }
        }
      }
      //tag::testValues[]
      float[] resultJavaCalcF = { this.parseExample.prc.x, this.parseExample.prc.y, this.parseExample.prc.z
          , this.parseExample.prc.m
          , Float.NaN, Float.NaN 
          , this.parseExample.prc.u, this.parseExample.prc.v, this.parseExample.prc.w, this.parseExample.prc.p
          };
      boolean[] resultJavaCalcB = { false, false, false, false
          , this.parseExample.prc.q6, this.parseExample.prc.q7
          };
      //end::testValues[]
      
      for(int ixCheck = 0; ixCheck < ixTest; ++ixCheck) {
        float javaVal = resultJavaCalcF[ixCheck];
        if(Float.isNaN(javaVal)) {
          boolean javaValB = resultJavaCalcB[ixCheck];
          test.expect(this.results[ixCheck].booleanValue() == javaValB, 6, "result: "  + this.results[ixCheck].booleanValue() + " == " + javaValB + "  Expr: " + this.backExpr[ixCheck].toString() + ":");
        } 
        else {
          test.expect(this.results[ixCheck].floatValue() == javaVal, 6, "result: " + this.results[ixCheck].floatValue() + " == " + javaVal + "  Expr: " + this.backExpr[ixCheck].toString() + ":" );
        }
      }
      String[] commentForCompare = {"???", "???", "???"};
      for(int ixCheck = 0; ixCheck < ixTest; ++ixCheck) {
        int posError = StringCmp.compare(this.expr[ixCheck].toString(), cmpExprRpn[ixCheck], true, commentForCompare);
        test.expect( posError<0, 6, " RPN " + (posError >=0 ? posError : "") + ", \"" + this.expr[ixCheck].toString() + "\"");
//      }
//      for(int ixCheck = 0; ixCheck < ixTest; ++ixCheck) {
        int posError2 = StringCmp.compare(this.backExpr[ixCheck], cmpBackExpr[ixCheck], true, commentForCompare);
        test.expect( posError2<0, 6, " back" + (posError2 >=0 ? posError2 : "") + ", \"" + this.backExpr[ixCheck] + "\"");
      }
//      for(int ixCheck = 0; ixCheck < ixTest; ++ixCheck) {
//        System.out.append("\n  , \"").append(this.expr[ixCheck].toString()).append("\"");
//      }
//      for(int ixCheck = 0; ixCheck < ixTest; ++ixCheck) {
//        System.out.append("\n  , \"").append(this.backExpr[ixCheck]).append("\"");
//      }
      
      
//      test.expect(this.results[0].booleanValue() == this.parseExample.prc.q6, 6, "4. expression: " + this.results[0].booleanValue() + " == " + this.parseExample.prc.q6);
//      test.expect(this.results[1].booleanValue() == this.parseExample.prc.q7, 6, "4. expression: " + this.results[1].booleanValue() + " == " + this.parseExample.prc.q7);
//      test.expect(this.results[2].floatValue() == this.parseExample.prc.u, 6, "1. expression: " + this.results[2].floatValue() + " == " + this.parseExample.prc.u);
//      test.expect(Math.abs(this.results[3].floatValue() - this.parseExample.prc.v) < 0.0001f, 6, "2. expression: " + this.results[3].floatValue() + " == " + this.parseExample.prc.v);
//      test.expect(Math.abs(this.results[4].floatValue() - this.parseExample.prc.w) < 0.0001f, 6, "3. expression: " + this.results[4].floatValue() + " == " + this.parseExample.prc.w);
    }
    catch(Exception exc) {
      test.exception(exc);
    }
    test.finish();
    Debugutil.stop();
  }
  
  
  
  //tag::executeRpnExpr[]
  /**Executes the prepared expression in RPN with the {@link CalculatorExpr}.
   * <ul> 
   * <li> First the given expr is converted in the necessary internal form 
   * using {@link CalculatorExpr#setRpnExpr(CharSequence, Map, Class)}.
   * <li> second this expression is evaluated.
   * <li> The result is returned and outside of this routine stored and compared as test result. 
   * </ul>
   * @param expr
   * @return result
   * @throws ParseException 
   */
  CalculatorExpr.Value executeRpnExpr ( JavaSrc.Expression expr) throws ParseException {
    String sExpr = expr.toString();                        
    System.out.append("\n-----------------------------------------------------------\n");
    System.out.append(sExpr);                    // prepare the RPN for the calculator
    this.calculator.setRpnExpr(sExpr, this.nameVariables, null);
    System.out.append("\n").append(this.calculator.toString()).append("\n");
    CalculatorExpr.Value result = null;
    try {                                        // calculate the stored RPN
      Object[] values = new Object[] { this.variable, this.variableBool};
      result = this.calculator.calcDataAccess(null, values);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return result;
  }
  //end::executeRpnExpr[]
  
  
  
  static class OperatorDst {
    final String sOperator; 
    final int precedence;
    final int start;
    int end;
    
    public OperatorDst(String sOperator, int precedence, int start, int end) {
      this.sOperator = sOperator;
      this.precedence = precedence;
      this.start = start; this.end = end;
    }
  }
  
  
  
  static class ExprSegmStack {
    StringBuilder b; int precedSegm;

    public ExprSegmStack(StringBuilder b, int precedSegm) {
      this.b = b;
      this.precedSegm = precedSegm;
    }
    
  }
  
  
  
  
  
  /**This is only for test, it converts a RPN expression back to the Java infix expression. 
   * Algorithm:<ul>
   * <li> It needs push of expression segments (some parts). 
   * <li> Idea using only one StringBuilder and insert operators and parenthesis on correct position
   *   has the impact, that the other position are changed while insertion. It is more complicated.
   * <li>The decision where to set parenthesis around the segment depends on the minimal precedence in the segment
   * and the precedence of the operator between segments. If it is higher, the segment should be in (...).
   * </ul>
   * @param exprRpn
   * @return
   */
  String convertToInfixExpr(JavaSrc.Expression exprRpn) {
    StringBuilder b = new StringBuilder();
    Deque<ExprSegmStack> uStack = new ArrayDeque<ExprSegmStack>(); 
    int precedSegm = 98;
    for(JavaSrc.ExprPart part : exprRpn.get_ExprPart()) {
      String sOperator = part.get_operator();
      int preced = precedSegm;
      if( ! sOperator.equals("@")) {
        preced = JavaSrc.Expression.operatorPreced.get(sOperator);
        if(sOperator.equals("&&")) {
          //preced = 4;                                    //to test
        }
      }
      String sUnary = part.get_unaryOperator();
      CharSequence sOperand = part.getStrValue();
      if(sOperand ==null) {     // "@"                     // it is a stack operand
        if(preced >= precedSegm && precedSegm >0) {
          b.insert(0, " (").append(") ");                    // then the current expression is the right part, sOperand
        }
        sOperand = b;
        ExprSegmStack sgm = uStack.pop();                  // continue with pushed expression
        b = sgm.b;
//        if(preced > sgm.precedSegm && sgm.precedSegm >0) {
//          b.insert(0, " (").append(") ");
//        }
        precedSegm = sgm.precedSegm;                       // new beginning segment with this precedence
      }
      
      
      if(sOperator.equals("@")) {
        if(b.length()>0) {
          uStack.push(new ExprSegmStack(b, precedSegm));
          b = new StringBuilder();
          precedSegm = 99;
        }
      }
      else {
        if(preced > precedSegm) {
          if(precedSegm >0) {
            b.insert(0, " (").append(") ");
          }
        } else {
          precedSegm = preced;                             // The lowest precedence of this segment of expression
        }
        b.append(sOperator);
      }
      b.append(' ');
      if(sUnary !=null) {
        b.append(sUnary);
      }
      b.append(sOperand).append(' ');        // write operand first if "@" or after operation.
    }
    return b.toString();
  }
  
  
  
  
  
  public JavaSrc parseJava(File fileIn) throws IllegalCharsetNameException, UnsupportedCharsetException, FileNotFoundException, IOException {
    boolean bOk = false;
    long timeStart = System.currentTimeMillis();
    Writer logParsingComponents = new FileWriter("T:/logParsingComponents.txt");
    this.parser.setLogComponents(logParsingComponents);
    //try 
    bOk = this.parser.parseFile(fileIn); 
    logParsingComponents.close();
    this.parser.setLogComponents(null);
    
    //catch(Exception exc){ throw new IllegalArgumentException("JavaParser - file ERROR; " + fileIn.getAbsolutePath() + ":" + exc.getMessage() ); }
    if(!bOk) {
      String sError = this.parser.getSyntaxErrorReport();
      System.err.println("ERROR Parsing file: " + fileIn.getAbsolutePath() + "\n" + sError);
      return null;
    }
    else {
      System.out.println("JavaParser: parsing ok ms:" + Long.toString(System.currentTimeMillis() - timeStart));
      try {
        Writer outStore = new FileWriter("T:/javaParsResult.text");
        this.parser.writeResultAsTextList(outStore);
        outStore.close();
        System.out.println("JavaParser: write to T:/javaParsResult.text done ms:" + Long.toString(System.currentTimeMillis() - timeStart));
        
        
        Zbnf2Xml.writeZbnf2Xml(parser, "T:/javaParsResult.xml", null);
        System.out.println("JavaParser: write to T:/javaParsResult.xml done ms:" + Long.toString(System.currentTimeMillis() - timeStart));
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      //Content resultFile = new Content(pathJavasrc);   //Container for the parsed file.
      JavaSrc_Zbnf result = new JavaSrc_Zbnf();
      ZbnfParseResultItem resultItem = this.parser.getFirstParseResult();
      try{ ZbnfJavaOutput.setOutputStrictNewFromType(result, resultItem, this.console); }
      catch(Exception exc) {
        String sError = exc.getMessage();
        System.out.println("JavaParser: store to internal data ERROR ms:" + Long.toString(System.currentTimeMillis() - timeStart) + " ..." + sError);
        throw new IllegalArgumentException("JavaParser - internal ERROR storing parse result; " + exc.getMessage());
      }
      System.out.println("JavaParser: store to internal data done ms:" + Long.toString(System.currentTimeMillis() - timeStart));
      return result.dataJavaSrc;
    }    
  }
    

  
}
