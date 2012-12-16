<? //

// must-aliasing with globals (1)

$a = 'hi';
foo();
echo $a;
function foo() {
    global $a;
    $f =& $a;
    bar(); 
}
function bar() {
    global $a;
    $a = 'you';
}



?>
