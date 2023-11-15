---
title: "1.7 Context and dependency injection"
linkTitle: "1.7 Context and dependency injection"
weight: 170
sectionnumber: 1.7
description: >
   Context and dependency injection in Quarkus
---


### {{% param sectionnumber %}}.1: Beans

A bean is a container-managed object that supports a set of basic services, such as injection of dependencies, lifecycle callbacks and interceptors. Container-managed means that we don't control the lifecycle of the object instance directly. Instead we can manage and affect the lifecycle through declarative means, such as annotations and configurations. The container is the environment where the applications runs. It creates and destroys the instances of beans, associates the instances with a designated context, and injects them into other beans.

As a developer we can focus ourselves on the business logic rather than troublying ourselves with the where and how. Dependency injection is one of the implementation techniques of the inversion of control (IoC) programming principle. You can read more about dependency injection and inversion of control in this [article](https://martinfowler.com/articles/injection.html).

We are going to write and define several beans in the following excercises, an example of a bean might look like this:

```java
@ApplicationScoped
public class DummyService {

    @Inject
    AnotherBean anotherBean;

    public String dummy() {
        return "dummy";
    }
}
```

This example defines a `DummyService` bean. The annotation `@ApplicationScoped` tells the container to create a single bean instance for the application and will be used by all other beans that inject `DummyService`.

The CDI process of matching beans to injection points is type-safe. Each bean defines set of bean types. In our example the defined `DummyService` has two types: `DummyService` and `java.lang.Object`. Exactly one bean must be assignable to the injection point, otherwise your build will fail with an `UnsatisfiedResolutionException` when none are assignable and with `AmbiguousResolutionException` when multiple are assignable. You can use the `jakarta.enterprise.inject.Instance` to resolve ambiguities at runtime and iterate over all beans implementing a given type:

```java
public class Translator {

    @Inject
    Instance<Dictionary> dictionaries; 

    String translate(String sentence) {
      for (Dictionary dict : dictionaries) { 
         // ...
      }
    }
}
```


### {{% param sectionnumber %}}.2: Injection types

You have several possibilities available to inject your beans into your classes.


#### {{% param sectionnumber %}}.2.1: Field injection

You can inject your beans with the field injection. This is the most used and most straight forward method for injection:

```java
@ApplicationScoped
public class DummyService {

    @Inject
    AnotherBean anotherBean;
    
    //...
}
```


#### {{% param sectionnumber %}}.2.2: Constructor injection

The constructor injection defines the injectable beans as injectable parameters in the constructor of your class. It is not necessary to use the `@Inject` annotation if your definition only has one no-args constructor defined.

```java
@ApplicationScoped
public class DummyService {

    private final AnotherBean anotherBean;

    DummyService(AnotherBean anotherBean) {
      this.anotherBean = anotherBean;
    }
    
    //...
}
```


#### {{% param sectionnumber %}}.2.3: Setter injection

The third option is using the setter injection. Instead of defining your dependencies in the class as `@Injected` fields you can annotate a method which sets your dependencies.

```java
@ApplicationScoped
public class DummyService {

    AnotherBean anotherBean;

    @Inject
    void setDependencies(AnotherBean anotherBean) {
      this.anotherBean = anotherBean;
    }
    
    //...
}
```


### {{% param sectionnumber %}}.3: Bean scopes

The scope of a bean defines the lifecycle of its instances, it defines where and when it should be created and destroyed. Every bean has exactly one scope. In Quarkus you can use all of the built-in scopes except for `jakarta.enterprise.context.ConversationScoped`:

Annotation | Description
---|---
`@jakarta.enterprise.context.ApplicationScoped` | A single bean instance is used for the application and shared among all injection points. The instance is created lazily, i.e. once a method is invoked upon the client proxy.
`@jakarta.inject.Singleton` | Just like `@ApplicationScoped` except that no client proxy is used. The instance is created when an injection point that resolves to a `@Singleton` bean is being injected.
`@jakarta.enterprise.context.RequestScoped` | The bean instance is associated with the current request (usually an HTTP request).
`@jakarta.enterprise.context.Dependent` | This is a pseudo-scope. The instances are not shared and every injection point spawns a new instance of the dependent bean. The lifecycle of dependent bean is bound to the bean injecting it - it will be created and destroyed along with the bean injecting it.
`@jakarta.enterprise.context.SessionScoped` | This scope is backed by a `jakarta.servlet.http.HttpSession` object. It’s only available if the quarkus-undertow extension is used.

Altough the `@Singleton` is more performant, in general use going for the `@ApplicationScoped` scope. The `@Singleton` bean has no client proxy and hence an instance is created eagerly when the bean is injected. The `@ApplicationScoped` bean is created lazily and will be created when a method is invoked upon an injected instance for the first time. Due to its nature the `@Singleton` scoped bean cannot be mocked using QuarkusMock (more on that later).


### {{% param sectionnumber %}}.4: More than only beans

In general there are several kinds of beans in java CDI. We distinguish:

* Class beans
* Producer methods
* Producer fields
* Synthetic beans

Synthetic beans are not going to be covered in this lab.

But let's take a look at producers. Producer methods and field are useful if you need more control over the instantiation of a bean. They are also useful when integrating third-party libraries where you don’t control the class source and may not add additional annotations etc.

```java
@ApplicationScoped
public class Producers {

    @Produces 
    double pi = Math.PI; 

    @Produces 
    List<String> names() {
       List<String> names = new ArrayList<>();
       names.add("Andy");
       names.add("Adalbert");
       names.add("Joachim");
       return names; 
    }
}

@ApplicationScoped
public class Consumer {

   @Inject
   double pi;

   @Inject
   List<String> names;

   // ...
}
```


### {{% param sectionnumber %}}.5: Additional services

So far we have seen that we can inject our beans into classes and do not need to worry about their lifecycles. Sounds neat, but there is more.


#### {{% param sectionnumber %}}.5.1: Lifecycle callbacks

A bean class may declare lifecylce callbacks: `@PostConstruct` and `@PreDestroy`. With these two annotations the lifecycle of the bean can be altered and controlled. The `@PostConstruct` annotated callback is invoked before the bean is put into service and can be used to do initializing work. The `@PreDestroy` callback is invoked before a bean is destroyed to do cleanup tasks.

```java
import jakarta.annotation.PostConstruct;;
import jakarta.annotation.PreDestroy;

@ApplicationScoped
public class Translator {

    @PostConstruct 
    void init() {
       // ...
    }

    @PreDestroy 
    void destroy() {
      // ...
    }
}
```


#### {{% param sectionnumber %}}.5.2: Interceptors

Interceptors are a helpful tool to separate cross-cutting concerns from business logic. There is a separate specification that defines the model and semantics.

For demonstration purposes we can check out a small example. We create the annotation `@Logged` as an `@InterceptorBinding` which should indicate that some logging will be done at invokation time.

```java
@Inherited
@InterceptorBinding
@Retention(RUNTIME)
@Target({METHOD, TYPE})
public @interface Logged {
}
```

Then we can create the interceptor:

```java
@Logged
@Interceptor
public class LoggedInterceptor implements Serializable {

    public LoggedInterceptor() {
    }

    @AroundInvoke
    public Object logMethodEntry(InvocationContext invocationContext)
            throws Exception {
        System.out.println("Entering method: "
                + invocationContext.getMethod().getName() + " in class "
                + invocationContext.getMethod().getDeclaringClass().getName());

        return invocationContext.proceed();
    }
}
```


#### {{% param sectionnumber %}}.5.3: Events and Observers

Beans may also produce and consume events to interact in a completely decoupled fashion. Any Java object can serve as an event payload. The optional qualifiers act as topic selectors.

For example you can control behaviour at startup times by observing the `StartupEvent`.

```java
@ApplicationScoped
public class DummyService {

    void onStart(@Observes StartupEvent event) {
        System.out.println("Application started");
    }

}
```
