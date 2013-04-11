<?

// collection of not-so-important testcases

/*
// TESTCASE
// isset

$x = new MyClass();
$x = isset($a);
$x->bar();  // not "myclass" any longer

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
*/


/*
// TESTCASE
// builtin call

$x = new MyClass();
$x = max(1,2);
$x->bar();  // not "myclass" any longer

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
*/

/*
// TESTCASE
// array()

$x = new MyClass();
$x = array();
$x->bar();  // not "myclass" any longer

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
*/


/*
// TESTCASE
// unset

$x = new MyClass();
unset($x);
$x->bar();  // not "myclass" any longer

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
*/




/*
// TESTCASE
// assignbinary

$x = new MyClass();
$x = 1 + 2;
$x->bar();  // not "myclass" any longer

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
*/



/*
// TESTCASE
// assignunary

$x = new MyClass();
$x = (string) 1;
$x->bar();  // not "myclass" any longer

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
*/





?>