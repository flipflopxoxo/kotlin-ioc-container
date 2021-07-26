package classes

import Inject

class InterfaceAImplCircularA @Inject constructor(val interfaceA: InterfaceA) : InterfaceA