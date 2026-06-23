# Agent Server Protocols

An agent server can use 3 protocols: **MCP**, **A2A**, and **AgUI**.

---

## 1. MCP (Model Context Protocol)

MCP is a client-server protocol that provides external context to AI models.

### Architecture
- **MCP Client** — The application (e.g., Claude Desktop) that sends prompts
- **MCP Server** — Plugins/tools that provide context (e.g., GitHub, Jira, Filesystem)

### How it works
1. User sends a prompt to the MCP client
2. Client sends the prompt + list of available MCP tools to the model (e.g., Sonnet)
3. The **MODEL** decides which MCP server to call based on the user's request
4. Client executes the tool call and returns the result to the model
5. Model uses the context from the MCP server to generate the final response

### Example
- User asks "Summarize my repositories"
- Model decides to call the GitHub MCP server
- GitHub MCP returns all repos as context
- Model summarizes and responds

> **Key point:** It is the **MODEL** that decides which MCP server to invoke, not the client.
> The client just executes what the model requests and returns the result.

---

## 2. A2A (Agent to Agent Protocol)

A2A is Google's protocol for agent-to-agent communication over HTTP/JSON-RPC.

### Example Agent Setup
| Agent | Role |
|-------|------|
| Orchestrator Agent | Default entry point, has full capability awareness |
| UI Agent | Handles frontend/UI tasks |
| Backend Agent | Handles backend/service tasks |

### How delegation works
- User sends a task to the orchestrator agent
- Orchestrator decides which sub-agent to delegate to based on the task
- If tasks are **INDEPENDENT** → sub-agents are called in **PARALLEL**
- If tasks are **DEPENDENT** → sub-agents are called **SEQUENTIALLY**

**Example of sequential dependency:**
> "Create a button and bind it to a service"
> - UI agent must create the button **FIRST**
> - Only then can Backend agent bind it to a service (it needs the button to exist)

### Context passing (orchestrator → sub-agent)
- Only a **TASK STRING** is passed, not full conversation history
- Sub-agent starts with a clean slate:
  ```python
  { "messages": [HumanMessage(content=task)] }
  ```
- The orchestrator summarizes what it needs in the task string
- In **continue mode**, sub-agent retains its own prior history via LangGraph checkpoint

### A2A message format — 3 tiers of context

**Tier 1 — Always passed (every request) via `metaParams`:**
- Identity/auth fields such as `auth_cookie`, `projectId`, `userEmail`, `env`, etc.
- These are identity/auth fields only, not project content

**Tier 2 — Fetched once (first turn of a session):**
- `AGENTS.md` — project-level instructions, injected as `<AGENTS_MD>` tag into first human message
- Skills catalog — names + descriptions only, injected as `<PROJECT_SKILLS>` tag
- Both are **SHARED** across all agents in the same project (no per-agent variant)
- `get_agent_resources` takes only `project_id`, not `agent_id`

**Tier 3 — Fetched lazily (on demand):**
- Skill bodies — fetched only when agent invokes `load_skill`, not upfront
- Design rationale: avoid sending heavy content unnecessarily

### A2A Task lifecycle states
```
submitted → working → completed / failed / cancelled
```

### Agent Card
- Each agent publishes an **agent card** (JSON) describing its capabilities
- The orchestrator uses this to know which agent to delegate to

### Important — A2A is only for EXTERNAL communication
| Communication | Protocol |
|---------------|----------|
| External caller → Orchestrator | A2A (HTTP/JSON-RPC) |
| Orchestrator → UI/Backend Agent | Direct LangGraph graph invocation (in-process) |

Internal orchestrator-to-subagent calls are **NOT** over A2A — they are direct in-process calls.

---

## 3. AgUI (Agent User Interaction Protocol)

AgUI is the protocol between the agent server and the frontend UI.
It standardizes real-time streaming communication so the user sees responses as they are generated.

### Server
- The agent server **IS** the AgUI server
- Exposes a WebSocket endpoint at `/v1/agui/run`
- The frontend connects to this WebSocket and receives streaming events

### Event types
| Event | Description |
|-------|-------------|
| `RUN_STARTED`, `RUN_FINISHED` | Lifecycle events |
| `TEXT_MESSAGE_START` | Agent starts generating text |
| `TEXT_MESSAGE_DELTA` | Streaming text chunk |
| `TEXT_MESSAGE_END` | Agent finishes text |
| `TOOL_CALL_START`, `TOOL_CALL_END` | Visibility into tool/MCP calls |
| `STATE_DELTA` | Shared state updates between agent and UI |

### Which agents use AgUI
- **ONLY** the orchestrator/entry-point agent talks to the UI directly over AgUI
- Sub-agents (UI Agent, Backend Agent) never directly serve AgUI
- Sub-agent activity (tool calls, text chunks) bubbles up through the same `AGUIStreamingHandler` that was established at the orchestrator level
- The user sees **ALL** activity (orchestrator + sub-agents) through **ONE single WebSocket connection**

```python
agent_id = run_input.agent_id or await get_default_entry_point_agent_id()
```

---

## Summary — How all 3 protocols work together

```
External caller
    ↓  (A2A over HTTP/JSON-RPC)
Orchestrator Agent
    ↓  (AgUI over WebSocket → streams events to the frontend)
    ↓  (Direct in-process LangGraph calls → UI Agent / Backend Agent)
        ↓  (MCP → GitHub, Jira, Filesystem, etc. for external context)
```
