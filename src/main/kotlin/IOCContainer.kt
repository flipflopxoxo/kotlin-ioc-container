import java.lang.reflect.Type

class IOCContainer {
    private val moduleMap = mutableMapOf<Type, () -> Any>()

    fun <T: Any> addModule(clazz: Class<T>, module: () -> T) {
        moduleMap[clazz] = module
    }

    fun <T> get(clazz: Class<T>): T {
        for (module in moduleMap) {
            if (module.key == clazz) {
                val invoke = module.value.invoke()
                if (clazz.isInstance(invoke)) {
                    return invoke as T
                }
            }
        }
        throw ModuleNotFoundException(clazz)
    }
}

class ModuleNotFoundException(clazz: Type) : Exception("No module is registered for the given type $clazz")