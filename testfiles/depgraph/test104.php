<? //



// function foo has two contexts;
// the second context is unused
// because function bar is never called;
// just for testing whether something
// unexpected happens here

$a = foo('hi');
echo $a;

function foo($fp) {
    return $fp;
}

function bar() {
    foo('from-bar');
}


?>