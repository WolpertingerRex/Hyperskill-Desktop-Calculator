package calculator;

import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;


public class Calculator extends JFrame {
    private final int WIDTH = 300;
    private final int HEIGHT = 500;
    private final int ROWS = 9;
    private final int COLUMNS = 4;
    JPanel panel = new JPanel();
    GridBagLayout layout = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    private final JLabel equationLabel = new JLabel();
    private final JLabel resultLabel = new JLabel("0");

    private final List<String> buttonValues = List.of(
            "()", "CE", "C", "Del",
            "x\u00B2", "x\u02B8", "\u221A", "\u00F7",
            "7", "8", "9", "\u00D7",
            "4", "5", "6", "-",
            "1", "2", "3", "+",
            "\u00B1", "0", ".", "=");
    private final List<String> buttonNames = List.of(
            "Parentheses", "CE", "Clear", "Delete",
            "PowerTwo", "PowerY", "SquareRoot", "Divide",
            "Seven", "Eight", "Nine", "Multiply",
            "Four", "Five", "Six", "Subtract",
            "One", "Two", "Three", "Add",
            "PlusMinus", "Zero", "Dot", "Equals");


    private final List<JButton> buttons = new ArrayList<>();

    public Calculator() {
        super("Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setResizable(false);
        panel.setBackground(Color.LIGHT_GRAY.brighter());
        panel.setLayout(layout);
        addComponents();
        init();
        setVisible(true);
    }

    private void addComponents() {

        equationLabel.setName("EquationLabel");
        equationLabel.setFont(new Font("SansSerif", Font.ITALIC, 15));
        equationLabel.setPreferredSize(new Dimension(100, 50));
        equationLabel.setOpaque(true);
        equationLabel.setForeground(Color.blue);
        equationLabel.setBackground(Color.white);

        Border border = BorderFactory.createLineBorder(Color.BLUE, 5);
        resultLabel.setBorder(border);
        resultLabel.setName("ResultLabel");
        resultLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        resultLabel.setOpaque(true);

        //add buttons
        for (int i = 0; i < buttonValues.size(); i++) {
            JButton b = new JButton(buttonValues.get(i));
            b.setName(buttonNames.get(i));
            b.setFont(new Font("SansSerif", Font.BOLD, 20));
            if (i < 8 || (i + 1) % 4 == 0) b.setBackground(Color.decode("#B5C1E4"));

            buttons.add(b);
        }

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridwidth = 4;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(resultLabel, gbc);
        gbc.gridy = 1;
        panel.add(equationLabel, gbc);

        gbc.gridwidth = 1;

        int buttonIndex = 0;
        for (int y = 3; y < ROWS; y++) {
            for (int x = 0; x < COLUMNS; x++) {
                gbc.gridx = x;
                gbc.gridy = y;
                if (buttonIndex < buttons.size()) {
                    panel.add(buttons.get(buttonIndex), gbc);
                    buttonIndex++;
                }
            }
        }

        this.getContentPane().add(panel, BorderLayout.CENTER);
    }

    private void init() {
        class Listener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {

                String command = e.getActionCommand();
                switch (command) {
                    case "=": {
                        String input = equationLabel.getText();
                        if (!input.isEmpty()) {
                            String verified = verify(input, command);
                            process(verified);
                        }
                    }
                    break;
                    case "C":
                        equationLabel.setText("");
                        resultLabel.setText("0");
                        break;
                    case "CE":
                        resultLabel.setText("0");
                        equationLabel.setText(removeLastNumber(equationLabel.getText()));
                        break;

                    case "Del":
                        String temp = equationLabel.getText();
                        equationLabel.setText(temp.substring(0, temp.length() - 1));
                        break;

                    case "()":
                        equationLabel.setText(processParentheses(equationLabel.getText()));
                        break;

                    case "\u221A":
                        equationLabel.setText(equationLabel.getText() + command + "(");
                        break;
                    case "x\u00B2":
                        equationLabel.setText(equationLabel.getText() + "^(2)");
                        break;
                    case "x\u02B8":
                        equationLabel.setText(equationLabel.getText() + "^(");
                        break;
                    case "\u00B1":
                        equationLabel.setText(processNegation(equationLabel.getText()));
                        break;

                    default:
                        String verified = verify(equationLabel.getText(), command);
                        equationLabel.setText(verified);
                        break;
                }
            }
        }
        Listener listener = new Listener();
        buttons.forEach(b -> b.addActionListener(listener));
    }

    private String removeLastNumber(String input) {
        Pattern pattern = Pattern.compile("(.+)*(\\d+)$");
        Matcher matcher = pattern.matcher(input);
        String expression = input;
        if (matcher.find()) {
            expression = matcher.group(1);
        }
        return expression;
    }

    private String processNegation(String input) {

        if (input.length() > 1) {
            String last = input.substring(input.length() - 2);
            if (last.equals("(-")) return input.substring(0, input.length() - 2);
        }

        Pattern patternUndo = Pattern.compile("(.+)*\\(-(\\d+)");
        Matcher matcherUndo = patternUndo.matcher(input);
        if (matcherUndo.find()) {
            String expression = matcherUndo.group(1);
            String number = matcherUndo.group(2);
            return expression != null ? expression + number : number;
        }

        Pattern pattern = Pattern.compile("(.+)*(\\d+)");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String expression = matcher.group(1);
            String number = matcher.group(2);

            return expression != null ? expression + "(-" + number : "(-" + number;
        }

        return input + "(-";
    }

    private String processParentheses(String input) {
        String signs = "\\+-\u00D7\u00F7";

        int leftCount = (int) Stream.of(input.split("")).filter(s -> s.equals("(")).count();
        int rightCount = (int) Stream.of(input.split("")).filter(s -> s.equals(")")).count();

        if (leftCount == rightCount) return input + "(";

        if (input.length() > 0) {
            char last = input.charAt((input.length() - 1));
            if (last == '(' || signs.contains(String.valueOf(last))) {
                return input + "(";
            }
        }
        return input + ")";
    }


    private String verify(String input, String command) {
        String signs = "\\+-\u00D7\u00F7\u221A";
        String digits = "0123456789";

        if (input.isBlank() && !command.equals("\u221A")) {
            if (signs.contains(command)) return input;
        }

        if (input.length() > 0) {
            char last = input.charAt(input.length() - 1);
            char beforeLast = 's';

            if (input.length() > 1) {
                beforeLast = input.charAt(input.length() - 2);
            }
            if (digits.contains(command)) {
                if (last == '.' && !digits.contains(String.valueOf(beforeLast))) {
                    int index = input.lastIndexOf('.');
                    input = input.substring(0, index) + "0.";
                    return input + command;
                }
            } else if (signs.contains(command) || command.equals("=")) {
                if (last == '.' && digits.contains(String.valueOf(beforeLast))) {
                    input = input + "0";
                    return command.equals("=") ? input : input + command;
                }
            }

        }

        if (input.length() > 0 && signs.contains(input.substring(input.length() - 1))) {
            if (command.equals("=")) {
                equationLabel.setForeground(Color.RED.darker());
                return input;
            } else if (signs.contains(command)) {
                input = input.replace(input.charAt(input.length() - 1), command.charAt(0));
                return input;
            }
        }

        if (input.endsWith("\u00F70")) {
            equationLabel.setForeground(Color.RED.darker());
            return input;
        }

        return input + command;
    }

    private void process(String input) {
        String result = Expression.calculate(input);
        equationLabel.setText(input + result);
        resultLabel.setText(result);
    }
}
