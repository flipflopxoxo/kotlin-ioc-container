import java.lang.reflect.Constructor
import java.lang.reflect.Type

class IOCContainer {
    private val moduleMap = mutableMapOf<Type, Module<out Any>>()

    fun <T : Any> addModule(clazz: Class<T>, module: () -> T) {
        moduleMap[clazz] = Module.IndependentModule(module)
    }

    fun <T> registerInjectableModule(clazz: Class<T>, instanceClazz: Class<out T>) {
        val injectConstructor = instanceClazz.constructors.singleOrNull {
            it.isAnnotationPresent(Inject::class.java)
        } ?: throw InjectConstructorNotFoundException()
        moduleMap[clazz] = Module.InjectModule(injectConstructor)
    }

    fun <T> get(clazz: Class<T>): T {
        return generateInstance(clazz, emptyList())
    }

    private fun <T> generateInstance(clazz: Class<T>, pendingList: List<Type>): T {
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

    private sealed class Module<T> {
        abstract fun generateInstance(container: IOCContainer, consumedModules: List<Type>): T

        class IndependentModule<T>(private val generator: () -> T) : Module<T>() {
            override fun generateInstance(container: IOCContainer, consumedModules: List<Type>): T {
                return generator()
            }
        }

        class InjectModule<T>(private val constructor: Constructor<T>) : Module<T>() {
            override fun generateInstance(container: IOCContainer, consumedModules: List<Type>): T {
                val parameterMap = mutableMapOf<Type, Any>()
                for (parameterType in constructor.parameterTypes) {
                    parameterMap[parameterType] = container.generateInstance(parameterType, consumedModules)
                }
                val parameter = constructor.parameterTypes.map {
                    parameterMap[it]
                }
                return constructor.newInstance(*parameter.toTypedArray())
            }
        }
    }
}

class ModuleNotFoundException(clazz: Type) : Exception("No module is registered for the given type $clazz")
class InjectConstructorNotFoundException() :
    Exception("No unique constructor with the @Inject annotation has been found")

class CircularDependencyException : Exception()