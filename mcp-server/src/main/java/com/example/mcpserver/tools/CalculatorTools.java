package com.example.mcpserver.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class CalculatorTools {

    private static final Logger log = LoggerFactory.getLogger(CalculatorTools.class);

    @McpTool(name = "add_numbers", description = "Add two numbers and return the sum")
    public double addNumbers(
            @McpToolParam(description = "First number", required = true) double a,
            @McpToolParam(description = "Second number", required = true) double b) {
        log.info("MCP Tool called: add_numbers(a={}, b={})", a, b);
        return a + b;
    }

    @McpTool(name = "subtract_numbers", description = "Subtract the second number from the first number (a - b)")
    public double subtractNumbers(
            @McpToolParam(description = "First number (minuend)", required = true) double a,
            @McpToolParam(description = "Second number (subtrahend)", required = true) double b) {
        log.info("MCP Tool called: subtract_numbers(a={}, b={})", a, b);
        return a - b;
    }

    @McpTool(name = "multiply_numbers", description = "Multiply two numbers and return the product")
    public double multiplyNumbers(
            @McpToolParam(description = "First number", required = true) double a,
            @McpToolParam(description = "Second number", required = true) double b) {
        log.info("MCP Tool called: multiply_numbers(a={}, b={})", a, b);
        return a * b;
    }

    @McpTool(name = "divide_numbers", description = "Divide the first number by the second number (a / b). Returns an error message if division by zero is attempted.")
    public String divideNumbers(
            @McpToolParam(description = "Numerator", required = true) double a,
            @McpToolParam(description = "Denominator", required = true) double b) {
        log.info("MCP Tool called: divide_numbers(a={}, b={})", a, b);
        if (b == 0.0) {
            log.warn("Division by zero attempted for a={}", a);
            return "Error: Cannot divide by zero";
        }
        return String.valueOf(a / b);
    }
}
