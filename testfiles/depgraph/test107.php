<? //

// explicit test for reduction step depgraph -> xssgraph

$a = foo($evil);

$x = 'two';
while ($d) {
    $x .= 'three';
}

while ($c) {
    $a = $a . $x;
}

echo $a;

function foo($fp) {
    return $fp;
}


?>
