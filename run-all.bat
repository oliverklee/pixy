set mypath=%~dp0
java -Xmx500m -Xms500m -Dpixy.home="%mypath%\" -classpath "%mypath%lib;%mypath%build\class" at.ac.tuwien.infosys.www.pixy.Checker -a -y xss:sql %*

