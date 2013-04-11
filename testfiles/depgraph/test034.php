<? //

// may-aliasing with formals

foo();
function foo() {
    global $rand;
    $f = 'one';
    $g = 'two';
    if ($rand) {
        $g =& $f;
    }
    bar(&$f);
    echo $g;
}
function bar(&$bp) {
    $bp = 'three';
}
?>