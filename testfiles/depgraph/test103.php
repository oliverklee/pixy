<? //

// nested function calls

$a = foo(bar('hi'));
echo $a;

function foo($fp) {
    return $fp;
}

function bar($bp) {
    return $bp;
}



?>