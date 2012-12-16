<? //

// recursive calls pose no problem to our
// depgraph construction algorithm

foo($a);

function foo($p) {
    if ($p) {
        foo($p);
    }
    echo $p;
}


?>
