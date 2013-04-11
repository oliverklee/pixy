<? //


// our depgraph construction is now context-sensitive!

$a = foo($evil);
$b = foo('good');

echo $a;    // vuln
echo $b;    // ok


function foo($p) {
    echo 'hei!';    // ok
    echo $p;        // vuln
    return $p;
}


?>