# Build OpenJDK

Ubuntu 24.04. Run these steps in the same Bash terminal.

Official instructions: [OpenJDK build guide for this checkout](https://github.com/openjdk/jdk/blob/jdk-27-ga/doc/building.md).

## 1. Install SDKMAN and Java

```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl zip unzip git autoconf build-essential \
  file pkg-config libasound2-dev libcups2-dev libfontconfig-dev libx11-dev \
  libxext-dev libxrandr-dev libxrender-dev libxt-dev libxtst-dev

curl -fsSL https://get.sdkman.io | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 27.0.0+35-open
sdk use java 27.0.0+35-open
```

## 2. Download jtreg from Shipilev

jtreg 8.3 is the latest tagged build listed on
[Shipilev's download page](https://builds.shipilev.net/jtreg/).

```bash
mkdir -p "$HOME/jdk-dev"
cd "$HOME/jdk-dev"
curl -fL 'https://builds.shipilev.net/jtreg/jtreg-8.3+1.zip' -o jtreg.zip
unzip jtreg.zip
export JT_HOME="$PWD/jtreg"
```

## 3. Check out OpenJDK 27

```bash
git clone --depth 1 --branch jdk-27-ga https://github.com/openjdk/jdk.git
cd jdk
```

- `--depth 1`: downloads one commit's history but all source files at that commit.
  This makes the clone smaller; omit it if you want full history.
- `--branch jdk-27-ga`: selects the fixed JDK 27 general-availability tag instead
  of `master`, which now targets JDK 28. Despite the option's name, it also
  accepts tags. This keeps the checkout matched to the boot JDK and jtreg versions
  above. A tag checkout leaves Git in detached-HEAD state, which is fine for building.

## 4. Build

```bash
bash configure --with-conf-name=demo --with-boot-jdk="$JAVA_HOME" --with-jtreg="$JT_HOME"
make images JOBS=4
build/demo/images/jdk/bin/java -version
```

## 5. Run tests

Run from the `jdk` source directory, after step 4. These commands test your built
JDK, not the SDKMAN boot JDK.

### One test, directly with jtreg

Run the presentation's HTTP-server test,
[CommandLinePortNotSpecifiedTest.java](https://github.com/openjdk/jdk/blob/jdk-27-ga/test/jdk/com/sun/net/httpserver/simpleserver/CommandLinePortNotSpecifiedTest.java).
Local TCP port **8000 must be free**. The test is marked `/manual` because it uses
this fixed port; it starts and stops the server itself. Do not add `-automatic`
(`-a`), which would exclude it.

```bash

"$JT_HOME/bin/jtreg" \
  -jdk:"$PWD/build/demo/images/jdk" \
  -othervm -ea -esa \
  -verbose:all,time -retain:all -report:executed \
  -J-Djavatest.report.kinds=html,text,xml -xml \
  -w:build/demo/httpserver/work \
  -r:build/demo/httpserver/report \
  test/jdk/com/sun/net/httpserver/simpleserver/CommandLinePortNotSpecifiedTest.java
```

- `-jdk`: selects the JDK used to compile and run the test.
- `-othervm`: uses separate JVMs for test actions, rather than reusing an agent
  JVM. This test already requires a fresh JVM through `@run junit/othervm/manual`.
- `-ea -esa`: enables Java assertions in application and system classes.
- `-verbose:all,time`: prints detailed results, including test stdout/stderr and
  action timings, for passing and failing tests.
- `-retain:all`: keeps scratch files from every test, including passing tests.
- `-report:executed`: limits the report to tests executed in this run.
- `-J-Djavatest.report.kinds=html,text,xml`: enables all three JavaTest report
  formats; `-J` sends this property to the jtreg launcher JVM, not the test JVM.
- `-xml`: additionally writes a `*.jtr.xml` file beside each `*.jtr` result in
  the work directory. This is Ant/JUnit-compatible XML for CI result import:
  status, duration and test metadata. Each jtreg test result becomes one
  `<testcase>`, not one entry per Jupiter `@Test` method. It does not enable JUnit
  execution or replace the JavaTest XML report selected by `javatest.report.kinds`.
- `-w` / `-r`: sets the work/results directory and the report directory.



Reference: [OpenJDK testing guide for this checkout](https://github.com/openjdk/jdk/blob/jdk-27-ga/doc/testing.md).
