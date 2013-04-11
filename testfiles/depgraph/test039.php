<? //

// context-sensitivity (CS & func), part 2

$a = $get1;
$b = $get2;

foo($a);
foo($b);

function foo($fp) {
    bar($fp);
}

function bar($bp) {
    echo $bp;
}
?>