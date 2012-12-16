<? //


// "global" test that is a bit difficult
// for context-sensitive depgraph construction
// (unexpected jump between functions), but
// that does not lead to reduced precision

$a = 1;
foo(7);
echo $a;

function foo($fp) {
    global $a;
    $a = $fp;
}




?>
