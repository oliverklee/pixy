<? //

// may-aliasing with globals;
// same effect as the one observed in an earlier example:
// analysis says that the echoed variable might also
// have the value 'one' (imprecise: cannot be)

$a = 'one';
$rand;
foo();
function foo() {
    global $a, $rand;
    $f = 'two';
    if ($rand) {
        $f =& $a;
    }
    bar(); 
    echo $f;
}
function bar() {
    global $a;
    $a = 'three';
}



?>
