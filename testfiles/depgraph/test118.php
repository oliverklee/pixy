<? //


// was a bug that led to an exception

$x = 'harmless';
$y = $evil;
if ($c) {
    $x = $f->get($z);
    $y = $f->get($z);
}
echo $x;
echo $y;





?>