<? //

// issue a warning for this and handle reasonably (as PHP does),
// but don't stop the analysis

foo($x, $y);

function foo($f1, $f2, $f3, $f4='hei', $f5) {
    echo $f1;
    echo $f2;
    echo $f3;
    echo $f4;
    echo $f5;
}



?>
