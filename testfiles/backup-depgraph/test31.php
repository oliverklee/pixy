<? //

// "global" (2)

$a = 'hi';
foo();
function foo() {
    global $a;
    echo $a;
}



?>
