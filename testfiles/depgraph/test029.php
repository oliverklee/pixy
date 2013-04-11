<? //

// must-aliasing with globals (2)

$a = 'hi';
foo();
function foo() {
    global $a;
    $f =& $a;
    bar();
    echo $f;
}
function bar() {
    global $a;
    $a = 'you';
}
?>