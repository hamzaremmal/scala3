//> using options -source future-migration

def foo(implicit x: Int) = ()
val _ = foo(1)
val _ = foo    (1)