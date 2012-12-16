<? //

// fixed globals mixing due to cs limitation (1)


foo();
$_GET['x'] = 'ok';
$a = 'harmless';
define('MYCONST', 'xyz');
foo();
echo $a;         // harmless
echo $_GET['x']; // ok
echo MYCONST;    // one of two harmless things (not supported yet)

function foo() {
    bar();
}

function bar() {
}



?>
