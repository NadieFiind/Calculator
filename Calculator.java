import java.util.ArrayList;
import java.lang.Character;

@SuppressWarnings("unchecked")

class Quantities {
	ArrayList<Object> quantities;
	int skips;
	Quantities(ArrayList<Object> _quantities, int _skips) {
		quantities = _quantities;
		skips = _skips;
	}
}

public class Calculator {
	final String DIGITS = "0123456789";
	final String ADD = "+";
	final String SUB = "-";
	final String MUL = "*";
	final String DIV = "/";
	final String MOD = "%";
	final String POW = "^";
	final String OPE = "(";
	final String CLO = ")";
	
	String validate(String input) {
		for (char c: input.toCharArray()) {
			String inp = String.valueOf(c);
			
			if (Character.isWhitespace(c)) {
				continue;
			}
			
			switch (inp) {
				case ADD: break;
				case SUB: break;
				case MUL: break;
				case DIV: break;
				case MOD: break;
				case POW: break;
				case OPE: break;
				case CLO: break;
				default:
					if (!DIGITS.contains(inp)) {
						throw new NumberFormatException();
					}
			}
		}
		
		if (!checkParentheses(input)) {
			throw new NumberFormatException();
		}
		
		return input;
	}
	boolean checkParentheses(String input) {
		boolean balanced = true;
		int count = 0;
		
		for (char c: input.toCharArray()) {
			String inp = String.valueOf(c);
			if (inp.equals(OPE)) {
				count++;
			} else if (inp.equals(CLO)) {
				count--;
			}
			if (count < 0) {
				break;
			}
		}
		
		if (count != 0) {
			balanced = false;
		}
		
		return balanced;
	}
	
	ArrayList<String> tokenize(String input) {
		ArrayList<String> tokens = new ArrayList<String>();
		tokens.add(ADD); /* initial sign so that a single parentheses quantity for example (3*2)
		                    is still a valid syntax because parentheses quantities are
		                    not allowed without an operator beside them */
		for (char c: input.toCharArray()) {
			String inp = String.valueOf(c);
			if (Character.isWhitespace(c)) {
				continue;
			} else if (DIGITS.contains(inp)) {
				String lastToken = tokens.get(tokens.size() - 1);
				String firstChar = String.valueOf(lastToken.charAt(0));
				if (DIGITS.contains(firstChar)) {
					tokens.set(tokens.size() - 1, lastToken + inp);
				} else {
					tokens.add(inp);
				}
			} else {
				tokens.add(inp);
			}
		}
		return tokens;
	}
	
	// P in PEMDAS
	Quantities quantify(ArrayList<String> tokens, int startIndex) {
		ArrayList<Object> quantities = new ArrayList<Object>();
		int s = 0; // no. of quantified tokens to be skipped so that it will not be quantified again
		int skips = s; // original value of 's' so that it can be passed when new 'Quantities' is returned from a recursion
		boolean hasOperator = true;
		for (int i = startIndex + 1; i < tokens.size(); i++) {
			if (s > 0) {
				s--;
				continue;
			} else if (tokens.get(i).equals(OPE)) {
				// check if the previous token is an operator
				if (i > 0) {
					hasOperator = isTokenOperator(tokens.get(i - 1));	
				}
				
				Quantities quantified = quantify(tokens, i);
				quantities.add(quantified.quantities);
				
				s = quantified.quantities.size() + quantified.skips + 1;
				skips = s;
			} else if (tokens.get(i).equals(CLO)) {
				// check if the next token is an operator
				if (i < tokens.size() - 1) {
					if (!hasOperator) {
						hasOperator = isTokenOperator(tokens.get(i + 1));
					}
				}
				break;
			} else {
				quantities.add(tokens.get(i));
			}
		}
		
		// check if the quantity does not have an operator
		if (!hasOperator) {
			throw new NumberFormatException();
		}
		
		return new Quantities(quantities, skips);
	}
	boolean isTokenOperator(String token) {
		boolean isOperator = false;
		switch (token) {
			case ADD:
				isOperator = true;
				break;
			case SUB:
				isOperator = true;
				break;
			case MUL:
				isOperator = true;
				break;
			case DIV:
				isOperator = true;
				break;
			case MOD:
				isOperator = true;
				break;
			case POW:
				isOperator = true;
		}
		return isOperator;
	}
	
	// EMD in PEMDAS
	ArrayList<String> solveEMD(ArrayList<Object> quantities) {
		ArrayList<String> terms = new ArrayList<String>();
		int skipTerms = 0; // used to skip the next term if it is already evaluated
		for (int i = 0; i < quantities.size(); i++) {
			String qttType = quantities.get(i).getClass().getSimpleName();
			if (qttType.equals("String")) {
				String qtt = (String) quantities.get(i);
				if (qtt.equals(POW) || qtt.equals(MUL) || qtt.equals(DIV) || qtt.equals(MOD)) {
					skipTerms = evaluateEMD(quantities, terms, i, qtt);
				} else {
					if (skipTerms > 0) {
						skipTerms--;
					} else {
						terms.add(qtt);
					}
				}
			} else {
				if (skipTerms > 0) {
					skipTerms--;
				} else {
					ArrayList<String> qtt = solveEMD((ArrayList<Object>) quantities.get(i));
					terms.addAll(qtt);
				}
			}
		}
		return terms;
	}
	int evaluateEMD(ArrayList<Object> quantities, ArrayList<String> terms, int i, String operator) {
		Object operand1 = terms.get(terms.size() - 1);
		Object operand2 = quantities.get(i + 1);
		String operand1Type = operand1.getClass().getSimpleName();
		String operand2Type = operand2.getClass().getSimpleName();
		double num1; // evaluated operand1
		double num2; // evaluated operand2
		int prevTerms = 1; // no. of previous terms to be removed once the operation is done
		int skipTerms = 1; // no. of next terms to be skipped because it is already evaluated
		
		// evaluating the operands itself first before evaluating the expression
		if (operand1Type.equals("ArrayList")) {
			prevTerms = ((ArrayList<Object>) operand1).size();
			num1 = solveAS(solveEMD((ArrayList<Object>) operand1));
		} else {
			num1 = Double.valueOf((String) operand1);
		}
		if (operand2Type.equals("ArrayList")) {
			num2 = solveAS(solveEMD((ArrayList<Object>) operand2));
		} else {
			String nextTerm = (String) operand2;
			if (nextTerm.equals(ADD)) {
				num2 = Double.valueOf((String) quantities.get(i + 2));
				skipTerms++;
			} else if (nextTerm.equals(SUB)) {
				num2 = -Double.valueOf((String) quantities.get(i + 2));
				skipTerms++;
			} else {
				num2 = Double.valueOf(nextTerm);
			}
		}
		
		// remove the previous evaluated operand from the terms array
		int termsSize = terms.size();
		for (int j = termsSize - 1; j >= termsSize - prevTerms; j--) {
			terms.remove(j);
		}
		
		// add the evaluated expression to the terms array
		switch (operator) {
			case POW:
				terms.add(String.valueOf(Math.pow(num1, num2)));
				break;
			case MUL:
				terms.add(String.valueOf(num1 * num2));
				break;
			case DIV:
				terms.add(String.valueOf(num1 / num2));
				break;
			case MOD:
				terms.add(String.valueOf(num1 % num2));
		}
		
		return skipTerms;
	}
	
	// AS in PEMDAS
	double solveAS(ArrayList<String> terms) {
		double result = 0;
		for (int i = 0; i < terms.size(); i++) {
			if (terms.size() == 1) {
				result = Double.valueOf(terms.get(i)); // ignore evaluation if the expression has only one term
			} else if (terms.get(i).equals(ADD)) {
				result += evaluateAS(terms, i, ADD);
			} else if (terms.get(i).equals(SUB)) {
				result += evaluateAS(terms, i, SUB);
			}
		}
		return result;
	}
	double evaluateAS(ArrayList<String> terms, int i, String operator) {
		double operand1;
		double operand2;
		double result = 0;
		
		if (i > 0) { // ensure that 'i' is not the first element of 'terms'
			operand1 = Double.valueOf(terms.get(i - 1));
		} else {
			operand1 = 0;
		}
		if (i < terms.size() - 1) { // ensure that 'i' is not the last element of 'terms'
			String nextTerm = terms.get(i + 1);
			if (DIGITS.contains(Character.toString(nextTerm.charAt(0)))) {
				operand2 = Double.valueOf(nextTerm);
			} else {
				// if the 'nextTerm' is not a digit then it must also be an operator
				if (operator.equals(ADD) && nextTerm.equals(SUB)
					|| // if two operators are +- or -+ then its subtraction else it's addition
					operator.equals(SUB) && nextTerm.equals(ADD)) {
					operator = SUB;
				} else {
					operator = ADD;
				}
				// set the 'operand2' to the next next term instead
				operand2 = Double.valueOf(terms.get(i + 2));
			}
		} else {
			operand2 = 0;
		}
		
		if (operand2 != 0) {
			terms.set(i + 1, "0"); // set the next term to zero so that it will not be added again to the 'result' when another operator operates upon it
		}
		
		switch (operator) {
			case ADD:
				result = operand1 + operand2;
				break;
			case SUB:
				result = operand1 - operand2;
		}
		
		return result;
	}
	
	double calculate(String input) {
		String validatedInput = validate(input);
		ArrayList<String> tokens = tokenize(validatedInput);
		ArrayList<Object> quantities = quantify(tokens, 0).quantities;
		ArrayList<String> terms = solveEMD(quantities);
		double answer = solveAS(terms);
		return answer;
	}
}
