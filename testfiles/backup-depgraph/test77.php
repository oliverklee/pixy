<? //

// disabled alias analysis:
// replacing locals with globals during conversion

$x = $evil;
foo();
echo $x;        // has become harmless

function foo() {
   global $x;
   $x = 1; 
}



?>
