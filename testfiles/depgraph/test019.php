<? //

// simple aliasing;
// the generated taint graph does not explicity tell us
// about the alias relation; could perhaps be refined
// (if necessary, which I doubt)

$a = 'hello';
$b =& $a;
$a = 'world';
echo $b;



?>