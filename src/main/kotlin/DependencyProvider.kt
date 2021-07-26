import kotlin.reflect.*

class DependencyProvider {
    private val moduleMap = mutableMapOf<KClass<out Any>, Generator<out Any>>()

    /**
     * Register a module that generates an instance of type T to provide as a dependency.
     */
    fun <T : Any> registerModule(clazz: KClass<T>, module: Module<T>) {
        moduleMap[clazz] = Generator.IndependentGenerator(module)
    }

    /**
     * Register a class with exactly one constructor annotated with @Inject to generate a new instance, while injecting
     * the required parameters, to provide as a dependency.
     */
    fun <T : Any> registerInstanceType(dependencyClass: KClass<T>, instanceClazz: KClass<out T>) {
        val injectConstructor = instanceClazz.constructors.singleOrNull {
            it.annotations.filter { it is Inject }.isNotEmpty()
        } ?: throw InjectConstructorNotFoundException()
        moduleMap[dependencyClass] = Generator.InjectGenerator(injectConstructor)
    }

    /**
     * Register an instance that will be provided whenever a request for the dependency is made
     */
    fun <T : Any> registerInstance(dependencyClass: KClass<out T>, instance: T) {
        moduleMap[dependencyClass] = Generator.InstanceProvider(instance)
    }

    /**
     * Retrieve an instance of the required T
     */
    fun <T : Any> get(clazz: KClass<T>): T {
        return generateInstance(clazz, emptyList())
    }

    private fun <T : Any> generateInstance(clazz: KClass<out T>, pendingList: List<KClass<out T>>): T {
        if (pendingList.contains(clazz)) {
            throw CircularDependencyException()
        }
        for (module in moduleMap) {
            if (module.key == clazz) {
                val invoke = module.value.generateInstance(this, pendingList + clazz)
                if (clazz.isInstance(invoke)) {
                    return clazz.cast(invoke)
                }
            }
        }
        throw ModuleNotFoundException(clazz)
    }

    private sealed class Generator<T : Any> {
        abstract fun generateInstance(container: DependencyProvider, consumedModules: List<KClass<out Any>>): T

        class IndependentGenerator<T : Any>(private val generator: Module<T>) : Generator<T>() {
            override fun generateInstance(container: DependencyProvider, consumedModules: List<KClass<out Any>>): T {
                return generator.createInstance()
            }
        }

        class InjectGenerator<T : Any>(private val constructor: KCallable<T>) : Generator<T>() {
            override fun generateInstance(container: DependencyProvider, consumedModules: List<KClass<out Any>>): T {
                val parameterMap = mutableMapOf<KParameter, Any>()
                for (parameterType in constructor.parameters) {

                    val classifier = parameterType.type.classifier
                    if (classifier is KClass<out Any>) {
                        parameterMap[parameterType] =
                            container.generateInstance(classifier, consumedModules)
                    } else {
                        throw ModuleNotFoundException(classifier)
                    }
                }
                val parameter = constructor.parameters.map {
                    parameterMap[it]
                }
                return constructor.call(*parameter.toTypedArray())
            }
        }

        class InstanceProvider<T : Any>(private val instance: T) : Generator<T>() {
            override fun generateInstance(container: DependencyProvider, consumedModules: List<KClass<out Any>>): T {
                return instance
            }

        }
    }
}

class ModuleNotFoundException(clazz: KClassifier?) : Exception("No module is registered for the given type $clazz")
class InjectConstructorNotFoundException() :
    Exception("No unique constructor with the @Inject annotation has been found")

class CircularDependencyException : Exception()