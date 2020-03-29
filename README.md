# Monix MDC

Monix MDC provides support for [MDC](https://logback.qos.ch/manual/mdc.html)
when using [Monix Task](https://monix.io/). This allows you to use `MDC.put`
as well as `MDC.get` when working within a `Task` and have it correctly propagated.
Monix's own `Task` `Local` is used to achieve this, which is the equivalent of a `ThreadLocal`
but has better handling of scope.

You can also mix scala `Future` along with Monix `Task` if you use `TracingScheduler`
which propagates Monix's `Local` over async boundaries.

The initial code was taken from [Oleg Pyzhcov's blog post](https://olegpy.com/better-logging-monix-1/)
so credit goes to him for the initial implementation.

## Installation

Add the following into SBT

```
libraryDependencies ++= List(
  "org.mdedetrich" %% "monix-mdc" % "0.1.0-SNAPSHOT"
)
```

## Usage

Firstly in order to use Monix Task you need to enable `TaskLocal` in your
`Task`, there are multiple ways to do this

* You can import the `Task.defaultOptions.enableLocalContextPropagation` implicit and use `runToFutureOpt`
* Applying a transformation `.executeWithOptions(_.enableLocalContextPropagation)` on each Task that uses a Local
* Setting system property `monix.environment.localContextPropagation` to 1

You also need to override the `MDC` logging adaptor by calling `MonixMDCAdapter.initialize()`. Typically you
only need to do this once in your `Main` (or equivalent)

Unfortunately MDC never added support to override the `LogbackMDCAdapter` in their API, so the `MonixMDCAdapter.initialize()`
uses reflection to achieve this. This can cause other issues (see the [limitations](#limitations) for more info)

### Interopt with Future

If you are mixing usage of `Future` with `Task` (i.e. freely using MDC both within `Task` or `Future`)
you also should `disableLocalContextIsolateOnRun`, here are the ways of doing his. Note that this requires
at least Monix 3.2.0

* You can import the `Task.defaultOptions.enableLocalContextPropagation.disableLocalContextIsolateOnRun` implicit anduse `runToFutureOpt`
* Applying a transformation `.executeWithOptions(_.enableLocalContextPropagation.disableLocalContextIsolateOnRun)` on each Task that uses a Local
* Setting system property `monix.environment.disableLocalContextIsolateOnRun` to 1

You also need to use the Monix `TracingScheduler`, just make sure that you use the same `TracingScheduler` for all `Future` operations
(as well as when running `runToFuture`/`runToFutureOpt` on your `Task`)
