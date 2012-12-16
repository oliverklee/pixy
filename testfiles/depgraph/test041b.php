<? //

// canonical example for enhanced precision of
// functional analysis compared to call-string analysis;
// functional analysis says that this program is OK
// (which it is), call-string analysis reports a false positive;
// this case is explained in the Pixy documentation file
// THIS KIND OF FALSE POSITIVES HAS BEEN ELIMINATED BY MEANS OF
// AN ADDITIONAL MOD-ANALYSIS!



if ($u) {
   $x = 'good';
   echo $x;
   UserValid();
   echo $x;      // FORMER false positive: was regarded as tainted here
} 
UserValid();  

function UserValid()  {
   foo();
}

function foo() {
}



?>
