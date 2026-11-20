# Search Engine

A multithreaded web crawler, inverted index and search web application in Java, written for the University of San Francisco course CS 212 (Software Development), 2018.

## Features

- **Indexing** of local text and HTML files (`-path`) or of web pages reached by crawling from a seed URL (`-url`, capped by `-limit`, default 50 pages).
- **Concurrency**: a work queue of worker threads (`-threads`) building a thread-safe inverted index protected by a custom read/write lock.
- **Search**: exact (`-exact`) or partial (prefix) search over a file of queries (`-query`), with results ranked and written as JSON (`-results`); the index itself is written with `-index`.
- **Web interface**: Jetty servlets (`-port`, default 8080) for search, results and visited pages, with login, registration and password change backed by MySQL.

## Layout

| Path | Content |
|---|---|
| `src/` | Java source |
| `webpages/` | Login, registration and welcome pages |
| `html/`, `query/` | Sample inputs from the course (Project Gutenberg texts, NumPy documentation, test pages) and query files |
| `database.properties.example` | Template for the database settings |

## Building and running

Dependencies: Jetty (servlet), the Java Servlet API, Log4j 2 and a MySQL JDBC driver. With their JAR files in `lib/`:

```bash
javac -cp "lib/*" -d bin src/*.java
java  -cp "bin:lib/*" Driver -path html/simple -query query/simple.txt -results results.json -threads 4
java  -cp "bin:lib/*" Driver -url https://example.org/ -limit 50 -port 8080
```

For the web interface, copy `database.properties.example` to `database.properties` and fill in the database settings; `database.properties` is listed in `.gitignore`.

## License

MIT — see [LICENSE](LICENSE).
