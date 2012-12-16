<?php

// "global"
    
foo();
$x->bar();

function foo() {
    global $x;
    $x = new MyClass();
}


class MyClass {
    function bar() {
        echo $_GET['foo'];  // this here is called
    }
}

class SomeClass {
    function bar() {
        echo $_GET['bar'];
    }
}




?>
