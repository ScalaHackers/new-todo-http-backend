todo-http-backend
=================

This example demonstrate how to create REST services using Akka HTTP and Slick.

#License
[MIT License](https://opensource.org/licenses/MIT)

=================
for mysql demo, the following are needed:
1. download mysql 5.7 and install it.
2. create a dbuser of (test, password)
3. create a db of 'tododb'
4. create table of 'todos':
    create table todos (id VARCHAR(16) NOT NULL, title VARCHAR(32), completed INT, `order` INT);
