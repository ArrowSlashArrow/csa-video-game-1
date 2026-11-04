cd src;
try {
    rm  *.class # remove old class files (which would still run when the compile fails)
    $libraries = "../lib/jline-4.4.3.jar;." # declare libraries
    javac -cp $libraries .\Main.java        # compile
    java --enable-native-access=ALL-UNNAMED -cp $libraries -Xss8m Main # run
} catch {
    echo $_ # forward error to stdout
} finally {
    cd $PSScriptRoot;    # return to project root when done
}