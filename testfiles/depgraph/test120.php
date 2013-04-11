<? //

// also execute constructor;
// also in combination with dynamic includes

new MyClass();
$x = 'test120a.php';
include $x;
new YourClass();

class MyClass {

    function MyClass() {
        echo $_GET['in_myclass'];
    }

}





?>