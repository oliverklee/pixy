<? //

// "global" keyword

$a = 'hi';
foo();
echo $a;
function foo() {
    global $a;
    $a = 'you';
}



?>
