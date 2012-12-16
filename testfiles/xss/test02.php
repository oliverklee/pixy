<? //


// $PHP_SELF (and other harmless server vars) was only modelled
// together with SERVER; led to false positives




echo $PHP_SELF;     // dangerous!
echo $_SERVER['PHP_SELF'];  // dangerous!
echo $HTTP_SERVER_VARS['PHP_SELF'];         // dangerous!

echo $_SERVER['EVIL'];      // dangerous!



?>
