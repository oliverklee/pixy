<?php

// interprocedurality check

$x = foo();
$x->bar();

function foo() {
    $f = new MyClass();
    return foo2($f);
}

function foo2($p) {
    return $p;
}

class MyClass {

    function bar() {
        echo $_GET['one'];  // this one is called!
    }

}

class SomeClass {
    function bar() {
        echo $_GET['two'];
    }
}



?>