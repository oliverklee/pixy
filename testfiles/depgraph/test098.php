<? //

// typical call-string limitation
// (really, I'm absolutely sure;
// draw a picture with dep flows!)

$a = foo();
foo();

echo $a;    // false positive (if not using MOD-info)

function foo() {
    bar();
    return 3;
}

function bar() {
    return 777;
}

?>
