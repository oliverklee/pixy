<? //


// in case of duplicate class definitions:
// simply use the first one (and issue a warning)


echo $f->blob();

class Foo {
    function blob() {
        return $_GET['foo_1'];
    }
}

class Foo {
    function blob() {
        return $_GET['foo_2'];
    }
}





?>
