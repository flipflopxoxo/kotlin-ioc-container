import classes.*
import org.junit.Test
import org.junit.jupiter.api.Assertions.*

internal class IOCContainerTest {
    @Test
    fun hasNoModule_requestDependency_throwsException() {
        val container = IOCContainer()

        assertThrows(ModuleNotFoundException::class.java) {
            container.get(InterfaceA::class)
        }
    }

    @Test
    fun hasMatchingModule_requestDependency_createsInstance() {
        val container = IOCContainer()
        container.addModule(InterfaceA::class, object : Module<InterfaceA> {
            override fun createInstance() = InterfaceAImplA()
        })

        container.get(InterfaceA::class)
    }

    @Test
    fun hasInjectModuleWithMissingDependency_requestInstance_throwsException() {
        val container = IOCContainer()

        container.registerInstanceType(InterfaceA::class, InterfaceAImplB::class)

        assertThrows(ModuleNotFoundException::class.java) {
            container.get(InterfaceA::class)
        }
    }

    @Test
    fun hasInjectModuleWithAllDependency_requestInstance_createInstance() {
        val container = IOCContainer()

        container.registerInstanceType(InterfaceA::class, InterfaceAImplB::class)
        container.addModule(Item::class, object : Module<Item> {
            override fun createInstance() = Item()
        })

        container.get(InterfaceA::class)
    }

    @Test
    fun hasInjectModuleWithCircularDependency_requestInstance_throwsException() {
        val container = IOCContainer()

        container.registerInstanceType(InterfaceA::class, InterfaceAImplCircularA::class)

        assertThrows(CircularDependencyException::class.java) {
            container.get(InterfaceA::class)
        }
    }

    @Test
    fun hasInjectModuleWithNontrivialCircularDependency_requestInstance_throwsException() {
        val container = IOCContainer()

        container.registerInstanceType(InterfaceA::class, InterfaceACircularB::class)
        container.registerInstanceType(InterfaceB::class, InterfaceBImplInterfaceADependent::class)

        assertThrows(CircularDependencyException::class.java) {
            container.get(InterfaceA::class)
        }
    }
}