<? //

// simple methods: wrappers around sinks

$f = new Foo;
$f->foo1($evil, "ok");

class Foo extends Bar {
    function foo1($fp1, $fp2) {
        echo $fp1;
        echo $fp2;
        echo $_GET['x'];
    }
}



?>
