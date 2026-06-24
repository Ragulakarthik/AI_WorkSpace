# Neo4j Learning Notes

My personal notes as I learn Neo4j — written in simple words.

---

## What is Neo4j?

Neo4j is a **graph database**. Instead of storing data in tables (like MySQL), it stores data as a graph.

From DSA, we already know what a graph is:
- **Nodes** (also called vertices) — the things/entities
- **Edges** — the connections between those things
- Edges can be **directed** (one-way) or **undirected** (both ways)
- Graphs can be **cyclic**, **acyclic (DAG)**, **connected**, or **disconnected**
- We can traverse graphs using **DFS** and **BFS**

Neo4j takes that exact same idea and makes it a **persistent database** — so instead of building a graph in memory for one algorithm run, you store your data as a graph permanently and query it.

---

## Core Concepts

### Nodes, Labels and Properties

A **node** in Neo4j is like a JSON object or a Java class instance:
- The **label** is like the class name — tells you what type of thing it is (e.g. `Person`, `Movie`)
- The **properties** are the key-value pairs — the actual data about that thing

```
(:Person {name: "Karthik", age: 25})
(:Movie {title: "Inception", year: 2010})
```

Just like a Java object:
```java
Person person = new Person();
person.name = "Karthik";
person.age = 25;
```

---

### Relationships

A **relationship** connects two nodes. It always has:
- A **direction** — one node points to another (like a directed edge in DSA)
- A **type** written in ALL_CAPS — describes what the connection means
- Optional **properties** — extra info about the connection (like edge weight in DSA)

```
(Karthik)-[:FOLLOWS]->(Chakri)         // Instagram: Karthik follows Chakri
(Karthik)-[:FRIENDS_WITH]->(Lokesh)    // Facebook: Karthik is friends with Lokesh
(Karthik)-[:WORKS_AT {since: 2023}]->(WaveMaker)  // relationship with a property
```

Real-world use cases:
- **Social networks** — who follows who, who is friends with who
- **Recommendation engines** — User A liked Movie X, Movie X is similar to Movie Y → recommend Y to A
- **Org charts** — Employee reports to Manager, Manager belongs to Department

---

### Why Graph DB over SQL?

In SQL, relationships between data = **JOINs**. The more tables and relationships you have, the more JOINs you need, and the **slower** your queries get.

In Neo4j:
- Relationships are stored **directly** alongside the data
- No JOINs needed — you just **follow the connection**
- Much faster when your data is highly connected (social networks, recommendation engines, org charts, etc.)
- You can also **visualize** the graph and see how things are connected — makes it easier to understand complex relationships

---

## Cypher Query Language

Cypher is Neo4j's query language — like SQL but for graphs. The syntax visually looks like the graph itself using arrows and parentheses.

### Basic Commands

| What you want to do | Cypher | SQL equivalent |
|---|---|---|
| Find data | `MATCH` | `SELECT` |
| Add data | `CREATE` | `INSERT` |
| Remove data | `DELETE` | `DELETE` |
| Show results | `RETURN` | (part of SELECT) |

### Examples

**Create a node:**
```cypher
CREATE (:Person {name: "Karthik", age: 25})
```

**Find a node:**
```cypher
MATCH (p:Person {name: "Karthik"}) RETURN p
```

**Create two nodes and a relationship:**
```cypher
CREATE (k:Person {name: "Karthik"})-[:FOLLOWS]->(c:Person {name: "Chakri"})
```

**Find who Karthik follows:**
```cypher
MATCH (k:Person {name: "Karthik"})-[:FOLLOWS]->(other) RETURN other
```

**Delete a node:**
```cypher
MATCH (p:Person {name: "Karthik"}) DELETE p
```

### SET — Update properties

Like `UPDATE` in SQL — find a node and change its properties.

```cypher
MATCH (p:Person {email: "karthik@gmail.com"})
SET p.age = 26
RETURN p
```

Multiple properties at once:
```cypher
SET p.age = 26, p.name = "Karthik R"
```

---

### MERGE — Create only if not exists

`CREATE` always creates a new node — even if one already exists with the same data. This causes duplicates.

`MERGE` = check first, create only if missing. Like upsert.

**Real example — user signup/signin:**
- If you use `CREATE` every time a user logs in → you get duplicate Person nodes for the same user
- Use `MERGE` instead → finds the existing user if they exist, creates them only if they don't

```cypher
MERGE (p:Person {email: "karthik@gmail.com"})
RETURN p
```

This is safe to call on both signup and signin — no duplicates ever created.

### Chaining Relationships — Friends of Friends

In SQL, "friends of friends" needs multiple JOINs and gets messy fast.

In Cypher, you just **chain the pattern** — it reads exactly like plain English:

```cypher
// Who does Chakri follow, that Karthik also follows?
MATCH (k:Person {name: "Karthik"})-[:FOLLOWS]->(chakri)-[:FOLLOWS]->(other)
RETURN other
```

This is one of the biggest strengths of Neo4j — deep relationship queries are simple and fast, no matter how many levels deep you go.

### Filtering, Sorting, Limiting

Cypher has the same helpers as SQL:

| What you want | Cypher | SQL equivalent |
|---|---|---|
| Filter results | `WHERE` | `WHERE` |
| Sort results | `ORDER BY` | `ORDER BY` |
| Limit results | `LIMIT` | `LIMIT` |
| Count results | `COUNT()` | `COUNT()` |

**Real example — last 10 ATM transactions for a user:**
```cypher
MATCH (u:User {name: "Karthik"})-[:MADE]->(t:Transaction)
RETURN t
ORDER BY t.date DESC
LIMIT 10
```

- `ORDER BY t.date DESC` — most recent first
- `LIMIT 10` — only return 10 results

---

## POC — Code Dependency Graph for AIRA

### Problem
AIRA is an agentic AI platform that generates web and mobile apps. When the LLM changes something (a Variable, API, AppVariable), it doesn't know what else in the app is affected. Without this, the LLM makes blind changes and breaks things.

### Solution
Store the AIRA app structure as a graph in Neo4j. When something changes → call one API → get back everything impacted with the full path showing how it's connected.

### Graph Model

**Nodes:** `App`, `Page`, `Variable`, `AppVariable`, `API`

**Relationships:**
```
App       -[:HAS_APP_VARIABLE]-> AppVariable
Page      -[:BELONGS_TO]->       App
Page      -[:HAS_VARIABLE]->     Variable
Page      -[:USES]->             AppVariable
Variable  -[:CONNECTED_TO]->     API
```

**Two types of variables:**
- **Variable** — belongs to one Page only. If it changes, only that Page is impacted.
- **AppVariable** — global, belongs to the App. If it changes, ALL Pages in that App are impacted automatically (because every Page `BELONGS_TO` the App).

### 3 APIs Built

| API | What it does |
|---|---|
| `POST /nodes` | Add a node — body: `{"name": "LoginPage", "type": "Page"}` |
| `POST /relationships` | Connect two nodes — body: `{"fromName": "LoginPage", "fromType": "Page", "toName": "MyApp", "toType": "App", "relationshipType": "BELONGS_TO"}` |
| `GET /impacted/{name}?type=Variable` | Return all impacted nodes with full path |

### The Key Cypher Query

```cypher
MATCH p=(impacted)-[*]->(n:Variable {name: $name})
RETURN DISTINCT labels(impacted) AS type, impacted.name AS name,
       [r IN relationships(p) | type(r)] AS relationships,
       [node IN nodes(p) | node.name] AS path
```

- `[*]` — traverse any depth (no limit), so adding deeper nodes in future won't break it
- Traverses **incoming** direction — finds everything that depends on the changed node
- Returns the full path so the LLM knows exactly how each node is connected

### Example Response

`GET /impacted/globalTheme?type=AppVariable`

```json
[
  {"name": "LoginPage",     "type": ["Page"], "path": "LoginPage -[USES]-> globalTheme"},
  {"name": "DashboardPage", "type": ["Page"], "path": "DashboardPage -[USES]-> globalTheme"},
  {"name": "MyApp",         "type": ["App"],  "path": "MyApp -[HAS_APP_VARIABLE]-> globalTheme"},
  {"name": "LoginPage",     "type": ["Page"], "path": "LoginPage -[BELONGS_TO]-> MyApp -[HAS_APP_VARIABLE]-> globalTheme"},
  {"name": "DashboardPage", "type": ["Page"], "path": "DashboardPage -[BELONGS_TO]-> MyApp -[HAS_APP_VARIABLE]-> globalTheme"}
]
```

The LLM receives this and immediately knows which Pages to review without scanning the entire app.

### Tech Stack
- Spring Boot + Maven
- Spring Data Neo4j (`@Node`, `@Relationship`, `Neo4jRepository`)
- Neo4j 5 via Docker
- Raw `Driver` for the impact query (mixed return types — can return Page, Variable, API all in one query)

### Setup

**Run Neo4j via Docker:**
```bash
docker run --name neo4j-aira -p 7474:7474 -p 7687:7687 -e NEO4J_AUTH=neo4j/password123 -d neo4j:5
```

**application.properties:**
```properties
spring.neo4j.uri=bolt://localhost:7687
spring.neo4j.authentication.username=neo4j
spring.neo4j.authentication.password=password123
```

### Project Location
`/home/karthikr_700073/Projects/personal/AI_WorkSpace/Neo4j/graph-database`

---

## Indexes

Just like the index at the back of a book — instead of reading every page, you jump directly to what you need.

Without an index → Neo4j scans **every single node** to find a match (slow for large data).
With an index → Neo4j jumps **directly** to the right node (fast).

### When to create an index:
- On properties you **frequently search by** — like `email`, `userId`
- On properties that are **unique** — like a primary key in SQL

### When NOT to index:
- Properties like `name` — too many duplicates, not useful for jumping to exact node
- Every single property — indexes take **extra storage** and **slow down writes** (every CREATE/UPDATE must also update the index)

### How to create an index in Cypher:
```cypher
CREATE INDEX FOR (p:Person) ON (p.email)
```

### Unique constraint (index + uniqueness check):
```cypher
CREATE CONSTRAINT FOR (p:Person) REQUIRE p.email IS UNIQUE
```
This ensures no two Person nodes can have the same email — like a primary key in SQL.

---

## ACID in Neo4j

Neo4j is fully ACID compliant — same guarantees as a relational database.

| Property | Meaning |
|---|---|
| **Atomicity** | A transaction is all or nothing — either the whole thing completes or nothing happens |
| **Consistency** | Data is always in a valid state before and after a transaction |
| **Isolation** | Two transactions happening at the same time don't interfere with each other |
| **Durability** | Once a transaction is committed, it is saved permanently — even if the system crashes |

**Example:** If you're transferring money — debit one account and credit another. If the credit fails, the debit is also rolled back. That's Atomicity.

---

## Java / Spring Boot Integration

Using Spring Boot for POC — it handles all the connection setup automatically, less boilerplate.

### 1. Add dependency (pom.xml)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-neo4j</artifactId>
</dependency>
```

### 2. Configure connection (application.properties)
```properties
spring.neo4j.uri=bolt://localhost:7687
spring.neo4j.authentication.username=neo4j
spring.neo4j.authentication.password=your_password
```
Keep credentials in `.env` — never hardcode them in the code.

### 3. Define a Node as a Java class
```java
@Node
public class Person {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String email;
}
```
- `@Node` — this class maps to a Neo4j node label
- `@Id` — unique identifier, like a primary key in SQL

### 4. Define a Repository to query
```java
public interface PersonRepository extends Neo4jRepository<Person, Long> {
    Person findByEmail(String email);
}
```
Spring auto-generates the Cypher query — same pattern as Spring Data JPA for SQL.

### 5. Map Relationships between nodes

Like `@OneToMany` in JPA — use `@Relationship` to connect two node classes:

```java
@Node
public class Person {
    @Id @GeneratedValue
    private Long id;
    private String name;

    @Relationship(type = "LIKES", direction = Direction.OUTGOING)
    private List<Movie> likedMovies;

    @Relationship(type = "FOLLOWS", direction = Direction.OUTGOING)
    private List<Person> following;
}
```

- `type` — the relationship name in Neo4j (matches what you'd write in Cypher)
- `OUTGOING` — this node points to the other (arrow goes out)
- `INCOMING` — the other node points to this one (arrow comes in)

---

## When to Use Neo4j vs SQL

### Use Neo4j when your data has lots of relationships:
- Social networks (Instagram, Facebook) — who follows who, who is friends with who
- Maps & navigation — roads, locations, routes
- Banking & fraud detection — money flowing between accounts
- Recommendation engines — users, products, likes, purchases
- Org charts — employees, managers, departments

### Use SQL when your data is simple and flat (not many connections):
- Product inventory — just a list of items with price and stock
- Payroll system — employee name, salary, bank account
- Logs & reports — rows of events with timestamps, queried by date not by connection

**Rule of thumb:** If your main queries are about **how things are connected**, use Neo4j. If your main queries are about **filtering rows of data**, use SQL.

---

## Useful Links

> Links to docs, tutorials, or resources I find helpful.
