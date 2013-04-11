<?php

// disambiguation through other method calls

// evil instantiation that is currently not supported
// by type analysis (i.e., it doesn't know which class it is)
$cname = 'MyClass';
$x = new $cname();

// since method foo is only defined in MyClass,
// $x must be an instance of MyClass...
$x->foo();

// ...and this is why this call also has
// to refer to a method in MyClass
$x->bar();

class MyClass {

    function bar() {
        echo $_GET['one'];  // this one is called!
    }

    function foo() {
    }

}

class SomeClass {

    function bar() {
        echo $_GET['two'];
    }

}




?>