<? //


// enhanced method resolution using
// $this and static hints

echo Foo::blob();   // Foo
echo Bar::blob();   // Bar
echo $o->gaga();    // Foo
echo $p->blob();    // not resolvable

class Foo {
    function blob() {
        return $_GET['in_foo'];
    }
    function gaga() {
        return $this->blob();
    }
}

class Bar {
    function blob() {
        return $_GET['in_bar'];
    }
}





?>
