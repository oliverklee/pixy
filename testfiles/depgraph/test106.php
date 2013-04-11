<? //

// "global" test that is difficult for
// context-sensitive depgraph construction,
// and that leads to reduced precision

$a = 1;
foo(7);
echo $a;    // should be 7 here

foo(8);

function foo($fp) {
    global $a;
    $a = $fp;
}



?>