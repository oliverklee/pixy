<? //

// context-sensitivity (both CS and functional)

$a = $get1;
$b = $get2;

foo($a);
foo($b);

function foo($fp) {
    echo $fp;
}



?>
