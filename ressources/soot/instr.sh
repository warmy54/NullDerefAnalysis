javac -g ./src/*.java -d .
java -cp soot-4.2.1-jar-with-dependencies.jar soot.Main -f J -cp . -pp Version
