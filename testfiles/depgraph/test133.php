<? //

// just an informal, not-automatic test for "unreachable code" warnings;
// generate warnings only for the marked points in the program

bar($y);
foo();      // warn here
$a = 1;
$b = 1;
bar($x);
foo();

function foo() {
    foo();      // warn here
}

function bar($p) {
    echo $p;
}





?>