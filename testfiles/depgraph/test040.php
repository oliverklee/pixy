<? //

// context-sensitivity (CS and func), part 3


$a = $get1;
$b = $get2;

foo($a);
foo($b);

function foo($fp) {
    bar($fp);
    bar($fp);
}

function bar($bp) {
    echo $bp;
}
?>