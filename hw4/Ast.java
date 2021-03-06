// This is supporting software for CS321/CS322 Compilers and Language Design.
// Copyright (c) Portland State University.
//---------------------------------------------------------------------------
// For CS321 F'15 (J. Li).
//

// MATTHEW OBRIEN CS321 F'15


import java.util.*;

// A class for representing set of variables.
//---------------------------------------------------------------------------
// This class defines immutable sets of Strings by extending Java's (mutable) 
// HashSet class. For every operation of union, intersect, and add, a new set 
// is created to hold the result.
//
class VarSet extends HashSet<String> {
  static VarSet union(VarSet s1, VarSet s2) {
    VarSet s = new VarSet();
    s.addAll(s1);
    s.addAll(s2);
    return s;
  }

  static VarSet intersect(VarSet s1, VarSet s2) {
    VarSet s = new VarSet();
    for (String x : s1)
      if (s2.contains(x))
	s.add(x);
    return s;
  }

  static VarSet add(VarSet s0, String x) { 
    VarSet s = new VarSet();
    s.addAll(s0);
    s.add(x);
    return s;
  }
}

// A class for reporting static errors.
//---------------------------------------------------------------------------
//
class StaticError extends Exception {
  StaticError(String message) { super(message); }
}

// AST Definition.
//---------------------------------------------------------------------------
//

class Ast {
  static int tab=0;	// indentation for printing AST.

  public abstract static class Node {
    String tab() {
      String str = "";
      for (int i = 0; i < Ast.tab; i++)
	str += " ";
      return str;
    }
  }

  // Define constant nodes for classes with no fields
  // --- to avoid unnecessary object allocation.
  //
  public static final IntType IntType = new IntType();
  public static final DblType DblType = new DblType();
  public static final BoolType BoolType = new BoolType();
  public static final This This = new This();

  // Program Node -------------------------------------------------------

  public static class Program extends Node {
    public final ClassDecl[] classes;

    public Program(ClassDecl[] ca) { classes=ca; }
    public Program(List<ClassDecl> cl) { 
      this(cl.toArray(new ClassDecl[0]));
    }

    public String toString() { 
      String str = "# AST Program\n";
      for (ClassDecl c: classes) 
	str += c;
      return str;
    }

    // Static analysis for detecting unreachable statements
    //
    public void checkReach() throws Exception {
      for (ClassDecl c: classes) 
	c.checkReach();
    }

    // Static analysis for detecting uninitialized variables
    //
    public void checkVarInit() throws Exception {
      for (ClassDecl c: classes) 
	c.checkVarInit(new VarSet());
    }
  }   

  // Declarations -------------------------------------------------------

  public static class ClassDecl extends Node {
    public final String nm;	       // class name
    public final String pnm;	       // parent class name (could be null)
    public final VarDecl[] flds;       // fields
    public final MethodDecl[] mthds;   // methods

    public ClassDecl(String c, String p, VarDecl[] va, MethodDecl[] ma) {
      nm=c; pnm=p; flds=va; mthds=ma;
    }
    public ClassDecl(String c, String p, List<VarDecl> vl, List<MethodDecl> ml) {
      this(c, p, vl.toArray(new VarDecl[0]), ml.toArray(new MethodDecl[0]));
    }
    public String toString() { 
      String str = "ClassDecl " + nm + " " + (pnm==null ? "" : pnm) + "\n"; 
      Ast.tab = 2; 
      for (VarDecl v: flds) 
	str += v;
      for (MethodDecl m: mthds) 
	str += m;
      return str;
    }

    void checkReach() throws Exception {
      for (MethodDecl m: mthds) 
	m.checkReach(true);
    }


    //Task 2
    void checkVarInit(VarSet initSet) throws Exception {
      for(VarDecl v: flds)
	  initSet = initSet.add(initSet, v.nm);
      for(MethodDecl m: mthds)
	m.checkVarInit(initSet);
      // ... need code ...
    }
  }

  public static class MethodDecl extends Node {
    public final Type t;	    // return type (could be null)
    public final String nm;	    // method name
    public final Param[] params;    // param parameters
    public final VarDecl[] vars;    // local variables
    public final Stmt[] stmts;	    // method body

    public MethodDecl(Type rt, String m, Param[] fa, VarDecl[] va, Stmt[] sa) {
      t=rt; nm=m; params=fa; vars=va; stmts=sa;
    }
    public MethodDecl(Type rt, String m, List<Param> fl, List<VarDecl> vl, List<Stmt> sl) {
      this(rt, m, fl.toArray(new Param[0]), 
	   vl.toArray(new VarDecl[0]), sl.toArray(new Stmt[0]));
    }
    public String toString() { 
      String str = "  MethodDecl " + (t==null ? "void" : t) + " " + nm + " ("; 
      for (Param f: params) 
	str += f + " ";
      str += ")\n";
      Ast.tab = 3; 
      for (VarDecl v: vars) 
	str += v;
      for (Stmt s: stmts) 
	str += s;
      return str;
    }


    //Task 1
    boolean checkReach(boolean reachable) throws Exception {
      boolean flag = true;
  
      for (Stmt s : stmts){
	if(!flag)
	  s.checkReach(false);
	else
	  flag = s.checkReach(true); 
      }
      if(flag)
	return false;
      else
	return true;
      //return true;
      
    }


    //Task 2
    void checkVarInit(VarSet initSet) throws Exception {

      
      //Coding to be done.

      for(Param p: params)
	initSet = initSet.add(initSet, p.nm);
      for(VarDecl v: vars){
	if(v.init != null)
	  initSet = initSet.add(initSet, v.nm);
      }
      for(Stmt s: stmts)
	initSet = s.checkVarInit(initSet);
    }


  }

  public static class VarDecl extends Node {
    public final Type t;     // variable type
    public final String nm;  // variable name
    public final Exp init;   // init expr (could be null)

    public VarDecl(Type at, String v, Exp e) { t=at; nm=v; init=e; }

    public String toString() { 
      return tab() + "VarDecl " + t + " " + nm + " " + 
	(init==null ? "()" : init) + "\n"; 
    }
  }

  public static class Param extends Node {
    public final Type t;     // parameter type
    public final String nm;  // parameter name

    public Param(Type at, String v) { t=at; nm=v; }

    public String toString() { 
      return "(Param " + t + " " + nm + ")"; 
    }
  }

  // Types --------------------------------------------------------------

  public static abstract class Type extends Node {}

  public static class IntType extends Type {
    public String toString() { return "IntType"; }
  }

  public static class DblType extends Type {
    public String toString() { return "Double"; }
  }

  public static class BoolType extends Type {
    public String toString() { return "BoolType"; }
  }

  public static class ArrayType extends Type {
    public final Type et;  // array element type

    public ArrayType(Type t) { et=t; }

    public String toString() { 
      return "(ArrayType " + et + ")"; 
    }
  }

  public static class ObjType extends Type {
    public final String nm;  // object's class name

    public ObjType(String i) { nm=i; }

    public String toString() { 
      return "(ObjType " + nm + ")"; 
    }
  }

  // Statements ---------------------------------------------------------

  public static abstract class Stmt extends Node {
    abstract boolean checkReach(boolean reachable) throws Exception;
    abstract VarSet checkVarInit(VarSet initSet) throws Exception;
  }

  public static class Block extends Stmt {
    public final Stmt[] stmts;

    public Block(Stmt[] sa) { stmts=sa; }
    public Block(List<Stmt> sl) { 
      this(sl.toArray(new Stmt[0])); 
    }
    public String toString() { 
      String s = "";
      if (stmts!=null) {
	s = tab() + "{\n";
	Ast.tab++; 
	for (Stmt st: stmts) 
	  s += st;
	Ast.tab--;
	s += tab() + "}\n"; 
      }
      return s;
    }


    //Task 1
    boolean checkReach(boolean reachable) throws Exception {
      boolean returnFlag = false;

      if (!reachable) 
	throw new StaticError("Unreachable statement: " + this);

      for (Stmt s: stmts){

	if(returnFlag)
	  s.checkReach(false);
	else
	  s.checkReach(true);
	

	if(s instanceof Ast.Return)
	  returnFlag = true;
      }

      if(returnFlag)
	return false;
      else 
	return true;
       
    }
 
    //Task 2
    VarSet checkVarInit(VarSet initSet) throws Exception {
      for(Stmt s: stmts)
	initSet = s.checkVarInit(initSet);
      return initSet;
    }

  }

  public static class Assign extends Stmt {
    public final Exp lhs;  
    public final Exp rhs;  

    public Assign(Exp e1, Exp e2) { lhs=e1; rhs=e2; }

    public String toString() { 
      return tab() + "Assign " + lhs + " " + rhs + "\n"; 
    }

    //Task 1
    boolean checkReach(boolean reachable) throws Exception {
      if (!reachable) 
	throw new StaticError("Unreachable statement: " + this);
      return true;
    }




    //Task 2
    VarSet checkVarInit(VarSet initSet) throws Exception {
      rhs.checkVarInit(initSet);
      
      //See if it's an ID
      if(lhs instanceof Ast.Id){     
        Ast.Id temp; 
        temp = (Ast.Id)lhs;     
        initSet = initSet.add(initSet,temp.nm);
      }
      //Check for uninitialized array element
      else if(lhs instanceof Ast.ArrayElm){
        lhs.checkVarInit(initSet);
      }
      //Check for new array
      //Not needed for this part, handled elsewhere.
/*      else if(rhs instanceof Ast.NewArray){
        Ast.NewArray temp;
        temp = (Ast.Id)lhs;
        initSet = initSet.add(initSet,temp.nm);
      } 
*/
      //Check for new object
      else if(lhs instanceof Ast.NewObj){
        Ast.NewObj temp;
        temp = (Ast.NewObj)lhs;
        initSet = initSet.add(initSet,temp.nm);
      }
      //Check for field
      else if(lhs instanceof Ast.Field){
        Ast.Field temp;
        temp = (Ast.Field)lhs;
        initSet = initSet.add(initSet,temp.nm);   
      }        
      return initSet;
    }


  }

  public static class CallStmt extends Stmt {
    public final Exp obj;     // class object
    public final String nm;   // method name
    public final Exp[] args;  // arguments

    public CallStmt(Exp e, String s, Exp[] ea) { 
      obj=e; nm=s; args=ea; 
    }
    public CallStmt(Exp e, String s, List<Exp> el) { 
      this(e, s, el.toArray(new Exp[0])); 
    }
    public String toString() { 
      String s = tab() + "CallStmt " + obj + " " + nm + " ("; 
      for (Exp e: args) 
	s += e + " "; 
      s += ")\n"; 
      return s;
    }


    //Task 1
    boolean checkReach(boolean reachable) throws Exception {
      if (!reachable) 
	throw new StaticError("Unreachable statement: " + this);
      return true;
    }



    //Task 2
    VarSet checkVarInit(VarSet initSet) throws Exception {
      obj.checkVarInit(initSet);

      for(Exp e: args)
	e.checkVarInit(initSet);
      return initSet;
    }
  }

  public static class If extends Stmt {
    public final Exp cond;  
    public final Stmt s1;   // then clause
    public final Stmt s2;   // else clause (could be null)

    public If(Exp e, Stmt as1, Stmt as2) { cond=e; s1=as1; s2=as2; }

    public String toString() { 
      String str = tab() + "If " + cond + "\n"; 
      Ast.tab++; 
      str += s1; 
      Ast.tab--;
      if (s2 != null) {
	str += tab() + "Else\n";
	Ast.tab++; 
	str += s2; 
	Ast.tab--;
      }
      return str;
    }


    //Task 1
    boolean checkReach(boolean reachable) throws Exception {

      boolean boolFlag1 = true;
      boolean boolFlag2 = true;

      if (!reachable) 
	throw new StaticError("Unreachable statement: " + this);
   
      if(s1 instanceof Ast.Return){
	boolFlag1 = false;
	s1.checkReach(true);
      }
      else
	boolFlag1 = s1.checkReach(true);

      if(s2 !=null){
	if(s2 instanceof Ast.Return){
	  boolFlag2 = false;
	  s2.checkReach(true);
	}
	else
	  boolFlag2 = s2.checkReach(true);
      }
      if(!boolFlag1 && !boolFlag2)
	return false;
      else
	return true;
    }

    //Task 2
    VarSet checkVarInit(VarSet initSet) throws Exception {
      if(s2 != null){
        VarSet temp = new VarSet();
        VarSet temp2 = new VarSet();

        temp = s1.checkVarInit(initSet);
        temp2 = s2.checkVarInit(initSet);

        temp2 = temp2.intersect(temp2, temp);

        initSet = initSet.union(initSet, temp2);
      }      
      else
        s1.checkVarInit(initSet);

      cond.checkVarInit(initSet);
      return initSet;
    }


  }


  public static class While extends Stmt {
    public final Exp cond;
    public final Stmt s;

    public While(Exp e, Stmt as) { cond=e; s=as; }

    public String toString() { 
      String str = tab() + "While " + cond + "\n";
      Ast.tab++; 
      str += s; 
      Ast.tab--;
      return str;
    }


    //Task 1
    boolean checkReach(boolean reachable) throws Exception {
      boolean checkFlag = false;

      if (!reachable) 
	throw new StaticError("Unreachable statement: " + this);
      if(s instanceof Ast.Return){
	s.checkReach(true);
	checkFlag = true;
      }   
      else if(s instanceof Ast.Block){
	//Ast.Block block = (Ast.Block)s;
        //for(Stmt s: stmts){
	  if(checkFlag)
	    s.checkReach(false);
	  else
	    s.checkReach(true);
	  if(s instanceof Ast.Return)
	    checkFlag = true;
        }
      //}  
      else
	s.checkReach(true);

      if(checkFlag)
	return true;
      else
	return false;

 //     s.checkReach(reachable);
 //     return true;
    }


    //Task 2
    VarSet checkVarInit(VarSet initSet) throws Exception {
      cond.checkVarInit(initSet);
      return initSet;
    }


  }   

  public static class Print extends Stmt {
    public final Exp arg;  // (could be null)

    public Print(Exp e) { arg=e; }

    public String toString() { 
      return tab() + "Print " + (arg==null ? "()" : arg) + "\n"; 
    }


    //Task 1
    boolean checkReach(boolean reachable) throws Exception {
      if (!reachable) 
	throw new StaticError("Unreachable statement: " + this);
      return true;
    }


    //Task 2
    VarSet checkVarInit(VarSet initSet) throws Exception {
      if(arg != null)
        arg.checkVarInit(initSet);
      return initSet;
    }


  }

  public static class Return extends Stmt {
    public final Exp val;  // (could be null)

    public Return(Exp e) { val=e; }

    public String toString() { 
      return tab() + "Return " + (val==null ? "()" : val) + "\n"; 
    }


    //Task 1
    boolean checkReach(boolean reachable) throws Exception {
      if(!reachable)
	throw new StaticError("Unreachable statement: " + this);
      return false;
    }


    //Task 2
    VarSet checkVarInit(VarSet initSet) throws Exception {
      if(val != null)
        val.checkVarInit(initSet);
      return initSet;
    }


  }

  // Expressions --------------------------------------------------------

  public static abstract class Exp extends Node {
    // default routine for Exp nodes
    void checkVarInit(VarSet initSet) throws Exception {}
  }

  public static class Binop extends Exp {
    public final BOP op;
    public final Exp e1;
    public final Exp e2;

    public Binop(BOP o, Exp ae1, Exp ae2) { op=o; e1=ae1; e2=ae2; }

    public String toString() { 
      return "(Binop " + op + " " + e1 + " " + e2 + ")";
    }
    
    //Task 2
    void checkVarInit(VarSet initSet) throws Exception{
      e1.checkVarInit(initSet);
      e2.checkVarInit(initSet);     

    }
  }

  public static class Unop extends Exp {
    public final UOP op;
    public final Exp e;

    public Unop(UOP o, Exp ae) { op=o; e=ae; }

    public String toString() { 
      return "(Unop " + op + " " + e + ")";
    }

    //Task 2
    void checkVarInit(VarSet initSet) throws Exception{
      e.checkVarInit(initSet);
    }

  }

  public static class Call extends Exp {
    public final Exp obj;     // class object
    public final String nm;   // method name
    public final Exp[] args;  // arguments

    public Call(Exp e, String s, Exp[] ea) { 
      obj=e; nm=s; args=ea; 
    }
    public Call(Exp e, String s, List<Exp> el) { 
      this(e, s, el.toArray(new Exp[0])); 
    }
    public String toString() { 
      String str ="(Call " + obj + " " + nm + " ("; 
      for (Exp e: args) 
	str += e + " "; 
      str += "))"; 
      return str; 
    }
    
    //Task 2
    void checkVarInit(VarSet initSet) throws Exception{
      obj.checkVarInit(initSet); 
      for(Exp e: args)
	e.checkVarInit(initSet); 
    }

  }

  public static class NewArray extends Exp {
    public final Type et;  // element type
    public final int len;  // array length

    public NewArray(Type t, int i) { et=t; len=i; }

    public String toString() { 
      return "(NewArray " + et + " " + len + ")";
    }
  }

  public static class ArrayElm extends Exp {
    public final Exp ar;   // array object
    public final Exp idx;  // element's index

    public ArrayElm(Exp e1, Exp e2) { ar=e1; idx=e2; }

    public String toString() { 
      return "(ArrayElm " + ar + " " + idx +")";
    }
    
    //Task 2
    void checkVarInit(VarSet initSet) throws Exception {
      ar.checkVarInit(initSet);
      idx.checkVarInit(initSet);

    }

  }

  public static class NewObj extends Exp {
    public final String nm;   // class name

    public NewObj(String s) { nm=s; }
    public String toString() { 
      return "(NewObj " + nm + ")"; 
    }
  }

  public static class Field extends Exp {
    public final Exp obj;    // class object
    public final String nm;  // field name

    public Field(Exp e, String s) { obj=e; nm=s; }

    public String toString() { 
      return "(Field " + obj + " " +  nm + ") ";
    }
 
    //Task 2
    void checkVarInit(VarSet initSet) throws Exception {
      obj.checkVarInit(initSet);
    }

  }

  public static class Id extends Exp {
    public final String nm;  // id name

    public Id(String s) { nm=s; }
    public String toString() { return nm; }
 
    void checkVarInit(VarSet initSet) throws Exception {
     
      //Stuff to type
      VarSet temp = new VarSet();
      VarSet temp2 = new VarSet();

      //Add string to temp VarSet
      temp = temp.add(temp, nm);
     
      //Check for intersection of two VarSets
      temp2 = temp2.intersect(initSet, temp);

      //Check if temp2 is empty, if so throw exception
      if(temp2.equals(temp))
        return;
      else
        throw new StaticError("Uninitialized variable " + this);
    }
  }

  public static class This extends Exp {
    public String toString() { return "This"; }
  }

  public static class IntLit extends Exp {
    public final int i; 
    
    public IntLit(int ai) { i=ai; }
    public String toString() { return i + ""; }
  }

  public static class DblLit extends Exp {
    public final double d; 
    
    public DblLit(double ad) { d=ad; }
    public String toString() { return d + ""; }
  }

  public static class BoolLit extends Exp {
    public final boolean b;	

    public BoolLit(boolean ab) { b=ab; }
    public String toString() { return b + ""; }
  }

  // String literal is not an expression
  public static class StrLit extends Exp {
    public final String s;	

    public StrLit(String as) { s=as; }
    public String toString() { return "\"" + s + "\""; }
  }

  // Operators ----------------------------------------------------------

  public static enum BOP {
    ADD("+"), SUB("-"), MUL("*"), DIV("/"), AND("&&"), OR("||"),
    EQ("=="), NE("!="), LT("<"), LE("<="), GT(">"), GE(">=");
    private String name;

    BOP(String n) { name = n; }
    public String toString() { return name; }
  }

  public static enum UOP {
    NEG("-"), NOT("!");
    private String name;

    UOP(String n) { name = n; }
    public String toString() { return name; }
  }

}
