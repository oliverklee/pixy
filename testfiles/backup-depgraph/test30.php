<? //

// must-aliasing with formals

foo();
function foo() {
    $f = 'hi';
    bar(&$f); 
    echo $f;
}
function bar(&$bp) {
    $bp = 'you';
}



?>
