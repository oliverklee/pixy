<?php

// implicit default constructors must not confuse the analysis

$x = new MyClass();
$x->bar();

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