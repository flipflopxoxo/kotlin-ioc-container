#IoC Container
##Approach
The idea is to provide three different ways of generating dependencies from the DependencyContainer.
- A single instance that will be reused whenever the dependency is requested
- A generator created from the constructor of the registered class with the @Inject annotation
- A generator that generates a new object whenever the dependency is requested

These are represented by these three corresponding methods:
- registerInstance(class, instance)
- registerInstanceType(class, implementingClass)
- registerModule(class, module)

While the behavior of the registerInstance and registerModule are straightforward,
the  registerInstanceType method is the most nuanced.

When you register an implementation of a class using registerInstanceType, it will look for the constructor annotated with @Inject.
When retrieving a dependency, it will use the constructor, and the values for its parameters will be resolved using the
registered providers in the DependencyProvider.

The DependencyProvider class also handle circular dependencies by keeping a stack of dependencies that are child
dependencies while resolving the main dependency.

A TDD approach is done while writing the implementation. Therefore, the test cases demonstrates how the DependencyManager
should be used.