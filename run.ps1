cd src;
try {
    rm  *.class
    javac -cp "../lib/jline-4.4.3.jar;." .\Main.java
    java --enable-native-access=ALL-UNNAMED -cp "../lib/jline-4.4.3.jar;." Main 
} catch {
    echo $_
} finally {
    cd $PSScriptRoot;
}
    