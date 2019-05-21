package app;

import java.io.*;
import java.util.*;
//import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";
			
    /**
     * Populates the vars list with simple variables, and arrays lists with arrays
     * in the expression. For every variable (simple or array), a SINGLE instance is created 
     * and stored, even if it appears more than once in the expression.
     * At this time, values for all variables and all array items are set to
     * zero - they will be loaded from a file in the loadVariableValues method.
     * 
     * @param expr The expression
     * @param vars The variables array list - already created by the caller
     * @param arrays The arrays array list - already created by the caller
     */
    public static void 
    makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	/** COMPLETE THIS METHOD **/
    	/** DO NOT create new vars and arrays - they are already created before being sent in
    	 ** to this method - you just need to fill them in.
    	 **/
    	for (int i = 0; i < expr.length(); i++) {
    		String varName = "";
    		while(i < expr.length() && Character.isLetter(expr.charAt(i)) == true) {
    			varName += expr.charAt(i);
    			if(i == expr.length() - 1) {
    				break;
    			}else {
    				i++;
    			}
    		}
    		
    		if(varName != "") {
    			if(expr.charAt(i) == '[') {
    				if(arrays.contains(new Array(varName))) {
    					continue;
    				}
    				arrays.add(new Array(varName));
        		}else {
        			if(vars.contains(new Variable(varName))) {
    					continue;
    				}
        			vars.add(new Variable(varName));
        		}
    		}
    	}
    	printArray(vars);
    	printArray(arrays);
    }
    
    private static <T> void printArray(ArrayList<T> a) {
    	for(int i = 0; i < a.size(); i++) {
    		System.out.println(a.get(i).toString());
    	}
    }
    
    /**
     * Loads values for variables and arrays in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     * @param vars The variables array list, previously populated by makeVariableLists
     * @param arrays The arrays array list - previously populated by makeVariableLists
     */
    public static void 
    loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String tok = st.nextToken();
            Variable var = new Variable(tok);
            Array arr = new Array(tok);
            int vari = vars.indexOf(var);
            int arri = arrays.indexOf(arr);
            if (vari == -1 && arri == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                vars.get(vari).value = num;
            } else { // array symbol
            	arr = arrays.get(arri);
            	arr.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;              
                }
            }
        }
    }
    
    /**
     * Evaluates the expression.
     * 
     * @param vars The variables array list, with values for all variables in the expression
     * @param arrays The arrays array list, with values for all array items
     * @return Result of evaluation
     */
    public static float 
    evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	expr = expr.replaceAll("\\s+","");
    	if(expr.length() == 1 && Character.isDigit(expr.charAt(0))) {
    		float answer = Float.valueOf(expr);
    		return answer;
    	}else if(expr.matches("[0-9]+")) {
    		return Float.valueOf(expr);
    	}else if(expr.length() == 1 && Character.isLetter(expr.charAt(0))){
    		return (float) vars.get(0).value;
    	}else if(!(vars.isEmpty()) && expr.equals(vars.get(0).name)) {
    		return (float) vars.get(0).value;
    	} 
    	
    	String expression = update(expr, vars, arrays);
    	expression = evaluate(expression);
    	return Float.parseFloat(expression);
    } 
    private static String evaluate(String s) {
    	if(s == null || s.length() == 0) {
    		return "0";
    	}
    	if(!(s.contains("["))){
    		Stack<String> stk = new Stack<>();
        	if(!(s.contains("("))){
        		for(int i = 0; i < s.length(); i++) {
        			if(s.charAt(i) == '*' || s.charAt(i) == '/') {
        				String temp1 = "";
        				int o = i + 1;
        				boolean check = s.charAt(o) == '-';
        				while(o < s.length() && (Character.isDigit(s.charAt(o)) || s.charAt(o) == '.' || check || (s.charAt(o) == 'E' && s.charAt(o+1) == '-') || (s.charAt(o-1) == 'E' && s.charAt(o) == '-'))) {
        					temp1 += s.charAt(o);
        					check = false;
        					o++;
        				}
        				if(s.charAt(i) == '*') {
        					double fir = Double.parseDouble(temp1);
        					float first = (float) fir;
        					double sec = Double.parseDouble(stk.pop());
        					float second = (float) sec;
        					stk.push(String.valueOf(first*second));
        				}else {
        					double fir = Double.parseDouble(temp1);
        					float first = (float) fir;
        					double sec = Double.parseDouble(stk.pop());
        					float second = (float) sec;
        					stk.push(String.valueOf(second/first));
        				}
        				i = o - 1;
        			}else if(s.charAt(i) == '+' || s.charAt(i) == '-' && i != 0) {
        				stk.push(Character.toString(s.charAt(i)));
        				String temp2 = "";
        				int o = i + 1;
        				boolean check = s.charAt(o) == '-';
        				while(o < s.length() && (Character.isDigit(s.charAt(o)) || s.charAt(o) == '.' || check || (s.charAt(o) == 'E' && s.charAt(o+1) == '-') || (s.charAt(o-1) == 'E' && s.charAt(o) == '-'))) {
        					temp2 += s.charAt(o);
        					check = false;
        					o++;
        				}
        				stk.push(temp2);
        				i = o - 1;
        			}else {
        				String temp3 = "";
        				int o = i;
        				boolean check = s.charAt(0) == '-';
        				while(o < s.length() && (Character.isDigit(s.charAt(o)) || s.charAt(o) == '.' || check || (s.charAt(o) == 'E' && s.charAt(o+1) == '-') || (s.charAt(o-1) == 'E' && s.charAt(o) == '-'))) {
        					temp3 += s.charAt(o);
        					check = false;
        					o++;
        				}
        				stk.push(temp3);
        				i = o - 1;
        			}    			
        		}
        		Stack <String> stkC = new Stack<>();
        		while(!(stk.isEmpty())) {
        			stkC.push(stk.pop());
        		}
        		stk = stkC;
        		while(!(stk.size() == 1)) {
            		String pop2 = stk.pop();
            		if(stk.peek().equals("-")) {
            			stk.pop();
            			String pop1 = stk.pop();
            			if(pop1.charAt(0) == '-') {
            				pop1 = pop1.substring(1);
            				stk.push(String.valueOf((Float.parseFloat(pop2) + Float.parseFloat(pop1)))); 
            			}else {
            				String check = String.valueOf((Float.parseFloat(pop2) - Float.parseFloat(pop1)));
            				stk.push(check); // empty list
            			}
            		}else {
            			stk.pop();
            			stk.push(String.valueOf((Float.parseFloat(stk.pop()) + Float.parseFloat(pop2))));            		}
            	}
            	return stk.pop();
        	}else {
        		return withPar(s);
        	}	
    	}
    	return "";	
    }
    
    private static String update(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	
    	String newS = "";
    	
    	for(int i = 0; i < expr.length(); i++) {
    		String temp = "";
    		while(i < expr.length() && Character.isLetter(expr.charAt(i))) {
    			temp += expr.charAt(i);
    			i++;
    		}
    		if(vars.size() == 0) {
    			newS += temp;
    		}
    		for(int o = 0; o < vars.size(); o++) {
    			if(vars.get(o).name.equals(temp)) {
    				newS += vars.get(o).value;
    				break;
    			}else if(!(vars.get(o).name.equals(temp)) && i < expr.length() && expr.charAt(i) == '['){
    				 newS += temp;
    				 break;
    			} 
    		}
    		if(i < expr.length() && !(Character.isLetter(expr.charAt(i)))) {
    			newS += expr.charAt(i);
    		}
    	}
    	if(newS.contains("[")) {
    		int open = newS.indexOf("[");
    		int close = newS.indexOf("]");
    		int temp = -1;
    		String arrayN = "";
    		String start = "";
    		String end = "";
    		while(open < close) {
    			temp = open;
    			int a = newS.substring(open + 1).indexOf('[');
    			open = temp + 1 + newS.substring(open + 1).indexOf('[');
    			if(a == -1) {
    				break;
    			}
    		}
    		int i = temp - 1;
    		while(i > 0 && Character.isLetter(newS.charAt(i))) {
    			i--;
    		}
    		if(i > 0) {
    			i = i + 1;
    		}
    		arrayN = newS.substring(i, temp);
    		start = newS.substring(0, i);
    		if(close < newS.length() - 1) {
    			end = newS.substring(close + 1);
    		}else {
    			end = "";
    		}
    		String s = newS.substring(temp+1, close);
    		String valS = evaluate(s);
    		Float value = Float.parseFloat(valS);
    		int valueInt = (int) Math.round(value);
    		String val = "";
    		for(int o = 0; o < arrays.size() ; o++) {
    			if(arrays.get(o).name.equals(arrayN)) {
    				val = "" + arrays.get(o).values[valueInt];
    			}
    		}
        	return update(start+val+end, vars, arrays);
    	}
    	return newS;
    }
    
    /*private static String withPar(String s) {
    	int index = s.indexOf('(');
    	if(index == -1) {
    		return evaluate(s);
    	}else {
    		int closeIndex = s.indexOf(')');
    		int temp = -1;
    		String start = "";
    		String end = "";
    		String middle = "";
    		while(index < closeIndex) {
    			temp = index;
    			int a = s.substring(index + 1).indexOf('(');
    			index = s.substring(index + 1).indexOf('(') + s.substring(0, temp).length() + 1;
    			if(a == -1) {
    				break;
    			}
    			
    		}
    		start = s.substring(0, temp);
    		String toEvaluate = s.substring(temp+1, closeIndex);
    		middle = evaluate(toEvaluate);
    		if(closeIndex < s.length() - 1) {
    			end = s.substring(closeIndex + 1);
    		}else {
    			end = "";
    		}
    		return withPar(start + middle + end);
    	}
    }*/
    
    private static String withPar(String s) {
    	String temp1 = "";
		String temp = new String(s);
		if(s.indexOf('(') == -1) {
			return evaluate(s);
		}else if(onlyOne(s)){
			boolean check = false;
			if(s.indexOf(')') == s.length() - 1) {
				check = true;
			}
			int i = 0;
			while(s.charAt(i) != '(') {
				temp1 += s.charAt(i);
				i++;
			}
			if(check) {
				String answer =  evaluate(s.substring(i+1, s.length() - 1));
				temp1 += answer;
				return withPar(temp1);
			}else {
				String answer = evaluate(s.substring(i+1, s.indexOf(")")));
				temp1 += answer;
				return withPar(temp1 + s.substring(s.indexOf(")") + 1));
			}
		}else {
			while(temp.indexOf('(') != -1) {
    			int a = temp.indexOf('(');
    			int a2 = -1;
    			int b = temp.indexOf(')');
    			String temp2 = "";
    			while(a < b) {
    				a2 = a;
    				temp2 = temp.substring(a2 + 1);
    				a = a + 1 + temp2.indexOf("(");
    				if(temp2.indexOf("(") == -1) {
    					break;
    				}
    				
    			}
    			
    			String start = temp.substring(0, a2);
    			String end = temp.substring(b+1);
    			String middle = evaluate(temp.substring(a2+1, b));
    			
    			return withPar(start+middle+end);
    			
			}
		}
		return "";
	}
    
    private static boolean onlyOne(String s) {
    	if(s.indexOf("(", s.indexOf('(') + 1) == -1) {
    		return true;
    	}
			return false;
    }

}



