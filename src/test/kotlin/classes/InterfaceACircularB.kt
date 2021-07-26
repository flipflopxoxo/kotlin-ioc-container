package classes

import Inject

class InterfaceACircularB @Inject constructor(interfaceB: InterfaceB) : InterfaceA

interface InterfaceB

class InterfaceBImplInterfaceADependent @Inject constructor(interfaceA: InterfaceA) : InterfaceB