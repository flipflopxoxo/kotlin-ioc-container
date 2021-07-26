import classes.InterfaceA
import classes.InterfaceAImplA
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
    fun hasMatchingModule_requestDependency_throwsException() {
        val container = IOCContainer()
        container.addModule(InterfaceA::class.java) {
            InterfaceAImplA()
        }

        val instance = container.get(InterfaceA::class.java)

        assert(instance is InterfaceA)
    }
}