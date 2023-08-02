import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Stack;

@WebServlet("/calc")
public class CalcServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, IOException {
        String expression = request.getParameter("expression");
        Map<String, String[]> params = request.getParameterMap();

        int result = evaluateExpression(expression, params);
        response.getWriter().write(String.valueOf(result));
    }

    private int evaluateExpression(String expression, Map<String, String[]> params) {
        Stack<Integer> operandStack = new Stack<>();
        Stack<Character> operatorStack = new Stack<>();

        int i = 0;
        while (i < expression.length()) {
            char c = expression.charAt(i);

            if (Character.isWhitespace(c)) {
                i++; // Skip whitespace
            } else if (Character.isDigit(c)) {
                // Handle numbers
                int numStart = i;
                while (i < expression.length() && Character.isDigit(expression.charAt(i))) {
                    i++;
                }
                int num = Integer.parseInt(expression.substring(numStart, i));
                operandStack.push(num);
            } else if (Character.isLetter(c)) {
                // Handle variables
                int varStart = i;
                while (i < expression.length() && Character.isLetter(expression.charAt(i))) {
                    i++;
                }
                String variable = expression.substring(varStart, i);
                int value = getValue(variable, params);
                operandStack.push(value);
            } else if (c == '(') {
                operatorStack.push(c);
                i++;
            } else if (c == ')') {
                while (!operatorStack.isEmpty() && operatorStack.peek() != '(') {
                    performOperation(operandStack, operatorStack);
                }
                operatorStack.pop(); // Pop '('
                i++;
            } else if (isOperator(c)) {
                // Handle operators
                while (!operatorStack.isEmpty() && precedence(c) <= precedence(operatorStack.peek())) {
                    performOperation(operandStack, operatorStack);
                }
                operatorStack.push(c);
                i++;
            }
        }

        while (!operatorStack.isEmpty()) {
            performOperation(operandStack, operatorStack);
        }

        return operandStack.pop();
    }

    private int getValue(String variable, Map<String, String[]> params) {
        if (Character.isDigit(variable.charAt(0))) {
            return Integer.parseInt(variable);
        } else {
            String[] values = params.get(variable);
            if (values != null && values.length > 0) {
                return getValue(values[0], params);
            }
            throw new IllegalArgumentException("Variable '" + variable + "' not found or has an invalid value.");
        }
    }

    private void performOperation(Stack<Integer> operandStack, Stack<Character> operatorStack) {
        char operator = operatorStack.pop();
        int operand2 = operandStack.pop();
        int operand1 = operandStack.pop();
        int result = applyOperator(operator, operand1, operand2);
        operandStack.push(result);
    }

    private int applyOperator(char operator, int operand1, int operand2) {
        switch (operator) {
            case '+':
                return operand1 + operand2;
            case '-':
                return operand1 - operand2;
            case '*':
                return operand1 * operand2;
            case '/':
                if (operand2 == 0) {
                    throw new ArithmeticException("Division by zero.");
                }
                return operand1 / operand2;
            default:
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private int precedence(char operator) {
        if (operator == '+' || operator == '-') {
            return 1;
        } else if (operator == '*' || operator == '/') {
            return 2;
        } else {
            return 0;
        }
    }
}
