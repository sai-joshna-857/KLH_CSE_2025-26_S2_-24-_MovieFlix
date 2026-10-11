# MovieFlix - Intelligent Movie Recommendation & Search Engine

MovieFlix is a high-performance, Java-based web application built from scratch to demonstrate the practical application of custom data structures, advanced pattern-matching algorithms, dynamic programming, network flow optimization, greedy techniques, and cryptographic hashing within an interactive entertainment platform.

---

## 🚀 Complete Course Outcome (CO1 – CO6) Algorithmic Modules

The project is structured around rigorous computer science principles categorized across standard course outcomes:

*   **CO1: Linear & Non-Linear Data Structures**
    *   `CustomList<T>`: A generic dynamic array implemented from scratch with automated capacity doubling and index boundary enforcement.
    *   `CustomQueue<T>`: A generic circular FIFO queue using modular arithmetic for graph traversal and algorithm execution.
*   **CO2: Foundational, Sorting & Number-Theoretic Algorithms**
    *   `MillerRabin.java`: Implements probabilistic primality testing using modular exponentiation and bitwise operations.
*   **CO3: Advanced String Matching & Dynamic Programming**
    *   `KMPMatcher.java`: Linear-time $O(N + M)$ pattern search using a Longest Prefix Suffix (LPS) preprocessing array for instant title, actor, and director querying.
    *   `EditDistance.java`: Levenshtein distance dynamic programming matrix calculation for robust fuzzy search matching and typo correction.
    *   `NeedlemanWunsch.java`: Implements global sequence alignment dynamic programming for advanced string similarity scoring.
*   **CO4: Graph Theory & Network Flow Algorithms**
    *   `MaxFlow.java`: Ford-Fulkerson maximum flow algorithm integrated with Breadth-First Search (BFS) path augmentation for network routing logic.
*   **CO5: Greedy & Optimization Algorithms**
    *   `BipartiteMatching.java`: Implements advanced network graph matching for relationship and resource pairing.
    *   `KnapsackApproximation.java`: Implements greedy and dynamic programming approximation techniques for optimization problems.
*   **CO6: Advanced String Matching & Cryptographic Hashing**
    *   `RabinKarp.java`: Implements rolling hash-based string searching for efficient multi-pattern matching.
    *   `UniversalHashing.java`: Implements advanced hashing functions and collision-resistant data structures.
*   **Embedded Web Server & Responsive Dashboard**
    *   A multithreaded local Java `HttpServer` serving dynamic JSON APIs and a dark-mode single-page HTML/JS dashboard featuring spotlight hero cards, real-time search debouncing, and a Vibe Matcher widget.

---

## 📂 Project Directory Structure

```text
KLH_CSE_2026-27_S2_-24-_MovieFlix/
│
├── bin/                        # Compiled .class files output directory
├── data/                       # Dataset fallback files (e.g., movies.csv)
├── docs/                       # Project documentation and reports
├── reports/                    # Execution reports and assets
├── results/                    # Test execution results
│
└── src/                        # Source code root
    └── com/
        └── movieflix/
            ├── WebServer.java  # Custom Java HttpServer controller and embedded UI template
            │
            ├── dataset/
            │   └── TMDBFetcher.java    # Curated blockbusters dataset with verified artwork URLs
            │
            ├── engine/
            │   ├── BipartiteMatching.java       # CO5: Graph matching algorithm
            │   ├── EditDistance.java          # CO3: Levenshtein distance fuzzy search
            │   ├── KMPMatcher.java            # CO3: Knuth-Morris-Pratt pattern search
            │   ├── KnapsackApproximation.java # CO5: Optimization algorithm
            │   ├── MaxFlow.java               # CO4: Ford-Fulkerson network flow
            │   ├── MillerRabin.java           # CO2: Primality testing
            │   ├── MovieSearchEngine.java     # Core search manager
            │   ├── MovieSimilarity.java       # Similarity scoring engine
            │   ├── NeedlemanWunsch.java       # CO3: Sequence alignment
            │   ├── RabinKarp.java             # CO6: Rolling hash search
            │   ├── StringMatcher.java         # General string utility
            │   └── UniversalHashing.java      # CO6: Advanced hashing
            │
            ├── model/
            │   └── Movie.java          # Movie entity model object
            │
            └── util/
                ├── CustomList.java     # CO1: Custom dynamic array list implementation
                └── CustomQueue.java    # CO1: Custom circular queue implementation