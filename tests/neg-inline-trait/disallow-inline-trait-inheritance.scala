trait Foo1
inline trait Foo2
class Foo3

inline trait Bar1 extends Foo1 // error
inline trait Bar2 extends Foo2 // error
inline trait Bar3 extends Foo3 // error
inline trait Bar4 extends AnyRef
