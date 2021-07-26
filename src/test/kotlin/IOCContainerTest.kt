import classes.*
import org.junit.Test
import org.junit.jupiter.api.Assertions.*

internal class IOCContainerTest {
    @Test
    fun hasNoModule_requestDependency_throwsException() {
        val container = IOCContainer()

        assertThrows(ModuleNotFoundException::class.java) {
            container.get(InterfaceA::class.java)
        }
    }

    @Test
    fun hasMatchingModule_requestDependency_createsInstance() {
        val container = IOCContainer()
        container.addModule(InterfaceA::class.java) {
            InterfaceAImplA()
        }

        val instance = container.get(InterfaceA::class.java)

        assert(instance is InterfaceA)
    }

    @Test
    fun hasInjectModuleWithMissingDependency_requestInstance_throwsException() {
        val container = IOCContainer()

        container.registerInjectableModule(InterfaceA::class.java, InterfaceAImplB::class.java)

        assertThrows(ModuleNotFoundException::class.java) {
            container.get(InterfaceA::class.java)
        }
    }

    @Test
    fun hasInjectModuleWithAllDependency_requestInstance_createInstance() {
        val container = IOCContainer()

        container.registerInjectableModule(InterfaceA::class.java, InterfaceAImplB::class.java)
        container.addModule(Item::class.java) {
            Item()
        }
        val instance = container.get(InterfaceA::class.java)

        assert(instance is InterfaceA)
    }

    @Test
    fun hasInjectModuleWithCircularDependency_requestInstance_throwsException() {
        val container = IOCContainer()

        container.registerInjectableModule(InterfaceA::class.java, InterfaceAImplCircularA::class.java)

        assertThrows(CircularDependencyException::class.java) {
            container.get(InterfaceA::class.java)
        }
    }

    @Test
    fun hasInjectModuleWithNontrivialCircularDependency_requestInstance_throwsException() {
        val container = IOCContainer()

        container.registerInjectableModule(InterfaceA::class.java, InterfaceACircularB::class.java)
        container.registerInjectableModule(InterfaceB::class.java, InterfaceBImplInterfaceADependent::class.java)

        assertThrows(CircularDependencyException::class.java) {
            container.get(InterfaceA::class.java)
        }
    }
}