<? //

// disabled alias analysis:
// reference assignments have the same immediate effect as normal
// assignments, even if no alias analysis is performed

$x = 1;
$y = $evil;
$x =& $y;
echo $x;



?>
