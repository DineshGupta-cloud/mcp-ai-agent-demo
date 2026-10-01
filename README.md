# MCP AI Agent Demo — MySQL Database-Backed Enterprise Tools

A complete, production-style runnable application demonstrating:

```text
React (Vite) ──[REST]──> Spring Boot AI Agent (ChatClient) ──[MCP Streamable HTTP]──> Spring Boot MCP Server ──[Spring Data JPA]──> MySQL Database
```

---

## 1. Architecture Overview

```text
┌───────────────────────────────────────────────┐
│              React + Vite Client              │
│              http://localhost:5173            │
└───────────────────────┬───────────────────────┘
                        │
                        │ HTTP POST /api/agent/chat (JSON)
                        ▼
┌───────────────────────────────────────────────┐
│            Spring Boot AI Agent               │
│            http://localhost:8081              │
│                                               │
│   • OpenRouter / OpenAI ChatClient            │
│   • SyncMcpToolCallbackProvider               │
│   • System Prompt & CORS Configuration        │
└───────────────────────┬───────────────────────┘
                        │
                        │ Streamable-HTTP Protocol (/mcp)
                        ▼
┌───────────────────────────────────────────────┐
│            Spring Boot MCP Server             │
│            http://localhost:8080              │
│                                               │
│   • CalculatorTools (@McpTool)                │
│   • EmployeeTools (@McpTool)                  │
│   • EmployeeService & EmployeeRepository      │
└───────────────────────┬───────────────────────┘
                        │
                        │ JDBC (Hibernate / JPA)
                        ▼
┌───────────────────────────────────────────────┐
│               MySQL Database                  │
│               mcp_demo.employees              │
└───────────────────────────────────────────────┘
```

### Key Architectural Concepts
1. **Model Context Protocol (MCP)**: An open standard enabling AI models to safely interact with tools and database backends without vendor lock-in.
2. **MCP Server**: Hosts strongly-typed business tools (`@McpTool` and `@McpToolParam`). Queries MySQL using Spring Data JPA and formats results for the LLM.
3. **MCP Client**: Connects via Streamable HTTP (`STREAMABLE`), discovers tools from the MCP server, and presents them dynamically to Spring AI's `ChatClient`.
4. **AI Agent**: Dispatches user questions to OpenRouter (using free models such as `openrouter/free` or `deepseek/deepseek-chat:free`), invokes MCP tools when needed, and produces conversational answers.
5. **MySQL Data Store**: Replaces the initial in-memory mock with persistent table storage (`mcp_demo.employees`).

---

## 2. Directory Structure

```text
mcp-ai-agent-demo/
│
├── README.md
│
├── mcp-server/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/example/mcpserver/
│       │   │   ├── McpServerApplication.java
│       │   │   ├── model/
│       │   │   │   └── Employee.java                 (JPA Entity)
│       │   │   ├── repository/
│       │   │   │   └── EmployeeRepository.java       (Spring Data JPA)
│       │   │   ├── service/
│       │   │   │   └── EmployeeService.java          (Transaction Service)
│       │   │   └── tools/
│       │   │       ├── CalculatorTools.java          (MCP Math Tools)
│       │   │       └── EmployeeTools.java            (MCP Database Tools)
│       │   └── resources/
│       │       └── application.properties            (MySQL & MCP Configuration)
│       └── test/
│           ├── java/com/example/mcpserver/
│           │   └── McpServerTests.java               (Integration & Unit Tests)
│           └── resources/
│               └── application.properties            (H2 MySQL-mode Test DB)
│
├── ai-agent/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/example/aiagent/
│       │   │   ├── AiAgentApplication.java
│       │   │   ├── config/
│       │   │   │   ├── ChatConfig.java
│       │   │   │   └── WebCorsConfig.java
│       │   │   ├── controller/
│       │   │   │   └── AgentController.java
│       │   │   └── dto/
│       │   │       ├── ChatRequest.java
│       │   │       └── ChatResponse.java
│       │   └── resources/
│       │       └── application.properties            (OpenRouter & MCP Client Config)
│       └── test/
│           └── java/com/example/aiagent/
│               └── AgentControllerTests.java
│
└── react-client/
    ├── package.json
    ├── vite.config.js
    ├── index.html
    ├── .env
    └── src/
        ├── main.jsx
        ├── App.jsx
        ├── index.css
        ├── services/
        │   └── api.js
        ├── components/
        │   ├── ChatWindow.jsx
        │   ├── ChatInput.jsx
        │   └── Message.jsx
        └── test/
            ├── setup.js
            └── App.test.jsx
```

---

## 3. Database Setup (MySQL)

### 3.1 Start MySQL Service
Ensure MySQL 8.0+ is running on your machine:

**Windows PowerShell:**
```powershell
Start-Service MySQL80
# Or check status:
Get-Service MySQL80
```

**Linux / macOS:**
```bash
sudo systemctl start mysql
# or: brew services start mysql
```

### 3.2 Create Database & Table
Run the following SQL commands in your MySQL client (`mysql -u root -p`):

```sql
CREATE DATABASE IF NOT EXISTS mcp_demo;
USE mcp_demo;

CREATE TABLE IF NOT EXISTS employees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150),
    department VARCHAR(100),
    designation VARCHAR(100),
    salary DECIMAL(12,2),
    active BOOLEAN DEFAULT TRUE
);
```

### 3.3 Insert Sample Employee Data
```sql
INSERT INTO employees (name, email, department, designation, salary, active) VALUES
('Rahul Sharma', 'rahul.sharma@example.com', 'IT', 'Senior Developer', 95000.00, true),
('Priya Patel', 'priya.patel@example.com', 'IT', 'Cloud Architect', 135000.00, true),
('Amit Kumar', 'amit.kumar@example.com', 'IT', 'DevOps Engineer', 88000.00, true),
('Neha Gupta', 'neha.gupta@example.com', 'HR', 'HR Manager', 78000.00, true),
('Vikram Singh', 'vikram.singh@example.com', 'Finance', 'Financial Analyst', 82000.00, true),
('Ananya Roy', 'ananya.roy@example.com', 'Engineering', 'Lead Java Developer', 115000.00, true),
('Rohit Verma', 'rohit.verma@example.com', 'Sales', 'Account Executive', 72000.00, true),
('Inactive John', 'john.inactive@example.com', 'IT', 'Junior Developer', 50000.00, false);
```

---

## 4. MCP Tools Reference

### Calculator Tools (`CalculatorTools.java`)
* `add_numbers(a, b)`: Adds two numbers.
* `subtract_numbers(a, b)`: Subtracts `b` from `a`.
* `multiply_numbers(a, b)`: Multiplies two numbers.
* `divide_numbers(a, b)`: Divides `a` by `b` with divide-by-zero protection.

### Database Employee Tools (`EmployeeTools.java`)
* `get_employee(id)`: Returns full details for an employee by ID.
* `list_employees()`: Returns all active employees from MySQL.
* `search_employees(name, department, designation)`: Searches active employees with all parameters optional.
* `count_employees(department)`: Counts active employees overall or by specific department.
* `employees_by_department(department)`: Returns all active employees in the specified department.

---

## 5. Startup Instructions

### Step 1: Start MCP Server (`http://localhost:8080`)

Configure your MySQL credentials via environment variables:

**Windows PowerShell:**
```powershell
cd "e:\Virtual App\MCPSERVER\mcp-ai-agent-demo\mcp-server"
$env:JAVA_HOME="C:\Program Files\Java\jdk-21.0.11"
$env:DB_URL="jdbc:mysql://localhost:3306/mcp_demo?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_mysql_password"

& "C:\Users\dinesh.gupta\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run
```

**Linux / macOS:**
```bash
cd mcp-server
export DB_URL="jdbc:mysql://localhost:3306/mcp_demo"
export DB_USERNAME="root"
export DB_PASSWORD="your_mysql_password"

mvn spring-boot:run
```

Verify health:
```bash
curl http://localhost:8080/actuator/health
```

---

### Step 2: Start AI Agent (`http://localhost:8081`)

Get a free API key at [openrouter.ai/keys](https://openrouter.ai/keys).

**Windows PowerShell:**
```powershell
cd "e:\Virtual App\MCPSERVER\mcp-ai-agent-demo\ai-agent"
$env:JAVA_HOME="C:\Program Files\Java\jdk-21.0.11"
$env:OPENROUTER_API_KEY="sk-or-v1-YOUR_KEY"

# Optional: override model if desired (default is openrouter/free)
# $env:OPENROUTER_MODEL="deepseek/deepseek-chat:free"

& "C:\Users\dinesh.gupta\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run
```

**Linux / macOS:**
```bash
cd ai-agent
export OPENROUTER_API_KEY="sk-or-v1-YOUR_KEY"
mvn spring-boot:run
```

Verify health:
```bash
curl http://localhost:8081/actuator/health
```

---

### Step 3: Start React Client (`http://localhost:5173`)

```powershell
cd "e:\Virtual App\MCPSERVER\mcp-ai-agent-demo\react-client"
npm run dev
```

Open browser at: `http://localhost:5173`

---

## 6. Example Questions to Ask in React

Test these natural language prompts in the chat interface:

1. `"Get employee 1"`
   - *Calls `get_employee(id=1)`*
2. `"Show all employees"`
   - *Calls `list_employees()`*
3. `"Find employees in IT"`
   - *Calls `employees_by_department(department="IT")` or `search_employees`*
4. `"How many employees are in IT?"`
   - *Calls `count_employees(department="IT")`*
5. `"Show all developers"`
   - *Calls `search_employees(designation="Developer")`*
6. `"Group employees by department"`
   - *Calls `list_employees()` and groups them dynamically*
7. `"Who has the highest salary?"`
   - *Calls `list_employees()` and inspects salaries from MySQL*
8. `"What is 125 * 8?"`
   - *Calls `multiply_numbers(a=125, b=8)` (Calculator tool preserved)*

---

## 7. Running Automated Test Suites

```bash
# MCP Server Tests (Repository, Service, Calculator, MCP Tools)
cd mcp-server
mvn test

# AI Agent Tests (Controller, Validation, Error Handling)
cd ai-agent
mvn test

# React Client Tests (Vitest & Testing Library)
cd react-client
npm test
```
