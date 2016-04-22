# om-alarming
Graphing and alarming application for monitoring

This project will continue to be developed in a private repo once stopped here
This project is more for reading than running as the java server files are not available - however the client s/be able to run w/out the server
It has the genesis of certain useful libraries in it, which will later be open sourced - related to what goes on before you see the pixel on the graph

To build and run:
1. `lein clean`
2. `lein deps`
3. from IntelliJ do a complete build - this will give us class files from the simlink-ed java files (easy to forget!!)
4. `client.sh` -> gives the figwheel/browser REPL
5. `server.sh` -> gives a server REPL. You have to `(go)` to get the server to run

All the server side dependencies are in an uber (library non runnable jar that has all 3rd party and config stuff) called smartgas-deps.
After change this jar file need to `lein clean`, `lein deps` - the deps part brings in the fresh jar file. 
