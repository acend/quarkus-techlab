---
title: "3.1 Quick introduction JUnit 5"
linkTitle: "3.1  Quick introduction JUnit 5"
weight: 310
sectionnumber: 3.1
description: >
  A short overview of JUnit 5.
---


## Task {{% param sectionnumber %}}.1: JUnit 5

In Quarkus application we usually use JUnit 5. Most people are still familiar with version 4 and we will take a short peek at the main differences between JUnit 4 and JUnit 5.

The JUnit 4 clearly had some limitations:

* The entire JUnit 4 framework was delivered in a single jar. Using only parts of the framework was not possible, blowing our applications up in size.
* There is no support in running more than one test runner.
* JUnit 4 never advanced beyond Java 7, JUnit 5 adopts Java 8 and it's features.

JUnit 5 was designed to tackle these clear drawbacks of JUnit 4. Unlike version 4 the framework was split up into three different sub-projects:

* **JUnit Plattform** is the foundation for launching testing frameworks on the JVM. It defines the `TestEngine` API for developing a testing framework that runs on the plattform.
* **JUnit Jupiter** is the combination of the new programming model and extension model for writing tests and extensions in JUnit 5. The Jupiter sub-project provides a TestEngine for running Jupiter based tests on the platform.
* **JUnit Vintage** provides a TestEngine for running JUnit 3 and JUnit 4 based tests on the platform.


### Task {{% param sectionnumber %}}.1.1: Differences


#### Task {{% param sectionnumber %}}.1.1.1: Annotations

JUnit 5 changed some annotations in comparison to it's predecessor. One imoprtant change is that we can no longer us the `@Test` annotation to specify expectations:

JUnit 4:
```java

@Test(expected = Exception.class)
public void shouldRaiseAnException() throws Exception {
    // ...
}

```

Instead we use the `assertThrows` method:

```java

public void shouldRaiseAnException() throws Exception {
    Assertions.assertThrows(Exception.class, () -> {
        //...
    });
}

```

The same goes for the `timeout` attribute in JUnit 4:

```java

@Test(timeout = 1)
public void shouldFailBecauseTimeout() throws InterruptedException {
    Thread.sleep(10);
}

```

Now, the assertTimeout method in JUnit 5:


```java

@Test
public void shouldFailBecauseTimeout() throws InterruptedException {
    Assertions.assertTimeout(Duration.ofMillis(1), () -> Thread.sleep(10));
}

```

Some other annotations were renamed for readability reasons:

* `@Before` annotation is renamed to `@BeforeEach`
* `@After` annotation is renamed to `@AfterEach`
* `@BeforeClass` annotation is renamed to `@BeforeAll`
* `@AfterClass` annotation is renamed to `@AfterAll`
* `@Ignore` annotation is renamed to `@Disabled`


#### Task {{% param sectionnumber %}}.1.1.2: Assertions

In JUnit 5 assertions can now be written in a lambda expression:

```java

@Test
public void shouldFailBecauseTheNumbersAreNotEqual_lazyEvaluation() {
    Assertions.assertTrue(
      2 == 3, 
      () -> "Numbers " + 2 + " and " + 3 + " are not equal!");
}

```

Additionally we can group assertions in JUnit 5:

```java

@Test
public void shouldAssertAllTheGroup() {
    List<Integer> list = Arrays.asList(1, 2, 4);
    Assertions.assertAll("List is not incremental",
        () -> Assertions.assertEquals(list.get(0).intValue(), 1),
        () -> Assertions.assertEquals(list.get(1).intValue(), 2),
        () -> Assertions.assertEquals(list.get(2).intValue(), 3));
}

```


#### Task {{% param sectionnumber %}}.1.1.3: Display names

 With JUnit 5, you can add the `@DisplayName` annotation to classes and methods. The name is used when generating reports, which makes it easier to describe the purpose of tests and track down failures, for example:

 ```java

@DisplayName("Test MyClass")
class MyClassTest {
    @Test
    @DisplayName("Verify MyClass.myMethod returns true")
    void testMyMethod() throws Exception {    
        // ...
    }
}

 ```
