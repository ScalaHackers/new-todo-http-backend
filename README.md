todo-akka-backend
=================

This prototype demonstrates REST services by using Akka Actor, Akka HTTP, FSM, Slick. Clustering/FT will be added.

#License
[MIT License](https://opensource.org/licenses/MIT)

=================
for mysql demo, the followings are needed:
1. download mysql 5.7 and install it.
2. create a dbuser of (test, password)
3. create a db of 'tododb'
4. create table of 'todos':
    create table todotxs (id VARCHAR(16) NOT NULL PRIMARY KEY, extid VARCHAR(16),
                        request TEXT, state INT, substate INT, response TEXT,
                        starttime VARCHAR(16), endtime VARCHAR(16));
    request/response could be either real data or reference/url/link to payload.
5. run scala unit testing
6. use postman or curl to test REST interface, for example on windows:
--curl -i -X POST -H "Accept: application/json" -H "Content-Type:application/json" -d "{\"extid\":\"123456\", \"request\":\"a todo\"}" http://localhost:8080/todostxs"


