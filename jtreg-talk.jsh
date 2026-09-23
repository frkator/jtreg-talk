// JVisualBook chapter for Dublin Java User Group.
// Copyright (c) 2026 Ivan Šipka. Original material: CC BY 4.0.
// See LICENSE and NOTICE.md for scope, attribution and third-party exclusions.
// Presenter NOTE TO SELFs are marked NOTE TO SELF: and are intentionally visible in document mode;
// delete them for a cleaner final deck after rehearsal.

// # How do you test Java?
// ## Inside jtreg and OpenJDK testing
//
// **Ivan Šipka - Dublin Java User Group**
//
// A test harness, an execution model, and one uncomfortable question:
//
// > If every test that ran passed, how do you know every test that should have run actually did?

// # This presentation is a Java program
// JVisualBook evaluates Java snippets with JShell and renders their output inline.
//
// So let's start with the one of the most reassuring tests imaginable.
int add(int a, int b) { return a + b; }
int a = 2, b = 0;
var answer = add(a, b);
System.out.printf("%d + %d = %d%n",a,b,answer);
assert answer == 4;
System.out.println("PASS");

// # Good. Java works.
// ## ...doesn't it?
//
// We just used **Java to test Java**. But what if the thing under test is:
//
// - <span style="color: red">`javac`?</span>
// - `java.net.Socket`?
// - <span style="color: red">a garbage collector?</span>
// - <span style="color: red">JVM startup with a particular flag?</span>
// - <span style="color: red">code that is *supposed* to fail compilation?</span>
// - behaviour that exists only on Windows or only on Linux?
//
// The execution environment is now part of the experiment.

// ## In practice, usually yes.
// Because we use **jtreg to test Java**.
//
// - <span style="color: green">`javac`?</span> With jtreg, compilation is the test-not just the build.
// - `java.net.Socket`? jtreg can give each networking configuration a fresh JVM-not just a fresh socket.
// - <span style="color: green">a garbage collector?</span> jtreg selects the collector and keeps the harness outside the JVM that might crash.
// - <span style="color: green">JVM startup with a particular flag?</span> jtreg sets the JVM’s conditions before a test framework can execute its first instruction.
// - <span style="color: green">code that is *supposed* to fail compilation?</span> With jtreg, correctly refusing to compile is a passing test. jtreg checks the platform before running the experiment-not halfway through it.
// - behaviour that exists only on Windows or only on Linux?
//
// Created by **Iris Clark in 1997**, jtreg remains the JDK’s primary unit, regression and integration test harness.

// # The problem is larger than assertions
// A conventional unit-test question is roughly:
//
// > Given this program state, did this operation produce the expected result?
//
// JDK testing often has an additional question:
//
// > **Under exactly what JDK, process, platform and configuration should this experiment run?**
//
// That is the useful mental model for **jtreg**.
//
// jtreg is not "JUnit, but old". It is a regression-test harness and runner built around the JDK's testing needs.

// # Test frameworks and harnesses overlap more than they first appear
// **JUnit 4:** integrated framework. **JUnit 5 here:** Platform + Jupiter programming model/engine, shown together.
//
// | **Capability** | **jtreg** | **JUnit 4** | **JUnit 5** | **TestNG** |
// |---|---|---|---|---|
// | **Discovery / class-method selection** | Headers/files; delegated methods | Runner / Request | Platform selectors + engines | Annotations / XML / API |
// | **Test annotations** | Comment tags | Framework API | Jupiter | Framework API |
// | **Assertions** | Java code / library | Assert API | Jupiter Assertions | Assert API |
// | **Setup / teardown** | Code / hosted framework | `@Before`, `@After`, etc. | Jupiter lifecycle | Configuration annotations |
// | **Parameters / test data** | Action args / hosted framework | `Parameterized` runner | Jupiter parameters | `DataProvider` |
// | **Tags / categories / groups** | Keywords / groups | Categories | Jupiter tags + Platform filters | Groups |
// | **Listeners / reporting** | Harness observers / reports | `RunListener` | Platform listeners / reports | Listeners / reporters |
// | **Execution hooks** | Observers / timeout handlers | Runner / Rule | Jupiter extensions / Platform SPI | Hooks / transformers |
// | **Parallel execution** | Harness concurrency | [Experimental API](https://junit.org/junit4/javadoc/4.13.2/org/junit/experimental/ParallelComputer.html) / tooling | Jupiter engine | Native |
// | **Properties; environment (Java)** | Supplies context; Java access | <span style="color: red">Properties read/write; env read</span> | <span style="color: red">Properties read/write; env read</span> | <span style="color: red">Properties read/write; env read</span> |
// | **Read jtreg `test.*`** | Supplies properties | <span style="color: red">Java code, when supplied</span> | <span style="color: red">Java code, when supplied</span> | <span style="color: red">Java code, when supplied</span> |
// | **Launch child processes** | [`ProcessTools.createTestJavaProcessBuilder`](https://github.com/openjdk/jdk/blob/master/test/lib/jdk/test/lib/process/ProcessTools.java)  | Java `ProcessBuilder` | Java `ProcessBuilder` | Java `ProcessBuilder` |
// | **Test timeouts** | Harness action timeout | `@Test(timeout)` / Rule | Jupiter `@Timeout` | `@Test(timeOut)` |
//
// **Discovery, lifecycle, filtering and execution orchestration are shared concerns.**

// # jtreg owns the experiment, not just the test code
// **while all can invoke child vms, jtreg sets and records external conditions before test execution. JUnit and TestNG primarily run inside that runtime.**
//
// | **Capability** | **jtreg** | **JUnit 4** | **JUnit 5** | **TestNG** |
// |---|---|---|---|---|
// | **OS / arch / VM eligibility** | `@requires` | Runtime assumptions | Jupiter conditions / assumptions | <span style="color: red">Runtime checks</span> / filters |
// | **Multiple VM configurations** | Repeated `@run` variants | <span style="color: red">External orchestration</span> | <span style="color: red">External orchestration</span> | <span style="color: red">External orchestration</span> |
// | **JDK under test** | JAVA_HOME/JT_HOME vs `-jdk`/`-testjdk` | <span style="color: red">External tooling</span> | <span style="color: red">External tooling</span> | <span style="color: red">External tooling</span> |
// | **Modules / access** | `@modules` + VM options | <span style="color: red">External configuration</span> | <span style="color: red">External configuration</span> | <span style="color: red">External configuration</span> |
// | **Compile / `javac` options** | `-compilejdk` + `@compile` + options | <span style="color: red">Tooling / explicit code</span> | <span style="color: red">Tooling / explicit code</span> | <span style="color: red">Tooling / explicit code</span> |
// | **Expected compile failure** | `@compile/fail` | <span style="color: red">Explicit code</span> | <span style="color: red">Explicit code</span> | <span style="color: red">Explicit code</span> |
// | **Startup `-X`, `-XX`, `-D`** | Action options | <span style="color: red">External tooling</span> | <span style="color: red">External tooling</span> | <span style="color: red">External tooling</span> |
// | **Fresh JVM / reuse** | `/othervm` / `-agentvm` | <span style="color: red">External tooling</span> | <span style="color: red">External tooling</span> | <span style="color: red">External tooling</span> |
// | **Process/action timeout** | Harness supervision | <span style="color: red">External tooling</span> | <span style="color: red">External tooling</span> | <span style="color: red">External tooling</span> |
// | **Process exit / crash** | Harness detection / results | <span style="color: red">Tooling; code for children</span> | <span style="color: red">Tooling; code for children</span> | <span style="color: red">Tooling; code for children</span> |
// | **Source/classes/work/report** | **Harness-defined layout** | <span style="color: red">Build / runner</span> | <span style="color: red">Build paths</span>; Platform reports | <span style="color: red">Build paths</span>; native reports |
// | **VM options to children** | `test.*` + <span style="color: red">explicit helper</span> | <span style="color: red">Explicit helper</span> | <span style="color: red">Explicit helper</span> | <span style="color: red">Explicit helper</span> |
//
// [jtreg command-line reference](https://openjdk.org/jtreg/command-help.html)
//
// ```text
// OUTSIDE: jtreg establishes / supervises / records the test invocation
//     -> TEST JVM: JUnit 4 / Platform + Jupiter / TestNG / main program
//         -> CHILD JVM: test code calls ProcessBuilder; separate startup conditions
// ```
//
// **Observe:** `System.getProperty("foo")`. **Mutate runtime:** `System.setProperty("foo", "bar")`.
//
// **Choose invocation:** `-XX:+UseG1GC` versus `-XX:+UseSerialGC` requires another JVM.
//
// **Resources:** observe CPU/memory, constrain heap (`-Xmx`), condition eligibility (`@requires`).
// <span style="color: red"><strong>Red = external to framework:</strong></span> build/IDE/CI, custom test code or test libraries.
// **OpenJDK helper:** [`ProcessTools.createTestJavaProcessBuilder`](https://github.com/openjdk/jdk/blob/master/test/lib/jdk/test/lib/process/ProcessTools.java) adds `test.vm.opts` / `test.java.opts`; plain `ProcessBuilder` does not.
//
// **JUnit describes execution inside the test runtime; jtreg also describes the conditions under which that runtime comes into existence.**

// # **Harness-defined layout**: What changes jtreg's output layout?
// | Dimension | Effect on the filesystem |
// |---|---|
// | Work / report roots | `-w:` / `-r:` choose the roots; defaults: `JTwork` / `JTreport`. |
// | Test descriptions in one source | Separate `.jtr` results, e.g. `Foo_id0.jtr`, `Foo_id1.jtr` for generated IDs. One source file need not equal one test. |
// | Multiple suite roots | Per-suite subdirectories, commonly `test_jdk`, `test_langtools`, `hotspot_jtreg`; name collisions get suffixes. |
// | Concurrency | Independently numbered worker slots: `classes/0/`, `classes/1/` and shared `scratch/0/`, `scratch/1/`. Not PIDs. |
// | Execution mode / retention | Shared versus test-specific scratch locations; which generated files survive. |
// | Libraries / packages / modules / suite version | Can change compiled output: per-test `.d` directories, shared libraries, `modules/` and `patches/`. |
// | Report formats | HTML / text and optional JavaTest XML under the report root; separate `-xml` writes per-test `.jtr.xml` in the work tree. |
// | Surrounding wrapper | OpenJDK `make` adds test-descriptor directories. A group-named outer directory is not direct jtreg grouping. |
//
// **Retention changes scratch placement, not just deletion**
//
// | Invocation characteristic | Scratch / retained-file behavior |
// |---|---|
// | No `-retain`, or `-retain:lastRun` | Reuse shared scratch; clear previous contents before the next test. Final scratch contents remain. |
// | `-agentvm -retain:all` | Run in shared / worker scratch; move retained files to test-specific result directories. |
// | `-othervm -retain:all` | Run directly in the test-specific retained-file directory. |
// | `-retain:fail,error`, file patterns, or `-retain:none` | Select which test-generated files survive; the basic `.jtr` identity is unchanged. |
//
// [jtreg command-line reference](https://openjdk.org/jtreg/command-help.html) / [Scratch-directory implementation](https://github.com/openjdk/jtreg/blob/master/src/share/classes/com/sun/javatest/regtest/exec/ScratchDirectory.java)

// # Meet a jtreg test
// A minimal test can be remarkably small:
//
// ```java
// /*
//  * @test
//  * @summary Verify that Thing does the thing
//  * @run main ThingTest
//  */
// public class ThingTest {
//     public static void main(String[] args) throws Exception {
//         // arrange → act → verify
//     }
// }
// ```
//
// `@test` identifies the test. `@summary` says why it exists. `@run` describes an action.
//

// # The interesting part is HOW it runs
// Change only the metadata:
//
// ```java
// /*
//  * @test
//  * @requires os.family == "linux"
//  * @run main/othervm -Xmx64m ThingTest
//  */
// ```
//
// Now the test says something about its experimental conditions:
//
// **Linux. Fresh JVM. 64 MiB heap. Run this entry point.**
//
// Why care about a fresh JVM? VM flags, system properties, class loading, agents, native state, GC state, crashes, and contamination from earlier tests.

// # jtreg can also host JUnit
// This is a real test in today's OpenJDK:
//
// [`CommandLinePortNotSpecifiedTest.java`](https://github.com/openjdk/jdk/blob/master/test/jdk/com/sun/net/httpserver/simpleserver/CommandLinePortNotSpecifiedTest.java)
//
// ```java
// /*
//  * @test
//  * @bug 8276848
//  * @summary Tests the java -m jdk.httpserver command with port not specified
//  * @modules jdk.httpserver
//  * @library /test/lib
//  * @run junit/othervm/manual CommandLinePortNotSpecifiedTest
//  */
// ```
//
// **jtreg and JUnit are not competitors here.**
//
// JUnit can express assertions and lifecycle inside the test; jtreg describes how that test belongs in the JDK experiment and how it is executed.
//
// NOTE TO SELF: This example comes from the Simple Web Server area introduced around JEP 408.

// # What is that test actually doing?
// [`CommandLinePortNotSpecifiedTest.java`](https://github.com/openjdk/jdk/blob/master/test/jdk/com/sun/net/httpserver/simpleserver/CommandLinePortNotSpecifiedTest.java) launches another Java process running `jdk.httpserver`, waits for the server to start, checks its output, and terminates it.
//
// ```text
// jtreg
//   └─ junit/othervm/manual
//        └─ test JVM
//             └─ ProcessBuilder
//                  └─ java -m jdk.httpserver
//                       └─ HTTP server process
// ```
//
// One "test" can therefore be a small orchestration system.
//
// This is why the harness matters: the process topology is part of correctness.

// # Run one test
// Run from the built OpenJDK source checkout. [Test source](https://github.com/openjdk/jdk/blob/jdk-27-ga/test/jdk/com/sun/net/httpserver/simpleserver/CommandLinePortNotSpecifiedTest.java): port **8000 must be free**; do not use `-automatic` / `-a` (the test is `/manual`, but starts and stops its own server).
//
// **Via OpenJDK make**
//
// ```bash
// make test-only CONF=demo \
//   JT_HOME="$JT_HOME" \
//   JDK_UNDER_TEST="$PWD/build/demo/images/jdk" \
//   TEST="test/jdk/com/sun/net/httpserver/simpleserver/CommandLinePortNotSpecifiedTest.java" \
//   JTREG="TEST_MODE=othervm;ASSERT=true;MANUAL=true;VERBOSE=all,time;RETAIN=all;REPORT=executed;OPTIONS=-xml" \
//   JTREG_LAUNCHER_OPTIONS="-Djavatest.report.kinds=html,text,xml" \
//   TEST_SUPPORT_DIR="$PWD/build/demo/httpserver/work" \
//   TEST_RESULTS_DIR="$PWD/build/demo/httpserver/report"
// ```
//
// **Direct jtreg**
//
// ```bash
// "$JT_HOME/bin/jtreg" \
//   -jdk:"$PWD/build/demo/images/jdk" \
//   -othervm -ea -esa \
//   -verbose:all,time -retain:all -report:executed \
//   -J-Djavatest.report.kinds=html,text,xml -xml \
//   -w:build/demo/httpserver/work \
//   -r:build/demo/httpserver/report \
//   test/jdk/com/sun/net/httpserver/simpleserver/CommandLinePortNotSpecifiedTest.java
// ```
//
// | Option | Meaning |
// |---|---|
// | `-jdk` | Built JDK used to compile and run the test, not the SDKMAN boot JDK. |
// | `-othervm` | Separate JVMs for test actions; this test already specifies `@run junit/othervm/manual`. |
// | `-ea -esa` | Enable Java assertions in application and system classes. |
// | `-verbose:all,time` | Detailed results, stdout/stderr and action timings, for passing and failing tests. |
// | `-retain:all` | Keep scratch files from every test, including passing tests. |
// | `-report:executed` | Limit the report to tests executed in this run. |
// | `-J-Djavatest.report.kinds=html,text,xml` | All three JavaTest report formats. `-J` targets the jtreg launcher JVM, not the test JVM. |
// | `-xml` | Ant/JUnit-compatible `*.jtr.xml` beside each `*.jtr`: status, duration, metadata. One `<testcase>` per jtreg result, not per Jupiter `@Test`; neither enables JUnit nor replaces JavaTest XML. |
// | `-w` / `-r` | Work/results directory / report directory. |

// # One green test
// ## Are we done?
//
// ```text
// Passed: 1
// ```
//
// What have we established?
//
// - one selected test
// - on this platform
// - on this architecture
// - with this JDK build
// - under this configuration
// - in this execution
//
// **A green observation has a scope.**

// # Now multiply
// JDK verification is not one test invocation.
//
// ```text
//                    tests
//                      ×
//                  platforms
//                      ×
//                architectures
//                      ×
//               configurations
//                      ×
//                   changes
//                      ↓
//              enormous evidence set
// ```
//
// OpenJDK therefore uses component groups and progressively broader **tiers** to balance coverage, cost and stability.
//
// NOTE TO SELF: Tell the real release-verification story here: tens of thousands of tests reconciled against millions of CI executions. Keep internal implementation details abstract.

// # The trap
// Imagine the release dashboard says:
//
// ```text
// Expected tests:  30,142
// Executed tests:  30,141
// Failures:             0
// ```
//
// ## Did the build pass?
//
// NOTE TO SELF: **Ask the room. Do not advance immediately.**

// # No.
// ## More precisely: we do not know.
//
// ```text
// Expected
//    │
//    ├──────────── Executed
//    │                 │
//    │              ┌──┴──┐
//    │            PASS   FAIL
//    │
//    └──────────── Missing
//                      │
//                   UNKNOWN
// ```
//
// > **A test that did not run cannot fail.**
//
// Therefore:
//
// **zero failures ≠ complete verification**

// # Test execution is itself something to verify
// This is the layer above the individual test harness:
//
// ```text
// Source / test inventory                 CI evidence
//          │                                  │
//          └──────────┐            ┌──────────┘
//                     ▼            ▼
//                    RECONCILIATION
//                          │
//               ┌──────────┼──────────┐
//               ▼          ▼          ▼
//             PASS        FAIL      MISSING
// ```
//
// The important property is not merely **correct results**.
//
// It is **accountable evidence that the required experiments occurred**.
//
// This idea transfers directly to CI/CD, security scans, data pipelines and distributed verification systems.

// # A real tier1 run
// From the JDK checkout: the [`test/jdk:tier1` group](https://github.com/openjdk/jdk/blob/master/test/jdk/TEST.groups), not all OpenJDK tier1 suites.
//
// ```bash
// STATS='run=%r passed=%p passedNotSkipped=%P failed=%f errors=%e failedOrError=%F'
// STATS+=' skipped=%s notRun=%n excluded=%x notMatched=%X keywords=%k requires=%R'
// STATS+=' priorStatus=%S timeLimit=%t%?{ modules=m}%?{ otherFilters=o}'
//
// time JTREG_JAVA="$(readlink -f "$(command -v java)")" \
//   ~/apps/jtreg-8.3+1/bin/jtreg \
//   -concurrency:10 -othervm -ea -esa \
//   -verbose:all,time -retain:all -report:executed \
//   -J-Djavatest.report.kinds=html,text,xml -xml -automatic \
//   "-J-Djtreg.stats.format=$STATS" \
//   -w:build/demo/tiers/work \
//   -r:build/demo/tiers/report \
//   test/jdk:tier1
// ```
//
// `-J-Djtreg.stats.format` configures the launcher's summary, not the test JVM. `STATS` is the same format string, split into Bash assignments.
// `-concurrency:10` permits ten simultaneous jtreg tests; it does not fix the total number of JVMs or threads.

// # The run, as reported
// Recorded output; summary counts annotated below.
//
// ```text
// run=2610                 jtreg results: passed + failed + errors
// passed=2423              Includes the runtime-skipped test
// passedNotSkipped=2422    Passed minus runtime skips
// failed=93               Test reported failure
// errors=94               Test could not be evaluated normally; see its .jtr
// failedOrError=187        failed + errors = 93 + 94
// skipped=1               Runtime SkippedException; already counted in passed
// notRun=0                NOT_RUN status, not the number rejected by filters
// excluded=0              Rejected by an exclude/problem list
// notMatched=0            Rejected because absent from a supplied match list
// keywords=9              Keyword filtering; -automatic excludes manual tests
// requires=34             Unsatisfied @requires conditions
// priorStatus=0           Previous result did not match the -status selection
// timeLimit=0             Declared-timeout filtering, NOT runtime timeouts
//
// Framework-based tests: 246,660 = 46,916 TestNG + 199,744 JUnit
//   TestNG: reported test cases run, including failures and skips
//   JUnit: test cases found, not necessarily started or passed
//   Many framework cases can belong to one jtreg result; do not add to run.
//
// Report written to /home/linski/workspace/jdk/build/demo/tiers/report/html/report.html
// Results written to /home/linski/workspace/jdk/build/demo/tiers/work
// Error: Some tests failed or other problems occurred.
//
// real    44m58.062s
// user    396m22.711s
// sys     23m56.597s
// ```
//
// | Timing | Meaning |
// |---|---|
// | `real` | Elapsed wall-clock time: just under 45 minutes. |
// | `user` | Accumulated user-space CPU time for the command and its children. |
// | `sys` | Accumulated kernel CPU time for the command and its children. |
//
// CPU time adds across concurrent processes/threads, so it can exceed elapsed time. This is one measured run, not a tier1 duration guarantee.
// **187 failed/error results make this an unsuccessful run.** The `.jtr` files explain why; the summary alone does not establish 187 JDK bugs.

// # What the result counts mean
// These are **jtreg test-description results**, not Java methods or assertions.
//
// | Counter | Value | Meaning |
// |---|---:|---|
// | `run` (`%r`) | 2610 | Passed + failed + error results. |
// | `passed` (`%p`) | 2423 | Passing status, including jtreg runtime skips. |
// | `passedNotSkipped` (`%P`) | 2422 | Passed minus skipped. |
// | `failed` (`%f`) | 93 | Test reported failure. |
// | `errors` (`%e`) | 94 | Test could not be evaluated normally; inspect the `.jtr` reason. |
// | `failedOrError` (`%F`) | 187 | Failed + error results. |
// | `skipped` (`%s`) | 1 | Executed, then threw `jtreg.SkippedException`; already included in passed. |
// | `notRun` (`%n`) | 0 | Results with `NOT_RUN` status; not the filter-rejection total. |
//
// **2610 = 2423 + 93 + 94; 2423 = 2422 + 1; 187 = 93 + 94.** [Counter definitions (jtreg 8.3+1)](https://github.com/openjdk/jtreg/blob/jtreg-8.3%2B1/src/share/classes/com/sun/javatest/regtest/report/TestStats.java).
//
// **Framework-based tests are a different counting unit:** 246,660 = 46,916 TestNG + 199,744 JUnit.
// TestNG contributes its reported total tests run; JUnit contributes tests found, not just tests started or passed. Parameterized cases can multiply these counts.
// One jtreg result can cover many framework cases. Do not add these counts to `run`, or interpret 246,660 as successful executions. [Framework aggregation](https://github.com/openjdk/jtreg/blob/jtreg-8.3%2B1/src/share/classes/com/sun/javatest/regtest/report/SummaryReporter.java).

// # Filtered out is not the same as skipped
// Filters reject test descriptions **before execution**; `skipped=1` describes a runtime outcome.
//
// | Counter | Value | Reason execution was filtered out |
// |---|---:|---|
// | `excluded` (`%x`) | 0 | Exclude/problem list (`-exclude`). |
// | `notMatched` (`%X`) | 0 | Absent from the allowed match list (`-match`). |
// | `keywords` (`%k`) | 9 | Keyword selection; here `-automatic` rejects manual tests. |
// | `requires` (`%R`) | 34 | Unsatisfied `@requires` expression. |
// | `priorStatus` (`%S`) | 0 | Previous result does not match `-status`. |
// | `timeLimit` (`%t`) | 0 | Declared timeout exceeds `-timelimit`; not runtime timeouts. |
// | `modules` (`%?{ modules=m}`) | 0 | Required modules unavailable. |
// | `otherFilters` (`%?{ otherFilters=o}`) | 0 | Other filter rejections. |
//
// `%?{...}` suppresses zero-valued fields: that is why `modules` and `otherFilters` are absent. [Counter implementation](https://github.com/openjdk/jtreg/blob/jtreg-8.3%2B1/src/share/classes/com/sun/javatest/regtest/report/TestStats.java); [selection options](https://openjdk.org/jtreg/command-help.html).
//
// **`notRun=0` still coexists with 43 filter rejections: 9 + 34.**
// Check `build/demo/tiers/report/text/notRun.txt` for filtered test names/reasons. These counters do not establish completeness against an independent required-test inventory.

// # And then there is - magic!
//
// <img src="/images/magic.gif" alt="Shia LaBeouf performing magic" style="display: block; width: 450px; max-width: 100%; height: auto; max-height: 55vh; object-fit: contain; margin: 0 auto">
//
// <!-- Third-party GIF, excluded from CC BY 4.0; permission unverified. See NOTICE.md. Source: https://gifdb.com/gif/shia-labeouf-showing-magic-meme-2224i1cqgndcb0yj.html -->

// ## The infamous flaky test
//
// <pre><span style="color: green">PASS  PASS  PASS  PASS</span>  <span style="color: red">FAIL</span>  <span style="color: green">PASS  PASS</span></pre>
//
// Only on Windows.
//
// Only under load.
//
// Only after another test.
//
// Only in CI.
//
// Cannot reproduce locally.
//
// **A test suite is a distributed measurement system. Treat it like one.**

// # What should you remember about jtreg?
// 1. **The environment is part of the test.**
// 2. **Metadata describes the experiment**, not just the assertion.
// 3. **Process isolation is a correctness tool.**
// 4. **JUnit can live inside jtreg**; they solve different layers of the problem.
// 5. At scale, **execution completeness becomes a first-class correctness property**.
//
// # Caveats
// 1. **Version compatibility of the the launcher JDK vs. test JDK vs. jtreg must be compatible - deduce by respecetive release dates.** [incompatibility effects ticket](https://bugs.openjdk.org/browse/CODETOOLS-7902044)
//
// 2. **Actual state is not necessarily the state in your head: verify that the source you read, binaries you execute, and dumps you inspect belong to the experiment you think you ran.**
//
// 3. **Passing is not completeness / equal totals can conceal different executions: reconcile what you intended to run with what actually ran - and account for every exclusion, skip, and missing execution across test identities × platforms × configurations.**

// # **How do you test Java?**
//
// **Carefully - and then you verify that the verification actually happened.**
//

// # You can test Java
// The JDK is not a mysterious binary underneath your applications.
//
// It is a codebase you can inspect, build, break and test.
//
// ```bash
// git clone https://github.com/openjdk/jdk.git
// cd jdk
// bash configure
// make images
// make test-tier1
// ```
//
// Then pick one test from OpenJDK's [`test/jdk`](https://github.com/openjdk/jdk/tree/master/test/jdk) directory:
//
// **read → predict → run → break → diagnose → fix**

// # One challenge
// This week:
//
// 1. Open one jtreg test in OpenJDK's [`test/jdk`](https://github.com/openjdk/jdk/tree/master/test/jdk) directory.
// 2. Explain every header tag to yourself.
// 3. Predict what process(es) it will create.
// 4. Run it.
// 5. Deliberately make it fail.
// 6. Find the evidence explaining *why* it failed.
//
// If you can do that, jtreg has stopped being infrastructure somebody else owns.

// # Thank you
// ## Questions?
//

// ---
// References for the final deck:
// Original presentation material: Copyright (c) 2026 Ivan Šipka, [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/). Third-party material below is excluded.
//
// - Magic GIF: [GIFDB source](https://gifdb.com/gif/shia-labeouf-showing-magic-meme-2224i1cqgndcb0yj.html). Third-party material; redistribution permission unverified.
// - Quoted OpenJDK test header: Copyright (c) 2021, 2025, Oracle and/or its affiliates; [GPLv2 only](https://github.com/openjdk/jdk/blob/master/LICENSE).
// - OpenJDK, *Testing the JDK*: https://github.com/openjdk/jdk/blob/master/doc/testing.md
// - jtreg project / FAQ: https://openjdk.org/jtreg/
// - JVisualBook: https://github.com/forax/jvisualbook
// - OpenJDK example: [`CommandLinePortNotSpecifiedTest.java`](https://github.com/openjdk/jdk/blob/master/test/jdk/com/sun/net/httpserver/simpleserver/CommandLinePortNotSpecifiedTest.java)
// - [jtreg tag specification](https://github.com/openjdk/jtreg/blob/master/src/share/doc/javatest/regtest/tag-spec.html) and [FAQ](https://github.com/openjdk/jtreg/blob/master/src/share/doc/javatest/regtest/faq.md)
// - [JUnit 4 API](https://junit.org/junit4/javadoc/4.13.2/overview-summary.html), including experimental [ParallelComputer](https://junit.org/junit4/javadoc/4.13.2/org/junit/experimental/ParallelComputer.html)
// - [JUnit 5 User Guide: Platform and Jupiter](https://docs.junit.org/5.13.4/user-guide/)
// - [TestNG documentation](https://testng.org/)
// - OpenJDK test library: [ProcessTools.java](https://github.com/openjdk/jdk/blob/master/test/lib/jdk/test/lib/process/ProcessTools.java)
