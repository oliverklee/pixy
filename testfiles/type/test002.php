<?php

// using type analysis to disambiguate method calls

//$x = new MyClass();
$x =& new MyClass();
$x->bar();

class MyClass {

    function MyClass() {
    }

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