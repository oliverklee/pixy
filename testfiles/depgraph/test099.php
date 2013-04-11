<? //

// compare the "typical call-string
// limitiation" example with this
// very similar example, which has
// no false positive

foo();
$a = foo();

echo $a;    // false positive

function foo() {
    bar();
    return 3;
}

function bar() {
    return 777;
}


?>