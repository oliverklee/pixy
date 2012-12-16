<? //

// typical call-string limitation
// (really, I'm absolutely sure;
// draw a picture with dep flows!)

$a = foo();
foo();

echo $a;    // false positive

function foo() {
    bar();
    return 3;
}

function bar() {
    return 777;
}

?>
