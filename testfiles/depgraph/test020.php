<? //

// simple may-aliasing;
// here we see an interesting effect (which makes the
// result less precise) that has been concealed in the
// original tainted/untainted lattice: the taint graph
// tells us that $b could also have the value 7, which
// is not true; this is because the assignment $a=9 performs
// a lub over all of $a's may-aliases (including $b)
// that is not intelligent enough to remove the first
// assignment from b's taint set;
// can probably be ignored


$a = 7;
if ($rand) {
    $b =& $a;
} else {
    $b = 8;
}
$a = 9;
echo $b;




?>