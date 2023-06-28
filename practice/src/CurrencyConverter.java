import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CurrencyConverter {
    private double rubToDollarRate;

    public CurrencyConverter(double rubToDollarRate) {
        this.rubToDollarRate = rubToDollarRate;
    }

    public double toRubles(double dollars) {
        return dollars * rubToDollarRate;
    }

    public double toDollars(double rubles) {
        return rubles / rubToDollarRate;
    }

    public static void main(String[] args) {
        double rubToDollarRate = readExchangeRateFromFile("config.txt");
        CurrencyConverter converter = new CurrencyConverter(rubToDollarRate);

        Scanner scanner = new Scanner(System.in);

        System.out.println("Введите выражение для конвертации:");
        String input = scanner.nextLine();

        double result = evaluateExpression(input, converter);
        System.out.println("Результат: " + result);

        scanner.close();
    }

    private static double readExchangeRateFromFile(String fileName) {
        double rate = 0.0;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line = reader.readLine();
            rate = Double.parseDouble(line);
        } catch (IOException e) {
            System.out.println("Ошибка чтения файла конфигурации: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Некорректный формат курса в файле конфигурации.");
        }

        return rate;
    }

    private static double evaluateExpression(String input, CurrencyConverter converter) {
        String pattern = "(toDollars|toRubles)\\(([^\\)]+)\\)";
        Pattern expressionPattern = Pattern.compile(pattern);

        while (true) {
            Matcher matcher = expressionPattern.matcher(input);
            if (!matcher.find()) {
                break;
            }

            String operation = matcher.group(1);
            String argument = matcher.group(2);

            double value = evaluateExpression(argument, converter);

            double result = 0.0;

            if (operation.equals("toDollars")) {
                result = converter.toDollars(value);
            } else if (operation.equals("toRubles")) {
                result = converter.toRubles(value);
            }

            input = input.replaceFirst(Pattern.quote(matcher.group()), Double.toString(result));
        }

        double finalResult = evaluateArithmeticExpression(input);

        return finalResult;
    }

    private static double evaluateArithmeticExpression(String input) {
        String cleanedInput = input.replaceAll("\\s+", ""); // Удаление пробелов

        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();

        for (int i = 0; i < cleanedInput.length(); i++) {
            char c = cleanedInput.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                StringBuilder sb = new StringBuilder();
                while (i < cleanedInput.length()
                        && (Character.isDigit(cleanedInput.charAt(i)) || cleanedInput.charAt(i) == '.')) {
                    sb.append(cleanedInput.charAt(i));
                    i++;
                }
                i--;

                double value = Double.parseDouble(sb.toString());
                values.push(value);
            } else if (c == '(') {
                operators.push(c);
            } else if (c == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    double value2 = values.pop();
                    double value1 = values.pop();
                    char operator = operators.pop();

                    double result = performOperation(value1, value2, operator);
                    values.push(result);
                }
                operators.pop(); // Удалить '('
            } else if (c == '+' || c == '-' || c == '*' || c == '/') {
                while (!operators.isEmpty() && hasPrecedence(c, operators.peek())) {
                    double value2 = values.pop();
                    double value1 = values.pop();
                    char operator = operators.pop();

                    double result = performOperation(value1, value2, operator);
                    values.push(result);
                }

                operators.push(c);
            }
        }

        while (!operators.isEmpty()) {
            double value2 = values.pop();
            double value1 = values.pop();
            char operator = operators.pop();

            double result = performOperation(value1, value2, operator);
            values.push(result);
        }

        return values.pop();
    }

    private static double performOperation(double value1, double value2, char operator) {
        switch (operator) {
            case '+':
                return value1 + value2;
            case '-':
                return value1 - value2;
            case '*':
                return value1 * value2;
            case '/':
                return value1 / value2;
            default:
                throw new IllegalArgumentException("Некорректный оператор: " + operator);
        }
    }

    private static boolean hasPrecedence(char operator1, char operator2) {
        if (operator2 == '(' || operator2 == ')') {
            return false;
        }
        return (operator1 == '*' || operator1 == '/') && (operator2 == '+' || operator2 == '-');
    }
}