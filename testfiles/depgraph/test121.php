<? //

// also execute constructor, this time with ref-assignment

$x =& new MyClass();

class MyClass {

    function MyClass() {
      echo $_GET['x'];
    }

}





?>