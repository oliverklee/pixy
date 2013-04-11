<? //

// don't give up method resolution just because of member access

$x->y->bar();

class MyClass {

    function bar() {
        echo $_GET['one'];
    }

}




?>